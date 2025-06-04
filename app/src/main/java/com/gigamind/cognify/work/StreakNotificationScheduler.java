// StreakNotificationScheduler.java
package com.gigamind.cognify.work;

import android.content.Context;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.gigamind.cognify.data.repository.UserRepository;

import java.util.concurrent.TimeUnit;

public class StreakNotificationScheduler {
    private static final String UNIQUE_WORK_NAME = "streak_notification_work";

    /**
     * Should be called whenever lastPlayedTimestamp is updated in SharedPrefs (or at app launch).
     * It reads the timestamp, computes "delay = (last + 24h) - now", and enqueues a OneTimeWorkRequest.
     *
     * @param firebaseUid   the signed‐in user’s UID, or null if not signed in.
     *                      We’ll include it in Worker’s input so the Worker can re-fetch if needed.
     * @param context       application context
     */
    public static void scheduleFromSharedPrefs(String firebaseUid, Context context) {
        UserRepository repo = new UserRepository(context);
        long lastMillis = repo.getLastPlayedTimestamp();
        if (lastMillis <= 0) {
            // No timestamp yet → nothing to schedule.
            return;
        }

        long now = System.currentTimeMillis();
        long twentyFourH = TimeUnit.HOURS.toMillis(1); // 1 hour for testing, but 24h in prod
        long runAt = lastMillis + twentyFourH;
        long delay = runAt - now;
        if (delay < 0) {
            // If already past 24 h, schedule immediately (or skip?)
            delay = 0;
        }

        // Package the firebaseUid into Worker’s input so it can re-sync if offline
        Data inputData = new Data.Builder()
                .putString("firebaseUid", firebaseUid)
                .build();

        WorkRequest work = new OneTimeWorkRequest.Builder(StreakNotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(UNIQUE_WORK_NAME)
                .build();

        // Cancel any previously queued work with the same name, then enqueue fresh
        WorkManager.getInstance(context)
                .enqueueUniqueWork(
                        UNIQUE_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        (OneTimeWorkRequest) work
                );
    }
}
