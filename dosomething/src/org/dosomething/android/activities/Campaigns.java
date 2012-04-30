package org.dosomething.android.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.acra.ErrorReporter;
import org.dosomething.android.R;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.json.JSONArray;
import org.json.JSONException;
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
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	
	@InjectView(R.id.list) private ListView list;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaigns);
        
        fetchCampaigns();
    }
	
	private void fetchCampaigns(){
		new MyTask().execute();
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
		
		private final SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy");
		
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
			//String url = API_URL + "?q=campaigns";
			
			String url = "http://dl.dropbox.com/u/15016480/campaigns.json";
			
			JSONObject json = getObject(url);
			
			JSONArray names = json.names();
			
			campaigns = new ArrayList<Campaign>();
			
			for(int i = 0; i < names.length(); i++){
				String name = names.getString(i);
				JSONObject object = json.getJSONObject(name);
				try{
					campaigns.add(convert(object));
				}catch(Exception e){
					ErrorReporter.getInstance().handleSilentException(e);
				}
				
			}
		}

		private Campaign convert(JSONObject object) throws JSONException, ParseException {
			Campaign answer = new Campaign();
			
			JSONObject co = object.getJSONObject("campaign");
			
			answer.setName(co.getString("campaign-name"));
			answer.setBackgroundColor("#" + co.getString("logo-bg-color"));
			answer.setStartDate(df.parse(co.getString("start-date")));
			answer.setEndDate(df.parse(co.getString("end-date")));
			answer.setLogoUrl(co.getString("logo"));
			answer.setTeaser(object.getJSONObject("main").getString("teaser"));
			
			return answer;
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
