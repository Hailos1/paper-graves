package com.graves;

import java.util.OptionalInt;

/**
 * Finds a safe block position for a grave skull.
 */
public final class GravePlacement {

    private GravePlacement() {
    }

    /**
     * @param diedOnNetherRoof true when the player died at Y &gt;= netherRoofY in the Nether
     * @param netherRoofY      nether roof threshold (default 127)
     */
    public static OptionalInt findY(
            BlockColumnReader world,
            int x,
            int startY,
            int z,
            boolean diedOnNetherRoof,
            int netherRoofY
    ) {
        int clampedStart = clamp(startY, world.minY(), world.maxY());

        if (world.isFluidAt(x, clampedStart, z)) {
            OptionalInt fluidSurface = searchAboveFluid(world, x, clampedStart, z, diedOnNetherRoof, netherRoofY);
            if (fluidSurface.isPresent()) {
                return fluidSurface;
            }
        }

        OptionalInt upward = searchUpward(world, x, clampedStart, z, diedOnNetherRoof, netherRoofY);
        if (upward.isPresent()) {
            return upward;
        }

        if (world.isNether() && !diedOnNetherRoof) {
            int belowRoofStart = Math.min(clampedStart, netherRoofY - 1);
            OptionalInt belowRoof = searchDownward(world, x, belowRoofStart, z, diedOnNetherRoof, netherRoofY);
            if (belowRoof.isPresent()) {
                return belowRoof;
            }
        }

        return searchDownward(world, x, clampedStart - 1, z, diedOnNetherRoof, netherRoofY);
    }

    public static boolean isValidGraveY(
            BlockColumnReader world,
            int x,
            int y,
            int z,
            boolean diedOnNetherRoof,
            int netherRoofY
    ) {
        if (y < world.minY() || y > world.maxY()) {
            return false;
        }
        if (world.isNether() && !diedOnNetherRoof && y >= netherRoofY) {
            return false;
        }

        return canPlaceAt(world, x, y, z) && hasSolidSupport(world, x, y, z);
    }

    static boolean isValidFluidSurfaceY(
            BlockColumnReader world,
            int x,
            int y,
            int z,
            boolean diedOnNetherRoof,
            int netherRoofY
    ) {
        if (y < world.minY() || y > world.maxY()) {
            return false;
        }
        if (world.isNether() && !diedOnNetherRoof && y >= netherRoofY) {
            return false;
        }

        return canPlaceAt(world, x, y, z) && world.isFluidAt(x, y - 1, z);
    }

    static boolean canPlaceAt(BlockColumnReader world, int x, int y, int z) {
        if (world.isAirAt(x, y, z)) {
            return true;
        }
        return !world.isFluidAt(x, y, z) && !world.isSolidAt(x, y, z);
    }

    static boolean hasSolidSupport(BlockColumnReader world, int x, int y, int z) {
        if (world.isAirAt(x, y - 1, z) || world.isFluidAt(x, y - 1, z)) {
            return false;
        }
        return world.isSolidAt(x, y - 1, z);
    }

    private static OptionalInt searchUpward(
            BlockColumnReader world,
            int x,
            int startY,
            int z,
            boolean diedOnNetherRoof,
            int netherRoofY
    ) {
        int maxY = world.maxY();
        if (world.isNether() && !diedOnNetherRoof) {
            maxY = Math.min(maxY, netherRoofY - 1);
        }

        for (int y = startY; y <= maxY; y++) {
            if (isValidGraveY(world, x, y, z, diedOnNetherRoof, netherRoofY)) {
                return OptionalInt.of(y);
            }
        }
        return OptionalInt.empty();
    }

    private static OptionalInt searchAboveFluid(
            BlockColumnReader world,
            int x,
            int startY,
            int z,
            boolean diedOnNetherRoof,
            int netherRoofY
    ) {
        int maxY = world.maxY();
        if (world.isNether() && !diedOnNetherRoof) {
            maxY = Math.min(maxY, netherRoofY - 1);
        }

        for (int y = startY + 1; y <= maxY; y++) {
            if (world.isFluidAt(x, y, z)) {
                continue;
            }
            if (isValidFluidSurfaceY(world, x, y, z, diedOnNetherRoof, netherRoofY)) {
                return OptionalInt.of(y);
            }
            return OptionalInt.empty();
        }
        return OptionalInt.empty();
    }

    private static OptionalInt searchDownward(
            BlockColumnReader world,
            int x,
            int startY,
            int z,
            boolean diedOnNetherRoof,
            int netherRoofY
    ) {
        for (int y = startY; y >= world.minY(); y--) {
            if (isValidGraveY(world, x, y, z, diedOnNetherRoof, netherRoofY)) {
                return OptionalInt.of(y);
            }
        }
        return OptionalInt.empty();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
