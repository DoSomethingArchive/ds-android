package org.dosomething.android.transfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WebFormField implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String label;
	private String type;
	private String name;
	private boolean required;
	private List<WebFormSelectOptions> selectOptions;
	
	public WebFormField() {}
	
	public WebFormField(JSONObject obj) throws JSONException {
		
		label = obj.optString("label");
		type = obj.optString("type");
		name = obj.optString("name");
		required = obj.optBoolean("required", false);
		
		JSONArray o = obj.optJSONArray("select-options");
		if(o!=null) {
			selectOptions = new ArrayList<WebFormSelectOptions>(o.length());
			for(int i=0; i<o.length(); i++) {
				selectOptions.add(new WebFormSelectOptions(o.getJSONObject(i)));
			}
		}
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public List<WebFormSelectOptions> getSelectOptions() {
		return selectOptions;
	}

	public void setSelectOptions(List<WebFormSelectOptions> selectOptions) {
		this.selectOptions = selectOptions;
	}
}
