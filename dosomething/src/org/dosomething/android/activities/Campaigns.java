package org.dosomething.android.activities;

import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.json.JSONArray;
import org.json.JSONObject;

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
		new MyTask().execute();
		
//		List<CampaignRef> campaigns = new ArrayList<CampaignRef>();
//		
//		Date date = new Date();
//		
//		for(int i = 0; i < 20; i++){
//			CampaignRef campaign = new CampaignRef();
//			campaign.setLogoUrl("http://placehold.it/350x150" + "&text=" + i + i + i);
//			campaign.setStartDate(date);
//			campaign.setEndDate(date);
//			campaign.setBackgroundColor(i % 2 == 0 ? "#DB709B" : "#3059E3");
//			campaigns.add(campaign);
//		}
//		
//		list.setOnItemClickListener(itemClickListener);
//		list.setAdapter(new MyAdapter(this, campaigns));
	}
	
	private final OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> av, View v, int position,
				long id) {
			Campaign campaign = (Campaign) list.getAdapter().getItem(position);
			
			startActivity(org.dosomething.android.activities.Campaign.getIntent(getApplicationContext(), campaign));
		}
	};
	
	private class MyTask extends AbstractWebserviceTask {

		private List<Campaign> campaigns;
		
		@Override
		protected void onSuccess() {
			list.setOnItemClickListener(itemClickListener);
			list.setAdapter(new MyAdapter(getApplicationContext(), campaigns));
		}

		@Override
		protected void onFinish() {}

		@Override
		protected void onError() {
		
		}

		@Override
		protected void doWebOperation() throws Exception {
			
			//JSONObject json = getObject(API_URL + "?q=campaigns");
			JSONObject json = getObject("http://api.shoutz.com/api/v2/shouts/3937");
			
			JSONArray names = json.names();
			
			for(int i = 0; i < names.length(); i++){
				String name = names.getString(0);
				json.get(name);
				
			}
			
		}
		
	}
	
	private class MyAdapter extends ArrayAdapter<Campaign> {

		public MyAdapter(Context context, List<Campaign> objects){
			super(context, android.R.layout.simple_list_item_1, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.campaign_row, null);
			}
			
			Campaign campaign = getItem(position);
			
			v.setBackgroundColor(Color.parseColor(campaign.getBackgroundColor()));
			
			ImageView imageView = (ImageView) v.findViewById(R.id.image);
			
			imageLoader.displayImage(campaign.getLogoUrl(), imageView);

			return v;
		}
		
	}
	
}
