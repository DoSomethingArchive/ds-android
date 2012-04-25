package org.dosomething.android.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.transfer.CampaignRef;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.ImageLoader;

public class Campaigns extends RoboActivity {
	
	@Inject
	private LayoutInflater inflater;
	@Inject
	private ImageLoader imageLoader;
	
	@InjectView(R.id.list) private ListView list;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaigns);
        
        fetchCampaigns();
    }
	
	private void fetchCampaigns(){
		List<CampaignRef> campaigns = new ArrayList<CampaignRef>();
		
		Date date = new Date();
		
		for(int i = 0; i < 20; i++){
			CampaignRef campaign = new CampaignRef();
			campaign.setLogoUrl("http://placehold.it/350x150" + "&text=" + i + i + i);
			campaign.setStartDate(date);
			campaign.setEndDate(date);
			campaign.setBackgroundColor(i % 2 == 0 ? "#DB709B" : "#3059E3");
			campaigns.add(campaign);
		}
		
		list.setOnItemClickListener(itemClickListener);
		list.setAdapter(new MyAdapter(this, campaigns));
	}
	
	private final OnItemClickListener itemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> av, View v, int position,
				long id) {
			CampaignRef campaign = (CampaignRef) list.getAdapter().getItem(position);
			
		}
	};
	
	private class MyAdapter extends ArrayAdapter<CampaignRef> {

		public MyAdapter(Context context, List<CampaignRef> objects){
			super(context, android.R.layout.simple_list_item_1, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.campaign_row, null);
			}
			
			CampaignRef campaign = getItem(position);
			
			v.setBackgroundColor(Color.parseColor(campaign.getBackgroundColor()));
			
			ImageView imageView = (ImageView) v.findViewById(R.id.image);
			
			imageLoader.displayImage(campaign.getLogoUrl(), imageView);

			return v;
		}
		
	}
	
}
