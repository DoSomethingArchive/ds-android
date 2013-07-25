package org.dosomething.android.transfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class People implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String intro;
	private List<PeopleItem> items;
	
	public People() {}
	
	public People(JSONObject obj) throws JSONException {
		intro = obj.optString("intro");
		
		JSONArray arrItems = obj.optJSONArray("items");
		items = new ArrayList<PeopleItem>(arrItems.length());
		for (int i = 0; i < arrItems.length(); i++) {
			items.add(new PeopleItem(arrItems.getJSONObject(i)));
		}
	}
	
	public String getIntro() {
		return intro;
	}
	
	public List<PeopleItem> getItems() {
		return items;
	}
}
