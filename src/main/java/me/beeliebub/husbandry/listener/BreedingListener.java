package me.beeliebub.husbandry.listener;

import me.beeliebub.husbandry.animal.AnimalData;
import me.beeliebub.husbandry.animal.Gender;
import me.beeliebub.husbandry.item.HusbandryItems;
import me.beeliebub.husbandry.trait.InheritanceType;
import me.beeliebub.husbandry.trait.Trait;
import me.beeliebub.husbandry.trait.TraitApplier;
import me.beeliebub.husbandry.trait.TraitRarity;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles offspring trait inheritance, special breeding traits (Barren, Fertile),
 * and quality feed for rare trait inheritance.
 */
public class BreedingListener implements Listener {

    private static final Set<EntityType> WHEAT_FEED_ANIMALS = Set.of(
            EntityType.COW, EntityType.SHEEP, EntityType.MOOSHROOM, EntityType.GOAT);
    private static final Set<EntityType> CARROT_FEED_ANIMALS = Set.of(EntityType.PIG);
    private static final Set<EntityType> GOLDEN_CARROT_FEED_ANIMALS = Set.of(
            EntityType.RABBIT, EntityType.HORSE, EntityType.DONKEY);
    private static final Set<EntityType> SEED_FEED_ANIMALS = Set.of(EntityType.CHICKEN);

    private final Plugin plugin;
    private final AnimalData animalData;
    private final TraitApplier traitApplier;
    private final HusbandryItems husbandryItems;

    /** Tracks animals that have been fed quality feed (UUID → feed type). */
    private final Set<UUID> qualityFedAnimals = new HashSet<>();

    public BreedingListener(Plugin plugin, AnimalData animalData, TraitApplier traitApplier,
                            HusbandryItems husbandryItems) {
        this.plugin = plugin;
        this.animalData = animalData;
        this.traitApplier = traitApplier;
        this.husbandryItems = husbandryItems;
    }

    // ── Quality feed tracking ──────────────────────────────────────

    @EventHandler
    public void onPlayerFeedAnimal(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Animals animal)) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(event.getHand());
        if (!husbandryItems.isQualityFeed(item)) return;

        String feedType = husbandryItems.getItemType(item);
        EntityType entityType = animal.getType();

        // Validate that this feed type works for this animal
        boolean valid = switch (feedType) {
            case HusbandryItems.TYPE_QUALITY_WHEAT_FEED -> WHEAT_FEED_ANIMALS.contains(entityType);
            case HusbandryItems.TYPE_QUALITY_CARROT_FEED -> CARROT_FEED_ANIMALS.contains(entityType);
            case HusbandryItems.TYPE_QUALITY_GOLDEN_CARROT_FEED -> GOLDEN_CARROT_FEED_ANIMALS.contains(entityType);
            case HusbandryItems.TYPE_QUALITY_SEED_FEED -> SEED_FEED_ANIMALS.contains(entityType);
            default -> false;
        };

        if (valid) {
            qualityFedAnimals.add(animal.getUniqueId());
        }
    }

    // ── Breeding ───────────────────────────────────────────────────

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

        // Check if quality feed was used on both parents
        boolean qualityBreeding = qualityFedAnimals.remove(mother.getUniqueId())
                & qualityFedAnimals.remove(father.getUniqueId());

        // Determine offspring traits via inheritance rules
        Set<Trait> childTraits = resolveInheritance(motherTraits, fatherTraits, child, qualityBreeding);

        // Initialize the child
        Gender childGender = ThreadLocalRandom.current().nextBoolean() ? Gender.MALE : Gender.FEMALE;
        animalData.initialize(child, childGender, childTraits);
        traitApplier.applyAll(child, childTraits);

        // Fertile: if both parents have it, spawn a twin on the next tick
        if (motherTraits.contains(Trait.FERTILE) && fatherTraits.contains(Trait.FERTILE)) {
            plugin.getServer().getScheduler().runTask(plugin,
                    () -> spawnTwin(child, motherTraits, fatherTraits, qualityBreeding));
        }
    }

    private void spawnTwin(Animals sibling, Set<Trait> motherTraits, Set<Trait> fatherTraits,
                           boolean qualityBreeding) {
        Animals twin = (Animals) sibling.getWorld().spawnEntity(sibling.getLocation(), sibling.getType());
        Set<Trait> twinTraits = resolveInheritance(motherTraits, fatherTraits, twin, qualityBreeding);
        Gender twinGender = ThreadLocalRandom.current().nextBoolean() ? Gender.MALE : Gender.FEMALE;
        animalData.initialize(twin, twinGender, twinTraits);
        traitApplier.applyAll(twin, twinTraits);
    }

    private Set<Trait> resolveInheritance(Set<Trait> motherTraits, Set<Trait> fatherTraits,
                                          Animals child, boolean qualityBreeding) {
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
                case SPECIAL -> {
                    // With quality feed, RARE-rarity SPECIAL traits can be inherited
                    // only if BOTH parents have the trait (like RECESSIVE)
                    // LEGENDARY-rarity SPECIAL traits are never inherited
                    if (qualityBreeding && trait.getRarity() == TraitRarity.RARE
                            && motherTraits.contains(trait) && fatherTraits.contains(trait)) {
                        result.add(trait);
                    }
                }
            }
        }

        return result;
    }
}