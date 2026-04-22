package me.beeliebub.husbandry.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Factory and identifier for the Trait Analyzer and Trait Editor custom items.
 * Both are comparators with a PDC tag distinguishing them.
 */
public final class HusbandryItems {

    public static final String TYPE_ANALYZER = "analyzer";
    public static final String TYPE_EDITOR = "editor";

    private final Plugin plugin;
    private final NamespacedKey itemTypeKey;

    public HusbandryItems(Plugin plugin) {
        this.plugin = plugin;
        this.itemTypeKey = new NamespacedKey(plugin, "item_type");
    }

    public ItemStack createAnalyzer() {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Trait Analyzer", NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Right-click an animal to", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("analyze its traits.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, TYPE_ANALYZER);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createEditor() {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Trait Editor", NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Right-click an animal to", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("edit its traits.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, TYPE_EDITOR);

        item.setItemMeta(meta);
        return item;
    }

    /** Returns "analyzer", "editor", or null if the item is not a Husbandry item. */
    public @Nullable String getItemType(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(itemTypeKey, PersistentDataType.STRING);
    }

    /** Registers the Trait Analyzer crafting recipe with the server. */
    public void registerRecipes() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "trait_analyzer");
        ShapelessRecipe recipe = new ShapelessRecipe(recipeKey, createAnalyzer());
        recipe.setCategory(CraftingBookCategory.MISC);

        recipe.addIngredient(Material.COMPARATOR);
        recipe.addIngredient(Material.BEEF);
        recipe.addIngredient(Material.PORKCHOP);
        recipe.addIngredient(Material.CHICKEN);
        recipe.addIngredient(Material.FEATHER);
        recipe.addIngredient(new org.bukkit.inventory.RecipeChoice.MaterialChoice(Tag.ITEMS_EGGS));
        recipe.addIngredient(Material.LEATHER);
        recipe.addIngredient(Material.MUTTON);
        recipe.addIngredient(new org.bukkit.inventory.RecipeChoice.MaterialChoice(Tag.WOOL));

        plugin.getServer().addRecipe(recipe);
    }
}