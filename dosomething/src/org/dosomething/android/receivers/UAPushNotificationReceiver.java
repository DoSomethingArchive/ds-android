package org.dosomething.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

import org.dosomething.android.DSConstants;
import org.dosomething.android.activities.SplashScreenActivity;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Receiver to handle Urban Airship push notification events.
 */
public class UAPushNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "UA_PUSH_RECEIVER";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(PushManager.ACTION_NOTIFICATION_OPENED)) {
            Log.v(TAG, "ACTION_NOTIFICATION_OPENED");

            if (!DSConstants.inProduction) {
                debugPushExtras(intent);
            }

            // Launches the app to the splash screen
            Intent launch = new Intent(Intent.ACTION_MAIN);
            launch.setClass(UAirship.shared().getApplicationContext(), SplashScreenActivity.class);
            launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // TODO: Open to specific screens based on key/value pairs received from the push notification

            UAirship.shared().getApplicationContext().startActivity(launch);
        }
        else if (action.equals(PushManager.ACTION_PUSH_RECEIVED)) {}
        else if (action.equals(PushManager.ACTION_REGISTRATION_FINISHED)) {}
    }

    private void debugPushExtras(Intent intent) {
        Log.i(TAG, "User clicked notification. Message: " + intent.getStringExtra(PushManager.EXTRA_ALERT));

        Set<String> keys = intent.getExtras().keySet();
        for (String key : keys) {

            // Create list of standard GCM and UA keys that can be ignored
            List<String> ignoredKeys = (List<String>) Arrays.asList(
                    "collapse_key", // GCM collapse key
                    "from", // GCM sender
                    PushManager.EXTRA_NOTIFICATION_ID, // int id of generated notification (ACTION_PUSH_RECEIVED only)
                    PushManager.EXTRA_PUSH_ID, // Internal UA push id
                    PushManager.EXTRA_ALERT); // String sent in the alert field by the push notification

            if (ignoredKeys.contains(key)) {
                continue;
            }

            // What remains are the key value pairs packaged into the intent by the push notification
            Log.i(TAG, "Push Notification Extra: ["+key+" : " + intent.getStringExtra(key) + "]");
        }
    }
}
