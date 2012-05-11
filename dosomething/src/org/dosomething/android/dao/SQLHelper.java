package org.dosomething.android.dao;

import org.dosomething.android.domain.CompletedCampaignAction;
import org.dosomething.android.domain.UserCampaign;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {
	
	private static final String DB_NAME = "ds_db";

	private static final int CURRENT_VERSION = 1;

	public SQLHelper(Context context) {
		super(context, DB_NAME, null, CURRENT_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(UserCampaign.CREATE_TABLE);
		db.execSQL(CompletedCampaignAction.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
