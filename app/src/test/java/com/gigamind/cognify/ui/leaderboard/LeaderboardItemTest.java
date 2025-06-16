package com.gigamind.cognify.ui.leaderboard;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderboardItemTest {
    @ParameterizedTest
    @CsvSource({
            "0,Rookie",
            "500,Apprentice",
            "1200,Adept",
            "1600,Expert",
            "2500,Veteran",
            "3500,Elite",
            "4500,Master",
            "5200,Champion",
            "7500,Hero",
            "9500,Legend"
    })
    void badgeTypeMatchesXpThreshold(int xp, String expected) {
        LeaderboardItem item = new LeaderboardItem("id", "name", xp, "US");
        assertEquals(expected, item.getBadgeType());
    }
}
