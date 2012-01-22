package com.github.learnin.youroomer;

import java.lang.ref.SoftReference;
import java.util.Map;

import android.graphics.Bitmap;

// TODO Collections.synchronizedMapやReentrantReadWriteLockの使用を検討
public class UserImageCache {

	private static final int LIMIT_CACHE_SIZE = 100;

	private static final Map<String, SoftReference<Bitmap>> mCache = new LruHashMap<String, SoftReference<Bitmap>>(
		LIMIT_CACHE_SIZE);

	public static synchronized Bitmap getUserImage(String userImageURI) {
		SoftReference<Bitmap> reference = mCache.get(userImageURI);
		if (reference != null) {
			return reference.get();
		}
		return null;
	}

	public static synchronized void setUserImage(String userImageURI, Bitmap bitmap) {
		if (userImageURI != null && bitmap != null) {
			mCache.put(userImageURI, new SoftReference<Bitmap>(bitmap));
		}
	}

	public static synchronized void clear() {
		mCache.clear();
	}

}
