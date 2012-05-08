package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Challenge;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_actions);
        
        actionBar.setHomeAction(Campaigns.getHomeAction(this));
        
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        
        actionBar.setTitle(campaign.getName());
        
        list.addHeaderView(getHeader(campaign));
        
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
	
	private class MyAdapter extends ArrayAdapter<Challenge> {
		
		public MyAdapter(Context context, List<Challenge> objects) {
			super(context, android.R.layout.simple_list_item_1, objects);
		}

		@Override
		public View getView(int index, View v, ViewGroup parent) {
			if (v == null) {
				v = inflater.inflate(R.layout.action_row, null);
			}
			
			Challenge challenge = (Challenge) getItem(index);
			
			TextView body = (TextView)v.findViewById(R.id.body);
			body.setText(challenge.getText());
			
			return v;
		}
		
	}
	
}
