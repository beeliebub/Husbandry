package me.beeliebub.husbandry.listener;

import me.beeliebub.husbandry.animal.AnimalData;
import me.beeliebub.husbandry.trait.Trait;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityEnterBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Set;

/**
 * Handles all event-driven trait effects: damage cancellation, drop modification,
 * bee honey mechanics, chicken egg mechanics, and sheep wool mechanics.
 */
public class TraitEffectListener implements Listener {

    private final Plugin plugin;
    private final AnimalData animalData;

    public TraitEffectListener(Plugin plugin, AnimalData animalData) {
        this.plugin = plugin;
        this.animalData = animalData;
    }

    // ── Damage traits: Invincible, Waterbreathing ───────────────────

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Animals animal)) return;
        if (!animalData.isInitialized(animal)) return;

        Set<Trait> traits = animalData.getTraits(animal);

        if (traits.contains(Trait.INVINCIBLE)) {
            event.setCancelled(true);
            return;
        }

        if (traits.contains(Trait.WATERBREATHING)
                && event.getCause() == EntityDamageEvent.DamageCause.SUFFOCATION) {
            event.setCancelled(true);
        }
    }

    // ── Death drops: Bountiful ──────────────────────────────────────

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Animals animal)) return;
        if (!animalData.isInitialized(animal)) return;
        if (!animalData.getTraits(animal).contains(Trait.BOUNTIFUL)) return;

        for (ItemStack item : event.getDrops()) {
            item.setAmount(item.getAmount() * 2);
        }
    }

    // ── Item drops: Wooly (sheep shearing), Empty/Eggy (chicken eggs) ─

    @EventHandler
    public void onEntityDropItem(EntityDropItemEvent event) {
        // Sheep wool doubling
        if (event.getEntity() instanceof Sheep sheep && !sheep.isDead()) {
            if (isWool(event.getItemDrop().getItemStack().getType())
                    && animalData.isInitialized(sheep)
                    && animalData.getTraits(sheep).contains(Trait.WOOLY)) {
                ItemStack item = event.getItemDrop().getItemStack();
                item.setAmount(item.getAmount() * 2);
                event.getItemDrop().setItemStack(item);
            }
            return;
        }

        // Chicken egg mechanics
        if (event.getEntity() instanceof Chicken chicken && !chicken.isDead()) {
            if (event.getItemDrop().getItemStack().getType() != Material.EGG) return;
            if (!animalData.isInitialized(chicken)) return;

            Set<Trait> traits = animalData.getTraits(chicken);

            if (traits.contains(Trait.EMPTY)) {
                event.setCancelled(true);
                return;
            }

            if (traits.contains(Trait.EGGY)) {
                chicken.getWorld().dropItemNaturally(
                        chicken.getLocation(), new ItemStack(Material.EGG));
            }
        }
    }

    // ── Bee honey mechanics: Honey Hindrance, Honey Helper ──────────

    @EventHandler
    public void onBeeEnterHive(EntityEnterBlockEvent event) {
        if (!(event.getEntity() instanceof Bee bee)) return;
        if (!bee.hasNectar()) return;
        if (!animalData.isInitialized(bee)) return;

        Set<Trait> traits = animalData.getTraits(bee);
        Block block = event.getBlock();

        if (traits.contains(Trait.HONEY_HINDRANCE)) {
            // Undo the honey level increase that vanilla applies when a nectar bee enters
            plugin.getServer().getScheduler().runTask(plugin, () -> adjustHoney(block, -1));
        } else if (traits.contains(Trait.HONEY_HELPER)) {
            plugin.getServer().getScheduler().runTask(plugin, () -> adjustHoney(block, 1));
        }
    }

    private void adjustHoney(Block block, int delta) {
        BlockData data = block.getBlockData();
        if (data instanceof Beehive hive) {
            int newLevel = Math.clamp(
                    hive.getHoneyLevel() + delta, 0, hive.getMaximumHoneyLevel());
            hive.setHoneyLevel(newLevel);
            block.setBlockData(hive);
        }
    }

    // ── Bee pollination: Pollinator ─────────────────────────────────

    @EventHandler
    public void onBeePollinate(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof Bee bee)) return;
        if (!(event.getBlock().getBlockData() instanceof Ageable)) return;
        if (!animalData.isInitialized(bee)) return;
        if (!animalData.getTraits(bee).contains(Trait.POLLINATOR)) return;

        Block block = event.getBlock();
        // Let vanilla grow happen, then force to max age on next tick
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            BlockData data = block.getBlockData();
            if (data instanceof Ageable ageable) {
                ageable.setAge(ageable.getMaximumAge());
                block.setBlockData(ageable);
            }
        });
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private static boolean isWool(Material material) {
        return material.name().endsWith("_WOOL");
    }
}