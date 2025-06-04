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
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Now that SharedPreferences is already kept up-to-date by CognifyApplication’s real-time listener,
 * this Worker does NOT attempt any synchronous Firestore fetch() calls.
 * It simply reads from prefs to decide whether to fire a notification today, and then re-schedules itself.
 */
public class StreakNotificationWorker extends Worker {
    private static final String TAG        = "StreakNotificationWorker";
    private static final String CHANNEL_ID = "streak_reminder_channel";
    private static final int    NOTIF_ID   = 3001;

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
        UserRepository repo = new UserRepository(ctx);

        // (1) Read “last played” timestamp straight from SharedPreferences
        long lastMillis = repo.getLastPlayedTimestamp();
        if (lastMillis <= 0) {
            Log.d(TAG, "No lastPlayedTimestamp → no active streak, skipping notification.");
            return Result.success();
        }

        // (2) Compare the date portion only (yyyy-MM-dd)
        Calendar lastCal  = Calendar.getInstance();
        lastCal.setTimeInMillis(lastMillis);
        Calendar todayCal = Calendar.getInstance();

        String lastDateStr  = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(lastCal.getTime());
        String todayDateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(todayCal.getTime());

        if (todayDateStr.equals(lastDateStr)) {
            // User already played today → no notification needed
            Log.d(TAG, "User already played today (" + todayDateStr + "), skipping notification.");
        } else {
            // User hasn’t played in 24+: fire the “your streak is in danger” notification
            sendStreakNotification(ctx);
        }

        // (3) If the current streak is still > 0, re-schedule the Worker for “lastMillis + 24h”
        int currentStreak = repo.getCurrentStreak();
        if (currentStreak > 0) {
            StreakNotificationScheduler.scheduleFromSharedPrefs(
                    /*firebaseUid=*/ (repo.getLastPlayedTimestamp() > 0 ?
                            (FirebaseAuth.getInstance().getCurrentUser() != null
                                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                                    : null)
                            : null),
                    ctx
            );
            Log.d(TAG, "Re-scheduled next notification for 24h after " + new Date(lastMillis));
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
                .setContentText("You haven’t played for 24 hours. Tap here to keep it alive.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pi);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIF_ID, builder.build());
        }
    }

    private void createNotificationChannelIfNeeded(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name        = "Streak Reminder";
            String description       = "Notifies if you haven’t played in 24 hours";
            int importance           = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager nm =
                    (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }
}
