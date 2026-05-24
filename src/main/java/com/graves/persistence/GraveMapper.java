package com.graves.persistence;

import com.graves.Grave;
import com.graves.SlottedItem;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class GraveMapper {

    private GraveMapper() {
    }

    public static GraveEntryDto toDto(Grave grave) {
        List<SlottedItemDto> items = new ArrayList<>(grave.items().size());
        for (SlottedItem item : grave.items()) {
            items.add(new SlottedItemDto(item.slot(), item.itemStack().serialize()));
        }
        return new GraveEntryDto(
                grave.id().toString(),
                grave.ownerId().toString(),
                grave.ownerName(),
                grave.worldName(),
                grave.x(),
                grave.y(),
                grave.z(),
                Instant.ofEpochMilli(grave.createdAtMillis()).toString(),
                Instant.ofEpochMilli(grave.expiresAtMillis()).toString(),
                List.copyOf(items)
        );
    }

    public static Grave fromDto(GraveEntryDto dto) {
        List<SlottedItem> items = new ArrayList<>(dto.items().size());
        for (SlottedItemDto itemDto : dto.items()) {
            ItemStack stack = ItemStack.deserialize(itemDto.stack());
            items.add(new SlottedItem(itemDto.slot(), stack));
        }
        return new Grave(
                UUID.fromString(dto.id()),
                UUID.fromString(dto.ownerId()),
                dto.ownerName(),
                dto.world(),
                dto.x(),
                dto.y(),
                dto.z(),
                Instant.parse(dto.createdAtUtc()).toEpochMilli(),
                Instant.parse(dto.expiresAtUtc()).toEpochMilli(),
                items
        );
    }

    public static GraveDocument toDocument(List<Grave> graves) {
        List<GraveEntryDto> entries = new ArrayList<>(graves.size());
        for (Grave grave : graves) {
            entries.add(toDto(grave));
        }
        return new GraveDocument(GraveJsonCodec.CURRENT_VERSION, List.copyOf(entries));
    }
}
