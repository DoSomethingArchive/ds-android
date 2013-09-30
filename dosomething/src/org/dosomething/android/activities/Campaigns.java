package org.dosomething.android.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.markupartist.android.widget.ActionBar;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.cache.Cache;
import org.dosomething.android.cache.DSPreferences;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.tasks.AbstractFetchCampaignsTask;
import org.dosomething.android.tasks.NoInternetException;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import roboguice.inject.InjectView;

public class Campaigns extends AbstractActionBarActivity {

    //private static final String TAG = "Campaigns";
    private static final int REQ_LOGIN_FOR_PROFILE = 112;

    @Inject private LayoutInflater inflater;
    @Inject private ImageLoader imageLoader;
    @Inject private UserContext userContext;
    @Inject private Cache cache;
    @Inject @Named("DINComp-CondBold")Typeface calloutTypeface;

    @InjectView(R.id.popup) private RelativeLayout popupView;
    @InjectView(R.id.popupMsg) private TextView popupMsgView;
    @InjectView(R.id.popupClose) private Button popupCloseButton;
    @InjectView(R.id.list) private PullToRefreshListView pullToRefreshView;
    @InjectView(R.id.drawer_layout) private DrawerLayout mDrawerLayout;
    @InjectView(R.id.left_drawer) private ListView mDrawerList;

    private ActionBarDrawerToggle mDrawerToggle;
    private ListView list;
    private CampaignListAdapter listAdapter;

    private final OnItemClickListener itemClickListener = new CampaignItemClickListener();

    @Override
    protected String getPageName() {
        return "campaigns";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Progress bar needs to be requested before setContentView is called
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.campaigns);

        list = pullToRefreshView.getRefreshableView();
        pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                fetchCampaigns(true);
            }
        });

        popupMsgView.setTypeface(calloutTypeface);
        // Upgrade notification popup click listeners
        popupMsgView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open link to the Google Play Store
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.dosomething.android")));
            }
        });
        popupCloseButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.setVisibility(View.GONE);
            }
        });

        // Setup drawer navigation
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

        setupDrawerNavigation();

        fetchCampaigns(false);
    }

    private void setupDrawerNavigation() {
        // Navigation options change depending on if user is logged in or not
        String[] navItems;
        if (userContext.isLoggedIn()) {
            navItems = new String[4];
            navItems[0] = getString(R.string.drawer_item_campaigns);
            navItems[1] = getString(R.string.drawer_item_profile);
            navItems[2] = getString(R.string.drawer_item_settings);
            navItems[3] = getString(R.string.drawer_item_logout);
        }
        else {
            navItems = new String[3];
            navItems[0] = getString(R.string.drawer_item_campaigns);
            navItems[1] = getString(R.string.drawer_item_profile);
            navItems[2] = getString(R.string.drawer_item_login);
        }

        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, navItems));
        mDrawerList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int pos, long id) {
                Context ctx = getApplicationContext();
                switch (pos) {
                    case 0:
                        // Already on this page. Just close the drawer.
                        mDrawerLayout.closeDrawer(mDrawerList);
                        break;
                    case 1:
                        startActivity(Profile.getIntent(ctx));
                        break;
                    case 2:
                        if (userContext.isLoggedIn())
                            startActivity(new Intent(ctx, ProfileConfig.class));
                        else
                            Login.logout(ctx);
                        break;
                    case 3:
                        Login.logout(ctx);
                        break;
                }
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

    private void fetchCampaigns(boolean forceSearch) {
        CampaignsTask task = new CampaignsTask();

        task.setForceSearch(forceSearch);
        task.execute();
    }

    // TODO: remove all references to this
    public static ActionBar.Action getHomeAction(Context context){
        return new ActionBar.IntentAction(context, new Intent(context, Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP), R.drawable.action_bar_home);
    }

    public static Intent getIntent(Context context) {
        Intent answer = new Intent(context, Campaigns.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return answer;
    }

    private class CampaignItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {

            int openHeight = getResources().getDimensionPixelSize(R.dimen.campaign_row_height_open);
            if (v.getLayoutParams().height == openHeight) {
                Campaign campaign = (Campaign) list.getAdapter().getItem(position);
                startActivity(org.dosomething.android.activities.Campaign.getIntent(getApplicationContext(), campaign));
            }
            else {
                listAdapter.setItemOpenState(position - 1, true);

                ExpandCampaignAnimation anim = new ExpandCampaignAnimation(v);
                v.setAnimation(anim);
                anim.start();
            }
        }
    };

    private class ExpandCampaignAnimation extends Animation {
        private View campaignView;
        private int heightDelta, startingHeight;
        private int animDuration = 150;	// in milliseconds

        public ExpandCampaignAnimation(View v) {
            this.campaignView = v;
            this.setDuration(animDuration);
            this.setInterpolator(new AccelerateInterpolator());

            this.startingHeight = getResources().getDimensionPixelSize(R.dimen.campaign_row_height_closed);
            int targetHeight = getResources().getDimensionPixelSize(R.dimen.campaign_row_height_open);
            this.heightDelta = targetHeight - this.startingHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int newHeight = (int)(this.heightDelta * interpolatedTime) + this.startingHeight;
            campaignView.getLayoutParams().height = newHeight;
            campaignView.requestLayout();
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }


    private class CampaignsTask extends AbstractFetchCampaignsTask {

        private boolean currentVersionOutdated = false;
        private boolean forceSearch;

        public CampaignsTask() {
            super(Campaigns.this, userContext, cache, null);

            forceSearch = false;
        }

        @Override
        protected void onSuccess() {
            list.setOnItemClickListener(itemClickListener);

            // Don't display campaigns that require a higher version than what we have
            int version = 0;
            try {
                PackageInfo pInfo = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0);
                version = pInfo.versionCode;
            }
            catch (NameNotFoundException e) {
            }

            List<Campaign> campaigns = getCampaigns();
            Iterator<Campaign> iter = campaigns.iterator();
            while (iter.hasNext()) {
                Campaign campaign = iter.next();
                if (campaign.getMinVersion() > version) {
                    currentVersionOutdated = true;
                    iter.remove();
                }
                else if (campaign.isHidden()) {
                    iter.remove();
                }
            }

            listAdapter = new CampaignListAdapter(getApplicationContext(), campaigns);
            list.setAdapter(listAdapter);
        }

        @Override
        protected void onError(Exception e) {
            String message;
            if(e instanceof NoInternetException) {
                message = getString(R.string.campaigns_no_internet);
            } else {
                message = getString(R.string.campaigns_failed);
            }

            new AlertDialog.Builder(Campaigns.this)
                    .setMessage(message)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.ok_upper), null)
                    .create()
                    .show();
        }

        @Override
        protected void doWebOperation() throws Exception {
            if (forceSearch) {
                cache.clearCampaigns();
            }

            super.doWebOperation();
        }

        @Override
        protected void onFinish() {
            super.onFinish();
            pullToRefreshView.onRefreshComplete();

            if (currentVersionOutdated) {
                popupView.setVisibility(View.VISIBLE);
            }
            else {
                popupView.setVisibility(View.GONE);
            }
        }

        protected void setForceSearch(boolean force) {
            this.forceSearch = force;
        }
    }

    private class CampaignListAdapter extends ArrayAdapter<Campaign> {

        private boolean itemOpenState[];

        public CampaignListAdapter(Context context, List<Campaign> objects){
            super(context, android.R.layout.simple_list_item_1, objects);

            this.itemOpenState = new boolean[objects.size()];
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.campaign_row, null);
            }

            Campaign campaign = getItem(position);

            v.setBackgroundColor(Color.parseColor(campaign.getBackgroundColor()));

            ImageView bgImageView = (ImageView) v.findViewById(R.id.background);
            if(campaign.getBackgroundUrl()!=null) {
                imageLoader.displayImage(campaign.getBackgroundUrl(), bgImageView);
            } else {
                bgImageView.setImageDrawable(null);
            }

            ImageView imageView = (ImageView) v.findViewById(R.id.image);
            ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
            imageLoader.displayImage(campaign.getLogoUrl(), imageView, new ProgressBarImageLoadingListener(progressBar));

            Calendar cal = Calendar.getInstance();
            Date todayDate = cal.getTime();
            if (itemOpenState[position] != true && todayDate.after(campaign.getEndDate())) {
                // Semi-hide past campaigns
                itemOpenState[position] = false;
                int closedHeight = getResources().getDimensionPixelSize(R.dimen.campaign_row_height_closed);
                if (v.getLayoutParams() != null)
                    v.getLayoutParams().height = closedHeight;
                else {
                    v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, closedHeight));
                }
            }
            else {
                itemOpenState[position] = true;
                int openHeight = getResources().getDimensionPixelSize(R.dimen.campaign_row_height_open);
                if (v.getLayoutParams() != null) {
                    v.getLayoutParams().height = openHeight;
                }
                else {
                    v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, openHeight));
                }
            }

            TextView textView = (TextView) v.findViewById(R.id.callout);
            if(campaign.getCallout() != null && campaign.getCallout().length() > 0) {
                textView.setText(campaign.getCallout());
                textView.setTypeface(calloutTypeface);
                textView.setVisibility(TextView.VISIBLE);

                // Change text color and background color if it's a past campaign
                if (todayDate.after(campaign.getEndDate())) {
                    // TODO: for past campaigns, maybe also set "Past Campaigns" text, even if none was set
                    int bgColor = getResources().getColor(R.color.campaigns_past_campaign_callout_background);
                    textView.setBackgroundColor(bgColor);
                    int textColor = getResources().getColor(R.color.campaigns_past_campaign_callout_text);
                    textView.setTextColor(textColor);
                }
                else {
                    int bgColor = getResources().getColor(R.color.campaigns_callout_background);
                    textView.setBackgroundColor(bgColor);
                    int textColor = getResources().getColor(R.color.campaigns_callout_text);
                    textView.setTextColor(textColor);
                }
            }
            else {
                // we use GONE instead of INVISIBLE because we dont want it to leave a blank space
                textView.setVisibility(TextView.GONE);
            }

            ImageView imageCause = (ImageView) v.findViewById(R.id.cause_tag);
            imageCause.setVisibility(ImageView.GONE);	// GONE by default

            int[] tags = campaign.getCauseTags();
            if(tags != null && tags.length > 0) {
                boolean bValidTag = false;
                DSPreferences prefs = new DSPreferences(getApplicationContext());
                int[] userCauses = prefs.getCauses();

                for(int i=0; i<tags.length && !bValidTag; i++) {
                    for(int j=0; j<userCauses.length && !bValidTag; j++) {
                        if(tags[i] == userCauses[j] && getCauseDrawable(tags[i]) > 0) {
                            imageCause.setImageResource(getCauseDrawable(tags[i]));
                            imageCause.setVisibility(ImageView.VISIBLE);

                            bValidTag = true;
                            break;
                        }
                    }
                }
            }

            return v;
        }

        public void setItemOpenState(int position, boolean opened) {
            if (itemOpenState.length > position) {
                itemOpenState[position] = opened;
            }
        }

        private int getCauseDrawable(int cause_id) {
            if (cause_id == DSConstants.CAUSE_TAG.ANIMALS.getValue())
                return R.drawable.cause_animals_tag;
            else if (cause_id == DSConstants.CAUSE_TAG.BULLYING.getValue())
                return R.drawable.cause_bullying_tag;
            else if (cause_id == DSConstants.CAUSE_TAG.DISASTERS.getValue())
                return R.drawable.cause_disasters_tag;
            else if (cause_id == DSConstants.CAUSE_TAG.DISCRIMINATION.getValue())
                return R.drawable.cause_discrimination_tag;
            else if (cause_id == DSConstants.CAUSE_TAG.EDUCATION.getValue())
                return R.drawable.cause_education_tag;
            else if (cause_id == DSConstants.CAUSE_TAG.ENVIRONMENT.getValue())
                return R.drawable.cause_environment_tag;
            else if (cause_id == DSConstants.CAUSE_TAG.POVERTY.getValue())
                return R.drawable.cause_poverty_tag;
            else if (cause_id == DSConstants.CAUSE_TAG.HUMAN_RIGHTS.getValue())
                return R.drawable.cause_human_rights_tag;
            else if (cause_id == DSConstants.CAUSE_TAG.TROOPS.getValue())
                return R.drawable.cause_troops_tag;
            else if (cause_id == DSConstants.CAUSE_TAG.HEALTH.getValue())
                return R.drawable.cause_health_tag;
            else if (cause_id == DSConstants.CAUSE_TAG.RELATIONSHIPS.getValue())
                return R.drawable.cause_relationships_tag;
            else
                return -1;
        }

    }

}
