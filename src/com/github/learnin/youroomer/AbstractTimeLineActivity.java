package com.github.learnin.youroomer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import youroom4j.Entry;
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
import android.widget.Toast;

// FIXME 他の画面も含めて全般的に、プリファレンスに保存されているOAuthクレデンシャルで401エラーになった場合(YouRoomClientで特定例外スロー？)は、OAuth認証フローへ導くようにする。
// FIXME プログレス表示
public abstract class AbstractTimeLineActivity extends Activity {

	private static final String GET_TIME_LINE_TASK_STATUS_RUNNING = "com.github.learnin.youroomer.AbstractTimeLineActivity.GET_TIME_LINE_TASK_STATUS_RUNNING";

	protected static final int DIALOG_CONTEXT_MENU_ID = 0;
	protected static final int DIALOG_CONFIRM_DESTROY_ENTRY_ID = 1;

	protected static final int MENU_ITEM_EDIT_ID = 0;
	protected static final int MENU_ITEM_DESTROY_ID = 1;
	protected static final int MENU_ITEM_SHOW_COMMENT_ID = 2;
	protected static final int MENU_ITEM_DO_COMMENT_ID = 3;
	protected static final int MENU_ITEM_SHARE_ID = 4;

	protected YouRoomClient mYouRoomClient;
	protected boolean mIsLoaded = false;
	private Dialog mContextMenuDialog;
	private Dialog mConfirmDestroyEntryDialog;
	private long mTargetEntryId;
	private String mTargetGroupToParam;

	private GetTimeLineTask mGetTimeLineTask;
	private DestroyEntryTask mDestroyEntryTask;

	protected ListView mListView;
	protected Button mReload;

	protected void setupYouRoomClient() {
		// FIXME YouRoomClient使用箇所で毎回以下を書くのは面倒。共通化するなり保持させるなりする。
		SharedPreferences sharedPreferences = getSharedPreferences("oauth",
				Context.MODE_PRIVATE);
		OAuthTokenCredential oAuthTokenCredential = new OAuthTokenCredential();
		oAuthTokenCredential.setToken(sharedPreferences.getString("token", ""));
		oAuthTokenCredential.setTokenSecret(sharedPreferences.getString(
				"tokenSecret", ""));
		mYouRoomClient = YouRoomClientBuilder.createYouRoomClient();
		mYouRoomClient.setOAuthTokenCredential(oAuthTokenCredential);
	}

	protected void setupView(final Bundle savedInstanceState) {
		mListView = (ListView) findViewById(R.id.entry_list);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView listView = (ListView) parent;
				Entry entry = (Entry) listView.getItemAtPosition(position);
				Bundle bundle = new Bundle();
				if (savedInstanceState != null) {
					bundle.putAll(savedInstanceState);
				}
				bundle.putSerializable("ENTRY", entry);
				mTargetEntryId = entry.getId();
				mTargetGroupToParam = entry.getParticipation().getGroup()
						.getToParam();
				showDialog(DIALOG_CONTEXT_MENU_ID, bundle);
			}
		});

		mReload = (Button) findViewById(R.id.reload_button);
		mReload.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
//				UserImageCache.getInstance().clear();
				doGetTimeLineTask();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		dismissDialog();
		cancelGetTimeLineTask();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		removeDialog();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mGetTimeLineTask != null
				&& mGetTimeLineTask.getStatus() == AsyncTask.Status.RUNNING) {
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

	protected Dialog createContextMenuDialog(final Bundle bundle) {
		final View ContextMenuDialogView = getLayoutInflater().inflate(
				R.layout.context_menu_dialog, null);
		AlertDialog.Builder contextMenuDialogBuilder = new AlertDialog.Builder(
				this);
		mContextMenuDialog = contextMenuDialogBuilder
				.setCancelable(true)
				.setPositiveButton(getString(R.string.close),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						}).setView(ContextMenuDialogView).create();

		ListView contextMenuItemListView = (ListView) ContextMenuDialogView
				.findViewById(R.id.context_menu_item_list);
		contextMenuItemListView
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						ListView listView = (ListView) parent;
						MenuItem menuItem = (MenuItem) listView
								.getItemAtPosition(position);
						Entry entry = menuItem.getEntry();
						if (entry != null) {
							switch (menuItem.getId()) {
							case MENU_ITEM_EDIT_ID:
								goEditEntry(entry);
								break;
							case MENU_ITEM_DESTROY_ID:
								showDialog(DIALOG_CONFIRM_DESTROY_ENTRY_ID,
										bundle);
								break;
							case MENU_ITEM_SHOW_COMMENT_ID:
								goShowComment(entry);
								break;
							case MENU_ITEM_DO_COMMENT_ID:
								// FIXME コメント入力画面へインテント
								break;
							case MENU_ITEM_SHARE_ID:
								goShareEntry(entry);
								break;
							default:
								break;
							}
						}
					}
				});
		return mContextMenuDialog;
	}

	protected Dialog createConfirmDestroyEntryDialog() {
		AlertDialog.Builder confirmDestroyEntryDialogBuilder = new AlertDialog.Builder(
				this);
		mConfirmDestroyEntryDialog = confirmDestroyEntryDialogBuilder
				.setMessage(R.string.confirm_delete_entry)
				.setCancelable(true)
				.setPositiveButton(R.string.delete,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								doDestroyEntry();
								dismissDialog(DIALOG_CONTEXT_MENU_ID);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								dismissDialog(DIALOG_CONTEXT_MENU_ID);
							}
						}).create();
		return mConfirmDestroyEntryDialog;
	}

	protected void prepareContextMenuDialog(Dialog dialog, Bundle bundle) {
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
			menuItem2.setId(MENU_ITEM_DESTROY_ID);
			menuItem2.setText("削除する");
			menuItem2.setEntry(entry);
			menuItemList.add(menuItem2);
		}

		if (entry != null && entry.getDescendantsCount() > 0) {
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

		ListView contextMenuItemListView = (ListView) dialog
				.findViewById(R.id.context_menu_item_list);
		contextMenuItemListView.setAdapter(new ContextMenuItemListAdapter(
				getApplicationContext(), menuItemList));
	}

	private void goEditEntry(Entry entry) {
		String content = entry.getContent();
		Intent intent = new Intent(getApplicationContext(),
				EditEntryActivity.class);
		intent.setAction("UPDATE");
		intent.putExtra("GROUP_TO_PARAM", entry.getParticipation().getGroup()
				.getToParam());
		intent.putExtra("ID", entry.getId());
		intent.putExtra("CONTENT", content);
		startActivity(intent);
	}

	private void goShowComment(Entry entry) {
		Intent intent = new Intent(getApplicationContext(),
				ShowEntryActivity.class);
		intent.putExtra("GROUP_TO_PARAM", entry.getParticipation().getGroup()
				.getToParam());
		intent.putExtra("ID", entry.getId());
		startActivity(intent);
	}

	private void goShareEntry(Entry entry) {
		String content = entry.getContent();
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, content);
		try {
			startActivity(Intent.createChooser(intent,
					getString(R.string.title_of_action_send_intent)));
		} catch (android.content.ActivityNotFoundException e) {
			// FIXME
			// 該当するActivityがないときの処理。事前にあるか調べてからインテントする方がよいか？
		}
	}

	private void doDestroyEntry() {
		mDestroyEntryTask = new DestroyEntryTask(this);
		mDestroyEntryTask.execute(mTargetEntryId);
	}

	protected void dismissDialog() {
		if (mContextMenuDialog != null) {
			dismissDialog(DIALOG_CONTEXT_MENU_ID);
		}
		if (mConfirmDestroyEntryDialog != null) {
			dismissDialog(DIALOG_CONFIRM_DESTROY_ENTRY_ID);
		}
	}

	protected void removeDialog() {
		removeDialog(DIALOG_CONTEXT_MENU_ID);
		removeDialog(DIALOG_CONFIRM_DESTROY_ENTRY_ID);
	}

	protected void doGetTimeLineTask() {
		if (mGetTimeLineTask == null
				|| mGetTimeLineTask.getStatus() != AsyncTask.Status.RUNNING) {
			mReload.setEnabled(false);
			mGetTimeLineTask = new GetTimeLineTask(this);
			mGetTimeLineTask.execute();
		}
	}

	protected void cancelGetTimeLineTask() {
		if (mGetTimeLineTask != null
				&& mGetTimeLineTask.getStatus() == AsyncTask.Status.RUNNING) {
			mGetTimeLineTask.cancel(true);
		}
		mGetTimeLineTask = null;
		mReload.setEnabled(true);
	}

	abstract protected List<Entry> doGetTimeLine() throws YouRoom4JException;

	/**
	 * エントリ一覧を表示します。<br>
	 *
	 * @param entryList
	 *            エントリ一覧
	 */
	abstract protected void showEntryList(List<Entry> entryList);

	private void afterDestroyEntry() {
		Toast.makeText(getApplicationContext(), R.string.deleted,
				Toast.LENGTH_SHORT).show();
	}

	// TODO Support library使って、AsyncTaskLoader使うようにして可能なら外出してHomeTLのTaskと共通化する。
	private static class GetTimeLineTask extends
			AsyncTask<Void, Integer, List<Entry>> {

		private WeakReference<AbstractTimeLineActivity> mTimeLineActivity;

		private GetTimeLineTask(AbstractTimeLineActivity timeLineActivity) {
			mTimeLineActivity = new WeakReference<AbstractTimeLineActivity>(
					timeLineActivity);
		}

		/*
		 * バックグラウンドでデータを取得します。<br>
		 */
		@Override
		protected List<Entry> doInBackground(Void... params) {
			final AbstractTimeLineActivity timeLineActivity = mTimeLineActivity
					.get();
			if (timeLineActivity != null) {
				try {
					return timeLineActivity.doGetTimeLine();
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
			final AbstractTimeLineActivity timeLineActivity = mTimeLineActivity
					.get();
			if (timeLineActivity != null) {
				if (entryList != null) {
					timeLineActivity.showEntryList(entryList);
					timeLineActivity.mIsLoaded = true;
				} else {
					Toast.makeText(timeLineActivity.getApplicationContext(),
							"YouRoomアクセスでエラーが発生しました。", Toast.LENGTH_LONG)
							.show();
				}
				timeLineActivity.mReload.setEnabled(true);
			}
		}

		@Override
		protected void onCancelled() {
			final AbstractTimeLineActivity timeLineActivity = mTimeLineActivity
					.get();
			if (timeLineActivity != null) {
				timeLineActivity.mGetTimeLineTask = null;
			}
		}
	}

	// TODO Support library使って、AsyncTaskLoader使うようにして可能なら外出してHomeTLのTaskと共通化する。
	private static class DestroyEntryTask extends
			AsyncTask<Long, Integer, Boolean> {

		private WeakReference<AbstractTimeLineActivity> mTimeLineActivity;

		private DestroyEntryTask(AbstractTimeLineActivity timeLineActivity) {
			mTimeLineActivity = new WeakReference<AbstractTimeLineActivity>(
					timeLineActivity);
		}

		@Override
		protected Boolean doInBackground(Long... params) {
			final AbstractTimeLineActivity timeLineActivity = mTimeLineActivity
					.get();
			if (timeLineActivity != null) {
				try {
					timeLineActivity.mYouRoomClient.destroyEntry(
							timeLineActivity.mTargetGroupToParam, params[0]);
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
			final AbstractTimeLineActivity timeLineActivity = mTimeLineActivity
					.get();
			if (timeLineActivity != null) {
				if (result) {
					timeLineActivity.afterDestroyEntry();
				} else {
					Toast.makeText(timeLineActivity.getApplicationContext(),
							"YouRoomアクセスでエラーが発生しました。", Toast.LENGTH_LONG)
							.show();
				}
			}
		}

		@Override
		protected void onCancelled() {
			final AbstractTimeLineActivity timeLineActivity = mTimeLineActivity
					.get();
			if (timeLineActivity != null) {
				timeLineActivity.mDestroyEntryTask = null;
			}
		}
	}

}