package com.gigamind.cognify.engine;

import com.gigamind.cognify.InstantExecutorExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InstantExecutorExtension.class)
public class GameStateManagerTest {
    private GameStateManager manager;

    @BeforeEach
    void setUp() {
        manager = GameStateManager.getInstance();
        manager.reset();
    }

    @Test
    void testStartAndAddScore() {
        manager.startGame(1000L);
        assertTrue(manager.isGameActive());
        assertEquals(0, manager.getScore().getValue());
        assertEquals(1000L, manager.getTimeRemaining().getValue());

        manager.addScore(15);
        assertEquals(15, manager.getScore().getValue());

        manager.endGame();
        assertFalse(manager.isGameActive());
        manager.addScore(5);
        assertEquals(15, manager.getScore().getValue(), "Score should not change after game ended");
    }

    @Test
    void testUsedWordsAndTime() {
        manager.startGame(5000L);
        assertFalse(manager.isWordUsed("TEST"));
        manager.addUsedWord("TEST");
        assertTrue(manager.isWordUsed("TEST"));

        manager.updateTimeRemaining(2500L);
        assertEquals(2500L, manager.getTimeRemaining().getValue());
    }

    @Test
    void testReset() {
        manager.startGame(1000L);
        manager.addScore(20);
        manager.addUsedWord("HELLO");

        manager.reset();
        assertFalse(manager.isGameActive());
        assertEquals(0, manager.getScore().getValue());
        assertEquals(0L, manager.getTimeRemaining().getValue());
        assertFalse(manager.isWordUsed("HELLO"));
    }

    @Test
    void testStartGameTwiceResetsState() {
        manager.startGame(1000L);
        manager.addScore(10);
        manager.addUsedWord("WORD");

        manager.startGame(2000L);

        assertTrue(manager.isGameActive());
        assertEquals(0, manager.getScore().getValue());
        assertFalse(manager.isWordUsed("WORD"));
        assertEquals(2000L, manager.getTimeRemaining().getValue());
    }
}
