package org.dosomething.android.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.MyDAO;
import org.dosomething.android.domain.CompletedCampaignAction;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.tasks.AbstractFetchCampaignsTask;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Challenge;
import org.dosomething.android.widget.CustomActionBar;
import org.json.JSONArray;
import org.json.JSONObject;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
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
import com.google.inject.name.Named;
import com.markupartist.android.widget.ActionBar.Action;

public class Profile extends AbstractActivity {
	
	//private static final String TAG = "Profile";
	private static final int REQ_LOGIN_FOR_PROFILE = 112;
	private static final String DF = "MM/dd/yy";
	private static final String FROM_CAUSE_SEL = "from_cause_sel";
	
	@Inject private LayoutInflater inflater;
	@Inject private UserContext userContext;
	@Inject private @Named("DINComp-CondBold")Typeface headerTypeface;
	@Inject private Cache cache;
	@Inject private MyDAO dao;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.content) private LinearLayout content;
	
	private ListView list;
	
	private Context context;
	private Action profileAction;
	private boolean initializingActivity = true;
	
	@Override
	protected String getPageName() {
		return "profile";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        
        context = this;
        
        actionBar.addAction(causeAction);
        actionBar.addAction(Campaigns.getHomeAction(this));
        
        if (getIntent() != null && getIntent().getExtras() != null) {
	        boolean bFromCauseSel = getIntent().getExtras().getBoolean(FROM_CAUSE_SEL);
	        if (bFromCauseSel) {
	        	Toast toast = Toast.makeText(this, R.string.cause_confirm, Toast.LENGTH_LONG);
	        	toast.setGravity(Gravity.TOP, 0, 64);
	        	toast.show();
	        	getIntent().removeExtra(FROM_CAUSE_SEL);
	        }
        }
        
        // onResume is always call next
    }
	
	private final Action causeAction = new Action() {
		@Override
		public int getDrawable() {
			return R.drawable.action_bar_cause;
		}

		@Override
		public void performAction(View view) {
			Context ctx = getApplicationContext();
			startActivity(new Intent(ctx, CauseSelector.class));
		}
	};
	
	private final Action loginAction = new Action(){

		@Override
		public int getDrawable() {
			return R.drawable.action_bar_login;
		}

		@Override
		public void performAction(View view) {
			Context ctx = getApplicationContext();
			startActivityForResult(new Intent(ctx, Login.class), REQ_LOGIN_FOR_PROFILE);
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == REQ_LOGIN_FOR_PROFILE && resultCode == RESULT_OK){
			if(new UserContext(this).isLoggedIn()){
				startActivity(Profile.getIntent(getApplicationContext()));
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		content.removeAllViews();
		
		if (profileAction != null) {
			actionBar.removeAction(profileAction);
			profileAction = null;
		}
		
		if (userContext.isLoggedIn()) {
			// Add logout action
			profileAction = Login.getLogoutAction(this, userContext);
			actionBar.addAction(profileAction);
			
			// Start process to find campaigns the user's signed up for
			if (initializingActivity) {
				// Only query for user data on initial activity load
				new UserTask().execute();
				initializingActivity = false;
			}
			else {
				// When activity is just being brought to the front again, skip user sync
				new CampaignTask().execute();
			}
		}
		else {
			// Add login action
			profileAction = loginAction;
			actionBar.addAction(profileAction);
			
			// Show the "no campaigns" layout
			content.addView(inflater.inflate(R.layout.profile_logged_out, null));
		}
	}
	
	public void findCampaigns(View v) {
		startActivity(new Intent(this, Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	}
	
	public void goLoginRegister(View v) {
		startActivityForResult(new Intent(this, Login.class), REQ_LOGIN_FOR_PROFILE);
	}

	public static Intent getIntent(Context context) {
		return new Intent(context, Profile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
		
		private List<UserCampaign> userCampaigns;
		private List<Campaign> campaigns;
		
		public MyAdapter(Context context, List<UserCampaign> userCampaigns, List<Campaign> campaigns){
			super(context, android.R.layout.simple_list_item_1, campaigns);
			this.userCampaigns = userCampaigns;
			this.campaigns = campaigns;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				v = inflater.inflate(R.layout.profile_campaign_row, null);
			}
			
			UserCampaign userCampaign = userCampaigns.get(position);
			Campaign campaign = campaigns.get(position);
			
			TextView txtName = (TextView) v.findViewById(R.id.name);
			txtName.setTypeface(headerTypeface, Typeface.BOLD);
			txtName.setText(campaign.getName());
			
			List<CompletedCampaignAction> completeActions = dao.getCompletedActions(userCampaign.getId());
			
			TextView txtCompleted = (TextView) v.findViewById(R.id.completed);
			txtCompleted.setText("Completed: " + completeActions.size() + " of " + campaign.getChallenges().size());
			
			TextView txtEndDate = (TextView) v.findViewById(R.id.end_date);
			txtEndDate.setText("Ends: " + new SimpleDateFormat(DF, Locale.US).format(campaign.getEndDate()));

			return v;
		}
		
	}
	
	/**
	 * Task to get user's profile info from server.
	 */
	private class UserTask extends AbstractWebserviceTask {
		private ArrayList<Integer> gids = new ArrayList<Integer>();
		
		public UserTask() {
			super(userContext);
		}
		
		@Override
		protected void doWebOperation() throws Exception {
			String uid = new UserContext(context).getUserUid();
			String url = DSConstants.API_URL_BASE + "user/"+uid+".json";
			
			WebserviceResponse response = doGet(url);
			if (!response.hasErrorStatusCode()) {
				// gid's found in group_audience object show what campaigns, clubs,
				// and other OG groups the user is signed up for
				JSONObject jsonResponse = response.getBodyAsJSONObject();
				JSONObject group_audience = jsonResponse.getJSONObject("group_audience");
				JSONArray groups_array = group_audience.getJSONArray("und");
				if (groups_array != null) {
					for (int i = 0; i < groups_array.length(); i++) {
						JSONObject group = groups_array.getJSONObject(i);
						int gid = group.getInt("gid");
						gids.add(Integer.valueOf(gid));
					}
				}
			}
		}

		@Override
		protected void onSuccess() {
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if(actionBar != null){
				actionBar.setProgressBarVisibility(ProgressBar.VISIBLE);
			}
		}

		@Override
		protected void onFinish() {
			if(actionBar != null){
				actionBar.setProgressBarVisibility(ProgressBar.GONE);
			}
			
			// Start task to retrieve campaigns
			new CampaignTask(gids).execute();
		}

		@Override
		protected void onError(Exception e) {
		}
	}
	
	private class CampaignTask extends AbstractFetchCampaignsTask {
		private ArrayList<Integer> gids;
		
		public CampaignTask() {
			super(Profile.this.context, userContext, cache, actionBar);
		}

		public CampaignTask(ArrayList<Integer> gids){
			super(Profile.this.context, userContext, cache, actionBar);
			this.gids = gids;
		}
		
		@Override
		protected void onSuccess() {
			List<Campaign> campaigns = new ArrayList<Campaign>();
			List<UserCampaign> userCampaigns = new ArrayList<UserCampaign>();
			
			boolean foundSignUpOnServer = false;
			
			Map<String, UserCampaign> userCampaignsMap = new HashMap<String, UserCampaign>();
			String uid = new UserContext(context).getUserUid();
			if (uid != null) {
				List<UserCampaign> allUserCampaigns = dao.findUserCampaigns(uid);
				for(UserCampaign userCampaign : allUserCampaigns){
					userCampaignsMap.put(userCampaign.getCampaignId(), userCampaign);
				}
				
				for(Campaign campaign : getCampaigns()){
					boolean addCampaign = false;
					if(userCampaignsMap.containsKey(campaign.getId())){
						addCampaign = true;
					}
					else {
						// User OG id's to determine if user signed up for this campaign on the website
						for (int i = 0; gids != null && i < gids.size(); i++) {
							if (campaign.getGid() == gids.get(i).intValue()) {
								
								// Save campaign as being signed up for
								Long userCampaignId = dao.setSignedUp(uid, campaign.getId());
								// And mark the sign-up challenge as completed
								List<Challenge> challenges = campaign.getChallenges();
								if (challenges != null){
									for (Challenge challenge : challenges) {
										if ("sign-up".equals(challenge.getCompletionPage())) {
											dao.addCompletedAction(new CompletedCampaignAction(userCampaignId, challenge.getText()));
											break;
										}
									}
								}
								
								// Add to userCampaignsMap HashMap
								UserCampaign newUserCampaign = dao.findUserCampaign(uid, campaign.getId());
								if (newUserCampaign != null) {
									userCampaignsMap.put(newUserCampaign.getCampaignId(), newUserCampaign);
									
									// Only add to list for display if this succeeds. Otherwise, will crash
									addCampaign = true;
									foundSignUpOnServer = true;
								}
								
								break;
							}
						}
					}
					
					if (addCampaign) {
						campaigns.add(campaign);
						userCampaigns.add(userCampaignsMap.get(campaign.getId()));
					}
				}
				
				if (foundSignUpOnServer) {
					Toast toast = Toast.makeText(context, R.string.profile_campaign_synced, Toast.LENGTH_LONG);
					toast.show();
				}
			}
			
			if(campaigns.isEmpty()){
				content.addView(inflater.inflate(R.layout.profile_no_campaigns, null));
			}else{
				list = new ListView(Profile.this);
				content.addView(list, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT, 1));
			
				list.setOnItemClickListener(itemClickListener);
				list.setAdapter(new MyAdapter(context, userCampaigns, campaigns));
			}
		}

		@Override
		protected void onError(Exception e) {
			Toast toast = Toast.makeText(context, R.string.profile_load_error, Toast.LENGTH_LONG);
			toast.show();
		}
		
	}
	
}
