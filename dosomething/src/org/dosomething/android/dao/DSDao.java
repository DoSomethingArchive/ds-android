package org.dosomething.android.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.inject.Inject;

import org.dosomething.android.domain.CompletedCampaignAction;
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
	
	public UserCampaign findUserCampaign(String uid, String campaignId){
		UserCampaign answer = null;
		
		SQLHelper sql = new SQLHelper(context);
		
		Cursor c = sql.getReadableDatabase().query("user_campaign", new String[]{"id", "campaign_id", "uid"}, "uid = ? and campaign_id = ?", new String[]{uid, campaignId}, null, null, null);
		if(c.moveToFirst()){
			answer = new UserCampaign(c);
		}
		
		c.close();
		sql.close();
		
		return answer;
	}
	
	public List<UserCampaign> findUserCampaigns(String uid){
		List<UserCampaign> answer = new ArrayList<UserCampaign>();
		
		SQLHelper sql = new SQLHelper(context);
		
		Cursor c = sql.getReadableDatabase().query("user_campaign", new String[]{"id", "campaign_id", "uid"}, "uid = ?", new String[]{uid}, null, null, null);
		while(c.moveToNext()){
			UserCampaign uc = new UserCampaign(c);
			answer.add(uc);
		}
		
		c.close();
		sql.close();
		
		return answer;
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
		Cursor cursor = db.query(
                "user_campaign",
                new String[]{"id"},
                "uid=? and campaign_id=?",
                new String[]{uid, campaignId},
                null,
                null,
                null,
                "1"
        );

        Long keyId;
        // If campaign's already signed up for, just return that pre-existing key id
        if (cursor != null && cursor.moveToFirst()) {
            UserCampaign uc = new UserCampaign(cursor);
            keyId = uc.getId();
        }
        // Otherwise, insert the campaign into the table
        else {
            keyId = db.insertOrThrow(UserCampaign.TABLE_NAME, null, userCampaign.getContentValues());
        }

		cursor.close();
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
	
	public void addCompletedAction(CompletedCampaignAction action){
		removeCompletedAction(action.getUserCampaignId(), action.getActionText());
		
		ContentValues cv = new ContentValues();
		cv.put("user_campaign_id", action.getUserCampaignId());
		cv.put("action_text", action.getActionText());
		
		SQLHelper sql = new SQLHelper(context);
        
        sql.getWritableDatabase().insertOrThrow("completed_campaign_action", null, cv);
         
        sql.close();
	}
	
	public List<CompletedCampaignAction> getCompletedActions(Long userCampaignId){
		List<CompletedCampaignAction> answer = new ArrayList<CompletedCampaignAction>();

		SQLHelper sql = new SQLHelper(context);

		Cursor cursor = sql.getReadableDatabase().query("completed_campaign_action", new String[]{"id", "user_campaign_id", "action_text"}, "user_campaign_id = ?", new String[]{Long.toString(userCampaignId)}, null, null, null);

		if(cursor != null){
			while(cursor.moveToNext()){
				CompletedCampaignAction c = new CompletedCampaignAction(cursor);
				answer.add(c);
			}

			cursor.close();
		}

		sql.close();

		return answer;
	}

	public void removeCompletedAction(Long userCampaignId, String actionText) {
		SQLHelper sql = new SQLHelper(context);
		
		sql.getWritableDatabase().delete("completed_campaign_action", "user_campaign_id = ? and action_text = ?", new String[]{Long.toString(userCampaignId), actionText});
		
		sql.close();
	}
	
}
