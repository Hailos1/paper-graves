package com.graves.listener;

import com.graves.Grave;
import com.graves.GraveManager;
import com.graves.GravesConfig;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public final class GraveInteractListener implements Listener {

    private final GravesConfig config;
    private final GraveManager graveManager;

    public GraveInteractListener(GravesConfig config, GraveManager graveManager) {
        this.config = config;
        this.graveManager = graveManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Optional<Grave> graveOpt = graveManager.getByBlock(block.getWorld(), block.getX(), block.getY(), block.getZ());
        if (graveOpt.isEmpty()) {
            return;
        }

        event.setDropItems(false);
        event.setCancelled(true);
        handleCollect(event.getPlayer(), graveOpt.get());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Optional<Grave> graveOpt = graveManager.getByBlock(block.getWorld(), block.getX(), block.getY(), block.getZ());
        if (graveOpt.isEmpty()) {
            return;
        }

        event.setUseInteractedBlock(Event.Result.DENY);
        event.setCancelled(true);
        handleCollect(event.getPlayer(), graveOpt.get());
    }

    private void handleCollect(Player player, Grave grave) {
        if (!grave.ownerId().equals(player.getUniqueId())) {
            graveManager.sendMessage(player, config.message("not-owner"));
            return;
        }

        if (graveManager.tryCollect(player, grave)) {
            graveManager.sendMessage(player, config.message("grave-collected"));
        }
    }
}
