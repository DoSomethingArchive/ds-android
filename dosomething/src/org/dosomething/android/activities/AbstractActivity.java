package org.dosomething.android.activities;

import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.transfer.Campaign;

import roboguice.activity.RoboActivity;

public abstract class AbstractActivity extends RoboActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	protected abstract String getPageName();
	
	public void onStart(){
	   super.onStart();
	   Analytics.startSession(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		boolean bLogged = false;
		if (getIntent() != null && getIntent().getExtras() != null) {
			Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
			if (campaign != null) {
				Analytics.logCampaignPageView(this, this.getPageName(), campaign);
				bLogged = true;
			}
		}
		
		if (!bLogged) {
			Analytics.logPageView(this, this.getPageName());
		}
	}
	
	public void onStop(){
	   super.onStop();
	   Analytics.endSession(this);
	}
	
}
