package com.graves;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class BukkitBlockColumnReader implements BlockColumnReader {

    private final World world;

    public BukkitBlockColumnReader(World world) {
        this.world = world;
    }

    @Override
    public boolean isNether() {
        return world.getEnvironment() == World.Environment.NETHER;
    }

    @Override
    public int minY() {
        return world.getMinHeight();
    }

    @Override
    public int maxY() {
        return world.getMaxHeight() - 1;
    }

    @Override
    public boolean isAirAt(int x, int y, int z) {
        return materialAt(x, y, z).isAir();
    }

    @Override
    public boolean isFluidAt(int x, int y, int z) {
        Material material = materialAt(x, y, z);
        return material == Material.WATER
                || material == Material.LAVA
                || material == Material.BUBBLE_COLUMN;
    }

    @Override
    public boolean isSolidAt(int x, int y, int z) {
        return materialAt(x, y, z).isSolid();
    }

    private Material materialAt(int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        return block.getType();
    }
}
