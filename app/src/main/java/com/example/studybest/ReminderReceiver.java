package com.example.studybest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");

        // fallback values if extras are missing
        if (title == null || title.isEmpty()) title = "StudyBest Reminder";
        if (text == null || text.isEmpty()) text = "You have a task due!";

        Intent openApp = new Intent(context, HomeActivity.class);
        PendingIntent pi = PendingIntent.getActivity(
                context,
                0,
                openApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, "studybest_reminders")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pi);

        try {
            NotificationManagerCompat nm = NotificationManagerCompat.from(context);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || nm.areNotificationsEnabled()) {
                nm.notify((int) System.currentTimeMillis(), builder.build());
            }
        } catch (SecurityException e) {
            // notification permission was not granted, nothing to do
        }
    }
}
