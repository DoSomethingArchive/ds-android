package org.dosomething.android.transfer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class Challenge implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String text;
	private String completionPage;
	
	public Challenge() {}
	
	public Challenge(JSONObject obj) throws JSONException {
		
		text = obj.getString("text");
		completionPage = obj.optString("completion-page");
		
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getCompletionPage() {
		return completionPage;
	}
	public void setCompletionPage(String completionPage) {
		this.completionPage = completionPage;
	}
}
