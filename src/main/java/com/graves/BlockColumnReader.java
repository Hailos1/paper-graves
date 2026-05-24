package com.graves;

/**
 * Abstraction for block lookups used by {@link GravePlacement}.
 */
public interface BlockColumnReader {

    boolean isNether();

    int minY();

    int maxY();

    boolean isAirAt(int x, int y, int z);

    boolean isFluidAt(int x, int y, int z);

    boolean isSolidAt(int x, int y, int z);
}
