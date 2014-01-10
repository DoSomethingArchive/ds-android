package org.dosomething.android.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.inject.Inject;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.adapters.DrawerListAdapter;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.fragments.CampaignsListFragment;
import org.dosomething.android.fragments.UserCampaignsFragment;

import java.util.ArrayList;
import java.util.List;

import roboguice.inject.InjectView;

public class Campaigns extends AbstractActionBarActivity implements ActionBar.TabListener {

    private static final int REQ_LOGIN_FOR_PROFILE = 112;

    @Inject private UserContext userContext;

    @InjectView(R.id.drawer_layout) private DrawerLayout mDrawerLayout;
    @InjectView(R.id.left_drawer) private ListView mDrawerList;
    @InjectView(R.id.pager) private ViewPager mViewPager;

    private ActionBarDrawerToggle mDrawerToggle;
    private CampaignsPagerAdapter mCampaignsPagerAdapter;

    private String[] mTabTitles = new String[3];
    private boolean mHideTabs;

    @Override
    public String getPageName() {
        return "campaigns";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Progress bar needs to be requested before setContentView is called
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.campaigns);

        // Setup drawer navigation
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.drawer_open,
                R.string.drawer_close
        ) {};
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Setup ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Add tabs to the ActionBar
        mTabTitles[0] = getString(R.string.campaigns_tab_browse_title);
        mTabTitles[1] = getString(R.string.campaigns_tab_in_progress_title);
        mTabTitles[2] = getString(R.string.campaigns_tab_completed_title);

        actionBar.addTab(actionBar.newTab().setText(mTabTitles[0]).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(mTabTitles[1]).setTabListener(this));
        actionBar.addTab(actionBar.newTab().setText(mTabTitles[2]).setTabListener(this));

        mCampaignsPagerAdapter = new CampaignsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mCampaignsPagerAdapter);

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

        // Set the initial tab it should be on based on arguments passed to this activity
        if (getIntent() != null && getIntent().getExtras() != null) {
            int tabIndex = getIntent().getExtras().getInt(DSConstants.EXTRAS_KEY.CAMPAIGNS_TAB.getValue(), 0);
            mViewPager.setCurrentItem(tabIndex);
        }
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

        setupDrawerNavigation();
    }

    private void setupDrawerNavigation() {
        // Navigation options change depending on if user is logged in or not
        List<String> navItems = new ArrayList<String>();
        if (userContext.isLoggedIn()) {
            navItems.add(0, getString(R.string.drawer_item_campaigns));
            navItems.add(1, getString(R.string.drawer_item_settings));
            navItems.add(2, getString(R.string.drawer_item_logout));
        }
        else {
            navItems.add(0, getString(R.string.drawer_item_campaigns));
            navItems.add(1, getString(R.string.drawer_item_login));
        }

        mDrawerList.setAdapter(new DrawerListAdapter(this, navItems));
        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int pos, long id) {
                Context ctx = view.getContext();
                switch (pos) {
                    case 0:
                        // Already on this page, so do nothing.
                        break;
                    case 1:
                        if (userContext.isLoggedIn())
                            startActivity(ProfileConfig.getIntent(ctx));
                        else
                            startActivityForResult(Login.getIntent(ctx), REQ_LOGIN_FOR_PROFILE);
                        break;
                    case 2:
                        Login.logout(Campaigns.this);
                        break;
                }

                // Close the side navigation drawer
                mDrawerLayout.closeDrawer(mDrawerList);
            }
        });
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
     * Implementation needed for ActionBar.TabListener. Triggered when tab is selected.
     *
     * @param tab The selected tab
     * @param ft FragmentTransaction for queuing fragment operations
     */
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        // Do Something here
        if (mViewPager != null && mCampaignsPagerAdapter != null) {
            mViewPager.setCurrentItem(tab.getPosition());
        }
    }

    /**
     * Empty implementations, but needed for ActionBar.TabListener.
     */
    public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {}

    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}


    public static Intent getIntent(Context context) {
        Intent answer = new Intent(context, Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return answer;
    }

    public static Intent getIntent(Context context, DSConstants.CAMPAIGNS_TAB tab) {
        Intent answer = new Intent(context, Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        answer.putExtra(DSConstants.EXTRAS_KEY.CAMPAIGNS_TAB.getValue(), tab.ordinal());
        return answer;
    }

    /**
     * FragmentStatePagerAdapter to handle how fragments get displayed with each tab.
     */
    private class CampaignsPagerAdapter extends FragmentStatePagerAdapter {

        public CampaignsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    // Do It
                    return new CampaignsListFragment();
                case 1:
                    // Doing It
                    return new UserCampaignsFragment(false);
                case 2:
                    // Done
                    return new UserCampaignsFragment(true);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mTabTitles.length;
        }
    }

}
