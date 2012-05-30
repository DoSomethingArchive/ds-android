package org.dosomething.android.activities;

import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Prize;
import org.dosomething.android.transfer.PrizeItem;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CampaignPrizes extends AbstractActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	@Override
	protected String getPageName() {
		return "campaign-prizes";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_prizes);
        
        actionBar.setHomeAction(Campaigns.getHomeAction(this));
        
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        Prize prize = campaign.getPrize();
        
        if(prize.getScholarship()!=null) {
        	list.addHeaderView(createScholarshipView(prize.getScholarship()));
        }
    	list.setAdapter(new MyAdapter(getApplicationContext(), prize.getOthers()));
    	if(prize.getRulesUrl()!=null && prize.getRulesUrl().length() > 0) {
    		list.addFooterView(createRulesView(prize.getRulesUrl()));
    	}
    }
	
	private View createScholarshipView(PrizeItem scholarship) {
		View v = inflater.inflate(R.layout.prize_row, null);
		
		TextView header = (TextView)v.findViewById(R.id.header);
		header.setText(scholarship.getHeader());
		
		ImageView image = (ImageView)v.findViewById(R.id.image);
		imageLoader.displayImage(scholarship.getImageUrl(), image);
		
		TextView body = (TextView)v.findViewById(R.id.body);
		body.setText(scholarship.getBody());
		
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
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, CampaignPrizes.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
	private class MyAdapter extends ArrayAdapter<PrizeItem> {
		
		public MyAdapter(Context context, List<PrizeItem> objects) {
			super(context, android.R.layout.simple_list_item_1, objects);
		}
		
		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public View getView(int index, View v, ViewGroup parent) {
			
			if (v == null) {
				v = inflater.inflate(R.layout.prize_row, null);
			}
			
			PrizeItem prizeItem = (PrizeItem) getItem(index);
			
			TextView header = (TextView)v.findViewById(R.id.header);
			header.setText(prizeItem.getHeader());
			
			ImageView image = (ImageView)v.findViewById(R.id.image);
			imageLoader.displayImage(prizeItem.getImageUrl(), image);
			
			TextView body = (TextView)v.findViewById(R.id.body);
			body.setText(prizeItem.getBody());
			
			return v;
		}
		
	}
}
