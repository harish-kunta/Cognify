package com.gigamind.cognify.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper for managing the first-time Word Dash tutorial state.
 */
public class TutorialHelper {
    private final SharedPreferences prefs;

    public TutorialHelper(Context context) {
        this.prefs = context.getSharedPreferences(Constants.PREF_APP, Context.MODE_PRIVATE);
    }

    /**
     * Returns true if the user already completed the tutorial.
     */
    public boolean isTutorialCompleted() {
        return prefs.getBoolean(Constants.PREF_TUTORIAL_COMPLETED, false);
    }

    /**
     * Marks the tutorial as completed.
     */
    public void markTutorialCompleted() {
        prefs.edit().putBoolean(Constants.PREF_TUTORIAL_COMPLETED, true).apply();
    }
}
