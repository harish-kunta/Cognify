package com.gigamind.cognify.engine.scoring;

/**
 * Strategy interface for calculating scores in the math game.
 *
 * <p>Extracting this allows alternative scoring rules to be provided
 * without modifying the {@code MathGameEngine}, following the
 * <em>Open/Closed</em> principle from Clean Code and Code Complete.</p>
 */
public interface MathScoreStrategy {

    /**
     * Calculates a score given whether the answer was correct, the response
     * time in milliseconds and the difficulty level of the question.
     *
     * @param correct        whether the player's answer was correct
     * @param responseTimeMs how long it took the player to answer
     * @param difficulty     the difficulty level of the current question
     * @return the score to award (or deduct)
     */
    int calculateScore(boolean correct, long responseTimeMs, int difficulty);
}

