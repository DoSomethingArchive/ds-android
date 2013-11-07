package org.dosomething.android.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.adapters.DrawerListAdapter;
import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.tasks.AbstractFetchCampaignsTask;
import org.dosomething.android.tasks.AbstractWebserviceTask;
import org.dosomething.android.tasks.ErrorResponseCodeException;
import org.dosomething.android.transfer.Campaign;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.inject.InjectView;

public class Profile extends AbstractActionBarActivity {

    private static final int REQ_LOGIN_FOR_PROFILE = 112;
    private static final String DF = "MM/dd/yy";
    private static final String FROM_CAUSE_SEL = "from_cause_sel";

    @Inject private LayoutInflater inflater;
    @Inject private UserContext userContext;
    @Inject private @Named("DINComp-CondBold")Typeface headerTypeface;
    @Inject private Cache cache;
    @Inject private DSDao dao;

    @InjectView(R.id.content) private LinearLayout content;
    // Side navigation drawer
    @InjectView(R.id.drawer_layout) private DrawerLayout mDrawerLayout;
    @InjectView(R.id.left_drawer) private ListView mDrawerList;

    // Class to handle toggling of the navigation drawer
    private ActionBarDrawerToggle mDrawerToggle;

    private ListView list;

    private Context context;
    private boolean initializingActivity = true;

    private List<Campaign> webCampaigns;

    @Override
    public String getPageName() {
        return "profile";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Progress bar needs to be requested before setContentView is called
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.profile);

        context = this;

        if (getIntent() != null && getIntent().getExtras() != null) {
            boolean bFromCauseSel = getIntent().getExtras().getBoolean(FROM_CAUSE_SEL);
            if (bFromCauseSel) {
                Toast toast = Toast.makeText(this, R.string.cause_confirm, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 64);
                toast.show();
                getIntent().removeExtra(FROM_CAUSE_SEL);
            }
        }

        // Setup drawer navigation toggling
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {};
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        content.removeAllViews();

        if (userContext.isLoggedIn()) {
            // Start process to find campaigns the user's signed up for
            new CampaignTask().execute();
        }
        else {
            // Show the "no campaigns" layout
            content.addView(inflater.inflate(R.layout.profile_logged_out, null));

            Button btnAction = (Button)findViewById(R.id.profile_action_button);
            btnAction.setTypeface(headerTypeface, Typeface.BOLD);
        }

        setupDrawerNavigation();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_LOGIN_FOR_PROFILE && resultCode == RESULT_OK){
            if(new UserContext(this).isLoggedIn()){
                startActivity(Profile.getIntent(getApplicationContext()));
            }
        }
    }

    /**
     * Get confirmation from user that they want to exit the application when the press the Back button
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.exit_confirm))
                .setPositiveButton(R.string.yes_upper, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Profile.this.finish();
                    }
                })
                .setNegativeButton(R.string.no_upper, null)
                .create()
                .show();
    }

    private final OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            Campaign campaign = (Campaign) list.getAdapter().getItem(position);
            startActivity(org.dosomething.android.activities.Campaign.getIntent(getApplicationContext(), campaign));
        }
    };

    private void setupDrawerNavigation() {
        // Navigation options change depending on if user is logged in or not
        List<String> navItems = new ArrayList<String>();
        if (userContext.isLoggedIn()) {
            navItems.add(0, getString(R.string.drawer_item_campaigns));
            navItems.add(1, getString(R.string.drawer_item_profile));
            navItems.add(2, getString(R.string.drawer_item_settings));
            navItems.add(3, getString(R.string.drawer_item_logout));
        }
        else {
            navItems.add(0, getString(R.string.drawer_item_campaigns));
            navItems.add(1, getString(R.string.drawer_item_profile));
            navItems.add(2, getString(R.string.drawer_item_login));
        }

        mDrawerList.setAdapter(new DrawerListAdapter(this, navItems));
        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int pos, long id) {
                Context ctx = view.getContext();
                switch (pos) {
                    case 0:
                        startActivity(Campaigns.getIntent(ctx));
                        break;
                    case 1:
                        // Already on this page, so do nothing.
                        break;
                    case 2:
                        if (userContext.isLoggedIn())
                            startActivity(ProfileConfig.getIntent(ctx));
                        else
                            startActivityForResult(Login.getIntent(ctx), REQ_LOGIN_FOR_PROFILE);
                        break;
                    case 3:
                        Login.logout(ctx);
                        break;
                }

                // Close the side navigation drawer
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });
    }

    public void findCampaigns(View v) {
        startActivity(new Intent(this, Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, Profile.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private class CampaignListAdapter extends ArrayAdapter<Campaign> {

        private List<UserCampaign> userCampaigns;
        private List<Campaign> campaigns;

        public CampaignListAdapter(Context context, List<UserCampaign> userCampaigns, List<Campaign> campaigns){
            super(context, android.R.layout.simple_list_item_1, campaigns);
            this.userCampaigns = userCampaigns;
            this.campaigns = campaigns;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.profile_campaign_row, null);
            }

            UserCampaign userCampaign = userCampaigns.get(position);
            Campaign campaign = campaigns.get(position);

            TextView txtName = (TextView) v.findViewById(R.id.name);
            txtName.setTypeface(headerTypeface, Typeface.BOLD);
            txtName.setText(campaign.getName());

            return v;
        }

    }

    /**
     * Updates the ListView component of the Profile view with the campaigns the
     * user is known to be signed up for.
     *
     * @param gids List of gids that the user's signed up for as found through their Profile
     */
    private void updateProfileList(List<Integer> gids) {
        List<Campaign> signedUpCampaigns = new ArrayList<Campaign>();
        List<UserCampaign> userCampaigns = new ArrayList<UserCampaign>();

        // userCampaignsMap stores list of campaigns the user has signed up for on the app
        Map<String, UserCampaign> userCampaignsMap = new HashMap<String, UserCampaign>();
        String uid = new UserContext(context).getUserUid();
        if (uid != null) {
            List<UserCampaign> allUserCampaigns = dao.findUserCampaigns(uid);
            for(UserCampaign userCampaign : allUserCampaigns){
                userCampaignsMap.put(userCampaign.getCampaignId(), userCampaign);
            }

            // webCampaigns fetched from the CampaignTask add to list of campaigns to check against
            List<Campaign> allCampaigns = new ArrayList<Campaign>();
            allCampaigns.addAll(webCampaigns);

            // cachedCampaigns are fetched from a known private file on the system
            List<Campaign> cachedCampaigns = dao.fetchSignedUpCampaignData(uid);
            for (Campaign cachedCampaign : cachedCampaigns) {

                // Ensure no campaigns are added multiple times
                boolean bDuplicateFound = false;
                for (Campaign webCampaign : allCampaigns) {
                    if (webCampaign.getId().equals(cachedCampaign.getId())) {
                        bDuplicateFound = true;
                        break;
                    }
                }

                if (!bDuplicateFound)
                    allCampaigns.add(cachedCampaign);
            }

            // Flag indicating a campaign sign up was only detected on the server,
            // and not on the app itself.
            boolean foundSignUpOnServer = false;

            // Cycle through all known campaigns from web or cache and check if they
            // match any from the list of user campaigns (saved from when they
            // signed up for a campaign on the app).
            for (Campaign campaign : allCampaigns) {
                boolean addCampaign = false;

                if (userCampaignsMap.containsKey(campaign.getId())) {
                    addCampaign = true;
                }
                else if (gids != null) {
                    // User OG id's to determine if user signed up for this campaign on the website
                    for (int i = 0; gids != null && i < gids.size(); i++) {
                        if (campaign.getGid() == gids.get(i).intValue()) {

                            // Save campaign as being signed up for
                            UserCampaign uc = new UserCampaign.UserCampaignCVBuilder()
                                    .campaignId(campaign.getId())
                                    .uid(uid)
                                    .build();

                            // Add to userCampaignsMap HashMap
                            UserCampaign newUserCampaign = dao.findUserCampaign(uid, campaign.getId());
                            if (newUserCampaign != null) {
                                userCampaignsMap.put(newUserCampaign.getCampaignId(), newUserCampaign);

                                // Only add to list for display if this succeeds. Otherwise, will crash
                                addCampaign = true;
                                foundSignUpOnServer = true;
                            }

                            break;
                        }
                    }
                }

                if (addCampaign) {
                    signedUpCampaigns.add(campaign);
                    userCampaigns.add(userCampaignsMap.get(campaign.getId()));
                }
            }

            // Display Toast notifying user some campaigns were found from the profile sync
            if (foundSignUpOnServer) {
                Toast toast = Toast.makeText(context, R.string.profile_campaign_synced, Toast.LENGTH_LONG);
                toast.show();
            }
        }

        // If no campaigns were found, change to the layout with no profile activity
        if (signedUpCampaigns.isEmpty()) {
            content.removeAllViews();
            content.addView(inflater.inflate(R.layout.profile_no_campaigns, null));

            Button btnAction = (Button)findViewById(R.id.profile_action_button);
            btnAction.setTypeface(headerTypeface, Typeface.BOLD);
        }
        // Otherwise, clear the ListView and repopulate with updated content
        else {
            list = new ListView(Profile.this);
            content.removeAllViews();
            content.addView(list, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

            list.setOnItemClickListener(itemClickListener);
            list.setAdapter(new CampaignListAdapter(context, userCampaigns, signedUpCampaigns));

            // Write campaigns to file cache
            for (Campaign c : signedUpCampaigns) {
                try {
                    dao.saveSignedUpCampaignData(uid, c);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Task to get user's profile info from server.
     */
    private class UserTask extends AbstractWebserviceTask {
        private ArrayList<Integer> gids = new ArrayList<Integer>();
        private boolean isSessionStale = false;

        public UserTask() {
            super(userContext);
        }

        @Override
        protected void doWebOperation() throws Exception {
            String uid = new UserContext(context).getUserUid();
            String url = DSConstants.API_URL_BASE + "user/"+uid+".json";

            WebserviceResponse response = null;
            try {
                response = doGet(url);
            }
            catch (ErrorResponseCodeException e) {
                // If web op receives a 401, indicates session cookie info is stale
                if (e.getResponseCode() == 401) {
                    isSessionStale = true;
                }
                else {
                    e.printStackTrace();
                }
            }

            if (response != null && !response.hasErrorStatusCode()) {
                // gid's found in group_audience object show what campaigns, clubs,
                // and other OG groups the user is signed up for
                JSONObject jsonResponse = response.getBodyAsJSONObject();
                try {
                    JSONObject group_audience = jsonResponse.getJSONObject("group_audience");
                    JSONArray groups_array = group_audience.getJSONArray("und");
                    if (groups_array != null) {
                        for (int i = 0; i < groups_array.length(); i++) {
                            JSONObject group = groups_array.getJSONObject(i);
                            int gid = group.getInt("gid");
                            gids.add(Integer.valueOf(gid));
                        }
                    }
                }
                catch (Exception e) {
                    // The group_audience field is returned as an array when empty :(
                    // Catching the type-mismatch error here, and doing nothing
                    // since there should be no groups to add anyway.
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onSuccess() {
            updateProfileList(gids);
        }

        @Override
        protected void onFinish() {
            setProgressBarIndeterminateVisibility(Boolean.FALSE);

            // If the session's stale, then logout the user and force a re-login
            if (isSessionStale) {
                new AlertDialog.Builder(Profile.this)
                        .setMessage(getString(R.string.profile_session_stale_error))
                        .setPositiveButton(R.string.ok_upper, new Login.OnLogoutClickListener(Profile.this))
                        .create()
                        .show();
            }
        }

        @Override
        protected void onError(Exception e) {
        }
    }

    /**
     * Task to fetch the campaign data from the server.
     */
    private class CampaignTask extends AbstractFetchCampaignsTask {

        public CampaignTask() {
            super(Profile.this.context, userContext, cache);
        }

        @Override
        protected void onSuccess() {
            webCampaigns = getCampaigns();

            updateProfileList(null);

            if (initializingActivity) {
                // Only query for user data on initial activity load
                new UserTask().execute();
                initializingActivity = false;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressBarIndeterminateVisibility(Boolean.TRUE);
        }

        @Override
        protected void onError(Exception e) {
            Toast toast = Toast.makeText(context, R.string.profile_load_error, Toast.LENGTH_LONG);
            toast.show();
        }

    }

}
