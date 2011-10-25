package com.github.learnin.youroomer;

import java.text.SimpleDateFormat;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RoomListAdapter extends ArrayAdapter<Group> {

	private LayoutInflater mLayoutInflater;
	private TextView mName;
	private TextView mUpdatedAt;

	public RoomListAdapter(Context context, List<Group> groupList) {
		super(context, 0, groupList);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;

		if (convertView == null) {
			view = mLayoutInflater.inflate(R.layout.room_row, null);
		}

		Group group = this.getItem(position);
		if (group != null) {
			mName = (TextView) view.findViewById(R.id.name);
			mName.setText(group.getName());
			mUpdatedAt = (TextView) view.findViewById(R.id.updated_at);
			SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			mUpdatedAt.setText(df.format(group.getUpdatedAt()));
		}
		return view;
	}
}
