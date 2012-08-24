package org.dosomething.android.tasks;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.acra.ErrorReporter;
import org.dosomething.android.cache.Cache;
import org.dosomething.android.cache.PersistentCampaignsCache;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.transfer.Campaign;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.widget.ProgressBar;

import com.markupartist.android.widget.ActionBar;

public abstract class AbstractFetchCampaignsTask extends AbstractWebserviceTask {

	private final Context context;
	private final ActionBar actionBar;
	private final Cache cache;

	private List<Campaign> campaigns;

	public AbstractFetchCampaignsTask(Context context, UserContext userContext, Cache cache, ActionBar actionBar){
		super(userContext);
		this.context = context;
		this.cache = cache;
		this.actionBar = actionBar;
	}

	@Override
	protected void doWebOperation() throws Exception {

		if(cache != null){
			campaigns = cache.getCampaigns();
		}

		if(campaigns == null){
			
			PersistentCampaignsCache persistentCache = new PersistentCampaignsCache(context);
			
			boolean isOnline = isOnline(context);
			
			if(isOnline){
				
				try{
					String url = API_URL + "?q=campaigns";

					JSONObject json = doGet(url).getBodyAsJSONObject();

					setCampaigns(json);
					
					persistentCache.setCampaigns(json);

					if(cache != null){
						cache.setCampaigns(campaigns);
					}
					
				}catch(Exception e){
					ErrorReporter.getInstance().handleSilentException(e);
					
					//fall back to the last fetched, if any
					JSONObject json = persistentCache.getCampaigns();
					
					if(json != null){
						setCampaigns(json);
					}else{
						throw e;
					}
				}

			}else{
				//fall back to the last fetched, if any
				JSONObject json = persistentCache.getCampaigns();
				
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

	private Campaign convert(String id, JSONObject object) throws JSONException, ParseException {
		return new Campaign(id, object);
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
	}

}
