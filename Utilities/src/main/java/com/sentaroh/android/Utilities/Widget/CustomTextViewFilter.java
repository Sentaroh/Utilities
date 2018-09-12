package com.sentaroh.android.Utilities.Widget;
/*
    http://fuwafuwapukapuka.hatenablog.com/entry/2014/03/26/215747
    の記事を利用させていただきました。
*/

import android.text.InputFilter;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;

public class CustomTextViewFilter implements InputFilter {
    private final CustomTextView view;

    public CustomTextViewFilter(CustomTextView view) {
        this.view = view;
    }

    //@Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        TextPaint paint = view.getPaint();
        int w = view.getWidth();
        int wpl = view.getCompoundPaddingLeft();
        int wpr = view.getCompoundPaddingRight();
        int width = w - wpl - wpr;
//        Log.v("","source="+source);
//        Log.v("","start="+start+", end="+end+", width="+width+", w="+w+", wpl="+wpl+", wpr="+wpr);

        if (width<=0) return source;//Modified by F.Hoshino 2018/08/29
        SpannableStringBuilder result = new SpannableStringBuilder();
        for (int index = start; index < end; index++) {
            float rts=Layout.getDesiredWidth(source, start, index + 1, paint);
            if (rts > width) {
                result.append(source.subSequence(start, index));
                result.append("\n");
                start = index;
//                Log.v("","result="+result);
            } else if (source.charAt(index) == '\n') {
                result.append(source.subSequence(start, index));
                start = index;
            }
//            Log.v("","start="+start+", end="+end+", index="+index+", rts="+rts);
        }

        if (start < end) {
            result.append(source.subSequence(start, end));
        }
//        Log.v("","result="+ StringUtil.getDumpFormatHexString(result.toString().getBytes(), 0,result.toString().getBytes().length));
        return result;
    }
}