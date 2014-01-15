package org.dosomething.android.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import org.dosomething.android.R;

/**
 * Activity to display Campaign reminders.
 */
public class RemindersActivity extends AbstractActionBarActivity {

    @Override
    public String getPageName() {
        return "reminders";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminders_activity);

        // Enable home button on ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If home button is selected on ActionBar, then end the activity
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Intent for other activities to use to open this activity.
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, RemindersActivity.class);
    }
}
