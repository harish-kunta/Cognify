package com.gigamind.cognify.analytics;

/**
 * Buckets representing how quickly a player responded.
 */
public enum ResponseTimeBucket {
    FAST("fast"),
    MEDIUM("medium"),
    SLOW("slow");

    private final String label;

    ResponseTimeBucket(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /**
     * Returns the corresponding bucket for the given elapsed time.
     */
    public static ResponseTimeBucket fromTime(long millis) {
        if (millis < 3000) {
            return FAST;
        }
        if (millis < 6000) {
            return MEDIUM;
        }
        return SLOW;
    }
}
