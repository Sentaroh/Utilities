package com.sentaroh.android.Utilities.ContextMenu;

import java.util.ArrayList;

import com.sentaroh.android.Utilities.R;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomContextMenuAdapter extends BaseAdapter {
//	private Context context = null;
	private LayoutInflater mLayoutInflater=null;
	
	private ArrayList<CustomContextMenuItem> mItems = 
			new ArrayList<CustomContextMenuItem>();

	public CustomContextMenuAdapter(LayoutInflater l) {
		mLayoutInflater = l;
	}
//	public CustomContextMenuAdapter(Context c) {
//		context = c;
//	}

	public void addMenuItem(CustomContextMenuItem menuItem) {
		mItems.add(menuItem);
	}

	public void setMenuItemList(ArrayList<CustomContextMenuItem> mil) {mItems=mil;};


	@Override
	public int getCount() {
		return mItems.size();
	}

	public void clear() {
		mItems.clear();
		return ;
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CustomContextMenuItem item = (CustomContextMenuItem) getItem(position);
	 	final ViewHolder holder;

        View v = convertView;
        if (v == null) {
//            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//            v = vi.inflate(R.layout.custom_context_menu_item, null);
        	v = mLayoutInflater.inflate(R.layout.custom_context_menu_item, null);
            holder=new ViewHolder();

        	holder.iv_icon=(ImageView)v.findViewById(R.id.custom_context_menu_icon);
           	holder.tv_menu=(TextView)v.findViewById(R.id.custom_context_menu_name);

            v.setTag(holder); 
        } else {
     	   holder= (ViewHolder)v.getTag();
        }
        if (item != null) {
        	holder.iv_icon.setImageDrawable(item.image);
        	holder.tv_menu.setText(item.text);
        	if(item.menu_enabled) {
        		holder.tv_menu.setEnabled(true);
        		holder.tv_menu.setTextColor(Color.BLACK);
        	} else {
        		holder.tv_menu.setEnabled(false);
        		holder.tv_menu.setTextColor(Color.GRAY);
        	}
        }
        return v;

	};
	static class ViewHolder {
		ImageView iv_icon;
		TextView tv_menu;
	}

};