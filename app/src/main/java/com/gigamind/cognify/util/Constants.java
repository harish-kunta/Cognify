package com.gigamind.cognify.util;

public class Constants {
    public static final int TOTAL_QUESTIONS = 10;
    public static final String INTENT_SCORE = "score";
    public static final String INTENT_TIME = "time";
    public static final String INTENT_TYPE = "type";
    public static final String TYPE_QUICK_MATH = "quick_math";
    public static final String GAME_TYPE_WORD_DASH = "word_dash";
    /** Array of all supported game types used throughout the app. */
    public static final String[] SUPPORTED_GAME_TYPES = {
            GAME_TYPE_WORD_DASH,
            TYPE_QUICK_MATH
    };
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
    // Marks that the onboarding tutorial has been completed
    public static final String PREF_ONBOARDING_COMPLETED = "tutorial_completed";
    // Prefix for per-game tutorial completion flags
    public static final String PREF_TUTORIAL_COMPLETED_PREFIX = "game_tutorial_completed_";
    public static final String PREF_SOUND_ENABLED = "sound_enabled";
    public static final String PREF_HAPTICS_ENABLED = "haptics_enabled";
    public static final String PREF_ANIMATIONS_ENABLED = "animations_enabled";
    public static final String PREF_DARK_MODE_ENABLED = "dark_mode_enabled";

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
    public static final String ANALYTICS_SCREEN_HOME = "home_screen";
    public static final String ANALYTICS_SCREEN_PROFILE = "profile_screen";
    public static final String ANALYTICS_SCREEN_LEADERBOARD = "leaderboard_screen";
    public static final String ANALYTICS_SCREEN_SETTINGS = "settings_screen";

    // Avatar customization keys
    public static final String AVATAR_SKIN = "avatar_skin";
    public static final String AVATAR_HAIR = "avatar_hair";
    public static final String AVATAR_EYES = "avatar_eyes";
    public static final String AVATAR_MOUTH = "avatar_mouth";
    public static final String AVATAR_BODY = "avatar_body";
    public static final String AVATAR_NOSE = "avatar_nose";
    public static final String AVATAR_EARS = "avatar_ears";
    public static final String AVATAR_FACIAL_HAIR = "avatar_facial_hair";
    public static final String AVATAR_ACCESSORY = "avatar_accessory";
    public static final String AVATAR_EYEBROWS = "avatar_eyebrows";
    public static final String AVATAR_GLASSES = "avatar_glasses";
    public static final String AVATAR_TATTOO = "avatar_tattoo";
    public static final String AVATAR_FACE_SHAPE = "avatar_face_shape";
    public static final String USER_PROFILE_PIC = "profile_picture";
}