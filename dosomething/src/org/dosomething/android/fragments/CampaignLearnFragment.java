package org.dosomething.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dosomething.android.R;

/**
 * Campaign sub-page to Learn how to participate in the campaign.
 */
public class CampaignLearnFragment extends AbstractCampaignFragment {

    @Override
    public String getFragmentName() {
        return "Learn";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_campaign_learn, container, false);
        return rootView;
    }
}
