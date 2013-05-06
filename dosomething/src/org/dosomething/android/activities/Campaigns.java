package org.dosomething.android.activities;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.cache.Cache;
import org.dosomething.android.cache.DSPreferences;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractFetchCampaignsTask;
import org.dosomething.android.tasks.NoInternetException;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.widget.CustomActionBar;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;
import com.nostra13.universalimageloader.core.ImageLoader;

public class Campaigns extends AbstractActivity {
	
	//private static final String TAG = "Campaigns";
	private static final int REQ_LOGIN_FOR_PROFILE = 112;
	
	@Inject private LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@Inject private UserContext userContext;
	@Inject private Cache cache;
	@Inject @Named("DINComp-CondBold")Typeface calloutTypeface;
	
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.popup) private RelativeLayout popupView;
	@InjectView(R.id.popupMsg) private TextView popupMsgView;
	@InjectView(R.id.popupClose) private Button popupCloseButton;
	@InjectView(R.id.list) private PullToRefreshListView pullToRefreshView;
	
	private ListView list;
	
	private final OnItemClickListener itemClickListener = new MyItemClickListener();
	
	@Override
	protected String getPageName() {
		return "campaigns";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaigns);
        
        list = pullToRefreshView.getRefreshableView();
        pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {
        	@Override
        	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        		fetchCampaigns(true);
        	}
        });
        
        actionBar.addAction(profileButtonAction);
        
        // Upgrade notification popup click listeners
        popupMsgView.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		// Open link to the Google Play Store
        		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.dosomething.android")));
        	}
        });
        popupCloseButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		popupView.setVisibility(View.GONE);
        	}
        });
        
        // onResume is always called next
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		
		fetchCampaigns(false);
	}
	
	private final Action profileButtonAction = new Action(){

		@Override
		public int getDrawable() {
			return R.drawable.action_bar_profile;
		}

		@Override
		public void performAction(View view) {
			Context context = getApplicationContext();
			startActivity(Profile.getIntent(context));
			finish();
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
	
	private void fetchCampaigns(boolean forceSearch) {
		MyTask task = new MyTask();
		
		task.setForceSearch(forceSearch);
		task.execute();
	}
	
	public static Action getHomeAction(Context context){
		return new IntentAction(context, new Intent(context, Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), R.drawable.action_bar_home);
	}
	
	private class MyItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> av, View v, int position,
				long id) {
			Campaign campaign = (Campaign) list.getAdapter().getItem(position);
			
			startActivity(org.dosomething.android.activities.Campaign.getIntent(getApplicationContext(), campaign));
		}
	};
	
	private class MyTask extends AbstractFetchCampaignsTask {

		private boolean currentVersionOutdated = false;
		private boolean forceSearch;

		public MyTask() {
			super(Campaigns.this, userContext, cache, actionBar);
			
			forceSearch = false;
		}

		@Override
		protected void onSuccess() {
			list.setOnItemClickListener(itemClickListener);

			// Don't display campaigns that require a higher version than what we have
			int version = 0;
			try {
				PackageInfo pInfo = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0);
				version = pInfo.versionCode;
			}
			catch (NameNotFoundException e) {
			}
			
			List<Campaign> campaigns = getCampaigns();
			Iterator<Campaign> iter = campaigns.iterator();
			while (iter.hasNext()) {
				Campaign campaign = iter.next();
				if (campaign.getMinVersion() > version) {
					currentVersionOutdated = true;
					iter.remove();
				}
			}
			
			list.setAdapter(new MyAdapter(getApplicationContext(), campaigns));
		}

		@Override
		protected void onError(Exception e) {
			String message;
			if(e instanceof NoInternetException) {
				message = getString(R.string.campaigns_no_internet);
			} else {
				message = getString(R.string.campaigns_failed);
			}
			
			new AlertDialog.Builder(Campaigns.this)
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton(getString(R.string.ok_upper), null)
				.create()
				.show();
		}
		
		@Override
		protected void doWebOperation() throws Exception {
			if (forceSearch) {
				cache.clearCampaigns();
			}
			
			super.doWebOperation();
		}
		
		@Override
		protected void onFinish() {
			super.onFinish();
			pullToRefreshView.onRefreshComplete();
			
			if (currentVersionOutdated) {
				popupView.setVisibility(View.VISIBLE);
			}
			else {
				popupView.setVisibility(View.GONE);
			}
		}
		
		protected void setForceSearch(boolean force) {
			this.forceSearch = force;
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
			
			ImageView bgImageView = (ImageView) v.findViewById(R.id.background);
			if(campaign.getBackgroundUrl()!=null) {
				imageLoader.displayImage(campaign.getBackgroundUrl(), bgImageView);
			} else {
				bgImageView.setImageDrawable(null);
			}
			
			ImageView imageView = (ImageView) v.findViewById(R.id.image);
			ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
			imageLoader.displayImage(campaign.getLogoUrl(), imageView, new ProgressBarImageLoadingListener(progressBar));
			
			TextView textView = (TextView) v.findViewById(R.id.callout);
			if(campaign.getCallout() != null && campaign.getCallout().length() > 0) {
				textView.setText(campaign.getCallout());
				textView.setTypeface(calloutTypeface);
				textView.setVisibility(TextView.VISIBLE);
				
				// Change text color and background color if it's a past campaign
				Calendar cal = Calendar.getInstance();
				Date todayDate = cal.getTime();
				if (todayDate.after(campaign.getEndDate())) {
					// TODO: for past campaigns, maybe also set "Past Campaigns" text, even if none was set
					int bgColor = getResources().getColor(R.color.campaigns_past_campaign_callout_background);
					textView.setBackgroundColor(bgColor);
					int textColor = getResources().getColor(R.color.campaigns_past_campaign_callout_text);
					textView.setTextColor(textColor);
				}
				else {
					int bgColor = getResources().getColor(R.color.campaigns_callout_background);
					textView.setBackgroundColor(bgColor);
					int textColor = getResources().getColor(R.color.campaigns_callout_text);
					textView.setTextColor(textColor);
				}
			}
			else {
				// we use GONE instead of INVISIBLE because we dont want it to leave a blank space
				textView.setVisibility(TextView.GONE);
			}
			
			ImageView imageCause = (ImageView) v.findViewById(R.id.cause_tag);
			imageCause.setVisibility(ImageView.GONE);	// GONE by default
			
			int[] tags = campaign.getCauseTags();
			if(tags != null && tags.length > 0) {
				boolean bValidTag = false;
				DSPreferences prefs = new DSPreferences(getApplicationContext());
				int[] userCauses = prefs.getCauses();
				
				for(int i=0; i<tags.length && !bValidTag; i++) {
					for(int j=0; j<userCauses.length && !bValidTag; j++) {
						if(tags[i] == userCauses[j] && getCauseDrawable(tags[i]) > 0) {
							imageCause.setImageResource(getCauseDrawable(tags[i]));
							imageCause.setVisibility(ImageView.VISIBLE);
							
							bValidTag = true;
							break;
						}
					}
				}
			}

			return v;
		}
		
		private int getCauseDrawable(int cause_id) {
			switch(cause_id) {
			case DSPreferences.CAUSE_ANIMALS:
				return R.drawable.cause_animals_tag;
			case DSPreferences.CAUSE_BULLYING:
				return R.drawable.cause_bullying_tag;
			case DSPreferences.CAUSE_DISASTERS:
				return R.drawable.cause_disasters_tag;
			case DSPreferences.CAUSE_DISCRIMINATION:
				return R.drawable.cause_discrimination_tag;
			case DSPreferences.CAUSE_EDUCATION:
				return R.drawable.cause_education_tag;
			case DSPreferences.CAUSE_ENVIRONMENT:
				return R.drawable.cause_environment_tag;
			case DSPreferences.CAUSE_HEALTH:
				return R.drawable.cause_health_tag;
			case DSPreferences.CAUSE_HUMAN_RIGHTS:
				return R.drawable.cause_human_rights_tag;
			case DSPreferences.CAUSE_POVERTY:
				return R.drawable.cause_poverty_tag;
			case DSPreferences.CAUSE_RELATIONSHIPS:
				return R.drawable.cause_relationships_tag;
			case DSPreferences.CAUSE_TROOPS:
				return R.drawable.cause_troops_tag;
			default:
				return -1;
			}
		}
		
	}
	
}
