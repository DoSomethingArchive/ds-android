package org.dosomething.android.context;

import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserContext {
	
	private static final String MY_PREFS = "my_prefs";
	private static final String USER_NAME = "user_name";
	private static final String UID = "user_uid";
	private static final String SESSION_ID = "session_id";
	private static final String SESSION_NAME = "session_name";
	private static final String EXPIRES_AT = "expires_at";
	
	private static final long EXPIRES_AT_PADDING = 24 * 60 * 60 * 1000; // 24hrs
	
	private final Context context;
	
	public UserContext(Context context){
		this.context = context;
	}
	
	public String getUserName(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(USER_NAME, null);
		return uid;
	}
	
	public String getUserUid(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(UID, null);
		return uid;
	}
	
	public String getSessionId(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(SESSION_ID, null);
		return uid;
	}
	
	public String getSessionName(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(SESSION_NAME, null);
		return uid;
	}
	
	public boolean isLoggedIn(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(UID, null);
		long expires_at = settings.getLong(EXPIRES_AT, 0);
		long deadline = System.currentTimeMillis() + EXPIRES_AT_PADDING;
		
		return (uid != null && expires_at != 0 && expires_at > deadline);
	}
	
	public void setLoggedIn(String userName, String uid, String sessionId, String sessionName, long expiresMins){
		
		long expiresAt = System.currentTimeMillis() + (expiresMins * 60 * 1000);
		
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(USER_NAME, userName);
		editor.putString(UID, uid);
		editor.putString(SESSION_ID, sessionId);
		editor.putString(SESSION_NAME, sessionName);
		editor.putLong(EXPIRES_AT, expiresAt);
		editor.commit();
	}
	
	public void clear(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.remove(USER_NAME);
		editor.remove(UID);
		editor.remove(SESSION_ID);
		editor.remove(SESSION_NAME);
		editor.remove(EXPIRES_AT);
		editor.commit();
	}
}
