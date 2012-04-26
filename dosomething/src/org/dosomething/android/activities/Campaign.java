package org.dosomething.android.activities;

import org.dosomething.android.R;
import org.dosomething.android.widgets.ActionBar;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class Campaign extends RoboActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign);
        
        org.dosomething.android.transfer.Campaign campaign = (org.dosomething.android.transfer.Campaign) getIntent().getSerializableExtra(CAMPAIGN);
        
        actionBar.setTitle(campaign.getName());
        
    }

	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, Campaign.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
}
