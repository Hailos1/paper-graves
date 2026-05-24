package com.graves.persistence;

import java.util.List;

public record GraveEntryDto(
        String id,
        String ownerId,
        String ownerName,
        String world,
        int x,
        int y,
        int z,
        String createdAtUtc,
        String expiresAtUtc,
        List<SlottedItemDto> items
) {
}
