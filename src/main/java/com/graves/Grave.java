package com.graves;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.UUID;

public final class Grave {

    private final UUID id;
    private final UUID ownerId;
    private final String ownerName;
    private final String worldName;
    private final int x;
    private final int y;
    private final int z;
    private final long createdAtMillis;
    private final long expiresAtMillis;
    private final List<SlottedItem> items;
    private volatile boolean collected;

    public Grave(
            UUID id,
            UUID ownerId,
            String ownerName,
            String worldName,
            int x,
            int y,
            int z,
            long createdAtMillis,
            long expiresAtMillis,
            List<SlottedItem> items
    ) {
        this.id = id;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.createdAtMillis = createdAtMillis;
        this.expiresAtMillis = expiresAtMillis;
        this.items = List.copyOf(items);
    }

    public UUID id() {
        return id;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public String ownerName() {
        return ownerName;
    }

    public String worldName() {
        return worldName;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public long createdAtMillis() {
        return createdAtMillis;
    }

    public long expiresAtMillis() {
        return expiresAtMillis;
    }

    public List<SlottedItem> items() {
        return items;
    }

    public boolean isCollected() {
        return collected;
    }

    void markCollected() {
        this.collected = true;
    }

    public long remainingSeconds(long nowMillis) {
        return Math.max(0, (expiresAtMillis - nowMillis + 999) / 1000);
    }

    public Location toLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z);
    }

    public String blockKey() {
        return worldName + ":" + x + ":" + y + ":" + z;
    }
}
