package org.dosomething.android.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import org.dosomething.android.R;
import org.dosomething.android.cache.DSPreferences;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Shows splash screen when app is launched and decides which screen to show next.
 */
public class SplashScreenActivity extends RoboActivity {

    private final int SPLASH_DURATION = 3000; // in milliseconds

    // Alarm notification key
    private static final String NOTIF_ALARM_CAMPAIGN = "AlarmReminder.campaign";

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
                DSPreferences prefs = new DSPreferences(SplashScreenActivity.this);
                // If this is the initial app execution, go to the Welcome Activity
                if (prefs != null && !prefs.getHasRun()) {
                    // Set boolean to indicate that this app has now been run
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
                // Otherwise, open to the user's profile page
                else {
                    Intent i = new Intent(SplashScreenActivity.this, Profile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }

                finish();
            }
        }, SPLASH_DURATION);
    }
}
