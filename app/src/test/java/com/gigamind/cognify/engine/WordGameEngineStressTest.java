package com.gigamind.cognify.engine;

import com.gigamind.cognify.util.GameConfig;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stress tests that generate 100 dynamic tests for the WordGameEngine.
 * Each dynamic test generates a new grid and verifies its constraints.
 */
public class WordGameEngineStressTest {
    @TestFactory
    List<DynamicTest> generateMultipleGrids() {
        Set<String> dictionary = new HashSet<>();
        dictionary.add("CAT");
        WordGameEngine engine = new WordGameEngine(dictionary);

        List<DynamicTest> tests = new ArrayList<>();
        IntStream.range(0, 100).forEach(i ->
            tests.add(DynamicTest.dynamicTest("gridTest" + i, () -> {
                char[] grid = engine.generateGrid();
                assertEquals(GameConfig.TOTAL_LETTERS, grid.length);
                long vowels = IntStream.range(0, grid.length)
                        .mapToObj(j -> grid[j])
                        .filter(c -> "AEIOU".indexOf(c) >= 0)
                        .count();
                assertTrue(vowels >= 2, "Grid should contain at least two vowels");
                assertTrue(vowels <= grid.length / 2, "Grid should contain at most half vowels");
                for (char c : grid) {
                    assertTrue(Character.isUpperCase(c));
                }
            }))
        );
        return tests;
    }
}
