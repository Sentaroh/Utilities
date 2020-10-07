package com.sentaroh.android.Utilities.LogUtil;

/*
The MIT License (MIT)
Copyright (c) 2011-2013 Sentaroh

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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.sentaroh.android.Utilities.CommonGlobalParms;
import com.sentaroh.android.Utilities.ContextButton.ContextButtonUtil;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.Dialog.MessageDialogFragment;
import com.sentaroh.android.Utilities.Dialog.ProgressBarDialogFragment;
import com.sentaroh.android.Utilities.LocalMountPoint;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities.R;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.ThemeUtil;
import com.sentaroh.android.Utilities.ThreadCtrl;
import com.sentaroh.android.Utilities.ZipUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class CommonLogFileListDialogFragment extends DialogFragment{
    private final static Logger log= LoggerFactory.getLogger(CommonLogFileListDialogFragment.class);
//	private final static boolean log.isInfoEnabled()=false;
	private final static String APPLICATION_TAG="LogFileManagement";

	private final static String MAIL_TO="gm.developer.fhoshino@gmail.com";

	private Dialog mDialog=null;
	private boolean mTerminateRequired=true;
	private CommonLogFileListDialogFragment mFragment=null;
	private String mDialogTitle=null;
	private CommonGlobalParms mGp=null;
	
	private CommonLogFileListAdapter mLogFileManagementAdapter=null;
	
	private Context mContext=null;
	
	private Handler mUiHandler=null;

	private NotifyEvent mNotifyUpdateLogOption=null;
	
	private ArrayList<CommonLogFileListItem> mLogFileList=null;

	private boolean mShowSaveButton =true;
	private String mEnableMessage="";
    private String mSendMessage="";
    private String mSendSubject="";

	public static CommonLogFileListDialogFragment newInstance(boolean retainInstance, String title,
                                                              String send_msg, String enable_msg, String send_subject) {
		if (log.isInfoEnabled()) log.info("newInstance");
		CommonLogFileListDialogFragment frag = new CommonLogFileListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("theme_id", "");
        bundle.putBoolean("retainInstance", retainInstance);
        bundle.putBoolean("showSaveButton", true);
        bundle.putString("title", title);
        bundle.putString("msgtext", send_msg);
        bundle.putString("enableMsg", enable_msg);
        bundle.putString("subject", send_subject);
        frag.setArguments(bundle);
        return frag;
    }

    public static CommonLogFileListDialogFragment newInstance(String theme_id, boolean retainInstance, String title,
                                                              String send_msg, String enable_msg, String send_subject) {
        if (log.isInfoEnabled()) log.info("newInstance");
        CommonLogFileListDialogFragment frag = new CommonLogFileListDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("theme_id", theme_id);
        bundle.putBoolean("retainInstance", retainInstance);
        bundle.putBoolean("showSaveButton", true);
        bundle.putString("title", title);
        bundle.putString("msgtext", send_msg);
        bundle.putString("enableMsg", enable_msg);
        bundle.putString("subject", send_subject);
        frag.setArguments(bundle);
        return frag;
    }

    public CommonLogFileListDialogFragment() {
		if (log.isInfoEnabled()) log.info("Constructor(Default)");
	}; 
	
	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    if (log.isInfoEnabled()) log.info("onAttach");
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {  
		super.onSaveInstanceState(outState);
		if (log.isInfoEnabled()) log.info("onSaveInstanceState");
		if(outState.isEmpty()){
	        outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
	    }
    	saveViewContents();
	};  
	
	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	    if (log.isInfoEnabled()) log.info("onConfigurationChanged");

	    reInitViewWidget();
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    if (log.isInfoEnabled()) log.info("onActivityCreated");
	};
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	if (log.isInfoEnabled()) log.info("onCreateView");
    	View view=super.onCreateView(inflater, container, savedInstanceState);
    	return view;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        if (log.isInfoEnabled()) log.info("onCreate");
        mUiHandler=new Handler();
    	mFragment=this;
        if (!mTerminateRequired) {
//            mGp=(CommonGlobalParms)getActivity().getApplication();
            mContext=getActivity();
            Bundle bd=getArguments();
            setRetainInstance(bd.getBoolean("retainInstance"));
            mDialogTitle=bd.getString("title");
            mEnableMessage=bd.getString("enableMsg");
            mSendMessage=bd.getString("msgtext");
            mSendSubject=bd.getString("subject");
            mShowSaveButton =bd.getBoolean("showSaveButton");
        	mLogFileList=CommonLogUtil.createLogFileList(mGp);
        }
    };
    
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	if (log.isInfoEnabled()) log.info("onCreateDialog");

        mDialog=new Dialog(getActivity(), ThemeUtil.getAppTheme(getActivity()));

        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setCanceledOnTouchOutside(false);

		if (!mTerminateRequired) {
			initViewWidget();
		}
		
        return mDialog;
    };
    
	@Override
	public void onStart() {
    	CommonDialog.setDlgBoxSizeLimit(mDialog,true);
	    super.onStart();
	    if (log.isInfoEnabled()) log.info("onStart");
	    if (mTerminateRequired) mDialog.cancel();
	    else {
	    	mDialog.setOnKeyListener(new OnKeyListener(){
    	        @Override
	    	    public boolean onKey ( DialogInterface dialog , int keyCode , KeyEvent event ){
	    	        // disable search button action
	    	        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction()==KeyEvent.ACTION_DOWN){
	    	        	if (mLogFileManagementAdapter.isShowCheckBox()) {
		    	        	for(int i=0;i<mLogFileManagementAdapter.getCount();i++) {
		    	        		mLogFileManagementAdapter.getItem(i).isChecked=false;
		    	        	}
		    	        	mLogFileManagementAdapter.setShowCheckBox(false);
		    	        	mLogFileManagementAdapter.notifyDataSetChanged();
		    	        	setContextButtonNormalMode();
		    	        	return true;
	    	        	}
	    	        }
	    	        return false;
	    	    }
	    	});
	    }
	};
	
	@Override
	public void onCancel(DialogInterface di) {
		if (log.isInfoEnabled()) log.info("onCancel");
		mFragment.dismiss();
		super.onCancel(di);
	};
	
	@Override
	public void onDismiss(DialogInterface di) {
		if (log.isInfoEnabled()) log.info("onDismiss");
		super.onDismiss(di);
	};

	@Override
	public void onStop() {
	    super.onStop();
	    if (log.isInfoEnabled()) log.info("onStop");
	};
	
	@Override
	public void onDestroyView() {
		if (log.isInfoEnabled()) log.info("onDestroyView");
	    if (getDialog() != null && getRetainInstance())
	        getDialog().setDismissMessage(null);
	    super.onDestroyView();
        deleteTempLogFile();
//        deleteZipLogFile();
	};
	
	@Override
	public void onDetach() {
	    super.onDetach();
	    if (log.isInfoEnabled()) log.info("onDetach");
	};


    private void reInitViewWidget() {
    	if (log.isInfoEnabled()) log.info("reInitViewWidget");
    	if (!mTerminateRequired) {
    		Handler hndl=new Handler();
    		hndl.post(new Runnable(){
				@Override
				public void run() {
					boolean scb=mLogFileManagementAdapter.isShowCheckBox();
			    	SaveViewContents sv=saveViewContents();
			    	initViewWidget();
			    	restoreViewContents(sv);
		        	if (scb) {
		        		mLogFileManagementAdapter.setShowCheckBox(true);
		        		setContextButtonSelecteMode();
		        	} else {
		        		setContextButtonNormalMode();
		        	}
				}
    		});
    	}
    };

    class SaveViewContents {
        public boolean log_enabled=false;
    }
    private SaveViewContents saveViewContents() {
        SaveViewContents sv=new SaveViewContents();
        final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
    	sv.log_enabled=cb_log_enabled.isChecked();
    	return sv;
    };
    
    private void restoreViewContents(SaveViewContents sv) {
        final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
        cb_log_enabled.setChecked(sv.log_enabled);
    };
    
    private void initViewWidget() {
    	if (log.isInfoEnabled()) log.info("initViewWidget");

    	mDialog.setContentView(R.layout.log_file_list_dlg);
    	
    	ThemeColorList tcl=ThemeUtil.getThemeColorList(getActivity());
    	
		final LinearLayout title_view=(LinearLayout) mDialog.findViewById(R.id.log_file_list_dlg_title_view);
		title_view.setBackgroundColor(tcl.title_background_color);
    	final TextView dlg_title=(TextView)mDialog.findViewById(R.id.log_file_list_dlg_title);
		dlg_title.setTextColor(tcl.title_text_color);

    	dlg_title.setText(mDialogTitle);
    	final ImageButton dlg_done=(ImageButton)mDialog.findViewById(R.id.log_file_list_dlg_done);
    	dlg_done.setVisibility(ImageButton.GONE);
    	
    	final ListView lv_log_file=(ListView)mDialog.findViewById(R.id.log_file_list_dlg_log_listview);
    	final Button btn_close=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_close);
    	
    	NotifyEvent ntfy_cb_listener=new NotifyEvent(mContext);
    	ntfy_cb_listener.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				if (mLogFileManagementAdapter.isShowCheckBox()) {
					setContextButtonSelecteMode();
				}
			};

			@Override
			public void negativeResponse(Context c, Object[] o) {}
    	});
    	
    	mLogFileManagementAdapter=
    				new CommonLogFileListAdapter(getActivity(), R.layout.log_file_list_item,mLogFileList,  
    						ntfy_cb_listener);
    	lv_log_file.setAdapter(mLogFileManagementAdapter);
    	
    	setContextButtonListener();
    	setContextButtonNormalMode();
    	
    	final Button btn_save=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_save);
    	final Button btn_send_dev=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_send);
    	final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
    	if (mShowSaveButton) btn_save.setVisibility(Button.VISIBLE);
    	else btn_save.setVisibility(Button.GONE);
    	CommonDialog.setButtonEnabled(getActivity(), btn_save, mGp.isLogEnabled());
        CommonDialog.setButtonEnabled(getActivity(), btn_send_dev, mGp.isLogEnabled());
    	cb_log_enabled.setChecked(mGp.isLogEnabled());
        mDisableChangeLogEnabled=true;
    	cb_log_enabled.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!mDisableChangeLogEnabled)
				    confirmSettingsLogOption(isChecked);
				mDisableChangeLogEnabled=false;
			}
    	});
    	mUiHandler.post(new Runnable(){
            @Override
            public void run() {
                mDisableChangeLogEnabled=false;
            }
        });
    	
    	btn_save.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				rotateLogFile(mLogFileManagementAdapter);
			}
    	});

    	btn_send_dev.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				confirmSendLog();
			}
    	});

    	lv_log_file.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				if (mLogFileManagementAdapter.getItem(0).log_file_name==null) return;
				if (mLogFileManagementAdapter.isShowCheckBox()) {
					mLogFileManagementAdapter.getItem(pos).isChecked=
							!mLogFileManagementAdapter.getItem(pos).isChecked;
					mLogFileManagementAdapter.notifyDataSetChanged();
		        	setContextButtonSelecteMode();
				} else {
					showLogFile(mLogFileManagementAdapter,pos);
				}
			}
    	});
    	
    	lv_log_file.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
				if (mLogFileManagementAdapter.isEmptyAdapter()) return true;
				if (!mLogFileManagementAdapter.getItem(pos).isChecked) {
					if (mLogFileManagementAdapter.isAnyItemSelected()) {
						int down_sel_pos=-1, up_sel_pos=-1;
						int tot_cnt=mLogFileManagementAdapter.getCount();
						if (pos+1<=tot_cnt) {
							for(int i=pos+1;i<tot_cnt;i++) {
								if (mLogFileManagementAdapter.getItem(i).isChecked) {
									up_sel_pos=i;
									break;
								}
							}
						}
						if (pos>0) {
							for(int i=pos;i>=0;i--) {
								if (mLogFileManagementAdapter.getItem(i).isChecked) {
									down_sel_pos=i;
									break;
								}
							}
						}
//						Log.v("","up="+up_sel_pos+", down="+down_sel_pos);
						if (up_sel_pos!=-1 && down_sel_pos==-1) {
							for (int i=pos;i<up_sel_pos;i++) 
								mLogFileManagementAdapter.getItem(i).isChecked=true;
						} else if (up_sel_pos!=-1 && down_sel_pos!=-1) {
							for (int i=down_sel_pos+1;i<up_sel_pos;i++) 
								mLogFileManagementAdapter.getItem(i).isChecked=true;
						} else if (up_sel_pos==-1 && down_sel_pos!=-1) {
							for (int i=down_sel_pos+1;i<=pos;i++) 
								mLogFileManagementAdapter.getItem(i).isChecked=true;
						}
						mLogFileManagementAdapter.notifyDataSetChanged();
					} else {
						mLogFileManagementAdapter.setShowCheckBox(true);
						mLogFileManagementAdapter.getItem(pos).isChecked=true;
						mLogFileManagementAdapter.notifyDataSetChanged();
					}
					setContextButtonSelecteMode();
				}
				return true;
			}
    	});

    	btn_close.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				mFragment.dismiss();
			}
    	});
    	
//    	CommonDialog.setDlgBoxSizeLimit(mDialog, true);
    };
    
    private ThemeColorList mThemeColorList=null;
	private void confirmSendLog() {
		CommonLogUtil.flushLog(mContext, mGp);

        File ilf=new File(mGp.getLogDirName()+"/"+mGp.getLogFileName()+".txt");
        if (ilf.length()==0) {
            MessageDialogFragment mdf =MessageDialogFragment.newInstance(false, "W",
                    mContext.getString(R.string.msgs_log_file_list_empty_can_not_send), "");
            mdf.showDialog(mFragment.getFragmentManager(), mdf, null);
            return ;
        }

		mThemeColorList=ThemeUtil.getThemeColorList(getActivity());
		createTempLogFile();


        final Dialog dialog=new Dialog(getActivity(), ThemeUtil.getAppTheme(getActivity()));
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.confirm_send_log_dlg);
		dialog.setCanceledOnTouchOutside(false);
		
		LinearLayout title_view=(LinearLayout)dialog.findViewById(R.id.confirm_send_log_dlg_title_view);
		title_view.setBackgroundColor(mThemeColorList.title_background_color);
		TextView title=(TextView)dialog.findViewById(R.id.confirm_send_log_dlg_title);
		title.setTextColor(mThemeColorList.title_text_color);
		TextView msg=(TextView)dialog.findViewById(R.id.confirm_send_log_dlg_msg);
//		msg.setTextColor(mThemeColorList.text_color_primary);
//		msg.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
		msg.setText(mSendMessage);

        final Button btn_send_password=(Button)dialog.findViewById(R.id.confirm_send_log_dlg_send_password);
        final Button btn_ok=(Button)dialog.findViewById(R.id.confirm_send_log_dlg_ok_btn);
		final Button btn_cancel=(Button)dialog.findViewById(R.id.confirm_send_log_dlg_cancel_btn);
		final Button btn_preview=(Button)dialog.findViewById(R.id.confirm_send_log_dlg_preview);
		
		CommonDialog.setDlgBoxSizeLimit(dialog, false);
		
		btn_preview.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
			    try {
                    startLogfileViewerIntent(mContext, getTempLogFilePath());
                } catch(Exception e) {
                    MessageDialogFragment mdf =MessageDialogFragment.newInstance(false, "W",
                            mContext.getString(R.string.msgs_log_file_browse_app_can_not_found), e.getMessage());
                    mdf.showDialog(mFragment.getFragmentManager(), mdf, null);
                }
			}
		});

        btn_send_password.setVisibility(Button.GONE);
        CommonDialog.setViewEnabled(getActivity(), btn_send_password, true);
        btn_send_password.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mZipFilePassword!=null && !mZipFilePassword.equals("")) {
                    confirmSendPassword(mZipFilePassword);
                }
            }
        });

        btn_ok.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
//				sendLogFileToDeveloper(getTempLogFilePath());
				getProblemDescription(getTempLogFilePath(), btn_send_password);
//				dialog.dismiss();
			}
		});
		
		btn_cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
                deleteTempLogFile();
			}
		});

		dialog.setOnCancelListener(new OnCancelListener(){
			@Override
			public void onCancel(DialogInterface arg0) {
				btn_cancel.performClick();
			}
		});

		dialog.show();
	};

	public void startLogfileViewerIntent(Context c, String fpath) {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://"+fpath), "text/plain");
        startActivity(Intent.createChooser(intent, fpath));
    }

    private String mZipFilePassword="";

    private void getProblemDescription(final String fp, final Button btn_send_password) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirm_send_log_option_dlg);

        final LinearLayout ll_dlg_view = (LinearLayout) dialog.findViewById(R.id.confirm_send_log_option_dlg_view);
//        CommonUtilities.setDialogBoxOutline(mContext, ll_dlg_view);
//        ll_dlg_view.setBackgroundColor(mGp.themeColorList.dialog_msg_background_color);

        final LinearLayout title_view = (LinearLayout) dialog.findViewById(R.id.confirm_send_log_option_dlg_title_view);
        final TextView tv_title = (TextView) dialog.findViewById(R.id.confirm_send_log_option_dlg_title);
        title_view.setBackgroundColor(mThemeColorList.title_background_color);
        tv_title.setTextColor(mThemeColorList.title_text_color);

        final TextView tv_msg=(TextView)dialog.findViewById(R.id.confirm_send_log_option_dlg_msg);
        tv_msg.setTextColor(mThemeColorList.text_color_error);
        final EditText et_prob=(EditText)dialog.findViewById(R.id.confirm_send_log_option_dlg_question);
        et_prob.setHint(mContext.getString(R.string.msgs_log_file_prob_question_desc_title));
        et_prob.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        final CheckedTextView ct_use_password=(CheckedTextView)dialog.findViewById(R.id.confirm_send_log_option_dlg_use_password);
        final LinearLayout ll_password_view=(LinearLayout)dialog.findViewById(R.id.confirm_send_log_option_dlg_password_view);
        final EditText et_password=(EditText)dialog.findViewById(R.id.confirm_send_log_option_dlg_password);
        final EditText et_confirm_password=(EditText)dialog.findViewById(R.id.confirm_send_log_option_dlg_confirm_password);
        final Button btn_ok=(Button)dialog.findViewById(R.id.confirm_send_log_option_dlg_ok_btn);
        final Button btn_cancel=(Button)dialog.findViewById(R.id.confirm_send_log_option_dlg_cancel_btn);

        CommonDialog.setDlgBoxSizeLimit(dialog, false);

        ct_use_password.setChecked(false);
        ll_password_view.setVisibility(LinearLayout.GONE);
        ct_use_password.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked=!ct_use_password.isChecked();
                ct_use_password.setChecked(isChecked);
                if (isChecked) ll_password_view.setVisibility(LinearLayout.VISIBLE);
                else ll_password_view.setVisibility(LinearLayout.GONE);
                setSendButtonEnabled(dialog);
            }
        });
        et_prob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                setSendButtonEnabled(dialog);
            }
        });
        et_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                setSendButtonEnabled(dialog);
            }
        });
        et_confirm_password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                setSendButtonEnabled(dialog);
            }
        });

        setSendButtonEnabled(dialog);
        btn_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendLogFileToDeveloper(getTempLogFilePath(), et_prob.getText().toString(), et_password.getText().toString());
                if (ct_use_password.isChecked() && !et_password.getText().toString().equals("")) {
                    mZipFilePassword=et_password.getText().toString();
                    btn_send_password.setVisibility(Button.VISIBLE);
                }
                dialog.dismiss();
            }
        });

        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                btn_cancel.performClick();
            }
        });

        dialog.show();
    }

    private void setSendButtonEnabled(Dialog dialog) {
        final TextView msg=(TextView)dialog.findViewById(R.id.confirm_send_log_option_dlg_msg);

        final Button btn_ok=(Button)dialog.findViewById(R.id.confirm_send_log_option_dlg_ok_btn);
        final Button btn_cancel=(Button)dialog.findViewById(R.id.confirm_send_log_option_dlg_cancel_btn);

        final CheckedTextView ct_use_password=(CheckedTextView)dialog.findViewById(R.id.confirm_send_log_option_dlg_use_password);
        final LinearLayout ll_password_view=(LinearLayout)dialog.findViewById(R.id.confirm_send_log_option_dlg_password_view);
        final EditText et_prob=(EditText)dialog.findViewById(R.id.confirm_send_log_option_dlg_question);
        final EditText et_password=(EditText)dialog.findViewById(R.id.confirm_send_log_option_dlg_password);
        final EditText et_confirm_password=(EditText)dialog.findViewById(R.id.confirm_send_log_option_dlg_confirm_password);
        if(et_prob.getText().length()<10) {
            msg.setText(mContext.getString(R.string.msgs_log_file_list_confirm_send_log_description));
            CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
            et_prob.requestFocus();
            return;
        }
        if (ct_use_password.isChecked()) {
            if (et_password.length()==0) {
                msg.setText(mContext.getString(R.string.msgs_log_file_list_confirm_send_log_password_not_specified));
                CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
                return;
            }
            if (et_confirm_password.length()==0) {
                msg.setText(mContext.getString(R.string.msgs_log_file_list_confirm_send_log_confirm_password_not_specified));
                CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
                return;
            }
            if (et_password.getText().length()<6 || et_password.getText().length()>20) {
                msg.setText(mContext.getString(R.string.msgs_log_file_list_confirm_send_log_password_length_error));
                CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
                return;
            }
            if (!et_password.getText().toString().equals(et_confirm_password.getText().toString())) {
                msg.setText(mContext.getString(R.string.msgs_log_file_list_confirm_send_log_password_and_confirm_password_not_matched));
                CommonDialog.setViewEnabled(getActivity(), btn_ok, false);
                return;
            }
        }
        msg.setText("");
        CommonDialog.setViewEnabled(getActivity(), btn_ok, true);
    }

    private void confirmSendPassword(final String password) {
        NotifyEvent ntfy=new NotifyEvent(mContext);
        ntfy.setListener(new NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                sendPasswordMail(password);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {

            }
        });
        MessageDialogFragment mdf=MessageDialogFragment.newInstance(true, "W",
                mContext.getString(R.string.msgs_log_file_list_confirm_send_log_send_password_title),
                mContext.getString(R.string.msgs_log_file_list_confirm_send_log_send_password_message));
        mdf.showDialog(getFragmentManager(), mdf, ntfy);
    }


    private void sendPasswordMail(String password) {
        Intent intent=new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{MAIL_TO});
        intent.putExtra(Intent.EXTRA_SUBJECT, "None");
        intent.putExtra(Intent.EXTRA_TEXT, password);
        mContext.startActivity(intent);
    }

    private void deleteTempLogFile() {
	    String fp=getTempLogFilePath();
	    if (fp!=null) {
            File lf=new File(fp);
            lf.delete();
        }
    }

    private String getTempLogFilePath() {
	    if (mGp!=null) {
            return mGp.getLogDirName()+"/temp_log.txt";
        }
	    return null;
    }

    private void deleteZipLogFile() {
        File lf=new File(getZipLogFilePath());
        lf.delete();
    }

    private String getZipLogFilePath() {
        return mGp.getLogDirName()+"/log.zip";
    }

    private void createTempLogFile() {
        log.info("Create temp log file");

        File olf=new File(getTempLogFilePath());
		ArrayList<File> in_log_file_list=new ArrayList<File>();
        for(int i=mLogFileList.size()-1;i>=0;i--) {
            if (mLogFileList.get(i).log_file_path!=null) {
                in_log_file_list.add(new File(mLogFileList.get(i).log_file_path));
                log.info("log file appended, path="+mLogFileList.get(i).log_file_path);
            }
        }
        in_log_file_list.add(new File(mGp.getLogDirName()+"/"+mGp.getLogFileName()+".txt"));
		try {
			FileOutputStream fos=new FileOutputStream(olf);
            for(File in_file:in_log_file_list) {
                FileInputStream fis=new FileInputStream(in_file);
                byte[] buff=new byte[1024*256];
                int rc=0;
                fos.write((new String(in_file.getPath()+"\n").getBytes()));
                while((rc=fis.read(buff))>0) {
                    fos.write(buff, 0, rc);
                }
                fis.close();
                fos.write((new String("\n")).getBytes());
            }
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	};

	final public void sendLogFileToDeveloper(String log_file_path, String desc, String password) {
		CommonLogUtil.resetLogReceiver(mContext, mGp);
		
		String zip_file_name=getZipLogFilePath();
		
		File lf=new File(zip_file_name);
		lf.delete();
		
//		createZipFile(zip_file_name,log_file_path);
		String[] lmp=LocalMountPoint.convertFilePathToMountpointFormat(mContext, log_file_path);
		ZipUtil.createZipFile(mContext, null, null, zip_file_name, lmp[0], log_file_path);
//        ZipUtil.createAes256EncZipFile(mContext, null, null, zip_file_name, lmp[0], "com.sentaroh.android.LogSend", log_file_path);
        if (password.equals("")) ZipUtil.createZipFile(mContext, null, null, zip_file_name, lmp[0], log_file_path);
        else ZipUtil.createAes256EncZipFile(mContext, null, null, zip_file_name, lmp[0], password, log_file_path);

        Intent intent=new Intent();
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(Intent.ACTION_SEND);  
//	    intent.setType("message/rfc822");  
//	    intent.setType("text/plain");
	    intent.setType("application/zip");
	      
	    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{MAIL_TO});
//		    intent.putExtra(Intent.EXTRA_CC, new String[]{"cc@example.com"});  
//		    intent.putExtra(Intent.EXTRA_BCC, new String[]{"bcc@example.com"});  
	    intent.putExtra(Intent.EXTRA_SUBJECT, mSendSubject);
	    intent.putExtra(Intent.EXTRA_TEXT, desc);//mContext.getString(R.string.msgs_log_file_list_confirm_send_log_description));
	    Uri file_uri=createLogFileUri(mContext, lf.getPath());
	    intent.putExtra(Intent.EXTRA_STREAM, file_uri);//Uri.fromFile(lf));
	    try {
            mContext.startActivity(intent);
        } catch (Exception e) {
            CommonDialog mCommonDlg=new CommonDialog(getActivity(), getActivity().getSupportFragmentManager());
            mCommonDlg.showCommonDialog(false, "E",
                    mContext.getString(R.string.msgs_log_file_send_app_can_not_found), e.getMessage(), null);
        }
	};

    public Uri createLogFileUri(Context c, String fpath) {
        Uri uri=Uri.fromFile(new File(fpath));
        return uri;
    }

    private boolean mDisableChangeLogEnabled=false;
	final private void confirmSettingsLogOption(final boolean enabled) {
    	final Button btn_save=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_save);
    	final Button btn_send_dev=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_send);
    	final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
                final Button btn_save=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_save);
                final Button btn_send_dev=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_send);
                final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
				if (mNotifyUpdateLogOption!=null)
					mNotifyUpdateLogOption.notifyToListener(true, new Object[]{enabled});
		    	btn_save.setEnabled(enabled);
		    	btn_send_dev.setEnabled(enabled);

		    	Handler hndl=new Handler();
		    	hndl.postDelayed(new Runnable(){
					@Override
					public void run() {
					    int prev_count=mLogFileManagementAdapter.getCount();
						mLogFileList=CommonLogUtil.createLogFileList(mGp);
						mLogFileManagementAdapter.replaceDataList(mLogFileList);
						mLogFileManagementAdapter.notifyDataSetChanged();
						if (mLogFileManagementAdapter.getCount()!=0) setContextButtonNormalMode();
					}
		    	}, 200);
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
                final Button btn_save=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_save);
                final Button btn_send_dev=(Button)mDialog.findViewById(R.id.log_file_list_dlg_log_send);
                final CheckBox cb_log_enabled=(CheckBox)mDialog.findViewById(R.id.log_file_list_dlg_log_enabled);
				mDisableChangeLogEnabled=true;
				cb_log_enabled.setChecked(!cb_log_enabled.isChecked());
			}
		});
		String msg="";
		if (enabled) msg=mEnableMessage;
        else  msg=getString(R.string.msgs_log_file_list_confirm_log_disable);
        MessageDialogFragment cdf =MessageDialogFragment.newInstance(true, "W", msg, "");
        cdf.showDialog(getFragmentManager(),cdf,ntfy);
	};

    
	private void setContextButtonListener() {
		LinearLayout ll_prof=(LinearLayout) mDialog.findViewById(R.id.log_context_view);
        ImageButton ib_delete=(ImageButton)ll_prof.findViewById(R.id.log_context_button_delete);
        ImageButton ib_share=(ImageButton)ll_prof.findViewById(R.id.log_context_button_share);
        ImageButton ib_select_all=(ImageButton)ll_prof.findViewById(R.id.log_context_button_select_all);
        ImageButton ib_unselect_all=(ImageButton)ll_prof.findViewById(R.id.log_context_button_unselect_all);
    	final ImageButton dlg_done=(ImageButton)mDialog.findViewById(R.id.log_file_list_dlg_done);
    	
    	dlg_done.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mLogFileManagementAdapter.setAllItemChecked(false);
				mLogFileManagementAdapter.setShowCheckBox(false);
				mLogFileManagementAdapter.notifyDataSetChanged();
				setContextButtonNormalMode();
			}
        });
        
        ib_delete.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				confirmDeleteLogFile();
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, ib_delete, 
        		mContext.getString(R.string.msgs_log_file_list_label_delete));

        ib_share.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				sendLogFile();
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, ib_share, 
        		mContext.getString(R.string.msgs_log_file_list_label_share));

        ib_select_all.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mLogFileManagementAdapter.setAllItemChecked(true);
				mLogFileManagementAdapter.setShowCheckBox(true);
				setContextButtonSelecteMode();
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, ib_select_all, 
        		mContext.getString(R.string.msgs_log_file_list_label_select_all));

        ib_unselect_all.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mLogFileManagementAdapter.setAllItemChecked(false);
//				mLogFileManagementAdapter.setShowCheckBox(false);
//				setContextButtonNormalMode(mLogFileManagementAdapter);
			}
        });
        ContextButtonUtil.setButtonLabelListener(mContext, ib_unselect_all, 
        		mContext.getString(R.string.msgs_log_file_list_label_unselect_all));

	};

	private void setContextButtonSelecteMode() {
		final TextView dlg_title=(TextView)mDialog.findViewById(R.id.log_file_list_dlg_title);
    	String sel=""+mLogFileManagementAdapter.getItemSelectedCount()+"/"+mLogFileManagementAdapter.getCount();
    	dlg_title.setText(sel);

    	final ImageButton dlg_done=(ImageButton)mDialog.findViewById(R.id.log_file_list_dlg_done);
    	dlg_done.setVisibility(ImageButton.VISIBLE);
		
		LinearLayout ll_prof=(LinearLayout) mDialog.findViewById(R.id.log_context_view);
		LinearLayout ll_delete=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_delete_view);
		LinearLayout ll_share=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_share_view);
		LinearLayout ll_select_all=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_select_all_view);
		LinearLayout ll_unselect_all=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_unselect_all_view);
		
		boolean deletable_log_selected=false;
		for(int i=0;i<mLogFileManagementAdapter.getCount();i++) {
			if (mLogFileManagementAdapter.getItem(i).isChecked && !mLogFileManagementAdapter.getItem(i).isCurrentLogFile) {
				deletable_log_selected=true;
				break;
			}
		}
		if (deletable_log_selected) ll_delete.setVisibility(LinearLayout.VISIBLE);
		else ll_delete.setVisibility(LinearLayout.GONE);
		
		if (mLogFileManagementAdapter.getItemSelectedCount()>0) ll_share.setVisibility(LinearLayout.VISIBLE);
		else ll_share.setVisibility(LinearLayout.GONE);
        
        ll_select_all.setVisibility(LinearLayout.VISIBLE);
        if (mLogFileManagementAdapter.isAnyItemSelected()) ll_unselect_all.setVisibility(LinearLayout.VISIBLE);
        else ll_unselect_all.setVisibility(LinearLayout.GONE);
	};

	private void setContextButtonNormalMode() {
		final TextView dlg_title=(TextView)mDialog.findViewById(R.id.log_file_list_dlg_title);
		dlg_title.setText(mDialogTitle);

    	final ImageButton dlg_done=(ImageButton)mDialog.findViewById(R.id.log_file_list_dlg_done);
    	dlg_done.setVisibility(ImageButton.GONE);

		LinearLayout ll_prof=(LinearLayout) mDialog.findViewById(R.id.log_context_view);
		LinearLayout ll_delete=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_delete_view);
		LinearLayout ll_share=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_share_view);
		LinearLayout ll_select_all=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_select_all_view);
		LinearLayout ll_unselect_all=(LinearLayout)ll_prof.findViewById(R.id.log_context_button_unselect_all_view);

		ll_delete.setVisibility(LinearLayout.GONE);
		ll_share.setVisibility(LinearLayout.GONE);
        
    	if (mLogFileManagementAdapter.isEmptyAdapter()) {
            ll_select_all.setVisibility(LinearLayout.GONE);
            ll_unselect_all.setVisibility(LinearLayout.GONE);
    	} else {
            ll_select_all.setVisibility(LinearLayout.VISIBLE);
            ll_unselect_all.setVisibility(LinearLayout.GONE);
    	}
	};

    private void showLogFile(CommonLogFileListAdapter lfm_adapter, int pos) {
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.parse("file://"+lfm_adapter.getItem(pos).log_file_path), "text/plain");
		
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			CommonDialog mCommonDlg=new CommonDialog(getActivity(), getActivity().getSupportFragmentManager());
			mCommonDlg.showCommonDialog(false, "E", 
					mContext.getString(R.string.msgs_log_file_browse_app_can_not_found), e.getMessage(), null);
		}
    };

    private void sendLogFile() {
		final String zip_file_name=getZipLogFilePath();
		
		int no_of_files=0;
		for (int i=0;i<mLogFileManagementAdapter.getCount();i++) {
			if (mLogFileManagementAdapter.getItem(i).isChecked) no_of_files++;
		}
		final String[] file_name=new String[no_of_files];
		int files_pos=0;
		for (int i=0;i<mLogFileManagementAdapter.getCount();i++) {
			if (mLogFileManagementAdapter.getItem(i).isChecked) {
				file_name[files_pos]=mLogFileManagementAdapter.getItem(i).log_file_path;
				files_pos++;
			}
		}
		final ThreadCtrl tc=new ThreadCtrl();
		NotifyEvent ntfy=new NotifyEvent(mContext);
		ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
				tc.setDisabled();
			}
		});

		final ProgressBarDialogFragment pbdf=ProgressBarDialogFragment.newInstance(
				mContext.getString(R.string.msgs_log_file_list_dlg_send_zip_file_creating), 
				"",
				mContext.getString(R.string.msgs_common_dialog_cancel),
				mContext.getString(R.string.msgs_common_dialog_cancel));
		pbdf.showDialog(getFragmentManager(), pbdf, ntfy,true);
		Thread th=new Thread() {
			@Override
			public void run() {
				File lf=new File(zip_file_name);
				lf.delete();
				String[] lmp=LocalMountPoint.convertFilePathToMountpointFormat(mContext, file_name[0]);
				ZipUtil.createZipFile(mContext, tc,pbdf,zip_file_name,lmp[0],file_name);
				if (tc.isEnabled()) {
				    Intent intent=new Intent();
				    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				    intent.setAction(Intent.ACTION_SEND);  
//				    intent.setType("message/rfc822");  
//				    intent.setType("text/plain");
				    intent.setType("application/zip");
				    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(lf)); 
                    try {
                        mFragment.getActivity().startActivity(intent);
                    } catch (Exception e) {
                        CommonDialog mCommonDlg=new CommonDialog(getActivity(), getActivity().getSupportFragmentManager());
                        mCommonDlg.showCommonDialog(false, "E",
                                mContext.getString(R.string.msgs_log_file_send_app_can_not_found), e.getMessage(), null);
                    }

				    mUiHandler.post(new Runnable(){
						@Override
						public void run() {
							mLogFileManagementAdapter.setAllItemChecked(false);
							mLogFileManagementAdapter.setShowCheckBox(false);
							mLogFileManagementAdapter.notifyDataSetChanged();
							setContextButtonNormalMode();
						}
				    });
				} else {
					lf.delete();

					MessageDialogFragment mdf =MessageDialogFragment.newInstance(false, "W",
							mContext.getString(R.string.msgs_log_file_list_dlg_send_zip_file_cancelled),
			        		"");
			        mdf.showDialog(mFragment.getFragmentManager(), mdf, null);

				}
				pbdf.dismiss();
			};
		};
		th.start();
    };
    
    private void confirmDeleteLogFile() {
    	String delete_list="",sep="";
    	final ArrayList<String> file_path_list=new ArrayList<String>();
    	for (int i=0;i<mLogFileManagementAdapter.getCount();i++) {
    		CommonLogFileListItem item=mLogFileManagementAdapter.getItem(i);
    		if (item.isChecked && !item.isCurrentLogFile) {
    			delete_list+=sep+item.log_file_name;
    			sep="\n";
    			file_path_list.add(item.log_file_path);
    		}
    	}
    	
    	NotifyEvent ntfy=new NotifyEvent(null);
    	ntfy.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				for (int i=0;i<file_path_list.size();i++) {
					File lf=new File(file_path_list.get(i));
					lf.delete();
				}

                mLogFileManagementAdapter.setAllItemChecked(false);
                mLogFileManagementAdapter.setShowCheckBox(false);
				mLogFileList=CommonLogUtil.createLogFileList(mGp);
                mLogFileManagementAdapter.replaceDataList(mLogFileList);
                mLogFileManagementAdapter.notifyDataSetChanged();
				setContextButtonNormalMode();

			}

			@Override
			public void negativeResponse(Context c, Object[] o) {}
    	});
        MessageDialogFragment cdf =MessageDialogFragment.newInstance(true, "W",
        		mContext.getString(R.string.msgs_log_file_list_delete_confirm_msg),
        		delete_list);
        cdf.showDialog(mFragment.getFragmentManager(),cdf,ntfy);
    };

    public void performRotateLog() {
        CommonLogUtil.rotateLogFile(mContext, mGp);
    }

    public void rotateLogFile(final CommonLogFileListAdapter lfm_adapter) {
        performRotateLog();

    	mUiHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				lfm_adapter.setAllItemChecked(false);
				lfm_adapter.setShowCheckBox(false);
				mLogFileList=CommonLogUtil.createLogFileList(mGp);
				lfm_adapter.replaceDataList(mLogFileList);
				lfm_adapter.notifyDataSetChanged();
				setContextButtonNormalMode();
			}
    	},200);
    };

    public void showDialog(FragmentManager fm, Fragment frag, CommonGlobalParms gp, NotifyEvent ntfy) {
    	if (log.isInfoEnabled()) log.info("showDialog");
    	mTerminateRequired=false;
    	mGp=gp;
	    FragmentTransaction ft = fm.beginTransaction();
	    ft.add(frag,null);
	    ft.commitAllowingStateLoss();
	    mNotifyUpdateLogOption=ntfy;
//    	show(fm, APPLICATION_TAG);
    };


}
