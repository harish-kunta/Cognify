package com.gigamind.cognify.util;

import com.gigamind.cognify.model.Quest;
import java.util.Calendar;

/** Provides rotating daily and weekly quests. */
public final class QuestManager {
    private static final Quest[] DAILY_QUESTS = new Quest[] {
            new Quest("Find 10 four-letter words", "50 XP"),
            new Quest("Score 200 points in Math", "50 XP"),
            new Quest("Play Word Dash for 2 minutes", "Badge: Speedster")
    };

    private static final Quest[] WEEKLY_QUESTS = new Quest[] {
            new Quest("Complete 5 Daily Challenges", "Double XP Coupon"),
            new Quest("Reach 1000 XP this week", "Badge: Weekly Warrior")
    };

    private QuestManager() { /* no instances */ }

    /** Returns today's rotating daily quest. */
    public static Quest getDailyQuest() {
        int index = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % DAILY_QUESTS.length;
        return DAILY_QUESTS[index];
    }

    /** Returns this week's rotating quest. */
    public static Quest getWeeklyQuest() {
        int index = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR) % WEEKLY_QUESTS.length;
        return WEEKLY_QUESTS[index];
    }
}
