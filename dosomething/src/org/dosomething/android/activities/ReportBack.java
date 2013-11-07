package org.dosomething.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.WebForm;

import java.util.HashMap;

public class ReportBack extends AbstractWebForm {
	
	private static final String CAMPAIGN = "campaign";
	
	private WebForm webForm;
	
	@Override
	public String getPageName() {
		return "report-back";
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
		webForm = campaign.getReportBack();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onSubmitSuccess() {
		Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
		
		DSDao dao = new DSDao(this);

        UserCampaign uc = new UserCampaign.UserCampaignCVBuilder()
                .campaignId(campaign.getId())
                .uid(new UserContext(this).getUserUid())
                .build();
        Long userCampaignId = dao.setSignedUp(uc);
		
		// Track submission in analytics - Flurry Analytics
		HashMap<String, String> param = new HashMap<String, String>();
		param.put("campaign", campaign.getName());
		Analytics.logEvent("report-back-submit", param);
		
		// and Google Analytics
		Analytics.logEvent("report-back", "normal-submit", campaign.getName());
		
		startActivity(CampaignShare.getIntentForReportedBack(this, campaign));
		finish();
	}
	
	@Override
	protected WebForm getWebForm() {
		return webForm;
	}
	
	protected int getContentViewResourceId() {
		return R.layout.report_back;
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, ReportBack.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}

}
