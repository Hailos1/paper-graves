package com.graves;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Restores grave items to a player's inventory.
 */
public final class ItemRestorer {

    public record RestoreResult(int toOriginalSlots, int toOtherSlots, int dropped) {
    }

    private ItemRestorer() {
    }

    public static final int PLAYER_INVENTORY_SLOTS = 41;

    public static RestoreResult restore(Player player, List<SlottedItem> items) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] contents = copyContents(inventory);
        List<ItemStack> overflow = new ArrayList<>();

        int toOriginal = 0;
        int toOther = 0;

        for (SlottedItem slotted : items) {
            ItemStack stack = slotted.itemStack().clone();
            if (stack.getType().isAir()) {
                continue;
            }

            int slot = slotted.slot();
            if (slot >= 0 && slot < contents.length && isEmpty(contents[slot])) {
                contents[slot] = stack;
                toOriginal++;
                continue;
            }

            int free = firstEmpty(contents);
            if (free >= 0) {
                contents[free] = stack;
                toOther++;
            } else {
                overflow.add(stack);
            }
        }

        applyContents(inventory, contents);

        int dropped = 0;
        for (ItemStack stack : overflow) {
            player.getWorld().dropItemNaturally(player.getLocation(), stack);
            dropped++;
        }

        return new RestoreResult(toOriginal, toOther, dropped);
    }

    /**
     * Testable restoration without Bukkit player instance.
     */
    public static RestoreResult restoreToArray(ItemStack[] contents, List<SlottedItem> items, List<ItemStack> drops) {
        ItemStack[] working = contents.clone();
        int toOriginal = 0;
        int toOther = 0;
        int dropped = 0;

        for (SlottedItem slotted : items) {
            ItemStack stack = slotted.itemStack().clone();
            if (stack.getType().isAir()) {
                continue;
            }

            int slot = slotted.slot();
            if (slot >= 0 && slot < working.length && isEmpty(working[slot])) {
                working[slot] = stack;
                toOriginal++;
                continue;
            }

            int free = firstEmpty(working);
            if (free >= 0) {
                working[free] = stack;
                toOther++;
            } else {
                drops.add(stack);
                dropped++;
            }
        }

        System.arraycopy(working, 0, contents, 0, working.length);
        return new RestoreResult(toOriginal, toOther, dropped);
    }

    public static List<SlottedItem> captureInventory(PlayerInventory inventory) {
        List<SlottedItem> items = new ArrayList<>();

        ItemStack[] storage = inventory.getStorageContents();
        for (int i = 0; i < storage.length; i++) {
            addIfPresent(items, i, storage[i]);
        }

        ItemStack[] armor = inventory.getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            addIfPresent(items, 36 + i, armor[i]);
        }

        addIfPresent(items, 40, inventory.getItemInOffHand());
        return items;
    }

    private static void addIfPresent(List<SlottedItem> items, int slot, ItemStack stack) {
        if (stack != null && !stack.getType().isAir()) {
            items.add(new SlottedItem(slot, stack.clone()));
        }
    }

    private static ItemStack[] copyContents(PlayerInventory inventory) {
        ItemStack[] contents = new ItemStack[PLAYER_INVENTORY_SLOTS];
        ItemStack[] storage = inventory.getStorageContents();
        System.arraycopy(storage, 0, contents, 0, Math.min(storage.length, 36));

        ItemStack[] armor = inventory.getArmorContents();
        for (int i = 0; i < armor.length && i < 4; i++) {
            contents[36 + i] = cloneOrNull(armor[i]);
        }

        contents[40] = cloneOrNull(inventory.getItemInOffHand());
        return contents;
    }

    private static void applyContents(PlayerInventory inventory, ItemStack[] contents) {
        ItemStack[] storage = new ItemStack[36];
        System.arraycopy(contents, 0, storage, 0, 36);
        inventory.setStorageContents(storage);

        inventory.setArmorContents(new ItemStack[]{
                contents[36], contents[37], contents[38], contents[39]
        });
        inventory.setItemInOffHand(contents[40] != null ? contents[40] : new ItemStack(org.bukkit.Material.AIR));
    }

    private static ItemStack cloneOrNull(ItemStack stack) {
        return stack == null ? null : stack.clone();
    }

    static boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getType().isAir();
    }

    static int firstEmpty(ItemStack[] contents) {
        for (int i = 0; i < contents.length; i++) {
            if (isEmpty(contents[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Merges duplicate slot entries keeping the larger stack (defensive).
     */
    public static List<SlottedItem> normalize(List<SlottedItem> items) {
        Map<Integer, ItemStack> bySlot = new HashMap<>();
        for (SlottedItem item : items) {
            bySlot.merge(item.slot(), item.itemStack().clone(), (left, right) ->
                    left.getAmount() >= right.getAmount() ? left : right);
        }
        List<SlottedItem> normalized = new ArrayList<>(bySlot.size());
        bySlot.forEach((slot, stack) -> normalized.add(new SlottedItem(slot, stack)));
        return normalized;
    }
}
