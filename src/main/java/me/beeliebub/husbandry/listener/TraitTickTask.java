package me.beeliebub.husbandry.listener;

import me.beeliebub.husbandry.animal.AnimalData;
import me.beeliebub.husbandry.trait.Trait;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Animals;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

/**
 * Runs every 20 ticks (1 second) to apply periodic trait effects:
 * <ul>
 *   <li><b>Hydrophobic</b> - half heart damage in water or rain</li>
 *   <li><b>Hydrophilic</b> - half heart healing in water or rain</li>
 * </ul>
 */
public class TraitTickTask extends BukkitRunnable {

    private final Plugin plugin;
    private final AnimalData animalData;

    public TraitTickTask(Plugin plugin, AnimalData animalData) {
        this.plugin = plugin;
        this.animalData = animalData;
    }

    /** Starts the repeating task. Call once during plugin enable. */
    public void start() {
        runTaskTimer(plugin, 20L, 20L); // 1 second interval
    }

    @Override
    public void run() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Animals animal : world.getEntitiesByClass(Animals.class)) {
                if (!animalData.isInitialized(animal)) continue;

                Set<Trait> traits = animalData.getTraits(animal);
                if (traits.isEmpty()) continue;

                boolean inWater = animal.isInWater();
                boolean inRain = !inWater && isExposedToRain(animal);

                if (inWater || inRain) {
                    if (traits.contains(Trait.HYDROPHOBIC)) {
                        animal.damage(1.0); // half heart
                    }
                    if (traits.contains(Trait.HYDROPHILIC)) {
                        AttributeInstance maxHealth = animal.getAttribute(Attribute.MAX_HEALTH);
                        if (maxHealth != null) {
                            double newHealth = Math.min(
                                    animal.getHealth() + 1.0, maxHealth.getValue());
                            animal.setHealth(newHealth);
                        }
                    }
                }
            }
        }
    }

    private boolean isExposedToRain(Animals animal) {
        World world = animal.getWorld();
        if (!world.hasStorm()) return false;

        Location loc = animal.getLocation();
        return world.getHighestBlockYAt(loc) <= loc.getBlockY();
    }
}