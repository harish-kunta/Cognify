package com.gigamind.cognify.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameExceptionTest {
    @Test
    void constructorSetsMessageAndCode() {
        GameException ex = new GameException(GameException.ErrorCode.INVALID_WORD_LENGTH,
                "Too short");
        assertEquals(GameException.ErrorCode.INVALID_WORD_LENGTH, ex.getErrorCode());
        assertEquals("Too short", ex.getMessage());
    }

    @Test
    void constructorWithCausePreservesCause() {
        Throwable cause = new IllegalArgumentException("bad");
        GameException ex = new GameException(GameException.ErrorCode.WORD_ALREADY_USED,
                "Used", cause);
        assertEquals(cause, ex.getCause());
        assertEquals(GameException.ErrorCode.WORD_ALREADY_USED, ex.getErrorCode());
    }
}
