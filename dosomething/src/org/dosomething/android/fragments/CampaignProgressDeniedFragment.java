package org.dosomething.android.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.dosomething.android.R;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * Fragment for the CampaignPagerAdapter to prompt user to sign up in order to progress
 * and participate in the campaign.
 */
public class CampaignProgressDeniedFragment extends RoboFragment {

    private Typeface mTypefaceLEngineer;

    @InjectView(R.id.progress_denied_text)TextView mDeniedText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        mTypefaceLEngineer = Typeface.createFromAsset(getActivity().getAssets(), "LEngineer-Regular.otf");

        View rootView = inflater.inflate(R.layout.fragment_campaign_progress_denied, container, false);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mDeniedText.setTypeface(mTypefaceLEngineer);
    }
}
