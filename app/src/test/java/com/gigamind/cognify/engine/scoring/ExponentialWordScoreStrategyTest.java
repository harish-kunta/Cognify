package com.gigamind.cognify.engine.scoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExponentialWordScoreStrategyTest {

    @Test
    void testLongWordScoresHigher() {
        ScoreStrategy strategy = new ExponentialWordScoreStrategy();
        assertTrue(strategy.calculateScore("QUIZ") > strategy.calculateScore("CAT"));
    }

    @Test
    void testInvalidWordReturnsZero() {
        ScoreStrategy strategy = new ExponentialWordScoreStrategy();
        assertEquals(0, strategy.calculateScore(null));
        assertEquals(0, strategy.calculateScore("hi"));
    }
}
