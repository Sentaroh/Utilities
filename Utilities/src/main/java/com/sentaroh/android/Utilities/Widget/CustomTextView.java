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

    public CustomTextView(Context context) {
        super(context);
        setFilters(new InputFilter[] { new CustomTextViewFilter(this) });
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFilters(new InputFilter[] { new CustomTextViewFilter(this) });
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFilters(new InputFilter[] { new CustomTextViewFilter(this) });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
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