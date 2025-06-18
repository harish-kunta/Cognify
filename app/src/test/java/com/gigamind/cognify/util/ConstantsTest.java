package com.gigamind.cognify.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConstantsTest {

    @Test
    void testIntentConstants() {
        assertEquals("score", Constants.INTENT_SCORE);
        assertEquals("time", Constants.INTENT_TIME);
    }

    @Test
    void testPreferences() {
        assertNotNull(Constants.PREFS_NAME);
        assertNotNull(Constants.PREF_APP);
        assertEquals("tutorial_completed", Constants.PREF_ONBOARDING_COMPLETED);
    }
}
