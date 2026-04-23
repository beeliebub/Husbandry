package me.beeliebub.husbandry.listener;

import me.beeliebub.husbandry.animal.AnimalData;
import me.beeliebub.husbandry.animal.Gender;
import me.beeliebub.husbandry.trait.Trait;
import me.beeliebub.husbandry.trait.TraitApplier;
import me.beeliebub.husbandry.trait.TraitRarity;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * When entities are loaded (chunk load), every {@link Animals} without the
 * Husbandry PDC tag is initialized with a random gender and random traits
 * appropriate for its entity type. Uses config for rarity weights.
 */
public class ChunkLoadListener implements Listener {

    private final Plugin plugin;
    private final AnimalData animalData;
    private final TraitApplier traitApplier;

    public ChunkLoadListener(Plugin plugin, AnimalData animalData, TraitApplier traitApplier) {
        this.plugin = plugin;
        this.animalData = animalData;
        this.traitApplier = traitApplier;
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (!(entity instanceof Animals animal)) continue;
            if (animalData.isSpawnerMob(animal)) continue;
            if (animalData.isInitialized(animal)) {
                traitApplier.applyAll(animal, animalData.getTraits(animal));
                continue;
            }
            initializeAnimal(animal);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!(event.getEntity() instanceof Animals animal)) return;

        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();

        // Spawner-block animals get flagged and never receive traits
        if (reason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
            animalData.markSpawnerMob(animal);
            return;
        }

        if (reason != CreatureSpawnEvent.SpawnReason.COMMAND
                && reason != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG) return;

        if (!animalData.isInitialized(animal)) {
            initializeAnimal(animal);
        }
    }

    private void initializeAnimal(Animals animal) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        Gender gender = rng.nextBoolean() ? Gender.MALE : Gender.FEMALE;
        Set<Trait> traits = rollRandomTraits(rng, animal.getType());

        animalData.initialize(animal, gender, traits);
        traitApplier.applyAll(animal, traits);
    }

    private Set<Trait> rollRandomTraits(ThreadLocalRandom rng, EntityType entityType) {
        Set<Trait> result = EnumSet.noneOf(Trait.class);
        int count = rng.nextInt(1, 3); // 1 or 2 traits

        for (int i = 0; i < count; i++) {
            TraitRarity rarity = pickRarity(rng);
            List<Trait> pool = Trait.byRarityFor(rarity, entityType);
            if (!pool.isEmpty()) {
                result.add(pool.get(rng.nextInt(pool.size())));
            }
        }
        return result;
    }

    private TraitRarity pickRarity(ThreadLocalRandom rng) {
        FileConfiguration config = plugin.getConfig();
        double basic = config.getDouble("animal-traits.basic-chance", 75) / 100.0;
        double rare = config.getDouble("animal-traits.rare-chance", 20) / 100.0;

        double roll = rng.nextDouble();
        if (roll < basic) return TraitRarity.BASIC;
        if (roll < basic + rare) return TraitRarity.RARE;
        return TraitRarity.LEGENDARY;
    }
}