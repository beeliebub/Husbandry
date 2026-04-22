package me.beeliebub.husbandry.trait;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Animals;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import java.util.Set;

/**
 * Applies and removes the effects of traits on living entities.
 * Handles both attribute modifiers (via deterministic {@link NamespacedKey}) and
 * potion effects (permanent, no particles).
 */
public class TraitApplier {

    private final Plugin plugin;

    public TraitApplier(Plugin plugin) {
        this.plugin = plugin;
    }

    /** Applies all effects for the given traits to the animal. */
    public void applyAll(Animals animal, Set<Trait> traits) {
        for (Trait trait : traits) {
            apply(animal, trait);
        }
    }

    /** Removes all effects for the given traits from the animal. */
    public void removeAll(Animals animal, Set<Trait> traits) {
        for (Trait trait : traits) {
            remove(animal, trait);
        }
    }

    /** Applies a single trait's attribute modifiers and potion effects to the animal. */
    public void apply(Animals animal, Trait trait) {
        for (Trait.ModifierEntry entry : trait.getModifiers()) {
            AttributeInstance instance = animal.getAttribute(entry.attribute());
            if (instance == null) continue;

            NamespacedKey key = modifierKey(trait, entry);
            instance.removeModifier(key);
            instance.addModifier(new AttributeModifier(key, entry.amount(), entry.operation()));
        }

        for (Trait.PotionEntry entry : trait.getPotionEffects()) {
            animal.addPotionEffect(new PotionEffect(
                    entry.type(),
                    PotionEffect.INFINITE_DURATION,
                    entry.amplifier(),
                    false,  // ambient
                    false,  // particles
                    true    // icon
            ));
        }
    }

    /** Removes a single trait's attribute modifiers and potion effects from the animal. */
    public void remove(Animals animal, Trait trait) {
        for (Trait.ModifierEntry entry : trait.getModifiers()) {
            AttributeInstance instance = animal.getAttribute(entry.attribute());
            if (instance == null) continue;
            instance.removeModifier(modifierKey(trait, entry));
        }

        for (Trait.PotionEntry entry : trait.getPotionEffects()) {
            animal.removePotionEffect(entry.type());
        }
    }

    private NamespacedKey modifierKey(Trait trait, Trait.ModifierEntry entry) {
        return new NamespacedKey(plugin,
                trait.name().toLowerCase() + "_" + entry.attribute().key().value());
    }
}