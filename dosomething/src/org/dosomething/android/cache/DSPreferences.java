package org.dosomething.android.cache;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.inject.Inject;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DSPreferences {

    private static final String DS_PREFS = "ds_prefs";
    private static final String HAS_RUN = "has_run";
    private static final String CAMPAIGNS = "campaigns";
    private static final String CAUSE_1 = "cause_1";
    private static final String CAUSE_2 = "cause_2";
    private static final String CAUSE_3 = "cause_3";
    private static final String LAST_SURVEY_ID = "last_survey_id";
    private static final String STEP_REMINDERS = "campaign_step_reminders";

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

    /**
     * Retrieve the saved campaigns as a JSONObject.
     *
     * @return JSONObject of the campaigns
     * @throws JSONException
     */
    public JSONObject getCampaigns() throws JSONException {
        JSONObject answer = null;
        SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
        String json = settings.getString(CAMPAIGNS, null);

        if(json != null){
            answer = new JSONObject(json);
        }

        return answer;
    }

    /**
     * Retrieve the saved campaigns as a List.
     *
     * @return the saved campaign data as a List of Campaign objects
     * @throws JSONException
     * @throws ParseException
     */
    public List<Campaign> getCampaignsAsList() throws JSONException, ParseException {

        JSONObject jsonCampaigns = getCampaigns();

        if (jsonCampaigns == null) {
            return null;
        }

        JSONArray names = jsonCampaigns.names();

        List<Campaign> campaigns = new ArrayList<Campaign>();

        for(int i = 0; i < names.length(); i++){
            String name = names.getString(i);
            JSONObject jsonCampaign = jsonCampaigns.getJSONObject(name);
            campaigns.add(new Campaign(name, jsonCampaign));
        }

        Collections.sort(campaigns, new Comparator<Campaign>() {
            @Override
            public int compare(Campaign lhs, Campaign rhs) {
                return lhs.getOrder() - rhs.getOrder();
            }
        });

        return campaigns;
    }

    /**
     * Saves the campaign data to the SharePreferences.
     *
     * @param json the campaign data to save
     */
    public void setCampaigns(JSONObject json){
        SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
        Editor editor = settings.edit();
        editor.putString(CAMPAIGNS, json.toString());
        editor.commit();
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

    /**
     * Remove a campaign step reminder by modifying the JSON string that logs the scheduled campaign
     * step reminders.
     *
     * @param campaignId unique campaign id
     * @param campaignStep name of the campaign step
     */
    public void clearStepReminder(String campaignId, String campaignStep) {
        if (isStepReminderSet(campaignId, campaignStep)) {
            SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
            String reminders = settings.getString(STEP_REMINDERS, "{}");

            try {
                JSONObject jsonReminders = new JSONObject(reminders);
                JSONObject jsonSteps = jsonReminders.optJSONObject(campaignId);
                if (jsonSteps != null) {
                    // Remove the step from the list of reminders for the campaign
                    jsonSteps.remove(campaignStep);

                    // If the campaign has no more reminder steps, then remove the campaign
                    if (jsonSteps.length() == 0) {
                        jsonReminders.remove(campaignId);
                    }
                    // Otherwise, update the campaign reminders with the remaining steps
                    else {
                        jsonReminders.put(campaignId, jsonSteps);
                    }

                    reminders = jsonReminders.toString();

                    // Commit the updates to SharedPreferences
                    Editor editor = settings.edit();
                    editor.putString(STEP_REMINDERS, reminders);
                    editor.commit();
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Add or update a campaign step reminder by modifying the JSON string that logs the scheduled
     * campaign step reminders.
     *
     * @param campaignId unique campaign id
     * @param campaignStep name of the campaign step
     * @param reminderTime time in milliseconds the reminder is set for
     */
    public void setStepReminder(String campaignId, String campaignStep, long reminderTime) {
        SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
        String reminders = settings.getString(STEP_REMINDERS, "{}");

        try {
            JSONObject jsonReminders = new JSONObject(reminders);
            JSONObject jsonSteps = jsonReminders.optJSONObject(campaignId);
            if (jsonSteps == null) {
                jsonSteps = new JSONObject();
            }

            jsonSteps.put(campaignStep, reminderTime);
            jsonReminders.put(campaignId, jsonSteps);

            reminders = jsonReminders.toString();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        Editor editor = settings.edit();
        editor.putString(STEP_REMINDERS, reminders);
        editor.commit();
    }

    /**
     * Get the time a reminder is set for.
     *
     * @param campaignId unique campaign id
     * @param campaignStep name of the campaign step
     * @return time in milliseconds that the reminder is set for
     */
    public long getStepReminder(String campaignId, String campaignStep) {
        SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
        String reminders = settings.getString(STEP_REMINDERS, "{}");
        long reminderTime = -1;

        try {
            JSONObject jsonReminders = new JSONObject(reminders);
            JSONObject jsonSteps = jsonReminders.optJSONObject(campaignId);
            if (jsonSteps != null) {
                reminderTime = jsonSteps.optLong(campaignStep, -1);
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return reminderTime;
    }

    /**
     * Check whether or not a reminder is set for the given campaign step.
     *
     * @param campaignId unique campaign id
     * @param campaignStep name of the campaign step
     * @return true if a reminder is set for this step, otherwise false
     */
    public boolean isStepReminderSet(String campaignId, String campaignStep) {
        SharedPreferences settings = context.getSharedPreferences(DS_PREFS, 0);
        String reminders = settings.getString(STEP_REMINDERS, "{}");

        try {
            JSONObject jsonReminders = new JSONObject(reminders);
            JSONObject jsonSteps = jsonReminders.optJSONObject(campaignId);
            if (jsonSteps != null) {
                boolean isStepSet = !jsonSteps.isNull(campaignStep);
                return isStepSet;
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        return false;
    }
}
