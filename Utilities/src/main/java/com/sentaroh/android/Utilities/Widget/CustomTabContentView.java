package com.sentaroh.android.Utilities.Widget;

import com.sentaroh.android.Utilities.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class CustomTabContentView extends FrameLayout {  
    public CustomTabContentView(Context context) {  
        super(context);  
    }  
    @SuppressLint("InflateParams")
	public CustomTabContentView(Context context, String title) {  
        this(context);  
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
        View childview1 = inflater.inflate(R.layout.tab_widget1, null);  
        TextView tv1 = (TextView) childview1.findViewById(R.id.tab_widget1_textview);  
        tv1.setText(title);  
        addView(childview1);  
   }  
};
