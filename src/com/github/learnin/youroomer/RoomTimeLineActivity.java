package com.github.learnin.youroomer;

import java.util.List;

import youroom4j.Entry;
import youroom4j.YouRoom4JException;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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
		return mYouRoomClient.getRoomTimeLine(mGroupToParam);
	}

	/**
	 * エントリ一覧を表示します。<br>
	 *
	 * @param entryList エントリ一覧
	 */
	protected void showEntryList(List<Entry> entryList) {
		mListView.setAdapter(new RoomTimeLineListAdapter(getApplicationContext(), entryList));
		// FIXME 進捗バー
	}

}
