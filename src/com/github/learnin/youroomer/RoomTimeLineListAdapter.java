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

		UserImageCache.clear();
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
		Bitmap bitmap = UserImageCache.getUserImage(userImageURI);
		if (bitmap == null) {
			GetUserImageTask getUserImageTask = new GetUserImageTask(mYouRoomClient, holder.mUserImage);
			getUserImageTask.execute(userImageURI);
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
