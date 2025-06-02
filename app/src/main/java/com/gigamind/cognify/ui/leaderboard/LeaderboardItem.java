package com.gigamind.cognify.ui.leaderboard;

import com.google.firebase.firestore.IgnoreExtraProperties;

// This class corresponds to each row in the leaderboard.
// It will be filled by Firestore’s .toObject(LeaderboardItem.class).
@IgnoreExtraProperties
public class LeaderboardItem {
    private String userId;
    private String displayName;
    private int totalXP;
    private String countryCode;

    // Rank is not stored in Firestore; we’ll assign it client‐side.
    private int rank;

    public LeaderboardItem() {
        // Required empty constructor for Firestore’s .toObject()
    }

    public LeaderboardItem(
            String userId,
            String displayName,
            int totalXP,
            String countryCode
    ) {
        this.userId = userId;
        this.displayName = displayName;
        this.totalXP = totalXP;
        this.countryCode = countryCode;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getTotalXP() {
        return totalXP;
    }
    public void setTotalXP(int totalXP) {
        this.totalXP = totalXP;
    }

    public String getCountryCode() {
        return countryCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getRank() {
        return rank;
    }
    public void setRank(int rank) {
        this.rank = rank;
    }

    /**
     * Based on totalXP, return a badge type string:
     *  - "bronze" if XP < 1000,
     *  - "silver" if 1000 ≤ XP < 2000,
     *  - "gold" if XP ≥ 2000.
     * You can adjust these thresholds as desired.
     */
    public String getBadgeType() {
        if (totalXP >= 2000) {
            return "gold";
        } else if (totalXP >= 1000) {
            return "silver";
        } else {
            return "bronze";
        }
    }
}
