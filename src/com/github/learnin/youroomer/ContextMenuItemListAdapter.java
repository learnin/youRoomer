package com.github.learnin.youroomer;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ContextMenuItemListAdapter extends ArrayAdapter<MenuItem> {

	private LayoutInflater mLayoutInflater;

	public ContextMenuItemListAdapter(Context context, List<MenuItem> menuItemList) {
		super(context, 0, menuItemList);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MenuItem menuItem = this.getItem(position);
		if (menuItem == null) {
			if (convertView == null) {
				return mLayoutInflater.inflate(R.layout.context_menu_item_row, null);
			}
			return convertView;
		}

		View view = convertView;
		ViewHolder holder;

		if (convertView == null) {
			view = mLayoutInflater.inflate(R.layout.context_menu_item_row, null);
			holder = new ViewHolder();
			holder.mText = (TextView) view.findViewById(R.id.text);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.mText.setText(menuItem.getText());
		return view;
	}

	private static class ViewHolder {
		TextView mText;
	}

}
