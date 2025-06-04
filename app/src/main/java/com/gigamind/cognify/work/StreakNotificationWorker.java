// StreakNotificationWorker.java
package com.gigamind.cognify.work;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gigamind.cognify.R;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.ui.MainActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * When executed, this Worker:
 * 1) Optionally re-syncs from Firestore if we have a firebaseUid in input.
 * 2) Checks “lastPlayedTimestamp” from SharedPrefs vs. today’s date.
 * 3) If user has not played within the last 24 hours (currentDay != lastDay), show notification.
 * 4) If still in an active streak, reschedule itself for “lastPlayed + 24 hours” again.
 */
public class StreakNotificationWorker extends Worker {
    private static final String TAG              = "StreakNotificationWorker";
    private static final String CHANNEL_ID       = "streak_reminder_channel";
    private static final int    NOTIF_ID         = 3001;

    public StreakNotificationWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params
    ) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        String firebaseUid = getInputData().getString("firebaseUid");

        UserRepository repo = new UserRepository(ctx);

        // 1) If we have a firebaseUid and user is signed in, re‐sync Firestore→SharedPrefs
        if (firebaseUid != null && !firebaseUid.isEmpty()) {
            try {
                // This blocks until fetch completes, but in background thread.
                // If you prefer asynchronous, you can chain continueWith;
                // for simplicity we do a synchronous get() here:
                DocumentSnapshot snap = repo.syncUserData().getResult();
                // After this call, SharedPrefs holds the freshest "lastPlayedTimestamp"
            } catch (Exception e) {
                Log.w(TAG, "Failed Firestore sync, will proceed with local prefs", e);
            }
        }

        // 2) Check “did the user play today?” by comparing dates
        long lastMillis = repo.getLastPlayedTimestamp();
        if (lastMillis <= 0) {
            // No record = no active streak
            Log.d(TAG, "No lastPlayedTimestamp, skipping notification.");
            return Result.success();
        }

        Calendar lastCal = Calendar.getInstance();
        lastCal.setTimeInMillis(lastMillis);

        Calendar todayCal = Calendar.getInstance();
        String lastDayString  = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(lastCal.getTime());
        String todayString    = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
                .format(todayCal.getTime());

        if (todayString.equals(lastDayString)) {
            // Already played “today” → no notification
            Log.d(TAG, "User already played today (" + todayString + "), skipping notification.");
        } else {
            // User has not played since last day → send a “streak in danger” notification
            sendStreakNotification(ctx);
        }

        // 3) If streak is still > 0, reschedule for “lastMillis + 24 hours”
        int currentStreak = repo.getCurrentStreak();
        if (currentStreak > 0) {
            // This will queue a new Worker to fire at (lastMillis + 24h)
            StreakNotificationScheduler.scheduleFromSharedPrefs(firebaseUid, ctx);
        }

        return Result.success();
    }

    private void sendStreakNotification(Context context) {
        createNotificationChannelIfNeeded(context);

        Intent toMain = new Intent(context, MainActivity.class);
        toMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pi = PendingIntent.getActivity(
                context,
                0,
                toMain,
                PendingIntent.FLAG_UPDATE_CURRENT
                        | (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        ? PendingIntent.FLAG_IMMUTABLE
                        : 0)
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_streak)
                .setContentTitle("🔥 Your streak is in danger!")
                .setContentText("You haven’t played in 24 hours. Tap here to keep your streak alive.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi);

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIF_ID, builder.build());
        }
    }

    private void createNotificationChannelIfNeeded(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name        = "Streak Reminder";
            String description       = "Alerts you if you haven’t played in 24 hours";
            int importance           = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager nm = (NotificationManager)
                    ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }
}

