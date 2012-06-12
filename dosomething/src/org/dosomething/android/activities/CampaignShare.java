package org.dosomething.android.activities;

import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.widget.CustomActionBar;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class CampaignShare extends AbstractActivity {
	
	private static final String CAMPAIGN = "campaign";
	private static final String TYPE = "type";
	
	public static final int SIGNED_UP = 222;
	public static final int REPORTED_BACK = 223;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.header) private TextView header;
	
	private Campaign campaign;
	private int type;
	
	@Override
	protected String getPageName() {
		return "campaign-signed-up";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_share);
        
        actionBar.addAction(Campaigns.getHomeAction(this));
        
        campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        type = getIntent().getExtras().getInt(TYPE);
        
        switch (type) {
		case SIGNED_UP:
			header.setText(getString(R.string.campaign_share_signup_header));
			break;
		case REPORTED_BACK:
			header.setText(getString(R.string.campaign_share_report_header));
			break;
		default:
			throw new RuntimeException();
		}
    }
	
	@Override
	public void onStart() {
		super.onStart();
		Analytics.logCampaignPageView(this, this.getPageName(), campaign);
	}
	
	public void share(View v){
		startActivity(Intent.createChooser(campaign.getShareIntent(), getString(R.string.campaign_share_chooser)));
	}
	
	public void done(View v){
		finish();
	}

	public static Intent getIntentForSignedUp(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, CampaignShare.class);
		answer.putExtra(CAMPAIGN, campaign);
		answer.putExtra(TYPE, SIGNED_UP);
		return answer;
	}
	
	public static Intent getIntentForReportedBack(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, CampaignShare.class);
		answer.putExtra(CAMPAIGN, campaign);
		answer.putExtra(TYPE, REPORTED_BACK);
		return answer;
	}
	
}
