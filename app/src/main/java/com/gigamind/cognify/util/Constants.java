package com.gigamind.cognify.util;

public class Constants {
    public static final int TOTAL_QUESTIONS = 10;
    public static final String INTENT_SCORE = "score";
    public static final String INTENT_TIME = "time";
    public static final String INTENT_TYPE = "type";
    public static final String TYPE_QUICK_MATH = "quick_math";
    public static final String GAME_TYPE_WORD_DASH = "word_dash";
    public static final String INTENT_FOUND_WORDS = "found_words";

    // XP bonuses
    public static final int BONUS_NEW_PB       = 20; // extra XP if player beats PB
    public static final int BONUS_STREAK_PER_DAY = 10;

    // SharedPreferences names & keys
    public static final String PREFS_NAME = "GamePrefs";
    public static final String PREF_APP = "AppPrefs";
    public static final String PREF_IS_FIRST_LAUNCH = "isFirstLaunch";
    public static final String PREF_ASKED_NOTIFICATIONS = "asked_for_notifications";
    public static final String PREF_IS_GUEST = "is_guest_mode";
    public static final String PREF_NOTIFICATION = "notification_preferences";
    public static final String PREF_NOTIFICATION_ENABLED = "notifications_enabled";

    // Intent extras and other keys
    public static final String INTENT_GAME_TYPE = "GAME_TYPE";
    public static final String INTENT_IS_DAILY = "IS_DAILY_CHALLENGE";
    public static final String GAME_TYPE_WORD = "WORD";
    public static final String GAME_TYPE_MATH = "MATH";
    public static final String PREF_DAILY_COMPLETED_PREFIX = "daily_completed_";
    public static final String EXTRA_CHALLENGE_SCORE = "challenge_score";
    public static final String EXTRA_CHALLENGE_TYPE = "challenge_type";

    // Analytics screen names
    public static final String ANALYTICS_SCREEN_RESULT = "result_screen";
    public static final String ANALYTICS_SCREEN_WORD_DASH = "word_dash_game";
    public static final String ANALYTICS_SCREEN_QUICK_MATH = "quick_math_game";
}