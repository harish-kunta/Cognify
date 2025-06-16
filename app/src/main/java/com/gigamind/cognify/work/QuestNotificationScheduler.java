package com.gigamind.cognify.work;

import android.content.Context;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/** Schedules daily quest reminder notifications. */
public class QuestNotificationScheduler {
    private static final String UNIQUE_WORK_NAME = "quest_notification_work";

    public static void scheduleDaily(Context context) {
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 18); // 6 PM reminder
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long runAt = cal.getTimeInMillis();
        if (runAt <= now) runAt += TimeUnit.DAYS.toMillis(1);
        long delay = runAt - now;

        WorkRequest work = new OneTimeWorkRequest.Builder(QuestNotificationWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(UNIQUE_WORK_NAME)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                (OneTimeWorkRequest) work
        );
    }
}
