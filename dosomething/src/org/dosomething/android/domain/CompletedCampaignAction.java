package org.dosomething.android.domain;

import android.database.Cursor;

public class CompletedCampaignAction {

	public static final String CREATE_TABLE = "create table completed_campaign_action (id integer primary key autoincrement, user_campaign_id integer references user_campaign, action_text text not null);";
	
	private Long id;
	private Long userCampaignId;
	private String actionText;
	
	public CompletedCampaignAction(){}
	
	public CompletedCampaignAction(Long userCampaignId, String actionText) {
		super();
		this.userCampaignId = userCampaignId;
		this.actionText = actionText;
	}
	
	public CompletedCampaignAction(Cursor cursor){
		int i = 0;
		
		setId(cursor.getLong(i++));
		setUserCampaignId(cursor.getLong(i++));
		setActionText(cursor.getString(i++));
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getUserCampaignId() {
		return userCampaignId;
	}
	public void setUserCampaignId(Long userCampaignId) {
		this.userCampaignId = userCampaignId;
	}
	public String getActionText() {
		return actionText;
	}
	public void setActionText(String actionText) {
		this.actionText = actionText;
	}
}
