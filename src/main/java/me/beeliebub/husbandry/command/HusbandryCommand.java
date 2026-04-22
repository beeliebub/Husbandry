package me.beeliebub.husbandry.command;

import me.beeliebub.husbandry.item.HusbandryItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * Handles {@code /husbandry <analyzer|editor>} admin commands.
 */
public class HusbandryCommand implements CommandExecutor, TabCompleter {

    private final HusbandryItems husbandryItems;

    public HusbandryCommand(HusbandryItems husbandryItems) {
        this.husbandryItems = husbandryItems;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /husbandry <analyzer|editor>", NamedTextColor.RED));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "analyzer" -> {
                player.getInventory().addItem(husbandryItems.createAnalyzer());
                player.sendMessage(Component.text("Trait Analyzer added to your inventory.", NamedTextColor.GREEN));
            }
            case "editor" -> {
                player.getInventory().addItem(husbandryItems.createEditor());
                player.sendMessage(Component.text("Trait Editor added to your inventory.", NamedTextColor.GREEN));
            }
            default -> player.sendMessage(
                    Component.text("Unknown subcommand. Use: /husbandry <analyzer|editor>", NamedTextColor.RED));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("analyzer", "editor")
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}