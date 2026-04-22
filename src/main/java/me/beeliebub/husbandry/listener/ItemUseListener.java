package me.beeliebub.husbandry.listener;

import me.beeliebub.husbandry.gui.TraitGui;
import me.beeliebub.husbandry.item.HusbandryItems;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles right-clicking animals with the Trait Analyzer / Editor to open the GUI,
 * and prevents the custom comparator items from being placed as blocks.
 */
public class ItemUseListener implements Listener {

    private final HusbandryItems husbandryItems;
    private final TraitGui traitGui;

    public ItemUseListener(HusbandryItems husbandryItems, TraitGui traitGui) {
        this.husbandryItems = husbandryItems;
        this.traitGui = traitGui;
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Animals animal)) return;

        ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());
        String type = husbandryItems.getItemType(item);
        if (type == null) return;

        event.setCancelled(true);

        boolean isEditor = HusbandryItems.TYPE_EDITOR.equals(type);
        traitGui.open(event.getPlayer(), animal, isEditor);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (husbandryItems.getItemType(event.getItem()) != null) {
            event.setCancelled(true);
        }
    }
}