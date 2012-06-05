package org.dosomething.android.activities;

import org.dosomething.android.analytics.Analytics;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class StubActivity extends Activity {
	
    private static final String TARGET_URI = "market://details?id=org.dosomething.android";

    @Override
    public final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Analytics.startSession(this);
        
        Uri target = Uri.parse(TARGET_URI);
        startActivity(new Intent(Intent.ACTION_VIEW, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
    
    public void onStop(){
 	   super.onStop();
 	   Analytics.endSession(this);
 	}
    
    public String getPageName() {
    	return "StubActivity";
    }
}
