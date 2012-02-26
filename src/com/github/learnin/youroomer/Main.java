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

import youroom4j.YouRoom4JException;
import youroom4j.YouRoomClient;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class Main extends Activity {

	private static final String CALLBACK_URL = "com.github.learnin.youroomer.main://oauthcallback";
	private static final int HOME_TIME_LINE_REQUEST_CODE = 0;

	private YouRoomClient mYouRoomClient;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences sharedPreferences = getSharedPreferences("oauth", Context.MODE_PRIVATE);
		if (sharedPreferences.getString("token", null) != null
			&& sharedPreferences.getString("tokenSecret", null) != null) {
			Intent intent = new Intent(getApplicationContext(), HomeTimeLineActivity.class);
			startActivityForResult(intent, HOME_TIME_LINE_REQUEST_CODE);
		} else {
			mYouRoomClient = YouRoomClientBuilder.createYouRoomClient();
		}
	}

	public void onLoginClick(View v) {
		String url = null;
		try {
			url = mYouRoomClient.oAuthRequestTokenRequest(CALLBACK_URL);
		} catch (YouRoom4JException e) {
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
				startActivityForResult(intent2, HOME_TIME_LINE_REQUEST_CODE);
			} catch (YouRoom4JException e) {
				// FIXME
				showToast("YouRoomアクセスでエラーが発生しました。");
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case HOME_TIME_LINE_REQUEST_CODE:
			finish();
			break;
		default:
			break;
		}
	}

}