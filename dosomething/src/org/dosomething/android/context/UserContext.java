package org.dosomething.android.context;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserContext {
	
	private static final String MY_PREFS = "my_prefs";
	private static final String UID = "user_uid";
	
	private final Context context;
	
	public UserContext(Context context){
		this.context = context;
	}
	
	public boolean isLoggedIn(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		String uid = settings.getString(UID, null);
		return uid != null;
	}
	
	public void setLoggedIn(String uid){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putString(UID, uid);
		editor.commit();
	}
	
	public void clear(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.remove(UID);
		editor.commit();
	}
}
