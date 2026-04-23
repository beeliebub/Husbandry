package me.beeliebub.husbandry.command;

import me.beeliebub.husbandry.crop.CropData;
import me.beeliebub.husbandry.item.HusbandryItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Handles {@code /husbandry <subcommand>} admin commands.
 */
public class HusbandryCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of(
            "analyzer", "editor", "crop_analyzer", "crop_editor",
            "quality_wheat_feed", "quality_carrot_feed",
            "quality_golden_carrot_feed", "quality_seed_feed",
            "quality_crop"
    );

    /** All crop materials that can receive the quality_crop marker. */
    private static final List<String> QUALITY_CROP_TYPES = List.of(
            "wheat", "beetroot", "carrot", "potato",
            "wheat_seeds", "beetroot_seeds", "pumpkin_seeds", "melon_seeds",
            "pumpkin", "melon_slice"
    );

    private final HusbandryItems husbandryItems;
    private final CropData cropData;

    public HusbandryCommand(HusbandryItems husbandryItems, CropData cropData) {
        this.husbandryItems = husbandryItems;
        this.cropData = cropData;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /husbandry <" + String.join("|", SUBCOMMANDS) + ">",
                    NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "analyzer" -> give(player, husbandryItems.createAnalyzer(), "Trait Analyzer");
            case "editor" -> give(player, husbandryItems.createEditor(), "Trait Editor");
            case "crop_analyzer" -> give(player, husbandryItems.createCropAnalyzer(), "Crop Analyzer");
            case "crop_editor" -> give(player, husbandryItems.createCropEditor(), "Crop Editor");
            case "quality_wheat_feed" -> give(player, husbandryItems.createQualityWheatFeed(), "Quality Wheat Feed");
            case "quality_carrot_feed" -> give(player, husbandryItems.createQualityCarrotFeed(), "Quality Carrot Feed");
            case "quality_golden_carrot_feed" -> give(player, husbandryItems.createQualityGoldenCarrotFeed(), "Quality Golden Carrot Feed");
            case "quality_seed_feed" -> give(player, husbandryItems.createQualitySeedFeed(), "Quality Seed Feed");
            case "quality_crop" -> handleQualityCrop(player, args);
            default -> player.sendMessage(
                    Component.text("Unknown subcommand. Use: /husbandry <" + String.join("|", SUBCOMMANDS) + ">",
                            NamedTextColor.RED));
        }

        return true;
    }

    private void handleQualityCrop(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text(
                    "Usage: /husbandry quality_crop <" + String.join("|", QUALITY_CROP_TYPES) + ">",
                    NamedTextColor.RED));
            return;
        }

        Material material = switch (args[1].toLowerCase()) {
            case "wheat" -> Material.WHEAT;
            case "beetroot" -> Material.BEETROOT;
            case "carrot" -> Material.CARROT;
            case "potato" -> Material.POTATO;
            case "wheat_seeds" -> Material.WHEAT_SEEDS;
            case "beetroot_seeds" -> Material.BEETROOT_SEEDS;
            case "pumpkin_seeds" -> Material.PUMPKIN_SEEDS;
            case "melon_seeds" -> Material.MELON_SEEDS;
            case "pumpkin" -> Material.PUMPKIN;
            case "melon_slice" -> Material.MELON_SLICE;
            default -> null;
        };

        if (material == null) {
            player.sendMessage(Component.text(
                    "Unknown crop type. Use: /husbandry quality_crop <" + String.join("|", QUALITY_CROP_TYPES) + ">",
                    NamedTextColor.RED));
            return;
        }

        ItemStack item = new ItemStack(material);
        cropData.applyToItem(item, EnumSet.noneOf(me.beeliebub.husbandry.crop.CropTrait.class),
                true, CropData.materialDisplayName(material));
        give(player, item, "Quality " + CropData.materialDisplayName(material));
    }

    private void give(Player player, ItemStack item, String name) {
        player.getInventory().addItem(item);
        player.sendMessage(Component.text(name + " added to your inventory.", NamedTextColor.GREEN));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("quality_crop")) {
            return QUALITY_CROP_TYPES.stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}