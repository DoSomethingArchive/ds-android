package org.dosomething.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dosomething.android.R;

/**
 * Campaign sub-page to start the campaign report back process.
 */
public class CampaignReportBackFragment extends AbstractCampaignFragment {

    @Override
    public String getFragmentName() {
        return "Report-Back";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_campaign_reportback, container, false);
        return rootView;
    }
}
