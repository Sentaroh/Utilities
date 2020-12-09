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

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

@SuppressWarnings("deprecation")
public class CustomContextMenuItem {

	public final CharSequence text;
	public final Drawable image;
	public boolean menu_enabled=true;

	public CustomContextMenuItem(Resources res, int textResourceId,
			int imageResourceId) {
		text = res.getString(textResourceId);
		if (imageResourceId != -1) {
			image = res.getDrawable(imageResourceId);
		} else {
			image = null;
		}
	}

	public CustomContextMenuItem(Resources res, CharSequence title,
			int imageResourceId) {
		text = title;
		if (imageResourceId != -1) {
			image = res.getDrawable(imageResourceId);
		} else {
			image = null;
		}
	}

	public CustomContextMenuItem(Resources res, int textResourceId,
			int imageResourceId, boolean enabled) {
		text = res.getString(textResourceId);
		if (imageResourceId != -1) {
			image = res.getDrawable(imageResourceId);
		} else {
			image = null;
		}
		menu_enabled=enabled;
	}

	public CustomContextMenuItem(Resources res, CharSequence title,
			int imageResourceId, boolean enabled) {
		text = title;
		if (imageResourceId != -1) {
			image = res.getDrawable(imageResourceId);
		} else {
			image = null;
		}
		menu_enabled=enabled;
	}

	public interface CustomContextMenuOnClickListener {
		public abstract void onClick(CharSequence menuTitle);
	};

	public interface CustomContextMenuOnCleanupListener {
		public abstract void onCleanup();
	};

}
