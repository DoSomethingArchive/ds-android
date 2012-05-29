package org.dosomething.android.analytics;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.flurry.android.FlurryAgent;

public class Analytics {

	public static void startSession(Context context){
		FlurryAgent.onStartSession(context, "P4R2NTE9XSRAHB9IR9M2");
	}
	
	public static void endSession(Context context){
		FlurryAgent.onEndSession(context);
	}
	
	public static void logPageView(Context context, String pageName){
		logEvent("page-" + pageName, new HashMap<String, String>());
	}
	
	public static void logEvent(String eventId, Map<String,String> parameters){
		FlurryAgent.logEvent(eventId, parameters);
	}
	
}
