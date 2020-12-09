/*
The MIT License (MIT)
Copyright (c) 2016 Sentaroh

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

package com.sentaroh.android.Utilities.ContextButton;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.Toast;

import com.sentaroh.android.Utilities.Dialog.CommonDialog;

public class ContextButtonUtil {

    public static void setButtonLabelListener(final Context c, ImageButton ib, final String label) {
    	
        ib.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
//				LayoutInflater inflater = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//				View layout = inflater.inflate(R.layout.custom_toast_view, null);
//				TextView tv=(TextView) layout.findViewById(R.id.text);
//				tv.setText(label);
//				Toast toast=new Toast(c);
//				toast.setView(layout);
				Toast toast=Toast.makeText(c, label, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM , 0, 100);
				toast.show();
				return true;
			}
        });
    };

	@SuppressWarnings("unused")
	final static private float toPixel(Resources res, int dip) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
		return px;
	};

    public static void setButtonLabelListener(final Activity a, final ImageButton ib, final String label) {
        ib.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
//                Toast toast= CommonDialog.getToastShort(a, label);
//                positionToast(toast, v, a.getWindow(), 0, 0);
//                toast.show();
                CommonDialog.showPopupMessageAsUpAnchorView(a, ib, label, 2);
                return true;
            }
        });
    };

    static public void positionToast(Toast toast, View view, Window window, int offsetX, int offsetY) {
        // toasts are positioned relatively to decor view, views relatively to their parents, we have to gather additional data to have a common coordinate system
        Rect rect = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(rect);
        // covert anchor view absolute position to a position which is relative to decor view
        int[] viewLocation = new int[2];
        view.getLocationInWindow(viewLocation);
        int viewLeft = viewLocation[0] - rect.left;
        int viewTop = viewLocation[1] - rect.top;

        // measure toast to center it relatively to the anchor view
        DisplayMetrics metrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(metrics.widthPixels, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(metrics.heightPixels, View.MeasureSpec.UNSPECIFIED);
        toast.getView().measure(widthMeasureSpec, heightMeasureSpec);
        int toastWidth = toast.getView().getMeasuredWidth();

        // compute toast offsets
        int toastX = viewLeft + (view.getWidth() - toastWidth) / 2 + offsetX;
        int toastY = view.getHeight()*2;//viewTop + view.getHeight() + offsetY;

        toast.setGravity(Gravity.LEFT | Gravity.BOTTOM, toastX, toastY);
    }

}
