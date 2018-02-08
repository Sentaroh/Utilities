package com.sentaroh.android.Utilities.Widget;

import java.util.ArrayList;

import com.sentaroh.android.Utilities.R;

import android.content.Context;
import android.support.v7.internal.view.SupportMenuInflater;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.support.v7.internal.view.menu.MenuPopupHelper;
import android.support.v7.internal.view.menu.MenuPresenter;
import android.support.v7.internal.view.menu.SubMenuBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Copied from android.support.v7.widget.PopupMenu.
 * "mPopup.setForceShowIcon(true);" in the constructor does the trick :)
 * 
 * @author maikvlcek
 * @since 5:00 PM - 1/27/14
 */
public class CustomPopupMenu implements MenuBuilder.Callback, MenuPresenter.Callback {
		private Context mContext;
		private MenuBuilder mMenu;
		private View mAnchor;
		private MenuPopupHelper mPopup;
		private OnMenuItemClickListener mMenuItemClickListener;
		private OnDismissListener mDismissListener;

		/**
		 * Callback interface used to notify the application that the menu has closed.
		 */
		public interface OnDismissListener {
			/**
			 * Called when the associated menu has been dismissed.
			 *
			 * @param menu The PopupMenu that was dismissed.
			 */
			public void onDismiss(CustomPopupMenu menu);
		}

		/**
		 * Construct a new PopupMenu.
		 *
		 * @param context Context for the PopupMenu.
		 * @param anchor Anchor view for this popup. The popup will appear below the anchor if there
		 *               is room, or above it if there is not.
		 */
		public CustomPopupMenu(Context context, View anchor) {
			init(context, anchor);
		};

		public CustomPopupMenu(Context context, View anchor, int gravity) {
			init(context, anchor);
			mPopup.setGravity(gravity);
		};

		private void init(Context context, View anchor) {
			mContext = context;
			mMenu = new MenuBuilder(context);
			mMenu.setCallback(this);
			mAnchor = anchor;
			mPopup = new MenuPopupHelper(context, mMenu, anchor);
			mPopup.setCallback(this);
//			mPopup.setForceShowIcon(true);
		};
		
		/**
		 * @return the {@link android.view.Menu} associated with this popup. Populate the returned Menu with
		 * items before calling {@link #show()}.
		 *
		 * @see #show()
		 * @see #getMenuInflater()
		 */
		public Menu getMenu() {
			return mMenu;
		}

		/**
		 * @return a {@link android.view.MenuInflater} that can be used to inflate menu items from XML into the
		 * menu returned by {@link #getMenu()}.
		 *
		 * @see #getMenu()
		 */
		public MenuInflater getMenuInflater() {
			return new SupportMenuInflater(mContext);
		}

		/**
		 * Inflate a menu resource into this PopupMenu. This is equivalent to calling
		 * popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu()).
		 * @param menuRes Menu resource to inflate
		 */
		public void inflate(int menuRes) {
			getMenuInflater().inflate(menuRes, mMenu);
		}

		/**
		 * Show the menu popup anchored to the view specified during construction.
		 * @see #dismiss()
		 */
		public void show() {
			mPopup.show();
			replacePopupMenuAdapter();
		}

		/**
		 * Dismiss the menu popup.
		 * @see #show()
		 */
		public void dismiss() {
			mPopup.dismiss();
		}

		/**
		 * Set a listener that will be notified when the user selects an item from the menu.
		 *
		 * @param listener Listener to notify
		 */
		public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
			mMenuItemClickListener = listener;
		}

		/**
		 * Set a listener that will be notified when this menu is dismissed.
		 *
		 * @param listener Listener to notify
		 */
		public void setOnDismissListener(OnDismissListener listener) {
			mDismissListener = listener;
		}

		/**
		 * @hide
		 */
		public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
			if (mMenuItemClickListener != null) {
				return mMenuItemClickListener.onMenuItemClick(item);
			}
			return false;
		}

		/**
		 * @hide
		 */
		public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
			if (mDismissListener != null) {
				mDismissListener.onDismiss(this);
			}
		}

		/**
		 * @hide
		 */
		public boolean onOpenSubMenu(MenuBuilder subMenu) {
			if (subMenu == null) return false;

			if (!subMenu.hasVisibleItems()) {
				return true;
			}

			// Current menu will be dismissed by the normal helper, submenu will be shown in its place.
			new MenuPopupHelper(mContext, subMenu, mAnchor).show();
			return true;
		}

		/**
		 * @hide
		 */
		public void onCloseSubMenu(SubMenuBuilder menu) {
		}

		/**
		 * @hide
		 */
		public void onMenuModeChange(MenuBuilder menu) {
		}

		/**
		 * Interface responsible for receiving menu item click events if the items themselves
		 * do not have individual item click listeners.
		 */
		public interface OnMenuItemClickListener {
			/**
			 * This method will be invoked when a menu item is clicked if the item itself did
			 * not already handle the event.
			 *
			 * @param item {@link MenuItem} that was clicked
			 * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
			 */
			public boolean onMenuItemClick(MenuItem item);
		}

		private void replacePopupMenuAdapter() {
		    final ArrayList<MenuItemImpl> ml=new ArrayList<MenuItemImpl>();
		    BaseAdapter adapter=(BaseAdapter) mPopup.getPopup().getListView().getAdapter();
		    
		    boolean w_icon_specified=false;
		    for(int i=0;i<adapter.getCount();i++) {
		    	MenuItemImpl mii=(MenuItemImpl) adapter.getItem(i);
		    	ml.add(mii);
		    	if (mii.getIcon()!=null) w_icon_specified=true;
		    }
		    final boolean icon_specified=w_icon_specified;
	        BaseAdapter mListPopupAdapter = new BaseAdapter() {
	            class ViewHolder {
	                private TextView title;
	                private ImageView icon;
	            }

	            @Override
	            public int getCount() {
	                return ml.size();
	            }

	            @Override
	            public Object getItem(int position) {
	                return ml.get(position);
	            }

	            @Override
	            public long getItemId(int position) {
	                return position;
	            }

	            @Override
	            public View getView(int position, View convertView, ViewGroup parent) {
	                if (convertView == null) {
	                    convertView = LayoutInflater.from(parent.getContext()).inflate(
	                            R.layout.custom_popup_menu_item, parent, false);
	                    ViewHolder viewHolder = new ViewHolder();
	                    viewHolder.icon = (ImageView) convertView.findViewById(R.id.icon);
	                    viewHolder.title = (TextView) convertView.findViewById(R.id.title);
	                    convertView.setTag(viewHolder);
	                }

	                ViewHolder viewHolder = (ViewHolder) convertView.getTag();
	                viewHolder.title.setText(ml.get(position).getTitle());
	                if (icon_specified) {
	                	viewHolder.icon.setVisibility(ImageView.VISIBLE);
	                	viewHolder.icon.setImageDrawable(ml.get(position).getIcon());
	                } else {
	                	viewHolder.icon.setVisibility(ImageView.INVISIBLE);
	                }
	                
	                return convertView;
	            }

	        };
	        mPopup.getPopup().getListView().setAdapter(mListPopupAdapter);

		}
		
}