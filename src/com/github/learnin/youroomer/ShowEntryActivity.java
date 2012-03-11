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

import java.util.ArrayList;
import java.util.List;

import youroom4j.Entry;
import youroom4j.YouRoom4JException;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.os.Bundle;
import android.widget.ListView;

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

	protected void setupView(final Bundle savedInstanceState) {
		super.setupView(savedInstanceState);
		// FIXME xml指定にする
		Drawable drawable = new PaintDrawable(0x80444444);
		mListView.setSelector(drawable);
		drawable.setCallback(null);
	}

	@Override
	public void onResume() {
		Intent intent = getIntent();
		if (intent != null) {
			mGroupToParam = intent.getStringExtra("GROUP_TO_PARAM");
		}
		super.onResume();
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

	@Override
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

		MenuItem menuItem3 = new MenuItem();
		menuItem3.setId(MENU_ITEM_DO_COMMENT_ID);
		menuItem3.setText("コメントする");
		menuItem3.setEntry(entry);
		menuItemList.add(menuItem3);

		MenuItem menuItem4 = new MenuItem();
		menuItem4.setId(MENU_ITEM_SHARE_ID);
		menuItem4.setText("このエントリを共有する");
		menuItem4.setEntry(entry);
		menuItemList.add(menuItem4);

		ListView contextMenuItemListView = (ListView) dialog.findViewById(R.id.context_menu_item_list);
		contextMenuItemListView.setAdapter(new ContextMenuItemListAdapter(getApplicationContext(), menuItemList));
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
	 * @param entryList エントリ一覧
	 */
	protected void showEntryList(List<Entry> entryList) {
		mListView.setAdapter(new EntryListAdapter(getApplicationContext(), entryList));
		// FIXME 進捗バー
	}

}
