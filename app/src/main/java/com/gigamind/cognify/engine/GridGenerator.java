package com.gigamind.cognify.engine;

/**
 * Strategy interface for generating the letter grid in the word game.
 */
public interface GridGenerator {
    /**
     * Generates a new grid of letters.
     *
     * @return an array representing the generated letter grid
     */
    char[] generate();
}
