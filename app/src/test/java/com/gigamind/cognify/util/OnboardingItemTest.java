package com.gigamind.cognify.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OnboardingItemTest {
    @Test
    void fieldsAreStoredCorrectly() {
        OnboardingItem item = new OnboardingItem.Builder().imageResId(1).description("Desc").title("Title").build();
        assertEquals(1, item.getImageResId());
        assertEquals("Title", item.getTitle());
        assertEquals("Desc", item.getDescription());
    }
}
