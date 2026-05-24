package com.graves;

import com.graves.listener.DeathListener;
import com.graves.listener.GraveInteractListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class GravesPlugin extends JavaPlugin {

    private GravesConfig gravesConfig;
    private GraveManager graveManager;
    private BukkitTask expiryTask;

    @Override
    public void onEnable() {
        gravesConfig = new GravesConfig(this);
        gravesConfig.reload();
        graveManager = new GraveManager(this, gravesConfig);
        graveManager.loadPersisted();

        getServer().getPluginManager().registerEvents(new DeathListener(gravesConfig, graveManager), this);
        getServer().getPluginManager().registerEvents(new GraveInteractListener(gravesConfig, graveManager), this);

        GravesCommand gravesCommand = new GravesCommand(gravesConfig, graveManager);
        PluginCommand command = getCommand("graves");
        if (command != null) {
            command.setExecutor(gravesCommand);
            command.setTabCompleter(gravesCommand);
        } else {
            getLogger().warning("Command /graves is not defined in plugin.yml");
        }

        expiryTask = getServer().getScheduler().runTaskTimer(this, graveManager::tickExpired, 20L, 20L);
        getLogger().info("PaperGraves enabled. Lifetime: " + gravesConfig.getLifetimeSeconds() + "s.");
    }

    @Override
    public void onDisable() {
        if (expiryTask != null) {
            expiryTask.cancel();
        }
        if (graveManager != null) {
            graveManager.savePersisted();
        }
    }

    public GravesConfig gravesConfig() {
        return gravesConfig;
    }

    public GraveManager graveManager() {
        return graveManager;
    }
}
