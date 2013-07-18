package org.dosomething.android.activities;

import org.dosomething.android.DSConstants;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.transfer.Campaign;

import roboguice.activity.RoboFragmentActivity;

public abstract class AbstractFragmentActivity extends RoboFragmentActivity {

	protected abstract String getPageName();
	
	public void onStart() {
		super.onStart();
		Analytics.startSession(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		boolean bLogged = false;
		if (getIntent() != null && getIntent().getExtras() != null) {
			Campaign campaign = (Campaign) getIntent().getExtras().get(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue());
			if (campaign != null) {
				Analytics.logCampaignPageView(this, this.getPageName(), campaign);
				bLogged = true;
			}
		}
		
		if (!bLogged) {
			Analytics.logPageView(this, this.getPageName());
		}
	}
	
	public void onStop() {
		super.onStop();
		Analytics.endSession(this);
	}
}
