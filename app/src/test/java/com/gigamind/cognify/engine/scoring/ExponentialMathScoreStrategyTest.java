package com.gigamind.cognify.engine.scoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExponentialMathScoreStrategyTest {

    @Test
    void testHigherDifficultyScoresMore() {
        MathScoreStrategy strategy = new ExponentialMathScoreStrategy();
        int easy = strategy.calculateScore(true, 0, 1);
        int hard = strategy.calculateScore(true, 0, 3);
        assertTrue(hard > easy);
    }

    @Test
    void testIncorrectAnswerPenalized() {
        MathScoreStrategy strategy = new ExponentialMathScoreStrategy();
        assertEquals(-5, strategy.calculateScore(false, 1000, 2));
    }
}
