package org.dosomething.android.analytics;

import java.util.HashMap;
import java.util.Map;

import org.dosomething.android.DSConstants;
import org.dosomething.android.transfer.Campaign;

import android.app.Activity;
import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.google.analytics.tracking.android.EasyTracker;

@SuppressWarnings("unused")
public class Analytics {

	public static void startSession(Activity activity) {
		if (!DSConstants.inProduction)
			return;

		// Flurry Analytics
		FlurryAgent.onStartSession(activity, DSConstants.FLURRY_API_KEY);
		
		// Google Analytics
		EasyTracker.getInstance().activityStart(activity);
	}
	
	public static void endSession(Activity activity) {
		if (!DSConstants.inProduction)
			return;

		// Flurry Analytics
		FlurryAgent.onEndSession(activity);
		
		// Google Analytics
		EasyTracker.getInstance().activityStop(activity);
	}
	
	public static void logPageView(Context context, String pageName) {
		if (!DSConstants.inProduction)
			return;

		// Flurry Analytics
		logEvent("page-" + pageName, new HashMap<String, String>());
		FlurryAgent.onPageView();
		
		// Google Analytics
		EasyTracker.getTracker().sendView(pageName);
	}
	
	public static void logCampaignPageView(Context context, String pageName, Campaign campaign) {
		if (!DSConstants.inProduction)
			return;

		// Flurry Analytics
		HashMap<String, String> param = new HashMap<String, String>();
		if (campaign != null) {
			param.put("campaign-name", campaign.getName());
		}
		logEvent("page-" + pageName, param);
		FlurryAgent.onPageView();
		
		// Google Analytics
		EasyTracker.getTracker().sendView(pageName + " - " + campaign.getName());
	}
	
	public static void logEvent(String eventId, Map<String,String> parameters) {
		if (!DSConstants.inProduction)
			return;

		// Flurry Analytics event tracking
		FlurryAgent.logEvent(eventId, parameters);
	}
	
	public static void logEvent(String category, String action, String label) {
		if (!DSConstants.inProduction)
			return;

		// Google Analytics event tracking
		EasyTracker.getTracker().sendEvent(category, action, label, 1L);
	}
	
	public static void logEvent(String category, String action, String label, Long value) {
		if (!DSConstants.inProduction)
			return;

		// Google Analytics event tracking
		EasyTracker.getTracker().sendEvent(category, action, label, value);
	}
}