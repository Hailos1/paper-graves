package com.graves;

import com.graves.persistence.GraveEntryDto;
import com.graves.persistence.GraveJsonCodec.GraveExpirySplit;
import com.graves.persistence.GraveFileStore;
import com.graves.persistence.GraveJsonCodec;
import com.graves.persistence.GraveMapper;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class GraveManager {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private final JavaPlugin plugin;
    private final GravesConfig config;
    private final GraveFileStore fileStore;

    private final Map<UUID, Grave> gravesById = new ConcurrentHashMap<>();
    private final Map<String, UUID> graveIdByBlockKey = new ConcurrentHashMap<>();

    public GraveManager(JavaPlugin plugin, GravesConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.fileStore = new GraveFileStore(plugin);
    }

    public void loadPersisted() {
        GraveExpirySplit split = GraveJsonCodec.splitByExpiry(
                fileStore.load().graves(),
                Instant.now()
        );

        for (GraveEntryDto dto : split.expired()) {
            try {
                expireLoadedEntry(GraveMapper.fromDto(dto), false);
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.WARNING, "Skipping expired grave entry: " + ex.getMessage());
            }
        }

        for (GraveEntryDto dto : split.active()) {
            try {
                Grave grave = GraveMapper.fromDto(dto);
                if (!restoreGrave(grave)) {
                    plugin.getLogger().warning("Could not restore grave at "
                            + grave.worldName() + " " + grave.x() + "," + grave.y() + "," + grave.z()
                            + "; discarding.");
                    expireLoadedEntry(grave, false);
                    continue;
                }
                register(grave);
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.WARNING, "Skipping invalid grave entry: " + ex.getMessage());
            }
        }

        persist();
        plugin.getLogger().info("Loaded " + gravesById.size() + " grave(s) from graves.json.");
    }

    public void savePersisted() {
        List<GraveEntryDto> entries = new ArrayList<>();
        for (Grave grave : gravesById.values()) {
            if (!grave.isCollected()) {
                entries.add(GraveMapper.toDto(grave));
            }
        }
        fileStore.save(entries);
    }

    public Optional<Grave> getByBlock(World world, int x, int y, int z) {
        if (world == null) {
            return Optional.empty();
        }
        UUID id = graveIdByBlockKey.get(blockKey(world.getName(), x, y, z));
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(gravesById.get(id));
    }

    public List<Grave> getGravesForPlayer(UUID playerId) {
        List<Grave> result = new ArrayList<>();
        long now = System.currentTimeMillis();
        for (Grave grave : gravesById.values()) {
            if (grave.ownerId().equals(playerId) && !grave.isCollected() && grave.expiresAtMillis() > now) {
                result.add(grave);
            }
        }
        result.sort((a, b) -> Long.compare(a.createdAtMillis(), b.createdAtMillis()));
        return Collections.unmodifiableList(result);
    }

    public Optional<Location> createGrave(Player player, Location deathLocation, List<SlottedItem> items) {
        World world = deathLocation.getWorld();
        if (world == null || items.isEmpty()) {
            return Optional.empty();
        }

        int blockX = deathLocation.getBlockX();
        int blockY = deathLocation.getBlockY();
        int blockZ = deathLocation.getBlockZ();

        boolean diedOnNetherRoof = world.getEnvironment() == World.Environment.NETHER
                && blockY >= config.getNetherRoofY();

        BlockColumnReader reader = new BukkitBlockColumnReader(world);
        OptionalInt yOpt = GravePlacement.findY(
                reader,
                blockX,
                blockY,
                blockZ,
                diedOnNetherRoof,
                config.getNetherRoofY()
        );

        if (yOpt.isEmpty()) {
            plugin.getLogger().warning("No valid grave placement for " + player.getName()
                    + " at " + blockX + ", " + blockY + ", " + blockZ + " in " + world.getName());
            return Optional.empty();
        }

        int graveY = yOpt.getAsInt();
        Block block = world.getBlockAt(blockX, graveY, blockZ);
        if (!GravePlacement.canPlaceAt(reader, blockX, graveY, blockZ)) {
            return Optional.empty();
        }

        placeHeadBlock(block, player);

        long now = System.currentTimeMillis();
        Grave grave = new Grave(
                UUID.randomUUID(),
                player.getUniqueId(),
                player.getName(),
                world.getName(),
                blockX,
                graveY,
                blockZ,
                now,
                now + config.getLifetimeMillis(),
                ItemRestorer.normalize(items)
        );

        register(grave);
        persist();

        return Optional.of(new Location(world, blockX, graveY, blockZ));
    }

    public boolean tryCollect(Player player, Grave grave) {
        if (!GraveCollectionRules.canCollect(player.getUniqueId(), grave)) {
            return false;
        }

        grave.markCollected();
        removeBlock(grave);
        ItemRestorer.restore(player, grave.items());
        unregister(grave);
        persist();
        return true;
    }

    public void expireGrave(Grave grave) {
        if (grave.isCollected()) {
            return;
        }
        expireLoadedEntry(grave, true);
        persist();
    }

    public void removeAll() {
        List<Grave> snapshot = new ArrayList<>(gravesById.values());
        for (Grave grave : snapshot) {
            removeBlock(grave);
            unregister(grave);
        }
    }

    public void tickExpired() {
        long now = System.currentTimeMillis();
        for (Grave grave : new ArrayList<>(gravesById.values())) {
            if (!grave.isCollected() && grave.expiresAtMillis() <= now) {
                expireGrave(grave);
            }
        }
    }

    private void expireLoadedEntry(Grave grave, boolean notifyOwner) {
        if (grave.isCollected()) {
            return;
        }
        grave.markCollected();
        dropItems(grave);
        removeBlock(grave);
        unregister(grave);

        if (notifyOwner) {
            Player owner = Bukkit.getPlayer(grave.ownerId());
            if (owner != null && owner.isOnline()) {
                sendMessage(owner, config.message("grave-expired")
                        .replace("{x}", Integer.toString(grave.x()))
                        .replace("{y}", Integer.toString(grave.y()))
                        .replace("{z}", Integer.toString(grave.z())));
            }
        }
    }

    private boolean restoreGrave(Grave grave) {
        World world = Bukkit.getWorld(grave.worldName());
        if (world == null) {
            plugin.getLogger().warning("World '" + grave.worldName() + "' is not loaded; cannot restore grave "
                    + grave.id());
            return false;
        }

        Block block = world.getBlockAt(grave.x(), grave.y(), grave.z());
        Material type = block.getType();
        if (type == Material.PLAYER_HEAD || type == Material.PLAYER_WALL_HEAD) {
            return true;
        }

        BlockColumnReader reader = new BukkitBlockColumnReader(world);
        if (!GravePlacement.canPlaceAt(reader, grave.x(), grave.y(), grave.z())) {
            plugin.getLogger().warning("Cannot place grave block at "
                    + grave.worldName() + " " + grave.x() + "," + grave.y() + "," + grave.z()
                    + " (blocked by " + type + ")");
            return false;
        }

        OfflinePlayer owner = Bukkit.getOfflinePlayer(grave.ownerId());
        placeHeadBlock(block, owner);
        return true;
    }

    private void placeHeadBlock(Block block, OfflinePlayer owner) {
        block.setType(Material.PLAYER_HEAD, false);
        if (block.getState() instanceof Skull skull) {
            skull.setOwningPlayer(owner);
            skull.update(true, false);
        }
    }

    private void placeHeadBlock(Block block, Player owner) {
        placeHeadBlock(block, (OfflinePlayer) owner);
    }

    private void dropItems(Grave grave) {
        Location location = grave.toLocation();
        if (location == null) {
            return;
        }
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        for (SlottedItem item : grave.items()) {
            world.dropItemNaturally(location, item.itemStack().clone());
        }
    }

    private void removeBlock(Grave grave) {
        World world = Bukkit.getWorld(grave.worldName());
        if (world == null) {
            return;
        }
        Block block = world.getBlockAt(grave.x(), grave.y(), grave.z());
        if (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD) {
            block.setType(Material.AIR, false);
        }
    }

    private void register(Grave grave) {
        gravesById.put(grave.id(), grave);
        graveIdByBlockKey.put(grave.blockKey(), grave.id());
    }

    private void unregister(Grave grave) {
        gravesById.remove(grave.id());
        graveIdByBlockKey.remove(grave.blockKey());
    }

    private void persist() {
        savePersisted();
    }

    public void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(MINI_MESSAGE.deserialize(message));
    }

    public static String formatDuration(long seconds) {
        return GraveFormatting.formatDuration(seconds);
    }

    private static String blockKey(String worldName, int x, int y, int z) {
        return worldName + ":" + x + ":" + y + ":" + z;
    }
}
