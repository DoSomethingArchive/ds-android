package org.dosomething.android.receivers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.xtify.sdk.api.XtifyBroadcastReceiver;

/**
 * XtifyNotifier - handles notifications received from Xtify
 * 
 * XtifyBroadcastReceiver is a subclass of android.content.BroadcastReceiver. The
 * abstract onMessage, onRegistered, and onC2dmError methods are called from
 * BroadcastReceiver.onReceive, and have the same lifecycle as a BroadcastReceiver.
 *
 */

public class XtifyNotifier extends XtifyBroadcastReceiver {
	String TAG = XtifyNotifier.class.getName();
	
	// Simple notification title
	private static final String NOTIFICATION_TITLE = "com.xtify.sdk.NOTIFICATION_TITLE";
	// Simple notification content
	private static final String NOTIFICATION_CONTENT = "com.xtify.sdk.NOTIFICATION_CONTENT";
	// The type of the action that was set in TARGET for the SDK to perform. (e.g. com.xtify.sdk.OPEN_URL)
	private static final String NOTIF_ACTION_TYPE = "com.xtify.sdk.NOTIF_ACTION_TYPE";
	// Data related to the action type (e.g. for open URL the data will be website URL)
	private static final String NOTIF_ACTION_DATA = "com.xtify.sdk.NOTIF_ACTION_DATA";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
	}

	@Override
	protected void onMessage(Context context, Bundle msgExtras) {
		Log.v(TAG, "-- XtifyNotifier onMessage");
		Log.v(TAG, "Notification Title: " + msgExtras.getString(NOTIFICATION_TITLE));
		Log.v(TAG, "Notification Content: " + msgExtras.getString(NOTIFICATION_CONTENT));
		Log.v(TAG, "Notif Action Type: " + msgExtras.getString(NOTIF_ACTION_TYPE));
		Log.v(TAG, "Notif Action Data: " + msgExtras.getString(NOTIF_ACTION_DATA));
		Log.v(TAG, "Test Payload 1: " + msgExtras.getString("key1"));
		Log.v(TAG, "Test Payload 2: " + msgExtras.getString("key2"));
		
		// more extras
		Log.v(TAG, "NOTIFICATION_DETAILS: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_DETAILS"));
		Log.v(TAG, "NOTIFICATION_URL: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_URL"));
		Log.v(TAG, "NOTIFICATION_CP_ID: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_CP_ID"));
		Log.v(TAG, "NOTIFICATION_TICKER: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_TICKER"));
		Log.v(TAG, "NOTIFICATION_ACTION_COMPONENT: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_ACTION_COMPONENT"));
		Log.v(TAG, "NOTIFICATION_ACTION_LABEL: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_ACTION_LABEL"));
		Log.v(TAG, "NOTIFICATION_GROUP_ID: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_GROUP_ID"));
		Log.v(TAG, "NOTIFICATION_ACTION_NAME: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_ACTION_NAME"));
		Log.v(TAG, "NOTIFICATION_ACTION_CATEGORIES: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_ACTION_CATEGORIES"));
		Log.v(TAG, "NOTIFICATION_SHOW_THUMBS: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_SHOW_THUMBS"));
		Log.v(TAG, "NOTIFICATION_LOCATION_LATITUDE: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_LOCATION_LATITUDE"));
		Log.v(TAG, "NOTIFICATION_LOCATION_LONGITUDE: " + msgExtras.getString("com.xtify.sdk.NOTIFICATION_LOCATION_LONGITUDE"));
	}

	@Override
	protected void onRegistered(Context context) {
		Log.v(TAG, "Xtify SDK Registered");
	}
	
	@Override
	// TODO: Is this going to get deprecated with the switch to GCM?
	protected void onC2dmError(Context context, String errorId) {
		// TODO Auto-generated method stub
		Log.v(TAG, "C2DM Error: " + errorId);
	}

}
