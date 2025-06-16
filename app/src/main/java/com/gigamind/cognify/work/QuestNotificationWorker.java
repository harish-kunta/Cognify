package com.gigamind.cognify.work;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.gigamind.cognify.R;
import com.gigamind.cognify.ui.MainActivity;
import com.gigamind.cognify.util.NotificationUtils;

/** Worker that posts the daily quest reminder notification. */
public class QuestNotificationWorker extends Worker {
    private static final String CHANNEL_ID = "quest_reminder_channel";
    private static final int NOTIF_ID = 4001;

    public QuestNotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        sendQuestNotification(getApplicationContext());
        QuestNotificationScheduler.scheduleDaily(getApplicationContext());
        return Result.success();
    }

    private void sendQuestNotification(Context context) {
        NotificationUtils.createNotificationChannel(
                context,
                CHANNEL_ID,
                context.getString(R.string.quest_channel_name),
                context.getString(R.string.quest_channel_desc),
                android.app.NotificationManager.IMPORTANCE_HIGH
        );

        NotificationUtils.sendNotification(
                context,
                CHANNEL_ID,
                R.drawable.ic_streak,
                context.getString(R.string.notif_quest_title),
                context.getString(R.string.notif_quest_text),
                MainActivity.class,
                NOTIF_ID
        );
    }

}
