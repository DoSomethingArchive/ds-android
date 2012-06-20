package org.dosomething.android.activities;

import org.dosomething.android.R;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class Welcome extends AbstractActivity {

	private static final String DS_PREFS = "ds_prefs";
	private static final String HAS_RUN = "has_run";
	
	private Dialog splashDialog;
	private boolean skipWelcome = true;
	
	@Override
	protected String getPageName() {
		return "welcome";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome);
		
		MyModel model = (MyModel) getLastNonConfigurationInstance();
        if (model != null) {
	        if (model.isShowSplashScreen()) {
	            showSplashScreen();
	        }
	    } else {
	    	if (getIntent().hasCategory("android.intent.category.LAUNCHER")) {
	    		showSplashScreen();
	    	}
	    }
        
        if (isInitialLaunch()) {
        	// Set boolean to indicate that this app has now been run
        	setRanInitialLaunch();
        	skipWelcome = false;
        }
        else {
        	// On subsequent app executions, just send user to Profile page
        	skipWelcome = true;
        }
	}
	
	public void continueToCauses(View v) {
		startActivity(new Intent(this, CauseSelector.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
		finish();
	}
	
	private boolean isInitialLaunch() {
		SharedPreferences settings = this.getSharedPreferences(DS_PREFS, 0);
		boolean bHasRun = settings.getBoolean(HAS_RUN, false);
		return !bHasRun;
	}
	
	private void setRanInitialLaunch() {
		SharedPreferences settings = this.getSharedPreferences(DS_PREFS, 0);
		Editor editor = settings.edit();
		editor.putBoolean(HAS_RUN, true);
		editor.commit();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    MyModel model = new MyModel();
	    
	    if (splashDialog != null) {
	        model.setShowSplashScreen(true);
	        removeSplashScreen();
	    }
	    return model;
	}
	
	/**
	 * Removes the Dialog that displays the splash screen
	 */
	protected void removeSplashScreen() {
		if (skipWelcome) 
			startActivity(new Intent(this, Profile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
	    
	    if (splashDialog != null) {
	    	splashDialog.dismiss();
	    	splashDialog = null;
	    }
	    
	    if (skipWelcome)
	    	finish();
	}
	 
	/**
	 * Shows the splash screen over the full Activity
	 */
	protected void showSplashScreen() {
		splashDialog = new Dialog(this, R.style.SplashScreen);
		splashDialog.setContentView(R.layout.splash_screen);
		splashDialog.setCancelable(false);
		splashDialog.show();
	 
	    // Set Runnable to remove splash screen just in case
	    final Handler handler = new Handler();
	    handler.postDelayed(new Runnable() {
	      @Override
	      public void run() {
	        removeSplashScreen();
	      }
	    }, 3000);
	}
	
	/**
	 * Simple class for storing important data across config changes
	 */
	private class MyModel {
	    private boolean showSplashScreen = false;

		public boolean isShowSplashScreen() {
			return showSplashScreen;
		}
		public void setShowSplashScreen(boolean showSplashScreen) {
			this.showSplashScreen = showSplashScreen;
		}
	}

}
