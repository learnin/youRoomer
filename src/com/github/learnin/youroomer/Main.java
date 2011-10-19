package com.github.learnin.youroomer;

import java.io.IOException;
import java.io.Serializable;

import youroom4j.YouRoomClient;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

// FIXME HomeTimeLineActivityから戻ったときに、またHomeTimeLineActivityへ行かないように制御が必要。
// インテントフラグか、startActivityForResultで戻りを受けるコールバックメソッドでfinishするか
public class Main extends Activity {

	private static final String CALLBACK_URL = "com.github.learnin.youroomer.main://oauthcallback";

	private YouRoomClient mYouRoomClient;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (savedInstanceState != null) {
			Serializable oAuthTokenCredential = savedInstanceState.getSerializable("OAuthTokenCredential");
			if (oAuthTokenCredential != null) {
				Intent intent = new Intent(getApplicationContext(), HomeTimeLineActivity.class);
				startActivity(intent);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getSharedPreferences("oauth", Context.MODE_PRIVATE);
		if (sharedPreferences.getString("token", null) != null
			&& sharedPreferences.getString("tokenSecret", null) != null) {
			Intent intent = new Intent(getApplicationContext(), HomeTimeLineActivity.class);
			startActivity(intent);
		} else {
			mYouRoomClient = YouRoomClientBuilder.createYouRoomClient();
		}
	}

	public void onLoginClick(View v) {
		String url = null;
		try {
			url = mYouRoomClient.oAuthRequestTokenRequest(CALLBACK_URL);
		} catch (IOException e) {
			// FIXME
			showToast("YouRoomアクセスでエラーが発生しました。");
		}
		if (url != null) {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
		}
	}

	public void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Uri uri = intent.getData();
		if (uri != null && uri.toString().startsWith(CALLBACK_URL)) {
			String verifier = uri.getQueryParameter("oauth_verifier");
			try {
				mYouRoomClient.oAuthAccessTokenRequest(verifier);
				getSharedPreferences("oauth", MODE_PRIVATE)
					.edit()
					.putString("token", mYouRoomClient.getOAuthTokenCredential().getToken())
					.putString("tokenSecret", mYouRoomClient.getOAuthTokenCredential().getTokenSecret())
					.commit();

				Intent intent2 = new Intent(getApplicationContext(), HomeTimeLineActivity.class);
				startActivity(intent2);
			} catch (IOException e) {
				// FIXME
				showToast("YouRoomアクセスでエラーが発生しました。");
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		saveInstanceState(outState);
	}

	private void saveInstanceState(Bundle outState) {
		if (mYouRoomClient != null) {
			outState.putSerializable("OAuthTokenCredential", mYouRoomClient.getOAuthTokenCredential());
		}
	}

}