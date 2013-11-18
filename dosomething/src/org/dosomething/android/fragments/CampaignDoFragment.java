package org.dosomething.android.fragments;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.inject.Inject;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.dosomething.android.DSConstants;
import org.dosomething.android.FadeInResizeBitmapDisplayer;
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

    // Overlay view to display when fullsize image is displayed
    @InjectView(R.id.overlay) private View mOverlayView;

    // ImageViews for the thumbnailed gallery images
    private ImageView mGalleryThumb1;
    private ImageView mGalleryThumb2;
    private ImageView mGalleryThumb3;

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
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                int screenWidth = display.getWidth();

                ImageLoader imageLoader = ImageLoader.getInstance();
                DisplayImageOptions imageOptions = new DisplayImageOptions.Builder()
                        .displayer(new FadeInResizeBitmapDisplayer(DSConstants.IMAGE_LOADER_FADE_IN_TIME, screenWidth))
                        .build();
                imageLoader.displayImage(galleryData.getImageUrl1(), mGalleryImage1, imageOptions);
                imageLoader.displayImage(galleryData.getImageUrl2(), mGalleryImage2, imageOptions);
                imageLoader.displayImage(galleryData.getImageUrl3(), mGalleryImage3, imageOptions);

                // Set a listener on the ImageView thumbnails that were added to the view
                mGalleryThumb1 = (ImageView)mContentLayout.findViewById(R.id.galleryThumb1);
                mGalleryThumb2 = (ImageView)mContentLayout.findViewById(R.id.galleryThumb2);
                mGalleryThumb3 = (ImageView)mContentLayout.findViewById(R.id.galleryThumb3);

                mGalleryThumb1.setOnClickListener(this);
                mGalleryThumb2.setOnClickListener(this);
                mGalleryThumb3.setOnClickListener(this);
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
                zoomGalleryImage(mGalleryImage1);
                break;
            case R.id.galleryThumb2:
                zoomGalleryImage(mGalleryImage2);
                break;
            case R.id.galleryThumb3:
                zoomGalleryImage(mGalleryImage3);
                break;
        }
    }

    /**
     * Animate the display of the fullsize gallery image.
     *
     * @param zoomView ImageView to animate in
     */
    private void zoomGalleryImage(final ImageView zoomView) {
        // Cancel any animation in progress
        if (mCurrentZoomAnimator != null) {
            mCurrentZoomAnimator.cancel();
        }

        // Hide thumbnail and show zoomed-in view
        mOverlayView.setVisibility(View.VISIBLE);
        zoomView.setVisibility(View.VISIBLE);

        // Construct and run translation and scaling animations
        AnimatorSet animSet = new AnimatorSet();
        animSet.play(ObjectAnimator.ofFloat(zoomView, View.SCALE_X.getName(), 0.5f, 1f))
                .with(ObjectAnimator.ofFloat(zoomView, View.SCALE_Y.getName(), 0.5f, 1f));

        int animDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        animSet.setDuration(animDuration);
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

        // Zoom back down when zoomed-in image is clicked
        zoomView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentZoomAnimator != null) {
                    mCurrentZoomAnimator.cancel();
                }

                AnimatorSet animSet = new AnimatorSet();
                animSet.play(ObjectAnimator.ofFloat(zoomView, View.SCALE_X.getName(), 0.5f))
                        .with(ObjectAnimator.ofFloat(zoomView, View.SCALE_Y.getName(), 0.5f));
                int animDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
                animSet.setDuration(animDuration);
                animSet.setInterpolator(new AccelerateInterpolator() );
                animSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mOverlayView.setVisibility(View.GONE);
                        zoomView.setVisibility(View.GONE);
                        mCurrentZoomAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        mOverlayView.setVisibility(View.GONE);
                        zoomView.setVisibility(View.GONE);
                        mCurrentZoomAnimator = null;
                    }
                });

                animSet.start();
                mCurrentZoomAnimator = animSet;
            }
        });
    }
}
