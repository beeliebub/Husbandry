package me.beeliebub.husbandry.listener;

import me.beeliebub.husbandry.animal.AnimalData;
import me.beeliebub.husbandry.animal.Gender;
import me.beeliebub.husbandry.trait.InheritanceType;
import me.beeliebub.husbandry.trait.Trait;
import me.beeliebub.husbandry.trait.TraitApplier;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles offspring trait inheritance when two animals breed.
 *
 * <h3>Rules</h3>
 * <ul>
 *   <li><b>Dominant</b> - passed if at least one parent carries the trait.</li>
 *   <li><b>Recessive</b> - passed only if both parents carry the trait.</li>
 *   <li><b>Special</b> - never inherited through breeding.</li>
 *   <li>Breeding only succeeds when parents are one male and one female.</li>
 * </ul>
 */
public class BreedingListener implements Listener {

    private final AnimalData animalData;
    private final TraitApplier traitApplier;

    public BreedingListener(AnimalData animalData, TraitApplier traitApplier) {
        this.animalData = animalData;
        this.traitApplier = traitApplier;
    }

    @EventHandler
    public void onBreed(EntityBreedEvent event) {
        if (!(event.getMother() instanceof Animals mother)) return;
        if (!(event.getFather() instanceof Animals father)) return;
        if (!(event.getEntity() instanceof Animals child)) return;

        // Enforce male + female requirement
        Gender motherGender = animalData.getGender(mother);
        Gender fatherGender = animalData.getGender(father);

        if (motherGender == null || fatherGender == null) return;
        if (motherGender == fatherGender) {
            event.setCancelled(true);
            return;
        }

        // Gather parent traits
        Set<Trait> motherTraits = animalData.getTraits(mother);
        Set<Trait> fatherTraits = animalData.getTraits(father);

        // Determine offspring traits via inheritance rules
        Set<Trait> childTraits = resolveInheritance(motherTraits, fatherTraits);

        // Assign gender and traits to the child
        Gender childGender = ThreadLocalRandom.current().nextBoolean() ? Gender.MALE : Gender.FEMALE;
        animalData.initialize(child, childGender, childTraits);
        traitApplier.applyAll(child, childTraits);
    }

    /**
     * Applies inheritance rules across all traits present in either parent.
     */
    private Set<Trait> resolveInheritance(Set<Trait> motherTraits, Set<Trait> fatherTraits) {
        Set<Trait> result = EnumSet.noneOf(Trait.class);

        // Collect the union of all traits from both parents
        Set<Trait> allParentTraits = EnumSet.noneOf(Trait.class);
        allParentTraits.addAll(motherTraits);
        allParentTraits.addAll(fatherTraits);

        for (Trait trait : allParentTraits) {
            switch (trait.getInheritanceType()) {
                case DOMINANT -> {
                    // At least one parent has it (guaranteed since it's in the union)
                    result.add(trait);
                }
                case RECESSIVE -> {
                    if (motherTraits.contains(trait) && fatherTraits.contains(trait)) {
                        result.add(trait);
                    }
                }
                case SPECIAL -> {
                    // Never inherited
                }
            }
        }

        return result;
    }
}