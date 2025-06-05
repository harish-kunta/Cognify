package com.gigamind.cognify.engine.scoring;

/**
 * Strategy interface for word score calculation.
 */
public interface ScoreStrategy {
    /**
     * Calculates the score for a given word. Implementations decide the rules.
     *
     * @param word the word to score
     * @return the calculated score
     */
    int calculateScore(String word);
}
