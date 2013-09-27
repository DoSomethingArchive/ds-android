package org.dosomething.android.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Prize;
import org.dosomething.android.transfer.PrizeItem;

import roboguice.inject.InjectView;

public class CampaignPrizes extends AbstractActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@Inject @Named("DINComp-CondBold")Typeface headerTypeface;

	@InjectView(R.id.content) private LinearLayout content;
	
	@Override
	protected String getPageName() {
		return "campaign-prizes";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_prizes);
        
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        Prize prize = campaign.getPrize();
        
        if(prize.getMainText()!=null && prize.getMainText().length() > 0) {
        	content.addView(createMainTextView(prize.getMainText()));
        }
        
        if(prize.getScholarship()!=null) {
        	content.addView(createScholarshipView(prize.getScholarship()));
        }
    	
        if(prize.getOthers()!=null) {
        	for(PrizeItem prizeItem : prize.getOthers()) {
        		content.addView(createPrizeView(prizeItem));
        	}
        }
        
    	if(prize.getRulesUrl()!=null && prize.getRulesUrl().length() > 0) {
    		content.addView(createRulesView(prize.getRulesUrl()));
    	}
    }
	
	private View createScholarshipView(PrizeItem scholarship) {
		View v = inflater.inflate(R.layout.prize_row, null);
		
		TextView header = (TextView)v.findViewById(R.id.header);
		header.setText(scholarship.getHeader());
		
		ImageView image = (ImageView)v.findViewById(R.id.image);
		header.setTypeface(headerTypeface, Typeface.BOLD);
		imageLoader.displayImage(scholarship.getImageUrl(), image);
		
		TextView body = (TextView)v.findViewById(R.id.body);
		body.setText(scholarship.getBody());
		
		return v;
	}
	
	private View createPrizeView(PrizeItem prizeItem) {
		View v = inflater.inflate(R.layout.prize_row, null);
		
		TextView header = (TextView)v.findViewById(R.id.header);
		header.setTypeface(headerTypeface, Typeface.BOLD);
		header.setText(prizeItem.getHeader());
		
		ImageView image = (ImageView)v.findViewById(R.id.image);
		imageLoader.displayImage(prizeItem.getImageUrl(), image);
		
		TextView body = (TextView)v.findViewById(R.id.body);
		body.setText(prizeItem.getBody());
		
		return v;
	}
	
	private View createRulesView(final String rules) {
		View v = inflater.inflate(R.layout.prize_rules, null);
		
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(rules)));
			}
		});
		
		return v;
	}
	
	private View createMainTextView(final String mainText) {
		View v = inflater.inflate(R.layout.prize_main_text, null);
		
		TextView body = (TextView)v.findViewById(R.id.body);
		body.setText(mainText);
		
		return v;
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, CampaignPrizes.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
}
