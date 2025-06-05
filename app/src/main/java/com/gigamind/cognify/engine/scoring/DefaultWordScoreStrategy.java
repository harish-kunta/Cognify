package com.gigamind.cognify.engine.scoring;

import com.gigamind.cognify.util.GameConfig;

/**
 * Default scoring rules for the word game.
 */
public class DefaultWordScoreStrategy implements ScoreStrategy {
    @Override
    public int calculateScore(String word) {
        if (word == null || word.length() < GameConfig.MIN_WORD_LENGTH) {
            return 0;
        }

        int score = GameConfig.BASE_SCORE;

        // Length bonus
        score += (word.length() - GameConfig.MIN_WORD_LENGTH) * GameConfig.LENGTH_BONUS;

        // Complexity bonus for less common letters
        for (char c : word.toCharArray()) {
            if ("JQXZ".indexOf(c) >= 0) {
                score += GameConfig.COMPLEXITY_BONUS;
            } else if ("KWVY".indexOf(c) >= 0) {
                score += GameConfig.COMPLEXITY_BONUS / 2;
            }
        }

        return score;
    }
}
