package org.dosomething.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.dosomething.android.R;
import org.dosomething.android.adapters.UserCampaignListAdapter;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.domain.UserCampaign;

import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * UserCampaignsFragment
 * Displays a list of the campaigns a user is participating in.
 *
 * TODO items:
 * - get campaigns from the user_campaigns client-side db
 * - update adapter with campaigns found there
 * - setup click listener / backside of card shows campaign title & started date:
 *      - backside of Done campaigns: title & completed date:
 */
public class UserCampaignsFragment extends RoboFragment {

    @Inject private DSDao mDSDao;
    @Inject private UserContext mUserContext;

    // The list to display the campaigns in
    @InjectView(R.id.list) private ListView mListView;

    // Text to display if no campaigns are found
    @InjectView(R.id.empty_list) private TextView mEmptyListText;

    // True to show completed campaigns, False to show in progress campaigns
    private boolean mShowCompletedCampaigns;

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

                // TODO: Handle click events on the list
                //mListView.setOnItemClickListener(itemClickListener)

                mEmptyListText.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            }
            else {
                mEmptyListText.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);

                if (mShowCompletedCampaigns) {
                    mEmptyListText.setText(R.string.campaigns_tab_completed_none_found);
                }
                else {
                    mEmptyListText.setText(R.string.campaigns_tab_in_progress_none_found);
                }
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
