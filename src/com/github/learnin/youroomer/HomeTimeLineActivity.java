package com.github.learnin.youroomer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import youroom4j.Entry;
import youroom4j.Group;
import youroom4j.YouRoom4JException;
import youroom4j.YouRoomClient;
import youroom4j.oauth.OAuthTokenCredential;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

// FIXME 他の画面も含めて全般的に、プリファレンスに保存されているOAuthクレデンシャルで401エラーになった場合(YouRoomClientで特定例外スロー？)は、OAuth認証フローへ導くようにする。
// FIXME プログレス表示
public class HomeTimeLineActivity extends Activity {

	private static final String GET_TIME_LINE_TASK_STATUS_RUNNING =
		"com.github.learnin.youroomer.HomeTimeLineActivity.GET_TIME_LINE_TASK_STATUS_RUNNING";

	private static final int DIALOG_CONTEXT_MENU_ID = 0;
	private static final int DIALOG_ROOM_LIST_ID = 1;

	private static final int MENU_ITEM_EDIT_ID = 0;
	private static final int MENU_ITEM_DELETE_ID = 1;
	private static final int MENU_ITEM_SHOW_COMMENT_ID = 2;
	private static final int MENU_ITEM_DO_COMMENT_ID = 3;
	private static final int MENU_ITEM_SHARE_ID = 4;

	private YouRoomClient mYouRoomClient;
	private GetTimeLineTask mGetTimeLineTask;
	private GetRoomListTask mGetRoomListTask;
	private boolean mIsLoaded = false;
	private Dialog mContextMenuDialog;
	private Dialog mRoomListDialog;

	private ListView mListView;
	private Button mShowRoomList;
	private Button mReload;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_time_line);
		setupView(savedInstanceState);

		// FIXME YouRoomClient使用箇所で毎回以下を書くのは面倒。共通化するなり保持させるなりする。
		SharedPreferences sharedPreferences = getSharedPreferences("oauth", Context.MODE_PRIVATE);
		OAuthTokenCredential oAuthTokenCredential = new OAuthTokenCredential();
		oAuthTokenCredential.setToken(sharedPreferences.getString("token", ""));
		oAuthTokenCredential.setTokenSecret(sharedPreferences.getString("tokenSecret", ""));
		mYouRoomClient = YouRoomClientBuilder.createYouRoomClient();
		mYouRoomClient.setOAuthTokenCredential(oAuthTokenCredential);
	}

	private void setupView(final Bundle savedInstanceState) {
		mListView = (ListView) findViewById(R.id.entry_list);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				Entry entry = (Entry) listView.getItemAtPosition(position);
				Bundle bundle = new Bundle();
				if (savedInstanceState != null) {
					bundle.putAll(savedInstanceState);
				}
				bundle.putSerializable("ENTRY", entry);
				showDialog(DIALOG_CONTEXT_MENU_ID, bundle);
			}
		});

		mReload = (Button) findViewById(R.id.reload_button);
		mReload.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getTimeLine();
			}
		});

		mShowRoomList = (Button) findViewById(R.id.show_room_list_button);
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
		dismissDialog();
		cancelGetTimeLineTask();
		cancelGetRoomListTask();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		removeDialog(DIALOG_CONTEXT_MENU_ID);
		removeDialog(DIALOG_ROOM_LIST_ID);
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
		switch (id) {
		case DIALOG_CONTEXT_MENU_ID:
			final View ContextMenuDialogView = getLayoutInflater().inflate(R.layout.context_menu_dialog, null);
			AlertDialog.Builder contextMenuDialogBuilder = new AlertDialog.Builder(this);
			mContextMenuDialog =
				contextMenuDialogBuilder
					.setCancelable(true)
					.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.setView(ContextMenuDialogView)
					.create();

			ListView contextMenuItemListView =
				(ListView) ContextMenuDialogView.findViewById(R.id.context_menu_item_list);
			contextMenuItemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					ListView listView = (ListView) parent;
					MenuItem menuItem = (MenuItem) listView.getItemAtPosition(position);
					Entry entry = menuItem.getEntry();
					Intent intent;
					switch (menuItem.getId()) {
					case MENU_ITEM_EDIT_ID:
						if (entry != null) {
							String content = entry.getContent();
							intent = new Intent(getApplicationContext(), EditEntryActivity.class);
							intent.putExtra("ACTION", "UPDATE");
							intent.putExtra("GROUP_TO_PARAM", entry.getParticipation().getGroup().getToParam());
							intent.putExtra("ID", entry.getId());
							intent.putExtra("CONTENT", content);
							startActivity(intent);
						}
						break;
					case MENU_ITEM_DELETE_ID:
						// FIXME 削除確認、削除処理実装
						break;
					case MENU_ITEM_SHOW_COMMENT_ID:
						// FIXME 詳細画面へインテント
						break;
					case MENU_ITEM_DO_COMMENT_ID:
						// FIXME コメント入力画面へインテント
						break;
					case MENU_ITEM_SHARE_ID:
						if (entry != null) {
							String content = entry.getContent();
							intent = new Intent(Intent.ACTION_SEND);
							intent.setType("text/plain");
							intent.putExtra(Intent.EXTRA_TEXT, content);
							try {
								startActivity(Intent.createChooser(
									intent,
									getString(R.string.title_of_action_send_intent)));
							} catch (android.content.ActivityNotFoundException e) {
								// FIXME
								// 該当するActivityがないときの処理。事前にあるか調べてからインテントする方がよいか？
							}
						}
						break;
					default:
						break;
					}
				}
			});
			return mContextMenuDialog;
		case DIALOG_ROOM_LIST_ID:
			final View layoutView = getLayoutInflater().inflate(R.layout.room_list_dialog, null);
			AlertDialog.Builder roomListDialogBuilder = new AlertDialog.Builder(this);
			mRoomListDialog =
				roomListDialogBuilder
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

			ListView roomListView = (ListView) layoutView.findViewById(R.id.room_list);
			roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					ListView listView = (ListView) parent;
					Group group = (Group) listView.getItemAtPosition(position);
					Intent intent = new Intent(getApplicationContext(), RoomTimeLineActivity.class);
					intent.putExtra("GROUP_TO_PARAM", group.getToParam());
					startActivity(intent);
				}
			});
			return mRoomListDialog;
		default:
			return null;
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
		switch (id) {
		case DIALOG_CONTEXT_MENU_ID:
			List<MenuItem> menuItemList = new ArrayList<MenuItem>();
			Entry entry = null;
			if (bundle != null && bundle.getSerializable("ENTRY") != null) {
				entry = (Entry) bundle.getSerializable("ENTRY");
			}

			if (entry != null && entry.isUpdatable()) {
				MenuItem menuItem = new MenuItem();
				menuItem.setId(MENU_ITEM_EDIT_ID);
				menuItem.setText("編集する");
				menuItem.setEntry(entry);
				menuItemList.add(menuItem);

				MenuItem menuItem2 = new MenuItem();
				menuItem2.setId(MENU_ITEM_DELETE_ID);
				menuItem2.setText("削除する");
				menuItem2.setEntry(entry);
				menuItemList.add(menuItem2);
			}

			if (entry != null && entry.getChildren() != null && !entry.getChildren().isEmpty()) {
				MenuItem menuItem3 = new MenuItem();
				menuItem3.setId(MENU_ITEM_SHOW_COMMENT_ID);
				menuItem3.setText("コメントを見る");
				menuItem3.setEntry(entry);
				menuItemList.add(menuItem3);
			}

			MenuItem menuItem4 = new MenuItem();
			menuItem4.setId(MENU_ITEM_DO_COMMENT_ID);
			menuItem4.setText("コメントする");
			menuItem4.setEntry(entry);
			menuItemList.add(menuItem4);

			MenuItem menuItem5 = new MenuItem();
			menuItem5.setId(MENU_ITEM_SHARE_ID);
			menuItem5.setText("このエントリを共有する");
			menuItem5.setEntry(entry);
			menuItemList.add(menuItem5);

			ListView contextMenuItemListView = (ListView) dialog.findViewById(R.id.context_menu_item_list);
			contextMenuItemListView.setAdapter(new ContextMenuItemListAdapter(getApplicationContext(), menuItemList));
			break;
		case DIALOG_ROOM_LIST_ID:
			getRoomList(dialog);
			break;
		default:
			break;
		}
	}

	private void dismissDialog() {
		if (mContextMenuDialog != null) {
			dismissDialog(DIALOG_CONTEXT_MENU_ID);
		}
		if (mRoomListDialog != null) {
			dismissDialog(DIALOG_ROOM_LIST_ID);
		}
	}

	private void getTimeLine() {
		if (mGetTimeLineTask == null || mGetTimeLineTask.getStatus() != AsyncTask.Status.RUNNING) {
			mReload.setEnabled(false);
			mGetTimeLineTask = new GetTimeLineTask(this);
			mGetTimeLineTask.execute();
		}
	}

	private void cancelGetTimeLineTask() {
		if (mGetTimeLineTask != null && mGetTimeLineTask.getStatus() == AsyncTask.Status.RUNNING) {
			mGetTimeLineTask.cancel(true);
		}
		mGetTimeLineTask = null;
		mReload.setEnabled(true);
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
		}
		mGetRoomListTask = null;
	}

	/**
	 * エントリ一覧を表示します。<br>
	 *
	 * @param entryList エントリ一覧
	 */
	private void showEntryList(List<Entry> entryList) {
		mListView.setAdapter(new HomeTimeLineListAdapter(getApplicationContext(), entryList));
		// FIXME 進捗バー
	}

	/**
	 * ルーム一覧ダイアログにルーム一覧を表示します。<br>
	 *
	 * @param dialog ダイアログ
	 * @param groupList ルーム一覧
	 */
	private void showRoomListOnDialog(Dialog dialog, List<Group> groupList) {
		ListView listView = (ListView) dialog.findViewById(R.id.room_list);
		listView.setAdapter(new RoomListAdapter(getApplicationContext(), groupList));
		ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.progressBar);
		progressBar.setVisibility(View.GONE);
	}

	// Activityのライフサイクルに合わせてTaskのライフサイクルを制御する実装が漏れた場合に、
	// インナークラスによるエンクロージングクラスのインスタンスへの暗黙的な参照が残ってしまい、ActivityがGCされなくなることを防止するために
	// staticなインナークラスとし、Activityへの参照を弱参照にする。
	private static class GetTimeLineTask extends AsyncTask<Void, Integer, List<Entry>> {

		private WeakReference<HomeTimeLineActivity> mHomeTimeLineActivity;

		private GetTimeLineTask(HomeTimeLineActivity homeTimeLineActivity) {
			mHomeTimeLineActivity = new WeakReference<HomeTimeLineActivity>(homeTimeLineActivity);
		}

		/*
		 * バックグラウンドでデータを取得します。<br>
		 */
		@Override
		protected List<Entry> doInBackground(Void... params) {
			final HomeTimeLineActivity homeTimeLineActivity = mHomeTimeLineActivity.get();
			if (homeTimeLineActivity != null) {
				try {
					// FIXME ページング対応
					return homeTimeLineActivity.mYouRoomClient.getHomeTimeLine();
				} catch (YouRoom4JException e) {
					// FIXME
					e.printStackTrace();
				}
			}
			return null;
		}

		/*
		 * データ取得後、表示を行います。<br>
		 */
		@Override
		protected void onPostExecute(List<Entry> entryList) {
			final HomeTimeLineActivity homeTimeLineActivity = mHomeTimeLineActivity.get();
			if (homeTimeLineActivity != null) {
				if (entryList != null) {
					homeTimeLineActivity.showEntryList(entryList);
					homeTimeLineActivity.mIsLoaded = true;
				} else {
					Toast.makeText(
						homeTimeLineActivity.getApplicationContext(),
						"YouRoomアクセスでエラーが発生しました。",
						Toast.LENGTH_LONG).show();
				}
				homeTimeLineActivity.mReload.setEnabled(true);
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
					// FIXME
					// YouRoomClient実行時のRuntimeException発生時の考慮が必要。YouRoom4J側ではcatchしないので呼び出し側で個別にcatchするか、例外ハンドラで実装するか
				} catch (YouRoom4JException e) {
					// FIXME
					e.printStackTrace();
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
					homeTimeLineActivity.showRoomListOnDialog(dialog, groupList);
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