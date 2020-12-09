/*
The MIT License (MIT)
Copyright (c) 2012 Sentaroh

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

*/

package com.sentaroh.android.Utilities.ContextMenu;

import java.util.ArrayList;

import com.sentaroh.android.Utilities.ContextMenu.CustomContextMenuItem.CustomContextMenuOnCleanupListener;
import com.sentaroh.android.Utilities.ContextMenu.CustomContextMenuItem.CustomContextMenuOnClickListener;

import android.content.res.Resources;
import android.support.v4.app.FragmentManager;

public class CustomContextMenu{

	private ArrayList<CustomContextMenuItem> mMenuList = null;
	private Resources mParentResource=null;
	private FragmentManager mFragmentManager=null;
	
	private ArrayList<CustomContextMenuOnClickListener> mClickHandler = 
			new ArrayList<CustomContextMenuOnClickListener>();
	
	public CustomContextMenu(Resources r, FragmentManager fm) {
		mParentResource=r;
		mFragmentManager=fm;
		mMenuList = new ArrayList<CustomContextMenuItem>(); 
//				new CustomMenuAdapter(parentActivity,R.layout.custom_context_menu_list_item);
	};

	
	public CustomContextMenu addMenuItem(CharSequence title) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, title,
				-1));
		return this;
	};

	public CustomContextMenu addMenuItem(CharSequence title, int imageResourceId) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, title,
				imageResourceId));
		return this;
	};

	public CustomContextMenu addMenuItem(int textResourceId, int imageResourceId) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, textResourceId,
				imageResourceId));
		return this;
	};

	public CustomContextMenu addMenuItem(boolean enabled, CharSequence title) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, title,
				-1,enabled));
		return this;
	};

	public CustomContextMenu addMenuItem(boolean enabled, CharSequence title, int imageResourceId) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, title,
				imageResourceId,enabled));
		return this;
	};

	public CustomContextMenu addMenuItem(boolean enabled, int textResourceId, int imageResourceId) {
		mMenuList.add(new CustomContextMenuItem(mParentResource, textResourceId,
				imageResourceId,enabled));
		return this;
	};

	
	public void setOnClickListener(CustomContextMenuOnClickListener listener) {
		mClickHandler.add( listener);
	};

	public void createMenu(String title) {
		CustomContextMenuFragment ccmf =
				CustomContextMenuFragment.newInstance(title);
		ccmf.showDialog(mFragmentManager,ccmf,mMenuList,mClickHandler,null);
	};

	public void createMenu(String title, CustomContextMenuOnCleanupListener cl) {
		CustomContextMenuFragment ccmf =
				CustomContextMenuFragment.newInstance(title);
		ccmf.showDialog(mFragmentManager,ccmf,mMenuList,mClickHandler, cl);
	};

	public void createMenu() {
		createMenu("");
	};

	public void createMenu(CustomContextMenuOnCleanupListener cl) {
		createMenu("",cl);
	};

}
