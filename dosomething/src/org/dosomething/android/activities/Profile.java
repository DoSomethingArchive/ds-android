package org.dosomething.android.activities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.context.SessionContext;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class Profile extends RoboActivity {
	
	private static final String TAG = "Profile";
	private static final String DF = "MM/dd/yy";
	
	@Inject private LayoutInflater inflater;
	@Inject private SessionContext sessionContext;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.content) private LinearLayout content;
	
	private ListView list;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        actionBar.addAction(logoutAction);
        
        new MyTask().execute();
    }
	
	private final Action logoutAction = new Action(){

		@Override
		public int getDrawable() {
			return R.drawable.action_bar_profile;
		}

		@Override
		public void performAction(View view) {
			new UserContext(Profile.this).clear();
			startActivity(new Intent(getApplicationContext(), Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		}
		
	};
	
	public void findCampaigns(View v){
		finish();
	}

	public static Intent getIntent(Context context) {
		return new Intent(context, Profile.class);
	}
	
	private final OnItemClickListener itemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> av, View v, int position,
				long id) {
			Campaign campaign = (Campaign) list.getAdapter().getItem(position);
			
			startActivity(CampaignActions.getIntent(getApplicationContext(), campaign));
		}
	};
	
	private class MyAdapter extends ArrayAdapter<Campaign> {

		public MyAdapter(Context context, List<Campaign> objects){
			super(context, android.R.layout.simple_list_item_1, objects);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.profile_campaign_row, null);
			}
			
			Campaign campaign = getItem(position);
			
			TextView txtName = (TextView) v.findViewById(R.id.name);
			txtName.setText(campaign.getName());
			
			TextView txtEndDate = (TextView) v.findViewById(R.id.end_date);
			txtEndDate.setText("Ends: " + new SimpleDateFormat(DF).format(campaign.getEndDate()));

			return v;
		}
		
	}
	
	private class MyTask extends AbstractWebserviceTask {

		private List<Campaign> campaigns;
		
		public MyTask(){
			super(sessionContext);
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			actionBar.setProgressBarVisibility(ProgressBar.VISIBLE);
		}

		@Override
		protected void onSuccess() {
			if(campaigns.isEmpty()){
				content.addView(inflater.inflate(R.layout.profile_no_campaigns, null));
			}else{
				list = new ListView(Profile.this);
				content.addView(list, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1));
			
				list.setOnItemClickListener(itemClickListener);
				list.setAdapter(new MyAdapter(getApplicationContext(), campaigns));
			}
		}

		@Override
		protected void onFinish() {
			actionBar.setProgressBarVisibility(ProgressBar.GONE);
		}

		@Override
		protected void onError() {
		
		}

		@Override
		protected void doWebOperation() throws Exception {
			String url = API_URL + "?q=campaigns";
			
			//String url = "http://dl.dropbox.com/u/15016480/campaigns.json";
			
			try{
				JSONObject json = doGet(url).getBodyAsJSONObject();
				
				try{
					JSONArray names = json.names();
					
					campaigns = new ArrayList<Campaign>();
					
					for(int i = 0; i < names.length(); i++){
						String name = names.getString(i); 
						JSONObject object = json.getJSONObject(name);
						campaigns.add(convert(object));
					}
					
					Collections.sort(campaigns, new Comparator<Campaign>() {
						@Override
						public int compare(Campaign lhs, Campaign rhs) {
							return rhs.getEndDate().compareTo(lhs.getEndDate());
						}
					});
					
				}catch(Exception e){
					Log.e(TAG, "Failed to parse API.", e);
					toastError("Failed to parse API.");
				}
				
			}catch(Exception e){
				Log.e(TAG, "Failed to download API.", e);
				toastError("Failed to download API.");
			}
		}
		
		private void toastError(final String message) {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
				}
			});
		}

		private Campaign convert(JSONObject object) throws JSONException, ParseException {
			return new Campaign(object);
		}
		
	}
	
}
