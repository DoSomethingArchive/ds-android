package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.MyDAO;
import org.dosomething.android.domain.CompletedCampaignAction;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Challenge;
import org.dosomething.android.widget.CustomActionBar;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CampaignActions extends AbstractActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@Inject private UserContext userContext;
	@Inject @Named("DINComp-CondBold")Typeface headerTypeface;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	private Context context;
	private Campaign campaign;
	
	private Set<String> completedChallenges;
	
	private MyDAO dao;
	
	private UserCampaign userCampaign;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_actions);
        
        context = this;
        
        actionBar.addAction(Campaigns.getHomeAction(this));
        
        actionBar.addAction(Login.getLogoutAction(this, userContext));
        
        campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        
        actionBar.setTitle(campaign.getName());
        
        dao = new MyDAO(this);
        
        completedChallenges = new HashSet<String>();
        
        String userUid = new UserContext(this).getUserUid();
        
        userCampaign = dao.findUserCampaign(userUid, campaign.getId());
        
        if(userCampaign != null){
        	for(CompletedCampaignAction action : dao.getCompletedActions(userCampaign.getId())){
        		completedChallenges.add(action.getActionText());
        	}
        }
        
        list.addHeaderView(getHeader(campaign));

        List<Challenge> challenges = campaign.getChallenges();
        if(challenges == null){
        	challenges = new ArrayList<Challenge>();
        }
        
        list.setAdapter(new MyAdapter(getApplicationContext(), challenges));
    }
	
	@Override
	public void onStart() {
		super.onStart();
		Analytics.logCampaignPageView(this, this.getPageName(), campaign);
	}

	public void viewCampaign(View v){
		startActivity(org.dosomething.android.activities.Campaign.getIntent(this, campaign));
	}
	
	private View getHeader(Campaign campaign) {
		View answer = inflater.inflate(R.layout.campaign_actions_header, null);
		View v = answer.findViewById(R.id.image_container);
		v.setBackgroundColor(Color.parseColor(campaign.getBackgroundColor()));
		ImageView imageView = (ImageView) v.findViewById(R.id.image);
		imageLoader.displayImage(campaign.getLogoUrl(), imageView);
		
		TextView headerText = (TextView)answer.findViewById(R.id.actions_header);
		headerText.setTypeface(headerTypeface, Typeface.BOLD);
		
		return answer;
	}

	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, CampaignActions.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
	private enum ChallengeType {
		SIGN_UP("sign-up"),REPORT_BACK("report-back"),SHARE("share"),RESOURCES("resources");
		
		private final String value;
		
		private ChallengeType(String value){
			this.value = value;
		}

		public String getValue() {
			return value;
		}
		
		public static ChallengeType findByValue(String str){
			ChallengeType answer = null;
			for(ChallengeType ct : values()){
				if(ct.getValue().equals(str)){
					answer = ct;
					break;
				}
			}
			return answer;
		}

	}
	
	private class MyAdapter extends ArrayAdapter<Challenge> {
		
		public MyAdapter(Context context, List<Challenge> objects) {
			super(context, android.R.layout.simple_list_item_1, objects);
		}

		@Override
		public View getView(int index, View v, ViewGroup parent) {
			if (v == null) {
				v = inflater.inflate(R.layout.action_row, null);
			}
			
			final Challenge challenge = (Challenge) getItem(index);
			
			Log.d("asdf", "text="+challenge.getText() + "  complete="+challenge.getCompletionPage());
			
			boolean completed = completedChallenges.contains(challenge.getText());
			boolean actionable = !completed && challenge.getCompletionPage()!=null;
			
			Button button = (Button) v.findViewById(R.id.button);
			button.setOnClickListener(new MyActionClickListener(challenge));
			button.setVisibility(actionable ? Button.VISIBLE : Button.GONE);
			
			CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
			checkBox.setChecked(completed);
			checkBox.setOnCheckedChangeListener(new MyActionCheckListener(challenge, button));
			
			TextView body = (TextView)v.findViewById(R.id.body);
			body.setText(challenge.getText());
			
			return v;
		}
		
	}
	
	private synchronized void addActionCompleted(String actionText){
		if(userCampaign != null){
			completedChallenges.add(actionText);
			dao.addCompletedAction(new CompletedCampaignAction(userCampaign.getId(), actionText));
		}
	}
	
	private synchronized void removeActionCompleted(String actionText){
		if(userCampaign != null){
			completedChallenges.remove(actionText);
			dao.removeCompletedAction(userCampaign.getId(), actionText);
		}
	}
	
	private final class MyActionCheckListener implements OnCheckedChangeListener {
		
		private final Button actionButton;
		private final Challenge challenge;
		
		private MyActionCheckListener(Challenge challenge, Button actionButton){
			this.actionButton = actionButton;
			this.challenge = challenge;
		}
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			String challengeText = challenge.getText();
			
			
			if(isChecked) {
				if(challengeText != null){
					addActionCompleted(challengeText);
				}
				actionButton.setVisibility(Button.GONE);
			}else{
				if(challengeText != null){
					removeActionCompleted(challengeText);
				}
				boolean actionable = challenge.getCompletionPage()!=null;
				actionButton.setVisibility(actionable ? Button.VISIBLE : Button.GONE);
			}
		}
	}
	
	@Override
	protected String getPageName() {
		return "campaign-actions";
	}
	
	private final class MyActionClickListener implements OnClickListener {

		private final Challenge challenge;
		
		private MyActionClickListener(Challenge challenge){
			this.challenge = challenge;
		}
		
		@Override
		public void onClick(View v) {
			String text = challenge.getText();
				
			if(text != null){
				addActionCompleted(text);
			}
			
			String completion = challenge.getCompletionPage();
			if(completion != null){
				if(completion.startsWith("http")){
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(completion)));
				}else{
					
					ChallengeType type = ChallengeType.findByValue(challenge.getCompletionPage());
					
					if(type != null){
						switch(type){
						case SIGN_UP:
							startActivity(CampaignShare.getIntentForSignedUp(context, campaign));
							break;
						case REPORT_BACK:
							startActivity(ReportBack.getIntent(context, campaign));
							break;
						case RESOURCES:
							startActivity(CampaignResources.getIntent(context, campaign));
							break;
						case SHARE:
							startActivity(Intent.createChooser(campaign.getShareIntent(), getString(R.string.campaign_share_chooser)));
							break;
						}
					}

				}
				
			}
		}
	}
}
