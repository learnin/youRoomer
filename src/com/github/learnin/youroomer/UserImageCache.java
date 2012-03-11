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
import java.util.Map;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LruCache;

// BitmapによるネイティブヒープでのOutOfMemoryError発生をできるだけ避けるためにrecycleを実行する必要があるので、SoftReferenceは使用しない。
public class UserImageCache extends LruCache<String, Bitmap> implements Parcelable {

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

	public synchronized void putUserImage(String userImageURI, Bitmap bitmap) {
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

	public synchronized void putAll(UserImageCache cache) {
		if (cache != null) {
			Map<String, Bitmap> map = cache.snapshot();
			for (Map.Entry<String, Bitmap> entry : map.entrySet()) {
				put(entry.getKey(), entry.getValue());
			}
			map.clear();
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		Map<String, Bitmap> map = snapshot();
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("mapKeys", new ArrayList<String>(map.keySet()));
		bundle.putParcelableArrayList("mapValues", new ArrayList<Bitmap>(map.values()));
		dest.writeBundle(bundle);
		map.clear();
	}

	public static final Parcelable.Creator<UserImageCache> CREATOR = new Parcelable.Creator<UserImageCache>() {
		public UserImageCache createFromParcel(Parcel in) {
			return new UserImageCache(in);
		}

		public UserImageCache[] newArray(int size) {
			return new UserImageCache[size];
		}
	};

	private UserImageCache(Parcel in) {
		super(MAX_CACHE_SIZE);
		Bundle bundle = in.readBundle();
		List<String> keyList = bundle.getStringArrayList("mapKeys");
		List<Bitmap> valueList = bundle.getParcelableArrayList("mapValues");
		for (int i = 0, n = keyList.size(); i < n; i++) {
			put(keyList.get(i), valueList.get(i));
		}
	}

}
