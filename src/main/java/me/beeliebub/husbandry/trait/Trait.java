package me.beeliebub.husbandry.trait;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Every trait an animal can possess. Each enum constant carries its own rarity,
 * inheritance behaviour, display name, and a set of attribute modifiers that
 * are applied to the entity while the trait is active.
 *
 * <h3>Adding a new trait</h3>
 * <ol>
 *   <li>Add a constant here, passing rarity, inheritance type, and display name.</li>
 *   <li>In the static block, call {@code addModifier} to attach attribute modifiers.</li>
 *   <li>If the trait needs custom logic beyond modifiers, extend {@code TraitApplier}.</li>
 * </ol>
 */
public enum Trait {

    // ── Basic examples ──────────────────────────────────────────────
    STURDY(TraitRarity.BASIC, InheritanceType.DOMINANT, "Sturdy"),
    TIMID(TraitRarity.BASIC, InheritanceType.RECESSIVE, "Timid"),
    GENTLE(TraitRarity.BASIC, InheritanceType.SPECIAL, "Gentle"),

    // ── Rare examples ───────────────────────────────────────────────
    SWIFT(TraitRarity.RARE, InheritanceType.DOMINANT, "Swift"),
    FRAGILE(TraitRarity.RARE, InheritanceType.RECESSIVE, "Fragile"),
    GLOWING(TraitRarity.RARE, InheritanceType.SPECIAL, "Glowing"),

    // ── Legendary examples ──────────────────────────────────────────
    IRONHIDE(TraitRarity.LEGENDARY, InheritanceType.DOMINANT, "Ironhide"),
    ETHEREAL(TraitRarity.LEGENDARY, InheritanceType.RECESSIVE, "Ethereal"),
    CURSED(TraitRarity.LEGENDARY, InheritanceType.SPECIAL, "Cursed");

    // ── Static modifier registry ────────────────────────────────────
    private static final Map<Trait, List<ModifierEntry>> MODIFIER_MAP = new EnumMap<>(Trait.class);

    static {
        // Example: STURDY grants +4 max health
        addModifier(STURDY, Attribute.MAX_HEALTH, 4.0, AttributeModifier.Operation.ADD_NUMBER);

        // Example: SWIFT grants +0.03 movement speed
        addModifier(SWIFT, Attribute.MOVEMENT_SPEED, 0.03, AttributeModifier.Operation.ADD_NUMBER);

        // Example: IRONHIDE grants +8 armor
        addModifier(IRONHIDE, Attribute.ARMOR, 8.0, AttributeModifier.Operation.ADD_NUMBER);
    }

    // ── Instance fields ─────────────────────────────────────────────
    private final TraitRarity rarity;
    private final InheritanceType inheritanceType;
    private final String displayName;

    Trait(TraitRarity rarity, InheritanceType inheritanceType, String displayName) {
        this.rarity = rarity;
        this.inheritanceType = inheritanceType;
        this.displayName = displayName;
    }

    // ── Getters ─────────────────────────────────────────────────────

    public TraitRarity getRarity() {
        return rarity;
    }

    public InheritanceType getInheritanceType() {
        return inheritanceType;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Returns an unmodifiable list of attribute modifiers this trait applies. */
    public List<ModifierEntry> getModifiers() {
        return Collections.unmodifiableList(MODIFIER_MAP.getOrDefault(this, List.of()));
    }

    // ── Static helpers ──────────────────────────────────────────────

    /** Register an attribute modifier for a trait. Called in the static initializer. */
    private static void addModifier(Trait trait, Attribute attribute, double amount,
                                    AttributeModifier.Operation operation) {
        MODIFIER_MAP.computeIfAbsent(trait, k -> new ArrayList<>())
                .add(new ModifierEntry(attribute, amount, operation));
    }

    /**
     * Returns all traits matching the given rarity.
     */
    public static List<Trait> byRarity(TraitRarity rarity) {
        List<Trait> result = new ArrayList<>();
        for (Trait t : values()) {
            if (t.rarity == rarity) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * Looks up a trait by name (case-insensitive). Returns null if not found.
     */
    public static @Nullable Trait fromName(String name) {
        for (Trait t : values()) {
            if (t.name().equalsIgnoreCase(name)) {
                return t;
            }
        }
        return null;
    }

    // ── Inner record ────────────────────────────────────────────────

    /**
     * Holds the data needed to construct an {@link AttributeModifier} for an entity.
     */
    public record ModifierEntry(Attribute attribute, double amount,
                                AttributeModifier.Operation operation) {
    }
}