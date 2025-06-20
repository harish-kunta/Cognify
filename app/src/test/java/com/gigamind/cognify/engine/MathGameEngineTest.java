package com.gigamind.cognify.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.gigamind.cognify.util.GameConfig;

import static org.junit.jupiter.api.Assertions.*;

public class MathGameEngineTest {
    private MathGameEngine engine;

    @BeforeEach
    void setUp() {
        engine = new MathGameEngine();
    }

    @Test
    void testGenerateQuestionAndOptions() {
        engine.generateQuestion();

        String q = engine.getCurrentQuestion();
        assertNotNull(q, "Question should not be null");
        //assertTrue(q.matches("\\d+ [+\-\u00d7\u00f7] \\d+ = \\?"), "Question format invalid: " + q);

        int answer = engine.getCurrentAnswer();
        assertTrue(answer > 1, "Answer should be at least 2");

        List<Integer> options = engine.getOptions();
        assertEquals(6, options.size(), "There should be six options");

        Set<Integer> unique = new HashSet<>(options);
        assertEquals(6, unique.size(), "Options should be unique");
        assertTrue(options.contains(answer), "Options should contain the correct answer");
    }

    @Test
    void testOptionsWithinRange() {
        engine.generateQuestion();
        int answer = engine.getCurrentAnswer();
        for (int option : engine.getOptions()) {
            assertTrue(option > 0, "Options should be positive");
            assertTrue(Math.abs(option - answer) <= 5,
                    "Option out of range from correct answer");
        }
    }

    @Test
    void testCheckAnswerAndScore() {
        engine.generateQuestion();
        int correct = engine.getCurrentAnswer();

        assertTrue(engine.checkAnswer(correct));
        assertFalse(engine.checkAnswer(correct + 1));

        int difficulty = engine.getCurrentDifficulty();
        int base = (GameConfig.BASE_SCORE / 2) * difficulty * difficulty
                + (GameConfig.BASE_SCORE / 2);
        int expectedFast = base + GameConfig.LENGTH_BONUS * difficulty;
        assertEquals(expectedFast, engine.getScore(true, 0));
        int expectedPenalty = -GameConfig.BASE_SCORE * difficulty;
        assertEquals(expectedPenalty, engine.getScore(false, 0));

        // After MAX_RESPONSE_TIME_MS, base points are awarded
        assertEquals(base, engine.getScore(true, GameConfig.MAX_RESPONSE_TIME_MS + 1000));
    }
}
