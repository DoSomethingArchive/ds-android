package org.dosomething.android.activities;

import org.dosomething.android.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.google.inject.Inject;

public class Profile extends RoboActivity {
	
	private static final int REQ_CAMPAIGNS = 100;

	@Inject private LayoutInflater inflater;
	
	@InjectView(R.id.content) private LinearLayout content;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        content.addView(inflater.inflate(R.layout.profile_no_campaigns, null));
    }
	
	public void findCampaigns(View v){
		startActivityForResult(new Intent(this, Campaigns.class), REQ_CAMPAIGNS);
	}
	
}
