package org.dosomething.android.activities;

import org.dosomething.android.R;
import org.dosomething.android.widget.CustomActionBar;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;

import roboguice.inject.InjectView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.inject.Inject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DecodingType;

/**
 * Activity for displaying an image from a campaign's gallery 
 */
public class GalleryImageItemDisplay extends AbstractActivity {

	private static final String GALLERY_IMG_URL = "gallery-img-url";
	
	@Inject LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.galleryImageItemDisplay) private ImageView imageView;
	
	@Override
	protected String getPageName() {
		return "campaign-gallery-image";
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallery_image_fullscreen);
		
		actionBar.addAction(Campaigns.getHomeAction(this));

		Bundle bundle = getIntent().getExtras();
		String imgurl = bundle.getString(GALLERY_IMG_URL);
		if (imgurl!=null && !imgurl.equals("")) {
			// make a DisplayImageOptions without cacheInMemory or cacheOnDisc, because the large image will hurt the cache
			DisplayImageOptions imageLoaderOptions = 
					new DisplayImageOptions.Builder()
						.decodingType(DecodingType.MEMORY_SAVING)
						.build();
			
	        imageLoader.displayImage(imgurl, imageView, imageLoaderOptions);
			ProgressBar progressBar = (ProgressBar)findViewById(R.id.galleryImageItemDisplayProgress);
			imageLoader.displayImage(imgurl, imageView, new ProgressBarImageLoadingListener(progressBar));
		}
		else {
			Toast.makeText(this, R.string.gallery_image_item_display_error, Toast.LENGTH_LONG);
		}
	}
}
