package org.dosomething.android.context;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class UserContext {
	
	private static final String MY_PREFS = "my_prefs";
	private static final String ID = "user_id";
	
	private final Context context;
	
	public UserContext(Context context){
		this.context = context;
	}
	
	public boolean isLoggedIn(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		long id = settings.getLong(ID, -1);
		return id != -1;
	}
	
	public void setLoggedIn(Long id){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.putLong(ID, id);
		editor.commit();
	}
	
	public void clear(){
		SharedPreferences settings = context.getSharedPreferences(MY_PREFS, 0);
		Editor editor = settings.edit();
		editor.remove(ID);
		editor.commit();
	}
}
