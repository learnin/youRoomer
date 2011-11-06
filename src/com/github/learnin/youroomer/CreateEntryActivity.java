package com.github.learnin.youroomer;

import java.lang.ref.WeakReference;

import youroom4j.YouRoom4JException;
import youroom4j.YouRoomClient;
import youroom4j.oauth.OAuthTokenCredential;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

// TODO 入力文字数カウント実装
// TODO 未入力・文字数オーバー時は投稿ボタンを押せなくする
public class CreateEntryActivity extends Activity {

	private YouRoomClient mYouRoomClient;
	private CreateEntryTask mCreateEntryTask;
	private String mGroupToParam = null;

	private EditText mEntryEditText;
	private Button mCreateEntryButton;
	private Button mCancelButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_entry);
		setupView(savedInstanceState);

		// FIXME 共通化する
		SharedPreferences sharedPreferences = getSharedPreferences("oauth", Context.MODE_PRIVATE);
		OAuthTokenCredential oAuthTokenCredential = new OAuthTokenCredential();
		oAuthTokenCredential.setToken(sharedPreferences.getString("token", ""));
		oAuthTokenCredential.setTokenSecret(sharedPreferences.getString("tokenSecret", ""));
		mYouRoomClient = YouRoomClientBuilder.createYouRoomClient();
		mYouRoomClient.setOAuthTokenCredential(oAuthTokenCredential);
	}

	private void setupView(final Bundle savedInstanceState) {
		mEntryEditText = (EditText) findViewById(R.id.entry_edit_text);

		mCreateEntryButton = (Button) findViewById(R.id.create_entry_button);
		mCreateEntryButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				createEntry();
			}
		});

		mCancelButton = (Button) findViewById(R.id.cancel_button);
		mCancelButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if (intent != null) {
			CharSequence groupToParam = intent.getCharSequenceExtra("GROUP_TO_PARAM");
			if (groupToParam != null) {
				mGroupToParam = groupToParam.toString();
			}
		}
	}

	private void createEntry() {
		if (mCreateEntryTask == null || mCreateEntryTask.getStatus() != AsyncTask.Status.RUNNING) {
			mCreateEntryButton.setEnabled(false);
			mCreateEntryTask = new CreateEntryTask(this);
			mCreateEntryTask.execute();
		}
	}

	public void afterCreateEntry() {
		Toast.makeText(getApplicationContext(), "投稿しました。", Toast.LENGTH_SHORT).show();
		finish();
	}

	private static class CreateEntryTask extends AsyncTask<Void, Integer, Void> {

		private WeakReference<CreateEntryActivity> mCreateEntryActivity;

		private CreateEntryTask(CreateEntryActivity createEntryActivity) {
			mCreateEntryActivity = new WeakReference<CreateEntryActivity>(createEntryActivity);
		}

		@Override
		protected Void doInBackground(Void... params) {
			final CreateEntryActivity createEntryActivity = mCreateEntryActivity.get();
			if (createEntryActivity != null) {
				try {
					createEntryActivity.mYouRoomClient.createEntry(
						createEntryActivity.mGroupToParam,
						createEntryActivity.mEntryEditText.getText().toString(),
						null);
				} catch (YouRoom4JException e) {
					// FIXME
					e.printStackTrace();
					Toast.makeText(
						createEntryActivity.getApplicationContext(),
						"YouRoomアクセスでエラーが発生しました。",
						Toast.LENGTH_LONG).show();
					createEntryActivity.mCreateEntryButton.setEnabled(true);
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			final CreateEntryActivity createEntryActivity = mCreateEntryActivity.get();
			if (createEntryActivity != null) {
				createEntryActivity.afterCreateEntry();
			}
		}
	}

}
