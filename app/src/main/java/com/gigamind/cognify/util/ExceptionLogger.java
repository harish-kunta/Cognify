package com.gigamind.cognify.util;

import android.util.Log;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

/**
 * Utility class for centralized exception logging throughout the app.
 * <p>
 * All exceptions are written to logcat and forwarded to Firebase Crashlytics
 * so issues can be monitored remotely.
 */
public final class ExceptionLogger {
    private static final String DEFAULT_TAG = "ExceptionLogger";

    private ExceptionLogger() {
        // no instances
    }

    public static void log(String tag, Throwable t) {
        if (t == null) return;
        String logTag = tag != null ? tag : DEFAULT_TAG;
        Log.e(logTag, t.getMessage(), t);

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.log(logTag + ": " + t.getMessage());
        crashlytics.recordException(t);
    }

    public static void log(Throwable t) {
        log(DEFAULT_TAG, t);
    }
}
