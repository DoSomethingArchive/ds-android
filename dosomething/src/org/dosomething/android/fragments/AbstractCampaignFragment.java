package org.dosomething.android.fragments;

import roboguice.fragment.RoboFragment;

/**
 * Abstract class for campaign sub-pages.
 */
public abstract class AbstractCampaignFragment extends RoboFragment {

    public abstract String getFragmentName();

    public void onResume() {
        super.onResume();
    }
}
