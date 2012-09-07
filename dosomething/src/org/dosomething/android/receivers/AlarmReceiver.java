package org.dosomething.android.receivers;

import org.dosomething.android.R;
import org.dosomething.android.activities.Welcome;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * AlarmReceiver
 * 
 * Class to receive and handle triggers set by AlarmManager.
 */
public class AlarmReceiver extends BroadcastReceiver {
	
	private static final String CAMPAIGN_ID = "campaign-id";
	private static final String CAMPAIGN_NAME = "campaign-name";
	private static final String NOTIF_ALARM_CAMPAIGN = "AlarmReminder.campaign";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		if (context != null) {
			Context ctx = context.getApplicationContext();
			
			NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
			if (nm != null) {
				// Get data sent from the AlarmManager
				Bundle bundle = intent.getExtras();
				String campaignId = bundle.getString(CAMPAIGN_ID);
				String campaignName = bundle.getString(CAMPAIGN_NAME);
				
				Intent launchIntent = new Intent(ctx, Welcome.class);
				launchIntent.putExtra(NOTIF_ALARM_CAMPAIGN, campaignId);
				
				PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, launchIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
				
				// Display message reminding users to report back for campaign they signed up for
				int icon = R.drawable.ic_launcher;
				String notificationTitle = ctx.getString(R.string.reminder_title);
				String notificationBody = campaignName + " " + ctx.getString(R.string.reminder_body_reportback);
				
				// Note with API 11 and up, should use Notification.Builder
				Notification noti = new Notification(icon, notificationTitle, System.currentTimeMillis());
				noti.flags |= Notification.FLAG_AUTO_CANCEL;
				noti.setLatestEventInfo(context, notificationTitle, notificationBody, pendingIntent);
				nm.notify(1, noti);
			}
		}
	}

}
