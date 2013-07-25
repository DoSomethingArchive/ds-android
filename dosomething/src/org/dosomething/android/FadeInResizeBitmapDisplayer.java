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

	private final int durationMillis;

	public FadeInResizeBitmapDisplayer(int durationMillis) {
		this.durationMillis = durationMillis;
	}
	
	@Override
	public Bitmap display(Bitmap bitmap, ImageView imageView) {
		// Original bitmap image size
		int bitmapWidth = bitmap.getWidth();
		int bitmapHeight = bitmap.getHeight();
		
		// Measured dimensions for the view
		int measuredWidth = imageView.getMeasuredWidth();
		int measuredHeight = imageView.getMeasuredHeight();
		
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
		
		// Create new Bitmap with rescaled dimensions
		Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
		imageView.setImageBitmap(scaledBitmap);

		// Execute animation for the fade-in
		animate(imageView, durationMillis);

		return scaledBitmap;
	}

	/**
	 * Animates {@link ImageView} with "fade-in" effect
	 *
	 * @param imageView      {@link ImageView} which display image in
	 * @param durationMillis The length of the animation in milliseconds
	 */
	public static void animate(ImageView imageView, int durationMillis) {
		AlphaAnimation fadeImage = new AlphaAnimation(0, 1);
		fadeImage.setDuration(durationMillis);
		fadeImage.setInterpolator(new DecelerateInterpolator());
		imageView.startAnimation(fadeImage);
	}
}
