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
						Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);

						int srcWidth = bitmap.getWidth();
						int srcHeight = bitmap.getHeight();
						float density = mImageView.getContext().getResources().getDisplayMetrics().density;
						float scale = USER_IMAGE_PX / density / Math.max(srcWidth, srcHeight);
						bitmap = Bitmap.createScaledBitmap(
							bitmap,
							(int) (srcWidth * scale),
							(int) (srcHeight * scale),
							true);
						return bitmap;
					} finally {
						if (is != null) {
							is.close();
						}
					}
				}
			});
			UserImageCache.setUserImage(urls[0], image);
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
		}
	}

}