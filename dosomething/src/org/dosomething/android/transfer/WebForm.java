package org.dosomething.android.transfer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WebForm implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String nodeId;
	private String pageTitle;
	private String postUrl;
	private List<WebFormField> fields;
	
	public WebForm() {}
	
	public WebForm(JSONObject obj) throws JSONException {
		
		nodeId = obj.optString("node-id");
		pageTitle = obj.optString("page-title");
		postUrl = obj.optString("url");
		
		JSONArray f = obj.optJSONArray("fields");
		if(f!=null) {
			fields = new ArrayList<WebFormField>(f.length());
			for(int i=0; i<f.length(); i++) {
				fields.add(new WebFormField(f.getJSONObject(i)));
			}
		}
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject obj = new JSONObject();
		
		obj.put("node-id", nodeId);
		obj.put("page-itle", pageTitle);
		obj.put("url", postUrl);
		
		if (fields != null && fields.size() > 0) {
			JSONArray f = new JSONArray();
			for (WebFormField w : fields) {
				f.put(w.toJSON());
			}
			obj.put("fields", f);
		}
		
		return obj;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public List<WebFormField> getFields() {
		return fields;
	}

	public void setFields(List<WebFormField> fields) {
		this.fields = fields;
	}
	
	public String getPageTitle() {
		return pageTitle;
	}
	
	public String getPostUrl() {
		return postUrl;
	}

}
