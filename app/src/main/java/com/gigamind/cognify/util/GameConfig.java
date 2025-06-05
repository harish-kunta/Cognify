package com.gigamind.cognify.util;

public final class GameConfig {
    // Game durations
    public static final long WORD_DASH_DURATION_MS = 15_000; // 60 seconds
    public static final long QUICK_MATH_DURATION_MS = 45_000; // 45 seconds

    // Word game settings
    public static final int MIN_WORD_LENGTH = 3;
    public static final int GRID_SIZE = 4;
    public static final int TOTAL_LETTERS = GRID_SIZE * GRID_SIZE;

    // Scoring configuration
    public static final int BASE_SCORE = 10;
    public static final int LENGTH_BONUS = 5;
    public static final int COMPLEXITY_BONUS = 8;

    // Animation durations
    public static final long BUTTON_SCALE_DURATION_MS = 80;
    public static final long SCORE_ANIMATION_DURATION_MS = 100;

    private GameConfig() {
        // Prevent instantiation
        throw new AssertionError("No instances allowed");
    }
}
