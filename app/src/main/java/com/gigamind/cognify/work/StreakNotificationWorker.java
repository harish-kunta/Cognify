package com.gigamind.cognify.work;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gigamind.cognify.R;
import com.gigamind.cognify.data.repository.UserRepository;
import com.gigamind.cognify.ui.MainActivity;
import com.gigamind.cognify.data.firebase.FirebaseService;
import com.gigamind.cognify.util.NotificationUtils;
import com.gigamind.cognify.util.DateUtils;

import java.util.Date;

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
        String lastDateStr  = DateUtils.format(lastMillis);
        String todayDateStr = DateUtils.today();

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
                            FirebaseService.getInstance().getCurrentUserId()
                            : null),
                    ctx
            );
            Log.d(TAG, "Re-scheduled next notification for 24h after " + new Date(lastMillis));
        }

        return Result.success();
    }

    private void sendStreakNotification(Context context) {
        NotificationUtils.createNotificationChannel(
                context,
                CHANNEL_ID,
                context.getString(R.string.streak_channel_name),
                context.getString(R.string.streak_channel_desc),
                android.app.NotificationManager.IMPORTANCE_HIGH
        );

        NotificationUtils.sendNotification(
                context,
                CHANNEL_ID,
                R.drawable.ic_streak,
                context.getString(R.string.notif_streak_title),
                context.getString(R.string.notif_streak_text),
                MainActivity.class,
                NOTIF_ID
        );
    }

}
