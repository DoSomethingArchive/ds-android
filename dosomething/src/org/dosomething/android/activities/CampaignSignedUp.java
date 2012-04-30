package org.dosomething.android.activities;

import org.dosomething.android.R;

import roboguice.activity.RoboActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CampaignSignedUp extends RoboActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_signed_up);
    }

	public static Intent getIntent(Context context) {
		Intent answer = new Intent(context, CampaignSignedUp.class);
		return answer;
	}
	
}
