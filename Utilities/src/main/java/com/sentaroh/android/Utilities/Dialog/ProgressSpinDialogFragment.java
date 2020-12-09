package com.sentaroh.android.Utilities.Dialog;

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

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.R;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.ThemeUtil;

public class ProgressSpinDialogFragment extends DialogFragment {
	private final static boolean DEBUG_ENABLE=false;
	private final static String APPLICATION_TAG="ProgressSpinDialogFragment";

	private Dialog mDialog=null;
	private boolean mTerminateRequired=true;
	private ProgressSpinDialogFragment mFragment=null;
	
	private boolean mDialogCancellable=true;

    private String mDialogTitle="",mDialogMsgText="",
    		mDialogCanTitleInit,mDialogCanTitlePressed;
    private boolean mDialogCancelBtnEnabled=true;
    private Handler mUiHandler=new Handler();
	
	private NotifyEvent mNotifyEvent=null;
	
	public static ProgressSpinDialogFragment newInstance(
			String title, String msgtext, String cancelTitleInit, String cancelTitlePressed) {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"newInstance");
        ProgressSpinDialogFragment frag = new ProgressSpinDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("msgtext", msgtext);
        bundle.putString("cacelTitleInit", cancelTitleInit);
        bundle.putString("cacelTitlePressed", cancelTitlePressed);
        frag.setArguments(bundle);
        return frag;
    }
	public void setNotifyEvent(NotifyEvent ntfy) {mNotifyEvent=ntfy;}

	public ProgressSpinDialogFragment() {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"Constructor(Default)");
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {  
		super.onSaveInstanceState(outState);
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onSaveInstanceState");
		if(outState.isEmpty()){
	        outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
	    }
	};  
	
	@Override
	final public void onConfigurationChanged(final Configuration newConfig) {
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onConfigurationChanged");

//	    reInitViewWidget();
	};
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onCreateView");
    	View view=super.onCreateView(inflater, container, savedInstanceState);
    	
    	CommonDialog.setDlgBoxSizeCompact(mDialog);
    	
    	return view;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onCreate");
        if (!mTerminateRequired) {
        	mFragment=this;
            Bundle bd=getArguments();
            setRetainInstance(true);
        	mDialogTitle=bd.getString("title");
        	mDialogMsgText=bd.getString("msgtext");
            mDialogCanTitleInit=bd.getString("cacelTitleInit");
            mDialogCanTitlePressed=bd.getString("cacelTitlePressed");
        }
    };

	@Override
	final public void onActivityCreated(Bundle savedInstanceState) {
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onActivityCreated savedInstanceState="+savedInstanceState);
	    super.onActivityCreated(savedInstanceState);
	};
	@Override
	final public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onAttach");
	};
	@Override
	final public void onDetach() {
	    super.onDetach();
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onDetach");
	};
	@Override
	final public void onStart() {
//    	CommonDialog.setDlgBoxSizeCompact(mDialog);
	    super.onStart();
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onStart");
	    if (mTerminateRequired) mDialog.cancel();
	};
	@Override
	final public void onStop() {
	    super.onStop();
	    if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onStop");
	};

	@Override
	public void onDestroyView() {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onDestroyView");
	    if (getDialog() != null && getRetainInstance())
	        getDialog().setDismissMessage(null);
	    super.onDestroyView();
	}
	
	@Override
	public void onDestroy() {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onDestroy");
	    super.onDestroy();
	}

	@Override
	public void onCancel(DialogInterface di) {
	    super.onCancel(di);
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onCancel");
		if (!mTerminateRequired) {
			Button btnCancel = (Button) mDialog.findViewById(R.id.progress_spin_dlg_fragment_btn_cancel);
			btnCancel.performClick();
		}
		super.onCancel(di);
	}
	
	@Override
	public void onDismiss(DialogInterface di) {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onDismiss");
		super.onDismiss(di);
	}

	@Override
	public void dismiss() {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"dismiss");
		super.dismiss();
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onCreateDialog");
    	if (mDialog == null) {
        	super.setShowsDialog (false);
        	mDialog=new Dialog(getActivity(), ThemeUtil.getAppTheme(getActivity()));//, MiscUtil.getAppTheme(getActivity()));
    		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    		mDialog.setCancelable(mDialogCancellable);
    		mDialog.setCanceledOnTouchOutside(false);
    	}
		if (!mTerminateRequired) {
			initViewWidget();
		}
        return mDialog;
    };
    
    @SuppressWarnings("unused")
	private void reInitViewWidget() {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"reInitViewWidget");
    	if (!mTerminateRequired) {
    		Handler hndl=new Handler();
    		hndl.post(new Runnable(){
				@Override
				public void run() {
					mDialog.hide();
		    		mDialog.getWindow().getCurrentFocus().invalidate();
		        	initViewWidget();
		        	Button btnCancel = (Button) mDialog.findViewById(R.id.progress_spin_dlg_fragment_btn_cancel);
		        	if (mDialogCancelBtnEnabled) btnCancel.setEnabled(true);
		        	else btnCancel.setEnabled(true);
		        	CommonDialog.setDlgBoxSizeCompact(mDialog);
		        	mDialog.show();
				}
    		});
    	}
    };
    
    private ThemeColorList mThemeColorList;
    private void initViewWidget() {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"initViewWidget");
    	mThemeColorList=ThemeUtil.getThemeColorList(getActivity());

		mDialog.setContentView(R.layout.progress_spin_dlg_fragment);

		LinearLayout title_view=(LinearLayout)mDialog.findViewById(R.id.progress_spin_dlg_fragment_ll_title);
		title_view.setBackgroundColor(mThemeColorList.title_background_color);

		LinearLayout dlg_view=(LinearLayout)mDialog.findViewById(R.id.progress_spin_dlg_fragment);
//		dlg_view.setBackgroundColor(mThemeColorList.dialog_msg_background_color);

		TextView tv_title=(TextView)mDialog.findViewById(R.id.progress_spin_dlg_fragment_title);
		tv_title.setTextColor(mThemeColorList.title_text_color);
		tv_title.setText(mDialogTitle);

		TextView tv_msg=(TextView)mDialog.findViewById(R.id.progress_spin_dlg_fragment_msg);
		tv_msg.setText(mDialogMsgText);

		final Button btnCancel = (Button) mDialog.findViewById(R.id.progress_spin_dlg_fragment_btn_cancel);
		
//		CommonDialog.setDlgBoxSizeCompact(mDialog);
		
		// CANCELボタンの指定
		btnCancel.setText(mDialogCanTitleInit);
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDialogCancelBtnEnabled=false;
				btnCancel.setEnabled(false);
				btnCancel.setText(mDialogCanTitlePressed);
				if (mNotifyEvent!=null) 
					mNotifyEvent.notifyToListener(false,new Object[]{mFragment});
			}
		});
    }
    
    public void showDialog(FragmentManager fm, Fragment frag, NotifyEvent ntfy, boolean cancellable) {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"showDialog");
    	mTerminateRequired=false;
    	mNotifyEvent=ntfy;
	    FragmentTransaction ft = fm.beginTransaction();
	    ft.add(frag,null);
	    ft.commitAllowingStateLoss();
	    
	    mDialogCancellable=cancellable;
//    	show(fm, APPLICATION_TAG);
    };
    
    public void updateMsgText(final String msgtext) {
    	if (mUiHandler!=null) {
        	mUiHandler.post(new Runnable(){
    			@Override
    			public void run() {
    		    	mDialogMsgText=msgtext;
    				TextView tv_msg=(TextView)mDialog.findViewById(R.id.progress_spin_dlg_fragment_msg);
    				tv_msg.setText(mDialogMsgText);
    			}
        	});
    	}
    }
    
}
