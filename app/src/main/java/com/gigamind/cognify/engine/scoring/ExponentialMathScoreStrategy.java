package com.gigamind.cognify.engine.scoring;

import com.gigamind.cognify.util.GameConfig;

/**
 * Math scoring strategy that heavily rewards higher difficulty
 * questions. Score grows with the square of the difficulty level
 * so that solving complex problems yields substantially more points.
 */
public class ExponentialMathScoreStrategy implements MathScoreStrategy {
    @Override
    public int calculateScore(boolean correct, long responseTimeMs, int difficulty) {
        if (!correct) {
            return -5;
        }

        int diffFactor = difficulty * difficulty;
        int base = GameConfig.BASE_SCORE * diffFactor;
        long clamped = Math.min(responseTimeMs, GameConfig.MAX_RESPONSE_TIME_MS);
        double timeFactor = 1.0 + (double) (GameConfig.MAX_RESPONSE_TIME_MS - clamped)
                / GameConfig.MAX_RESPONSE_TIME_MS;
        return (int) Math.round(base * timeFactor);
    }
}
