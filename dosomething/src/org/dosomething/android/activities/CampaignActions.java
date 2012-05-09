package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Challenge;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CampaignActions extends RoboActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	private Context context;
	private Campaign campaign;
	
	private Set<ChallengeType> completedChallenges;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_actions);
        
        context = this;
        
        actionBar.setHomeAction(Campaigns.getHomeAction(this));
        
        campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        
        actionBar.setTitle(campaign.getName());
        
        list.addHeaderView(getHeader(campaign));
        
        completedChallenges = new HashSet<ChallengeType>();
        completedChallenges.add(ChallengeType.SIGN_UP);
        
        List<Challenge> challenges = campaign.getChallenges();
        if(challenges == null){
        	challenges = new ArrayList<Challenge>();
        }
        
        list.setAdapter(new MyAdapter(getApplicationContext(), challenges));
    }
	
	private View getHeader(Campaign campaign) {
		View answer = inflater.inflate(R.layout.campaign_actions_header, null);
		View v = answer.findViewById(R.id.image_container);
		v.setBackgroundColor(Color.parseColor(campaign.getBackgroundColor()));
		ImageView imageView = (ImageView) v.findViewById(R.id.image);
		imageLoader.displayImage(campaign.getLogoUrl(), imageView);
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
			
			CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox1);
			
			ChallengeType type = ChallengeType.findByValue(challenge.getCompletionPage());
			
			boolean checked = false;
			
			if(type != null){
				if(completedChallenges.contains(type)){
					checked = true;
				}
			}
			
			checkBox.setChecked(checked);
			
			checkBox.setOnCheckedChangeListener(new MyCheckedChangeListener(challenge));
			
			TextView body = (TextView)v.findViewById(R.id.body);
			body.setText(challenge.getText());
			
			return v;
		}
		
	}
	
	private final class MyCheckedChangeListener implements OnCheckedChangeListener {

		private final Challenge challenge;
		
		private MyCheckedChangeListener(Challenge challenge){
			this.challenge = challenge;
		}
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if(isChecked){
				String completion = challenge.getCompletionPage();
				if(completion != null){
					if(completion.startsWith("http")){
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(completion)));
					}else{
						
						ChallengeType type = ChallengeType.findByValue(challenge.getCompletionPage());
						
						if(type != null){
							switch(type){
							case SIGN_UP:
								startActivity(CampaignSignedUp.getIntent(context, campaign));
								break;
							case REPORT_BACK:
								//startActivity(new Intent(context, ReportBack.class));
								break;
							case RESOURCES:
								startActivity(CampaignResources.getIntent(context, campaign));
								break;
							case SHARE:
								Intent i = new Intent(android.content.Intent.ACTION_SEND);
								i.putExtra(android.content.Intent.EXTRA_TEXT, campaign.getAdditionalLinkUrl());
								i.setType("text/plain");
								startActivity(Intent.createChooser(i, getString(R.string.campaign_share_chooser)));
								break;
							}
						}
	
					}
					
				}
			}
		}
		
	}
	
}
