package com.gigamind.cognify.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameTypeTest {

    @Test
    void testId() {
        assertEquals("WORD", GameType.WORD.id());
        assertEquals("MATH", GameType.MATH.id());
    }

    @Test
    void testFromId() {
        assertEquals(GameType.WORD, GameType.fromId("WORD"));
        assertEquals(GameType.MATH, GameType.fromId("math"));
        assertThrows(IllegalArgumentException.class, () -> GameType.fromId("other"));
    }
}
