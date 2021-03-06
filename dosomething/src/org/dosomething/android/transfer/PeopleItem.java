package org.dosomething.android.transfer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class PeopleItem implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String header;
	private String body;
	private String imageUrl;
	
	public PeopleItem() {}
	
	public PeopleItem(JSONObject obj) throws JSONException {
		header = obj.optString("item-header");
		body = obj.optString("item-body");
		imageUrl = obj.optString("item-image");
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("item-header", header);
		obj.put("item-body", body);
		obj.put("item-image", imageUrl);
		
		return obj;
	}
	
	public String getHeader() {
		return header;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}
	
	public String getBody() {
		return body;
	}
}
