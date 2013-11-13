package org.dosomething.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dosomething.android.R;

import roboguice.fragment.RoboFragment;

/**
 * Fragment for the CampaignPagerAdapter to prompt user to sign up in order to progress
 * and participate in the campaign.
 */
public class CampaignProgressDeniedFragment extends RoboFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_campaign_progress_denied, container, false);
        return rootView;
    }
}
