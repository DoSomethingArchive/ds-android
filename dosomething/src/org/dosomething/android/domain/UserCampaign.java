package org.dosomething.android.domain;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserCampaign {

    public static final String TABLE_NAME = "user_campaign";

    // Database Version 1
    public static final String COL_ID = "id";
    public static final String COL_CAMPAIGN_ID = "campaign_id";
    public static final String COL_UID = "uid";
    // Added in Version 2
    public static final String COL_CAMPAIGN_NAME = "campaign_name";
    public static final String COL_CAUSE_TYPE = "cause_type";
    public static final String COL_COMPLETED_STEPS = "completed_steps";
    public static final String COL_DATE_COMPLETED = "date_completed";
    public static final String COL_DATE_ENDS = "date_ends";
    public static final String COL_DATE_SIGNED_UP = "date_signed_up";
    public static final String COL_DATE_STARTS = "date_starts";
    public static final String COL_URL_BACKGROUND = "url_background";
    public static final String COL_URL_LOGO = "url_logo";

    /**
     * Create the user_campaign table and its columns.
     *
     * @param db The database the table is being created to
     */
    public static final void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CAMPAIGN_ID + " TEXT NOT NULL, " +
                COL_UID + " TEXT NOT NULL, " +
                // Added in version 2 of the database
                COL_CAMPAIGN_NAME + " TEXT, " +
                COL_CAUSE_TYPE + " TEXT, " +
                COL_COMPLETED_STEPS + " TEXT, " +
                COL_DATE_COMPLETED + " INTEGER, " +
                COL_DATE_ENDS + " INTEGER, " +
                COL_DATE_SIGNED_UP + " INTEGER, " +
                COL_DATE_STARTS + " INTEGER);"
        );
    }

    /**
     * Modify the user_campaign table for version 2.
     *
     * @param db Database being upgraded
     */
    public static final void upgradeToV2(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_CAMPAIGN_NAME + " TEXT;");
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_CAUSE_TYPE + " TEXT");
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_COMPLETED_STEPS +" TEXT;");
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_DATE_COMPLETED + " INTEGER;");
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_DATE_ENDS + " INTEGER;");
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_DATE_SIGNED_UP + " INTEGER;");
        db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_DATE_STARTS + " INTEGER;");
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

    // Date the user signed up for the campaign (in seconds)
    private Long mDateSignedUp;

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
        mDateSignedUp = cursor.getLong(columnIdx++);
        mDateStarts = cursor.getLong(columnIdx++);
	}

    /**
     * Constructor to be used by the UserCampaignCVBuilder
     */
    private UserCampaign(String campaignId, String uid, String campaignName, String causeType,
                         String completedSteps, Long dateCompleted, Long dateEnds, Long dateSignedUp,
                         Long dateStarts) {
        mCampaignId = campaignId;
        mUid = uid;
        mCampaignName = campaignName;
        mCauseType = causeType;
        mCompletedSteps = completedSteps;
        mDateCompleted = dateCompleted;
        mDateEnds = dateEnds;
        mDateSignedUp = dateSignedUp;
        mDateStarts = dateStarts;
    }

    /**
     * Retrieve all column names from the user_campaign table.
     *
     * @return String[] of the column names
     */
    public static String[] getAllColumns() {
        return new String[] {
                COL_ID,
                COL_CAMPAIGN_ID,
                COL_UID,
                COL_CAMPAIGN_NAME,
                COL_CAUSE_TYPE,
                COL_COMPLETED_STEPS,
                COL_DATE_COMPLETED,
                COL_DATE_ENDS,
                COL_DATE_SIGNED_UP,
                COL_DATE_STARTS,
        };
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();

        cv.put(COL_CAMPAIGN_ID, mCampaignId);
        cv.put(COL_UID, mUid);
        cv.put(COL_CAMPAIGN_NAME, mCampaignName);
        cv.put(COL_CAUSE_TYPE, mCauseType);
        cv.put(COL_COMPLETED_STEPS, mCompletedSteps);
        cv.put(COL_DATE_COMPLETED, mDateCompleted);
        cv.put(COL_DATE_ENDS, mDateEnds);
        cv.put(COL_DATE_SIGNED_UP, mDateSignedUp);
        cv.put(COL_DATE_STARTS, mDateStarts);

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

    public Long getDateSignedUp() {
        return mDateSignedUp;
    }

    public Long getDateStarts() {
        return mDateStarts;
    }

	@Override
	public String toString() {
        return "UserCampaign [" +
                COL_ID + "=" + mId + ", " +
                COL_CAMPAIGN_ID + "=" + mCampaignId + ", " +
                COL_UID + "=" + mUid + ", " +
                COL_CAMPAIGN_NAME + "=" + mCampaignName + ", " +
                COL_CAUSE_TYPE + "=" + mCauseType + ", " +
                COL_COMPLETED_STEPS + "=" + mCompletedSteps + ", " +
                COL_DATE_COMPLETED + "=" + mDateCompleted + ", " +
                COL_DATE_ENDS + "=" + mDateEnds + ", " +
                COL_DATE_SIGNED_UP + "=" + mDateSignedUp + ", " +
                COL_DATE_STARTS + "=" + mDateStarts +
                "]";
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
        private Long mDateSignedUp;
        private Long mDateStarts;

        public UserCampaignCVBuilder() {
            mCampaignName = null;
            mCauseType = null;
            mCompletedSteps = null;
            mDateCompleted = null;
            mDateEnds = null;
            mDateSignedUp = null;
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

        public UserCampaignCVBuilder dateSignedUp(Long dateSignedUp) {
            this.mDateSignedUp = dateSignedUp;
            return this;
        }

        public UserCampaignCVBuilder dateStarts(Long dateStarts) {
            this.mDateStarts = dateStarts;
            return this;
        }

        public UserCampaign build() {
            return new UserCampaign(mCampaignId, mUid, mCampaignName, mCauseType, mCompletedSteps,
                    mDateCompleted, mDateEnds, mDateSignedUp, mDateStarts);
        }
    }
}
