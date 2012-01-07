package com.github.learnin.youroomer;

import java.util.ArrayList;
import java.util.List;

import youroom4j.Entry;
import youroom4j.YouRoom4JException;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

public class ShowEntryActivity extends AbstractTimeLineActivity {

	private String mGroupToParam = null;
	private long mRootEntryId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent != null) {
			mGroupToParam = intent.getStringExtra("GROUP_TO_PARAM");
			mRootEntryId = intent.getLongExtra("ID", 0L);
		}

		setContentView(R.layout.show_entry);
		setupView(savedInstanceState);
		setupYouRoomClient();
	}

	@Override
	public void onResume() {
		super.onResume();
		Intent intent = getIntent();
		if (intent != null) {
			mGroupToParam = intent.getStringExtra("GROUP_TO_PARAM");
		}
		if (!mIsLoaded) {
			doGetTimeLineTask();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// TODO DestroyEntryTaskもキャンセルするか要検討
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		switch (id) {
		case DIALOG_CONTEXT_MENU_ID:
			return createContextMenuDialog(bundle);
		case DIALOG_CONFIRM_DESTROY_ENTRY_ID:
			return createConfirmDestroyEntryDialog();
		default:
			return null;
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, final Bundle bundle) {
		switch (id) {
		case DIALOG_CONTEXT_MENU_ID:
			prepareContextMenuDialog(dialog, bundle);
			break;
		default:
			break;
		}
	}

	protected List<Entry> doGetTimeLine() throws YouRoom4JException {
		Entry entry = mYouRoomClient.showEntry(mGroupToParam, mRootEntryId);
		List<Entry> result = new ArrayList<Entry>();
		result.add(entry);
		setupCommentList(result, entry);
		return result;
	}

	// TODO この処理をYouRoomClientに移動して、この処理を通す版のshowEntryメソッドをつくるか検討する
	// Entry - Childの内包関係をフラットなリストに変換する
	private void setupCommentList(List<Entry> result, Entry entry) {
		List<Entry> children = entry.getChildren();
		if (children != null) {
			for (Entry child : children) {
				result.add(child);
				entry = child;
				setupCommentList(result, entry);
			}
		}
	}

	/**
	 * エントリ一覧を表示します。<br>
	 *
	 * @param entryList
	 *            エントリ一覧
	 */
	protected void showEntryList(List<Entry> entryList) {
		mListView.setAdapter(new EntryListAdapter(getApplicationContext(),
				entryList));
		// FIXME 進捗バー
	}

}
