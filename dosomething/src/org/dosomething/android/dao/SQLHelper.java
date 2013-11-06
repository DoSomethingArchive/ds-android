package org.dosomething.android.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.dosomething.android.domain.CompletedCampaignAction;
import org.dosomething.android.domain.UserCampaign;

public class SQLHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "ds_db";

	private static final int CURRENT_VERSION = 2;

	public SQLHelper(Context context) {
		super(context, DB_NAME, null, CURRENT_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		UserCampaign.createTable(db);
        CompletedCampaignAction.createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle version 1 to version 2 upgrades
        if (newVersion == 2 && oldVersion == 1) {
            // Update the UserCampaign table for version 2
            UserCampaign.upgradeToV2(db);
        }
	}

}
