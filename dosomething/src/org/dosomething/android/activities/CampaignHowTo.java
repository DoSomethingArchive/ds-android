package org.dosomething.android.activities;

import org.dosomething.android.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.markupartist.android.widget.ActionBar;

public class CampaignHowTo extends RoboActivity {
	
	private static final String HOW_TO = "how_to";
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_how_to);
        
        actionBar.setHomeAction(Campaigns.getHomeAction(this));
    }

	public Intent getIntent(Context context){
		Intent answer = new Intent(context, CampaignHowTo.class);
		return answer;
	}
	
}
