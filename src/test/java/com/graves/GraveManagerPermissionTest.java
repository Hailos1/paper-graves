package com.graves;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraveManagerPermissionTest {

    @Test
    void strangerCannotCollect() {
        UUID ownerId = UUID.randomUUID();
        Grave grave = new Grave(
                UUID.randomUUID(),
                ownerId,
                "Owner",
                "world",
                0,
                64,
                0,
                System.currentTimeMillis(),
                System.currentTimeMillis() + 60_000,
                List.of()
        );

        assertFalse(GraveCollectionRules.canCollect(UUID.randomUUID(), grave));
        assertFalse(grave.isCollected());
    }

    @Test
    void ownerCanCollect() {
        UUID ownerId = UUID.randomUUID();
        Grave grave = new Grave(
                UUID.randomUUID(),
                ownerId,
                "Owner",
                "world",
                0,
                64,
                0,
                System.currentTimeMillis(),
                System.currentTimeMillis() + 60_000,
                List.of()
        );

        assertTrue(GraveCollectionRules.canCollect(ownerId, grave));
    }
}
