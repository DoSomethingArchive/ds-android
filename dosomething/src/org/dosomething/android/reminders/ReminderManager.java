package org.dosomething.android.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.dosomething.android.receivers.AlarmReceiver;

/**
 * Utility for managing Campaign reminders.
 */
public class ReminderManager {

    public static PendingIntent createPendingIntent(Context context, String campaignId, String campaignName, String campaignStep) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.NOTIFICATION_TYPE, AlarmReceiver.NOTIF_CAMPAIGN_STEP_REMINDER);
        intent.putExtra(AlarmReceiver.EXTRA_CAMPAIGN_ID, campaignId);
        intent.putExtra(AlarmReceiver.EXTRA_CAMPAIGN_NAME, campaignName);
        intent.putExtra(AlarmReceiver.EXTRA_CAMPAIGN_STEP, campaignStep);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, AlarmReceiver.ALARM_ID_CAMPAIGN_STEP_REMINDER,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    public static void scheduleReminder(Context context, long alarmTime, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }
}
