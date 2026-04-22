package me.beeliebub.husbandry;

import me.beeliebub.husbandry.animal.AnimalData;
import me.beeliebub.husbandry.listener.BreedingListener;
import me.beeliebub.husbandry.listener.ChunkLoadListener;
import me.beeliebub.husbandry.listener.TraitEffectListener;
import me.beeliebub.husbandry.listener.TraitTickTask;
import me.beeliebub.husbandry.trait.TraitApplier;
import org.bukkit.plugin.java.JavaPlugin;

public final class Husbandry extends JavaPlugin {

    @Override
    public void onEnable() {
        AnimalData animalData = new AnimalData(this);
        TraitApplier traitApplier = new TraitApplier(this);

        getServer().getPluginManager().registerEvents(new ChunkLoadListener(animalData, traitApplier), this);
        getServer().getPluginManager().registerEvents(new BreedingListener(this, animalData, traitApplier), this);
        getServer().getPluginManager().registerEvents(new TraitEffectListener(this, animalData), this);

        new TraitTickTask(this, animalData).start();

        getLogger().info("Husbandry enabled - animal trait system active.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Husbandry disabled.");
    }
}