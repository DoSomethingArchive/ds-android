package org.dosomething.android.transfer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class SFGGalleryItem implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private boolean adopted;
	private String imageURL;
	private String name;
	private int shareCount;
	private String shelter;
	private String state;
	private String story;
	
	public SFGGalleryItem(JSONObject obj) throws JSONException {
		
		// Required items
		imageURL = obj.getString("image");
		name = obj.getString("name");
		shareCount = obj.getInt("share_count");
		
		// Optional items
		adopted = obj.optBoolean("adopted", false);
		shelter = obj.optString("shelter", null);
		state = obj.optString("state", null);
		story = obj.optString("story", null);
	}
	
	public boolean getAdopted() {
		return adopted;
	}
	
	public String getImageURL() {
		return imageURL;
	}
	
	public String getName() {
		return name;
	}
	
	public int getShareCount() {
		return shareCount;
	}
	
	public String getShelter() {
		return shelter;
	}
	
	public String getState() {
		return state;
	}
	
	public String getStory() {
		return story;
	}
}
