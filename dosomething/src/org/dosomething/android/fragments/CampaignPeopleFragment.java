package org.dosomething.android.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.DSConstants;
import org.dosomething.android.FadeInResizeBitmapDisplayer;
import org.dosomething.android.R;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.People;
import org.dosomething.android.transfer.PeopleItem;

import java.util.List;

/**
 * Campaign sub-page for people information.
 */
public class CampaignPeopleFragment extends AbstractCampaignFragment {

    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();

    @Inject private LayoutInflater inflater;
    @Inject private ImageLoader imageLoader;
    @Inject @Named("ProximaNova-Bold")Typeface headerTypeface;

    private TextView introText;
    private ListView list;

    @Override
    public String getFragmentName() {
        return "People";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.campaign_people, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        Campaign campaign = (Campaign)args.getSerializable(CAMPAIGN);

        introText = (TextView)view.findViewById(R.id.intro);
        list = (ListView)view.findViewById(R.id.list);

        People people = campaign.getPeople();
        if (people != null) {
            introText.setText(people.getIntro());

            List<PeopleItem> peopleItems = people.getItems();
            list.setAdapter(new PeopleListAdapter(getActivity(), peopleItems));
        }
    }

    private class PeopleListAdapter extends ArrayAdapter<PeopleItem> {
        public PeopleListAdapter(Context context, List<PeopleItem> objects) {
            super(context, android.R.layout.simple_list_item_1, objects);
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = inflater.inflate(R.layout.hib_row, null);
            }

            PeopleItem peopleItem = getItem(position);

            TextView header = (TextView)v.findViewById(R.id.header);
            header.setTypeface(headerTypeface, Typeface.BOLD);
            header.setText(peopleItem.getHeader());

            DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                    .displayer(new FadeInResizeBitmapDisplayer(DSConstants.IMAGE_LOADER_FADE_IN_TIME))
                    .build();
            ImageView image = (ImageView)v.findViewById(R.id.image);
            imageLoader.displayImage(peopleItem.getImageUrl(), image, imageOptions);

            TextView body = (TextView)v.findViewById(R.id.body);
            body.setText(peopleItem.getBody());

            return v;
        }
    }
}
