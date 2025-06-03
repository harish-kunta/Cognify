package com.gigamind.cognify.engine;

import android.content.Context;
import android.content.res.AssetManager;

import com.gigamind.cognify.util.GameConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WordGameEngineTest {

    @Mock
    private Context mockContext;

    @Mock
    private AssetManager mockAssetManager;

    private WordGameEngine wordGameEngine;

    private static final String TEST_DICTIONARY =
            "CAT\n" +        // Below min length
                    "WORD\n" +       // Valid
                    "QUIZ\n" +       // Valid with complex letters
                    "JAZZ\n" +       // Valid with complex letters
                    "XYLOPHONE\n" +  // Valid long word with complex letters
                    "TEST\n" +       // Valid
                    "VOWEL";         // Valid

    @BeforeEach
    void setUp() throws Exception {
        // Ensure our Context.getAssets() returns the mocked AssetManager
        when(mockContext.getAssets()).thenReturn(mockAssetManager);

        // Provide a fake InputStream for “words.txt”
        InputStream dictionaryStream = new ByteArrayInputStream(
                TEST_DICTIONARY.getBytes(StandardCharsets.UTF_8));
        when(mockAssetManager.open(anyString()))
                .thenReturn(dictionaryStream);

        // Instantiate the engine (it will call loadDictionary → read our TEST_DICTIONARY)
        wordGameEngine = new WordGameEngine(mockContext);
    }

    @Test
    void testGridGeneration() {
        char[] grid = wordGameEngine.generateGrid();

        // 1) Grid length must match GameConfig.TOTAL_LETTERS
        assertEquals(GameConfig.TOTAL_LETTERS, grid.length);

        // 2) Count how many vowels vs. consonants
        int vowelCount = 0;
        int consonantCount = 0;
        for (char c : grid) {
            if ("AEIOU".indexOf(c) >= 0) {
                vowelCount++;
            } else {
                consonantCount++;
            }
        }

        // 3) Verify at least 2 vowels
        assertTrue(vowelCount >= 2,
                "Grid should have at least 2 vowels, but was " + vowelCount);

        // 4) Verify vowelCount ≤ TOTAL_LETTERS/2
        assertTrue(vowelCount <= GameConfig.TOTAL_LETTERS / 2,
                "Grid should not have more than half vowels, but had " + vowelCount);

        // 5) All characters must be uppercase letters
        for (char c : grid) {
            assertTrue(Character.isUpperCase(c),
                    "All characters should be uppercase letters, but found '" + c + "'");
            assertTrue(Character.isLetter(c),
                    "All characters should be letters, but found '" + c + "'");
        }
    }

    @Test
    void testValidWordChecking() {
        // “WORD” and “QUIZ” are in our TEST_DICTIONARY
        assertTrue(wordGameEngine.isValidWord("WORD"));
        assertTrue(wordGameEngine.isValidWord("QUIZ"));
        // Lowercase should also pass
        assertTrue(wordGameEngine.isValidWord("word"));

        // “CAT” is length 3, below MIN_WORD_LENGTH if MIN_WORD_LENGTH ≥ 4
        assertFalse(wordGameEngine.isValidWord("CAT"));
        // A word not in dictionary
        assertFalse(wordGameEngine.isValidWord("NOTINDICT"));
        // Empty or null
        assertFalse(wordGameEngine.isValidWord(""));
        assertFalse(wordGameEngine.isValidWord(null));
    }

    @Test
    void testScoreCalculation() {
        // Assume GameConfig.BASE_SCORE is e.g. 10, LENGTH_BONUS and COMPLEXITY_BONUS are set accordingly.

        // “WORD” has length 4 → MIN_WORD_LENGTH=4? Then base score only.
        assertEquals(GameConfig.BASE_SCORE,
                wordGameEngine.calculateScore("WORD"));

        // “XYLOPHONE” has length 9. If MIN_WORD_LENGTH=4, length bonus = (9-4)*LENGTH_BONUS
        int expectedLengthBonus = ( "XYLOPHONE".length() - GameConfig.MIN_WORD_LENGTH )
                * GameConfig.LENGTH_BONUS;
        assertEquals(GameConfig.BASE_SCORE + expectedLengthBonus,
                wordGameEngine.calculateScore("XYLOPHONE"));

        // “QUIZ” contains Q and Z → each grants COMPLEXITY_BONUS
        int expectedComplexity = GameConfig.BASE_SCORE
                + 2 * GameConfig.COMPLEXITY_BONUS; // Q + Z
        assertEquals(expectedComplexity,
                wordGameEngine.calculateScore("QUIZ"));

        // Invalid or too-short => 0
        assertEquals(0, wordGameEngine.calculateScore("CAT"));
        assertEquals(0, wordGameEngine.calculateScore("NOTINDICT"));
        assertEquals(0, wordGameEngine.calculateScore(""));
        assertEquals(0, wordGameEngine.calculateScore(null));
    }

    @Test
    void testGetLettersReturnsClone() {
        char[] grid1 = wordGameEngine.getLetters();
        char[] grid2 = wordGameEngine.getLetters();

        // They must not reference the same array
        assertNotSame(grid1, grid2);

        // But contents should be identical
        assertArrayEquals(grid1, grid2);

        // Length check
        assertEquals(GameConfig.TOTAL_LETTERS, grid1.length);
    }

    @Test
    void testMultipleGridGenerationsAreDifferent() {
        char[] grid1 = wordGameEngine.generateGrid();
        char[] grid2 = wordGameEngine.generateGrid();

        // Extremely unlikely to be identical, but test for inequality
        assertFalse(java.util.Arrays.equals(grid1, grid2),
                "Two independently generated grids should differ");
        // Both must have correct length
        assertEquals(GameConfig.TOTAL_LETTERS, grid1.length);
        assertEquals(GameConfig.TOTAL_LETTERS, grid2.length);
    }
}
