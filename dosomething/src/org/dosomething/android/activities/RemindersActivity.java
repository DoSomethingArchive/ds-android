package org.dosomething.android.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.dosomething.android.R;
import org.dosomething.android.cache.DSPreferences;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import roboguice.inject.InjectView;

/**
 * Activity to display Campaign reminders.
 */
public class RemindersActivity extends AbstractActionBarActivity implements AdapterView.OnItemClickListener {

    @Inject @Named("ProximaNova-Bold")Typeface typefaceBold;
    @Inject @Named("ProximaNova-Reg")Typeface typefaceReg;

    // Interface to the SharedPreferences
    @Inject private DSPreferences dsPrefs;

    // For inflating layouts from XML
    @Inject private LayoutInflater inflater;

    // View to display the list of reminders
    @InjectView(R.id.list) private ListView mList;

    // View to display if there are no reminders found
    @InjectView(R.id.empty_view) private View mEmptyView;

    // Header text in the empty view
    @InjectView(R.id.empty_view_header) private TextView mEmptyHeader;

    // Body text in the empty view
    @InjectView(R.id.empty_view_body) private TextView mEmptyBody;

    // List of reminders pulled from DSPreferences
    private ArrayList<Reminder> mListReminders;

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

        // Set the typeface for empty view components
        mEmptyHeader.setTypeface(typefaceBold);
        mEmptyBody.setTypeface(typefaceReg);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get the list of the reminders saved in DSPreferences
        mListReminders = new ArrayList<Reminder>();
        String reminders = dsPrefs.getStepReminderRaw();

        try {
            JSONObject jsonReminders = new JSONObject(reminders);
            if (jsonReminders.length() > 0) {
                Iterator<String> keyIter = jsonReminders.keys();
                while (keyIter.hasNext()) {
                    String campaignId = keyIter.next();
                    JSONObject jsonSteps = jsonReminders.getJSONObject(campaignId);
                    String campaignName = jsonSteps.getString("name");

                    Iterator<String> stepIter = jsonSteps.keys();
                    while (stepIter.hasNext()) {
                        String campaignStep = stepIter.next();

                        // Add to the list of reminders as long as it's not the campaign "name" key
                        if (!campaignStep.equals("name")) {
                            long alarmTime = jsonSteps.getLong(campaignStep);
                            Reminder reminder = new Reminder(campaignId, campaignName, campaignStep, alarmTime);
                            mListReminders.add(reminder);
                        }
                    }
                }
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        if (!mListReminders.isEmpty()) {
            // Sort the results in ascending order
            Collections.sort(mListReminders, new ReminderComparator());

            // Populate the list with the custom adapter
            ReminderListAdapter listAdapter = new ReminderListAdapter(this, mListReminders);
            mList.setAdapter(listAdapter);

            // And set click listener
            mList.setOnItemClickListener(this);
        }
        else {
            // Display the empty view
            mList.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Reminder reminder = mListReminders.get(position);
        String campaignId = reminder.getCampaignId();
        String campaignStep = reminder.getCampaignStep();

        // Launch the Campaign activity and end this activity
        startActivity(Campaign.getIntent(RemindersActivity.this, campaignId, campaignStep));
        finish();
    }

    /**
     * Wrapper class for reminder data pulled from the DSPreferences cache.
     */
    private class Reminder {
        // Unique campaign id
        private String campaignId;

        // Display name of the campaign
        private String campaignName;

        // Display name of the campaign step the reminder is for
        private String campaignStep;

        // Time in milliseconds when the reminder should trigger
        private long time;

        public Reminder(String id, String name, String step, long t) {
            campaignId = id;
            campaignName = name;
            campaignStep = step;
            time = t;
        }

        public String getCampaignId() {
            return campaignId;
        }

        public String getCampaignName() {
            return campaignName;
        }

        public String getCampaignStep() {
            return campaignStep;
        }

        public long getTime() {
            return time;
        }
    }

    /**
     * Helper Comparator to sort a list of Reminders.
     */
    private class ReminderComparator implements Comparator<Reminder> {
        @Override
        public int compare(Reminder o1, Reminder o2) {
            long delta = o1.getTime() - o2.getTime();
            return (int)delta;
        }
    }

    /**
     * Custom adapter to display reminder info.
     */
    private class ReminderListAdapter extends ArrayAdapter<Reminder> {
        public ReminderListAdapter(Context context, List<Reminder> objects) {
            super(context, android.R.layout.simple_expandable_list_item_1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.reminders_row, null);
            }

            // Display the date this reminder is set for
            Reminder reminder = getItem(position);
            long reminderTime = reminder.getTime();

            Date reminderDate = new Date(reminderTime);
            DateFormat df = DateFormat.getDateInstance();
            df.setTimeZone(Calendar.getInstance().getTimeZone());
            String strDate = df.format(reminderDate);

            TextView dateView = (TextView)v.findViewById(R.id.reminder_date);
            dateView.setText(strDate);
            dateView.setTypeface(typefaceBold);

            // Display the campaign name
            TextView nameView = (TextView)v.findViewById(R.id.campaign_name);
            nameView.setText(reminder.getCampaignName());
            nameView.setTypeface(typefaceBold);

            // Display the campaign step
            TextView stepView = (TextView)v.findViewById(R.id.campaign_step);
            stepView.setText(reminder.getCampaignStep());
            stepView.setTypeface(typefaceReg);

            return v;
        }
    }

    /**
     * Intent for other activities to use to open this activity.
     */
    public static Intent getIntent(Context context) {
        return new Intent(context, RemindersActivity.class);
    }
}
