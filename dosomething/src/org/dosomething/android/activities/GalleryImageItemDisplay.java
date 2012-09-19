package org.dosomething.android.activities;

import org.dosomething.android.R;
import org.dosomething.android.widget.CustomActionBar;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
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

	private static final String CAMPAIGN = "campaign";
	private static final String GALLERY_IMG_URL = "gallery-img-url";
	
	@Inject LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	@InjectView(R.id.galleryImageItemDisplay) private ImageView imageView;
	
	private org.dosomething.android.transfer.Campaign campaign;
	private String imageURL;
	
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
		campaign = (org.dosomething.android.transfer.Campaign)bundle.get(CAMPAIGN);
		
		imageURL = bundle.getString(GALLERY_IMG_URL);
		if (imageURL!=null && !imageURL.equals("")) {
			// make a DisplayImageOptions without cacheInMemory or cacheOnDisc, because the large image will hurt the cache
			DisplayImageOptions imageLoaderOptions = 
					new DisplayImageOptions.Builder()
						.decodingType(DecodingType.MEMORY_SAVING)
						.build();
			
	        imageLoader.displayImage(imageURL, imageView, imageLoaderOptions);
			ProgressBar progressBar = (ProgressBar)findViewById(R.id.galleryImageItemDisplayProgress);
			imageLoader.displayImage(imageURL, imageView, new ProgressBarImageLoadingListener(progressBar));
		}
		else {
			Toast.makeText(this, R.string.gallery_image_item_display_error, Toast.LENGTH_LONG);
		}
		
		// Click listener on image
		if (imageView != null) {
			imageView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					Context ctx = v.getContext();
					Vibrator vibe = (Vibrator)ctx.getSystemService(Context.VIBRATOR_SERVICE);
					vibe.vibrate(20);
					
					startActivity(Intent.createChooser(getShareIntent(), getString(R.string.campaign_share_chooser)));
					return false;
				}
			});
		}
	}
	
	// Open Intent to share link to image
	private Intent getShareIntent() {
		String campaignName = campaign.getName();
		String shareTitle = getString(R.string.gallery_image_share_title, campaignName);
		
		Intent answer = new Intent(android.content.Intent.ACTION_SEND);
		answer.putExtra(android.content.Intent.EXTRA_SUBJECT, shareTitle);
		answer.putExtra(android.content.Intent.EXTRA_TEXT, imageURL);
		answer.setType("text/plain");
		return answer;
	}
}
