package org.dosomething.android;

import android.content.Context;
import android.graphics.Typeface;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import com.nostra13.universalimageloader.cache.disc.impl.LimitedAgeDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.dosomething.android.cache.Cache;
import org.dosomething.android.context.UserContext;

public class MyModule extends AbstractModule {

	private final Context context;
	
	public MyModule(Context context){
		this.context = context;
	}
	
	/**
	 * This configures the applications class to impl mappings for injected things.
	 */
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

        // Typeface Proxima Nova Reg
        bind(Typeface.class)
            .annotatedWith(Names.named("ProximaNova-Reg"))
            .toProvider(ProximaNovaRegProvider.class)
            .in(Singleton.class);

        // Typeface Proxima Nova Bold
        bind(Typeface.class)
            .annotatedWith(Names.named("ProximaNova-Bold"))
            .toProvider(ProximaNovaBoldProvider.class)
            .in(Singleton.class);
	}
	
	/**
	 * Provides a new configured ImageLoader when needed for injection.
	 */
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
				.cacheInMemory() // allow images to memory cache by default
				.cacheOnDisc() // allow images to disc cache by default
				.imageScaleType(ImageScaleType.EXACTLY)
				.build();
			
			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.memoryCacheExtraOptions(480/*px*/, 800/*px*/)
		        .threadPoolSize(5)
		        .threadPriority(Thread.MIN_PRIORITY + 2)
		        .denyCacheImageMultipleSizesInMemory()
		        .discCache(new LimitedAgeDiscCache(StorageUtils.getIndividualCacheDirectory(context), new Md5FileNameGenerator(), 604800/*seconds = 7days*/))
		        .memoryCache(new UsingFreqLimitedMemoryCache(2097152/*bytes = 2mb*/))
		        .defaultDisplayImageOptions(displayOptions)
		        .build();
			
			imageLoader.init(config);
			
			return imageLoader;
		}
		
	}

    /**
     * Provides Proxima Nova Regular font for injection.
     */
    public static class ProximaNovaRegProvider implements Provider<Typeface> {

        private Context context;

        @Inject
        public ProximaNovaRegProvider(Context context) {
            this.context = context;
        }

        @Override
        public Typeface get() {
            return Typeface.createFromAsset(context.getAssets(), "ProximaNova-Reg.otf");
        }
    }

    /**
     * Provides Proxima Nova Bold font for injection.
     */
    public static class ProximaNovaBoldProvider implements Provider<Typeface> {

        private Context context;

        @Inject
        public ProximaNovaBoldProvider(Context context) {
            this.context = context;
        }

        @Override
        public Typeface get() {
            return Typeface.createFromAsset(context.getAssets(), "ProximaNova-Bold.otf");
        }
    }
}
