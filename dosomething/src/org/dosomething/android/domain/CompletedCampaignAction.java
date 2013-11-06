package org.dosomething.android.domain;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CompletedCampaignAction {

    public static final String TABLE_NAME = "completed_campaign_action";

    /**
     * Create the completed_campaign_action table and its columns.
     *
     * @param db The database the table is being created to
     */
    public static final void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_campaign_id INTEGER REFERENCES user_campaign, " +
                "action_text TEXT NOT NULL);"
        );
    }
	
	private Long id;
	private Long userCampaignId;
	private String actionText;
	
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

	@Override
	public String toString() {
		return "CompletedCampaignAction [id=" + id + ", userCampaignId="
				+ userCampaignId + ", actionText=" + actionText + "]";
	}
}
