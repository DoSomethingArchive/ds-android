package org.dosomething.android.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.Faq;

import java.util.List;

/**
 * Campaign sub-page for FAQs.
 */
public class CampaignFaqFragment extends AbstractCampaignFragment {

    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();

    @Inject private LayoutInflater inflater;
    @Inject @Named("DINComp-CondBold")Typeface headerTypeface;

    private ExpandableListView list;

    @Override
    public String getFragmentName() {
        return "FAQ";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.campaign_faq, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        Campaign campaign = (Campaign)args.getSerializable(CAMPAIGN);

        list = (ExpandableListView)view.findViewById(R.id.list);
        list.setAdapter(new FaqListAdapter(campaign.getFaqs()));
    }

    private class FaqListAdapter extends BaseExpandableListAdapter {

        private List<Faq> faqs;

        public FaqListAdapter(List<Faq> faqs) {
            this.faqs = faqs;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return faqs.get(groupPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.faq_row_child, null);
            }

            Faq faq = (Faq) getGroup(groupPosition);

            TextView body = (TextView)v.findViewById(R.id.body);
            body.setText(faq.getBody());

            return v;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return faqs.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return faqs.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.faq_row, null);
            }

            Faq faq = (Faq) getGroup(groupPosition);

            TextView header = (TextView)v.findViewById(R.id.header);
            header.setTypeface(headerTypeface, Typeface.BOLD);
            header.setText(faq.getHeader());

            return v;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

    }
}
