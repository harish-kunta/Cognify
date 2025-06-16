package com.gigamind.cognify.util;

import androidx.annotation.NonNull;

/** Utility class for mapping XP to badge tiers. */
public final class BadgeUtils {
    private BadgeUtils() {}

    /**
     * XP thresholds for the different badge tiers. The values map directly
     * to the levels defined in {@code levels.txt} in the project root.
     * <p>
     * Each index corresponds to a badge tier defined in {@link #NAMES} and
     * {@link #ICONS}.
     */
    public static final int[] THRESHOLDS = {
            0, 10000, 20000, 30000, 40000,
            50000, 60000, 70000, 80000, 90000
    };

    /** Names of the badge tiers displayed to the user. */
    public static final String[] NAMES = {
            "Rookie", "Learner", "Thinker", "Solver", "Challenger",
            "Strategist", "Brainiac", "Genius", "Mastermind", "Legend"
    };

    /** Drawable resources for each badge tier. */
    private static final int[] ICONS = {
            com.gigamind.cognify.R.drawable.rookie,
            com.gigamind.cognify.R.drawable.learner,
            com.gigamind.cognify.R.drawable.thinker,
            com.gigamind.cognify.R.drawable.solver,
            com.gigamind.cognify.R.drawable.challenger,
            com.gigamind.cognify.R.drawable.strategist,
            com.gigamind.cognify.R.drawable.brainiac,
            com.gigamind.cognify.R.drawable.genius,
            com.gigamind.cognify.R.drawable.mastermind,
            com.gigamind.cognify.R.drawable.legend
    };

    /** Returns the badge index (0..9) for the given total XP. */
    public static int badgeIndexForXp(int xp) {
        int index = 0;
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (xp >= THRESHOLDS[i]) {
                index = i;
            } else {
                break;
            }
        }
        return index;
    }

    /** Returns the badge name for the given XP total. */
    @NonNull
    public static String badgeNameForXp(int xp) {
        return NAMES[badgeIndexForXp(xp)];
    }

    /**
     * Returns the drawable resource for the given badge index. If the index is
     * out of range, the last badge (Legend) is returned as a fallback.
     */
    public static int badgeIconResId(int index) {
        if (index < 0) {
            index = 0;
        } else if (index >= ICONS.length) {
            index = ICONS.length - 1;
        }
        return ICONS[index];
    }
}
