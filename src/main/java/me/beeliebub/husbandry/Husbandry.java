package me.beeliebub.husbandry;

import me.beeliebub.husbandry.animal.AnimalData;
import me.beeliebub.husbandry.command.HusbandryCommand;
import me.beeliebub.husbandry.crop.CropCraftListener;
import me.beeliebub.husbandry.crop.CropData;
import me.beeliebub.husbandry.crop.CropGui;
import me.beeliebub.husbandry.crop.CropHarvestListener;
import me.beeliebub.husbandry.crop.CropPlantListener;
import me.beeliebub.husbandry.gui.TraitGui;
import me.beeliebub.husbandry.item.HusbandryItems;
import me.beeliebub.husbandry.listener.BreedingListener;
import me.beeliebub.husbandry.listener.ChunkLoadListener;
import me.beeliebub.husbandry.listener.ItemUseListener;
import me.beeliebub.husbandry.listener.TraitEffectListener;
import me.beeliebub.husbandry.listener.TraitTickTask;
import me.beeliebub.husbandry.trait.TraitApplier;
import org.bukkit.plugin.java.JavaPlugin;

public final class Husbandry extends JavaPlugin {

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        AnimalData animalData = new AnimalData(this);
        CropData cropData = new CropData(this);
        TraitApplier traitApplier = new TraitApplier(this);
        HusbandryItems husbandryItems = new HusbandryItems(this);
        TraitGui traitGui = new TraitGui(animalData, traitApplier);
        CropGui cropGui = new CropGui(cropData);

        // Register crafting recipes
        husbandryItems.registerRecipes();

        // Register listeners
        getServer().getPluginManager().registerEvents(new ChunkLoadListener(this, animalData, traitApplier), this);
        getServer().getPluginManager().registerEvents(new BreedingListener(this, animalData, traitApplier, husbandryItems), this);
        getServer().getPluginManager().registerEvents(new TraitEffectListener(this, animalData), this);
        getServer().getPluginManager().registerEvents(new ItemUseListener(husbandryItems, traitGui, cropGui), this);
        getServer().getPluginManager().registerEvents(traitGui, this);

        // Crop listeners
        getServer().getPluginManager().registerEvents(new CropHarvestListener(this, cropData), this);
        getServer().getPluginManager().registerEvents(new CropPlantListener(cropData), this);
        getServer().getPluginManager().registerEvents(new CropCraftListener(this, cropData, husbandryItems), this);
        getServer().getPluginManager().registerEvents(cropGui, this);

        // Register commands
        HusbandryCommand command = new HusbandryCommand(husbandryItems, cropData);
        getCommand("husbandry").setExecutor(command);
        getCommand("husbandry").setTabCompleter(command);

        // Start periodic tasks
        new TraitTickTask(this, animalData).start();

        getLogger().info("Husbandry enabled - animal and crop trait system active.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Husbandry disabled.");
    }
}