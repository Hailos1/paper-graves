package com.graves;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class GravesConfig {

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
        String body = plugin.getConfig().getString("messages." + key, "");
        return prefix + body;
    }
}
