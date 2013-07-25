package org.dosomething.android.activities;

import java.util.List;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.People;
import org.dosomething.android.transfer.PeopleItem;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CampaignPeople extends AbstractActivity {
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@Inject @Named("DINComp-CondBold")Typeface headerTypeface;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.intro) private TextView introText;
	@InjectView(R.id.list) private ListView list;

	@Override
	protected String getPageName() {
		return "campaign-people";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.campaign_people);
		
		actionBar.addAction(Campaigns.getHomeAction(this));
		
		Campaign campaign = (org.dosomething.android.transfer.Campaign) getIntent().getExtras().get(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue());
		
		People people = campaign.getPeople();
		if (people != null) {
			introText.setText(people.getIntro());
			
			List<PeopleItem> peopleItems = people.getItems();
			list.setAdapter(new PeopleListAdapter(getApplicationContext(), peopleItems));
		}
	}
	
	public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
		Intent answer = new Intent(context, CampaignPeople.class);
		answer.putExtra(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue(), campaign);
		return answer;
	}

	private class PeopleListAdapter extends ArrayAdapter<PeopleItem> {
		public PeopleListAdapter(Context context, List<PeopleItem> objects) {
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
				v = inflater.inflate(R.layout.hib_row, null);
			}
			
			PeopleItem peopleItem = getItem(position);
			
			TextView header = (TextView)v.findViewById(R.id.header);
			header.setTypeface(headerTypeface, Typeface.BOLD);
			header.setText(peopleItem.getHeader());
			
			ImageView image = (ImageView)v.findViewById(R.id.image);
			imageLoader.displayImage(peopleItem.getImageUrl(), image);
			
			TextView body = (TextView)v.findViewById(R.id.body);
			body.setText(peopleItem.getBody());
			
			return v;
		}
	}
}
