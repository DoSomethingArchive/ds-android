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

    /**
     * Create the PendingIntent used to send a reminder notification to complete a campaign step.
     *
     * @param context Context
     * @param campaignId unique campaign id
     * @param campaignName campaign name to display
     * @param campaignStep name of the campaign step
     * @return the PendingIntent to pass on to ReminderManager.scheduleReminder() to set the reminder
     */
    public static PendingIntent createCampaignStepReminder(Context context, String campaignId, String campaignName, String campaignStep) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.NOTIFICATION_TYPE, AlarmReceiver.NOTIF_CAMPAIGN_STEP_REMINDER);
        intent.putExtra(AlarmReceiver.EXTRA_CAMPAIGN_ID, campaignId);
        intent.putExtra(AlarmReceiver.EXTRA_CAMPAIGN_NAME, campaignName);
        intent.putExtra(AlarmReceiver.EXTRA_CAMPAIGN_STEP, campaignStep);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, AlarmReceiver.ALARM_ID_CAMPAIGN_STEP_REMINDER,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    /**
     * Create the PendingIntent used to send a reminder notification to report back on a campaign.
     *
     * @param context Context
     * @param campaignId unique campaign id
     * @param campaignName campang name to display
     * @return the PendingIntent to pass on to ReminderManager.scheduleReminder() to set the reminder
     */
    public static PendingIntent createReportBackReminder(Context context, String campaignId, String campaignName) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.EXTRA_CAMPAIGN_ID, campaignId);
        intent.putExtra(AlarmReceiver.EXTRA_CAMPAIGN_NAME, campaignName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, AlarmReceiver.ALARM_ID_REPORT_BACK,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    /**
     * Uses the system's alarm service to set an alarm to trigger in the future.
     *
     * @param context Context
     * @param alarmTime time in milliseconds the alarm should trigger
     * @param pendingIntent PendingIntent packaged with data about the reminder
     */
    public static void scheduleReminder(Context context, long alarmTime, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
    }

    /**
     * Cancels a reminder from the alarm service.
     *
     * @param context Context
     * @param pendingIntent the PendingIntent created by the ReminderManager to cancel
     */
    public static void clearReminder(Context context, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
