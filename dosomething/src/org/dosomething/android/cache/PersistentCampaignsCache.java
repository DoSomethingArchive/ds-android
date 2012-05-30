package org.dosomething.android.cache;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

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
	
	public void setCampaigns(JSONObject json){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(CAMPAIGNS, json.toString());
		editor.commit();
	}
	
	
}
