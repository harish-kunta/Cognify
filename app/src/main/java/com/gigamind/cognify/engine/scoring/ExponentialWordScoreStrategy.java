package com.gigamind.cognify.engine.scoring;

import com.gigamind.cognify.util.GameConfig;

/**
 * Scoring strategy that rewards longer words using a tempered
 * quadratic function. The growth is still based on the square of
 * the length factor but scaled down so scores remain reasonable
 * while still incentivising bigger words.
 */
public class ExponentialWordScoreStrategy implements ScoreStrategy {
    @Override
    public int calculateScore(String word) {
        if (word == null || word.length() < GameConfig.MIN_WORD_LENGTH) {
            return 0;
        }

        int lengthFactor = word.length() - GameConfig.MIN_WORD_LENGTH + 1;
        // Scale the quadratic growth so larger words are rewarded
        // without skyrocketing the score.
        int score = (GameConfig.BASE_SCORE / 2) * lengthFactor * lengthFactor
                + (GameConfig.BASE_SCORE / 2);

        for (char c : word.toCharArray()) {
            if ("JQXZ".indexOf(c) >= 0) {
                score += GameConfig.COMPLEXITY_BONUS * lengthFactor;
            } else if ("KWVY".indexOf(c) >= 0) {
                score += (GameConfig.COMPLEXITY_BONUS / 2) * lengthFactor;
            }
        }
        return score;
    }
}
