package com.gigamind.cognify.util;

public final class GameConfig {
    // Game durations
    // Duration for the Word Dash game in milliseconds (60 seconds)
    public static final long WORD_DASH_DURATION_MS = 60_000;
    public static final long QUICK_MATH_DURATION_MS = 60_000; // 60 seconds

    // When less than this time remains, trigger final countdown effects
    public static final long FINAL_COUNTDOWN_MS = 10_000;

    // Max time considered for full points in Quick Math
    public static final long MAX_RESPONSE_TIME_MS = 5_000;


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
