package org.dosomething.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dosomething.android.R;

/**
 * Campaign sub-page to give info and tools to do a campaign.
 */
public class CampaignDoFragment extends AbstractCampaignFragment {

    @Override
    public String getFragmentName() {
        return "Do-It";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_campaign_do, container, false);
        return rootView;
    }
}
