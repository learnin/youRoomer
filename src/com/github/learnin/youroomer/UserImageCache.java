package com.github.learnin.youroomer;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

// BitmapによるネイティブヒープでのOutOfMemoryError発生をできるだけ避けるためにrecycleを実行する必要があるので、SoftReferenceは使用しない。
public class UserImageCache extends LruCache<String, Bitmap> {

	// キャッシュ最大数。最低、1画面内のListViewの行表示件数以上は必要。
	private static final int MAX_CACHE_SIZE = 50;

	private static final UserImageCache mCache = new UserImageCache(MAX_CACHE_SIZE);

	private UserImageCache(int maxSize) {
		super(maxSize);
	}

	public static UserImageCache getInstance() {
		return mCache;
	}

	public synchronized void setUserImage(String userImageURI, Bitmap bitmap) {
		// 非同期で同一URLの画像を同時に取得しに行き、後勝ちで先に取得した画像はrecycleされてしまい、画面表示時にエラーになってしまうのを防ぐため、キャッシュにある場合はputしない
		if (userImageURI != null && bitmap != null && get(userImageURI) == null) {
			put(userImageURI, bitmap);
		}
	}

	@Override
	protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
		if (oldValue != null && !oldValue.isRecycled()) {
			oldValue.recycle();
			oldValue = null;
		}
	}

	public synchronized void clear() {
		if (size() > 0) {
			evictAll();
		}
	}

}
