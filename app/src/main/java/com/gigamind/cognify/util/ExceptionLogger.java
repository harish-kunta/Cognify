package com.gigamind.cognify.util;

import android.util.Log;

/**
 * Utility class for centralized exception logging throughout the app.
 */
public final class ExceptionLogger {
    private static final String DEFAULT_TAG = "ExceptionLogger";

    private ExceptionLogger() {
        // no instances
    }

    public static void log(String tag, Throwable t) {
        if (t == null) return;
        Log.e(tag != null ? tag : DEFAULT_TAG, t.getMessage(), t);
    }

    public static void log(Throwable t) {
        log(DEFAULT_TAG, t);
    }
}
