package com.github.learnin.youroomer;

import java.text.SimpleDateFormat;
import java.util.List;

import youroom4j.Entry;
import youroom4j.YouRoomClient;
import youroom4j.oauth.OAuthTokenCredential;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class EntryListAdapter extends ArrayAdapter<Entry> {

	private LayoutInflater mLayoutInflater;
	private YouRoomClient mYouRoomClient;
	private final float mScale = getContext().getResources().getDisplayMetrics().density;

	public EntryListAdapter(Context context, List<Entry> entryList) {
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
			holder.mLinearLayout = (LinearLayout) convertView.findViewById(R.id.entry_container);
			holder.mUserImage = (ImageView) convertView.findViewById(R.id.user_image);
			holder.mUsername = (TextView) convertView.findViewById(R.id.username);
			holder.mCreatedAt = (TextView) convertView.findViewById(R.id.created_at);
			holder.mContent = (TextView) convertView.findViewById(R.id.content);
			holder.mHasRead = (TextView) convertView.findViewById(R.id.has_read);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Entry entry = getItem(position);

		// 背景色、文字色の設定
		int level = entry.getLevel();
		int backgroundColor = Color.WHITE;
		switch (level) {
		case 1:
			backgroundColor = 0xffd8ea9b;
			break;
		case 2:
			backgroundColor = 0xffd1efad;
			break;
		case 3:
			backgroundColor = 0xffcdf3e0;
			break;
		case 4:
			backgroundColor = 0xffa4e8df;
			break;
		case 5:
			backgroundColor = 0xff70deeb;
			break;
		case 6:
			backgroundColor = 0xffa2c7ff;
			break;
		default:
			break;
		}
		holder.mLinearLayout.setBackgroundColor(backgroundColor);
		holder.mUsername.setTextColor(Color.BLACK);
		holder.mCreatedAt.setTextColor(Color.BLACK);
		holder.mContent.setTextColor(Color.BLACK);
		holder.mHasRead.setTextColor(Color.BLACK);

		// コメントのインデント
		float paddingLeftDip = entry.getLevel() * 10.0f;
		int paddingLeftPx = (int) (paddingLeftDip * mScale + 0.5f);
		holder.mLinearLayout.setPadding(paddingLeftPx, 0, 0, 0);

		String userImageURI = entry.getParticipation().getUserImageURI();
		Bitmap bitmap = UserImageCache.getInstance().get(userImageURI);
		if (bitmap == null) {
			GetUserImageTask getUserImageTask = new GetUserImageTask(mYouRoomClient, holder.mUserImage);
			getUserImageTask.execute(userImageURI);
		} else {
			holder.mUserImage.setImageBitmap(bitmap);
		}

		holder.mUsername.setText(entry.getParticipation().getName());

		if (entry.hasRead()) {
			holder.mHasRead.setText("archived");
			holder.mHasRead.setVisibility(View.VISIBLE);
		} else {
			holder.mHasRead.setVisibility(View.GONE);
		}

		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		holder.mCreatedAt.setText(df.format(entry.getCreatedAt()));
		holder.mContent.setText(entry.getContent());
		return convertView;

		// FIXME unread表示の実装。親のunread-comment-idsと一致したら表示する。
	}

	private static class ViewHolder {
		LinearLayout mLinearLayout;
		ImageView mUserImage;
		TextView mUsername;
		TextView mCreatedAt;
		TextView mContent;
		TextView mHasRead;
	}

}
