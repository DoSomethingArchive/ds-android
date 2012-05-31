package org.dosomething.android.transfer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class PrizeItem implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String header;
	private String imageUrl;
	private String body;
	
	public PrizeItem() {}
	
	public PrizeItem(JSONObject obj) throws JSONException {
		
		header = obj.optString("item-header");
		imageUrl = obj.optString("item-image");
		body = obj.optString("item-body");	
	}
	
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
}
