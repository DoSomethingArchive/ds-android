package org.dosomething.android.transfer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Gallery implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String type;
	private String feed;
	
	public Gallery() {}
	
	public Gallery(JSONObject obj) throws JSONException {
		
		type = obj.getString("type");
		feed = obj.getString("feed");
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("type", type);
		obj.put("feed", feed);
		
		return obj;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFeed() {
		return feed;
	}

	public void setFeed(String feed) {
		this.feed = feed;
	}

}
