package com.gigamind.cognify.integration;

import com.gigamind.cognify.InstantExecutorExtension;
import com.gigamind.cognify.engine.GameStateManager;
import com.gigamind.cognify.engine.WordGameEngine;
import com.gigamind.cognify.util.GameConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests covering interaction between {@link WordGameEngine}
 * and {@link GameStateManager}.
 */
@ExtendWith(InstantExecutorExtension.class)
public class WordGameIntegrationTest {
    private WordGameEngine engine;
    private GameStateManager manager;

    @BeforeEach
    void setUp() {
        Set<String> dictionary = new HashSet<>();
        dictionary.add("CAT");
        dictionary.add("DOG");
        engine = new WordGameEngine(dictionary);
        manager = GameStateManager.getInstance();
        manager.reset();
    }

    @Test
    void playValidWordUpdatesGameState() {
        manager.startGame(GameConfig.WORD_DASH_DURATION_MS);
        assertTrue(manager.isGameActive());

        int points = engine.calculateScore("CAT");
        manager.addScore(points);
        manager.addUsedWord("CAT");

        assertEquals(points, manager.getScore().getValue());
        assertTrue(manager.isWordUsed("CAT"));
    }

    @Test
    void scoreIgnoredAfterGameEnds() {
        manager.startGame(GameConfig.WORD_DASH_DURATION_MS);
        manager.endGame();

        int points = engine.calculateScore("DOG");
        manager.addScore(points);
        manager.addUsedWord("DOG");

        assertEquals(0, manager.getScore().getValue());
        assertFalse(manager.isWordUsed("DOG"));
    }
}
