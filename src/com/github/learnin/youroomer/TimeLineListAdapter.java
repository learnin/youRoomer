package com.github.learnin.youroomer;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.github.learnin.youroomer.R;

public class TimeLineListAdapter extends ArrayAdapter<Entry> {

	private LayoutInflater mLayoutInflater;
	private TextView mContent;

	public TimeLineListAdapter(Context context, List<Entry> entryList) {
		super(context, 0, entryList);
		mLayoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;

		if (convertView == null) {
			view = mLayoutInflater.inflate(R.layout.entry_row, null);
		}

		Entry item = this.getItem(position);
		if (item != null) {
			String title = item.getContent();
			mContent = (TextView) view.findViewById(R.id.item_title);
			mContent.setText(title);
		}
		return view;
	}
}
