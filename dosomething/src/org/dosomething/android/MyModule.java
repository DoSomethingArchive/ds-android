package org.dosomething.android;

import java.lang.annotation.Annotation;

import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.UserContext;

import android.content.Context;
import android.graphics.Typeface;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
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
		
		bind(ImageLoader.class).toInstance(imageLoader);
		bind(UserContext.class).toInstance(new UserContext(context));
		bind(Cache.class).toInstance(new Cache());
		
		// Custom fonts
		bind(Typeface.class).annotatedWith(Names.named("DINComp-CondBold")).toProvider(new DinCompCondBoldProvider(context));
	}
	
	
	private static class DinCompCondBoldProvider implements Provider<Typeface> {
		
		private Context context;
		
		public DinCompCondBoldProvider(Context context) {
			this.context = context;
		}
		
		@Override
		public Typeface get() {
			return Typeface.createFromAsset(context.getAssets(), "DINComp-CondBold.ttf");
		}
	}

}
