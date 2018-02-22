package jp.wasabeef.sample;

import android.annotation.SuppressLint;
import android.app.Application;

import com.android.overlay.KeepAliveService;
import com.android.overlay.RunningEnvironment;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MyApplication extends Application {
	protected RunningEnvironment managers;

	@Override
	public void onCreate() {
		super.onCreate();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				this)
				.denyCacheImageMultipleSizesInMemory()
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheExtraOptions(480, 800)
				// .discCacheExtraOptions(480, 800, CompressFormat.PNG, 75,
				// null)
				.discCacheSize(1024 * 1024 * 50)
				.memoryCacheSize(1024 * 1024 * 2)
				.memoryCache(new LruMemoryCache(2 * 1024 * 1024)).build();
		ImageLoader.getInstance().init(config);

		if (managers == null) {
			managers = new RunningEnvironment("R.array.managers",
					"R.array.tables");
		} else {
			managers = RunningEnvironment.getInstance();
		}
		managers.onCreate(this);
		startService(KeepAliveService.createIntent(this));

	}

	@Override
	public void onTerminate() {
		if (managers != null) {
			managers.onTerminate();
		}
		super.onTerminate();
	}

	@Override
	public void onLowMemory() {
		if (managers != null) {
			managers.onLowMemory();
		}
		super.onLowMemory();
	}

	@SuppressLint("NewApi")
	@Override
	public void onTrimMemory(int level) {
		if (managers != null) {
			managers.onTrimMemory(level);
		}
		super.onTrimMemory(level);
	}
}
