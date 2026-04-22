package me.beeliebub.husbandry.trait;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

/**
 * Defines how rare a trait is, affecting its likelihood of being
 * randomly assigned to an animal on first encounter.
 */
public enum TraitRarity {

    BASIC(0.60, NamedTextColor.GRAY),
    RARE(0.30, NamedTextColor.AQUA),
    LEGENDARY(0.10, NamedTextColor.GOLD);

    private final double weight;
    private final TextColor color;

    TraitRarity(double weight, TextColor color) {
        this.weight = weight;
        this.color = color;
    }

    /** Relative probability weight used when randomly assigning traits. */
    public double getWeight() {
        return weight;
    }

    /** Display color associated with this rarity tier. */
    public TextColor getColor() {
        return color;
    }
}