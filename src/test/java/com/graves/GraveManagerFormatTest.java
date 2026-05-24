package com.graves;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraveManagerFormatTest {

    @Test
    void formatsDuration() {
        assertEquals("45s", GraveFormatting.formatDuration(45));
        assertEquals("2m 5s", GraveFormatting.formatDuration(125));
        assertEquals("1h 0m", GraveFormatting.formatDuration(3600));
    }
}
