package me.beeliebub.husbandry.trait;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Animals;
import org.bukkit.plugin.Plugin;

import java.util.Set;

/**
 * Applies and removes the attribute-modifier effects of traits on living entities.
 * Each modifier uses a deterministic {@link NamespacedKey} derived from the trait name,
 * so modifiers are idempotent and safely removable.
 *
 * <p>Extend this class to add non-modifier effects (particles, AI changes, etc.)
 * by overriding {@link #applyCustomEffect} and {@link #removeCustomEffect}.</p>
 */
public class TraitApplier {

    private final Plugin plugin;

    public TraitApplier(Plugin plugin) {
        this.plugin = plugin;
    }

    /** Applies all modifier effects for the given traits to the animal. */
    public void applyAll(Animals animal, Set<Trait> traits) {
        for (Trait trait : traits) {
            apply(animal, trait);
        }
    }

    /** Removes all modifier effects for the given traits from the animal. */
    public void removeAll(Animals animal, Set<Trait> traits) {
        for (Trait trait : traits) {
            remove(animal, trait);
        }
    }

    /** Applies a single trait's modifiers to the animal. */
    public void apply(Animals animal, Trait trait) {
        for (Trait.ModifierEntry entry : trait.getModifiers()) {
            AttributeInstance instance = animal.getAttribute(entry.attribute());
            if (instance == null) continue;

            NamespacedKey key = modifierKey(trait, entry);
            // Remove existing modifier with same key to avoid stacking
            instance.removeModifier(key);
            instance.addModifier(new AttributeModifier(key, entry.amount(), entry.operation()));
        }
        applyCustomEffect(animal, trait);
    }

    /** Removes a single trait's modifiers from the animal. */
    public void remove(Animals animal, Trait trait) {
        for (Trait.ModifierEntry entry : trait.getModifiers()) {
            AttributeInstance instance = animal.getAttribute(entry.attribute());
            if (instance == null) continue;
            instance.removeModifier(modifierKey(trait, entry));
        }
        removeCustomEffect(animal, trait);
    }

    /**
     * Override to add custom non-modifier effects (particles, glow, AI, etc.).
     * Called after attribute modifiers are applied.
     */
    protected void applyCustomEffect(Animals animal, Trait trait) {
        // Skeleton - override in subclass for trait-specific behaviour
    }

    /**
     * Override to remove custom non-modifier effects.
     * Called after attribute modifiers are removed.
     */
    protected void removeCustomEffect(Animals animal, Trait trait) {
        // Skeleton - override in subclass for trait-specific behaviour
    }

    private NamespacedKey modifierKey(Trait trait, Trait.ModifierEntry entry) {
        return new NamespacedKey(plugin,
                trait.name().toLowerCase() + "_" + entry.attribute().key().value());
    }
}