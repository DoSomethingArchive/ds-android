package org.dosomething.android.dao;

import java.util.ArrayList;
import java.util.List;

import org.dosomething.android.domain.CompletedCampaignAction;
import org.dosomething.android.domain.UserCampaign;

import com.google.inject.Inject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class MyDAO {
	
	private final Context context;
	
	@Inject
	public MyDAO(Context context){
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
			answer.add(new UserCampaign(c));
		}
		
		c.close();
		sql.close();
		
		return answer;
	}
	
	public void setSignedUp(String uid, String campaignId){
		ContentValues cv = new ContentValues();
		cv.put("uid", uid);
		cv.put("campaign_id", campaignId);
		
		SQLHelper sql = new SQLHelper(context);
        
        sql.getWritableDatabase().insertOrThrow("user_campaign", null, cv);
         
        sql.close();
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
				answer.add(new CompletedCampaignAction(cursor));
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
