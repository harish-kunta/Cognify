package com.gigamind.cognify.work;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
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
                .setContentTitle(context.getString(R.string.notif_quest_title))
                .setContentText(context.getString(R.string.notif_quest_text))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIF_ID, builder.build());
        }
    }

}
