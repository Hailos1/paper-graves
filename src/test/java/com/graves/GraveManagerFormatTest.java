package com.graves;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraveManagerFormatTest {

    @Test
    void formatsDuration() {
        assertEquals("45с", GraveFormatting.formatDuration(45));
        assertEquals("2м 5с", GraveFormatting.formatDuration(125));
        assertEquals("1ч 0м", GraveFormatting.formatDuration(3600));
    }
}
