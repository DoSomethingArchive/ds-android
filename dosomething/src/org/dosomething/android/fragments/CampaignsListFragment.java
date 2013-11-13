package org.dosomething.android.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.animations.CardFlipAnimation;
import org.dosomething.android.animations.Rotate3dAnimation;
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

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Fragment to display campaigns in a ListView.
 */
public class CampaignsListFragment extends RoboFragment {

    @Inject @Named("DINComp-CondBold")Typeface typefaceDin;

    @Inject private LayoutInflater inflater;
    @Inject private ImageLoader imageLoader;
    @Inject private UserContext userContext;
    @Inject private Cache cache;

    @InjectView(R.id.list) private PullToRefreshListView pullToRefreshView;
    @InjectView(R.id.popup) private RelativeLayout popupView;
    @InjectView(R.id.popupMsg) private TextView popupMsgView;
    @InjectView(R.id.popupClose) private Button popupCloseButton;

    private ListView list;

    private CampaignsTask mCampaignsTask;

    private final AdapterView.OnItemClickListener itemClickListener = new CampaignItemClickListener();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_campaigns_list, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        fetchCampaigns(false);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Cancel the task in progress since we're leaving this fragment.
        if (mCampaignsTask != null) {
            mCampaignsTask.cancel(true);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        list = pullToRefreshView.getRefreshableView();
        pullToRefreshView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                fetchCampaigns(true);
            }
        });

        // Setup the popup section to notify when app update is available
        popupMsgView.setTypeface(typefaceDin);
        // Upgrade notification popup click listeners
        popupMsgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open link to the Google Play Store
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.dosomething.android")));
            }
        });
        popupCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Executes task to fetch the list of campaign.
     *
     * @param forceSearch Set to true if data should be forced to retrieve data
     *                    from server instead of local cache even if the refresh
     *                    time limit hasn't been reached yet.
     */
    private void fetchCampaigns(boolean forceSearch) {
        // Only allow a single task to be executed at a time
        if (mCampaignsTask == null || !mCampaignsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            mCampaignsTask = new CampaignsTask();
            mCampaignsTask.setForceSearch(forceSearch);
            mCampaignsTask.execute();
        }
    }

    /**
     * Click listener for the list.
     */
    private class CampaignItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Campaign campaign = (Campaign) list.getAdapter().getItem(position);

            View cardBackside = view.findViewById(R.id.frame_backside);
            if (cardBackside != null && cardBackside.getVisibility() == View.INVISIBLE) {
                TextView title = (TextView)view.findViewById(R.id.preview_title);
                title.setText(campaign.getName());
                title.setTypeface(typefaceDin);

                TextView body = (TextView)view.findViewById(R.id.preview_body);
                body.setText(campaign.getTeaser());
                body.setTypeface(typefaceDin);

                CardFlipAnimation.animate(getActivity(), view, false);
            }
            else {
                startActivity(org.dosomething.android.activities.Campaign.getIntent(getActivity(), campaign));
            }
        }
    };

    private class CampaignsTask extends AbstractFetchCampaignsTask {

        private boolean currentVersionOutdated = false;
        private boolean forceSearch;

        public CampaignsTask() {
            super(getActivity(), userContext, cache);

            forceSearch = false;
        }

        @Override
        protected void onSuccess() {
            // Don't display campaigns that require a higher version than what we have
            int version = 0;
            try {
                PackageInfo pInfo = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0);
                version = pInfo.versionCode;
            }
            catch (PackageManager.NameNotFoundException e) {
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

            // Adapter to display the list items
            CampaignListAdapter listAdapter = new CampaignListAdapter(getActivity(), campaigns);
            list.setAdapter(listAdapter);

            // Handle click events
            list.setOnItemClickListener(itemClickListener);
        }

        @Override
        protected void onError(Exception e) {
            String message;
            if(e instanceof NoInternetException) {
                message = getString(R.string.campaigns_no_internet);
            } else {
                message = getString(R.string.campaigns_failed);
            }

            new AlertDialog.Builder(getActivity())
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

        private int lastItemUpdatePosition;

        public CampaignListAdapter(Context context, List<Campaign> objects) {
            super(context, android.R.layout.simple_expandable_list_item_1, objects);

            this.lastItemUpdatePosition = 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.campaign_row, null);

                // Set click listener for the close button
                Button closeButton = (Button)v.findViewById(R.id.preview_close);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // This seems sorta hacky. Must guarantee that this Button is two levels under
                        // the containing row layout, and that the Layout is a FrameLayout
                        CardFlipAnimation.animate(getActivity(), (FrameLayout)view.getParent().getParent(), true);
                    }
                });
            }

            // Ensure front side of the item is visible
            View backside = v.findViewById(R.id.frame_backside);
            backside.setVisibility(View.INVISIBLE);
            View frontside = v.findViewById(R.id.frame);
            frontside.setVisibility(View.VISIBLE);

            Campaign campaign = getItem(position);

            ImageView bgImageView = (ImageView) v.findViewById(R.id.background);
            bgImageView.setImageDrawable(null);
            bgImageView.setBackgroundColor(Color.parseColor(campaign.getBackgroundColor()));

            if (campaign.getBackgroundUrl() != null) {
                imageLoader.displayImage(campaign.getBackgroundUrl(), bgImageView);
            }

            ImageView imageView = (ImageView) v.findViewById(R.id.image);
            ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
            imageLoader.displayImage(campaign.getLogoUrl(), imageView, new ProgressBarImageLoadingListener(progressBar));

            Calendar cal = Calendar.getInstance();
            Date todayDate = cal.getTime();
            int openHeight = getResources().getDimensionPixelSize(R.dimen.campaign_row_height_open);
            if (v.getLayoutParams() != null) {
                v.getLayoutParams().height = openHeight;
            }
            else {
                v.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, openHeight));
            }

            TextView textView = (TextView) v.findViewById(R.id.callout);
            if(campaign.getCallout() != null && campaign.getCallout().length() > 0) {
                textView.setText(campaign.getCallout());
                textView.setTypeface(typefaceDin);
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
                DSPreferences prefs = new DSPreferences(getActivity());
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

            // Apply animation when item comes onto the screen, but only when scrolling down
            if (position > lastItemUpdatePosition) {
                Animation animRotate = new Rotate3dAnimation(0, 0, -15, 0, 0, 0);
                animRotate.setDuration(350);

                Animation animTranslate = new TranslateAnimation(0, 0, v.getHeight() / 2, 0);
                animTranslate.setDuration(400);

                AnimationSet animSet = new AnimationSet(false);
                animSet.addAnimation(animRotate);
                animSet.addAnimation(animTranslate);

                v.setAnimation(animSet);
            }

            lastItemUpdatePosition = position;

            return v;
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
