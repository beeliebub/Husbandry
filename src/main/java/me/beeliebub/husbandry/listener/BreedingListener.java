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
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles offspring trait inheritance and special breeding traits (Barren, Fertile).
 */
public class BreedingListener implements Listener {

    private final Plugin plugin;
    private final AnimalData animalData;
    private final TraitApplier traitApplier;

    public BreedingListener(Plugin plugin, AnimalData animalData, TraitApplier traitApplier) {
        this.plugin = plugin;
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

        Set<Trait> motherTraits = animalData.getTraits(mother);
        Set<Trait> fatherTraits = animalData.getTraits(father);

        // Barren: if either parent has it, no baby spawns
        if (motherTraits.contains(Trait.BARREN) || fatherTraits.contains(Trait.BARREN)) {
            event.setCancelled(true);
            return;
        }

        // Determine offspring traits via inheritance rules
        Set<Trait> childTraits = resolveInheritance(motherTraits, fatherTraits, child);

        // Initialize the child
        Gender childGender = ThreadLocalRandom.current().nextBoolean() ? Gender.MALE : Gender.FEMALE;
        animalData.initialize(child, childGender, childTraits);
        traitApplier.applyAll(child, childTraits);

        // Fertile: if both parents have it, spawn a twin on the next tick
        if (motherTraits.contains(Trait.FERTILE) && fatherTraits.contains(Trait.FERTILE)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> spawnTwin(child, motherTraits, fatherTraits));
        }
    }

    private void spawnTwin(Animals sibling, Set<Trait> motherTraits, Set<Trait> fatherTraits) {
        Animals twin = (Animals) sibling.getWorld().spawnEntity(sibling.getLocation(), sibling.getType());
        Set<Trait> twinTraits = resolveInheritance(motherTraits, fatherTraits, twin);
        Gender twinGender = ThreadLocalRandom.current().nextBoolean() ? Gender.MALE : Gender.FEMALE;
        animalData.initialize(twin, twinGender, twinTraits);
        traitApplier.applyAll(twin, twinTraits);
    }

    private Set<Trait> resolveInheritance(Set<Trait> motherTraits, Set<Trait> fatherTraits, Animals child) {
        Set<Trait> result = EnumSet.noneOf(Trait.class);

        Set<Trait> allParentTraits = EnumSet.noneOf(Trait.class);
        allParentTraits.addAll(motherTraits);
        allParentTraits.addAll(fatherTraits);

        for (Trait trait : allParentTraits) {
            // Only inherit traits applicable to the child's entity type
            if (!trait.isApplicableTo(child.getType())) continue;

            switch (trait.getInheritanceType()) {
                case DOMINANT -> result.add(trait);
                case RECESSIVE -> {
                    if (motherTraits.contains(trait) && fatherTraits.contains(trait)) {
                        result.add(trait);
                    }
                }
                case SPECIAL -> { /* never inherited */ }
            }
        }

        return result;
    }
}