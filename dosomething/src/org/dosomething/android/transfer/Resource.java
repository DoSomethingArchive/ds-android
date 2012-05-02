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
		
		body = obj.optString("item-body");
		linkUrl = obj.optString("item-link");
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
