package org.dosomething.android.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.google.inject.Key;

import org.dosomething.android.DSConstants;
import org.dosomething.android.analytics.Analytics;

import java.util.HashMap;
import java.util.Map;

import roboguice.RoboGuice;
import roboguice.activity.event.OnActivityResultEvent;
import roboguice.activity.event.OnConfigurationChangedEvent;
import roboguice.activity.event.OnContentChangedEvent;
import roboguice.activity.event.OnCreateEvent;
import roboguice.activity.event.OnDestroyEvent;
import roboguice.activity.event.OnNewIntentEvent;
import roboguice.activity.event.OnPauseEvent;
import roboguice.activity.event.OnRestartEvent;
import roboguice.activity.event.OnResumeEvent;
import roboguice.activity.event.OnStartEvent;
import roboguice.activity.event.OnStopEvent;
import roboguice.event.EventManager;
import roboguice.inject.RoboInjector;
import roboguice.util.RoboContext;

/**
 * Abstract base activity that allows us to use the ActionBar and RoboGuice
 * together. RoboGuice 3.0 will have a fix for this. The issue can be followed
 * here (https://github.com/roboguice/roboguice/issues/134) along with the basis
 * for the implementation below. For now we need to implement RoboGuice
 * functionality ourselves in order to also use the ActionBar.
 *
 * RoboSherlockActivity also referenced for this class's implementation:
 * https://github.com/roboguice/roboguice/blob/master/roboguice/src/main/java/roboguice/activity/RoboSherlockActivity.java
 */

public abstract class AbstractActionBarActivity extends ActionBarActivity implements RoboContext {

    protected HashMap<Key<?>,Object> scopedObjects = new HashMap<Key<?>,Object>();

    protected EventManager eventManager;

    protected String getPageName() {
        return "AbstractActionBarActivity";
    }

    protected void onCreate(Bundle savedInstanceState) {
        final RoboInjector injector = RoboGuice.getInjector(this);
        eventManager = injector.getInstance(EventManager.class);
        injector.injectMembersWithoutViews(this);

        super.onCreate(savedInstanceState);

        eventManager.fire(new OnCreateEvent(savedInstanceState));
    }

    public void onStart() {
        super.onStart();
        eventManager.fire(new OnStartEvent());

        Analytics.startSession(this);
    }

    protected void onResume() {
        super.onResume();
        eventManager.fire(new OnResumeEvent());

        // If this is the campaign page, do not log a page view. Allow the Fragments to handle it.
        if (getIntent() != null && getIntent().getExtras() != null) {
            org.dosomething.android.transfer.Campaign campaign = (org.dosomething.android.transfer.Campaign) getIntent().getExtras().get(DSConstants.EXTRAS_KEY.CAMPAIGN.getValue());
            if (campaign != null) {
                return;
            }
        }

        Analytics.logPageView(this, this.getPageName());
    }

    protected void onRestart() {
        super.onRestart();
        eventManager.fire(new OnRestartEvent());
    }

    protected void onPause() {
        super.onPause();
        eventManager.fire(new OnPauseEvent());
    }

    public void onStop() {
        try {
            eventManager.fire(new OnStopEvent());
        }
        finally {
            super.onStop();
        }

        Analytics.endSession(this);
    }

    protected void onDestroy() {
        try {
            eventManager.fire(new OnDestroyEvent());
        } finally {
            try {
                RoboGuice.destroyInjector(this);
            } finally {
                super.onDestroy();
            }
        }
    }

    protected void onNewIntent( Intent intent ) {
        super.onNewIntent(intent);
        eventManager.fire(new OnNewIntentEvent());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        final Configuration currentConfig = getResources().getConfiguration();
        super.onConfigurationChanged(newConfig);
        eventManager.fire(new OnConfigurationChangedEvent(currentConfig, newConfig));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        eventManager.fire(new OnActivityResultEvent(requestCode, resultCode, data));
    }

    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        onContentChanged();
    }

    public void onContentChanged() {
        super.onContentChanged();
        try {
            RoboGuice.getInjector(this).injectViewMembers(this);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        eventManager.fire(new OnContentChangedEvent());
    }

    @Override
    public Map<Key<?>, Object> getScopedObjectMap() {
        return scopedObjects;
    }

}
