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
import android.util.Log;

import com.sentaroh.android.Utilities.StringUtil;

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
//        Log.v("CustomTextView","source="+source);
//        Log.v("CustomTextView","start="+start+", end="+end+", width="+width+", w="+w+", wpl="+wpl+", wpr="+wpr);

        if (width<=0) return source;//Modified by F.Hoshino 2018/08/29
        SpannableStringBuilder result = new SpannableStringBuilder();
        for (int index = start; index < end; index++) {
            float rts=Layout.getDesiredWidth(source, start, index + 1, paint);
            if (rts > width) {
                result.append(source.subSequence(start, index));
                result.append("\n");
                start = index;
//                Log.v("CustomTextView","Append cr/lf, result="+result);
            } else if (source.charAt(index) == '\n') {
                result.append(source.subSequence(start, index));
                start = index;
            }
//            Log.v("CustomTextView","start="+start+", end="+end+", index="+index+", rts="+rts);
        }

        if (start < end) {
            result.append(source.subSequence(start, end));
        }
//        Log.v("CustomTextView","result="+ StringUtil.getDumpFormatHexString(result.toString().getBytes(), 0,result.toString().getBytes().length));
        return result;
    }
}