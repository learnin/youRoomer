/*
 * Copyright 2012 Manabu Inoue
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.learnin.youroomer;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

// BitmapによるネイティブヒープでのOutOfMemoryError発生をできるだけ避けるためにrecycleを実行する必要があるので、SoftReferenceは使用しない。
public class UserImageCache extends LruCache<String, Bitmap> {

	// キャッシュ最大数。最低、1画面内のListViewの行表示件数以上は必要。
	private static final int MAX_CACHE_SIZE = 50;

	private static final UserImageCache mCache = new UserImageCache(MAX_CACHE_SIZE);

	// ダウンロード中の画像URLのリスト
	private List<String> mDownloadingImageUrls = new ArrayList<String>();

	private UserImageCache(int maxSize) {
		super(maxSize);
	}

	public static UserImageCache getInstance() {
		return mCache;
	}

	public synchronized void setUserImage(String userImageURI, Bitmap bitmap) {
		if (userImageURI != null && bitmap != null) {
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

	public boolean isDownloadingImageUrl(String url) {
		return mDownloadingImageUrls.contains(url);
	}

	public void addDownloadingImageUrl(String url) {
		mDownloadingImageUrls.add(url);
	}

	public void removeDownloadingImageUrl(String url) {
		mDownloadingImageUrls.remove(url);
	}

}
