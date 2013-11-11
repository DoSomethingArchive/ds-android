package org.dosomething.android.animations;

import android.app.Activity;
import android.view.Display;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

import org.dosomething.android.R;

/**
 * Execute a card flip animation on the given view.
 */
public class CardFlipAnimation {

    /**
     * Animates an item in the list to flip over.
     *
     * @param view View to execute animation on
     */
    public static void doCardFlipAnimation(Activity activity, View view, boolean reverse) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        ScaleAnimation anim = new ScaleAnimation(1f, 0f, 1f, 1f, display.getWidth() / 2, display.getHeight() / 2);
        anim.setDuration(175);
        anim.setAnimationListener(new CardFlipAnimationListener(activity, view, reverse));
        view.startAnimation(anim);
    }

    /**
     * Listener on card flip animation to trigger the second half of the animation.
     */
    private static class CardFlipAnimationListener implements Animation.AnimationListener {

        // Activity animation is called from
        private Activity mActivity;

        // The view the animation is being executed on
        private View mCardView;

        // Set to true if animation should flip from backside to front
        private boolean mReverseFlip;

        public CardFlipAnimationListener(Activity activity, View view, boolean reverse) {
            mActivity = activity;
            mCardView = view;
            mReverseFlip = reverse;
        }

        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        public void onAnimationEnd(Animation animation) {
            Display display = mActivity.getWindowManager().getDefaultDisplay();
            ScaleAnimation anim = new ScaleAnimation(0f, 1f, 1f, 1f, display.getWidth() / 2, display.getHeight() / 2);
            anim.setDuration(175);
            mCardView.startAnimation(anim);

            View cardBackside = mCardView.findViewById(R.id.frame_backside);
            View cardFrontside = mCardView.findViewById(R.id.frame);
            if (mReverseFlip) {
                if (cardBackside != null)
                    cardBackside.setVisibility(View.INVISIBLE);
                if (cardFrontside != null)
                    cardFrontside.setVisibility(View.VISIBLE);
            }
            else {
                if (cardBackside != null)
                    cardBackside.setVisibility(View.VISIBLE);
                if (cardFrontside != null)
                    cardFrontside.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}
    }
}
