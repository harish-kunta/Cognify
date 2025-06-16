package com.gigamind.cognify.ui.leaderboard;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderboardItemTest {
    @ParameterizedTest
    @CsvSource({
            "0,Rookie",
            "10000,Learner",
            "20000,Thinker",
            "30000,Solver",
            "40000,Challenger",
            "50000,Strategist",
            "60000,Brainiac",
            "70000,Genius",
            "80000,Mastermind",
            "90000,Legend"
    })
    void badgeTypeMatchesXpThreshold(int xp, String expected) {
        LeaderboardItem item = new LeaderboardItem("id", "name", xp, "US");
        assertEquals(expected, item.getBadgeType());
    }
}
