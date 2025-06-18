package com.gigamind.cognify.engine.scoring;

import com.gigamind.cognify.util.GameConfig;

/**
 * Scoring strategy that rewards longer words exponentially.
 * Words that exceed the minimum length earn disproportionately
 * higher scores, encouraging players to tackle larger words.
 */
public class ExponentialWordScoreStrategy implements ScoreStrategy {
    @Override
    public int calculateScore(String word) {
        if (word == null || word.length() < GameConfig.MIN_WORD_LENGTH) {
            return 0;
        }

        int lengthFactor = word.length() - GameConfig.MIN_WORD_LENGTH + 1;
        int score = GameConfig.BASE_SCORE * lengthFactor * lengthFactor;

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
