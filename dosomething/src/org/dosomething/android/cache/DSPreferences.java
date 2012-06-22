package org.dosomething.android.cache;

import org.dosomething.android.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class DSPreferences {
	
	private static final String DS_PREFS = "ds_prefs";
	private static final String HAS_RUN = "has_run";
	private static final String CAUSE_1 = "cause_1";
	private static final String CAUSE_2 = "cause_2";
	private static final String CAUSE_3 = "cause_3";
	
	public static final int CAUSE_ANIMALS = 29;
	public static final int CAUSE_BULLYING = 28;
	public static final int CAUSE_DISASTERS = 27;
	public static final int CAUSE_DISCRIMINATION = 23;
	public static final int CAUSE_EDUCATION = 25;
	public static final int CAUSE_ENVIRONMENT = 20;
	public static final int CAUSE_POVERTY = 21;
	public static final int CAUSE_HUMAN_RIGHTS = 73;
	public static final int CAUSE_TROOPS = 24;
	public static final int CAUSE_HEALTH = 26;
	public static final int CAUSE_RELATIONSHIPS = 22;
	
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
		
		int fid = getCauseFeedId(cause_id);
		if (fid == c1 || fid == c2 || fid == c3)
			return;
		else {
			Editor editor = settings.edit();
			if (c1 == -1)
				editor.putInt(CAUSE_1, fid);
			else if (c2 == -1)
				editor.putInt(CAUSE_2, fid);
			else if (c3 == -1)
				editor.putInt(CAUSE_3, fid);
			
			editor.commit();
		}
	}
	
	public void unsetCause(int cause_id) {
		SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
		Editor editor = settings.edit();
		
		int fid = getCauseFeedId(cause_id);
		if (fid == settings.getInt(CAUSE_1, -1))
			editor.remove(CAUSE_1);
		else if (fid == settings.getInt(CAUSE_2, -1))
			editor.remove(CAUSE_2);
		else if (fid == settings.getInt(CAUSE_3, -1))
			editor.remove(CAUSE_3);
		
		editor.commit();
	}
	
	public void clearCauses() {
		SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
		Editor editor = settings.edit();
		editor.remove(CAUSE_1);
		editor.remove(CAUSE_2);
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
	
	/**
	 * Translates a cause's internal resource id to its corresponding value from
	 * the Campaigns or Action Finder feed.
	 */
	private int getCauseFeedId(int res_id) {
		switch(res_id) {
		case R.id.animals_btn:
			return CAUSE_ANIMALS;
		case R.id.bullying_violence_btn:
			return CAUSE_BULLYING;
		case R.id.disasters_btn:
			return CAUSE_DISASTERS;
		case R.id.discrimination_btn:
			return CAUSE_DISCRIMINATION;
		case R.id.education_btn:
			return CAUSE_EDUCATION;
		case R.id.environment_btn:
			return CAUSE_ENVIRONMENT;
		case R.id.homelessness_poverty_btn:
			return CAUSE_POVERTY;
		case R.id.human_rights_btn:
			return CAUSE_HUMAN_RIGHTS;
		case R.id.our_troops_btn:
			return CAUSE_TROOPS;
		case R.id.health_btn:
			return CAUSE_HEALTH;
		case R.id.sex_relationships_btn:
			return CAUSE_RELATIONSHIPS;
		default:
			return -1;
		}
	}
}
