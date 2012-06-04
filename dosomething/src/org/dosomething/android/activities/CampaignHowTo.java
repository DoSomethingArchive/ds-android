package org.dosomething.android.activities;

import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.HowTo;
import org.dosomething.android.widget.CustomActionBar;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.markupartist.android.widget.ActionBar;

public class CampaignHowTo extends AbstractActivity {
	
	private static final String CAMPAIGN = "campaign";
	
	@Inject private LayoutInflater inflater;
	@Inject @Named("DINComp-CondBold")Typeface headerTypeface;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.list) private ListView list;
	
	@Override
	protected String getPageName() {
		return "campaign-how-to";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaign_how_to);
        
        actionBar.addAction(Campaigns.getHomeAction(this));
        
        Campaign campaign = (Campaign) getIntent().getExtras().get(CAMPAIGN);
        
        list.setAdapter(new MyAdapter(getApplicationContext(), campaign.getHowTos()));
    }
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign){
		Intent answer = new Intent(context, CampaignHowTo.class);
		answer.putExtra(CAMPAIGN, campaign);
		return answer;
	}
	
	
	private class MyAdapter extends ArrayAdapter<HowTo> {

		public MyAdapter(Context context, List<HowTo> objects){
			super(context, android.R.layout.simple_list_item_1, objects);
		}
		
		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.how_to_row, null);
			}
			
			HowTo howTo = getItem(position);
			
			TextView header = (TextView)v.findViewById(R.id.header);
			header.setTypeface(headerTypeface, Typeface.BOLD);
			header.setText(howTo.getHeader());
			
			TextView body = (TextView)v.findViewById(R.id.body);
			body.setText(howTo.getBody());

			return v;
		}
		
	}
}
