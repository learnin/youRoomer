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
import android.widget.ImageView;

public class GetUserImageTask extends AsyncTask<String, Integer, Bitmap> {

	// ユーザー画像表示ピクセル
	private static final int USER_IMAGE_PX = 48;

	private YouRoomClient mYouRoomClient;
	private ImageView mImageView;

	public GetUserImageTask(YouRoomClient youRoomClient, ImageView imageView) {
		mYouRoomClient = youRoomClient;
		mImageView = imageView;
	}

	@Override
	protected Bitmap doInBackground(String... urls) {
		try {
			Bitmap image = mYouRoomClient.showPicture(urls[0], new HttpResponseHandler<Bitmap>() {
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
			float density = mImageView.getContext().getResources().getDisplayMetrics().density;
			float scale = USER_IMAGE_PX / density / Math.max(srcWidth, srcHeight);
			image = Bitmap.createScaledBitmap(image, (int) (srcWidth * scale), (int) (srcHeight * scale), true);
			UserImageCache.getInstance().setUserImage(urls[0], image);
			mImageView.setTag(urls[0]);
			return image;
		} catch (YouRoom4JException e) {
			// FIXME
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap image) {
		if (image != null) {
			mImageView.setImageBitmap(image);
			// 本来は、画像キャッシュ追加により古いキャッシュが削除された場合は、このタイミングでAdapter#notifyDataSetChanged()等の実行が必要だが、
			// (そうしないと、1画面内のListViewの行にrecycle済みのBitmapを参照しているものがあれば、エラーになってしまう)
			// 1画面内のListViewの行表示件数以上がキャッシュされていれば、大丈夫のはずなので省略する。
		}
	}

}