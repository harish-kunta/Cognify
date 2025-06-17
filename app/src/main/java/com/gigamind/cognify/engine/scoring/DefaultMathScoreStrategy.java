package com.gigamind.cognify.engine.scoring;

import com.gigamind.cognify.util.GameConfig;

/**
 * Default implementation of {@link MathScoreStrategy} mirroring the original
 * scoring logic from {@code MathGameEngine}.
 */
public class DefaultMathScoreStrategy implements MathScoreStrategy {

    @Override
    public int calculateScore(boolean correct, long responseTimeMs, int difficulty) {
        if (!correct) {
            return -5;
        }

        int base = GameConfig.BASE_SCORE * difficulty;
        long clamped = Math.min(responseTimeMs, GameConfig.MAX_RESPONSE_TIME_MS);
        double factor = 1.0
                + (double) (GameConfig.MAX_RESPONSE_TIME_MS - clamped)
                / GameConfig.MAX_RESPONSE_TIME_MS;

        return (int) Math.round(base * factor);
    }
}

