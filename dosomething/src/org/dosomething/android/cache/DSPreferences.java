package org.dosomething.android.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.inject.Inject;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;

public class DSPreferences {
	
	private static final String DS_PREFS = "ds_prefs";
	private static final String HAS_RUN = "has_run";
	private static final String CAUSE_1 = "cause_1";
	private static final String CAUSE_2 = "cause_2";
	private static final String CAUSE_3 = "cause_3";
    private static final String LAST_SURVEY_ID = "last_survey_id";
	
	private final Context context;
	
	@Inject
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
	
	public int getCause1() {
		SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
		return settings.getInt(CAUSE_1, -1);
	}
	
	public int getCause2() {
		SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
		return settings.getInt(CAUSE_2, -1);
	}
	
	public int getCause3() {
		SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
		return settings.getInt(CAUSE_3, -1);
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
			return DSConstants.CAUSE_TAG.ANIMALS.getValue();
		case R.id.bullying_violence_btn:
			return DSConstants.CAUSE_TAG.BULLYING.getValue();
		case R.id.disasters_btn:
			return DSConstants.CAUSE_TAG.DISASTERS.getValue();
		case R.id.discrimination_btn:
			return DSConstants.CAUSE_TAG.DISCRIMINATION.getValue();
		case R.id.education_btn:
			return DSConstants.CAUSE_TAG.EDUCATION.getValue();
		case R.id.environment_btn:
			return DSConstants.CAUSE_TAG.ENVIRONMENT.getValue();
		case R.id.homelessness_poverty_btn:
			return DSConstants.CAUSE_TAG.POVERTY.getValue();
		case R.id.human_rights_btn:
			return DSConstants.CAUSE_TAG.HUMAN_RIGHTS.getValue();
		case R.id.our_troops_btn:
			return DSConstants.CAUSE_TAG.TROOPS.getValue();
		case R.id.health_btn:
			return DSConstants.CAUSE_TAG.HEALTH.getValue();
		case R.id.sex_relationships_btn:
			return DSConstants.CAUSE_TAG.RELATIONSHIPS.getValue();
		default:
			return -1;
		}
	}
	
	/**
	 * Translates a cause's Campaigns or Action Finder feed id to the app's 
	 * corresponding internal resource id.
	 */
	public int getCauseResId(int feed_id) {
		if (feed_id == DSConstants.CAUSE_TAG.ANIMALS.getValue())
			return R.id.animals_btn;
		else if (feed_id == DSConstants.CAUSE_TAG.BULLYING.getValue())
			return R.id.bullying_violence_btn;
		else if (feed_id == DSConstants.CAUSE_TAG.DISASTERS.getValue())
			return R.id.disasters_btn;
		else if (feed_id == DSConstants.CAUSE_TAG.DISCRIMINATION.getValue())
			return R.id.discrimination_btn;
		else if (feed_id == DSConstants.CAUSE_TAG.EDUCATION.getValue())
			return R.id.education_btn;
		else if (feed_id == DSConstants.CAUSE_TAG.ENVIRONMENT.getValue())
			return R.id.environment_btn;
		else if (feed_id == DSConstants.CAUSE_TAG.POVERTY.getValue())
			return R.id.homelessness_poverty_btn;
		else if (feed_id == DSConstants.CAUSE_TAG.HUMAN_RIGHTS.getValue())
			return R.id.human_rights_btn;
		else if (feed_id == DSConstants.CAUSE_TAG.TROOPS.getValue())
			return R.id.our_troops_btn;
		else if (feed_id == DSConstants.CAUSE_TAG.HEALTH.getValue())
			return R.id.health_btn;
		else if (feed_id == DSConstants.CAUSE_TAG.RELATIONSHIPS.getValue())
			return R.id.sex_relationships_btn;
		else
			return -1;
	}
	
	public int getCauseDrawableByFeedId(int feed_id) {
		if (feed_id == DSConstants.CAUSE_TAG.ANIMALS.getValue())
			return R.drawable.cause_animals;
		else if (feed_id == DSConstants.CAUSE_TAG.BULLYING.getValue())
			return R.drawable.cause_bullying;
		else if (feed_id == DSConstants.CAUSE_TAG.DISASTERS.getValue())
			return R.drawable.cause_disasters;
		else if (feed_id == DSConstants.CAUSE_TAG.DISCRIMINATION.getValue())
			return R.drawable.cause_discrimination;
		else if (feed_id == DSConstants.CAUSE_TAG.EDUCATION.getValue())
			return R.drawable.cause_education;
		else if (feed_id == DSConstants.CAUSE_TAG.ENVIRONMENT.getValue())
			return R.drawable.cause_environment;
		else if (feed_id == DSConstants.CAUSE_TAG.POVERTY.getValue())
			return R.drawable.cause_poverty;
		else if (feed_id == DSConstants.CAUSE_TAG.HUMAN_RIGHTS.getValue())
			return R.drawable.cause_human_rights;
		else if (feed_id == DSConstants.CAUSE_TAG.TROOPS.getValue())
			return R.drawable.cause_troops;
		else if (feed_id == DSConstants.CAUSE_TAG.HEALTH.getValue())
			return R.drawable.cause_health;
		else if (feed_id == DSConstants.CAUSE_TAG.RELATIONSHIPS.getValue())
			return R.drawable.cause_relationships;
		else
			return -1;
	}

    /**
     * Retrieves the id of the last survey the user opened up.
     *
     * @return  the id of the last survey opened
     */
    public int getLastSurveyId() {
        SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
        return settings.getInt(LAST_SURVEY_ID, 0);
    }

    /**
     * Saves the id of the last survey the user opened up.
     *
     * @param   id  the survey id
     */
    public void setLastSurveyId(int id) {
        SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
        Editor editor = settings.edit();

        editor.putInt(LAST_SURVEY_ID, id);

        editor.commit();
    }
}
