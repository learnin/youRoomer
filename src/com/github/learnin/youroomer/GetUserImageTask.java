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

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpStatus;

import youroom4j.YouRoom4JException;
import youroom4j.YouRoomClient;
import youroom4j.http.HttpResponseEntity;
import youroom4j.http.HttpResponseHandler;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

public class GetUserImageTask extends AsyncTask<Void, Integer, Bitmap> {

	// ユーザー画像表示ピクセル
	private static final int USER_IMAGE_PX = 48;

	private YouRoomClient mYouRoomClient;
	private View mTargetView;
	private String mUrl;

	public GetUserImageTask(YouRoomClient youRoomClient, String url, View targetView) {
		mYouRoomClient = youRoomClient;
		mTargetView = targetView;
		mUrl = url;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		UserImageCache userImageCache = UserImageCache.getInstance();
		try {
			Bitmap image = mYouRoomClient.showPicture(mUrl, new HttpResponseHandler<Bitmap>() {
				@Override
				public Bitmap handleResponse(HttpResponseEntity responseEntity) throws IOException {
					if (responseEntity.getStatusCode() != HttpStatus.SC_OK) {
						throw new IOException("Http status code is " + responseEntity.getStatusCode());
					}
					InputStream is = null;
					try {
						is = responseEntity.getContent();
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inPreferredConfig = Bitmap.Config.RGB_565;
						return BitmapFactory.decodeStream(is, null, options);
					} finally {
						if (is != null) {
							is.close();
						}
					}
				}
			});
			int srcWidth = image.getWidth();
			int srcHeight = image.getHeight();
			float density = mTargetView.getContext().getResources().getDisplayMetrics().density;
			float scale = USER_IMAGE_PX / density / Math.max(srcWidth, srcHeight);
			image = Bitmap.createScaledBitmap(image, (int) (srcWidth * scale), (int) (srcHeight * scale), true);
			userImageCache.setUserImage(mUrl, image);
			return image;
		} catch (YouRoom4JException e) {
			// FIXME
			e.printStackTrace();
		} finally {
			userImageCache.removeDownloadingImageUrl(mUrl);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap bitmap) {
		if (bitmap != null) {
			if (mTargetView instanceof ListView) {
				// ListViewの複数行に同一ユーザー画像が表示必要な場合でもダウンロードは1つのTaskでしか行なっていないため、ListView配下のImageViewから対象を全て探してビットマップをセットする。
				ListView listView = (ListView) mTargetView;
				for (int i = 0, n = listView.getChildCount(); i < n; i++) {
					// この処理は、entry_row.xml の構造に依存する
					ImageView imageView = (ImageView) ((ViewGroup) ((ViewGroup) listView.getChildAt(i)).getChildAt(0)).getChildAt(0);
					if (mUrl.equals(imageView.getTag())) {
						imageView.setImageBitmap(bitmap);
					}
				}
			} else if (mTargetView instanceof ImageView) {
				((ImageView) mTargetView).setImageBitmap(bitmap);
			}
			// 本来は、画像キャッシュ追加により古いキャッシュが削除された場合は、このタイミングでAdapter#notifyDataSetChanged()等の実行が必要だが、
			// (そうしないと、1画面内のListViewの行にrecycle済みのBitmapを参照しているものがあれば、エラーになってしまう)
			// 1画面内のListViewの行表示件数以上がキャッシュされていれば、大丈夫のはずなので省略する。
		}
	}

}