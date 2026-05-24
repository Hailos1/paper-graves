package com.graves;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

public final class GravesConfig {

    private static final Map<String, String> DEFAULT_MESSAGES = Map.ofEntries(
            Map.entry("prefix", "<dark_gray>[<gold>Могилы</gold>]</dark_gray> "),
            Map.entry("grave-created", "<gray>Ваша могила создана на координатах <white>{x}, {y}, {z}</white> в мире <white>{world}</white>. Исчезнет через <white>{time}</white>.</gray>"),
            Map.entry("grave-collected", "<gray>Вы забрали вещи из своей могилы.</gray>"),
            Map.entry("grave-expired", "<gray>Ваша могила на координатах <white>{x}, {y}, {z}</white> исчезла.</gray>"),
            Map.entry("not-owner", "<red>Эта могила принадлежит другому игроку.</red>"),
            Map.entry("graves-disabled", "<red>Могилы после смерти сейчас отключены.</red>"),
            Map.entry("no-graves", "<gray>У вас нет активных могил.</gray>"),
            Map.entry("toggled-on", "<green>Могилы после смерти включены.</green>"),
            Map.entry("toggled-off", "<red>Могилы после смерти отключены.</red>"),
            Map.entry("no-permission", "<red>У вас нет прав на эту команду.</red>"),
            Map.entry("only-players", "<red>Эта команда доступна только игрокам.</red>"),
            Map.entry("list-heading", "<gray>Активные могилы: <white>{count}</white></gray>"),
            Map.entry("list-entry", "<white>{world}</white> <gray>{x}, {y}, {z} - осталось <white>{time}</white></gray>"),
            Map.entry("help-1", "<gold>/graves help</gold> <gray>- показать помощь</gray>"),
            Map.entry("help-2", "<gold>/graves list</gold> <gray>- список ваших активных могил</gray>"),
            Map.entry("help-3", "<gold>/graves toggle</gold> <gray>- включить или отключить могилы</gray>")
    );

    private static final Map<String, String> OLD_DEFAULT_MESSAGES = Map.ofEntries(
            Map.entry("prefix", "<gray>[<gold>Graves<gray>] "),
            Map.entry("grave-created", "<gray>Your grave was placed at <white>{x}, {y}, {z}<gray> in <white>{world}<gray>. Expires in <white>{time}<gray>."),
            Map.entry("grave-collected", "<gray>You collected your grave."),
            Map.entry("grave-expired", "<gray>Your grave at <white>{x}, {y}, {z}<gray> expired."),
            Map.entry("not-owner", "<red>This grave belongs to another player."),
            Map.entry("graves-disabled", "<red>Death graves are currently disabled."),
            Map.entry("no-graves", "<gray>You have no active graves."),
            Map.entry("toggled-on", "<green>Death graves enabled."),
            Map.entry("toggled-off", "<red>Death graves disabled."),
            Map.entry("no-permission", "<red>You do not have permission.")
    );

    private final JavaPlugin plugin;
    private boolean enabled;
    private int lifetimeSeconds;
    private int netherRoofY;

    public GravesConfig(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();
        migrateMessages(config);
        enabled = config.getBoolean("enabled", true);
        lifetimeSeconds = Math.max(1, config.getInt("lifetime-seconds", 3600));
        netherRoofY = config.getInt("nether-roof-y", 127);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("enabled", enabled);
        plugin.saveConfig();
    }

    public int getLifetimeSeconds() {
        return lifetimeSeconds;
    }

    public int getNetherRoofY() {
        return netherRoofY;
    }

    public long getLifetimeMillis() {
        return lifetimeSeconds * 1000L;
    }

    public String message(String key) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        String body = plugin.getConfig().getString("messages." + key, DEFAULT_MESSAGES.getOrDefault(key, ""));
        return prefix + body;
    }

    private void migrateMessages(FileConfiguration config) {
        boolean changed = false;
        for (Map.Entry<String, String> entry : DEFAULT_MESSAGES.entrySet()) {
            String path = "messages." + entry.getKey();
            String current = config.getString(path);
            if (current == null || current.equals(OLD_DEFAULT_MESSAGES.get(entry.getKey()))) {
                config.set(path, entry.getValue());
                changed = true;
            }
        }
        if (changed) {
            plugin.saveConfig();
        }
    }
}
