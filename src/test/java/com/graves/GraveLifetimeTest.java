package com.graves;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraveLifetimeTest {

    @Test
    void remainingSecondsRoundsUp() {
        Grave grave = new Grave(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Steve",
                "world",
                1,
                2,
                3,
                0L,
                5500L,
                List.of()
        );

        assertEquals(6, grave.remainingSeconds(0));
        assertEquals(1, grave.remainingSeconds(5000));
        assertEquals(0, grave.remainingSeconds(6000));
    }
}
