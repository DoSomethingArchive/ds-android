package org.dosomething.android.transfer;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class GalleryItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private GalleryItemType type;
	private String url;
	private String thumb;
	
	public GalleryItem() {}
	
	public GalleryItem(GalleryItemType type, JSONObject obj) throws JSONException {
		
		switch (type) {
		case IMAGE:
			url = obj.getString("image");
			thumb = obj.getString("thumbnail");
			break;
		case VIDEO:
			url = obj.getString("video");
			thumb = obj.getString("thumbnail");
			break;
		default:
			throw new RuntimeException();
		}
	}
	
	public GalleryItemType getType() {
		return type;
	}

	public void setType(GalleryItemType type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getThumb() {
		return thumb;
	}
	public void setThumb(String thumb) {
		this.thumb = thumb;
	}
	
	public static enum GalleryItemType {
		IMAGE,
		VIDEO
	}
}
