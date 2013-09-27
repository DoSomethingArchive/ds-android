package org.dosomething.android.activities;

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
import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.fragments.CampaignFaqFragment;
import org.dosomething.android.fragments.CampaignGalleryFragment;
import org.dosomething.android.fragments.CampaignHowToFragment;
import org.dosomething.android.fragments.CampaignMainFragment;
import org.dosomething.android.fragments.CampaignPeopleFragment;
import org.dosomething.android.fragments.CampaignPrizesFragment;
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
    private static final String SMS_REFER = "sms-refer";

    private static final int REQ_LOGIN_FOR_SIGN_UP = 111;
    private static final int SMS_REFER_ACTIVITY = 112;

    @Inject private UserContext userContext;
    @Inject private Cache cache;
    @InjectView(R.id.pager) private ViewPager mViewPager;

    private org.dosomething.android.transfer.Campaign campaign;
    private CampaignPagerAdapter mCampaignPagerAdapter;
    private ActionBar.TabListener mTabListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.campaign);

        // Setup ActionBar look and tab navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mTabListener = new ActionBarTabListener();

        // Setup ActionBar with available campaign info, or get it if we don't have it
        campaign = (org.dosomething.android.transfer.Campaign) getIntent().getSerializableExtra(CAMPAIGN);
        if (campaign != null) {
            HashMap tabHash = setupActionBarTabs();
            mCampaignPagerAdapter = new CampaignPagerAdapter(getSupportFragmentManager(), tabHash);
            mViewPager.setAdapter(mCampaignPagerAdapter);
            // OnPageChangeListener to update UI when user swipes to a different fragment page
            mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i2) {}

                @Override
                public void onPageSelected(int position) {
                    getSupportActionBar().setSelectedNavigationItem(position);
                }

                @Override
                public void onPageScrollStateChanged(int i) {}
            });
        }
        else {
            String campaignId = getIntent().getStringExtra(CAMPAIGN_ID);
            getSupportActionBar().setTitle(R.string.campaign_loading);
            // Load appropriate campaign from cache, otherwise download the data
            new CampaignsFetchTask(this, campaignId).execute();
        }
    }

    /**
     * TODO
     */
    private class CampaignPagerAdapter extends FragmentStatePagerAdapter {

        private HashMap<Integer, String> mTabHash;

        public CampaignPagerAdapter(FragmentManager fm, HashMap tabHash) {
            super(fm);

            mTabHash = tabHash;
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
            }
            return null;
        }

        @Override
        public int getCount() {
            return mTabHash.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "THIS IS A TEST, MARAH";
        }

        public int getTabItemByTitle(String title) {
            for (Map.Entry<Integer, String> entry : mTabHash.entrySet()) {
                if (title.contentEquals(entry.getValue())) {
                    return entry.getKey().intValue();
                }
            }

            return -1;
        }
    }

    /**
     * TODO
     */
    private class ActionBarTabListener implements ActionBar.TabListener {
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}

        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}
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
     * Check the campaign data and determine what tabs are needed.
     *
     * @return HashMap<Integer, String> where Integer is the ActionBar index of the tab
     *  and String is the title of the ActionBar tab.
     */
    private HashMap<Integer, String> setupActionBarTabs() {
        // Clear ActionBar tabs, if any, and repopulate based on available fields
        ActionBar actionBar = getSupportActionBar();
        actionBar.removeAllTabs();

        // First tab will always be the main campaign content
        actionBar.addTab(actionBar.newTab()
                        .setText(campaign.getName())
                        .setTabListener(mTabListener)
        );

        HashMap<Integer, String> tabHash = new HashMap<Integer, String>();
        int tabIndex = 0;

        tabHash.put(Integer.valueOf(tabIndex), campaign.getName());
        tabIndex++;

        if (!nullOrEmpty(campaign.getHowTos())) {
            actionBar.addTab(actionBar.newTab()
                            .setText(R.string.campaign_how_to_title)
                            .setTabListener(mTabListener)
            );

            tabHash.put(Integer.valueOf(tabIndex), getString(R.string.campaign_how_to_title));
            tabIndex++;
        }

        if (campaign.getGallery() != null) {
            actionBar.addTab(actionBar.newTab()
                            .setText(R.string.campaign_gallery_title)
                            .setTabListener(mTabListener)
            );

            tabHash.put(Integer.valueOf(tabIndex), getString(R.string.campaign_gallery_title));
            tabIndex++;
        }

        if (campaign.getPrize() != null) {
            actionBar.addTab(actionBar.newTab()
                            .setText(R.string.campaign_prizes_title)
                            .setTabListener(mTabListener)
            );

            tabHash.put(Integer.valueOf(tabIndex), getString(R.string.campaign_prizes_title));
            tabIndex++;
        }

        if (campaign.getPeople() != null) {
            actionBar.addTab(actionBar.newTab()
                            .setText(R.string.campaign_people_title)
                            .setTabListener(mTabListener)
            );

            tabHash.put(Integer.valueOf(tabIndex), getString(R.string.campaign_people_title));
            tabIndex++;
        }

        if (!nullOrEmpty(campaign.getResources()) || campaign.getMoreInfo() != null) {
            actionBar.addTab(actionBar.newTab()
                            .setText(R.string.campaign_resources_title)
                            .setTabListener(mTabListener)
            );

            tabHash.put(Integer.valueOf(tabIndex), getString(R.string.campaign_resources_title));
            tabIndex++;
        }

        if (!nullOrEmpty(campaign.getFaqs())) {
            actionBar.addTab(actionBar.newTab()
                            .setText(R.string.campaign_faq_title)
                            .setTabListener(mTabListener)
            );

            tabHash.put(Integer.valueOf(tabIndex), getString(R.string.campaign_faq_title));
            tabIndex++;
        }

        return tabHash;
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

    public void onClickActions(View v) {
        startActivity(CampaignActions.getIntent(this, campaign));
    }

    public void onClickHowTo(View v) {
        startActivity(CampaignHowTo.getIntent(this, campaign));
    }

    public void onClickGallery(View v) {
        startActivity(CampaignGallery.getIntent(this, campaign));
    }

    public void onClickPeople(View v) {
        startActivity(CampaignPeople.getIntent(this, campaign));
    }

    public void onClickPrizes(View v) {
        startActivity(CampaignPrizes.getIntent(this, campaign));
    }

    public void onClickResources(View v) {
        startActivity(CampaignResources.getIntent(this, campaign));
    }

    public void onClickFaq(View v) {
        startActivity(CampaignFAQ.getIntent(this, campaign));
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

    public static Intent getIntent(Context context, org.dosomething.android.transfer.Campaign campaign) {
        Intent answer = new Intent(context, Campaign.class);
        answer.putExtra(CAMPAIGN, campaign);
        return answer;
    }

    public static Intent getIntent(Context context, String campaignId) {
        Intent answer = new Intent(context, Campaign.class);
        answer.putExtra(CAMPAIGN_ID, campaignId);
        return answer;
    }

    @Override
    protected String getPageName() {
        return "campaign";
    }

    private class CampaignsFetchTask extends AbstractFetchCampaignsTask {

        private String campaignId;
        private Context context;

        public CampaignsFetchTask(Context _context, String _campaignId) {
            super(Campaign.this, userContext, cache, null);
            campaignId = _campaignId;
            context = _context;
        }

        @Override
        protected void onSuccess() {
            if (campaignId != null) {
                campaign = getCampaignById(campaignId);
                if (campaign != null) {
                    setupActionBarTabs();
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
