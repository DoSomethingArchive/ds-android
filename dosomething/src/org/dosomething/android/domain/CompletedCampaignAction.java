package org.dosomething.android.domain;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CompletedCampaignAction {

    public static final String TABLE_NAME = "completed_campaign_action";

    public static final String COL_ID = "id";
    public static final String COL_USER_CAMPAIGN_ID = "user_campaign_id";
    public static final String COL_ACTION_TEXT = "action_text";

    /**
     * Create the completed_campaign_action table and its columns.
     *
     * @param db The database the table is being created to
     */
    public static final void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USER_CAMPAIGN_ID + " INTEGER REFERENCES user_campaign, " +
                COL_ACTION_TEXT + " TEXT NOT NULL);"
        );
    }
	
	private Long mId;
	private Long mUserCampaignId;
	private String mActionText;
	
	public CompletedCampaignAction(Long userCampaignId, String actionText) {
		super();
		mUserCampaignId = userCampaignId;
		mActionText = actionText;
	}
	
	public CompletedCampaignAction(Cursor cursor){
		int i = 0;
		
		mId = cursor.getLong(i++);
		mUserCampaignId = cursor.getLong(i++);
		mActionText = cursor.getString(i++);
	}
	
	public Long getId() {
		return mId;
	}

	public Long getUserCampaignId() {
		return mUserCampaignId;
	}

	public String getActionText() {
		return mActionText;
	}

	@Override
	public String toString() {
        return "CompletedCampaignAction [" +
                COL_ID + "=" + mId + ", " +
                COL_USER_CAMPAIGN_ID + "=" + mUserCampaignId + ", " +
                COL_ACTION_TEXT + "=" + mActionText +
                "]";
	}
}
