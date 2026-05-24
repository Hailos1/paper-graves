package com.graves;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class GravesCommand implements CommandExecutor, TabCompleter {

    private final GravesConfig config;
    private final GraveManager graveManager;

    public GravesCommand(GravesConfig config, GraveManager graveManager) {
        this.config = config;
        this.graveManager = graveManager;
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        return switch (sub) {
            case "help", "?" -> {
                sendHelp(sender);
                yield true;
            }
            case "list", "ls" -> handleList(sender);
            case "toggle", "on", "off" -> handleToggle(sender, sub);
            default -> {
                sendHelp(sender);
                yield true;
            }
        };
    }

    private boolean handleList(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can list their graves.");
            return true;
        }
        if (!sender.hasPermission("graves.list")) {
            graveManager.sendMessage(player, config.message("no-permission"));
            return true;
        }

        List<Grave> graves = graveManager.getGravesForPlayer(player.getUniqueId());
        if (graves.isEmpty()) {
            graveManager.sendMessage(player, config.message("no-graves"));
            return true;
        }

        long now = System.currentTimeMillis();
        graveManager.sendMessage(player, "&7Active graves (" + graves.size() + "):");
        for (Grave grave : graves) {
            long remaining = grave.remainingSeconds(now);
            String line = "&f" + grave.worldName() + " "
                    + grave.x() + ", " + grave.y() + ", " + grave.z()
                    + " &7- &f" + GraveManager.formatDuration(remaining) + " left";
            graveManager.sendMessage(player, line);
        }
        return true;
    }

    private boolean handleToggle(CommandSender sender, String sub) {
        if (!sender.hasPermission("graves.admin")) {
            if (sender instanceof Player player) {
                graveManager.sendMessage(player, config.message("no-permission"));
            } else {
                sender.sendMessage("No permission.");
            }
            return true;
        }

        boolean newValue = switch (sub) {
            case "on" -> true;
            case "off" -> false;
            default -> !config.isEnabled();
        };
        config.setEnabled(newValue);
        String message = newValue ? config.message("toggled-on") : config.message("toggled-off");
        if (sender instanceof Player player) {
            graveManager.sendMessage(player, message);
        } else {
            sender.sendMessage(message.replace('&', '§'));
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6/graves help §7- show this help");
        sender.sendMessage("§6/graves list §7- list your active graves");
        sender.sendMessage("§6/graves toggle §7- enable/disable graves (admin)");
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>(Arrays.asList("help", "list"));
            if (sender.hasPermission("graves.admin")) {
                options.addAll(Arrays.asList("toggle", "on", "off"));
            }
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return options.stream().filter(s -> s.startsWith(prefix)).toList();
        }
        return List.of();
    }
}
