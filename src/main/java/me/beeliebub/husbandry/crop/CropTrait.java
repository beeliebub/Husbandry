package me.beeliebub.husbandry.crop;

import me.beeliebub.husbandry.trait.TraitRarity;
import org.jetbrains.annotations.Nullable;

/**
 * Traits that can be applied to crops. Each trait has a rarity that determines
 * its likelihood of being rolled when harvesting non-traited crops.
 */
public enum CropTrait {

    BARREN(TraitRarity.BASIC, "Barren", "Produces nothing on harvest"),
    BOUNTIFUL(TraitRarity.RARE, "Bountiful", "Doubles yield on harvest"),
    QUALITY(TraitRarity.LEGENDARY, "Quality", "Produces quality crops");

    private final TraitRarity rarity;
    private final String displayName;
    private final String description;

    CropTrait(TraitRarity rarity, String displayName, String description) {
        this.rarity = rarity;
        this.displayName = displayName;
        this.description = description;
    }

    public TraitRarity getRarity() {
        return rarity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /** Looks up a crop trait by name (case-insensitive). Returns null if not found. */
    public static @Nullable CropTrait fromName(String name) {
        for (CropTrait t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }
}