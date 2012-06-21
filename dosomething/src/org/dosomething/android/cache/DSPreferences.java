package org.dosomething.android.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class DSPreferences {
	
	private static final String DS_PREFS = "ds_prefs";
	private static final String HAS_RUN = "has_run";
	private static final String CAUSE_1 = "cause_1";
	private static final String CAUSE_2 = "cause_2";
	private static final String CAUSE_3 = "cause_3";
	
	private final Context context;
	
	public DSPreferences(Context context){
		this.context = context;
	}
	
	public void setHasRun() {
		SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
		Editor editor = settings.edit();
		editor.putBoolean(HAS_RUN, true);
		editor.commit();
	}
	
	public boolean getHasRun() {
		SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
		return settings.getBoolean(HAS_RUN, false);
	}
	
	public void setCause(int cause_id) {
		SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
		int c1 = settings.getInt(CAUSE_1, -1);
		int c2 = settings.getInt(CAUSE_2, -1);
		int c3 = settings.getInt(CAUSE_3, -1);
		
		if (cause_id == c1 || cause_id == c2 || cause_id == c3)
			return;
		else {
			Editor editor = settings.edit();
			if (c1 == -1)
				editor.putInt(CAUSE_1, cause_id);
			else if (c2 == -1)
				editor.putInt(CAUSE_2, cause_id);
			else if (c3 == -1)
				editor.putInt(CAUSE_3, cause_id);
			
			editor.commit();
		}
	}
	
	public void unsetCause(int cause_id) {
		SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
		Editor editor = settings.edit();
		
		if (cause_id == settings.getInt(CAUSE_1, -1))
			editor.remove(CAUSE_1);
		else if (cause_id == settings.getInt(CAUSE_2, -1))
			editor.remove(CAUSE_2);
		else if (cause_id == settings.getInt(CAUSE_3, -1))
			editor.remove(CAUSE_3);
		
		editor.commit();
	}
	
	public int[] getCauses() {
		SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
		int[] ids = new int[3];
		
		ids[0] = settings.getInt(CAUSE_1, -1);
		ids[1] = settings.getInt(CAUSE_2, -1);
		ids[2] = settings.getInt(CAUSE_3, -1);
		
		return ids;
	}
}
