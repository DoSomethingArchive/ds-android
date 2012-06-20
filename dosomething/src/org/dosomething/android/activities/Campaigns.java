package org.dosomething.android.activities;

import java.util.List;

import org.dosomething.android.R;
import org.dosomething.android.cache.Cache;
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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
	@InjectView(R.id.list) private ListView list;
	
	private final OnItemClickListener itemClickListener = new MyItemClickListener();
	
	@Override
	protected String getPageName() {
		return "campaigns";
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.campaigns);
        
        actionBar.addAction(profileButtonAction);
        
        // onResume is always called next
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		
		fetchCampaigns();
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
	
	private void fetchCampaigns(){
		new MyTask().execute();
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

		public MyTask() {
			super(Campaigns.this, userContext, cache, actionBar);
		}

		@Override
		protected void onSuccess() {
			list.setOnItemClickListener(itemClickListener);
			list.setAdapter(new MyAdapter(getApplicationContext(), getCampaigns()));
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
			}
			else {
				// we use GONE instead of INVISIBLE because we dont want it to leave a blank space
				textView.setVisibility(TextView.GONE);
			}

			return v;
		}
		
	}
	
}
