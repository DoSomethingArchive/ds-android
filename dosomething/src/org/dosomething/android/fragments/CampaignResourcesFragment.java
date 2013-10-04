package org.dosomething.android.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.activities.DSWebViewActivity;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.MoreInfo;
import org.dosomething.android.transfer.MoreInfoItem;
import org.dosomething.android.transfer.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Campaign sub-page for more resources.
 */
public class CampaignResourcesFragment extends AbstractCampaignFragment {

    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();

    private enum IRIType {
        MOREINFO,
        RESOURCE
    };

    @Inject private LayoutInflater inflater;
    @Inject private ImageLoader imageLoader;
    @Inject @Named("DINComp-CondBold")Typeface headerTypeface;

    private TextView introText;
    private ListView list;

    @Override
    public String getFragmentName() {
        return "Resources";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.campaign_resources, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        introText = (TextView)view.findViewById(R.id.intro);
        list = (ListView)view.findViewById(R.id.list);

        Bundle args = getArguments();
        Campaign campaign = (Campaign)args.getSerializable(CAMPAIGN);

        // Array holding the items to be displayed in the list
        List<InfoResourceItem> listItems = new ArrayList<InfoResourceItem>();

        // First listed are the More Info items, if any
        MoreInfo mi = campaign.getMoreInfo();
        if (mi != null && mi.getIntro() != null && mi.getIntro().length() > 0) {
            introText.setVisibility(View.VISIBLE);
            introText.setText(mi.getIntro());

            List<MoreInfoItem> infoItems = mi.getItems();
            for (int i = 0; i < infoItems.size(); i++) {
                MoreInfoItem miItem = infoItems.get(i);
                InfoResourceItem irItem = new InfoResourceItem(miItem);
                listItems.add(irItem);
            }
        }

        // Also appended to the list are Resource items, if any
        List<Resource> resources = campaign.getResources();
        if (resources != null) {
            for (int i = 0; i < resources.size(); i++) {
                Resource res = resources.get(i);
                InfoResourceItem irItem = new InfoResourceItem(res);
                listItems.add(irItem);
            }
        }

        list.setAdapter(new ResourceListAdapter(getActivity(), listItems));
        list.setOnItemClickListener(itemClickListener);
    }

    private final AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int position,
                                long id) {
            InfoResourceItem irItem = (InfoResourceItem) list.getAdapter().getItem(position);

            if (irItem.getType() == IRIType.RESOURCE) {
                Resource resource = irItem.getResource();

                if (resource.getLinkUrl() != null && resource.getLinkUrl().length() > 0) {
                    Intent i = new Intent(v.getContext(), DSWebViewActivity.class);
                    i.setData(Uri.parse(resource.getLinkUrl()));
                    startActivity(i);
                }
            }
        }
    };

    private class InfoResourceItem {

        private IRIType type;
        private MoreInfoItem moreInfo;
        private Resource resource;

        public InfoResourceItem(MoreInfoItem _moreInfo) {
            type = IRIType.MOREINFO;
            moreInfo = _moreInfo;
        }

        public InfoResourceItem(Resource _resource) {
            type = IRIType.RESOURCE;
            resource = _resource;
        }

        public IRIType getType() {
            return type;
        }

        public MoreInfoItem getMoreInfo() {
            return moreInfo;
        }

        public Resource getResource() {
            return resource;
        }
    }

    private class ResourceListAdapter extends ArrayAdapter<InfoResourceItem> {

        public ResourceListAdapter(Context context, List<InfoResourceItem> objects){
            super(context, android.R.layout.simple_list_item_1, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            InfoResourceItem irItem = getItem(position);

            View v = convertView;
            if (irItem.getType() == IRIType.MOREINFO) {
                v = inflater.inflate(R.layout.hib_row, null);

                MoreInfoItem mi = irItem.getMoreInfo();

                TextView header = (TextView)v.findViewById(R.id.header);
                header.setTypeface(headerTypeface, Typeface.BOLD);
                header.setText(mi.getHeader());

                DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                        .displayer(new FadeInBitmapDisplayer(DSConstants.IMAGE_LOADER_FADE_IN_TIME))
                        .build();
                ImageView image = (ImageView)v.findViewById(R.id.image);
                imageLoader.displayImage(mi.getImageUrl(), image, imageOptions);

                TextView body = (TextView)v.findViewById(R.id.body);
                body.setText(mi.getBody());
            }
            else if (irItem.getType() == IRIType.RESOURCE) {
                v = inflater.inflate(R.layout.resource_row, null);

                Resource resource = irItem.getResource();

                TextView body = (TextView)v.findViewById(R.id.body);
                body.setText(resource.getBody());
            }

            return v;
        }

        @Override
        public boolean isEnabled(int position) {
            InfoResourceItem irItem = getItem(position);

            // Don't allow the "More Info" items to be clickable
            if (irItem.getType() == IRIType.MOREINFO)
                return false;
            else
                return true;
        }

    }
}
