package org.dosomething.android.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import org.dosomething.android.transfer.Campaign;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PersistentCampaignsCache {
	
	private static final String MY_PREFS = "my_prefs";
	private static final String CAMPAIGNS = "campaigns";
	
	private final Context context;
	
	public PersistentCampaignsCache(Context context){
		this.context = context;
	}
	
	public JSONObject getCampaigns() throws JSONException{
		JSONObject answer = null;
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String json = settings.getString(CAMPAIGNS, null);
		
		if(json != null){
			answer = new JSONObject(json);
		}
		
		return answer;
	}

    public List<Campaign> getCampaignsAsList() throws JSONException, ParseException {

        JSONObject jsonCampaigns = getCampaigns();

        JSONArray names = jsonCampaigns.names();

        List<Campaign> campaigns = new ArrayList<Campaign>();

        for(int i = 0; i < names.length(); i++){
            String name = names.getString(i);
            JSONObject jsonCampaign = jsonCampaigns.getJSONObject(name);
            campaigns.add(new Campaign(name, jsonCampaign));
        }

        Collections.sort(campaigns, new Comparator<Campaign>() {
            @Override
            public int compare(Campaign lhs, Campaign rhs) {
                return lhs.getOrder() - rhs.getOrder();
            }
        });

        return campaigns;
    }
	
	public void setCampaigns(JSONObject json){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(CAMPAIGNS, json.toString());
		editor.commit();
	}
	
	
}
