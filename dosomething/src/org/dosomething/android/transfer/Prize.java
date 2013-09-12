package org.dosomething.android.transfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Prize implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private PrizeItem scholarship;
	private List<PrizeItem> others;
	private String rulesUrl;
	private String mainText;
	
	public Prize() {}
	
	public Prize(JSONObject obj) throws JSONException {
		
		JSONObject s = obj.optJSONObject("scholarships");
		if(s!=null) {
			scholarship = new PrizeItem(s);
		}
		
		JSONArray p = obj.optJSONArray("prizes");
		if(p!=null) {
			others = new ArrayList<PrizeItem>(p.length());
			for(int i=0; i<p.length(); i++) {
				others.add(new PrizeItem(p.getJSONObject(i)));
			}
		}
		
		rulesUrl = obj.optString("rules",null);
		
		mainText = obj.optString("main-text",null);
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("scholarships", scholarship.toJSON());
		
		if (others != null && others.size() > 0) {
			JSONArray jsonPrizes = new JSONArray();
			for (PrizeItem p : others) {
				jsonPrizes.put(p.toJSON());
			}
			obj.put("prizes", jsonPrizes);
		}
		
		obj.put("rules", rulesUrl);
		obj.put("main-text", mainText);
		
		return obj;
	}
	
	public PrizeItem getScholarship() {
		return scholarship;
	}
	public void setScholarship(PrizeItem scholarship) {
		this.scholarship = scholarship;
	}
	public List<PrizeItem> getOthers() {
		return others;
	}
	public void setOthers(List<PrizeItem> others) {
		this.others = others;
	}
	public String getRulesUrl() {
		return rulesUrl;
	}
	public void setRulesUrl(String rulesUrl) {
		this.rulesUrl = rulesUrl;
	}
	public String getMainText() {
		return mainText;
	}
}
