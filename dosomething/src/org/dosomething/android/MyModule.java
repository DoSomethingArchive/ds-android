package org.dosomething.android;

import java.io.File;

import android.content.Context;
import android.os.Environment;

import com.google.inject.AbstractModule;
import com.nostra13.universalimageloader.cache.disc.impl.FileCountLimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MyModule extends AbstractModule {

	private final Context context;
	
	public MyModule(Context context){
		this.context = context;
	}
	
	@Override
	protected void configure() {
		ImageLoader imageLoader = ImageLoader.getInstance();
		
		File cacheDir = new File(Environment.getExternalStorageDirectory(), "data/dosomething/cache");
		cacheDir.mkdirs();
		
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
	        .maxImageWidthForMemoryCache(800)
	        .maxImageHeightForMemoryCache(800)
	        .httpConnectTimeout(5000)
	        .httpReadTimeout(30000)
	        .threadPoolSize(5)
	        .threadPriority(Thread.MIN_PRIORITY + 2)
	        .denyCacheImageMultipleSizesInMemory()
	        .discCache(new FileCountLimitedDiscCache(cacheDir, 30)) // You can pass your own disc cache implementation
	        .memoryCache(new UsingFreqLimitedMemoryCache(2000000))
	        .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
	        .build();
		imageLoader.init(config);

		//imageLoader.init(ImageLoaderConfiguration.createDefault(context));
		
		bind(ImageLoader.class).toInstance(imageLoader);
	}

}
