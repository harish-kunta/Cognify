package com.gigamind.cognify.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gigamind.cognify.util.GameConfig;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class WordGameEngineTest {
    private WordGameEngine engine;
    private Set<String> dictionary;

    @BeforeEach
    void setUp() {
        dictionary = new HashSet<>();
        dictionary.add("CAT");
        dictionary.add("DOG");
        dictionary.add("QUIZ");
        dictionary.add("HI"); // below MIN_WORD_LENGTH should be ignored in isValidWord
        engine = new WordGameEngine(dictionary);
    }

    @Test
    void testGenerateGridContainsRequiredVowels() {
        char[] grid = engine.generateGrid();
        assertEquals(16, grid.length, "Grid should contain 16 letters");

        int vowels = 0;
        for (char c : grid) {
            if ("AEIOU".indexOf(c) >= 0) {
                vowels++;
            }
            assertTrue(Character.isUpperCase(c), "Letters should be uppercase");
        }
        assertTrue(vowels >= 2, "Grid should contain at least two vowels");
        assertTrue(vowels <= 8, "Grid should contain at most half vowels");
    }

    @Test
    void testIsValidWord() {
        assertTrue(engine.isValidWord("CAT"));
        assertTrue(engine.isValidWord("dog"), "Word lookup should be case-insensitive");
        assertFalse(engine.isValidWord("HI"), "Word shorter than min length should be invalid");
        assertFalse(engine.isValidWord("BIRD"), "Unknown word should be invalid");
        assertFalse(engine.isValidWord(null), "Null word should be invalid");
    }

    @Test
    void testIsValidWordEdgeCases() {
        assertFalse(engine.isValidWord(""), "Empty string should be invalid");
        assertFalse(engine.isValidWord("   "), "Whitespace-only word should be invalid");
        assertFalse(engine.isValidWord("123"), "Numeric word should be invalid");
        assertFalse(engine.isValidWord("CAT "), "Word with trailing space should be invalid");
    }

    @Test
    void testCalculateScore() {
        // Base score 10 for a minimum length word
        assertEquals(10, engine.calculateScore("CAT"));

        // Word with uncommon letters Q and Z should give larger bonus
        assertEquals(57, engine.calculateScore("QUIZ"));

        // Word with half complexity letters like W should add half bonus
        dictionary.add("BOW");
        assertEquals(14, engine.calculateScore("BOW"));

        // Invalid word should yield zero
        assertEquals(0, engine.calculateScore("BIRD"));
    }
}
