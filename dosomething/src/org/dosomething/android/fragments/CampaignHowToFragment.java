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
import org.dosomething.android.transfer.HowTo;

import java.util.List;

/**
 * Campaign sub-page for information on How To participate in a campaign.
 */
public class CampaignHowToFragment extends AbstractCampaignFragment {

    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();

    @Inject private LayoutInflater inflater;
    @Inject private ImageLoader imageLoader;
    @Inject @Named("DINComp-CondBold")Typeface headerTypeface;

    private ListView list;

    @Override
    public String getFragmentName() {
        return "How-To";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        super.onCreateView(inflater, container, savedInstance);

        View rootView = inflater.inflate(R.layout.campaign_how_to, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = (ListView)view.findViewById(R.id.list);

        Bundle args = getArguments();
        Campaign campaign = (Campaign)args.getSerializable(CAMPAIGN);

        list.setAdapter(new MyAdapter(getActivity(), campaign.getHowTos()));
    }

    private class MyAdapter extends ArrayAdapter<HowTo> {

        public MyAdapter(Context context, List<HowTo> objects){
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

            HowTo howTo = getItem(position);

            TextView header = (TextView)v.findViewById(R.id.header);
            header.setTypeface(headerTypeface, Typeface.BOLD);
            header.setText(howTo.getHeader());

            ImageView image = (ImageView)v.findViewById(R.id.image);
            DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                    .displayer(new FadeInResizeBitmapDisplayer(DSConstants.IMAGE_LOADER_FADE_IN_TIME))
                    .build();
            imageLoader.displayImage(howTo.getImageUrl(), image, imageOptions);

            TextView body = (TextView)v.findViewById(R.id.body);
            body.setText(howTo.getBody());

            return v;
        }

    }
}
