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
	private TextView mText;

	public ContextMenuItemListAdapter(Context context, List<MenuItem> menuItemList) {
		super(context, 0, menuItemList);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;

		if (convertView == null) {
			view = mLayoutInflater.inflate(R.layout.context_menu_item_row, null);
		}

		MenuItem menuItem = this.getItem(position);
		if (menuItem != null) {
			mText = (TextView) view.findViewById(R.id.text);
			mText.setText(menuItem.getText());
		}
		return view;
	}
}
