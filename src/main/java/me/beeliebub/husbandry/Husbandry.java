package me.beeliebub.husbandry;

import me.beeliebub.husbandry.animal.AnimalData;
import me.beeliebub.husbandry.command.HusbandryCommand;
import me.beeliebub.husbandry.crop.CropCraftListener;
import me.beeliebub.husbandry.crop.CropData;
import me.beeliebub.husbandry.crop.CropGui;
import me.beeliebub.husbandry.crop.CropHarvestListener;
import me.beeliebub.husbandry.crop.CropPlantListener;
import me.beeliebub.husbandry.crop.CropTrait;
import me.beeliebub.husbandry.gui.TraitGui;
import me.beeliebub.husbandry.item.HusbandryItems;
import me.beeliebub.husbandry.listener.BreedingListener;
import me.beeliebub.husbandry.listener.ChunkLoadListener;
import me.beeliebub.husbandry.listener.ItemUseListener;
import me.beeliebub.husbandry.listener.TraitEffectListener;
import me.beeliebub.husbandry.listener.TraitTickTask;
import me.beeliebub.husbandry.trait.TraitApplier;
import me.beeliebub.tweaks.Tweaks;
import me.beeliebub.tweaks.enchantments.Replant;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

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

        // Integrate with Tweaks' Replant enchantment: when Replant replants a crop using
        // a seed that was traited by our CropHarvestListener (at LOW priority), copy those
        // traits onto the replanted block's chunk PDC so the next harvest preserves them.
        registerReplantHook(cropData);

        // Start periodic tasks
        new TraitTickTask(this, animalData).start();

        getLogger().info("Husbandry enabled - animal and crop trait system active.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Husbandry disabled.");
    }

    private void registerReplantHook(CropData cropData) {
        Plugin tweaksPlugin = Bukkit.getPluginManager().getPlugin("Tweaks");
        if (!(tweaksPlugin instanceof Tweaks tweaks) || !tweaks.isEnabled()) {
            getLogger().warning("Tweaks plugin not available; Replant trait inheritance disabled.");
            return;
        }
        Replant replant = tweaks.getReplant();
        if (replant == null) {
            getLogger().warning("Tweaks#getReplant() returned null; Replant trait inheritance disabled.");
            return;
        }
        replant.addReplantHook((seed, block) -> {
            Set<CropTrait> traits = cropData.getTraits(seed);
            if (traits.isEmpty()) {
                cropData.removeBlockTraits(block);
            } else {
                cropData.setBlockTraits(block, traits);
            }
        });
    }
}