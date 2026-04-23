package me.beeliebub.husbandry.listener;

import me.beeliebub.husbandry.crop.CropData;
import me.beeliebub.husbandry.crop.CropGui;
import me.beeliebub.husbandry.gui.TraitGui;
import me.beeliebub.husbandry.item.HusbandryItems;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles right-clicking animals with the Trait Analyzer/Editor,
 * right-clicking crop blocks with the Crop Analyzer/Editor,
 * and prevents custom comparator items from being placed as blocks.
 */
public class ItemUseListener implements Listener {

    private final HusbandryItems husbandryItems;
    private final TraitGui traitGui;
    private final CropGui cropGui;

    public ItemUseListener(HusbandryItems husbandryItems, TraitGui traitGui, CropGui cropGui) {
        this.husbandryItems = husbandryItems;
        this.traitGui = traitGui;
        this.cropGui = cropGui;
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Animals animal)) return;

        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        String type = husbandryItems.getItemType(item);
        if (type == null) return;

        // Only animal analyzer/editor work on entities
        if (!HusbandryItems.TYPE_ANALYZER.equals(type) && !HusbandryItems.TYPE_EDITOR.equals(type)) return;

        event.setCancelled(true);

        boolean isEditor = HusbandryItems.TYPE_EDITOR.equals(type);
        traitGui.open(event.getPlayer(), animal, isEditor);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        String type = husbandryItems.getItemType(item);
        if (type == null) return;

        // Prevent all custom items from being placed as blocks
        event.setCancelled(true);

        // Handle crop analyzer/editor on crop blocks
        if (HusbandryItems.TYPE_CROP_ANALYZER.equals(type) || HusbandryItems.TYPE_CROP_EDITOR.equals(type)) {
            Block block = event.getClickedBlock();
            if (block == null) return;

            Material blockType = block.getType();
            if (CropData.isCropBlock(blockType) || CropData.isStemBlock(blockType)) {
                boolean isEditor = HusbandryItems.TYPE_CROP_EDITOR.equals(type);
                cropGui.open(event.getPlayer(), block, isEditor);
            }
        }
    }
}