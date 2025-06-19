package com.gigamind.cognify.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.gigamind.cognify.util.GameType;

/**
 * Helper for managing the first-time tutorial state for each game type.
 */
public class TutorialHelper {
    private final SharedPreferences prefs;
    private final String key;

    public TutorialHelper(Context context, GameType type) {
        this.prefs = context.getSharedPreferences(Constants.PREF_APP, Context.MODE_PRIVATE);
        this.key = Constants.PREF_TUTORIAL_COMPLETED_PREFIX + type.id();
    }

    /**
     * Returns true if the user already completed the tutorial.
     */
    public boolean isTutorialCompleted() {
        return prefs.getBoolean(key, false);
    }

    /**
     * Marks the tutorial as completed.
     */
    public void markTutorialCompleted() {
        prefs.edit().putBoolean(key, true).apply();
    }
}
