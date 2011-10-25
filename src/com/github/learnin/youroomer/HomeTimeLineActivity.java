package com.github.learnin.youroomer;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import youroom4j.YouRoomClient;
import youroom4j.oauth.OAuthTokenCredential;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

// FIXME 他の画面も含めて全般的に、プリファレンスに保存されているOAuthクレデンシャルで401エラーになった場合(YouRoomClientで特定例外スロー？)は、OAuth認証フローへ導くようにする。
// FIXME プログレス表示
public class HomeTimeLineActivity extends ListActivity {

	private static final String GET_TIME_LINE_TASK_STATUS_RUNNING = "GET_TIME_LINE_TASK_STATUS_RUNNING";
	private static final int DIALOG_ROOM_LIST_ID = 0;

	private ArrayList<Entry> mItems;
	private TimeLineListAdapter mAdapter;
	private YouRoomClient mYouRoomClient;
	private GetTimeLineTask mGetTimeLineTask;
	private GetRoomListTask mGetRoomListTask;
	private boolean mIsLoaded = false;

	private Button mShowRoomList;
	private Button mReload;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_time_line);
		setupView(savedInstanceState);

		SharedPreferences sharedPreferences = getSharedPreferences("oauth", Context.MODE_PRIVATE);
		OAuthTokenCredential oAuthTokenCredential = new OAuthTokenCredential();
		oAuthTokenCredential.setToken(sharedPreferences.getString("token", ""));
		oAuthTokenCredential.setTokenSecret(sharedPreferences.getString("tokenSecret", ""));
		mYouRoomClient = YouRoomClientBuilder.createYouRoomClient();
		mYouRoomClient.setOAuthTokenCredential(oAuthTokenCredential);

		registerForContextMenu(getListView());
	}

	private void setupView(final Bundle savedInstanceState) {
		mReload = (Button) findViewById(R.id.reload);
		mReload.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getTimeLine();
			}
		});
		mShowRoomList = (Button) findViewById(R.id.show_room_list);
		mShowRoomList.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_ROOM_LIST_ID, savedInstanceState);
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		// AsyncTaskはActivity抜けるときにはとめた方がいいだろう。ずっと動くものならServiceにすべきでそうでない非同期処理は画面に従属するのだから
		// 画面から離れたらとめるべき。ホーム画面に移ったのにバックでまだなんか動いてるってのはキモい。
		// で、そうすると、非同期処理実行中に例えばHOMEキー押した場合、再開してもonCreateはよばれないので処理がとまってしまうので、
		// onResumeでの実装が必要となる。
		if (!mIsLoaded) {
			getTimeLine();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		dismissDialog(DIALOG_ROOM_LIST_ID);
		cancelGetTimeLineTask();
		cancelGetRoomListTask();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mGetTimeLineTask != null && mGetTimeLineTask.getStatus() == AsyncTask.Status.RUNNING) {
			outState.putBoolean(GET_TIME_LINE_TASK_STATUS_RUNNING, true);
		} else {
			outState.putBoolean(GET_TIME_LINE_TASK_STATUS_RUNNING, false);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState.getBoolean(GET_TIME_LINE_TASK_STATUS_RUNNING)) {
			mIsLoaded = false;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		Dialog dialog = null;
		switch (id) {
		case DIALOG_ROOM_LIST_ID:
			final View layoutView = getLayoutInflater().inflate(R.layout.room_list_dialog, null);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			dialog =
				builder
					.setTitle(getString(R.string.dialog_room_list_title))
					.setCancelable(true)
					.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.setView(layoutView)
					.create();
			break;
		default:
			break;
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
		switch (id) {
		case DIALOG_ROOM_LIST_ID:
			getRoomList(dialog);
			break;
		default:
			break;
		}
	}

	private void getTimeLine() {
		if (mGetTimeLineTask == null || mGetTimeLineTask.getStatus() != AsyncTask.Status.RUNNING) {
			mReload.setEnabled(false);

			mItems = new ArrayList<Entry>();
			mAdapter = new TimeLineListAdapter(getApplicationContext(), mItems);

			mGetTimeLineTask = new GetTimeLineTask(this, mAdapter);
			mGetTimeLineTask.execute();
		}
	}

	private void cancelGetTimeLineTask() {
		if (mGetTimeLineTask != null && mGetTimeLineTask.getStatus() == AsyncTask.Status.RUNNING) {
			mGetTimeLineTask.cancel(true);
			mGetTimeLineTask = null;
		}
	}

	private void getRoomList(Dialog dialog) {
		if (mGetRoomListTask == null || mGetRoomListTask.getStatus() != AsyncTask.Status.RUNNING) {
			mGetRoomListTask = new GetRoomListTask(this, dialog);
			mGetRoomListTask.execute();
		}
	}

	private void cancelGetRoomListTask() {
		if (mGetRoomListTask != null && mGetRoomListTask.getStatus() == AsyncTask.Status.RUNNING) {
			mGetRoomListTask.cancel(true);
			mGetRoomListTask = null;
		}
	}

	// FIXME
	// Android標準UI的にはコンテキストメニュー表示は長押しだが、ここではタップ時の動きがないのと、タップの方が操作性がよいと思うので長押しではなくタップにする
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);

		// FIXME 3はコメントがある場合のみ。
		// FIXME 定数化
		menu.add(Menu.NONE, 1, Menu.NONE, "編集する");
		menu.add(Menu.NONE, 2, Menu.NONE, "削除する");
		menu.add(Menu.NONE, 3, Menu.NONE, "コメントを見る");
		menu.add(Menu.NONE, 4, Menu.NONE, "コメントする");
		menu.add(Menu.NONE, 5, Menu.NONE, "このエントリを共有する");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo adapterinfo = (AdapterContextMenuInfo) item.getMenuInfo();
		Entry entry = (Entry) getListAdapter().getItem(adapterinfo.position);
		switch (item.getItemId()) {
		case 1:
			// FIXME 編集画面へインテント
			break;
		case 2:
			// FIXME 削除確認、削除処理実装
			break;
		case 3:
			// FIXME 詳細画面へインテント
			break;
		case 4:
			// FIXME コメント入力画面へインテント
			break;
		case 5:
			// FIXME 共有インテント
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * ルーム一覧ダイアログにルーム一覧データをセットアップします。<br>
	 *
	 * @param dialog ダイアログ
	 * @param groupList ルーム一覧
	 */
	public void setupRoomListDialog(Dialog dialog, List<Group> groupList) {
		RoomListAdapter adapter = new RoomListAdapter(getApplicationContext(), groupList);
		ListView listView = (ListView) dialog.findViewById(R.id.room_list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				Group group = (Group) listView.getItemAtPosition(position);
				// FIXME ルームTLアクティビティへインテント
			}
		});
		ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.progressBar);
		progressBar.setVisibility(View.GONE);
	}

	// Activityのライフサイクルに合わせてTaskのライフサイクルを制御する実装が漏れた場合に、
	// インナークラスによるエンクロージングクラスのインスタンスへの暗黙的な参照が残ってしまい、ActivityがGCされなくなることを防止するために
	// staticなインナークラスとし、Activityへの参照を弱参照にする。
	private static class GetTimeLineTask extends AsyncTask<Void, Integer, TimeLineListAdapter> {

		private WeakReference<HomeTimeLineActivity> mHomeTimeLineActivity;
		private TimeLineListAdapter mAdapter;

		// FIXME ルーム一覧ダイアログ表示のため、どのみちListViewが必要になっているが、
		// 一方はListActivityで実装し、もう一方はListViewというのは見にくいのでListViewに統一する。
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
					List<Entry> entryList = homeTimeLineActivity.mYouRoomClient.getHomeTimeLine();
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
					homeTimeLineActivity.mIsLoaded = true;
					homeTimeLineActivity.mReload.setEnabled(true);
				}
			}
		}

		@Override
		protected void onCancelled() {
			final HomeTimeLineActivity homeTimeLineActivity = mHomeTimeLineActivity.get();
			if (homeTimeLineActivity != null) {
				homeTimeLineActivity.mGetTimeLineTask = null;
			}
		}
	}

	private static class GetRoomListTask extends AsyncTask<Void, Integer, List<Group>> {

		private WeakReference<HomeTimeLineActivity> mHomeTimeLineActivity;
		private WeakReference<Dialog> mDialog;

		private GetRoomListTask(HomeTimeLineActivity homeTimeLineActivity, Dialog dialog) {
			mHomeTimeLineActivity = new WeakReference<HomeTimeLineActivity>(homeTimeLineActivity);
			mDialog = new WeakReference<Dialog>(dialog);
		}

		/*
		 * バックグラウンドでデータを取得します。<br>
		 */
		@Override
		protected List<Group> doInBackground(Void... params) {
			final HomeTimeLineActivity homeTimeLineActivity = mHomeTimeLineActivity.get();
			if (homeTimeLineActivity != null) {
				try {
					return homeTimeLineActivity.mYouRoomClient.getMyGroups();
				} catch (IOException e) {
					// FIXME
					Toast.makeText(
						homeTimeLineActivity.getApplicationContext(),
						"YouRoomアクセスでエラーが発生しました。",
						Toast.LENGTH_LONG).show();
				}
			}
			return null;
		}

		/*
		 * データ取得後、表示を行います。<br>
		 */
		@Override
		protected void onPostExecute(List<Group> groupList) {
			if (groupList != null) {
				final HomeTimeLineActivity homeTimeLineActivity = mHomeTimeLineActivity.get();
				final Dialog dialog = mDialog.get();
				if (homeTimeLineActivity != null && dialog != null) {
					homeTimeLineActivity.setupRoomListDialog(dialog, groupList);
				}
			}
		}

		@Override
		protected void onCancelled() {
			final HomeTimeLineActivity homeTimeLineActivity = mHomeTimeLineActivity.get();
			if (homeTimeLineActivity != null) {
				homeTimeLineActivity.mGetRoomListTask = null;
			}
		}
	}

}