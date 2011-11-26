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
		Group group = this.getItem(position);
		if (group == null) {
			if (convertView == null) {
				return mLayoutInflater.inflate(R.layout.room_row, null);
			}
			return convertView;
		}

		View view = convertView;
		ViewHolder holder = new ViewHolder();

		if (convertView == null) {
			view = mLayoutInflater.inflate(R.layout.room_row, null);
			holder.mName = (TextView) view.findViewById(R.id.name);
			holder.mUpdatedAt = (TextView) view.findViewById(R.id.updated_at);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.mName.setText(group.getName());
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		holder.mUpdatedAt.setText(df.format(group.getUpdatedAt()));
		return view;
	}

	private static class ViewHolder {
		TextView mName;
		TextView mUpdatedAt;
	}

}
