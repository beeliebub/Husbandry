package me.beeliebub.husbandry.crop;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles all crop-related block events:
 * <ul>
 *   <li>Mature crop harvest: initial trait rolls on non-traited crops, trait effects</li>
 *   <li>Immature crop harvest: returns traited seed or nothing (Barren), cleans up PDC</li>
 *   <li>Stem harvest: applies traits to seed drops or clears them (Barren)</li>
 *   <li>Fruit harvest: reads traits from fruit block's chunk PDC (tagged on grow)</li>
 *   <li>Fruit growth: copies stem traits to fruit block's chunk PDC via BlockGrowEvent</li>
 * </ul>
 */
public class CropHarvestListener implements Listener {

    private static final BlockFace[] HORIZONTAL = {
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST
    };

    private final Plugin plugin;
    private final CropData cropData;

    public CropHarvestListener(Plugin plugin, CropData cropData) {
        this.plugin = plugin;
        this.cropData = cropData;
    }

    // ── Block grow: tag fruit blocks when they grow from stems ─────

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        Material newType = event.getNewState().getType();
        if (!CropData.isFruitBlock(newType)) return;

        Block fruitBlock = event.getBlock();
        Block stem = findAdjacentStem(fruitBlock, newType);
        if (stem == null) return;

        Set<CropTrait> stemTraits = cropData.getBlockTraits(stem);
        boolean qualityStem = cropData.isQualityStem(stem);

        if (stemTraits.isEmpty() && !qualityStem) return;

        // Build fruit traits from stem
        Set<CropTrait> fruitTraits = stemTraits.isEmpty()
                ? EnumSet.noneOf(CropTrait.class)
                : EnumSet.copyOf(stemTraits);

        // Quality trait on stem is one-shot: consume from stem, add to fruit
        if (stemTraits.contains(CropTrait.QUALITY)) {
            Set<CropTrait> updatedStemTraits = EnumSet.copyOf(stemTraits);
            updatedStemTraits.remove(CropTrait.QUALITY);
            if (updatedStemTraits.isEmpty()) {
                cropData.removeBlockTraits(stem);
            } else {
                cropData.setBlockTraits(stem, updatedStemTraits);
            }
        }

        // quality_stem flag also adds Quality to fruit and is consumed
        if (qualityStem) {
            fruitTraits.add(CropTrait.QUALITY);
            cropData.setQualityStem(stem, false);
        }

        // Store traits on the fruit block's location in chunk PDC
        if (!fruitTraits.isEmpty()) {
            cropData.setBlockTraits(fruitBlock, fruitTraits);
        }
    }

    // ── Block drop: handle crops, fruits, and stems ────────────────

    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent event) {
        Material blockType = event.getBlockState().getType();

        if (CropData.isCropBlock(blockType)) {
            handleCropHarvest(event);
        } else if (CropData.isFruitBlock(blockType)) {
            handleFruitHarvest(event);
        } else if (CropData.isStemBlock(blockType)) {
            handleStemHarvest(event);
        }
    }

    // ── Regular crop harvest (wheat, carrots, potatoes, beetroots) ──

    private void handleCropHarvest(BlockDropItemEvent event) {
        Block block = event.getBlock();
        Set<CropTrait> blockTraits = cropData.getBlockTraits(block);

        boolean isFullyGrown = true;
        BlockData data = event.getBlockState().getBlockData();
        if (data instanceof Ageable ageable) {
            isFullyGrown = ageable.getAge() >= ageable.getMaximumAge();
        }

        if (!blockTraits.isEmpty()) {
            if (isFullyGrown) {
                handleTraitedHarvest(event, blockTraits);
            } else {
                handleImmatureTraitedHarvest(event, blockTraits);
            }
        } else if (isFullyGrown) {
            handleInitialRoll(event);
        }
        // Immature + no traits = vanilla behavior, no action needed

        // Always clean up chunk PDC for this block
        if (!blockTraits.isEmpty()) {
            cropData.removeBlockTraits(block);
        }
    }

    // ── Fruit harvest (pumpkin, melon) ─────────────────────────────

    private void handleFruitHarvest(BlockDropItemEvent event) {
        Block block = event.getBlock();
        Set<CropTrait> fruitTraits = cropData.getBlockTraits(block);

        if (!fruitTraits.isEmpty()) {
            // Fruit was tagged via BlockGrowEvent — use its own traits
            handleTraitedHarvest(event, fruitTraits);
            cropData.removeBlockTraits(block);
        } else {
            // Fallback: check adjacent stem (for fruit that existed before tagging)
            Material blockType = event.getBlockState().getType();
            Block stem = findAdjacentStem(block, blockType);

            if (stem == null) return;

            Set<CropTrait> stemTraits = cropData.getBlockTraits(stem);
            boolean qualityStem = cropData.isQualityStem(stem);

            if (stemTraits.isEmpty() && !qualityStem) {
                handleInitialRoll(event);
            } else {
                // Build effective traits including quality_stem
                Set<CropTrait> effectiveTraits = stemTraits.isEmpty()
                        ? EnumSet.noneOf(CropTrait.class)
                        : EnumSet.copyOf(stemTraits);
                if (qualityStem) {
                    effectiveTraits.add(CropTrait.QUALITY);
                }

                handleTraitedHarvest(event, effectiveTraits);

                // Consume one-shot quality from stem
                if (stemTraits.contains(CropTrait.QUALITY)) {
                    Set<CropTrait> updated = EnumSet.copyOf(stemTraits);
                    updated.remove(CropTrait.QUALITY);
                    if (updated.isEmpty()) {
                        cropData.removeBlockTraits(stem);
                    } else {
                        cropData.setBlockTraits(stem, updated);
                    }
                }
                if (qualityStem) {
                    cropData.setQualityStem(stem, false);
                }
            }
        }
    }

    // ── Stem harvest ───────────────────────────────────────────────

    private void handleStemHarvest(BlockDropItemEvent event) {
        Block block = event.getBlock();
        Set<CropTrait> stemTraits = cropData.getBlockTraits(block);
        boolean qualityStem = cropData.isQualityStem(block);

        if (stemTraits.isEmpty() && !qualityStem) return;

        // Barren: drop nothing
        if (stemTraits.contains(CropTrait.BARREN)) {
            event.getItems().clear();
        } else {
            // Apply traits to dropped seeds
            for (Item item : event.getItems()) {
                ItemStack stack = item.getItemStack();
                if (CropData.isPlantableItem(stack.getType())) {
                    cropData.applyToItem(stack, stemTraits, false,
                            CropData.materialDisplayName(stack.getType()));
                    item.setItemStack(stack);
                }
            }
        }

        // Clean up chunk PDC
        cropData.removeBlockTraits(block);
        if (qualityStem) {
            cropData.setQualityStem(block, false);
        }
    }

    // ── Immature traited crop harvest ──────────────────────────────

    private void handleImmatureTraitedHarvest(BlockDropItemEvent event, Set<CropTrait> traits) {
        // Barren takes precedence: drop nothing at all
        if (traits.contains(CropTrait.BARREN)) {
            event.getItems().clear();
            return;
        }

        // Apply traits to the seed drops (return traited seeds)
        for (Item item : event.getItems()) {
            ItemStack stack = item.getItemStack();
            if (CropData.isPlantableItem(stack.getType())) {
                stack.setAmount(1);
                cropData.applyToItem(stack, traits, false,
                        CropData.materialDisplayName(stack.getType()));
                item.setItemStack(stack);
            }
        }
    }

    // ── Initial trait roll (mature non-traited crop) ───────────────

    private void handleInitialRoll(BlockDropItemEvent event) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        FileConfiguration config = plugin.getConfig();

        double barrenChance = config.getDouble("crop-traits.barren-chance", 15) / 100.0;
        double bountifulChance = config.getDouble("crop-traits.bountiful-chance", 10) / 100.0;
        double qualityChance = config.getDouble("crop-traits.quality-chance", 5) / 100.0;

        // Roll each trait independently
        Set<CropTrait> rolledTraits = EnumSet.noneOf(CropTrait.class);
        if (rng.nextDouble() < barrenChance) rolledTraits.add(CropTrait.BARREN);
        if (rng.nextDouble() < bountifulChance) rolledTraits.add(CropTrait.BOUNTIFUL);
        if (rng.nextDouble() < qualityChance) rolledTraits.add(CropTrait.QUALITY);

        if (rolledTraits.isEmpty()) return;

        // Apply rolled traits ONLY to plantable items and fruit items.
        // Non-plantable items (wheat, beetroot) never get traits from initial rolls.
        for (Item item : event.getItems()) {
            ItemStack stack = item.getItemStack();
            Material itemMat = stack.getType();

            if (CropData.isPlantableItem(itemMat) || CropData.isFruitItem(itemMat)) {
                cropData.applyToItem(stack, rolledTraits, false,
                        CropData.materialDisplayName(itemMat));
                item.setItemStack(stack);
            }
        }
    }

    // ── Traited crop harvest (mature) ──────────────────────────────

    private void handleTraitedHarvest(BlockDropItemEvent event, Set<CropTrait> traits) {
        // Barren: produce nothing
        if (traits.contains(CropTrait.BARREN)) {
            event.getItems().clear();
            return;
        }

        boolean hasQuality = traits.contains(CropTrait.QUALITY);

        // Build inheritable traits (everything except Quality — Quality does not pass down)
        Set<CropTrait> inheritableTraits = EnumSet.noneOf(CropTrait.class);
        for (CropTrait t : traits) {
            if (t != CropTrait.QUALITY) inheritableTraits.add(t);
        }

        for (Item item : event.getItems()) {
            ItemStack stack = item.getItemStack();
            Material itemMat = stack.getType();

            // Bountiful: double yield (stacks with fortune since fortune already applied)
            if (traits.contains(CropTrait.BOUNTIFUL)) {
                stack.setAmount(stack.getAmount() * 2);
            }

            if (CropData.isPlantableItem(itemMat) || CropData.isFruitItem(itemMat)) {
                // Plantable / fruit items:
                //   - Inherit traits in crop_traits (minus Quality)
                //   - Get quality_crop marker if crop had Quality (for crafting use)
                if (!inheritableTraits.isEmpty() || hasQuality) {
                    cropData.applyToItem(stack, inheritableTraits, hasQuality,
                            CropData.materialDisplayName(itemMat));
                }
            } else if (hasQuality) {
                // Non-plantable items (wheat, beetroot):
                //   - Get quality_crop marker only (quality crafting ingredient)
                cropData.applyToItem(stack, EnumSet.noneOf(CropTrait.class), true,
                        CropData.materialDisplayName(itemMat));
            }

            item.setItemStack(stack);
        }
    }

    // ── Stem finding ───────────────────────────────────────────────

    private Block findAdjacentStem(Block fruitBlock, Material fruitType) {
        Material stemMat = fruitType == Material.PUMPKIN
                ? Material.PUMPKIN_STEM : Material.MELON_STEM;
        Material attachedStemMat = fruitType == Material.PUMPKIN
                ? Material.ATTACHED_PUMPKIN_STEM : Material.ATTACHED_MELON_STEM;

        for (BlockFace face : HORIZONTAL) {
            Block neighbor = fruitBlock.getRelative(face);
            Material neighborType = neighbor.getType();
            if (neighborType == stemMat || neighborType == attachedStemMat) {
                return neighbor;
            }
        }
        return null;
    }
}