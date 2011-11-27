package com.github.learnin.youroomer;

import android.app.Application;

public class YouRoomerApplication extends Application {

	@Override
	public void onTerminate() {
		super.onTerminate();
		UserImageCache.clear();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		UserImageCache.clear();
	}

}
