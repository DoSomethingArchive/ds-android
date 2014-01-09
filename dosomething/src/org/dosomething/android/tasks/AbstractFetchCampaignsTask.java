package org.dosomething.android.tasks;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import org.acra.ErrorReporter;
import org.dosomething.android.DSConstants;
import org.dosomething.android.cache.Cache;
import org.dosomething.android.cache.DSPreferences;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.transfer.Campaign;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractFetchCampaignsTask extends AbstractWebserviceTask {

	protected final Context context;
	private final Cache cache;

	private List<Campaign> campaigns;

    // TODO: remove ActionBar parameter
	public AbstractFetchCampaignsTask(Context context, UserContext userContext, Cache cache) {
		super(userContext);
		this.context = context;
		this.cache = cache;
	}

	@Override
	protected void doWebOperation() throws Exception {

		if (cache != null) {
			campaigns = cache.getCampaigns();
		}

		if(campaigns == null){
			
			DSPreferences prefs = new DSPreferences(context);
			
			boolean isOnline = isOnline(context);
			
			if(isOnline){
				
				try{
					String url = DSConstants.CAMPAIGN_API_URL + "?q=campaigns";

					JSONObject json = doGet(url).getBodyAsJSONObject();

					setCampaigns(json);
					
					prefs.setCampaigns(json);

					if(cache != null){
						cache.setCampaigns(campaigns);
					}
					
				}catch(Exception e){
					ErrorReporter.getInstance().handleSilentException(e);
					
					//fall back to the last fetched, if any
					JSONObject json = prefs.getCampaigns();
					
					if(json != null){
						setCampaigns(json);
					}else{
						throw e;
					}
				}

			}else{
				//fall back to the last fetched, if any
				JSONObject json = prefs.getCampaigns();
				
				if(json != null){
					setCampaigns(json);
				}else{
					throw new NoInternetException();
				}
				
			}
		}
	}
	
	private void setCampaigns(JSONObject json) throws JSONException, ParseException{
		JSONArray names = json.names();

		campaigns = new ArrayList<Campaign>();

		for(int i = 0; i < names.length(); i++){
			String name = names.getString(i); 
			JSONObject object = json.getJSONObject(name);
			campaigns.add(convert(name, object));
		}

		Collections.sort(campaigns, new Comparator<Campaign>() {
			@Override
			public int compare(Campaign lhs, Campaign rhs) {
				return lhs.getOrder() - rhs.getOrder();
			}
		});
	}

	protected List<Campaign> getCampaigns(){
		return this.campaigns;
	}
	
	protected Campaign getCampaignById(String campaignId) {
		Iterator<Campaign> iter = campaigns.iterator();
		while (iter.hasNext()) {
			Campaign campaign = iter.next();
			if (campaign.getId().compareTo(campaignId) == 0) {
				return campaign;
			}
		}
		
		return null;
	}
	
	// Checks campaigns for 
	protected boolean hasHigherVersionCampaign() {
		int currVersionCode = 0;
		try {
			PackageInfo pInfo = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0);
			currVersionCode = pInfo.versionCode;
		}
		catch (NameNotFoundException e) {
			return false;
		}
		
		Iterator<Campaign> iter = campaigns.iterator();
		while (iter.hasNext()) {
			Campaign campaign = iter.next();
			if (campaign.getMinVersion() > currVersionCode) {
				return true;
			}
		}
		
		return false;
	}

	private Campaign convert(String id, JSONObject object) throws JSONException, ParseException {
		return new Campaign(id, object);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (context != null && context instanceof Activity) {
            Activity activityContext = (Activity)context;
            activityContext.setProgressBarIndeterminateVisibility(Boolean.TRUE);
        }
	}

	@Override
	protected void onFinish() {
		if (context != null && context instanceof Activity) {
            Activity activityContext = (Activity)context;
            activityContext.setProgressBarIndeterminateVisibility(Boolean.FALSE);
        }
	}

}