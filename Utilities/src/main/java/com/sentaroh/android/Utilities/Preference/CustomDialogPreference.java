package com.sentaroh.android.Utilities.Preference;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class CustomDialogPreference extends DialogPreference {
    public static interface CustomDialogPreferenceButtonListener {
        public void onButtonClick(DialogInterface dialog, int which);
    }
    private CustomDialogPreferenceButtonListener mPositiveButtonListener=null;
    public CustomDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    public CustomDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    public void setButtonListener(CustomDialogPreferenceButtonListener listener) {
        mPositiveButtonListener = listener;
    }
    
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mPositiveButtonListener != null) {
            mPositiveButtonListener.onButtonClick(dialog, which);
        }
    }
 }