package com.gigamind.cognify.model;

/** Simple data class representing a quest description and reward. */
public class Quest {
    public final String description;
    public final String reward;

    public Quest(String description, String reward) {
        this.description = description;
        this.reward = reward;
    }
}
