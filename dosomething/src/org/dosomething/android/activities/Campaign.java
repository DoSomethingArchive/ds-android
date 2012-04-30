package org.dosomething.android.activities;

import java.text.SimpleDateFormat;

import org.dosomething.android.R;
import org.dosomething.android.widgets.ActionBar;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.ImageLoader;

public class Campaign extends RoboActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@Inject private ImageLoader imageLoader;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.image) private ImageView imgLogo;
	@InjectView(R.id.image_container) private LinearLayout llImageContainer;
	@InjectView(R.id.dates) private TextView txtDates;
	@InjectView(R.id.teaser) private TextView txtTeaser;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign);
        
        org.dosomething.android.transfer.Campaign campaign = (org.dosomething.android.transfer.Campaign) getIntent().getSerializableExtra(CAMPAIGN);
        
        actionBar.setTitle(campaign.getName());
        
        SimpleDateFormat df = new SimpleDateFormat("MMMMM dd");
        
        txtDates.setText(df.format(campaign.getStartDate()) + "-" + df.format(campaign.getEndDate()));
        txtTeaser.setText(campaign.getTeaser());
        
        llImageContainer.setBackgroundColor(Color.parseColor(campaign.getBackgroundColor()));
        imageLoader.displayImage(campaign.getLogoUrl(), imgLogo);
    }
	
	public void signUp(View v){
		startActivity(CampaignSignedUp.getIntent(this));
	}

	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, Campaign.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
}
