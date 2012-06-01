package org.dosomething.android.domain;

import android.database.Cursor;

public class UserCampaign {

	public static final String CREATE_TABLE = "create table user_campaign (id integer primary key autoincrement, campaign_id text not null, uid text not null);";
	
	private Long id;
	private String campaignId;
	private String uid;
	
	public UserCampaign(Long id, String campaignId, String uid) {
		super();
		this.id = id;
		this.campaignId = campaignId;
		this.uid = uid;
	}
	
	public UserCampaign(Cursor cursor){
		int i = 0;
		
		setId(cursor.getLong(i++));
		setCampaignId(cursor.getString(i++));
		setUid(cursor.getString(i++));
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCampaignId() {
		return campaignId;
	}
	public void setCampaignId(String campaignId) {
		this.campaignId = campaignId;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}

	@Override
	public String toString() {
		return "UserCampaign [id=" + id + ", campaignId=" + campaignId
				+ ", uid=" + uid + "]";
	}
}
