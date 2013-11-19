package org.dosomething.android;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.display.BitmapDisplayer;

/**
 * Use fade-in animation when displaying the image, and resize to fit the ImageView's
 * measured bounds while maintaining the image's aspect ratio.
 */
public class FadeInResizeBitmapDisplayer implements BitmapDisplayer {

    // Duration of the fade in
    private final int durationMillis;

    // A preset width for the ImageView - particularly useful if the View is View.GONE
    // and doesn't have a measured width.
    private final int overrideWidth;

    public FadeInResizeBitmapDisplayer(int durationMillis) {
        this.durationMillis = durationMillis;
        this.overrideWidth = 0;
    }

    public FadeInResizeBitmapDisplayer(int durationMillis, int overrideWidth) {
        this.durationMillis = durationMillis;
        this.overrideWidth = overrideWidth;
    }

    @Override
    public Bitmap display(Bitmap bitmap, ImageView imageView) {
        // Original bitmap image size
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        int measuredWidth = 0;
        int measuredHeight = 0;
        if (overrideWidth > 0) {
            // If given an override, use it
            measuredWidth = overrideWidth;
        }
        else {
            // Measured dimensions for the view
            measuredWidth = imageView.getMeasuredWidth();
            measuredHeight = imageView.getMeasuredHeight();
        }

        float scaleX = (float)measuredWidth / (float)bitmapWidth;
        float scaleY = (float)measuredHeight / (float)bitmapHeight;

        float scale = 1.f;
        // If image needs to be scaled down, use the lesser value
        if ((scaleX < 1.f && scaleX > 0) || (scaleY < 1.f && scaleY > 0)) {
            if (scaleX <= scaleY && scaleX > 0) {
                scale = scaleX;
            }
            else if (scaleY > 0) {
                scale = scaleY;
            }
        }
        // If image needs to be scaled up, use the greater value
        else {
            if (scaleX >= scaleY) {
                scale = scaleX;
            }
            else {
                scale = scaleY;

            }
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        if (bitmapWidth > 0 && bitmapHeight > 0) {
            try {
                // Create new Bitmap with rescaled dimensions
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
                imageView.setImageBitmap(bitmap);
            }
            catch (IllegalArgumentException e) {
                // Not sure how this is happening, but bitmapWidth and bitmapHeight will be > 0 and
                // createBitmap() will still sometimes complain that width and height are not > 0.
                e.printStackTrace();
            }
        }

        // Execute animation for the fade-in
        animate(imageView, durationMillis);

        return bitmap;
    }

    /**
     * Animates {@link ImageView} with "fade-in" effect
     *
     * @param imageView      {@link ImageView} which display image in
     * @param durationMillis The length of the animation in milliseconds
     */
    public static void animate(ImageView imageView, int durationMillis) {
        AlphaAnimation fadeImage = new AlphaAnimation(0.5f, 1);
        fadeImage.setDuration(durationMillis);
        fadeImage.setInterpolator(new DecelerateInterpolator());
        imageView.startAnimation(fadeImage);
    }
}
