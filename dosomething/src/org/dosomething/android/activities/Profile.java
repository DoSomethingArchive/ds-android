package org.dosomething.android.activities;

import org.dosomething.android.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class Profile extends RoboActivity {
	
	private static final int REQ_CAMPAIGNS = 100;
	
	@InjectView(R.id.no_campaigns) private LinearLayout noCampaigns;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        noCampaigns.setVisibility(LinearLayout.VISIBLE);
    }
	
	public void findCampaigns(View v){
		startActivityForResult(new Intent(this, Campaigns.class), REQ_CAMPAIGNS);
	}
	
}
