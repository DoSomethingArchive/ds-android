package org.dosomething.android;

import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.UserContext;

import android.content.Context;
import android.graphics.Typeface;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.nostra13.universalimageloader.cache.disc.impl.FileCountLimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.DecodingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class MyModule extends AbstractModule {

	private final Context context;
	
	public MyModule(Context context){
		this.context = context;
	}
	
	@Override
	protected void configure() {

		// Image Loader singleton
		bind(ImageLoader.class)
			.toProvider(ImageLoaderProvider.class)
			.in(Singleton.class);
		
		// User Context singleton
		bind(UserContext.class)
			.in(Singleton.class);
		
		// App model Cache singleton
		bind(Cache.class)
			.in(Singleton.class);
		
		// Din custom font singleton
		bind(Typeface.class)
			.annotatedWith(Names.named("DINComp-CondBold"))
			.toProvider(DinCompCondBoldProvider.class)
			.in(Singleton.class);
	}
	
	public static class ImageLoaderProvider implements Provider<ImageLoader> {
		
		private Context context;
		
		@Inject
		public ImageLoaderProvider(Context context) {
			this.context = context;
		}
		
		@Override
		public ImageLoader get() {
			ImageLoader imageLoader = ImageLoader.getInstance();
			
			DisplayImageOptions displayOptions = new DisplayImageOptions.Builder()
				.cacheInMemory()
				.cacheOnDisc()
				.decodingType(DecodingType.MEMORY_SAVING)
				.build();
			
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.maxImageWidthForMemoryCache(480)
	            .maxImageHeightForMemoryCache(800)
		        .httpConnectTimeout(5000)
		        .httpReadTimeout(30000)
		        .threadPoolSize(5)
		        .threadPriority(Thread.MIN_PRIORITY + 2)
		        .denyCacheImageMultipleSizesInMemory()
		        .discCache(new FileCountLimitedDiscCache(StorageUtils.getIndividualCacheDirectory(context), 30))
		        .memoryCache(new UsingFreqLimitedMemoryCache(2000000))
		        .defaultDisplayImageOptions(displayOptions)
		        .build();
			
			imageLoader.init(config);
			
			return imageLoader;
		}
		
	}
	
	public static class DinCompCondBoldProvider implements Provider<Typeface> {
		
		private Context context;
		
		@Inject
		public DinCompCondBoldProvider(Context context) {
			this.context = context;
		}
		
		@Override
		public Typeface get() {
			return Typeface.createFromAsset(context.getAssets(), "DINComp-CondBold.ttf");
		}
	}

}
