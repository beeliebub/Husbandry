package me.beeliebub.husbandry.crop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Manages crop trait data on ItemStacks (PDC tags + display) and in chunk PDC
 * for planted crops and stems.
 *
 * <h3>Item PDC layout</h3>
 * <ul>
 *   <li>{@code husbandry:crop_traits} (STRING) - comma-separated CropTrait names for planting.
 *       CAN include QUALITY (the Quality trait causes the crop to produce quality items
 *       on harvest, but is not inherited to the next generation).</li>
 *   <li>{@code husbandry:quality_crop} (BYTE 1) - marks item as a quality crop for crafting.
 *       This is separate from the Quality trait — it denotes the item itself is a quality
 *       crafting ingredient (e.g., for Quality Feed recipes).</li>
 * </ul>
 *
 * <h3>Chunk PDC layout</h3>
 * <ul>
 *   <li>{@code husbandry:crop_data} (STRING) - semicolon-separated entries of "x,y,z:TRAIT1,TRAIT2"
 *       for planted crop traits at world coordinates.</li>
 *   <li>{@code husbandry:quality_stems} (STRING) - semicolon-separated "x,y,z" entries
 *       for stems that should produce one quality fruit.</li>
 * </ul>
 */
public final class CropData {

    private final NamespacedKey cropTraitsKey;
    private final NamespacedKey qualityCropKey;
    private final NamespacedKey cropDataKey;
    private final NamespacedKey qualityStemsKey;

    public CropData(Plugin plugin) {
        this.cropTraitsKey = new NamespacedKey(plugin, "crop_traits");
        this.qualityCropKey = new NamespacedKey(plugin, "quality_crop");
        this.cropDataKey = new NamespacedKey(plugin, "crop_data");
        this.qualityStemsKey = new NamespacedKey(plugin, "quality_stems");
    }

    // ── Item PDC methods ───────────────────────────────────────────

    /** Returns the set of planting traits on an item (can include QUALITY). */
    public Set<CropTrait> getTraits(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return EnumSet.noneOf(CropTrait.class);
        String raw = item.getItemMeta().getPersistentDataContainer()
                .get(cropTraitsKey, PersistentDataType.STRING);
        return decodeTraits(raw);
    }

    /** Returns true if the item has the quality_crop crafting marker. */
    public boolean isQualityCrop(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(qualityCropKey, PersistentDataType.BYTE);
    }

    /** Returns true if the item has any crop trait data (crop_traits or quality_crop). */
    public boolean hasAnyCropData(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(cropTraitsKey, PersistentDataType.STRING)
                || pdc.has(qualityCropKey, PersistentDataType.BYTE);
    }

    /**
     * Applies crop traits and/or quality crop marker to an item, updating its PDC and display.
     * Traits are stored as-is in crop_traits (including QUALITY if present).
     * The quality_crop marker is independent of the Quality trait.
     *
     * @param item            the item to modify
     * @param traits          planting traits to set (can include QUALITY)
     * @param qualityCrop     whether to mark as a quality crop (crafting marker)
     * @param displayName     material display name for quality crop name prefix
     */
    public void applyToItem(ItemStack item, Set<CropTrait> traits, boolean qualityCrop,
                            String displayName) {
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (!traits.isEmpty()) {
            pdc.set(cropTraitsKey, PersistentDataType.STRING, encodeTraits(traits));
        }

        if (qualityCrop) {
            pdc.set(qualityCropKey, PersistentDataType.BYTE, (byte) 1);
            meta.setEnchantmentGlintOverride(true);
        }

        // Build display
        List<Component> lore = new ArrayList<>();
        for (CropTrait trait : traits) {
            lore.add(Component.text(trait.getDisplayName(), trait.getRarity().getColor())
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (qualityCrop) {
            // Quality crop marker shown distinctly from Quality trait
            lore.add(Component.text("Quality Crop", NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
            meta.displayName(Component.text("Quality " + displayName, NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }

        item.setItemMeta(meta);
    }

    /**
     * Copies crop trait data from a source item (pumpkin/melon) to seeds.
     * All traits (including Quality) are passed through as-is in crop_traits.
     * The quality_crop crafting marker is passed through independently.
     * Quality trait on seeds means the planted crop will produce quality items;
     * quality_crop on seeds is only for crafting (e.g., Quality Feed recipes).
     */
    public void copyTraitsToSeeds(ItemStack source, ItemStack dest) {
        Set<CropTrait> sourceTraits = getTraits(source);
        boolean sourceQualityCrop = isQualityCrop(source);

        if (sourceTraits.isEmpty() && !sourceQualityCrop) return;
        applyToItem(dest, sourceTraits, sourceQualityCrop, materialDisplayName(dest.getType()));
    }

    // ── Chunk PDC methods (planted crops) ──────────────────────────

    /** Gets the crop traits stored at a block location in the chunk PDC. */
    public Set<CropTrait> getBlockTraits(Block block) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        String data = pdc.get(cropDataKey, PersistentDataType.STRING);
        if (data == null || data.isEmpty()) return EnumSet.noneOf(CropTrait.class);

        String key = blockKey(block);
        for (String entry : data.split(";")) {
            if (entry.isEmpty()) continue;
            String[] parts = entry.split(":", 2);
            if (parts.length == 2 && parts[0].equals(key)) {
                return decodeTraits(parts[1]);
            }
        }
        return EnumSet.noneOf(CropTrait.class);
    }

    /** Returns true if the block location has any crop traits stored. */
    public boolean hasBlockTraits(Block block) {
        return !getBlockTraits(block).isEmpty();
    }

    /** Sets crop traits at a block location in the chunk PDC. */
    public void setBlockTraits(Block block, Set<CropTrait> traits) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        String data = pdc.getOrDefault(cropDataKey, PersistentDataType.STRING, "");

        String key = blockKey(block);
        StringBuilder newData = new StringBuilder();

        // Copy existing entries except for this block
        for (String entry : data.split(";")) {
            if (entry.isEmpty()) continue;
            String[] parts = entry.split(":", 2);
            if (parts.length == 2 && parts[0].equals(key)) continue;
            if (!newData.isEmpty()) newData.append(';');
            newData.append(entry);
        }

        // Add new entry
        if (!traits.isEmpty()) {
            if (!newData.isEmpty()) newData.append(';');
            newData.append(key).append(':').append(encodeTraits(traits));
        }

        if (newData.isEmpty()) {
            pdc.remove(cropDataKey);
        } else {
            pdc.set(cropDataKey, PersistentDataType.STRING, newData.toString());
        }
    }

    /** Removes crop trait data at a block location. */
    public void removeBlockTraits(Block block) {
        setBlockTraits(block, EnumSet.noneOf(CropTrait.class));
    }

    // ── Quality stem methods ───────────────────────────────────────

    /** Returns true if the block is marked as a quality stem. */
    public boolean isQualityStem(Block block) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        String data = pdc.get(qualityStemsKey, PersistentDataType.STRING);
        if (data == null || data.isEmpty()) return false;

        String key = blockKey(block);
        for (String entry : data.split(";")) {
            if (entry.equals(key)) return true;
        }
        return false;
    }

    /** Marks a block as a quality stem. */
    public void setQualityStem(Block block, boolean quality) {
        Chunk chunk = block.getChunk();
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        String data = pdc.getOrDefault(qualityStemsKey, PersistentDataType.STRING, "");

        String key = blockKey(block);
        StringBuilder newData = new StringBuilder();

        // Copy existing entries except for this block
        for (String entry : data.split(";")) {
            if (entry.isEmpty() || entry.equals(key)) continue;
            if (!newData.isEmpty()) newData.append(';');
            newData.append(entry);
        }

        if (quality) {
            if (!newData.isEmpty()) newData.append(';');
            newData.append(key);
        }

        if (newData.isEmpty()) {
            pdc.remove(qualityStemsKey);
        } else {
            pdc.set(qualityStemsKey, PersistentDataType.STRING, newData.toString());
        }
    }

    // ── Crop type helpers ──────────────────────────────────────────

    /** Returns true if the material is a supported crop block. */
    public static boolean isCropBlock(Material material) {
        return switch (material) {
            case WHEAT, CARROTS, POTATOES, BEETROOTS -> true;
            default -> false;
        };
    }

    /** Returns true if the material is a pumpkin or melon stem. */
    public static boolean isStemBlock(Material material) {
        return switch (material) {
            case PUMPKIN_STEM, MELON_STEM, ATTACHED_PUMPKIN_STEM, ATTACHED_MELON_STEM -> true;
            default -> false;
        };
    }

    /** Returns true if the material is a pumpkin or melon fruit block. */
    public static boolean isFruitBlock(Material material) {
        return material == Material.PUMPKIN || material == Material.MELON;
    }

    /** Returns true if the material is a plantable crop item (seed or plantable food). */
    public static boolean isPlantableItem(Material material) {
        return switch (material) {
            case WHEAT_SEEDS, BEETROOT_SEEDS, PUMPKIN_SEEDS, MELON_SEEDS,
                 CARROT, POTATO -> true;
            default -> false;
        };
    }

    /** Returns true if the material is a pumpkin or melon slice (craftable into seeds). */
    public static boolean isFruitItem(Material material) {
        return material == Material.PUMPKIN || material == Material.MELON_SLICE;
    }

    /** Returns a display name for a material. */
    public static String materialDisplayName(Material material) {
        String[] words = material.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    // ── Serialization helpers ──────────────────────────────────────

    private static String blockKey(Block block) {
        return block.getX() + "," + block.getY() + "," + block.getZ();
    }

    private static String encodeTraits(Set<CropTrait> traits) {
        if (traits.isEmpty()) return "";
        List<String> names = new ArrayList<>(traits.size());
        for (CropTrait t : traits) {
            names.add(t.name());
        }
        return String.join(",", names);
    }

    static Set<CropTrait> decodeTraits(@Nullable String raw) {
        Set<CropTrait> result = EnumSet.noneOf(CropTrait.class);
        if (raw == null || raw.isBlank()) return result;
        for (String name : raw.split(",")) {
            CropTrait t = CropTrait.fromName(name.trim());
            if (t != null) {
                result.add(t);
            }
        }
        return result;
    }
}