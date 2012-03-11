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

import java.util.List;

import youroom4j.Entry;
import youroom4j.YouRoom4JException;
import android.content.Context;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

// FIXME 他の画面も含めて全般的に、プリファレンスに保存されているOAuthクレデンシャルで401エラーになった場合(YouRoomClientで特定例外スロー？)は、OAuth認証フローへ導くようにする。
// FIXME プログレス表示
public abstract class AbstractTimeLineActivity extends AbstractListActivity {

	private static final String NOW_PAGE = "com.github.learnin.youroomer.AbstractTimeLineActivity.NOW_PAGE";

	protected int mNowPage = 1;
	protected boolean mExistsMoreEntry = true;
	private int mPrevTotalCount = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mNowPage = 1;
		mExistsMoreEntry = true;
		mPrevTotalCount = 0;
	}

	protected void setupView(final Bundle savedInstanceState) {
		super.setupView(savedInstanceState);
		// フッターの追加
		mListViewFooter = getLayoutInflater().inflate(R.layout.listview_footer, null);
		mListView.addFooterView(mListViewFooter);

		mListView.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView abslistview, int i) {
			}

			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (!mExistsMoreEntry) {
					mListView.removeFooterView(mListViewFooter);
				} else if (totalItemCount > mPrevTotalCount && totalItemCount == firstVisibleItem + visibleItemCount) {
					// 末尾までスクロールされたので、自動的にページングを行う
					mPrevTotalCount = totalItemCount;
					mNowPage++;
					doGetTimeLineTask();
				}
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(NOW_PAGE, mNowPage);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mNowPage = savedInstanceState.getInt(NOW_PAGE, 1);
	}

	protected List<Entry> doGetTimeLine() throws YouRoom4JException {
		return doGetTimeLine(mNowPage);
	}

	abstract protected List<Entry> doGetTimeLine(int page) throws YouRoom4JException;

	/**
	 * エントリ一覧を表示します。<br>
	 *
	 * @param entryList エントリ一覧
	 */
	protected void showEntryList(List<Entry> entryList) {
		ArrayAdapter<Entry> adapter = getAdapter(mListView);
		if (adapter != null && entryList.isEmpty()) {
			mListView.removeFooterView(mListViewFooter);
			mExistsMoreEntry = false;
			return;
		}
		if (adapter == null) {
			mListView.setAdapter(createTimeLineListAdapter(getApplicationContext(), entryList));
		} else {
			for (Entry entry : entryList) {
				adapter.add(entry);
			}
			// データ追加反映時に、カーソルが画面上の1番上のデータにいくようにする
			int position = mListView.getFirstVisiblePosition();
			int y = mListView.getChildAt(0).getTop();
			adapter.notifyDataSetChanged();
			mListView.setSelectionFromTop(position, y);
		}
		// FIXME 進捗バー
	}

	abstract protected ListAdapter createTimeLineListAdapter(Context context, List<Entry> entryList);

}