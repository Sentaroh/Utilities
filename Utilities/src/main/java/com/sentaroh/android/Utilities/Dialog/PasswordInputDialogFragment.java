/*
The MIT License (MIT)
Copyright (c) 2015 Sentaroh

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
package com.sentaroh.android.Utilities.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.R;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.ThemeUtil;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;

public class PasswordInputDialogFragment extends DialogFragment {
	private final static boolean DEBUG_ENABLE=false;
	private final static String APPLICATION_TAG="PasswordInputDialogFragment";

	private Dialog mDialog=null;
	private PasswordInputDialogFragment mFragment=null;
	private boolean terminateRequired=true;

    private String mDialogTitle="";
    private int mDialogMinLength=0;
	
	private NotifyEvent mNotifyEvent=null;
	
	private ThemeColorList mThemeColorList;
	
	private EditText newPasswdEditText;
	private EditText confirmPasswdEditText;
	private TextView mDlgMsg;
	private Context mContext=null;
	private String mPswd="", mConfPswd="";
	
	public static PasswordInputDialogFragment newInstance(String title, int min_length) {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"newInstance sub="+title);
        PasswordInputDialogFragment frag = new PasswordInputDialogFragment();
        Bundle bundle = new Bundle();
        if (title!=null) bundle.putString("title", title);
        else bundle.putString("title", "");
        bundle.putInt("min_length", min_length);
        
        frag.setArguments(bundle);
        return frag;
    }
	public void setNotifyEvent(NotifyEvent ntfy) {mNotifyEvent=ntfy;}

	public PasswordInputDialogFragment() {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"Constructor(Default) terminateRequired="+terminateRequired);
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {  
		super.onSaveInstanceState(outState);
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onSaveInstanceState terminateRequired="+terminateRequired);
		if(outState.isEmpty()){
	        outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
	    }
	};  
	
	@Override
	final public void onConfigurationChanged(final Configuration newConfig) {
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onConfigurationChanged terminateRequired="+terminateRequired);

	    reInitViewWidget();
//	    CommonDialog.setDlgBoxSizeCompact(mDialog);
	};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onCreateView terminateRequired="+terminateRequired);
    	View view=super.onCreateView(inflater, container, savedInstanceState);
    	CommonDialog.setDlgBoxSizeCompact(mDialog);
    	return view;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onCreate terminateRequired="+terminateRequired);
        mContext=getActivity().getApplicationContext();
        if (!terminateRequired) {
            Bundle bd=getArguments();
            setRetainInstance(true);
        	mDialogTitle=bd.getString("title");
        	mDialogMinLength=bd.getInt("min_length");
        }
    	mFragment=this;
    }

	@Override
	final public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onActivityCreated terminateRequired="+terminateRequired);
	};
	@Override
	final public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onAttach terminateRequired="+terminateRequired);
	};
	@Override
	final public void onDetach() {
	    super.onDetach();
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onDetach terminateRequired="+terminateRequired);
	};
	@Override
	final public void onStart() {
//		CommonDialog.setDlgBoxSizeCompact(mDialog);
	    super.onStart();
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onStart terminateRequired="+terminateRequired);
	    if (terminateRequired) mDialog.cancel(); 
	};
	
	@Override
	final public void onStop() {
	    super.onStop();
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onStop terminateRequired="+terminateRequired);
	};

	@Override
	public void onDestroyView() {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onDestroyView terminateRequired="+terminateRequired);
	    if (getDialog() != null && getRetainInstance())
	        getDialog().setDismissMessage(null);
	    super.onDestroyView();
	};
	
	@SuppressWarnings("unused")
	@Override
	public void onCancel(DialogInterface di) {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onCancel terminateRequired="+terminateRequired);
//	    super.onCancel(di);
		if (!terminateRequired) {
			Button btnOk = (Button) mDialog.findViewById(R.id.common_dialog_btn_ok);
			Button btnCancel = (Button) mDialog.findViewById(R.id.common_dialog_btn_cancel);
			btnCancel.performClick();
		}
		super.onCancel(di);
	};
	
	@Override
	public void onDismiss(DialogInterface di) {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onDismiss terminateRequired="+terminateRequired);
		super.onDismiss(di);
	};

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onCreateDialog terminateRequired="+terminateRequired);
    	mDialog=new Dialog(getActivity(), ThemeUtil.getAppTheme(getActivity()));//, MiscUtil.getAppTheme(getActivity()));
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		mDialog.setCanceledOnTouchOutside(false);

		if (!terminateRequired) {
			mThemeColorList=ThemeUtil.getThemeColorList(getActivity());
			initViewWidget();
		}
		
        return mDialog;
    };

    private void reInitViewWidget() {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"reInitViewWidget");
		if (!terminateRequired) {
    		Handler hndl=new Handler();
    		hndl.post(new Runnable(){
				@Override
				public void run() {
					mDialog.hide();
//		    		mDialog.getWindow().getCurrentFocus().invalidate();
					String pswd,conf_pswd;
					pswd=newPasswdEditText.getText().toString();
					conf_pswd=confirmPasswdEditText.getText().toString();
					initViewWidget();
					newPasswdEditText.setText(pswd);
					confirmPasswdEditText.setText(conf_pswd);
					final Button btnOk = (Button) mDialog.findViewById(R.id.common_dialog_btn_ok);
					checkPassword(btnOk);
					CommonDialog.setDlgBoxSizeCompact(mDialog);
					mDialog.onContentChanged();
					mDialog.show();
				}
    		});
		}
    };
    
    private void initViewWidget() {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"initViewWidget");

    	mDialog.setContentView(R.layout.password_input_dlg);

    	ImageView title_icon=(ImageView)mDialog.findViewById(R.id.password_input_dlg_icon);
    	TextView title=(TextView)mDialog.findViewById(R.id.password_input_dlg_title);
    	LinearLayout title_view=(LinearLayout)mDialog.findViewById(R.id.password_input_dlg_title_view);
    	title_view.setBackgroundColor(mThemeColorList.title_background_color);
    	
    	LinearLayout btn_view=(LinearLayout)mDialog.findViewById(R.id.common_dialog_btn_view);
//    	btn_view.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
    	
		title_icon.setImageResource(R.drawable.dialog_information);
		title.setTextColor(mThemeColorList.title_text_color);
		title.setText(mDialogTitle);
		mDlgMsg=(TextView)mDialog.findViewById(R.id.password_input_preference_msg);
		
		final Button btnOk = (Button) mDialog.findViewById(R.id.common_dialog_btn_ok);
		final Button btnCancel = (Button) mDialog.findViewById(R.id.common_dialog_btn_cancel);
		btnCancel.setVisibility(View.VISIBLE);
		
//		if (Build.VERSION.SDK_INT<=10 && mThemeColorList.theme_is_light) {
//			btnOk.setTextColor(mThemeColorList.text_color_info);
//			btnCancel.setTextColor(mThemeColorList.text_color_info);
//		}
		
		newPasswdEditText=(EditText)mDialog.findViewById(R.id.password_input_preference_new_pswd);
		confirmPasswdEditText=(EditText)mDialog.findViewById(R.id.password_input_preference_conf_pswd);
		newPasswdEditText.setText(mPswd);
		confirmPasswdEditText.setText(mConfPswd);
		newPasswdEditText.setFocusable(true);
		mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_specify_new_password));

		checkPassword(btnOk);
		
	    newPasswdEditText.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				checkPassword(btnOk);
			}
	    });
	    confirmPasswdEditText.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				checkPassword(btnOk);
			}
	   });

//		CommonDialog.setDlgBoxSizeCompact(mDialog);
		
		// OKボタンの指定
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				mDialog.dismiss();
				mFragment.dismiss();
				if (mNotifyEvent!=null) mNotifyEvent.notifyToListener(true,
						new Object[]{newPasswdEditText.getText().toString()});
			}
		});
		// CANCELボタンの指定
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				mDialog.dismiss();
				mFragment.dismiss();
				if (mNotifyEvent!=null) mNotifyEvent.notifyToListener(false,null);
			}
		});
    }
    private void checkPassword(Button btn_ok) {
 		btn_ok.setEnabled(false);
 		if (mDialogMinLength==0) {
 	 		if (newPasswdEditText.getText().length()>0 && confirmPasswdEditText.getText().length()>0) {
 	 			if (newPasswdEditText.getText().toString().equals(confirmPasswdEditText.getText().toString())) {
 	 				btn_ok.setEnabled(true);
 	 				mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_match));
 	 			} else {
 	 				mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_unmatch));
 	 			}
 	 		} else {
 	 			if (newPasswdEditText.getText().length()==0) {
 	 				mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_new_not_specified));
 	 			} else {
 	 				mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_conf_not_specified));
 	 			}
 	 		}
 		} else {
 			if (newPasswdEditText.getText().length()>=mDialogMinLength) {
 	 			if (newPasswdEditText.getText().toString().equals(confirmPasswdEditText.getText().toString())) {
 	 				btn_ok.setEnabled(true);
 	 				mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_match));
 	 			} else {
 	 				mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_unmatch));
 	 			}
 			} else {
 	 			if (newPasswdEditText.getText().length()==0) {
 	 				mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_new_not_specified));
 	 			} else {
 	 				String msg=mContext.getString(R.string.msgs_password_input_preference_less_than_min_length);
 	 				mDlgMsg.setText(String.format(msg,mDialogMinLength));
 	 			}
 			}
 		
 		}
    }

    
//    public void showDialog(FragmentManager fm, NotifyEvent ntfy) {
//    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"showDialog");
//    	mNotifyEvent=ntfy;
//	    FragmentTransaction ft = fm.beginTransaction();
//	    ft.add(mFragment,null);
//	    ft.commitAllowingStateLoss();
////      show(fm, "MessageDialogFragment");
//    };
    public void showDialog(FragmentManager fm, Fragment frag, NotifyEvent ntfy) {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"showDialog");
    	terminateRequired=false;
    	mNotifyEvent=ntfy;
	    FragmentTransaction ft = fm.beginTransaction();
	    ft.add(frag,null);
	    ft.commitAllowingStateLoss();
//    	show(fm, APPLICATION_TAG);
    };

}
