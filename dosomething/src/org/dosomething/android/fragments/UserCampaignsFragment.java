package org.dosomething.android.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.dosomething.android.R;
import org.dosomething.android.adapters.UserCampaignListAdapter;
import org.dosomething.android.animations.CardFlipAnimation;
import org.dosomething.android.cache.DSPreferences;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.transfer.Campaign;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * UserCampaignsFragment
 * Displays a list of the campaigns a user is participating in.
 */
public class UserCampaignsFragment extends RoboFragment {

    @Inject private DSDao mDSDao;
    @Inject @Named("ProximaNova-Bold") private Typeface mTypefaceProximaBold;
    @Inject @Named("ProximaNova-Reg") private Typeface mTypefaceProximaReg;
    @Inject private UserContext mUserContext;

    // The list to display the campaigns in
    @InjectView(R.id.list) private ListView mListView;

    // Content to display if no campaigns are found
    @InjectView(R.id.empty_view) private ScrollView mEmptyView;
    @InjectView(R.id.empty_view_header) private TextView mEmptyViewHeader;
    @InjectView(R.id.empty_view_body) private TextView mEmptyViewBody;

    // True to show completed campaigns, False to show in progress campaigns
    private boolean mShowCompletedCampaigns;

    // Click listener for the list items
    private final AdapterView.OnItemClickListener itemClickListener = new CampaignItemClickListener();

    /**
     * UserCampaignsFragment contstructor. Defaults to show in-progress campaigns.
     */
    public UserCampaignsFragment() {
        mShowCompletedCampaigns = false;
    }

    /**
     * UserCampaignsFragment constructor for displaying a list of the campaigns a user
     * is participating in.
     *
     * @param bShowCompleted True if this fragment should show completed campaigns, False if
     *                       it should just show in progress campaigns.
     */
    public UserCampaignsFragment(boolean bShowCompleted) {
        mShowCompletedCampaigns = bShowCompleted;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_user_campaigns_list, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Get campaigns from the user_campaigns client-side db
        if (mUserContext.isLoggedIn()) {
            List<UserCampaign> userCampaigns = mDSDao.findUserCampaigns(mUserContext.getUserUid(), mShowCompletedCampaigns);
            if (userCampaigns.size() > 0) {
                UserCampaignListAdapter listAadpter = new UserCampaignListAdapter(getActivity(), userCampaigns, mShowCompletedCampaigns);
                mListView.setAdapter(listAadpter);

                // Handle click events on the list
                mListView.setOnItemClickListener(itemClickListener);

                mEmptyView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            }
            // If no campaigns are found, show the empty data view
            else {
                mEmptyView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);

                if (mShowCompletedCampaigns) {
                    mEmptyViewHeader.setText(R.string.campaigns_tab_completed_empty_header);
                    mEmptyViewBody.setText(R.string.campaigns_tab_completed_none_found);
                }
                else {
                    mEmptyViewHeader.setText(R.string.campaigns_tab_in_progress_empty_header);
                    mEmptyViewBody.setText(R.string.campaigns_tab_in_progress_none_found);
                }

                mEmptyViewHeader.setTypeface(mTypefaceProximaBold);
                mEmptyViewBody.setTypeface(mTypefaceProximaReg);
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Click listener for the list.
     */
    private class CampaignItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            UserCampaign userCampaign = (UserCampaign) mListView.getAdapter().getItem(position);

            View cardBackside = view.findViewById(R.id.frame_backside);
            if (cardBackside != null && cardBackside.getVisibility() == View.INVISIBLE) {
                TextView title = (TextView)view.findViewById(R.id.preview_title);
                title.setText(userCampaign.getCampaignName());
                title.setTypeface(mTypefaceProximaReg);

                TextView body = (TextView)view.findViewById(R.id.preview_body);
                Date date = new Date();
                String bodyText = "";
                if (mShowCompletedCampaigns) {
                    date.setTime(userCampaign.getDateCompleted().longValue() * 1000);
                    bodyText = "Completed: " + DateFormat.format("MM/dd/yyyy", date);
                }
                else {
                    date.setTime(userCampaign.getDateSignedUp().longValue() * 1000);
                    bodyText = "Signed up: " + DateFormat.format("MM/dd/yyyy", date);

                    // @todo Hiding the steps completed data until new 2014 campaign content is added
                    /*
                    DSDao dsDao = new DSDao(getActivity());
                    int[] completedSteps = dsDao.getCampaignStepsCompleted(mUserContext.getUserUid(), userCampaign.getCampaignId());
                    bodyText += "\n\nSteps Completed: ";
                    if (completedSteps != null && completedSteps.length > 0) {
                        for (int i = 0; i < completedSteps.length; i++) {
                            bodyText += completedSteps[i];
                            if (i < completedSteps.length - 1) {
                                bodyText += ", ";
                            }
                        }
                    }
                    else {
                        bodyText += "none";
                    }
                    */
                }
                body.setText(bodyText);
                body.setTypeface(mTypefaceProximaReg);

                CardFlipAnimation.animate(getActivity(), view, false);
            }
            // Open up campaign screen if it's one that's still in progress
            else if (!mShowCompletedCampaigns) {
                try {
                    List<Campaign> cachedCampaigns = new DSPreferences(getActivity()).getCampaignsAsList();

                    Iterator<Campaign> iter = cachedCampaigns.iterator();
                    while (iter.hasNext()) {
                        Campaign campaign = iter.next();
                        if (campaign.getId().equals(userCampaign.getCampaignId())) {
                            startActivity(org.dosomething.android.activities.Campaign.getIntent(getActivity(), campaign));
                            return;
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
