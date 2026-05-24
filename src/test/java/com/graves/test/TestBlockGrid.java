package com.graves.test;

import com.graves.BlockColumnReader;

import java.util.HashMap;
import java.util.Map;

public final class TestBlockGrid implements BlockColumnReader {

    private final boolean nether;
    private final int minY;
    private final int maxY;
    private final Map<String, CellType> blocks = new HashMap<>();

    public TestBlockGrid(boolean nether, int minY, int maxY) {
        this.nether = nether;
        this.minY = minY;
        this.maxY = maxY;
    }

    public TestBlockGrid set(int x, int y, int z, CellType type) {
        blocks.put(key(x, y, z), type);
        return this;
    }

    public TestBlockGrid fillColumn(int x, int z, int fromY, int toY, CellType type) {
        int low = Math.min(fromY, toY);
        int high = Math.max(fromY, toY);
        for (int y = low; y <= high; y++) {
            set(x, y, z, type);
        }
        return this;
    }

    @Override
    public boolean isNether() {
        return nether;
    }

    @Override
    public int minY() {
        return minY;
    }

    @Override
    public int maxY() {
        return maxY;
    }

    @Override
    public boolean isAirAt(int x, int y, int z) {
        return cellAt(x, y, z) == CellType.AIR;
    }

    @Override
    public boolean isFluidAt(int x, int y, int z) {
        return cellAt(x, y, z) == CellType.FLUID;
    }

    @Override
    public boolean isSolidAt(int x, int y, int z) {
        return cellAt(x, y, z) == CellType.SOLID;
    }

    private CellType cellAt(int x, int y, int z) {
        return blocks.getOrDefault(key(x, y, z), CellType.AIR);
    }

    private static String key(int x, int y, int z) {
        return x + ":" + y + ":" + z;
    }
}
