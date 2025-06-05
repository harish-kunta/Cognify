package com.gigamind.cognify.ui.leaderboard;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

public class LeaderboardItemTest {
    @ParameterizedTest
    @CsvSource({"50,bronze", "0,bronze", "-10,bronze", "1000,silver", "1500,silver", "2000,gold", "2500,gold"})
    void badgeTypeMatchesXpThreshold(int xp, String expected) {
        LeaderboardItem item = new LeaderboardItem("id", "name", xp, "US");
        assertEquals(expected, item.getBadgeType());
    }
}
