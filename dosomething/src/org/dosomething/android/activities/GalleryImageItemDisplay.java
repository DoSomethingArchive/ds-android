package org.dosomething.android.activities;

import java.util.HashMap;

import org.dosomething.android.R;
import org.dosomething.android.analytics.Analytics;
import org.dosomething.android.widget.CustomActionBar;
import org.dosomething.android.widget.ProgressBarImageLoadingListener;

import roboguice.inject.InjectView;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;

/**
 * Activity for displaying an image from a campaign's gallery 
 */
public class GalleryImageItemDisplay extends AbstractActivity {

	private static final String CAMPAIGN = "campaign";
	private static final String GALLERY_IMG_AUTHORS = "gallery-img-authors";
	private static final String GALLERY_NUM_ITEMS = "gallery-num-items";
	private static final String GALLERY_IMG_POS = "gallery-img-pos";
	private static final String GALLERY_IMG_URLS = "gallery-img-urls";
	
	@Inject LayoutInflater inflater;
	@Inject private ImageLoader imageLoader;
	@InjectView(R.id.actionbar) private CustomActionBar actionBar;
	private ViewPager imageGalleryPager;
	
	private org.dosomething.android.transfer.Campaign campaign;
	private GalleryPagerAdapter imageGalleryPagerAdapter;
	private int imageGalleryPos;
	
	private String[] imageURLs;
	private String[] imageAuthors;
	
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
		imageGalleryPos = bundle.getInt(GALLERY_IMG_POS);
		imageURLs = bundle.getStringArray(GALLERY_IMG_URLS);
		imageAuthors = bundle.getStringArray(GALLERY_IMG_AUTHORS);
		int numItems = bundle.getInt(GALLERY_NUM_ITEMS);
		
		imageGalleryPagerAdapter = new GalleryPagerAdapter(this, numItems);
		imageGalleryPager = (ViewPager)findViewById(R.id.galleryImagePager);
		imageGalleryPager.setAdapter(imageGalleryPagerAdapter);
		imageGalleryPager.setCurrentItem(imageGalleryPos);
	}
	
	private class GalleryPagerAdapter extends PagerAdapter {
		
		private Context ctx;
		private int count;
		
		public GalleryPagerAdapter(Context _ctx, int _count) {
			super();
			ctx = _ctx;
			count = _count;
		}

		@Override
		public int getCount() {
			return count;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			FrameLayout flv = (FrameLayout)inflater.inflate(R.layout.gallery_image_page, null);
			
			ImageView imageView = (ImageView)flv.findViewById(R.id.galleryImageItemDisplay);
			String imageURL = imageURLs[position];
			if (imageURL!=null && !imageURL.equals("")) {
				// make a DisplayImageOptions without cacheInMemory or cacheOnDisc, because the large image will hurt the cache
				DisplayImageOptions imageLoaderOptions = 
						new DisplayImageOptions.Builder()
							.imageScaleType(ImageScaleType.EXACTLY)
							.build();
				
				ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ctx)
					.memoryCacheExtraOptions(480/*px*/, 800/*px*/)
			        .threadPoolSize(5)
			        .threadPriority(Thread.MIN_PRIORITY + 2)
			        .denyCacheImageMultipleSizesInMemory()
			        .discCache(new LimitedAgeDiscCache(StorageUtils.getIndividualCacheDirectory(ctx), new Md5FileNameGenerator(), 604800/*seconds = 7days*/))
			        .memoryCache(new UsingFreqLimitedMemoryCache(2097152/*bytes = 2mb*/))
			        .defaultDisplayImageOptions(imageLoaderOptions)
			        .build();
				
				imageLoader.init(config);
				ProgressBar progressBar = (ProgressBar)flv.findViewById(R.id.galleryImageItemDisplayProgress);
				imageLoader.displayImage(imageURL, imageView, new ProgressBarImageLoadingListener(progressBar));
				
				TextView authorTextView = (TextView)flv.findViewById(R.id.galleryImageItemAuthor);
				String author = imageAuthors[position];
				if (author != null && !author.equals("") && authorTextView != null) {
					String authorLabel = getString(R.string.gallery_image_author, author);
					authorTextView.setText(authorLabel);
				}
			}
			else {
				Toast.makeText(ctx, R.string.gallery_image_item_display_error, Toast.LENGTH_LONG).show();
			}

			// Click listener on image
			if (imageView != null) {
				imageView.setOnLongClickListener(new GalleryImageLongClickListener(imageURL));
			}
			
			((ViewPager) container).addView(flv, 0);
			
			return flv;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((FrameLayout) object);
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == ((FrameLayout)obj);
		}
		
		public class GalleryImageLongClickListener implements OnLongClickListener {
			private String shareURL;
			public GalleryImageLongClickListener(String _shareURL) {
				super();
				shareURL = _shareURL;
			}
			
			@Override
			public boolean onLongClick(View v) {
				Context ctx = v.getContext();
				Vibrator vibe = (Vibrator)ctx.getSystemService(Context.VIBRATOR_SERVICE);
				vibe.vibrate(20);
				
				startActivity(Intent.createChooser(getShareIntent(shareURL), getString(R.string.campaign_share_chooser)));
				
				HashMap<String, String> param = new HashMap<String, String>();
				param.put("share", "chooser-opened");
				Analytics.logEvent(getPageName(), param);
				
				return false;
			}
			
			// Open Intent to share link to image
			private Intent getShareIntent(String imageURL) {
				String campaignName = campaign.getName();
				String shareTitle = getString(R.string.gallery_image_share_title, campaignName);
				
				Intent answer = new Intent(android.content.Intent.ACTION_SEND);
				answer.putExtra(android.content.Intent.EXTRA_SUBJECT, shareTitle);
				answer.putExtra(android.content.Intent.EXTRA_TEXT, imageURL);
				answer.setType("text/plain");
				return answer;
			}
		}
		
	}
}
