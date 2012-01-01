package com.github.learnin.youroomer;

import java.lang.ref.WeakReference;

import youroom4j.YouRoom4JException;
import youroom4j.YouRoomClient;
import youroom4j.oauth.OAuthTokenCredential;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditEntryActivity extends Activity {

	private static final int MAX_INPUT_LENGTH = 280;

	private YouRoomClient mYouRoomClient;
	private CreateEntryTask mCreateEntryTask;

	private String mAction = null;
	private String mGroupToParam = null;
	private long mEntryId;

	private EditText mEntryEditText;
	private TextView mInputLength;
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
		mEntryEditText.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				countInputLength();
				return false;
			}
		});

		mInputLength = (TextView) findViewById(R.id.input_length);

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
			mAction = intent.getStringExtra("ACTION");
			mGroupToParam = intent.getStringExtra("GROUP_TO_PARAM");
			if ("UPDATE".equals(mAction)) {
				mEntryId = intent.getLongExtra("ID", 0L);
				mEntryEditText.setText(intent.getCharSequenceExtra("CONTENT"));
				mCreateEntryButton.setText(R.string.update_entry);
			} else {
				mCreateEntryButton.setText(R.string.create_entry);
			}
		}
		countInputLength();
	}

	/**
	 * 入力された文字数をカウントし表示します。<br>
	 */
	private void countInputLength() {
		// 本来はサロゲートペアや合成文字を考慮して文字数カウントはjava.text.BreakIteratorでやるべきだが、少なくともyouRoomのWebフォームはサロゲートペアを
		// 考慮していないので念のため、あえて考慮せずにカウントする。
		int inputLength = mEntryEditText.getText().toString().length();
		mInputLength.setText(String.valueOf(MAX_INPUT_LENGTH - inputLength));
		if (inputLength == 0) {
			mInputLength.setTextColor(Color.WHITE);
			mCreateEntryButton.setEnabled(false);
		} else if (inputLength > 0 && inputLength <= MAX_INPUT_LENGTH) {
			mInputLength.setTextColor(Color.WHITE);
			mCreateEntryButton.setEnabled(true);
		} else {
			mInputLength.setTextColor(Color.RED);
			mCreateEntryButton.setEnabled(false);
		}
	}

	private void createEntry() {
		if (mCreateEntryTask == null || mCreateEntryTask.getStatus() != AsyncTask.Status.RUNNING) {
			mCreateEntryButton.setEnabled(false);
			mCreateEntryTask = new CreateEntryTask(this);
			mCreateEntryTask.execute();
		}
	}

	private void afterCreateEntry() {
		if ("CREATE".equals(mAction)) {
			Toast.makeText(getApplicationContext(), "投稿しました。", Toast.LENGTH_SHORT).show();
		} else if ("UPDATE".equals(mAction)) {
			Toast.makeText(getApplicationContext(), "更新しました。", Toast.LENGTH_SHORT).show();
		}
		finish();
	}

	private static class CreateEntryTask extends AsyncTask<Void, Integer, Boolean> {

		private WeakReference<EditEntryActivity> mCreateEntryActivity;

		private CreateEntryTask(EditEntryActivity createEntryActivity) {
			mCreateEntryActivity = new WeakReference<EditEntryActivity>(createEntryActivity);
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			final EditEntryActivity createEntryActivity = mCreateEntryActivity.get();

			if (createEntryActivity != null) {
				try {
					if ("CREATE".equals(createEntryActivity.mAction)) {
						createEntryActivity.mYouRoomClient.createEntry(
							createEntryActivity.mGroupToParam,
							createEntryActivity.mEntryEditText.getText().toString(),
							null);
					} else if ("UPDATE".equals(createEntryActivity.mAction)) {
						createEntryActivity.mYouRoomClient.updateEntry(
							createEntryActivity.mGroupToParam,
							createEntryActivity.mEntryId,
							createEntryActivity.mEntryEditText.getText().toString());
					}
				} catch (YouRoom4JException e) {
					// FIXME
					e.printStackTrace();
					return false;
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			final EditEntryActivity createEntryActivity = mCreateEntryActivity.get();
			if (createEntryActivity != null) {
				if (result) {
					createEntryActivity.afterCreateEntry();
				} else {
					Toast.makeText(
						createEntryActivity.getApplicationContext(),
						"YouRoomアクセスでエラーが発生しました。",
						Toast.LENGTH_LONG).show();
					createEntryActivity.mCreateEntryButton.setEnabled(true);
				}
			}
		}
	}

}
