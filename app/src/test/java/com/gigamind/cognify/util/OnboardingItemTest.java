package com.gigamind.cognify.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OnboardingItemTest {
    @Test
    void fieldsAreStoredCorrectly() {
        OnboardingItem item = new OnboardingItem(1, "Title", "Desc");
        assertEquals(1, item.imageResId);
        assertEquals("Title", item.title);
        assertEquals("Desc", item.description);
    }
}
