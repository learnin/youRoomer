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
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;

public class RoomTimeLineActivity extends AbstractTimeLineActivity {

	private String mGroupToParam = null;

	private Button mCreateEntry;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if (intent != null) {
			mGroupToParam = intent.getStringExtra("GROUP_TO_PARAM");
		}

		setContentView(R.layout.room_time_line);
		setupView(savedInstanceState);
		setupYouRoomClient();
	}

	protected void setupView(final Bundle savedInstanceState) {
		super.setupView(savedInstanceState);

		mCreateEntry = (Button) findViewById(R.id.create_entry_button);
		mCreateEntry.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), EditEntryActivity.class);
				intent.setAction("CREATE");
				intent.putExtra("GROUP_TO_PARAM", mGroupToParam);
				startActivity(intent);
			}
		});
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

	protected List<Entry> doGetTimeLine(int page) throws YouRoom4JException {
		return mYouRoomClient.getRoomTimeLine(mGroupToParam, page);
	}

	protected ListAdapter createTimeLineListAdapter(Context context, List<Entry> entryList) {
		return new RoomTimeLineListAdapter(context, entryList);
	}

}
