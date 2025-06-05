package com.gigamind.cognify.util;

/**
 * Enum representing the different game types available in the app.
 */
public enum GameType {
    WORD("WORD"),
    MATH("MATH");

    private final String id;

    GameType(String id) {
        this.id = id;
    }

    /**
     * Returns the identifier string used for analytics and intents.
     */
    public String id() {
        return id;
    }

    /**
     * Converts a string identifier back into a {@link GameType}.
     *
     * @throws IllegalArgumentException if the id does not match any type
     */
    public static GameType fromId(String id) {
        for (GameType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown game type: " + id);
    }
}
