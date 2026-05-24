package com.graves;

import com.graves.test.CellType;
import com.graves.test.TestBlockGrid;
import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GravePlacementTest {

    @Test
    void rejectsWaterAndLava() {
        TestBlockGrid grid = new TestBlockGrid(false, -64, 319)
                .set(0, 64, 0, CellType.FLUID)
                .set(0, 63, 0, CellType.SOLID);

        assertFalse(GravePlacement.isValidGraveY(grid, 0, 64, 0, false, 127));
    }

    @Test
    void requiresAirWithSolidBelow() {
        TestBlockGrid grid = new TestBlockGrid(false, -64, 319)
                .set(0, 64, 0, CellType.AIR)
                .set(0, 63, 0, CellType.SOLID);

        assertTrue(GravePlacement.isValidGraveY(grid, 0, 64, 0, false, 127));
    }

    @Test
    void searchesUpwardFromDeathLocation() {
        TestBlockGrid grid = new TestBlockGrid(false, -64, 319)
                .set(0, 70, 0, CellType.SOLID)
                .set(0, 71, 0, CellType.SOLID)
                .set(0, 72, 0, CellType.AIR)
                .set(0, 71, 0, CellType.SOLID);

        OptionalInt y = GravePlacement.findY(grid, 0, 70, 0, false, 127);
        assertTrue(y.isPresent());
        assertEquals(72, y.getAsInt());
    }

    @Test
    void fluidDeathPlacesAboveFluidBeforeCavesBelow() {
        TestBlockGrid grid = new TestBlockGrid(false, -64, 319)
                .fillColumn(0, 0, 45, 63, CellType.FLUID)
                .set(0, 40, 0, CellType.AIR)
                .set(0, 39, 0, CellType.SOLID);

        OptionalInt y = GravePlacement.findY(grid, 0, 55, 0, false, 127);
        assertTrue(y.isPresent());
        assertEquals(64, y.getAsInt());
    }

    @Test
    void fluidDeathFallsBackToNearestSurfaceWhenFluidTopIsBlocked() {
        TestBlockGrid grid = new TestBlockGrid(false, -64, 319)
                .fillColumn(0, 0, 45, 63, CellType.FLUID)
                .set(0, 64, 0, CellType.SOLID)
                .set(0, 65, 0, CellType.AIR)
                .set(0, 40, 0, CellType.AIR)
                .set(0, 39, 0, CellType.SOLID);

        OptionalInt y = GravePlacement.findY(grid, 0, 55, 0, false, 127);
        assertTrue(y.isPresent());
        assertEquals(65, y.getAsInt());
    }

    @Test
    void netherNonRoofDeathNeverOnRoof() {
        TestBlockGrid grid = new TestBlockGrid(true, 0, 255);
        for (int y = 0; y <= 126; y++) {
            grid.set(0, y, 0, CellType.SOLID);
        }
        grid.set(0, 120, 0, CellType.AIR);
        grid.set(0, 119, 0, CellType.SOLID);
        for (int y = 127; y <= 255; y++) {
            grid.set(0, y, 0, CellType.AIR);
            grid.set(0, y - 1, 0, CellType.SOLID);
        }

        OptionalInt y = GravePlacement.findY(grid, 0, 125, 0, false, 127);
        assertTrue(y.isPresent());
        assertTrue(y.getAsInt() < 127);
    }

    @Test
    void netherRoofDeathCanPlaceOnRoof() {
        TestBlockGrid grid = new TestBlockGrid(true, 0, 255)
                .set(0, 128, 0, CellType.AIR)
                .set(0, 127, 0, CellType.SOLID);

        OptionalInt y = GravePlacement.findY(grid, 0, 128, 0, true, 127);
        assertTrue(y.isPresent());
        assertEquals(128, y.getAsInt());
    }

    @Test
    void searchesBelowRoofWhenUpwardBlocked() {
        TestBlockGrid grid = new TestBlockGrid(true, 0, 255);
        for (int y = 0; y < 127; y++) {
            grid.set(0, y, 0, CellType.SOLID);
        }
        grid.set(0, 80, 0, CellType.AIR);
        grid.set(0, 79, 0, CellType.SOLID);

        OptionalInt y = GravePlacement.findY(grid, 0, 126, 0, false, 127);
        assertTrue(y.isPresent());
        assertEquals(80, y.getAsInt());
    }
}
