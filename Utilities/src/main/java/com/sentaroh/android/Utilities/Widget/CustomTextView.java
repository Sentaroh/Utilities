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
import android.content.Context;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextView extends TextView {

    private CharSequence mOrgText = "";
    private BufferType mOrgBufferType = BufferType.NORMAL;
    private boolean mWordWrapMode =false;

    public CustomTextView(Context context) {
        super(context);
        setWrapFilter();
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWrapFilter();
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWrapFilter();
    }

    private void setWrapFilter() {
        if (isWordWrapMode()) setFilters(new InputFilter[] {});
        else setFilters(new InputFilter[] { new CustomTextViewFilter(this) });
    }

    public void setWordWrapMode(boolean word_wrap_mode) {
        mWordWrapMode =word_wrap_mode;
        setWrapFilter();
    }

    public boolean isWordWrapMode() {
        return mWordWrapMode;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        setText(mOrgText, mOrgBufferType);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        mOrgText = text;
        mOrgBufferType = type;
        super.setText(text, type);
    }

    @Override
    public CharSequence getText() {
        return mOrgText;
    }

    @Override
    public int length() {
        return mOrgText.length();
    }
}