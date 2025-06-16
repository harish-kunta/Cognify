package com.gigamind.cognify.util;

import androidx.annotation.NonNull;

/** Utility class for mapping XP to badge tiers. */
public final class BadgeUtils {
    private BadgeUtils() {}

    public static final int[] THRESHOLDS = {
            0, 500, 1000, 1500, 2000, 3000, 4000, 5000, 7000, 9000
    };

    public static final String[] NAMES = {
            "Rookie", "Apprentice", "Adept", "Expert", "Veteran",
            "Elite", "Master", "Champion", "Hero", "Legend"
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

    /** Returns the drawable resource for the given badge index. */
    public static int badgeIconResId(int index) {
        return com.gigamind.cognify.R.drawable.ic_badge; // placeholder
    }
}
