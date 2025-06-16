package com.gigamind.cognify.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Utility methods for common date operations used throughout the app. */
public final class DateUtils {
    private static final SimpleDateFormat FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private DateUtils() { /* no instances */ }

    /** Returns today's date formatted as yyyy-MM-dd. */
    public static String today() {
        return FORMAT.format(Calendar.getInstance().getTime());
    }

    /** Returns yesterday's date formatted as yyyy-MM-dd. */
    public static String yesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        return FORMAT.format(cal.getTime());
    }

    /** Formats the given millis since epoch as yyyy-MM-dd. */
    public static String format(long millis) {
        return FORMAT.format(new Date(millis));
    }
}
