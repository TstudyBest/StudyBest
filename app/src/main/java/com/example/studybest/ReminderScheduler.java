package com.example.studybest;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ReminderScheduler {

    public static void schedule(
            Context context,
            long triggerAtMillis,
            String title,
            String text
    ) {

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, ReminderReceiver.class);
        i.putExtra("title", title);
        i.putExtra("text", text);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                (int) System.currentTimeMillis(),
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (am != null) {
            am.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pi
            );
        }
    }
}