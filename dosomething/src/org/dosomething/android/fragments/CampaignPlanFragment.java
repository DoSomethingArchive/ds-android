package org.dosomething.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.ICampaignSectionData;

import java.util.Iterator;
import java.util.List;

/**
 * Campaign sub-page providing info to prepare for a campaign.
 */
public class CampaignPlanFragment extends AbstractCampaignFragment {

    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();

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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout layout = (LinearLayout)view.findViewById(R.id.content);

        Bundle args = getArguments();
        Campaign campaign = (Campaign)args.getSerializable(CAMPAIGN);

        List<ICampaignSectionData> data = campaign.getPlanData();
        Iterator<ICampaignSectionData> iter = data.iterator();
        while (iter.hasNext()) {
            ICampaignSectionData sectionData = iter.next();
            sectionData.addToView(getActivity(), layout);
        }
    }
}
