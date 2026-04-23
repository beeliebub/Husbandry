package me.beeliebub.husbandry.crop;

import me.beeliebub.husbandry.item.HusbandryItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.Plugin;

import java.util.Set;

/**
 * Handles crafting interactions:
 * <ul>
 *   <li>Pumpkin/melon → seeds: transfers crop_traits (minus Quality) and quality_crop marker</li>
 *   <li>Quality feed recipes: validates quality_crop tags and full stacks, consumes stacks</li>
 * </ul>
 */
public class CropCraftListener implements Listener {

    private static final Set<String> QUALITY_FEED_RECIPES = Set.of(
            "quality_wheat_feed", "quality_carrot_feed", "quality_golden_carrot_feed",
            "quality_seed_feed_pumpkin", "quality_seed_feed_melon"
    );

    private final Plugin plugin;
    private final CropData cropData;
    private final HusbandryItems husbandryItems;

    public CropCraftListener(Plugin plugin, CropData cropData, HusbandryItems husbandryItems) {
        this.plugin = plugin;
        this.cropData = cropData;
        this.husbandryItems = husbandryItems;
    }

    // ── Prepare crafting: validate quality feed and transfer seed traits ──

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        CraftingInventory inv = event.getInventory();

        // Handle quality feed recipe validation
        if (recipe instanceof ShapelessRecipe shapeless && isQualityFeedRecipe(shapeless.getKey())) {
            validateQualityFeed(inv);
            return;
        }

        // Handle pumpkin/melon → seeds trait transfer
        handleSeedCraftPreview(inv);
    }

    // ── Craft event: consume full stacks for quality feed ──────────

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        Recipe recipe = event.getRecipe();

        if (recipe instanceof ShapelessRecipe shapeless && isQualityFeedRecipe(shapeless.getKey())) {
            // Prevent shift-click bulk crafting (too complex with stack consumption)
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }

            // Consume all ingredients (full stacks) on next tick
            CraftingInventory inv = event.getInventory();
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                for (int i = 1; i <= 9; i++) {
                    inv.setItem(i, null);
                }
            });
            return;
        }

        // Handle pumpkin/melon → seeds trait transfer on actual craft
        handleSeedCraft(event);
    }

    // ── Quality feed validation ────────────────────────────────────

    private void validateQualityFeed(CraftingInventory inv) {
        ItemStack[] matrix = inv.getMatrix();
        for (ItemStack ingredient : matrix) {
            if (ingredient == null || ingredient.getType() == Material.AIR) continue;
            // Gold ingots don't need quality tag
            if (ingredient.getType() == Material.GOLD_INGOT) {
                if (ingredient.getAmount() < 64) {
                    inv.setResult(null);
                    return;
                }
                continue;
            }
            if (ingredient.getAmount() < 64 || !cropData.isQualityCrop(ingredient)) {
                inv.setResult(null);
                return;
            }
        }
        // All ingredients valid - result stays as registered
    }

    // ── Seed craft trait transfer ───────────────────────────────────

    private void handleSeedCraftPreview(CraftingInventory inv) {
        ItemStack result = inv.getResult();
        if (result == null) return;

        Material resultType = result.getType();
        if (resultType != Material.PUMPKIN_SEEDS && resultType != Material.MELON_SEEDS) return;

        // Find the source item (pumpkin or melon slice) in the matrix
        ItemStack source = findTraitedSource(inv.getMatrix());
        if (source == null) return;

        // Create modified result with transferred traits
        ItemStack newResult = result.clone();
        cropData.copyTraitsToSeeds(source, newResult);
        inv.setResult(newResult);
    }

    private void handleSeedCraft(CraftItemEvent event) {
        ItemStack result = event.getCurrentItem();
        if (result == null) return;

        Material resultType = result.getType();
        if (resultType != Material.PUMPKIN_SEEDS && resultType != Material.MELON_SEEDS) return;

        ItemStack source = findTraitedSource(event.getInventory().getMatrix());
        if (source == null) return;

        // The PrepareItemCraftEvent already set the result with traits,
        // but we confirm here for safety
        cropData.copyTraitsToSeeds(source, result);
    }

    private ItemStack findTraitedSource(ItemStack[] matrix) {
        for (ItemStack item : matrix) {
            if (item == null || item.getType() == Material.AIR) continue;
            if (CropData.isFruitItem(item.getType()) && cropData.hasAnyCropData(item)) {
                return item;
            }
        }
        return null;
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private boolean isQualityFeedRecipe(NamespacedKey key) {
        return key.getNamespace().equals(plugin.getName().toLowerCase())
                && QUALITY_FEED_RECIPES.contains(key.getKey());
    }
}