package org.dosomething.android.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.google.inject.Inject;

import org.dosomething.android.R;
import org.dosomething.android.cache.DSPreferences;
import org.dosomething.android.context.UserContext;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Shows splash screen when app is launched and decides which screen to show next.
 */
public class SplashScreenActivity extends RoboActivity {

    // Duration of the splash screen in milliseconds
    private final int SPLASH_DURATION = 3000;

    // Alarm notification key
    private static final String NOTIF_ALARM_CAMPAIGN = "AlarmReminder.campaign";

    @Inject private UserContext userContext;

    @InjectView(R.id.versionName) private TextView mVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // Get the version number and set it to display in the TextView
        String version = "";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
            version = pInfo.versionName;
            if (version != null && version.length() > 0) {
                mVersion.setText("v" + version);
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Go to Welcome activity if there is no logged in user
                if (userContext != null && !userContext.isLoggedIn()) {
                    // Set boolean to indicate that this app has now been run
                    DSPreferences prefs = new DSPreferences(SplashScreenActivity.this);
                    prefs.setHasRun();

                    Intent i = new Intent(SplashScreenActivity.this, Welcome.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                // If launched from an alarm notification, open to the Campaign the alarm was for
                else if (getIntent() != null && getIntent().getStringExtra(NOTIF_ALARM_CAMPAIGN) != null) {
                    String intentCampaign = getIntent().getStringExtra(NOTIF_ALARM_CAMPAIGN);
                    if (intentCampaign != null) {
                        Intent i = org.dosomething.android.activities.Campaign.getIntent(SplashScreenActivity.this, intentCampaign);
                        startActivity(i);
                    }
                }
                // Otherwise, open to the main campaigns screen
                else {
                    Intent i = new Intent(SplashScreenActivity.this, Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }

                finish();
            }
        }, SPLASH_DURATION);
    }
}
