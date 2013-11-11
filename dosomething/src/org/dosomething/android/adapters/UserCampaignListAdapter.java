package org.dosomething.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.R;
import org.dosomething.android.cache.PersistentCampaignsCache;
import org.dosomething.android.domain.UserCampaign;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;

import java.util.Iterator;
import java.util.List;

/**
 * Adapter for displaying UserCampaign data in a ListView.
 */
public class UserCampaignListAdapter extends ArrayAdapter<UserCampaign> {

    @Inject @Named("DINComp-CondBold")Typeface typefaceDin;

    // List of cached campaigns
    private List<Campaign> mCachedCampaigns;

    // Current context
    private Context mContext;

    // True to show completed campaigns, False to show in progress campaigns
    private boolean mShowCompletedCampaigns;

    public UserCampaignListAdapter(Context context, List<UserCampaign> objects, boolean bShowCompleted) {
        super(context, android.R.layout.simple_expandable_list_item_1, objects);

        mContext = context;
        mShowCompletedCampaigns = bShowCompleted;

        try {
            mCachedCampaigns = new PersistentCampaignsCache(context).getCampaignsAsList();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            v = inflater.inflate(R.layout.user_campaign_row, null);
        }

        UserCampaign campaign = getItem(position);

        campaign.getCampaignId();
        campaign.getDateSignedUp();

        // Ensure front side of the item is visible
        View backside = v.findViewById(R.id.frame_backside);
        backside.setVisibility(View.INVISIBLE);
        View frontside = v.findViewById(R.id.frame);
        frontside.setVisibility(View.VISIBLE);

        if (mCachedCampaigns != null) {
            Iterator<Campaign> iter = mCachedCampaigns.iterator();
            while (iter.hasNext()) {
                Campaign cachedCampaign = iter.next();
                if (cachedCampaign.getId().equals(campaign.getCampaignId())) {
                    ImageView bgImageView = (ImageView) v.findViewById(R.id.background);
                    bgImageView.setImageDrawable(null);
                    bgImageView.setBackgroundColor(Color.parseColor(cachedCampaign.getBackgroundColor()));

                    ImageLoader imageLoader = ImageLoader.getInstance();

                    if (cachedCampaign.getBackgroundUrl() != null) {
                        imageLoader.displayImage(cachedCampaign.getBackgroundUrl(), bgImageView);
                    }

                    ImageView imageView = (ImageView) v.findViewById(R.id.image);
                    ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
                    imageLoader.displayImage(cachedCampaign.getLogoUrl(), imageView, new ProgressBarImageLoadingListener(progressBar));
                    break;
                }
            }
        }

        return v;
    }

    public int getCount() {
        return 1;
    }
}
