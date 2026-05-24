package com.graves;

import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemRestorePlannerTest {

    @Test
    void prefersOriginalSlotThenAnyFree() {
        BitSet occupied = new BitSet();
        occupied.set(5);

        List<ItemRestorePlanner.PlannedItem> items = List.of(
                new ItemRestorePlanner.PlannedItem(5, "dirt"),
                new ItemRestorePlanner.PlannedItem(10, "iron")
        );

        ItemRestorePlanner.Plan plan = ItemRestorePlanner.plan(41, occupied, items);
        assertEquals(1, plan.toOriginalSlots());
        assertEquals(1, plan.toOtherSlots());
        assertEquals(0, plan.overflow().size());
    }

    @Test
    void overflowWhenFull() {
        BitSet occupied = new BitSet();
        for (int i = 0; i < 41; i++) {
            occupied.set(i);
        }

        List<ItemRestorePlanner.PlannedItem> items = List.of(
                new ItemRestorePlanner.PlannedItem(0, "emerald")
        );

        ItemRestorePlanner.Plan plan = ItemRestorePlanner.plan(41, occupied, items);
        assertEquals(0, plan.toOriginalSlots());
        assertEquals(0, plan.toOtherSlots());
        assertEquals(1, plan.overflow().size());
    }
}
