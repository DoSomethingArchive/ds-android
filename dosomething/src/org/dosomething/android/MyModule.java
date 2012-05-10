package org.dosomething.android;

import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.SessionContext;

import android.content.Context;

import com.google.inject.AbstractModule;
import com.nostra13.universalimageloader.cache.disc.impl.FileCountLimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class MyModule extends AbstractModule {

	private final Context context;
	
	public MyModule(Context context){
		this.context = context;
	}
	
	@Override
	protected void configure() {
		ImageLoader imageLoader = ImageLoader.getInstance();
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
	        .maxImageWidthForMemoryCache(800)
	        .maxImageHeightForMemoryCache(800)
	        .httpConnectTimeout(5000)
	        .httpReadTimeout(30000)
	        .threadPoolSize(5)
	        .threadPriority(Thread.MIN_PRIORITY + 2)
	        .denyCacheImageMultipleSizesInMemory()
	        .discCache(new FileCountLimitedDiscCache(StorageUtils.getIndividualCacheDirectory(context), 30))
	        .memoryCache(new UsingFreqLimitedMemoryCache(2000000))
	        .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
	        .build();
		
		imageLoader.init(config);

		//imageLoader.init(ImageLoaderConfiguration.createDefault(context));
		
		bind(ImageLoader.class).toInstance(imageLoader);
		bind(SessionContext.class).toInstance(new SessionContext());
		bind(Cache.class).toInstance(new Cache());
	}

}
