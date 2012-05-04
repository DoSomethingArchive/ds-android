package org.dosomething.android.activities;

import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Prize;
import org.dosomething.android.transfer.PrizeItem;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CampaignPrizes extends RoboActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_prizes);
        
        actionBar.setHomeAction(Campaigns.getHomeAction(this));
        
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        Prize prize = campaign.getPrize();
        
    	list.setAdapter(new MyAdapter(prize));
    }
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, CampaignPrizes.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
	private class MyAdapter extends BaseAdapter {
		
		private Prize prize;
		
		public MyAdapter(Prize prize) {
			this.prize = prize;
		}
		
		@Override
		public int getCount() {
			int count = 0;
			
			if(prize.getScholarship()!=null) {
				count++;
			}
			
			if(prize.getOthers()!=null) {
				count+=prize.getOthers().size();
			}
			
			if(prize.getRulesUrl()!=null) {
				count++;
			}
			
			return count;
		}

		@Override
		public Object getItem(int index) {
			
			return null;
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		@Override
		public View getView(int index, View v, ViewGroup parent) {
			
			int othersOffset = (prize.getScholarship()!=null) ? 1 : 0;
			
			if(prize.getScholarship()!=null && index==0) {
				if (v == null) {
					v = inflater.inflate(R.layout.prize_row, null);
				}
				
				PrizeItem prizeItem = prize.getScholarship();
				
				TextView header = (TextView)v.findViewById(R.id.header);
				header.setText(prizeItem.getHeader());
				
				ImageView image = (ImageView)v.findViewById(R.id.image);
				imageLoader.displayImage(prizeItem.getImageUrl(), image);
				
				TextView body = (TextView)v.findViewById(R.id.body);
				body.setText(prizeItem.getBody());
				
			} else if(prize.getOthers()!=null && index-othersOffset < prize.getOthers().size()) {
				if (v == null) {
					v = inflater.inflate(R.layout.prize_row, null);
				}
				
				PrizeItem prizeItem = prize.getOthers().get(index-othersOffset);
				
				TextView header = (TextView)v.findViewById(R.id.header);
				header.setText(prizeItem.getHeader());
				
				ImageView image = (ImageView)v.findViewById(R.id.image);
				imageLoader.displayImage(prizeItem.getImageUrl(), image);
				
				TextView body = (TextView)v.findViewById(R.id.body);
				body.setText(prizeItem.getBody());
			} else {
				
				if (v == null) {
					v = inflater.inflate(R.layout.prize_row, null);
				}
				
				TextView header = (TextView)v.findViewById(R.id.header);
				header.setText(prize.getRulesUrl());
			}
			
			return v;
		}

		
		
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			View v = convertView;
//			if (v == null) {
//				v = inflater.inflate(R.layout.prize_row, null);
//			}
//			
//			final PrizeItem prizeItem = getItem(position);
//			
//			TextView header = (TextView)v.findViewById(R.id.header);
//			header.setText(prizeItem.getHeader());
//			
//			ImageView image = (ImageView)v.findViewById(R.id.image);
//			imageLoader.displayImage(prizeItem.getImageUrl(), image);
//			
//			TextView body = (TextView)v.findViewById(R.id.body);
//			body.setText(prizeItem.getBody());
//			
//
//			return v;
//		}
		
	}
}
