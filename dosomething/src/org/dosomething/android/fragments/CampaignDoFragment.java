package org.dosomething.android.fragments;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.inject.Inject;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.DSConstants;
import org.dosomething.android.R;
import org.dosomething.android.context.UserContext;
import org.dosomething.android.dao.DSDao;
import org.dosomething.android.transfer.Campaign;
import org.dosomething.android.transfer.CampaignGalleryData;
import org.dosomething.android.transfer.ICampaignSectionData;

import java.util.Iterator;
import java.util.List;

import roboguice.inject.InjectView;

/**
 * Campaign sub-page to give info and tools to do a campaign.
 */
public class CampaignDoFragment extends AbstractCampaignFragment implements View.OnClickListener {

    private static final String CAMPAIGN = DSConstants.EXTRAS_KEY.CAMPAIGN.getValue();
    private static final String GALLERY_IMG_AUTHORS = "gallery-img-authors";
    private static final String GALLERY_NUM_ITEMS = "gallery-num-items";
    private static final String GALLERY_IMG_POS = "gallery-img-pos";
    private static final String GALLERY_IMG_URLS = "gallery-img-urls";
    private final int STEP_NUMBER = 2;

    // Button to mark this step as being completed
    @InjectView(R.id.btn_did_this) private Button mButtonDidThis;

    // Layout container for dynamic page content
    @InjectView(R.id.content) private LinearLayout mContentLayout;

    // ImageViews for the zoomed in gallery images
    @InjectView(R.id.galleryImage1) private ImageView mGalleryImage1;
    @InjectView(R.id.galleryImage2) private ImageView mGalleryImage2;
    @InjectView(R.id.galleryImage3) private ImageView mGalleryImage3;

    // User Context
    @Inject private UserContext mUserContext;

    // TODO
    private AnimatorSet mCurrentZoomAnimator;

    // Campaign data
    private Campaign mCampaign;

    @Override
    public String getFragmentName() {
        return "Do-It";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_campaign_do, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        mCampaign = (Campaign)args.getSerializable(CAMPAIGN);

        // Populate content container
        List<ICampaignSectionData> data = mCampaign.getDoItData();
        Iterator<ICampaignSectionData> iter = data.iterator();
        while (iter.hasNext()) {
            ICampaignSectionData sectionData = iter.next();
            sectionData.addToView(getActivity(), mContentLayout);

            if (sectionData instanceof CampaignGalleryData) {
                CampaignGalleryData galleryData = (CampaignGalleryData)sectionData;

                // Load images into the fuller size ImageViews
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(galleryData.getImageUrl1(), mGalleryImage1);
                imageLoader.displayImage(galleryData.getImageUrl2(), mGalleryImage2);
                imageLoader.displayImage(galleryData.getImageUrl3(), mGalleryImage3);

                // Set a listener on the ImageView thumbnails that were added to the view
                ImageView thumb1 = (ImageView)mContentLayout.findViewById(R.id.galleryThumb1);
                ImageView thumb2 = (ImageView)mContentLayout.findViewById(R.id.galleryThumb2);
                ImageView thumb3 = (ImageView)mContentLayout.findViewById(R.id.galleryThumb3);

                thumb1.setOnClickListener(this);
                thumb2.setOnClickListener(this);
                thumb3.setOnClickListener(this);
            }
        }

        // Set style and behavior for the Did This button
        Typeface typeface = Typeface.create("DINComp-CondBold", Typeface.BOLD);
        mButtonDidThis.setTypeface(typeface);
        mButtonDidThis.setOnClickListener(this);

        Activity activity = getActivity();
        DSDao dsDao = new DSDao(activity);
        UserContext userContext = new UserContext(activity);
        boolean isStepComplete = dsDao.isCampaignStepComplete(userContext.getUserUid(), mCampaign.getId(), STEP_NUMBER);
        mButtonDidThis.setEnabled(!isStepComplete);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.btn_did_this:
                // Mark this step as being completed
                Activity activity = getActivity();
                DSDao dsDao = new DSDao(activity);
                UserContext userContext = new UserContext(activity);
                dsDao.setCampaignStepCompleted(userContext.getUserUid(), mCampaign.getId(), STEP_NUMBER);

                // Disable button
                view.setEnabled(false);
                break;
            case R.id.galleryThumb1:
                zoomImageFromThumb((ImageView) view, mGalleryImage1);
                break;
            case R.id.galleryThumb2:
                zoomImageFromThumb((ImageView) view, mGalleryImage2);
                break;
            case R.id.galleryThumb3:
                zoomImageFromThumb((ImageView) view, mGalleryImage3);
                break;
        }
    }

    private void zoomImageFromThumb(final ImageView thumbView, ImageView zoomView) {
        // Cancel any animation in progress
        if (mCurrentZoomAnimator != null) {
            mCurrentZoomAnimator.cancel();
        }

        // Calculate start and end bounds for the zoomed-in image
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        thumbView.getGlobalVisibleRect(startBounds);
        getActivity().findViewById(R.id.container).getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust start bounds to the same aspect ratio of the final bounds
        float startScale;
        if ((float)finalBounds.width() / finalBounds.height() > (float)startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float)startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        }
        else {
            // Extend start bounds vertically
            startScale = (float)startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide thumbnail and show zoomed-in view
        thumbView.setVisibility(View.INVISIBLE);
        zoomView.setVisibility(View.VISIBLE);

        // Construct and run translation and scaling animations
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(ObjectAnimator.ofFloat(zoomView, View.X.getName(), startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(zoomView, View.Y.getName(), startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(zoomView, View.SCALE_X.getName(), startScale, 1f))
                .with(ObjectAnimator.ofFloat(zoomView, View.SCALE_Y.getName(), startScale, 1f));
        animSet.setDuration(2000);
        animSet.setInterpolator(new DecelerateInterpolator());
        animSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentZoomAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentZoomAnimator = null;
            }
        });
        animSet.start();
        mCurrentZoomAnimator = animSet;

    }
}
