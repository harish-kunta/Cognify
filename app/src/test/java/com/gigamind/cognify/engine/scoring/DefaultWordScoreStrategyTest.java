package com.gigamind.cognify.engine.scoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DefaultWordScoreStrategyTest {

    @Test
    void testCalculateScoreBasicWord() {
        ScoreStrategy strategy = new DefaultWordScoreStrategy();
        assertEquals(10, strategy.calculateScore("CAT"));
    }

    @Test
    void testCalculateScoreComplexWord() {
        ScoreStrategy strategy = new DefaultWordScoreStrategy();
        // word length 5 => base 10 + (5-3)*5 = 20; letters J and X add 8 each
        assertEquals(36, strategy.calculateScore("JAXON"));
    }

    @Test
    void testInvalidWordReturnsZero() {
        ScoreStrategy strategy = new DefaultWordScoreStrategy();
        assertEquals(0, strategy.calculateScore("hi"));
        assertEquals(0, strategy.calculateScore(null));
    }
}
