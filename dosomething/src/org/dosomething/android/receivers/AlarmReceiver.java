package org.dosomething.android.receivers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import org.dosomething.android.R;
import org.dosomething.android.activities.Welcome;
import org.dosomething.android.cache.DSPreferences;

/**
 * AlarmReceiver
 *
 * Class to receive and handle triggers set by AlarmManager.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_CAMPAIGN_ID = "campaign-id";
    public static final String EXTRA_CAMPAIGN_NAME = "campaign-name";
    public static final String EXTRA_CAMPAIGN_STEP = "campaign-step";

    public static final String NOTIFICATION_TYPE = "notification-type";
    public static final String NOTIF_ALARM_CAMPAIGN = "Alarm.campaign";
    public static final String NOTIF_CAMPAIGN_STEP_REMINDER = "Alarm.campaignStep";

    public static final int ALARM_ID_REPORT_BACK = 100;
    public static final int ALARM_ID_CAMPAIGN_STEP_REMINDER = 101;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (context != null) {
            NotificationManager notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifManager != null) {
                // Get data sent from the AlarmManager
                Bundle bundle = intent.getExtras();
                String campaignId = bundle.getString(EXTRA_CAMPAIGN_ID);
                String campaignName = bundle.getString(EXTRA_CAMPAIGN_NAME);

                String notificationType = bundle.getString(NOTIFICATION_TYPE);
                if (notificationType.equals(NOTIF_ALARM_CAMPAIGN)) {
                    Intent launchIntent = new Intent(context, Welcome.class);
                    launchIntent.putExtra(NOTIF_ALARM_CAMPAIGN, campaignId);

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

                    // Display message reminding users to report back for campaign they signed up for
                    String notifyTitle = context.getString(R.string.reminder_title);
                    String notifyBody = context.getString(R.string.reminder_body_reportback, campaignName);

                    // Build the notification
                    NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.actionbar_logo)
                            .setContentTitle(notifyTitle)
                            .setContentText(notifyBody)
                            .setContentIntent(pendingIntent);

                    // Send the notification to the system
                    notifManager.notify(ALARM_ID_REPORT_BACK, notifyBuilder.build());
                }
                else if (notificationType.equals(NOTIF_CAMPAIGN_STEP_REMINDER)) {
                    // Notification title
                    String notifTitle = context.getString(R.string.reminder_title);

                    // Notification body text includes campaign name and step the reminder is for
                    String campaignStep = bundle.getString(EXTRA_CAMPAIGN_STEP);
                    String notifBody = context.getString(R.string.reminder_campaign_step_body,
                            campaignStep, campaignName);

                    // Build the notification
                    NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.actionbar_logo)
                            .setContentTitle(notifTitle)
                            .setContentText(notifBody);
                    // TODO: create the pending intent
                    //.setContentIntent(pendingIntent);

                    // Send the notification to the system
                    notifManager.notify(ALARM_ID_CAMPAIGN_STEP_REMINDER, notifyBuilder.build());

                    // Remove the log of this reminder from DSPreferences
                    DSPreferences dsPrefs = new DSPreferences(context);
                    dsPrefs.clearStepReminder(campaignId, campaignStep);
                }
            }
        }
    }

}
