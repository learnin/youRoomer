package com.github.learnin.youroomer;

import java.util.Map;

import android.graphics.Bitmap;

// TODO Collections.synchronizedMapやReentrantReadWriteLockの使用を検討
public class UserImageCache {

	private static final int LIMIT_CACHE_SIZE = 100;

	private static Map<String, Bitmap> cache = new LruHashMap<String, Bitmap>(LIMIT_CACHE_SIZE);

	public static synchronized Bitmap getUserImage(String userImageURI) {
		return cache.get(userImageURI);
	}

	public static synchronized void setUserImage(String userImageURI, Bitmap bitmap) {
		if (userImageURI != null && bitmap != null) {
			cache.put(userImageURI, bitmap);
		}
	}

	public static synchronized void clear() {
		cache.clear();
	}

}
