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
	private String selectType;
	private boolean required;
	private List<WebFormSelectOptions> selectOptions;
	
	public WebFormField() {}
	
	public WebFormField(JSONObject obj) throws JSONException {
		
		label = obj.optString("label",null);
		type = obj.optString("type",null);
		name = obj.optString("name",null);
		selectType = obj.optString("select-type",null);
		required = obj.optBoolean("required", false);
		
		JSONArray o = obj.optJSONArray("select-options");
		if(o!=null) {
			selectOptions = new ArrayList<WebFormSelectOptions>(o.length());
			for(int i=0; i<o.length(); i++) {
				selectOptions.add(new WebFormSelectOptions(o.getJSONObject(i)));
			}
		}
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("label", label);
		obj.put("type", type);
		obj.put("name", name);
		obj.put("select-type", selectType);
		obj.put("required", required);
		
		if (selectOptions != null) {
			JSONArray opts = new JSONArray(); 
			for (WebFormSelectOptions opt : selectOptions) {
				if (opt != null)
					opts.put(opt.toJSON());
			}
			obj.put("select-options", opts);
		}
		
		return obj;
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

	public String getSelectType() {
		return selectType;
	}

	public void setSelectType(String selectType) {
		this.selectType = selectType;
	}
}
