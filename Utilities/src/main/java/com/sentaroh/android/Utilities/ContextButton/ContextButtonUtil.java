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

    public static void setButtonLabelListener(final Activity a, ImageButton ib, final String label) {
        ib.setOnLongClickListener(new OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                Toast toast= CommonDialog.getToastShort(a, label);
                positionToast(toast, v, a.getWindow(), 0, 0);
                toast.show();
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
