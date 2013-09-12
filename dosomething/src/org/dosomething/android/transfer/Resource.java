package org.dosomething.android.transfer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Resource implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String body;
	private String linkUrl;
	
	public Resource() {}
	
	public Resource(JSONObject obj) throws JSONException {
		
		body = obj.optString("item-body",null);
		linkUrl = obj.optString("item-link",null);
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("item-body", body);
		obj.put("item-link", linkUrl);
		
		return obj;
	}

	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getLinkUrl() {
		return linkUrl;
	}
	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}
	
}
