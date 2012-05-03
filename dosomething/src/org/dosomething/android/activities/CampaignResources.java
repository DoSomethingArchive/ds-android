package org.dosomething.android.activities;

import org.dosomething.android.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.markupartist.android.widget.ActionBar;

public class CampaignResources extends RoboActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_resources);
        
        actionBar.setHomeAction(Campaigns.getHomeAction(this));
    }
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, CampaignResources.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
}
