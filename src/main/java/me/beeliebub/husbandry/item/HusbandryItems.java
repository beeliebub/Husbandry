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
 * Factory and identifier for all custom Husbandry items:
 * Trait Analyzer, Trait Editor, Crop Analyzer, Crop Editor, and Quality Feed items.
 */
public final class HusbandryItems {

    public static final String TYPE_ANALYZER = "analyzer";
    public static final String TYPE_EDITOR = "editor";
    public static final String TYPE_CROP_ANALYZER = "crop_analyzer";
    public static final String TYPE_CROP_EDITOR = "crop_editor";
    public static final String TYPE_QUALITY_WHEAT_FEED = "quality_wheat_feed";
    public static final String TYPE_QUALITY_CARROT_FEED = "quality_carrot_feed";
    public static final String TYPE_QUALITY_GOLDEN_CARROT_FEED = "quality_golden_carrot_feed";
    public static final String TYPE_QUALITY_SEED_FEED = "quality_seed_feed";

    private final Plugin plugin;
    private final NamespacedKey itemTypeKey;

    public HusbandryItems(Plugin plugin) {
        this.plugin = plugin;
        this.itemTypeKey = new NamespacedKey(plugin, "item_type");
    }

    // ── Animal tools ───────────────────────────────────────────────

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

    // ── Crop tools ─────────────────────────────────────────────────

    public ItemStack createCropAnalyzer() {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Crop Analyzer", NamedTextColor.DARK_GREEN)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Right-click a crop to", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("analyze its traits.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, TYPE_CROP_ANALYZER);

        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createCropEditor() {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("Crop Editor", NamedTextColor.DARK_RED)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Right-click a crop to", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("edit its traits.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, TYPE_CROP_EDITOR);

        item.setItemMeta(meta);
        return item;
    }

    // ── Quality Feed items ─────────────────────────────────────────

    public ItemStack createQualityWheatFeed() {
        ItemStack item = new ItemStack(Material.WHEAT, 4);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Quality Wheat Feed", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Feed to Cows, Sheep, Mooshrooms,", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("or Goats when breeding to pass", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("down rare traits.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, TYPE_QUALITY_WHEAT_FEED);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createQualityCarrotFeed() {
        ItemStack item = new ItemStack(Material.CARROT, 4);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Quality Carrot Feed", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Feed to Pigs when breeding", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("to pass down rare traits.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, TYPE_QUALITY_CARROT_FEED);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createQualityGoldenCarrotFeed() {
        ItemStack item = new ItemStack(Material.GOLDEN_CARROT, 4);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Quality Golden Carrot Feed", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Feed to Rabbits, Horses, or", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("Donkeys when breeding to pass", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("down rare traits.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, TYPE_QUALITY_GOLDEN_CARROT_FEED);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack createQualitySeedFeed() {
        ItemStack item = new ItemStack(Material.WHEAT_SEEDS, 4);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Quality Seed Feed", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Feed to Chickens when breeding", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.text("to pass down rare traits.", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(itemTypeKey, PersistentDataType.STRING, TYPE_QUALITY_SEED_FEED);
        item.setItemMeta(meta);
        return item;
    }

    // ── Item identification ────────────────────────────────────────

    /** Returns the item type string, or null if the item is not a Husbandry item. */
    public @Nullable String getItemType(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer()
                .get(itemTypeKey, PersistentDataType.STRING);
    }

    /** Returns true if the item is any type of quality feed. */
    public boolean isQualityFeed(@Nullable ItemStack item) {
        String type = getItemType(item);
        return type != null && type.startsWith("quality_");
    }

    // ── Recipe registration ────────────────────────────────────────

    public void registerRecipes() {
        registerAnimalAnalyzerRecipe();
        registerCropAnalyzerRecipe();
        registerQualityFeedRecipes();
    }

    private void registerAnimalAnalyzerRecipe() {
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

    private void registerCropAnalyzerRecipe() {
        NamespacedKey recipeKey = new NamespacedKey(plugin, "crop_analyzer");
        ShapelessRecipe recipe = new ShapelessRecipe(recipeKey, createCropAnalyzer());
        recipe.setCategory(CraftingBookCategory.MISC);

        recipe.addIngredient(Material.COMPARATOR);
        recipe.addIngredient(Material.WHEAT_SEEDS);
        recipe.addIngredient(Material.WHEAT);
        recipe.addIngredient(Material.POTATO);
        recipe.addIngredient(Material.CARROT);
        recipe.addIngredient(Material.MELON_SEEDS);
        recipe.addIngredient(Material.MELON_SLICE);
        recipe.addIngredient(Material.PUMPKIN);
        recipe.addIngredient(Material.PUMPKIN_SEEDS);

        plugin.getServer().addRecipe(recipe);
    }

    private void registerQualityFeedRecipes() {
        // Quality Wheat Feed: 3 wheat + 3 pumpkin + 3 melon slice
        registerQualityFeedRecipe("quality_wheat_feed", createQualityWheatFeed(),
                Material.WHEAT, 3, Material.PUMPKIN, 3, Material.MELON_SLICE, 3);

        // Quality Carrot Feed: 3 carrot + 3 potato + 3 beetroot
        registerQualityFeedRecipe("quality_carrot_feed", createQualityCarrotFeed(),
                Material.CARROT, 3, Material.POTATO, 3, Material.BEETROOT, 3);

        // Quality Golden Carrot Feed: 4 carrot + 4 melon slice + 1 gold ingot
        registerQualityFeedRecipe("quality_golden_carrot_feed", createQualityGoldenCarrotFeed(),
                Material.CARROT, 4, Material.MELON_SLICE, 4, Material.GOLD_INGOT, 1);

        // Quality Seed Feed (pumpkin variant): 3 wheat seeds + 3 beetroot seeds + 3 pumpkin seeds
        registerQualityFeedRecipe("quality_seed_feed_pumpkin", createQualitySeedFeed(),
                Material.WHEAT_SEEDS, 3, Material.BEETROOT_SEEDS, 3, Material.PUMPKIN_SEEDS, 3);

        // Quality Seed Feed (melon variant): 3 wheat seeds + 3 beetroot seeds + 3 melon seeds
        registerQualityFeedRecipe("quality_seed_feed_melon", createQualitySeedFeed(),
                Material.WHEAT_SEEDS, 3, Material.BEETROOT_SEEDS, 3, Material.MELON_SEEDS, 3);
    }

    private void registerQualityFeedRecipe(String name, ItemStack result,
                                           Material mat1, int count1,
                                           Material mat2, int count2,
                                           Material mat3, int count3) {
        NamespacedKey recipeKey = new NamespacedKey(plugin, name);
        ShapelessRecipe recipe = new ShapelessRecipe(recipeKey, result);
        recipe.setCategory(CraftingBookCategory.MISC);

        for (int i = 0; i < count1; i++) recipe.addIngredient(mat1);
        for (int i = 0; i < count2; i++) recipe.addIngredient(mat2);
        for (int i = 0; i < count3; i++) recipe.addIngredient(mat3);

        plugin.getServer().addRecipe(recipe);
    }
}