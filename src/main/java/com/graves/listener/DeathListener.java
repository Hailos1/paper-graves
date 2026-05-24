package com.graves.listener;

import com.graves.GraveManager;
import com.graves.GravesConfig;
import com.graves.ItemRestorer;
import com.graves.SlottedItem;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;
import java.util.Optional;

public final class DeathListener implements Listener {

    private final GravesConfig config;
    private final GraveManager graveManager;

    public DeathListener(GravesConfig config, GraveManager graveManager) {
        this.config = config;
        this.graveManager = graveManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!config.isEnabled()) {
            return;
        }

        Player player = event.getEntity();
        List<SlottedItem> items = ItemRestorer.captureInventory(player.getInventory());
        if (items.isEmpty()) {
            return;
        }

        event.getDrops().clear();
        event.setKeepInventory(false);

        Location deathLocation = player.getLocation();
        Optional<Location> graveLocation = graveManager.createGrave(player, deathLocation, items);
        if (graveLocation.isEmpty()) {
            for (SlottedItem item : items) {
                player.getWorld().dropItemNaturally(deathLocation, item.itemStack().clone());
            }
            return;
        }

        Location loc = graveLocation.get();
        String time = GraveManager.formatDuration(config.getLifetimeSeconds());
        graveManager.sendMessage(player, config.message("grave-created")
                .replace("{x}", Integer.toString(loc.getBlockX()))
                .replace("{y}", Integer.toString(loc.getBlockY()))
                .replace("{z}", Integer.toString(loc.getBlockZ()))
                .replace("{world}", loc.getWorld().getName())
                .replace("{time}", time));
    }
}
