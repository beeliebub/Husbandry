package me.beeliebub.husbandry.crop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Inventory GUI for viewing and editing crop traits on planted crop blocks and stems.
 * Analogous to {@link me.beeliebub.husbandry.gui.TraitGui} but for crops.
 */
public class CropGui implements Listener {

    private final CropData cropData;
    private final Map<UUID, CropGuiSession> sessions = new HashMap<>();

    public CropGui(CropData cropData) {
        this.cropData = cropData;
    }

    /** Opens the crop trait GUI for a planted crop or stem block. */
    public void open(Player player, Block block, boolean isEditor) {
        Set<CropTrait> blockTraits = cropData.getBlockTraits(block);
        CropTrait[] allTraits = CropTrait.values();
        int size = 9; // 3 trait panes in one row

        String blockName = CropData.materialDisplayName(block.getType());
        Component title;
        if (isEditor) {
            title = Component.text(blockName + " Traits ", NamedTextColor.DARK_GREEN)
                    .append(Component.text("(Editor)", NamedTextColor.RED));
        } else {
            title = Component.text(blockName + " Traits", NamedTextColor.DARK_GREEN);
        }

        Inventory inventory = Bukkit.createInventory(null, size, title);

        // Trait panes (slots 0-2)
        for (int i = 0; i < allTraits.length; i++) {
            CropTrait trait = allTraits[i];
            boolean hasTrait = blockTraits.contains(trait);
            inventory.setItem(i, createTraitPane(trait, hasTrait, isEditor));
        }

        sessions.put(player.getUniqueId(),
                new CropGuiSession(block.getX(), block.getY(), block.getZ(),
                        block.getWorld().getName(), isEditor));
        player.openInventory(inventory);
    }

    // ── Event handlers ──────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        CropGuiSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            event.setCancelled(true);

            if (!session.isEditor) return;

            int slot = event.getSlot();
            Block block = getSessionBlock(player, session);
            if (block == null) return;

            CropTrait[] allTraits = CropTrait.values();

            if (slot >= 0 && slot < allTraits.length) {
                handleTraitClick(player, session, block, allTraits[slot], slot,
                        event.getView().getTopInventory());
            }
        } else if (event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (sessions.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        sessions.remove(event.getPlayer().getUniqueId());
    }

    // ── Editor click handling ──────────────────────────────────────

    private void handleTraitClick(Player player, CropGuiSession session, Block block,
                                  CropTrait trait, int slot, Inventory inventory) {
        Set<CropTrait> traits = cropData.getBlockTraits(block);

        if (traits.contains(trait)) {
            traits.remove(trait);
        } else {
            traits.add(trait);
        }

        cropData.setBlockTraits(block, traits);
        inventory.setItem(slot, createTraitPane(trait, traits.contains(trait), true));
    }

    // ── GUI item builders ──────────────────────────────────────────

    private static ItemStack createTraitPane(CropTrait trait, boolean hasTrait, boolean isEditor) {
        Material mat = hasTrait ? Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(trait.getDisplayName(), trait.getRarity().getColor())
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.text(trait.getDescription(), NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());
        lore.add(Component.text("Rarity: " + formatEnum(trait.getRarity().name()), NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));

        if (isEditor) {
            lore.add(Component.empty());
            Component action = hasTrait
                    ? Component.text("Click to remove", NamedTextColor.RED)
                    : Component.text("Click to apply", NamedTextColor.GREEN);
            lore.add(action.decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private Block getSessionBlock(Player player, CropGuiSession session) {
        var world = Bukkit.getWorld(session.worldName);
        if (world == null) {
            player.closeInventory();
            player.sendMessage(Component.text("World is no longer available.", NamedTextColor.RED));
            return null;
        }
        Block block = world.getBlockAt(session.x, session.y, session.z);
        if (!CropData.isCropBlock(block.getType()) && !CropData.isStemBlock(block.getType())) {
            player.closeInventory();
            player.sendMessage(Component.text("Crop is no longer there.", NamedTextColor.RED));
            return null;
        }
        return block;
    }

    private static String formatEnum(String name) {
        String lower = name.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private record CropGuiSession(int x, int y, int z, String worldName, boolean isEditor) {
    }
}