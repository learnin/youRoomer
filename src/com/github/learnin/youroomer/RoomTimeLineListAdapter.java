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

import java.text.SimpleDateFormat;
import java.util.List;

import youroom4j.Entry;
import youroom4j.YouRoomClient;
import youroom4j.oauth.OAuthTokenCredential;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RoomTimeLineListAdapter extends ArrayAdapter<Entry> {

	private LayoutInflater mLayoutInflater;
	private YouRoomClient mYouRoomClient;

	public RoomTimeLineListAdapter(Context context, List<Entry> entryList) {
		super(context, 0, entryList);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		SharedPreferences sharedPreferences = context.getSharedPreferences("oauth", Context.MODE_PRIVATE);
		OAuthTokenCredential oAuthTokenCredential = new OAuthTokenCredential();
		oAuthTokenCredential.setToken(sharedPreferences.getString("token", ""));
		oAuthTokenCredential.setTokenSecret(sharedPreferences.getString("tokenSecret", ""));
		mYouRoomClient = YouRoomClientBuilder.createYouRoomClient();
		mYouRoomClient.setOAuthTokenCredential(oAuthTokenCredential);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.entry_row, null);
			holder = new ViewHolder();
			holder.mUserImage = (ImageView) convertView.findViewById(R.id.user_image);
			holder.mUsername = (TextView) convertView.findViewById(R.id.username);
			holder.mCreatedAt = (TextView) convertView.findViewById(R.id.created_at);
			holder.mContent = (TextView) convertView.findViewById(R.id.content);
			holder.mCommentCount = (TextView) convertView.findViewById(R.id.comment_count);
			holder.mHasRead = (TextView) convertView.findViewById(R.id.has_read);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Entry entry = getItem(position);
		String userImageURI = entry.getParticipation().getUserImageURI();
		holder.mUserImage.setTag(userImageURI);
		UserImageCache userImageCache = UserImageCache.getInstance();
		Bitmap bitmap = userImageCache.get(userImageURI);
		// 複数行に同一ユーザー画像がある場合に、同時にダウンロードTaskが走って、キャッシュ追加を行い、後勝ちで先に取得した画像がrecycleされて描画時にエラーになるのを防ぐため、
		// また、パフォーマンスや無駄な処理を行わないようにするため、ダウンロード中画像は取得しない。代わりにGetUserImageTaskの中でダウンロード後に該当ImageView全てへ画像をセットさせる。
		if ((bitmap == null || bitmap.isRecycled()) && !userImageCache.isDownloadingImageUrl(userImageURI)) {
			userImageCache.addDownloadingImageUrl(userImageURI);
			GetUserImageTask getUserImageTask = new GetUserImageTask(mYouRoomClient, userImageURI, parent);
			getUserImageTask.execute();
		} else {
			holder.mUserImage.setImageBitmap(bitmap);
		}

		holder.mUsername.setText(entry.getParticipation().getName());

		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		holder.mCreatedAt.setText(df.format(entry.getCreatedAt()));

		holder.mContent.setText(entry.getContent());

		if (entry.hasRead()) {
			holder.mHasRead.setText("archived");
			holder.mHasRead.setVisibility(View.VISIBLE);
		} else {
			holder.mHasRead.setVisibility(View.GONE);
		}

		if (entry.getDescendantsCount() > 0) {
			String mCommentCountText = entry.getDescendantsCount() + " comment";
			if (entry.getDescendantsCount() > 1) {
				mCommentCountText += "s";
			}
			if (entry.getUnreadCommentIds() != null && !entry.getUnreadCommentIds().isEmpty()) {
				mCommentCountText += "(" + entry.getUnreadCommentIds().size() + " unread)";
			}
			holder.mCommentCount.setText(mCommentCountText);
			holder.mCommentCount.setVisibility(View.VISIBLE);
		} else {
			holder.mCommentCount.setVisibility(View.GONE);
		}
		return convertView;
	}

	private static class ViewHolder {
		ImageView mUserImage;
		TextView mUsername;
		TextView mCreatedAt;
		TextView mContent;
		TextView mCommentCount;
		TextView mHasRead;
	}

}
