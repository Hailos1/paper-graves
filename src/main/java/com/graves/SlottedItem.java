package com.graves;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * Inventory item with its original slot index (0–40: storage, armor, offhand).
 */
public final class SlottedItem {

    private final int slot;
    private final ItemStack itemStack;

    public SlottedItem(int slot, ItemStack itemStack) {
        if (slot < 0) {
            throw new IllegalArgumentException("slot must be non-negative");
        }
        this.slot = slot;
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
    }

    public int slot() {
        return slot;
    }

    public ItemStack itemStack() {
        return itemStack;
    }
}
