package org.dosomething.android.tasks;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.SessionContext;
import org.dosomething.android.transfer.Campaign;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.widget.ProgressBar;

import com.markupartist.android.widget.ActionBar;

public abstract class AbstractFetchCampaignsTask extends AbstractWebserviceTask {
	
	private final ActionBar actionBar;
	private final Cache cache;
	
	private List<Campaign> campaigns;
	
	public AbstractFetchCampaignsTask(SessionContext context, Cache cache, ActionBar actionBar){
		super(context);
		this.cache = cache;
		this.actionBar = actionBar;
	}
	
	@Override
	protected void doWebOperation() throws Exception {

		if(cache != null){
			campaigns = cache.getCampaigns();
		}
		
		if(campaigns == null){

			String url = API_URL + "?q=campaigns";

			//String url = "http://dl.dropbox.com/u/15016480/campaigns.json";

			JSONObject json = doGet(url).getBodyAsJSONObject();

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
			
			if(cache != null){
				cache.setCampaigns(campaigns);
			}
		}
	}
	
	protected List<Campaign> getCampaigns(){
		return this.campaigns;
	}
	
	private Campaign convert(JSONObject object) throws JSONException, ParseException {
		return new Campaign(object);
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
