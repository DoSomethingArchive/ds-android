package org.dosomething.android.activities;

import org.dosomething.android.R;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.markupartist.android.widget.ActionBar;

public class CampaignSignedUp extends RoboActivity {
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_signed_up);
        
        actionBar.setHomeAction(Campaigns.getHomeAction(this));
    }
	
	public void share(View v){
		//TODO: share the main/additional-link url from campaign
		Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.putExtra(android.content.Intent.EXTRA_TEXT, "http://www.dosomething.org");
		i.setType("text/plain");
		startActivity(Intent.createChooser(i, getString(R.string.campaign_share_chooser)));
	}
	
	public void done(View v){
		finish();
	}

	public static Intent getIntent(Context context) {
		Intent answer = new Intent(context, CampaignSignedUp.class);
		return answer;
	}
	
}
