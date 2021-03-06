package org.dosomething.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.inject.Inject;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.fragments.AbstractCampaignFragment;
import org.dosomething.android.fragments.CampaignDoFragment;
import org.dosomething.android.fragments.CampaignFaqFragment;
import org.dosomething.android.fragments.CampaignGalleryFragment;
import org.dosomething.android.fragments.CampaignHowToFragment;
import org.dosomething.android.fragments.CampaignLearnFragment;
import org.dosomething.android.fragments.CampaignMainFragment;
import org.dosomething.android.fragments.CampaignPeopleFragment;
import org.dosomething.android.fragments.CampaignPlanFragment;
import org.dosomething.android.fragments.CampaignPrizesFragment;
import org.dosomething.android.fragments.CampaignProgressDeniedFragment;
import org.dosomething.android.fragments.CampaignReportBackFragment;
import org.dosomething.android.fragments.CampaignResourcesFragment;
import org.dosomething.android.tasks.AbstractFetchCampaignsTask;
import org.dosomething.android.tasks.NoInternetException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roboguice.inject.InjectView;

public class Campaign extends AbstractActionBarActivity {

    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();
    private static final String CAMPAIGN_ID = "campaign-id";
    private static final String CAMPAIGN_STEP = "campaign-step";
    private static final String SMS_REFER = "sms-refer";

    private static final int REQ_LOGIN_FOR_SIGN_UP = 111;
    private static final int SMS_REFER_ACTIVITY = 112;

    @Inject private UserContext userContext;
    @Inject private Cache cache;
    @InjectView(R.id.pager) private ViewPager mViewPager;

    private org.dosomething.android.transfer.Campaign campaign;
    private Activity mActivity;
    private CampaignPagerAdapter mCampaignPagerAdapter;

    @Override
    public String getPageName() {
        return "Campaign";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.campaign);

        // Setup ActionBar look and tab navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Setup ActionBar with available campaign info, or get it if we don't have it
        campaign = (org.dosomething.android.transfer.Campaign) getIntent().getSerializableExtra(CAMPAIGN);
        if (campaign != null) {
            populateTabs();

            // Log a page view for the initial subpage shown when the activity first loads
            logCampaignSubpageView(0);
        }
        else {
            String campaignId = getIntent().getStringExtra(CAMPAIGN_ID);
            getSupportActionBar().setTitle(R.string.campaign_loading);

            String campaignStep = getIntent().getStringExtra(CAMPAIGN_STEP);

            // Load appropriate campaign from cache, otherwise download the data
            new CampaignsFetchTask(this, campaignId, campaignStep).execute();
        }
    }

    /**
     * Populate the Action Bar tabs and Adapter with the campaign data.
     *
     * @return HashMap<Integer, String> of the tab index and corresponding tab title
     */
    public HashMap populateTabs() {
        HashMap tabHash = setupActionBarTabs();
        mCampaignPagerAdapter = new CampaignPagerAdapter(getSupportFragmentManager(), tabHash);
        mViewPager.setAdapter(mCampaignPagerAdapter);
        // OnPageChangeListener to update UI when user swipes to a different fragment page
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                ActionBar ab = getSupportActionBar();
                if (ab.getNavigationItemCount() > position) {
                    getSupportActionBar().setSelectedNavigationItem(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        return tabHash;
    }

    /**
     * Set the current tab to the index given.
     */
    public void setCurrentTab(int index) {
        mViewPager.setCurrentItem(index);
    }

    /**
     * Sets fragments to display in the Tabbed Action bar.
     */
    private class CampaignPagerAdapter extends FragmentStatePagerAdapter {

        private HashMap<Integer, String> mTabHash;

        public CampaignPagerAdapter(FragmentManager fm, HashMap tabHash) {
            super(fm);

            mTabHash = tabHash;
        }

        @Override
        public int getCount() {
            return mTabHash.size();
        }

        @Override
        public Fragment getItem(int i) {

            // Package the campaign data into Bundle for use by the other fragments
            Bundle args = new Bundle();
            args.putSerializable(CAMPAIGN, campaign);

            // First item should always be the main campaign info
            if (i == 0) {
                CampaignMainFragment fragment = new CampaignMainFragment();
                fragment.setArguments(args);
                return fragment;
            }
            else {
                String tabTitle = mTabHash.get(Integer.valueOf(i));

                if (tabTitle.contentEquals(getString(R.string.campaign_faq_title))) {
                    CampaignFaqFragment fragment = new CampaignFaqFragment();
                    fragment.setArguments(args);
                    return fragment;
                }
                else if (tabTitle.contentEquals(getString(R.string.campaign_gallery_title))) {
                    CampaignGalleryFragment fragment = new CampaignGalleryFragment();
                    fragment.setArguments(args);
                    return fragment;
                }
                else if (tabTitle.contentEquals(getString(R.string.campaign_how_to_title))) {
                    CampaignHowToFragment fragment = new CampaignHowToFragment();
                    fragment.setArguments(args);
                    return fragment;
                }
                else if (tabTitle.contentEquals(getString(R.string.campaign_prizes_title))) {
                    CampaignPrizesFragment fragment = new CampaignPrizesFragment();
                    fragment.setArguments(args);
                    return fragment;
                }
                else if (tabTitle.contentEquals(getString(R.string.campaign_people_title))) {
                    CampaignPeopleFragment fragment = new CampaignPeopleFragment();
                    fragment.setArguments(args);
                    return fragment;
                }
                else if (tabTitle.contentEquals(getString(R.string.campaign_resources_title))) {
                    CampaignResourcesFragment fragment = new CampaignResourcesFragment();
                    fragment.setArguments(args);
                    return fragment;
                }
                else if (tabTitle.contentEquals(getString(R.string.campaign_fragment_do_tab_title))) {
                    CampaignDoFragment fragment = new CampaignDoFragment();
                    fragment.setArguments(args);
                    return fragment;
                }
                else if (tabTitle.contentEquals(getString(R.string.campaign_fragment_learn_tab_title))) {
                    CampaignLearnFragment fragment = new CampaignLearnFragment();
                    fragment.setArguments(args);
                    return fragment;
                }
                else if (tabTitle.contentEquals(getString(R.string.campaign_fragment_plan_tab_title))) {
                    CampaignPlanFragment fragment = new CampaignPlanFragment();
                    fragment.setArguments(args);
                    return fragment;
                }
                else if (tabTitle.contentEquals(getString(R.string.campaign_fragment_reportback_title))) {
                    CampaignReportBackFragment fragment = new CampaignReportBackFragment();
                    fragment.setArguments(args);
                    return fragment;
                }
                else if (tabTitle.contentEquals(getString(R.string.campaign_fragment_progress_denied_title))) {
                    CampaignProgressDeniedFragment fragment = new CampaignProgressDeniedFragment();
                    return fragment;
                }
            }
            return null;
        }

        @Override
        public float getPageWidth(int position) {
            float width = super.getPageWidth(position);

            // For the "progress denied" fragment, don't allow it to scroll the full width of the screen
            String tabTitle = mTabHash.get(Integer.valueOf(position));
            if (tabTitle.contentEquals(getString(R.string.campaign_fragment_progress_denied_title))) {
                width = 0.7f;
            }

            return width;
        }

        public int getTabItemByTitle(String title) {
            for (Map.Entry<Integer, String> entry : mTabHash.entrySet()) {
                if (title.contentEquals(entry.getValue())) {
                    return entry.getKey().intValue();
                }
            }

            return -1;
        }

        public void setTabHash(HashMap<Integer, String> hash) {
            mTabHash = hash;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If home button is selected on ActionBar, then end the activity
            case android.R.id.home:
                // If this activity is launched from a notification, there's likely nothing else in
                // the back stack. So pressing the Home button on the action bar would exit the app.
                // Instead of doing that, if there's no back stack, launch a new Campaigns activity.
                if (isTaskRoot()) {
                    Intent i = Campaigns.getIntent(Campaign.this);
                    startActivity(i);
                }

                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Rebuild the tabs in the ActionBar.
     */
    public void refreshActionBarTabs() {
        HashMap<Integer, String> tabHash = setupActionBarTabs();
        mCampaignPagerAdapter = new CampaignPagerAdapter(getSupportFragmentManager(), tabHash);
        mViewPager.setAdapter(mCampaignPagerAdapter);
    }

    /**
     * Check the campaign data and determine what tabs are needed.
     *
     * @return HashMap<Integer, String> where Integer is the ActionBar index of the tab
     *  and String is the title of the ActionBar tab.
     */
    private HashMap<Integer, String> setupActionBarTabs() {
        // Clear ActionBar tabs, if any, and repopulate based on available fields
        ActionBar actionBar = getSupportActionBar();
        actionBar.removeAllTabs();

        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                if (mViewPager != null && mCampaignPagerAdapter != null) {
                    mViewPager.setCurrentItem(tab.getPosition());

                    logCampaignSubpageView(tab.getPosition());
                }
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}
        };

        // First tab will always be the main campaign content
        actionBar.addTab(actionBar.newTab()
                        .setText(campaign.getName())
                        .setTabListener(tabListener)
        );


        HashMap<Integer, String> tabHash = new HashMap<Integer, String>();
        int tabIndex = 0;

        tabHash.put(Integer.valueOf(tabIndex), campaign.getName());

        // If the user has not signed up for the campaign, add another fragment that can be scrolled
        // to but won't have a tab. Fragment will prompt user to sign up to progress.
        String uid = new UserContext(this).getUserUid();
        boolean isSignedUp = new DSDao(this).isSignedUpForCampaign(uid, campaign.getId());
        if (!isSignedUp) {
            tabHash.put(Integer.valueOf(++tabIndex), getString(R.string.campaign_fragment_progress_denied_title));
            return tabHash;
        }

        // If we have the data for the new format, then use it
        if (!nullOrEmpty(campaign.getLearnData()) && !nullOrEmpty(campaign.getPlanData()) && !nullOrEmpty(campaign.getDoItData())) {
            actionBar.addTab(actionBar.newTab()
                    .setText(R.string.campaign_fragment_learn_tab_title)
                    .setTabListener(tabListener));
            tabHash.put(Integer.valueOf(++tabIndex), getString(R.string.campaign_fragment_learn_tab_title));

            actionBar.addTab(actionBar.newTab()
                    .setText(R.string.campaign_fragment_plan_tab_title)
                    .setTabListener(tabListener));
            tabHash.put(Integer.valueOf(++tabIndex), getString(R.string.campaign_fragment_plan_tab_title));

            actionBar.addTab(actionBar.newTab()
                    .setText(R.string.campaign_fragment_do_tab_title)
                    .setTabListener(tabListener));
            tabHash.put(Integer.valueOf(++tabIndex), getString(R.string.campaign_fragment_do_tab_title));
        }
        // Otherwise, fall back on the older campaign template
        else{
            if (!nullOrEmpty(campaign.getHowTos())) {
                actionBar.addTab(actionBar.newTab()
                                .setText(R.string.campaign_how_to_title)
                                .setTabListener(tabListener)
                );

                tabHash.put(Integer.valueOf(++tabIndex), getString(R.string.campaign_how_to_title));
            }

            if (campaign.getGallery() != null) {
                actionBar.addTab(actionBar.newTab()
                                .setText(R.string.campaign_gallery_title)
                                .setTabListener(tabListener)
                );

                tabHash.put(Integer.valueOf(++tabIndex), getString(R.string.campaign_gallery_title));
            }

            if (campaign.getPrize() != null) {
                actionBar.addTab(actionBar.newTab()
                                .setText(R.string.campaign_prizes_title)
                                .setTabListener(tabListener)
                );

                tabHash.put(Integer.valueOf(++tabIndex), getString(R.string.campaign_prizes_title));
            }

            if (campaign.getPeople() != null) {
                actionBar.addTab(actionBar.newTab()
                                .setText(R.string.campaign_people_title)
                                .setTabListener(tabListener)
                );

                tabHash.put(Integer.valueOf(++tabIndex), getString(R.string.campaign_people_title));
            }

            if (!nullOrEmpty(campaign.getResources()) || campaign.getMoreInfo() != null) {
                actionBar.addTab(actionBar.newTab()
                                .setText(R.string.campaign_resources_title)
                                .setTabListener(tabListener)
                );

                tabHash.put(Integer.valueOf(++tabIndex), getString(R.string.campaign_resources_title));
            }

            if (!nullOrEmpty(campaign.getFaqs())) {
                actionBar.addTab(actionBar.newTab()
                                .setText(R.string.campaign_faq_title)
                                .setTabListener(tabListener)
                );

                tabHash.put(Integer.valueOf(++tabIndex), getString(R.string.campaign_faq_title));
            }
        }

        // The report back tab should always be last
        if (campaign.getReportBack() != null) {
            actionBar.addTab(actionBar.newTab()
                    .setText(R.string.campaign_fragment_reportback_title)
                    .setTabListener(tabListener)
            );

            tabHash.put(Integer.valueOf(++tabIndex), getString(R.string.campaign_fragment_reportback_title));
        }

        return tabHash;
    }

    /**
     * Log the view of a campaign subpage to analytics.
     *
     * @param position Position of fragment in the list of tabs
     */
    private void logCampaignSubpageView(int position) {
        if (mCampaignPagerAdapter != null) {
            // Log fragment view to Analytics. Not done on Fragment.onResume() or something similar
            // because the ViewPager will load up fragments for adjacent tabs even if they won't
            // eventually come into the user's view.
            AbstractCampaignFragment fragment = (AbstractCampaignFragment)mCampaignPagerAdapter.getItem(position);
            if (fragment != null) {
                String fragName = fragment.getFragmentName();
                Analytics.logCampaignPageView(mActivity, fragName, campaign);
            }
        }
    }

    public void playVideo(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(campaign.getVideoUrl())));
    }

    private static final boolean nullOrEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    private static boolean nullOrEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_LOGIN_FOR_SIGN_UP && resultCode == RESULT_OK) {
            if (userContext.isLoggedIn()) {
                startActivity(SignUp.getIntent(this, campaign));
            }
        }
        else if (requestCode == SMS_REFER_ACTIVITY && resultCode == RESULT_OK) {
            boolean smsReferResult = data.getBooleanExtra(SMS_REFER, false);
            if (smsReferResult) {
                Toast.makeText(this, getString(R.string.sms_refer_success), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Create the Intent used to launch a Campaign activity.
     *
     * @param context Context
     * @param campaign Campaign data
     * @return Intent to launch the activity
     */
    public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
        Intent answer = new Intent(context, Campaign.class);
        answer.putExtra(CAMPAIGN, campaign);
        return answer;
    }

    /**
     * Create the Intent used to launch a Campaign activity.
     *
     * @param context Context
     * @param campaignId unique campaign id
     * @return Intent to launch the activity
     */
    public static Intent getIntent(Context context, String campaignId) {
        Intent answer = new Intent(context, Campaign.class);
        answer.putExtra(CAMPAIGN_ID, campaignId);
        return answer;
    }

    /**
     * Create the Intent used to launch a Campaign activity and start at the provided campaign step.
     *
     * @param context Context
     * @param campaignId unique campaign id
     * @param campaignStep campaign step to launch to
     * @return Intent to launch the activity
     */
    public static Intent getIntent(Context context, String campaignId, String campaignStep) {
        Intent answer = new Intent(context, Campaign.class);
        answer.putExtra(CAMPAIGN_ID, campaignId);
        answer.putExtra(CAMPAIGN_STEP, campaignStep);
        return answer;
    }

    /**
     * Webservice Task to retrieve the campaigns either from cache or downloading it from the server.
     */
    private class CampaignsFetchTask extends AbstractFetchCampaignsTask {

        // Context
        private Context context;

        // Unique id of the campaign
        private String campaignId;

        // If provided, the campaign step to start on
        private String campaignStep;

        public CampaignsFetchTask(Context _context, String _campaignId, String _campaignStep) {
            super(Campaign.this, userContext, cache);
            campaignId = _campaignId;
            context = _context;
            campaignStep = _campaignStep;
        }

        @Override
        protected void onSuccess() {
            if (campaignId != null) {
                campaign = getCampaignById(campaignId);
                if (campaign != null) {
                    // Setup the action bar tabs and data
                    HashMap<Integer, String> tabHash = populateTabs();

                    // Go to the proper campaign step if one is provided
                    if (campaignStep != null) {
                        // Find out the tab title based on the campaign step name from the reminder
                        String titleToMatch = null;
                        if (campaignStep.equals(getString(R.string.reminder_campaign_step_learn))) {
                            titleToMatch = getString(R.string.campaign_fragment_learn_tab_title);
                        }
                        else if (campaignStep.equals(getString(R.string.reminder_campaign_step_plan))) {
                            titleToMatch = getString(R.string.campaign_fragment_plan_tab_title);
                        }
                        else if (campaignStep.equals(getString(R.string.reminder_campaign_step_do))) {
                            titleToMatch = getString(R.string.campaign_fragment_do_tab_title);
                        }

                        // Find the tab index the step is on
                        int tabItemIndex = -1;
                        if (titleToMatch != null) {
                            for (int i = 0; i < tabHash.size(); i++) {
                                String tabTitle = tabHash.get(Integer.valueOf(i));
                                if (tabTitle.equals(titleToMatch)) {
                                    tabItemIndex = i;
                                    break;
                                }
                            }
                        }

                        // Switch over to that tab
                        if (tabItemIndex >= 0) {
                            setCurrentTab(tabItemIndex);
                        }
                    }

                    // Remove loading title message from the action bar
                    getSupportActionBar().setTitle(null);
                }
                else {
                    onError(null);
                }
            }
            else {
                onError(null);
            }
        }

        @Override
        protected void onError(Exception e) {
            String message;
            if (e instanceof NoInternetException) {
                message = getString(R.string.campaigns_no_internet);
            }
            else {
                message = getString(R.string.campaign_load_error);
            }

            new AlertDialog.Builder(Campaign.this)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok_upper), new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(Campaigns.getIntent(context));
                        }
                    })
                    .create()
                    .show();
        }

    }

}
