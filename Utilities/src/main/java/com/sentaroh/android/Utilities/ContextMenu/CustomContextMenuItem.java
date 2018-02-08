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
