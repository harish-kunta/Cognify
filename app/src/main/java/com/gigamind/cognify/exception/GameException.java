package com.gigamind.cognify.exception;

public class GameException extends RuntimeException {
    public enum ErrorCode {
        INVALID_WORD_LENGTH,
        WORD_ALREADY_USED,
        INVALID_WORD_FORMATION,
        DICTIONARY_LOAD_ERROR,
        GAME_NOT_ACTIVE,
        WORD_NOT_IN_DICTIONARY
    }

    private final ErrorCode errorCode;

    public GameException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public GameException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}