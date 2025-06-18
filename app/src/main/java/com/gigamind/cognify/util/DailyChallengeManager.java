package com.gigamind.cognify.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;

/**
 * Utility for determining today's daily challenge type and perk.
 * Stores values in SharedPreferences so they remain constant for the day.
 */
public final class DailyChallengeManager {
    private static final String PREF_CHALLENGE_TYPE_PREFIX = "challenge_type_";
    private static final String PREF_CHALLENGE_PERK_PREFIX = "challenge_perk_";

    private static final String[] PERK_NAMES = new String[] {
            "25 XP",
            "50 XP",
            "75 XP"
    };
    private static final int[] PERK_VALUES = new int[] {25, 50, 75};

    private DailyChallengeManager() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String todayKey() {
        return new SimpleDateFormat("yyyy-DDD", Locale.US)
                .format(Calendar.getInstance().getTime());
    }

    /** Returns today's challenge game type, generating it if needed. */
    public static String getTodayType(Context context) {
        SharedPreferences p = prefs(context);
        String key = PREF_CHALLENGE_TYPE_PREFIX + todayKey();
        String stored = p.getString(key, null);
        if (stored != null) return stored;

        Random r = new Random(Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
        stored = r.nextBoolean() ? Constants.GAME_TYPE_WORD : Constants.GAME_TYPE_MATH;
        p.edit().putString(key, stored).apply();
        return stored;
    }

    /** Returns today's perk label, generating it if needed. */
    public static String getTodayPerk(Context context) {
        SharedPreferences p = prefs(context);
        String key = PREF_CHALLENGE_PERK_PREFIX + todayKey();
        String stored = p.getString(key, null);
        if (stored != null) return stored;

        Random r = new Random(Calendar.getInstance().get(Calendar.DAY_OF_YEAR) * 31);
        int index = r.nextInt(PERK_NAMES.length);
        stored = PERK_NAMES[index];
        p.edit().putString(key, stored).apply();
        return stored;
    }

    /** XP bonus associated with today's perk. */
    public static int getTodayPerkXp(Context context) {
        String perk = getTodayPerk(context);
        for (int i = 0; i < PERK_NAMES.length; i++) {
            if (PERK_NAMES[i].equals(perk)) {
                return PERK_VALUES[i];
            }
        }
        return 0;
    }

    public static boolean isCompleted(Context context) {
        return prefs(context).getBoolean(
                Constants.PREF_DAILY_COMPLETED_PREFIX + todayKey(), false);
    }

    public static void markCompleted(Context context) {
        prefs(context).edit().putBoolean(
                Constants.PREF_DAILY_COMPLETED_PREFIX + todayKey(), true).apply();
    }
}
