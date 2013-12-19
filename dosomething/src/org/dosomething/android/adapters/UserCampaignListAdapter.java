package org.dosomething.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.R;
import org.dosomething.android.animations.CardFlipAnimation;
import org.dosomething.android.animations.Rotate3dAnimation;
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

    // List of cached campaigns
    private List<Campaign> mCachedCampaigns;

    // Current context
    private Context mContext;

    // List position of the last item updated - used to determine if a new View
    // should be animated in.
    private int mLastItemUpdatePosition;

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

        // Set click listener for the close button
        LinearLayout closeButton = (LinearLayout)v.findViewById(R.id.preview_close);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // This seems sorta hacky. Must guarantee that this Button is two levels under
                // the containing row layout, and that the Layout is a FrameLayout
                CardFlipAnimation.animate(mContext, (FrameLayout) view.getParent().getParent(), true);
            }
        });

        UserCampaign campaign = getItem(position);

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

        // Apply animation when item comes onto the screen, but only when scrolling down
        if (position > mLastItemUpdatePosition) {
            Animation animRotate = new Rotate3dAnimation(0, 0, -15, 0, 0, 0);
            animRotate.setDuration(350);

            Animation animTranslate = new TranslateAnimation(0, 0, v.getHeight() / 2, 0);
            animTranslate.setDuration(400);

            AnimationSet animSet = new AnimationSet(false);
            animSet.addAnimation(animRotate);
            animSet.addAnimation(animTranslate);

            v.setAnimation(animSet);
        }

        mLastItemUpdatePosition = position;

        return v;
    }
}
