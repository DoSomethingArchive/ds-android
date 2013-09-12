package org.dosomething.android.transfer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Faq implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String header;
	private String body;
	
	public Faq() {}
	
	public Faq(JSONObject obj) throws JSONException {
		
		header = obj.getString("item-header");
		body = obj.getString("item-body");
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("item-header", header);
		obj.put("item-body", body);
		
		return obj;
	}

	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
}
