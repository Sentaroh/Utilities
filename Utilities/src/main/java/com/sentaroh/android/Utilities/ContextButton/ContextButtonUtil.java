package com.sentaroh.android.Utilities.ContextButton;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

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
	
}
