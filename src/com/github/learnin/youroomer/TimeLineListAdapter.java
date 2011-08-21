package com.github.learnin.youroomer;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class TimeLineListAdapter extends ArrayAdapter<Entry> {

	private LayoutInflater mLayoutInflater;
	private TextView mContent;
	private TextView mCreatedAt;

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

		Entry entry = this.getItem(position);
		if (entry != null) {
			mContent = (TextView) view.findViewById(R.id.content);
			mContent.setText(entry.getContent());
			mCreatedAt = (TextView) view.findViewById(R.id.created_at);
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			mCreatedAt.setText(df.format(entry.getCreatedAt()));
		}
		return view;
	}
}
