package org.dosomething.android.domain;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserCampaign {

    public static final String TABLE_NAME = "user_campaign";

    public static final String FIELD_ID = "id";
    public static final String FIELD_CAMPAIGN_ID = "campaign_id";
    public static final String FIELD_UID = "uid";
    public static final String FIELD_CAMPAIGN_NAME = "campaign_name";
    public static final String FIELD_CAUSE_TYPE = "cause_type";
    public static final String FIELD_COMPLETED_STEPS = "completed_steps";
    public static final String FIELD_DATE_COMPLETED = "date_completed";
    public static final String FIELD_DATE_ENDS = "date_ends";
    public static final String FIELD_DATE_STARTS = "date_starts";

    /**
     * Create the user_campaign table and its columns.
     *
     * @param db The database the table is being created to
     */
    public static final void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FIELD_CAMPAIGN_ID + " TEXT NOT NULL, " +
                FIELD_UID + " TEXT NOT NULL, " +
                // Added in version 2 of the database
                FIELD_CAMPAIGN_NAME + " TEXT, " +
                FIELD_CAUSE_TYPE + " TEXT, " +
                FIELD_COMPLETED_STEPS + " TEXT, " +
                FIELD_DATE_COMPLETED + " INTEGER, " +
                FIELD_DATE_ENDS + " INTEGER, " +
                FIELD_DATE_STARTS + " INTEGER);"
        );
    }

    /**
     * Modify the user_campaign table for version 2.
     *
     * @param db Database being upgraded
     */
    public static final void upgradeToV2(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + FIELD_CAMPAIGN_NAME + " TEXT;");
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + FIELD_CAUSE_TYPE + " TEXT");
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + FIELD_COMPLETED_STEPS +" TEXT;");
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + FIELD_DATE_COMPLETED + " INTEGER;");
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + FIELD_DATE_ENDS + " INTEGER;");
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + FIELD_DATE_STARTS + " INTEGER;");
    }

    ////////////////////
    // Fields from DB version 1
    ////////////////////

    // Primary key id
    private Long mId;

    // The campaign's unique id as provided by the API
    private String mCampaignId;

    // The user's unique id as provided by the Drupal backend
    private String mUid;

    ////////////////////
    // Fields added in DB version 2
    ////////////////////

    // The campaign's title
    private String mCampaignName;

    // The cause type(s) this campaign falls under
    private String mCauseType;

    // String representation of the campaign steps completed by the user
    private String mCompletedSteps;

    // Date the user completed the campaign (in seconds)
    private Long mDateCompleted;

    // Date the campaign ends (in seconds)
    private Long mDateEnds;

    // Date the campaign starts (in seconds)
    private Long mDateStarts;

    /**
     * Constructor when pulling data from the database
     *
     * @param cursor Interface to a database query's result set
     */
	public UserCampaign(Cursor cursor){
		int columnIdx = 0;
		
		mId = cursor.getLong(columnIdx++);
		mCampaignId = cursor.getString(columnIdx++);
		mUid = cursor.getString(columnIdx++);
        mCampaignName = cursor.getString(columnIdx++);
        mCauseType = cursor.getString(columnIdx++);
        mCompletedSteps = cursor.getString(columnIdx++);
        mDateCompleted = cursor.getLong(columnIdx++);
        mDateEnds = cursor.getLong(columnIdx++);
        mDateStarts = cursor.getLong(columnIdx++);
	}

    /**
     * Constructor to be used by the UserCampaignCVBuilder
     */
    private UserCampaign(String campaignId, String uid, String campaignName, String causeType,
                         String completedSteps, Long dateCompleted, Long dateEnds, Long dateStarts) {
        mCampaignId = campaignId;
        mUid = uid;
        mCampaignName = campaignName;
        mCauseType = causeType;
        mCompletedSteps = completedSteps;
        mDateCompleted = dateCompleted;
        mDateEnds = dateEnds;
        mDateStarts = dateStarts;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();

        cv.put(FIELD_CAMPAIGN_ID, mCampaignId);
        cv.put(FIELD_UID, mUid);
        cv.put(FIELD_CAMPAIGN_NAME, mCampaignName);
        cv.put(FIELD_CAUSE_TYPE, mCauseType);
        cv.put(FIELD_COMPLETED_STEPS, mCompletedSteps);
        cv.put(FIELD_DATE_COMPLETED, mDateCompleted);
        cv.put(FIELD_DATE_ENDS, mDateEnds);
        cv.put(FIELD_DATE_STARTS, mDateStarts);

        return cv;
    }

	public Long getId() {
		return mId;
	}

	public String getCampaignId() {
		return mCampaignId;
	}

	public String getUid() {
		return mUid;
	}

    public String getCampaignName() {
        return mCampaignName;
    }

    public String getCauseType() {
        return mCauseType;
    }

    public String getCompletedSteps() {
        return mCompletedSteps;
    }

    public Long getDateCompleted() {
        return mDateCompleted;
    }

    public Long getDateEnds() {
        return mDateEnds;
    }

    public Long getDateStarts() {
        return mDateStarts;
    }

	@Override
	public String toString() {
		return "UserCampaign [id=" + mId + ", campaignId=" + mCampaignId
				+ ", uid=" + mUid + "]";
	}

    /**
     * Builder class for UserCampaign
     */
    public static class UserCampaignCVBuilder {

        ////////////////////
        // Fields from DB version 1
        ////////////////////
        private String mCampaignId;
        private String mUid;

        ////////////////////
        // Fields added in DB version 2
        ////////////////////
        private String mCampaignName;
        private String mCauseType;
        private String mCompletedSteps;
        private Long mDateCompleted;
        private Long mDateEnds;
        private Long mDateStarts;

        public UserCampaignCVBuilder() {
            mCampaignName = null;
            mCauseType = null;
            mCompletedSteps = null;
            mDateCompleted = null;
            mDateEnds = null;
            mDateStarts = null;
        }

        public UserCampaignCVBuilder campaignId(String campaignId) {
            this.mCampaignId = campaignId;
            return this;
        }

        public UserCampaignCVBuilder uid(String uid) {
            this.mUid = uid;
            return this;
        }

        public UserCampaignCVBuilder campaignName(String campaignName) {
            this.mCampaignName = campaignName;
            return this;
        }

        public UserCampaignCVBuilder causeType(String causeType) {
            this.mCauseType = causeType;
            return this;
        }

        public UserCampaignCVBuilder completedSteps(String completedSteps) {
            this.mCompletedSteps = completedSteps;
            return this;
        }

        public UserCampaignCVBuilder dateCompleted(Long dateCompleted) {
            this.mDateCompleted = dateCompleted;
            return this;
        }

        public UserCampaignCVBuilder dateEnds(Long dateEnds) {
            this.mDateEnds = dateEnds;
            return this;
        }

        public UserCampaignCVBuilder dateStarts(Long dateStarts) {
            this.mDateStarts = dateStarts;
            return this;
        }

        public UserCampaign build() {
            return new UserCampaign(mCampaignId, mUid, mCampaignName, mCauseType, mCompletedSteps,
                    mDateCompleted, mDateEnds, mDateStarts);
        }
    }
}
