package com.github.learnin.youroomer;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.http.HttpStatus;

import youroom4j.Entry;
import youroom4j.YouRoom4JException;
import youroom4j.YouRoomClient;
import youroom4j.http.HttpResponseEntity;
import youroom4j.http.HttpResponseHandler;
import youroom4j.oauth.OAuthTokenCredential;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HomeTimeLineListAdapter extends ArrayAdapter<Entry> {

	private LayoutInflater mLayoutInflater;
	private YouRoomClient mYouRoomClient;

	public HomeTimeLineListAdapter(Context context, List<Entry> entryList) {
		super(context, 0, entryList);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		SharedPreferences sharedPreferences = context.getSharedPreferences("oauth", Context.MODE_PRIVATE);
		OAuthTokenCredential oAuthTokenCredential = new OAuthTokenCredential();
		oAuthTokenCredential.setToken(sharedPreferences.getString("token", ""));
		oAuthTokenCredential.setTokenSecret(sharedPreferences.getString("tokenSecret", ""));
		mYouRoomClient = YouRoomClientBuilder.createYouRoomClient();
		mYouRoomClient.setOAuthTokenCredential(oAuthTokenCredential);

		UserImageCache.clear();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		Entry entry = this.getItem(position);
		if (entry == null) {
			if (convertView == null) {
				return mLayoutInflater.inflate(R.layout.entry_row, null);
			}
			return convertView;
		}

		View view = convertView;
		ViewHolder holder = new ViewHolder();

		if (convertView == null) {
			view = mLayoutInflater.inflate(R.layout.entry_row, null);
			holder.mUserImage = (ImageView) view.findViewById(R.id.user_image);
			holder.mUsername = (TextView) view.findViewById(R.id.username);
			holder.mHasRead = (TextView) view.findViewById(R.id.has_read);
			holder.mCreatedAt = (TextView) view.findViewById(R.id.created_at);
			holder.mContent = (TextView) view.findViewById(R.id.content);
			holder.mCommentCount = (TextView) view.findViewById(R.id.comment_count);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		try {
			// FIXME このままだとスクロールの度にWebAPIリクエストで画像を取りに行くので遅い。TL表示とは非同期にする
			String userImageURI = entry.getParticipation().getUserImageURI();
			Bitmap bitmap = UserImageCache.getUserImage(userImageURI);
			if (bitmap == null) {
				bitmap = mYouRoomClient.showPicture(userImageURI, new HttpResponseHandler<Bitmap>() {
					@Override
					public Bitmap handleResponse(HttpResponseEntity responseEntity) throws IOException {
						if (responseEntity.getStatusCode() == HttpStatus.SC_OK) {
							InputStream is = null;
							try {
								is = responseEntity.getContent();
								return BitmapFactory.decodeStream(is);
							} finally {
								if (is != null) {
									is.close();
								}
							}
						}
						return null;
					}
				});
				UserImageCache.setUserImage(userImageURI, bitmap);
			}
			holder.mUserImage.setImageBitmap(bitmap);
		} catch (YouRoom4JException e) {
			// FIXME
			System.out.println(e);
		}

		holder.mUsername.setText(entry.getParticipation().getName());

		// FIXME 未読表示仮実装。画像にする
		if (entry.hasRead()) {
			holder.mHasRead.setVisibility(View.INVISIBLE);
		}

		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		holder.mCreatedAt.setText(df.format(entry.getCreatedAt()));

		holder.mContent.setText(entry.getContent());

		if (entry.getChildren() != null) {
			String mCommentCountText = entry.getChildren().size() + " comment";
			if (entry.getChildren().size() > 1) {
				mCommentCountText += "s";
			}
			holder.mCommentCount.setText(mCommentCountText);
		}
		return view;
	}

	private static class ViewHolder {
		ImageView mUserImage;
		TextView mUsername;
		TextView mHasRead;
		TextView mCreatedAt;
		TextView mContent;
		TextView mCommentCount;
	}

}
