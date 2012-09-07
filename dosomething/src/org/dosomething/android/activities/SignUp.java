package org.dosomething.android.activities;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.MyDAO;
import org.dosomething.android.domain.CompletedCampaignAction;
import org.dosomething.android.receivers.AlarmReceiver;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Challenge;
import org.dosomething.android.transfer.WebForm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SignUp extends AbstractWebForm {
	
	private static final String CAMPAIGN = "campaign";
	private static final String CAMPAIGN_ID = "campaign-id";
	private static final String CAMPAIGN_NAME = "campaign-name";
	private static final int REPORT_BACK_ALARM_ID = 100;
	
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
		
		MyDAO dao = new MyDAO(this);
		
		Long userCampaignId = dao.setSignedUp(new UserContext(this).getUserUid(), campaign.getId());
		
		List<Challenge> challenges = campaign.getChallenges();
		
		if(challenges != null){
			for(Challenge challenge : challenges){
				if("sign-up".equals(challenge.getCompletionPage())){
					dao.addCompletedAction(new CompletedCampaignAction(userCampaignId, challenge.getText()));
					break;
				}
			}
		}
		
		// Check if this campaign has a report back challenge
		boolean canReportBack = false;
		if (challenges != null) {
			for (Challenge challenge : challenges) {
				if ("report-back".equals(challenge.getCompletionPage())) {
					canReportBack = true;
					break;
				}
			}
		}
		
		// If so, then set to trigger a notification to remind them later to report back
		if (canReportBack) {
			
			Date endDate = campaign.getEndDate();
			Calendar c = Calendar.getInstance();
			Date todayDate = c.getTime();
			
			// Set notification to trigger a week before end of campaign at 5pm
			c.setTime(endDate);
			c.add(Calendar.DAY_OF_MONTH, -7);
			c.set(Calendar.HOUR_OF_DAY, 18);
			c.set(Calendar.MINUTE, 0);
			c.add(Calendar.SECOND, 5);
			
			long alarmTime = c.getTimeInMillis();
			
			// Only trigger alarm if it'll be set after today
			if (c.after(todayDate)) {
				Intent reminder = new Intent(this, AlarmReceiver.class);
				reminder.putExtra(CAMPAIGN_ID, campaign.getId());
				reminder.putExtra(CAMPAIGN_NAME, campaign.getName());
				
				PendingIntent sender = PendingIntent.getBroadcast(this, REPORT_BACK_ALARM_ID, reminder, PendingIntent.FLAG_UPDATE_CURRENT);
				AlarmManager am = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.RTC_WAKEUP, alarmTime, sender);
			}
		}
		
		HashMap<String, String> param = new HashMap<String, String>();
		param.put(CAMPAIGN, campaign.getName());
		Analytics.logEvent("sign-up-submit", param);
		
		startActivity(CampaignShare.getIntentForSignedUp(this, campaign));
		finish();
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, SignUp.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}

}
