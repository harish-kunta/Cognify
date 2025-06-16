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
    public static final String FIELD_PERSONAL_BEST_XP = "personalBestXP";

    /**
     * Returns the Firestore field name for "last<gameType>Score".
     * Example: if gameType="WordDash", returns "lastWordDashScore".
     */
    public static String lastGameScoreField(String gameType) {
        return "last" + camelCase(gameType) + "Score";
    }

    /**
     * Returns the Firestore field name for "total<gameType>Xp".
     * Example: if gameType="WordDash", returns "totalWordDashXp".
     */
    public static String totalGameXpField(String gameType) {
        return "total" + camelCase(gameType) + "Xp";
    }

    private static String camelCase(String str) {
        String[] parts = str.split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)))
              .append(p.substring(1));
        }
        return sb.toString();
    }
}

