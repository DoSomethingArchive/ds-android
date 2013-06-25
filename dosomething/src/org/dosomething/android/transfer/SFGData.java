package org.dosomething.android.transfer;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SFGData implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String galleryUrl;
	private String defaultEndpoint;
	private String locatorType;
	private String mySubmissionsEndpoint;
	private String shareSuccessMsg;
	private ArrayList<WebFormSelectOptions> typeOptions;
	private ArrayList<WebFormSelectOptions> locationOptions;
	
	public SFGData() {}
	
	public SFGData(JSONObject obj) throws JSONException {
		galleryUrl = obj.optString("gallery-url");
		defaultEndpoint = obj.optString("default-endpoint");
		locatorType = obj.optString("locator-type");
		mySubmissionsEndpoint = obj.optString("my-submissions-endpoint");
		shareSuccessMsg = obj.optString("share-success-message");
		
		JSONObject filterObj = obj.optJSONObject("filter-options");
		
		typeOptions = new ArrayList<WebFormSelectOptions>();
		JSONArray types = filterObj.optJSONArray("type");
		for (int i = 0; i < types.length(); i++) {
			JSONObject optObj = types.getJSONObject(i);
			WebFormSelectOptions opt = new WebFormSelectOptions(optObj);
			typeOptions.add(opt);
		}
		
		locationOptions = new ArrayList<WebFormSelectOptions>();
		JSONArray locations = filterObj.optJSONArray("location");
		for (int i = 0; i < locations.length(); i++) {
			JSONObject optObj = locations.getJSONObject(i);
			WebFormSelectOptions opt = new WebFormSelectOptions(optObj);
			locationOptions.add(opt);
		}
	}
	
	public String getGalleryUrl() {
		return galleryUrl;
	}
	
	public String getDefaultEndpoint() {
		return defaultEndpoint;
	}
	
	public String getLocatorType() {
		return locatorType;
	}
	
	public String getMySubmissionsEndpoint() {
		return mySubmissionsEndpoint;
	}
	
	public String getShareSuccessMsg() {
		return shareSuccessMsg;
	}
	
	public ArrayList<WebFormSelectOptions> getTypeOptions() {
		return typeOptions;
	}
	
	public ArrayList<WebFormSelectOptions> getLocationOptions() {
		return locationOptions;
	}
}
