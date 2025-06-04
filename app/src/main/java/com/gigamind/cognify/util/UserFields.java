package com.gigamind.cognify.util;

/**
 * All Firestore field names (and matching SharedPreferences keys) for user data.
 */
public final class UserFields {
    private UserFields() { /* no‐op */ }

    // Basic profile
    public static final String FIELD_UID               = "uid";
    public static final String FIELD_NAME              = "name";
    public static final String FIELD_EMAIL             = "email";

    // Game progress & streak
    public static final String FIELD_SCORE             = "score";
    public static final String FIELD_CURRENT_STREAK    = "currentStreak";
    public static final String FIELD_LAST_PLAYED_DATE  = "lastPlayedDate";
    public static final String FIELD_TOTAL_XP          = "totalXP";
    public static final String FIELD_LAST_PLAYED_TS    = "lastPlayedTimestamp";
    public static final String FIELD_LEADERBOARD_RANK  = "leaderboardRank";
    public static final String FIELD_TROPHIES          = "trophies";

    /**
     * Returns the Firestore field name for "last<gameType>Score".
     * Example: if gameType="WordDash", returns "lastWordDashScore".
     */
    public static String lastGameScoreField(String gameType) {
        return "last" + gameType + "Score";
    }
}

