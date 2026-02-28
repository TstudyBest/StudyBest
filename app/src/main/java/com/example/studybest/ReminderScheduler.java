package com.example.studybest;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class ReminderScheduler {

    public static void schedule(Context context, long triggerAtMillis, String title, String text) {
        if (context == null) return;

        // don't schedule if the time has already passed
        if (triggerAtMillis <= System.currentTimeMillis()) return;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent i = new Intent(context, ReminderReceiver.class);
        i.putExtra("title", title != null ? title : "StudyBest");
        i.putExtra("text", text != null ? text : "You have a task!");

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                (int) System.currentTimeMillis(),
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
    }
}
