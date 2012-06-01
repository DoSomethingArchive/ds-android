package org.dosomething.android.transfer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class WebFormSelectOptions implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String label;
	private String value;
	
	public WebFormSelectOptions() {}
	
	public WebFormSelectOptions(JSONObject obj) throws JSONException {
		
		label = obj.optString("label",null);
		value = obj.optString("value",null);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
