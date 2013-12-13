package org.dosomething.android.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.inject.Inject;

import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.transfer.Campaign;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DSDao {

    private final String SIGNED_UP_CAMPAIGNS_FILENAME = "SignedUpCampaignsCache";
    private final Context context;

    @Inject
    public DSDao(Context context){
        this.context = context;
    }

    /**
     * Retrieve the UserCampaign info from the database. A row will only be found if the user
     * has signed up for the campaign.
     *
     * @param uid User's UID
     * @param campaignId Campaign ID for the campaign
     * @return UserCampaign object if a matching campaign was found. Otherwise, null.
     */
    public UserCampaign findUserCampaign(String uid, String campaignId) {
        UserCampaign answer = null;

        SQLHelper sql = new SQLHelper(context);

        Cursor c = sql.getReadableDatabase().query(
                UserCampaign.TABLE_NAME,
                UserCampaign.getAllColumns(),
                UserCampaign.COL_UID + "=? and " + UserCampaign.COL_CAMPAIGN_ID + "=?",
                new String[] {uid, campaignId},
                null,
                null,
                null
        );

        if(c.moveToFirst()){
            answer = new UserCampaign(c);
        }

        c.close();
        sql.close();

        return answer;
    }

    /**
     * Retrieves a List of UserCampaigns that the user has signed up for.
     *
     * @param uid User ID to retrieve campaigns for
     * @param bCompletedCampaigns True to return campaigns that are completed, False to return
     *                            campaigns in progress.
     * @return List of UserCampaigns.
     */
    public List<UserCampaign> findUserCampaigns(String uid, boolean bCompletedCampaigns) {
        List<UserCampaign> answer = new ArrayList<UserCampaign>();

        SQLHelper sql = new SQLHelper(context);

        Cursor c = sql.getReadableDatabase().query(
                UserCampaign.TABLE_NAME,
                UserCampaign.getAllColumns(),
                UserCampaign.COL_UID + "=?",
                new String[] {uid},
                null,
                null,
                null
        );

        while (c.moveToNext()) {
            UserCampaign uc = new UserCampaign(c);
            if ((bCompletedCampaigns && uc.getDateCompleted() > 0) ||
                    (!bCompletedCampaigns && uc.getDateCompleted() == 0)) {
                answer.add(uc);
            }
        }

        c.close();
        sql.close();

        return answer;
    }

    /**
     * Get a list of steps completed for a given user and campaign.
     *
     * @param uid User's UID
     * @param campaignId ID for the campaign
     * @return Array of ints indicating the step numbers that have been completed
     */
    public int[] getCampaignStepsCompleted(String uid, String campaignId) {
        int[] results = null;

        SQLHelper sql = new SQLHelper(context);
        SQLiteDatabase readableDb = sql.getReadableDatabase();

        Cursor c = readableDb.query(
                UserCampaign.TABLE_NAME,
                new String[] {UserCampaign.COL_COMPLETED_STEPS},
                UserCampaign.COL_UID + "=? and " + UserCampaign.COL_CAMPAIGN_ID + "=?",
                new String[] {uid, campaignId},
                null,
                null,
                null
        );

        if (c.moveToFirst()) {
            String strSteps = c.getString(0);
            if (strSteps != null) {
                try {
                    JSONObject jsonSteps = new JSONObject(c.getString(0));
                    if (jsonSteps.length() > 0) {
                        results = new int[jsonSteps.length()];

                        int i = 0;
                        Iterator<?> keys = jsonSteps.keys();
                        while (keys.hasNext()) {
                            String key = (String)keys.next();
                            results[i++] = Integer.parseInt(key);
                        }
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        readableDb.close();
        sql.close();

        return results;
    }

    /**
     * Check if a campaign step is completed for the given step number, user, and campaign.
     *
     * @param uid User's UID
     * @param campaignId ID for the campaign
     * @param stepNumber Step number to check if completed
     * @return true if step is complete, otherwise false
     */
    public boolean isCampaignStepComplete(String uid, String campaignId, int stepNumber) {
        int isCompleted = 0;

        SQLHelper sql = new SQLHelper(context);
        SQLiteDatabase readableDb = sql.getReadableDatabase();

        Cursor c = readableDb.query(
                UserCampaign.TABLE_NAME,
                new String[]{UserCampaign.COL_COMPLETED_STEPS},
                UserCampaign.COL_UID + "=? and " + UserCampaign.COL_CAMPAIGN_ID + "=?",
                new String[]{uid, campaignId},
                null,
                null,
                null
        );

        if (c.moveToFirst()) {
            String completedSteps = c.getString(0);
            if (completedSteps != null) {
                try {
                    JSONObject jsonSteps = new JSONObject(completedSteps);
                    isCompleted = jsonSteps.optInt(Integer.toString(stepNumber), 0);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        readableDb.close();
        sql.close();

        return isCompleted == 1 ? true : false;
    }

    /**
     * Check if a given user has signed up for a given campaign.
     *
     * @param uid User's UID
     * @param campaignId Campaign ID for the campaign
     * @return boolean true if user did sign up for this campaign. Otherwise, false.
     */
    public boolean isSignedUpForCampaign(String uid, String campaignId) {
        if (findUserCampaign(uid, campaignId) != null) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Clear the list of completed steps for a user in a given campaign.
     *
     * @param uid User's UID
     * @param campaignId ID for the campaign
     */
    public void removeCompletedSteps(String uid, String campaignId) {
        SQLHelper sql = new SQLHelper(context);
        SQLiteDatabase writableDb = sql.getWritableDatabase();

        // Replace existing column value with an empty json object
        ContentValues updateValues = new ContentValues();
        updateValues.put(UserCampaign.COL_COMPLETED_STEPS, "{}");

        int rowsUpdated = writableDb.update(
                UserCampaign.TABLE_NAME,
                updateValues,
                UserCampaign.COL_UID + "=? and " + UserCampaign.COL_CAMPAIGN_ID + "=?",
                new String[] {uid, campaignId}
        );

        writableDb.close();
        sql.close();
    }

    /**
     * Deletes a user's sign up for a campaign from the table.
     *
     * @param uid User's UID
     * @param campaignId Campaign ID for the campaign
     * @return int > 0 if successful. Otherwise, 0.
     */
    public int removeSignUp(String uid, String campaignId) {
        SQLHelper sql = new SQLHelper(context);
        SQLiteDatabase db = sql.getWritableDatabase();

        int rowsRemoved = db.delete(
                UserCampaign.TABLE_NAME,
                UserCampaign.COL_UID + "=? and " + UserCampaign.COL_CAMPAIGN_ID + "=?",
                new String[] {uid, campaignId}
        );

        db.close();
        sql.close();

        return rowsRemoved;
    }

    /**
     * Update database with the step completed for the given user and campaign.
     *
     * @param uid User's UID
     * @param campaignId ID for the campaign
     * @param stepNumber Step number being completed
     */
    public void setCampaignStepCompleted(String uid, String campaignId, int stepNumber) {
        SQLHelper sql = new SQLHelper(context);

        SQLiteDatabase readableDb = sql.getReadableDatabase();

        // Get current list of steps completed
        Cursor c = readableDb.query(
                UserCampaign.TABLE_NAME,
                new String[] {UserCampaign.COL_COMPLETED_STEPS},
                UserCampaign.COL_UID + "=? and " + UserCampaign.COL_CAMPAIGN_ID + "=?",
                new String[] {uid, campaignId},
                null,
                null,
                null
        );

        if (c.moveToFirst()) {
            // Cursor only contains the completed steps column
            String completedSteps = c.getString(0);

            // If no steps found, create an empty JSON object
            if (completedSteps == null) {
                completedSteps = "{}";
            }

            readableDb.close();

            try {
                JSONObject jsonSteps = new JSONObject(completedSteps);

                // Update list of steps completed
                jsonSteps.put(Integer.toString(stepNumber), 1);

                // Update database with updated list
                SQLiteDatabase writableDb = sql.getWritableDatabase();

                ContentValues updateValues = new ContentValues();
                updateValues.put(UserCampaign.COL_COMPLETED_STEPS, jsonSteps.toString());

                int rowsUpdated = writableDb.update(
                        UserCampaign.TABLE_NAME,
                        updateValues,
                        UserCampaign.COL_UID + "=? and " + UserCampaign.COL_CAMPAIGN_ID + "=?",
                        new String[] {uid, campaignId}
                );

                writableDb.close();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        }

        sql.close();
    }

    /**
     * Inserts a row into the database, or finds the already existing row, to indicate that this
     * user has signed up for the given campaign.
     *
     * @param userCampaign UserCampaign object of the campaign being signed up for
     * @return Key id of the campaign's row in the db table
     */
    public Long setSignedUp(UserCampaign userCampaign) {
        SQLHelper sql = new SQLHelper(context);
        SQLiteDatabase db = sql.getWritableDatabase();

        String uid = userCampaign.getUid();
        String campaignId = userCampaign.getCampaignId();

        // Find out if this campaign's already been added to the table -
        // indicating it's already been signed up for.
        Cursor c = db.query(
                UserCampaign.TABLE_NAME,
                UserCampaign.getAllColumns(),
                UserCampaign.COL_UID + "=? and " + UserCampaign.COL_CAMPAIGN_ID + "=?",
                new String[] {uid, campaignId},
                null,
                null,
                null,
                "1"
        );

        Long keyId;
        // If campaign's already signed up for, just return that pre-existing key id
        if (c != null && c.moveToFirst()) {
            UserCampaign uc = new UserCampaign(c);
            keyId = uc.getId();
        }
        // Otherwise, insert the campaign into the table
        else {
            keyId = db.insertOrThrow(UserCampaign.TABLE_NAME, null, userCampaign.getContentValues());
        }

        c.close();
        sql.close();

        return keyId;
    }

    /**
     * Read private file of campaign data a user has signed up for
     *
     * @param uid User's uid we're retrieving data for
     * @return Cached campaign data as a JSONObject, or null if none is found
     */
    public List<Campaign> fetchSignedUpCampaignData(String uid) {
        String filename = SIGNED_UP_CAMPAIGNS_FILENAME + "-" + uid;
        String fileContent = "";
        ArrayList<Campaign> campaigns = new ArrayList<Campaign>();

        try {
            FileInputStream fis = context.openFileInput(filename);
            if (fis != null) {
                BufferedInputStream bis = new BufferedInputStream(fis);
                if (bis != null) {
                    byte[] buffer = new byte[1024];

                    int bytesRead = 0;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        String chunk = new String(buffer, 0, bytesRead);
                        fileContent += chunk;
                    }
                }
            }
        }
        catch (Exception e) {
            return campaigns;
        }

        if (fileContent.length() > 0) {
            try {
                JSONObject jsonContent = new JSONObject(fileContent);
                for (Iterator<?> iter = jsonContent.keys(); iter.hasNext();) {
                    String key = (String)iter.next();
                    Object campaignObj = jsonContent.get(key);
                    // Ensure that item is the top-level "campaign" JSONObject before adding to returned Campaign list
                    if (campaignObj instanceof JSONObject) {
                        JSONObject jsonCampaignObj = (JSONObject)campaignObj;
                        if (jsonCampaignObj.has("campaign")) {
                            Campaign addCampaign = null;
                            try {
                                addCampaign = new Campaign(key, (JSONObject)jsonContent.get(key));
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                                iter.remove();
                            }

                            if (addCampaign != null) {
                                campaigns.add(addCampaign);
                            }
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return campaigns;
    }

    /**
     * Write campaign data for campaigns that a user's signed up for to a private
     * file for future use - particularly in case the campaign data gets removed
     * from the server later.
     *
     * @param uid User's uid we're writing data for
     * @param campaign Campaign data being saved
     */
    public void saveSignedUpCampaignData(String uid, Campaign campaign) throws FileNotFoundException, IOException, JSONException {
        String filename = SIGNED_UP_CAMPAIGNS_FILENAME + "-" + uid;

        // Get current contents of the file
        String savedContent = "";
        try {
            FileInputStream fis = context.openFileInput(filename);
            if (fis != null) {
                BufferedInputStream bis = new BufferedInputStream(fis);
                if (bis != null) {
                    byte[] buffer = new byte[1024];

                    int bytesRead = 0;
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        String chunk = new String(buffer, 0, bytesRead);
                        savedContent += chunk;
                    }
                }
            }
        }
        // No worries if the read fails. Just continue and re-write the campaign data.
        catch (Exception e) {
            e.printStackTrace();
        }

        String newContent = "";

        // Convert current content string to a JSONObject
        String campaignKey = campaign.getId();
        if (savedContent.length() > 0) {
            JSONObject jsonSaved = new JSONObject(savedContent);
            // Overwrite campaign content if it's been previously saved
            if (jsonSaved.has(campaignKey)) {
                jsonSaved.remove(campaignKey);
            }

            jsonSaved.put(campaignKey, campaign.toJSON().getJSONObject(campaignKey));
            newContent = jsonSaved.toString();
        }
        else {
            newContent = campaign.toJSON().getJSONObject(campaignKey).toString();
        }

        FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
        if (fos != null) {
            fos.write(newContent.getBytes());
            fos.close();
        }
    }

}
