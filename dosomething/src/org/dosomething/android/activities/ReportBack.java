package org.dosomething.android.activities;

import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.MyDAO;
import org.dosomething.android.domain.CompletedCampaignAction;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Challenge;
import org.dosomething.android.transfer.WebForm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ReportBack extends AbstractWebForm {
	
	private static final String CAMPAIGN = "campaign";
	
	private WebForm webForm;
	
	@Override
	protected String getPageName() {
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
		
		MyDAO dao = new MyDAO(this);
		
		Long userCampaignId = dao.setSignedUp(new UserContext(this).getUserUid(), campaign.getId());
		
		List<Challenge> challenges = campaign.getChallenges();
		
		if(challenges != null){
			for(Challenge challenge : challenges){
				if("report-back".equals(challenge.getCompletionPage())){
					dao.addCompletedAction(new CompletedCampaignAction(userCampaignId, challenge.getText()));
					break;
				}
			}
		}
		
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
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, ReportBack.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}

}
