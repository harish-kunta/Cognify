package com.gigamind.cognify.analytics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResponseTimeBucketTest {

    @Test
    void testLabels() {
        assertEquals("fast", ResponseTimeBucket.FAST.label());
        assertEquals("medium", ResponseTimeBucket.MEDIUM.label());
        assertEquals("slow", ResponseTimeBucket.SLOW.label());
    }

    @Test
    void testFromTime() {
        assertEquals(ResponseTimeBucket.FAST, ResponseTimeBucket.fromTime(1000));
        assertEquals(ResponseTimeBucket.MEDIUM, ResponseTimeBucket.fromTime(4000));
        assertEquals(ResponseTimeBucket.SLOW, ResponseTimeBucket.fromTime(7000));
    }

    @Test
    void testFromTimeBoundaries() {
        assertEquals(ResponseTimeBucket.MEDIUM, ResponseTimeBucket.fromTime(3000));
        assertEquals(ResponseTimeBucket.SLOW, ResponseTimeBucket.fromTime(6000));
    }
}
