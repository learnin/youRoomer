package com.github.learnin.youroomer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import youroom4j.YouRoomClient;
import youroom4j.oauth.OAuthTokenCredential;
import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import com.github.learnin.youroomer.R;

public class HomeTimeLineActivity extends ListActivity {

	private ArrayList<Entry> mItems;
	private TimeLineListAdapter mAdapter;
	private YouRoomClient mYouRoomClient;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_time_line);

		Intent intent = getIntent();
		if (intent != null) {
			OAuthTokenCredential oAuthTokenCredential =
				(OAuthTokenCredential) intent.getSerializableExtra("OAuthTokenCredential");
			mYouRoomClient = YouRoomClientBuilder.createYouRoomClient();
			mYouRoomClient.setOAuthTokenCredential(oAuthTokenCredential);
			mItems = new ArrayList<Entry>();
			mAdapter = new TimeLineListAdapter(this, mItems);
			GetTimeLineTask task = new GetTimeLineTask(this, mAdapter);
			task.execute();
		} else {
			// FIXME
		}
	}

	// Activityのライフサイクルに合わせてTaskのライフサイクルを制御する実装が漏れた場合に、
	// インナークラスによるエンクロージングクラスのインスタンスへの暗黙的な参照が残ってしまい、ActivityがGCされなくなることを防止するために
	// staticなインナークラスとし、Activityへの参照を弱参照にする。
	private static class GetTimeLineTask extends AsyncTask<Void, Integer, TimeLineListAdapter> {

		private WeakReference<HomeTimeLineActivity> mHomeTimeLineActivity;
		private TimeLineListAdapter mAdapter;

		private GetTimeLineTask(HomeTimeLineActivity homeTimeLineActivity, TimeLineListAdapter mAdapter) {
			mHomeTimeLineActivity = new WeakReference<HomeTimeLineActivity>(homeTimeLineActivity);
			this.mAdapter = mAdapter;
		}

		/*
		 * バックグラウンドでデータを取得します。<br>
		 */
		@Override
		protected TimeLineListAdapter doInBackground(Void... params) {
			final HomeTimeLineActivity homeTimeLineActivity = mHomeTimeLineActivity.get();
			if (homeTimeLineActivity != null) {
				try {
					List<Entry> entryList = homeTimeLineActivity.mYouRoomClient.getTimeLine();
					for (Entry entry : entryList) {
						mAdapter.add(entry);
					}
				} catch (IOException e) {
					// FIXME
					Toast.makeText(
						homeTimeLineActivity.getApplicationContext(),
						"YouRoomアクセスでエラーが発生しました。",
						Toast.LENGTH_LONG).show();
				}
			}
			return mAdapter;
		}

		/*
		 * データ取得後、表示を行います。<br>
		 */
		@Override
		protected void onPostExecute(TimeLineListAdapter timeLineListAdapter) {
			if (timeLineListAdapter != null) {
				final HomeTimeLineActivity homeTimeLineActivity = mHomeTimeLineActivity.get();
				if (homeTimeLineActivity != null) {
					homeTimeLineActivity.setListAdapter(timeLineListAdapter);
				}
			}
		}
	}

}