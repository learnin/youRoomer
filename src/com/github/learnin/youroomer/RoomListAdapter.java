package com.github.learnin.youroomer;

import java.text.SimpleDateFormat;
import java.util.List;

import youroom4j.Group;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class RoomListAdapter extends ArrayAdapter<Group> {

	private LayoutInflater mLayoutInflater;

	public RoomListAdapter(Context context, List<Group> groupList) {
		super(context, 0, groupList);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.room_row, null);
			holder = new ViewHolder();
			holder.mName = (TextView) convertView.findViewById(R.id.name);
			holder.mUpdatedAt = (TextView) convertView.findViewById(R.id.updated_at);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		Group group = getItem(position);
		holder.mName.setText(group.getName());
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		holder.mUpdatedAt.setText(df.format(group.getUpdatedAt()));
		return convertView;
	}

	private static class ViewHolder {
		TextView mName;
		TextView mUpdatedAt;
	}

}
