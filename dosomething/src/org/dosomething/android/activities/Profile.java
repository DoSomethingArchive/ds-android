package org.dosomething.android.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dosomething.android.R;
import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.SessionContext;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.MyDAO;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.tasks.AbstractFetchCampaignsTask;
import org.dosomething.android.transfer.Campaign;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class Profile extends RoboActivity {
	
	//private static final String TAG = "Profile";
	private static final String DF = "MM/dd/yy";
	
	@Inject private LayoutInflater inflater;
	@Inject private SessionContext sessionContext;
	@Inject private Cache cache;
	
	@InjectView(R.id.actionbar) private ActionBar actionBar;
	@InjectView(R.id.content) private LinearLayout content;
	
	private ListView list;
	
	private Context context;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        context = this;
        
        actionBar.addAction(logoutAction);
        
        new MyTask().execute();
    }
	
	private final Action logoutAction = new Action(){

		@Override
		public int getDrawable() {
			return R.drawable.action_bar_logout;
		}

		@Override
		public void performAction(View view) {
			new UserContext(Profile.this).clear();
			sessionContext.clear();
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
	
	private class MyTask extends AbstractFetchCampaignsTask {

		public MyTask(){
			super(sessionContext, cache, actionBar);
		}
		
		@Override
		protected void onSuccess() {
			List<Campaign> campaigns = new ArrayList<Campaign>();
			
			List<UserCampaign> userCampaigns = new MyDAO(context).findUserCampaigns(new UserContext(context).getUserUid());
			
			Set<String> userCampaignIds = new HashSet<String>();
			
			for(UserCampaign campaign : userCampaigns){
				userCampaignIds.add(campaign.getCampaignId());
			}
			
			for(Campaign campaign : getCampaigns()){
				if(userCampaignIds.contains(campaign.getId())){
					campaigns.add(campaign);
				}
			}
			
			if(campaigns.isEmpty()){
				content.addView(inflater.inflate(R.layout.profile_no_campaigns, null));
			}else{
				list = new ListView(Profile.this);
				content.addView(list, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1));
			
				list.setOnItemClickListener(itemClickListener);
				list.setAdapter(new MyAdapter(context, campaigns));
			}
		}

		@Override
		protected void onError() {}
		
	}
	
}
