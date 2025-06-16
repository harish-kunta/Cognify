package com.gigamind.cognify.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

/** Helper methods for notification related operations. */
public final class NotificationUtils {
    private NotificationUtils() { /* no instances */ }

    /**
     * Creates the given notification channel if running on Android O or higher.
     */
    public static void createNotificationChannel(
            Context context,
            String channelId,
            CharSequence name,
            String description,
            int importance
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }
}
