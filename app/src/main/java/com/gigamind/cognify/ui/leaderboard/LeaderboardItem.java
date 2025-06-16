package com.gigamind.cognify.ui.leaderboard;

import com.google.firebase.firestore.IgnoreExtraProperties;

// This class corresponds to each row in the leaderboard.
// It will be filled by Firestore’s .toObject(LeaderboardItem.class).
@IgnoreExtraProperties
public class LeaderboardItem {
    private String userId;
    private String name;
    private int totalXP;
    private String countryCode;

    // Rank is not stored in Firestore; we’ll assign it client‐side.
    private int rank;

    public LeaderboardItem() {
        // Required empty constructor for Firestore’s .toObject()
    }

    public LeaderboardItem(
            String userId,
            String name,
            int totalXP,
            String countryCode
    ) {
        this.userId = userId;
        this.name = name;
        this.totalXP = totalXP;
        this.countryCode = countryCode;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }
    public void setDisplayName(String name) {
        this.name = name;
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
     * Returns the badge tier name for this item's total XP.
     * The mapping of XP ranges to badge names lives in {@link com.gigamind.cognify.util.BadgeUtils}.
     */
    public String getBadgeType() {
        return com.gigamind.cognify.util.BadgeUtils.badgeNameForXp(totalXP);
    }
}
