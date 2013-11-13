package org.dosomething.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dosomething.android.R;

/**
 * Campaign sub-page providing info to prepare for a campaign.
 */
public class CampaignPlanFragment extends AbstractCampaignFragment {

    @Override
    public String getFragmentName() {
        return "Plan";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_campaign_plan, container, false);
        return rootView;
    }
}
