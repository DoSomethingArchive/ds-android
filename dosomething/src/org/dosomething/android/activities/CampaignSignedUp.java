package org.dosomething.android.activities;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.markupartist.android.widget.ActionBar;

public class CampaignSignedUp extends AbstractActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	
	private Campaign campaign;
	
	@Override
	protected String getPageName() {
		return "campaign-signed-up";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_signed_up);
        
        campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        
        actionBar.setHomeAction(Campaigns.getHomeAction(this));
    }
	
	public void share(View v){
		startActivity(Intent.createChooser(campaign.getShareIntent(), getString(R.string.campaign_share_chooser)));
	}
	
	public void done(View v){
		finish();
	}

	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, CampaignSignedUp.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
}
