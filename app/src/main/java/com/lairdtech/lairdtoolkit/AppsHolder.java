/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppsHolder extends BaseAdapter{
	private Context mContext;
	private final String[] names;
	private final int[] images;
	
	
	public AppsHolder(Context c,String[] names,int[] images ) {
		mContext = c;
		this.images = images;
		this.names = names;
	}
	@Override
	public int getCount() {
		return names.length;
	}
	@Override
	public Object getItem(int position) {
		return null;
	}
	@Override
	public long getItemId(int position) {
		return 0;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View grid;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			grid = new View(mContext);
			grid = inflater.inflate(R.layout.item_apps_holder, null);
			TextView textView = (TextView) grid.findViewById(R.id.label);
			ImageView imageView = (ImageView)grid.findViewById(R.id.image);
			textView.setText(names[position]);
			imageView.setImageResource(images[position]);
		} else {
			grid = (View) convertView;
		}
		return grid;
	}
}