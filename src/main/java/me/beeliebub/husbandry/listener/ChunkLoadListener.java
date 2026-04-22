package me.beeliebub.husbandry.listener;

import me.beeliebub.husbandry.animal.AnimalData;
import me.beeliebub.husbandry.animal.Gender;
import me.beeliebub.husbandry.trait.Trait;
import me.beeliebub.husbandry.trait.TraitApplier;
import me.beeliebub.husbandry.trait.TraitRarity;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.EntitiesLoadEvent;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * When entities are loaded (chunk load), every {@link Animals} without the
 * Husbandry PDC tag is initialized with a random gender and random traits.
 */
public class ChunkLoadListener implements Listener {

    private final AnimalData animalData;
    private final TraitApplier traitApplier;

    public ChunkLoadListener(AnimalData animalData, TraitApplier traitApplier) {
        this.animalData = animalData;
        this.traitApplier = traitApplier;
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (!(entity instanceof Animals animal)) continue;
            if (animalData.isInitialized(animal)) {
                // Already tagged - reapply modifiers so they survive restarts
                traitApplier.applyAll(animal, animalData.getTraits(animal));
                continue;
            }
            initializeAnimal(animal);
        }
    }

    private void initializeAnimal(Animals animal) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        Gender gender = rng.nextBoolean() ? Gender.MALE : Gender.FEMALE;
        Set<Trait> traits = rollRandomTraits(rng);

        animalData.initialize(animal, gender, traits);
        traitApplier.applyAll(animal, traits);
    }

    /**
     * Rolls a small random set of traits using weighted rarity selection.
     * Currently assigns 1-2 traits per animal; tune as needed.
     */
    private Set<Trait> rollRandomTraits(ThreadLocalRandom rng) {
        Set<Trait> result = EnumSet.noneOf(Trait.class);
        int count = rng.nextInt(1, 3); // 1 or 2 traits

        for (int i = 0; i < count; i++) {
            TraitRarity rarity = pickRarity(rng);
            List<Trait> pool = Trait.byRarity(rarity);
            if (!pool.isEmpty()) {
                result.add(pool.get(rng.nextInt(pool.size())));
            }
        }
        return result;
    }

    private TraitRarity pickRarity(ThreadLocalRandom rng) {
        double roll = rng.nextDouble();
        double cumulative = 0.0;
        for (TraitRarity rarity : TraitRarity.values()) {
            cumulative += rarity.getWeight();
            if (roll < cumulative) return rarity;
        }
        return TraitRarity.BASIC; // fallback
    }
}