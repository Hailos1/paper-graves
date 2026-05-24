package com.graves.persistence;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraveJsonCodecTest {

    @Test
    void roundTripsDocument() {
        GraveEntryDto entry = sampleEntry(
                "2026-05-24T08:00:00Z",
                "2026-05-24T09:00:00Z"
        );
        GraveDocument original = new GraveDocument(GraveJsonCodec.CURRENT_VERSION, List.of(entry));

        GraveDocument decoded = GraveJsonCodec.decode(GraveJsonCodec.encode(original));

        assertEquals(GraveJsonCodec.CURRENT_VERSION, decoded.version());
        assertEquals(1, decoded.graves().size());
        GraveEntryDto grave = decoded.graves().getFirst();
        assertEquals(entry.id(), grave.id());
        assertEquals(entry.ownerId(), grave.ownerId());
        assertEquals(entry.ownerName(), grave.ownerName());
        assertEquals(entry.world(), grave.world());
        assertEquals(10, grave.x());
        assertEquals(64, grave.y());
        assertEquals(-5, grave.z());
        assertEquals("2026-05-24T08:00:00Z", grave.createdAtUtc());
        assertEquals("2026-05-24T09:00:00Z", grave.expiresAtUtc());
        assertEquals(1, grave.items().size());
        assertEquals(3, grave.items().getFirst().slot());
        assertEquals("DIAMOND_SWORD", grave.items().getFirst().stack().get("type"));
    }

    @Test
    void emptyJsonReturnsEmptyDocument() {
        GraveDocument document = GraveJsonCodec.decode("");
        assertEquals(GraveJsonCodec.CURRENT_VERSION, document.version());
        assertTrue(document.graves().isEmpty());
    }

    @Test
    void splitByExpiryUsesUtcInstant() {
        GraveEntryDto active = sampleEntry(
                "2026-05-24T08:00:00Z",
                "2026-05-24T10:00:01Z"
        );
        GraveEntryDto expired = sampleEntry(
                "2026-05-24T07:00:00Z",
                "2026-05-24T10:00:00Z"
        );

        GraveJsonCodec.GraveExpirySplit split = GraveJsonCodec.splitByExpiry(
                List.of(active, expired),
                Instant.parse("2026-05-24T10:00:00Z")
        );

        assertEquals(1, split.active().size());
        assertEquals(active.id(), split.active().getFirst().id());
        assertEquals(1, split.expired().size());
        assertEquals(expired.id(), split.expired().getFirst().id());
    }

    @Test
    void isExpiredWhenExpiresAtNotAfterNow() {
        GraveEntryDto entry = sampleEntry(
                "2026-05-24T08:00:00Z",
                "2026-05-24T10:00:00Z"
        );
        assertFalse(GraveJsonCodec.isExpired(entry, Instant.parse("2026-05-24T09:59:59Z")));
        assertTrue(GraveJsonCodec.isExpired(entry, Instant.parse("2026-05-24T10:00:00Z")));
        assertTrue(GraveJsonCodec.isExpired(entry, Instant.parse("2026-05-24T10:00:01Z")));
    }

    private static GraveEntryDto sampleEntry(String createdAtUtc, String expiresAtUtc) {
        return new GraveEntryDto(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "Steve",
                "world_nether",
                10,
                64,
                -5,
                createdAtUtc,
                expiresAtUtc,
                List.of(new SlottedItemDto(3, Map.of(
                        "type", "DIAMOND_SWORD",
                        "v", 4189,
                        "amount", 1
                )))
        );
    }
}
