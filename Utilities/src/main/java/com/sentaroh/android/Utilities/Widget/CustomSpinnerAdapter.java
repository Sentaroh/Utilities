package com.sentaroh.android.Utilities.Widget;

/*
The MIT License (MIT)
Copyright (c) 2013 Sentaroh

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

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {
	
//	private int mResourceId;
	private Context mContext;

//	private ThemeColorList mThemeColorList;
	
	public CustomSpinnerAdapter(Context c, int textViewResourceId) {
		super(c, textViewResourceId);
//		mResourceId=textViewResourceId;
		mContext=c;
//		mThemeColorList=ThemeUtil.getThemeColorList(c);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        TextView view;
        if (convertView == null) {
//        	LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//          view = (TextView)vi.inflate(mResourceId, null);
          view=(TextView)super.getView(position,convertView,parent);
        } else {
            view = (TextView)convertView;
        }
        view.setText(getItem(position));
//        view.setTextColor(Color.WHITE);//mThemeColorList.text_color_disabled);
        view.setCompoundDrawablePadding(10);
        view.setCompoundDrawablesWithIntrinsicBounds(
        		mContext.getResources().getDrawable(android.R.drawable.arrow_down_float), 
        		null, null, null);
        
//        if (text_color!=0) view.setTextColor(text_color);
//        if (text_size!=0) view.setTextSize(text_size);

        return view;
	}
//	@SuppressWarnings("deprecation")
//	@Override
//	public View getDropDownView(int position, View convertView, ViewGroup parent) {
//        final TextView text=(TextView)super.getDropDownView(position, convertView, parent);
////        text.setBackgroundColor(Color.LTGRAY);
//        if (Build.VERSION.SDK_INT>=11) {
////    		text.setCompoundDrawablePadding(10);
////    		text.setCompoundDrawablesWithIntrinsicBounds(
////          		mContext.getResources().getDrawable(android.R.drawable.btn_radio),
////          		null, null, null);
////    		//2014-09-24 ボタンを右側に変更
//    		text.setCompoundDrawablesWithIntrinsicBounds(null,null,
//              		mContext.getResources().getDrawable(android.R.drawable.btn_radio),
//              		null );
////    		text.setEllipsize(TruncateAt.START);
//    		text.post(new Runnable(){
//				@Override
//				public void run() {
//					text.setSingleLine(false);
//				}
//    		});
//        }
////        if (text_color!=0) text.setTextColor(text_color);
////        if (text_size!=0) text.setTextSize(text_size);
//
//        return text;
//	}
	
}
