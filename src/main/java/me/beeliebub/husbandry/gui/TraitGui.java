package me.beeliebub.husbandry.gui;

import me.beeliebub.husbandry.animal.AnimalData;
import me.beeliebub.husbandry.animal.Gender;
import me.beeliebub.husbandry.trait.Trait;
import me.beeliebub.husbandry.trait.TraitApplier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the trait GUI for both the Analyzer (read-only) and the Editor (toggle traits).
 * Implements {@link Listener} to handle inventory click/close events.
 */
public class TraitGui implements Listener {

    private final AnimalData animalData;
    private final TraitApplier traitApplier;
    private final Map<UUID, GuiSession> sessions = new HashMap<>();

    public TraitGui(AnimalData animalData, TraitApplier traitApplier) {
        this.animalData = animalData;
        this.traitApplier = traitApplier;
    }

    /** Opens the trait GUI for the given animal. */
    public void open(Player player, Animals animal, boolean isEditor) {
        // Initialize if needed (edge case: animal spawned after chunk load)
        if (!animalData.isInitialized(animal)) {
            Gender gender = ThreadLocalRandom.current().nextBoolean() ? Gender.MALE : Gender.FEMALE;
            animalData.initialize(animal, gender, EnumSet.noneOf(Trait.class));
        }

        EntityType entityType = animal.getType();
        List<Trait> applicableTraits = getApplicableTraits(entityType);
        Set<Trait> animalTraits = animalData.getTraits(animal);
        Gender gender = animalData.getGender(animal);

        // Calculate inventory size (traits + 1 gender item, multiple of 9, min 9)
        int totalItems = applicableTraits.size() + 1;
        int size = ((totalItems + 8) / 9) * 9;
        size = Math.max(9, size);

        String animalName = formatEntityType(entityType);
        Component title;
        if (isEditor) {
            title = Component.text(animalName + "'s Traits ", NamedTextColor.DARK_GREEN)
                    .append(Component.text("(Editor)", NamedTextColor.RED));
        } else {
            title = Component.text(animalName + "'s Traits", NamedTextColor.DARK_GREEN);
        }

        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (int i = 0; i < applicableTraits.size(); i++) {
            Trait trait = applicableTraits.get(i);
            boolean hasTrait = animalTraits.contains(trait);
            inventory.setItem(i, createTraitPane(trait, hasTrait, isEditor));
        }

        // Place gender dye in the last slot of the inventory
        inventory.setItem(size - 1, createGenderItem(gender));

        sessions.put(player.getUniqueId(),
                new GuiSession(animal.getUniqueId(), isEditor, applicableTraits));
        player.openInventory(inventory);
    }

    // ── Event handlers ──────────────────────────────────────────────

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        GuiSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        // Cancel all clicks in the top inventory; cancel shift-clicks in the bottom
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            event.setCancelled(true);

            if (!session.isEditor) return;

            int slot = event.getSlot();
            if (slot < 0 || slot >= session.traitLayout.size()) return;

            handleEditorClick(player, session, slot, event.getView().getTopInventory());
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

    // ── Editor click handling ───────────────────────────────────────

    private void handleEditorClick(Player player, GuiSession session, int slot, Inventory inventory) {
        if (!(Bukkit.getEntity(session.animalUuid) instanceof Animals animal) || !animal.isValid()) {
            player.closeInventory();
            player.sendMessage(Component.text("Animal is no longer available.", NamedTextColor.RED));
            return;
        }

        Trait trait = session.traitLayout.get(slot);
        Set<Trait> traits = animalData.getTraits(animal);

        if (traits.contains(trait)) {
            traits.remove(trait);
            traitApplier.remove(animal, trait);
        } else {
            traits.add(trait);
            traitApplier.apply(animal, trait);
        }

        animalData.setTraits(animal, traits);

        // Update the glass pane
        boolean nowHasTrait = traits.contains(trait);
        inventory.setItem(slot, createTraitPane(trait, nowHasTrait, true));
    }

    // ── GUI item builders ───────────────────────────────────────────

    private static ItemStack createTraitPane(Trait trait, boolean hasTrait, boolean isEditor) {
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
        lore.add(Component.text("Inheritance: " + formatEnum(trait.getInheritanceType().name()), NamedTextColor.GRAY)
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

    private static ItemStack createGenderItem(Gender gender) {
        boolean isMale = gender == Gender.MALE;
        Material mat = isMale ? Material.LIGHT_BLUE_DYE : Material.PINK_DYE;
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();

        NamedTextColor color = isMale ? NamedTextColor.AQUA : NamedTextColor.LIGHT_PURPLE;
        String label = isMale ? "Male" : "Female";
        meta.displayName(Component.text(label, color).decoration(TextDecoration.ITALIC, false));

        item.setItemMeta(meta);
        return item;
    }

    // ── Helpers ─────────────────────────────────────────────────────

    private static List<Trait> getApplicableTraits(EntityType entityType) {
        List<Trait> result = new ArrayList<>();
        for (Trait trait : Trait.values()) {
            if (trait.isApplicableTo(entityType)) {
                result.add(trait);
            }
        }
        return result;
    }

    private static String formatEntityType(EntityType type) {
        String[] words = type.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    private static String formatEnum(String name) {
        String lower = name.toLowerCase().replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    // ── Session record ──────────────────────────────────────────────

    private record GuiSession(UUID animalUuid, boolean isEditor, List<Trait> traitLayout) {
    }
}