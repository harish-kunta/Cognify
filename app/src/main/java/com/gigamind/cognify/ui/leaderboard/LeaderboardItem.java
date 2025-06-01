package com.gigamind.cognify.ui.leaderboard;

public class LeaderboardItem {
    private String userId;
    private String name;
    private int score;
    private int rank;

    // Empty constructor for Firebase
    public LeaderboardItem() {}

    public LeaderboardItem(String userId, String name, int score) {
        this.userId = userId;
        this.name = name;
        this.score = score;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
} 