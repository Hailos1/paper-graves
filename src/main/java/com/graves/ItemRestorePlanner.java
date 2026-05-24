package com.graves;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Pure-Java inventory restoration planning (slot assignment).
 */
public final class ItemRestorePlanner {

    public record PlannedItem(int preferredSlot, String itemKey) {
    }

    public record Plan(int toOriginalSlots, int toOtherSlots, List<PlannedItem> overflow) {
    }

    private ItemRestorePlanner() {
    }

    public static Plan plan(int inventorySize, BitSet occupied, List<PlannedItem> items) {
        BitSet working = (BitSet) occupied.clone();
        int toOriginal = 0;
        int toOther = 0;
        List<PlannedItem> overflow = new ArrayList<>();

        for (PlannedItem item : items) {
            int preferred = item.preferredSlot();
            if (preferred >= 0 && preferred < inventorySize && !working.get(preferred)) {
                working.set(preferred);
                toOriginal++;
                continue;
            }

            int free = working.nextClearBit(0);
            if (free < inventorySize) {
                working.set(free);
                toOther++;
            } else {
                overflow.add(item);
            }
        }

        return new Plan(toOriginal, toOther, overflow);
    }
}
