package com.gigamind.cognify.engine.scoring;

import com.gigamind.cognify.util.GameConfig;

/**
 * Math scoring strategy inspired by {@link ExponentialWordScoreStrategy}.
 * Base points grow with the square of the difficulty level just like
 * longer words earn more in Word Dash. A small bonus is added for fast
 * responses instead of multiplying the entire score, keeping totals
 * closer to the word game.
 */
public class ExponentialMathScoreStrategy implements MathScoreStrategy {
    @Override
    public int calculateScore(boolean correct, long responseTimeMs, int difficulty) {
        if (!correct) {
            return -5;
        }

        int diffFactor = difficulty;
        int base = GameConfig.BASE_SCORE * diffFactor * diffFactor;
        long clamped = Math.min(responseTimeMs, GameConfig.MAX_RESPONSE_TIME_MS);

        double bonusFactor = (double) (GameConfig.MAX_RESPONSE_TIME_MS - clamped)
                / GameConfig.MAX_RESPONSE_TIME_MS;
        double bonus = GameConfig.LENGTH_BONUS * diffFactor * bonusFactor;

        return (int) Math.round(base + bonus);
    }
}
