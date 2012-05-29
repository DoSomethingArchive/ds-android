package org.dosomething.android.activities;

import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.MyDAO;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.WebForm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SignUp extends AbstractWebForm {
	
	private static final String CAMPAIGN = "campaign";
	
	private WebForm webForm;
	
	@Override
	protected String getPageName() {
		return "sign-up";
	}
	
	protected int getContentViewResourceId() {
		return R.layout.sign_up;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
		webForm = campaign.getSignUp();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected WebForm getWebForm() {
		return webForm;
	}
	
	@Override
	protected void onSubmitSuccess() {
		Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
		new MyDAO(this).setSignedUp(new UserContext(this).getUserUid(), campaign.getId());
		startActivity(CampaignActions.getIntent(this, campaign));
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, SignUp.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
}
