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

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

import youroom4j.Entry;
import youroom4j.Group;
import youroom4j.YouRoom4JException;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

// FIXME 他の画面も含めて全般的に、プリファレンスに保存されているOAuthクレデンシャルで401エラーになった場合(YouRoomClientで特定例外スロー？)は、OAuth認証フローへ導くようにする。
// FIXME プログレス表示
public class HomeTimeLineActivity extends AbstractTimeLineActivity {

	private static final int DIALOG_ROOM_LIST_ID = 2;

	private GetRoomListTask mGetRoomListTask;
	private Dialog mRoomListDialog;

	private Button mShowRoomList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_time_line);
		setupView(savedInstanceState);
		setupYouRoomClient();
	}

	protected void setupView(final Bundle savedInstanceState) {
		super.setupView(savedInstanceState);

		mShowRoomList = (Button) findViewById(R.id.show_room_list_button);
		mShowRoomList.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				showDialog(DIALOG_ROOM_LIST_ID, savedInstanceState);
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		cancelGetRoomListTask();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UserImageCache.getInstance().clear();
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		switch (id) {
		case DIALOG_CONTEXT_MENU_ID:
			return createContextMenuDialog(bundle);
		case DIALOG_CONFIRM_DESTROY_ENTRY_ID:
			return createConfirmDestroyEntryDialog();
		case DIALOG_ROOM_LIST_ID:
			return createRoomListDialog();
		default:
			return null;
		}
	}

	private Dialog createRoomListDialog() {
		final View layoutView = getLayoutInflater().inflate(R.layout.room_list_dialog, null);
		AlertDialog.Builder roomListDialogBuilder = new AlertDialog.Builder(this);
		mRoomListDialog =
			roomListDialogBuilder
				.setTitle(getString(R.string.dialog_room_list_title))
				.setCancelable(true)
				.setPositiveButton(getString(R.string.close), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				})
				.setView(layoutView)
				.create();

		ListView roomListView = (ListView) layoutView.findViewById(R.id.room_list);
		roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				Group group = (Group) listView.getItemAtPosition(position);
				Intent intent = new Intent(getApplicationContext(), RoomTimeLineActivity.class);
				intent.putExtra("GROUP_TO_PARAM", group.getToParam());
				startActivity(intent);
			}
		});
		return mRoomListDialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
		switch (id) {
		case DIALOG_CONTEXT_MENU_ID:
			prepareContextMenuDialog(dialog, bundle);
			break;
		case DIALOG_ROOM_LIST_ID:
			getRoomList(dialog);
			break;
		default:
			break;
		}
	}

	protected void dismissDialog() {
		super.dismissDialog();
		if (mRoomListDialog != null) {
			dismissDialog(DIALOG_ROOM_LIST_ID);
		}
	}

	@Override
	protected void removeDialog() {
		super.removeDialog();
		removeDialog(DIALOG_ROOM_LIST_ID);
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

	protected ListAdapter createTimeLineListAdapter(Context context, List<Entry> entryList) {
		return new HomeTimeLineListAdapter(context, entryList);
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

	protected List<Entry> doGetTimeLine(int page) throws YouRoom4JException {
		List<Entry> entryList = mYouRoomClient.getHomeTimeLine(page);
		// アーカイブされたエントリを削除
		for (Iterator<Entry> it = entryList.iterator(); it.hasNext();) {
			if (it.next().hasRead()) {
				it.remove();
			}
		}
		return entryList;
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
				}
			}
			return null;
		}

		/*
		 * データ取得後、表示を行います。<br>
		 */
		@Override
		protected void onPostExecute(List<Group> groupList) {
			final HomeTimeLineActivity homeTimeLineActivity = mHomeTimeLineActivity.get();
			if (homeTimeLineActivity != null) {
				if (groupList != null) {
					final Dialog dialog = mDialog.get();
					if (dialog != null) {
						homeTimeLineActivity.showRoomListOnDialog(dialog, groupList);
					}
				} else {
					Toast.makeText(
						homeTimeLineActivity.getApplicationContext(),
						"YouRoomアクセスでエラーが発生しました。",
						Toast.LENGTH_LONG).show();
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