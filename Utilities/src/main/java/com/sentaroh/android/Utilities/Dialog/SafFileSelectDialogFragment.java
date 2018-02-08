package com.sentaroh.android.Utilities.Dialog;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import com.sentaroh.android.Utilities.MiscUtil;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.android.Utilities.SafFileManager;
import com.sentaroh.android.Utilities.ThemeUtil;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistAdapter;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistItem;
import com.sentaroh.android.Utilities.Widget.CustomTextView;
import com.sentaroh.android.Utilities.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SafFileSelectDialogFragment extends DialogFragment {

	private boolean mDebugEnable=false;
	private final static String APPLICATION_TAG="FileSelectDialogFragment";

	private Dialog mDialog=null;
	private boolean mTerminateRequired=true;
	private Dialog mCreateDirDialog=null;
	private SafFileSelectDialogFragment mFragment=null;

    private String mDialogTitle="", mDialogLocalUuid="", mDialogLocalDir="", mDialogFileName="";
    private boolean mDialogEnableCreate=true;
    private boolean mDialogFileOnly=false;
    private boolean mDialogIncludeRoot=false;
    private boolean mDialogSingleSelect=true;
    private boolean mDialogSelectedFilePathWithMountPoint=false;
//    private CustomContextMenu mCcMenu=null;
	
	private NotifyEvent mNotifyEvent=null;
	
	SafFileManager mSafFileMgr=null;
	private int mRestartStatus=0;
	
//	public static FileSelectDialogFragment newInstance(
//			boolean enableCreate, String lurl, String ldir, String file_name,String title) {
//        return newInstance(false, enableCreate, false, lurl, ldir, file_name, title);
//    }
//	public static FileSelectDialogFragment newInstance(
//			boolean enableCreate, boolean fileOnly, String lurl, String ldir, String file_name,String title) {
//		return newInstance(false, enableCreate, false, lurl, ldir, file_name, title);
//	}

	public static SafFileSelectDialogFragment newInstance(String uuid, String ldir, String file_name,String title) {
        SafFileSelectDialogFragment frag = new SafFileSelectDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("debug", false);
        bundle.putString("title", title);
        bundle.putString("filename", file_name);
        bundle.putString("uuid", uuid);
        bundle.putString("ldir", ldir);
        bundle.putBoolean("enableCreate", true);
        bundle.putBoolean("fileOnly", false);
        bundle.putBoolean("includeRoot", false);
        bundle.putBoolean("hideMp", false);
        bundle.putBoolean("singleSelect", true);
        bundle.putBoolean("mpInternalSdcard", false);
        bundle.putBoolean("selectedFilePathWithMountPoint", false);
        frag.setArguments(bundle);
        return frag;
    };
    
    public void setOptionDebug(boolean p) {
    	Bundle bundle=getArguments();
    	bundle.putBoolean("debug", p);
    	setArguments(bundle);
    };
    public void setOptionEnableCreate(boolean enableCreate) {
    	Bundle bundle=getArguments();
    	bundle.putBoolean("enableCreate", enableCreate);
    	setArguments(bundle);
    };
    public void setOptionFileOnly(boolean fileOnly) {
    	Bundle bundle=getArguments();
    	bundle.putBoolean("fileOnly", fileOnly);
    	setArguments(bundle);
    };
    public void setOptionDirectoryOnly(boolean dirOnly) {
    	Bundle bundle=getArguments();
    	bundle.putBoolean("dirOnly", dirOnly);
    	setArguments(bundle);
    };
    public void setOptionIncludeRoot(boolean includeRoot) {
    	Bundle bundle=getArguments();
    	bundle.putBoolean("includeRoot", includeRoot);
    	setArguments(bundle);
    };
    public void setOptionHideMountPoint(boolean hideMp) {
    	Bundle bundle=getArguments();
    	bundle.putBoolean("hideMp", hideMp);
    	setArguments(bundle);
    };
    public void setOptionSingleSelect(boolean singleSelect) {
    	Bundle bundle=getArguments();
    	bundle.putBoolean("singleSelect", singleSelect);
    	setArguments(bundle);
    };
    public void setOptionLimitMountPoint(boolean limit_mp) {
    	Bundle bundle=getArguments();
    	bundle.putBoolean("mpInternalSdcard", limit_mp);
    	setArguments(bundle);
    };
    public void setOptionSelectedFilePathWithMountPoint(boolean with_mp) {
    	Bundle bundle=getArguments();
    	bundle.putBoolean("selectedFilePathWithMountPoint", with_mp);
    	setArguments(bundle);
    };

	public void setNotifyEvent(NotifyEvent ntfy) {mNotifyEvent=ntfy;}

	public SafFileSelectDialogFragment() {
		if (mDebugEnable) Log.v(APPLICATION_TAG,"Constructor(Default)");
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {  
		super.onSaveInstanceState(outState);
		if (mDebugEnable) Log.v(APPLICATION_TAG,"onSaveInstanceState");
		if(outState.isEmpty()){
	        outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
	    }
    	saveViewContents();
	};  
	
	@Override
	final public void onConfigurationChanged(final Configuration newConfig) {
	    // Ignore orientation change to keep activity from restarting
	    super.onConfigurationChanged(newConfig);
	    if (mDebugEnable) Log.v(APPLICATION_TAG,"onConfigurationChanged");
	    reInitViewWidget();
	};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	if (mDebugEnable) Log.v(APPLICATION_TAG,"onCreateView");
    	View view=super.onCreateView(inflater, container, savedInstanceState);
    	CommonDialog.setDlgBoxSizeLimit(mDialog,true);
    	return view;
    };

    private SafFile mDialogSafRoot=null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        Bundle bd=getArguments();
        mDebugEnable=bd.getBoolean("debug");
    	mDialogTitle=bd.getString("title");
    	mDialogFileName=bd.getString("filename");
    	mDialogLocalUuid=bd.getString("uuid");
    	mDialogLocalDir=bd.getString("ldir");
    	mDialogEnableCreate=bd.getBoolean("enableCreate");
    	mDialogFileOnly=bd.getBoolean("fileOnly");
    	mDialogIncludeRoot=bd.getBoolean("includeRoot");
    	bd.getBoolean("hideMp");
    	mDialogSingleSelect=bd.getBoolean("singleSelect");
    	bd.getBoolean("mpInternalSdcard");
    	mDialogSelectedFilePathWithMountPoint=bd.getBoolean("selectedFilePathWithMountPoint");
        if (mDebugEnable) Log.v(APPLICATION_TAG,"onCreate");
        
        if (savedInstanceState!=null) mRestartStatus=2;

        mSafFileMgr=new SafFileManager(getActivity().getApplicationContext(), mDebugEnable);

    	mDialogSafRoot=mSafFileMgr.getSafFileByUuid(mDialogLocalUuid);
        
    	mFragment=this;
    	if (!mTerminateRequired) {
//            setRetainInstance(true);
        	
        	if (mDebugEnable) Log.v(APPLICATION_TAG,"Create="+mDialogEnableCreate+
        			", FileOnly="+mDialogFileOnly+
        			", IncludeRoot="+mDialogIncludeRoot+
        			", SingleSelect="+mDialogSingleSelect+
        			", Title="+mDialogTitle+", Uuid="+mDialogLocalUuid+
        			", ldir="+mDialogLocalDir+", file name="+mDialogFileName);
        	
//        	mCcMenu=new CustomContextMenu(getActivity().getResources(), this.getFragmentManager());
    	}
    }

	@Override
	final public void onResume() {
	    super.onResume();
	    if (mDebugEnable) Log.v(APPLICATION_TAG,"onResume restart="+mRestartStatus);
	    if (mRestartStatus==1) {
	    }
	    mRestartStatus=1;
	};
    
	@Override
	final public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	    if (mDebugEnable) Log.v(APPLICATION_TAG,"onActivityCreated");
	};
	@Override
	final public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    if (mDebugEnable) Log.v(APPLICATION_TAG,"onAttach");
	};
	@Override
	final public void onDetach() {
	    super.onDetach();
	    if (mDebugEnable) Log.v(APPLICATION_TAG,"onDetach");
	};
	@Override
	final public void onStart() {
//    	CommonDialog.setDlgBoxSizeLimit(mDialog,true);
	    super.onStart();
	    if (mDebugEnable) Log.v(APPLICATION_TAG,"onStart");
	    if (mTerminateRequired) mDialog.cancel();
	};
	@Override
	final public void onStop() {
	    super.onStop();
	    if (mDebugEnable) Log.v(APPLICATION_TAG,"onStop");
	};

	@Override
	public void onDestroyView() {
		if (mDebugEnable) Log.v(APPLICATION_TAG,"onDestroyView");
	    if (getDialog() != null && getRetainInstance())
	        getDialog().setDismissMessage(null);
//	    mCcMenu.cleanup();
	    super.onDestroyView();
	}
	@Override
	public void onCancel(DialogInterface di) {
		if (mDebugEnable) Log.v(APPLICATION_TAG,"onCancel");
//	    super.onCancel(di);
		if (!mTerminateRequired) {
			Button btnCancel = (Button) mDialog.findViewById(R.id.file_select_edit_dlg_cancel_btn);
			btnCancel.performClick();
		}
		super.onCancel(di);
	}
	@Override
	public void onDismiss(DialogInterface di) {
		if (mDebugEnable) Log.v(APPLICATION_TAG,"onDismiss");
		super.onDismiss(di);
	}

	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	if (mDebugEnable) Log.v(APPLICATION_TAG,"onCreateDialog");
    	mDialog=new Dialog(getActivity());//, MiscUtil.getAppTheme(getActivity()));
    	mDialog.setCanceledOnTouchOutside(false);
    	mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

    	if (!mTerminateRequired) {
    		initViewWidget();
    		restoreViewContents();
    	}
//    	setShowsDialog(true);
    	
        return mDialog;
    };

    class SavedViewContentsValue {
        public String createDialogEditText=null;
        public int createDialogEditTextSelStart=0,createDialogEditTextSelEnd=0;
        public String mainDialogFilename=null;
        public int mainDialogFilenameSelStart=0,mainDialogFilenameTextSelEnd=0;
        public String mainDialogDirName=null;
        
        public int[] mainDailogListViewPos=new int[]{-1,-1};
        public ArrayList<TreeFilelistItem> mainDailogListItems=null;
        public int mainDialogSpinnerPos=-1;
    }
    
    private SavedViewContentsValue mSavedViewContentsValue=null;
    private void resetSavedViewContents() {
    	mSavedViewContentsValue=null;
    }
    
    private void saveViewContents() {
    	if (mDebugEnable) Log.v(APPLICATION_TAG,"saveViewContents");
    	mSavedViewContentsValue=new SavedViewContentsValue();
	    if (mCreateDirDialog!=null) {
	    	final EditText etDir=(EditText) mCreateDirDialog.findViewById(R.id.single_item_input_dir);
	    	mSavedViewContentsValue.createDialogEditText=etDir.getText().toString();
	    	mSavedViewContentsValue.createDialogEditTextSelStart=etDir.getSelectionStart();
	    	mSavedViewContentsValue.createDialogEditTextSelEnd=etDir.getSelectionEnd();
	    	mCreateDirDialog.dismiss();
	    }
		EditText filename = (EditText) mDialog.findViewById(R.id.file_select_edit_dlg_file_name);
		mSavedViewContentsValue.mainDialogFilename=filename.getText().toString();
		mSavedViewContentsValue.mainDialogFilenameSelStart=filename.getSelectionStart();
		mSavedViewContentsValue.mainDialogFilenameTextSelEnd=filename.getSelectionEnd();

		final CustomTextView dir_name = (CustomTextView) mDialog.findViewById(R.id.file_select_edit_dlg_dir_name);
		mSavedViewContentsValue.mainDialogDirName=dir_name.getText().toString();

		mSavedViewContentsValue.mainDailogListViewPos[0]=mTreeFileListView.getFirstVisiblePosition();
		if (mTreeFileListView.getChildAt(0)!=null)
			mSavedViewContentsValue.mainDailogListViewPos[1]=mTreeFileListView.getChildAt(0).getTop();
		mSavedViewContentsValue.mainDailogListItems=mTreeFilelistAdapter.getDataList();
		
    };

    private void restoreViewContents() {
    	if (mDebugEnable) Log.v(APPLICATION_TAG,"restoreViewContents mSavedViewContentsValue="+mSavedViewContentsValue);
    	if (mSavedViewContentsValue==null) return;
    	Handler hndl=new Handler();
    	hndl.postDelayed(new Runnable(){
			@Override
			public void run() {
			    if (mCreateDirDialog!=null) {
			    	Button btnCreate=(Button)mDialog.findViewById(R.id.file_select_edit_dlg_create_btn);
			    	btnCreate.performClick();
			    	final EditText etDir=(EditText) mCreateDirDialog.findViewById(R.id.single_item_input_dir);
			    	etDir.setText(mSavedViewContentsValue.createDialogEditText);
			    	etDir.setSelection(
			    			mSavedViewContentsValue.createDialogEditTextSelStart, 
			    			mSavedViewContentsValue.createDialogEditTextSelEnd);
			    }
			}
    	},10);

		if (mSavedViewContentsValue.mainDailogListItems!=null){
		    mTreeFilelistAdapter.setDataList(mSavedViewContentsValue.mainDailogListItems);
		    mTreeFileListView.setSelectionFromTop(mSavedViewContentsValue.mainDailogListViewPos[0],
		    		mSavedViewContentsValue.mainDailogListViewPos[1]);
		}
    	resetSavedViewContents();
    };

    private void reInitViewWidget() {
    	if (mDebugEnable) Log.v(APPLICATION_TAG,"reInitViewWidget");
    	if (!mTerminateRequired) {
    		Handler hndl=new Handler();
    		hndl.post(new Runnable(){
				@Override
				public void run() {
					mDialog.hide();
		        	saveViewContents();
		    		mDialog.getWindow().getCurrentFocus().invalidate();
		        	initViewWidget();
		    		restoreViewContents();
		    		CommonDialog.setDlgBoxSizeLimit(mDialog,true);
		    		mDialog.onContentChanged();
		    		mDialog.show();
				}
    		});
    	}
    };
    
	@SuppressWarnings("deprecation")
	public static void setSpinnerBackground(Context c, Spinner spinner, boolean theme_is_light) {
		if (theme_is_light) spinner.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.spinner_color_background_light));
		else spinner.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.spinner_color_background));
	};
    
    private ListView mTreeFileListView=null;
    private TreeFilelistAdapter mTreeFilelistAdapter=null;
    private ThemeColorList mThemeColorList;
    private void initViewWidget() {
    	if (mDebugEnable) Log.v(APPLICATION_TAG,"initViewWidget");

    	if (mDebugEnable) Log.v(APPLICATION_TAG,"Create="+mDialogEnableCreate+
    			", Title="+mDialogTitle+", lurl="+mDialogLocalUuid+
    			", ldir="+mDialogLocalDir+", file name="+mDialogFileName);

    	
    	mThemeColorList=ThemeUtil.getThemeColorList(getActivity());
    	
		mDialog.setContentView(R.layout.file_select_edit_dlg);
		LinearLayout title_view=(LinearLayout)mDialog.findViewById(R.id.file_select_edit_dlg_title_view);
		title_view.setBackgroundColor(mThemeColorList.dialog_title_background_color);
		TextView title=(TextView)mDialog.findViewById(R.id.file_select_edit_dlg_title);
		title.setTextColor(mThemeColorList.text_color_dialog_title);
		title.setText(mDialogTitle);
		final TextView dlg_msg = (TextView)mDialog.findViewById(R.id.file_select_edit_dlg_msg);
		final Button btnHome = (Button) mDialog.findViewById(R.id.file_select_edit_dlg_home_dir_btn);
		btnHome.setTextColor(mThemeColorList.text_color_primary);
		btnHome.setVisibility(Button.VISIBLE);

		final Button btnCreate = (Button) mDialog.findViewById(R.id.file_select_edit_dlg_create_btn);
		btnCreate.setTextColor(mThemeColorList.text_color_primary);
		final Button btnOk = (Button) mDialog.findViewById(R.id.file_select_edit_dlg_ok_btn);
//		btnOk.setTextColor(mThemeColorList.text_color_primary);
		final Button btnCancel = (Button)mDialog.findViewById(R.id.file_select_edit_dlg_cancel_btn);
		btnCancel.setTextColor(mThemeColorList.text_color_primary);
		final Button btnRefresh = (Button) mDialog.findViewById(R.id.file_select_edit_dlg_refresh_btn);
		btnRefresh.setTextColor(mThemeColorList.text_color_primary);

		LinearLayout ll_dlg_view=(LinearLayout) mDialog.findViewById(R.id.file_select_edit_dlg_view);
//		LinearLayout ll_dlg_view_filename=(LinearLayout) mDialog.findViewById(R.id.file_select_edit_dlg_view_filename);
//		LinearLayout ll_dlg_view_create=(LinearLayout) mDialog.findViewById(R.id.file_select_edit_dlg_view_create);
//		LinearLayout ll_dlg_view_btn=(LinearLayout) mDialog.findViewById(R.id.file_select_edit_dlg_view_btn);
		ll_dlg_view.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
//		ll_dlg_view_filename.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
//		ll_dlg_view_create.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
//		ll_dlg_view_btn.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
		
		
		final Activity activity=getActivity();
		final Context context=activity.getApplicationContext();
	
		if (mDialogEnableCreate) {
			btnCreate.setVisibility(TextView.VISIBLE);
		}
		
	    LinearLayout ll_mp=(LinearLayout)mDialog.findViewById(R.id.file_select_edit_dlg_mp_view);
	    ll_mp.setVisibility(LinearLayout.GONE);
		
		mTreeFileListView = (ListView) mDialog.findViewById(android.R.id.list);
		final EditText filename = (EditText) mDialog.findViewById(R.id.file_select_edit_dlg_file_name);
		final CustomTextView dir_name = (CustomTextView) mDialog.findViewById(R.id.file_select_edit_dlg_dir_name);
		filename.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent event) {
	             if (//event.getAction() == KeyEvent.ACTION_DOWN &&
	                       keyCode == KeyEvent.KEYCODE_ENTER) {
	            	 return true;
	             }
	             return false;
			}
        });
		if (!mDialogSingleSelect) {
			filename.setVisibility(EditText.GONE);
		}
	//    if (dirs.size()<=2)	v_spacer.setVisibility(TextView.VISIBLE);
		
		mTreeFilelistAdapter= new TreeFilelistAdapter(activity, mDialogSingleSelect, true);
	    mTreeFileListView.setAdapter(mTreeFilelistAdapter);
//	    if (mDialogFileOnly) {
//		    mTreeFilelistAdapter.setDirectorySelectable(false);
//		    mTreeFilelistAdapter.setFileSelectable(true);
//	    }
//	    if (mDialogDirOnly) {
//		    mTreeFilelistAdapter.setDirectorySelectable(true);
//		    mTreeFilelistAdapter.setFileSelectable(false);
//	    }
	    
		ArrayList<TreeFilelistItem> tfl = createLocalFilelist(mDialogFileOnly, mDialogLocalUuid,"");
        if (tfl.size()==0) {
        	tfl.add(new TreeFilelistItem(context.getString(R.string.msgs_file_select_edit_dir_empty)));
        } else {
	        mTreeFilelistAdapter.setDataList(tfl);
        	if (!mDialogLocalDir.equals("")) {
        		String sel_dir=mDialogLocalDir;
        		String n_dir="", e_dir="";
        		if (sel_dir.startsWith("/")) n_dir=sel_dir.substring(1);
        		else n_dir=sel_dir;
        		if (n_dir.endsWith("/")) e_dir=n_dir.substring(0,n_dir.length()-1);
        		else e_dir=n_dir;
        		selectLocalDirTree(e_dir);
        	}
    		if (!mDialogFileName.equals("")) selectLocalDirTreeFile(mDialogFileName);
        }
	    mTreeFileListView.setScrollingCacheEnabled(false);
	    mTreeFileListView.setScrollbarFadingEnabled(false);

	    if (mSavedViewContentsValue!=null && mSavedViewContentsValue.mainDialogFilename!=null) {
			filename.setText(mSavedViewContentsValue.mainDialogFilename);
			filename.setSelection(
					mSavedViewContentsValue.mainDialogFilenameSelStart,
					mSavedViewContentsValue.mainDialogFilenameTextSelEnd);
			dir_name.setText(mSavedViewContentsValue.mainDialogDirName);
	    } else {
//	    	if (mDialogLocalDir.equals("")) filename.setText(mDialogLocalUuid+mDialogFileName);
//	    	else filename.setText(mDialogLocalUuid+mDialogLocalDir+"/"+mDialogFileName);
//			filename.setSelection(filename.getText().toString().length());
			dir_name.setText(mDialogLocalDir+"/");
			filename.setText(mDialogFileName);
	    }

//		CommonDialog.setDlgBoxSizeLimit(mDialog,true);
	//	setDlgBoxSize(dialog,0,0,false);
	
	    if (!mDialogSingleSelect) btnOk.setEnabled(false);
	    	
		NotifyEvent cb_ntfy=new NotifyEvent(context);
		// set file list thread response listener 
		cb_ntfy.setListener(new NotifyEventListener() {
			@Override
			public void positiveResponse(Context c,Object[] o) {
				int p=(Integer) o[0];
				boolean p_chk=(Boolean) o[1];
				if (mDialogSingleSelect) {
					if (mTreeFilelistAdapter.getDataItem(p).isChecked() && !p_chk) {
						if (p!=-1) {
							if (mTreeFilelistAdapter.getDataItem(p).isChecked()) {
								if (mTreeFilelistAdapter.getDataItem(p).isDir()) {
									dir_name.setText(mTreeFilelistAdapter.getDataItem(p).getPath()+
											mTreeFilelistAdapter.getDataItem(p).getName()+"/");
//									filename.setText("");
								} else {
									dir_name.setText(mTreeFilelistAdapter.getDataItem(p).getPath());
									filename.setText(mTreeFilelistAdapter.getDataItem(p).getName());
								}
							}
						}
					}
					if (mDialogFileOnly) {
						if(filename.getText().length()>0) {
							btnOk.setEnabled(true);
							putDlgMsg(dlg_msg,"");
						} else {
							btnOk.setEnabled(false);
							putDlgMsg(dlg_msg, context.getString(R.string.msgs_file_select_edit_dlg_filename_not_specified));
						}
					} else {
						if (mTreeFilelistAdapter.isDataItemIsSelected() || filename.getText().length()>0) {
							btnOk.setEnabled(true);
							putDlgMsg(dlg_msg,"");
						} else {
							putDlgMsg(dlg_msg,context.getString(R.string.msgs_file_select_edit_dlg_directory_not_selected));
							btnOk.setEnabled(false);
						}
					}
				} else {
					if (mTreeFilelistAdapter.getDataItem(p).isDir()) {
						dir_name.setText(mTreeFilelistAdapter.getDataItem(p).getPath()+
								mTreeFilelistAdapter.getDataItem(p).getName()+"/");
					} else {
						dir_name.setText(mTreeFilelistAdapter.getDataItem(p).getPath());
						filename.setText(mTreeFilelistAdapter.getDataItem(p).getName());
					}
					putDlgMsg(dlg_msg,"");
					btnOk.setEnabled(true);
				}
			}
			@Override
			public void negativeResponse(Context c,Object[] o) {
				boolean checked=false;
	//			int p=(Integer) o[0];
				boolean p_chk=(Boolean) o[1];
				if (mDialogSingleSelect) {
					if (p_chk) {
						for (int i=0;i<mTreeFilelistAdapter.getDataItemCount();i++) {
							if (mTreeFilelistAdapter.getDataItem(i).isChecked()) {
								checked=true;
								break;
							}
						}
						if (checked) btnOk.setEnabled(true);
						else btnOk.setEnabled(false);
					}
				} else {
//					Log.v("","sel="+p_chk);
					btnOk.setEnabled(false);
					for (int i=0;i<mTreeFilelistAdapter.getDataItemCount();i++) {
						if (mTreeFilelistAdapter.getDataItem(i).isChecked()) {
							btnOk.setEnabled(true);
							break;
						}
					}
				}
			}
		});
		mTreeFilelistAdapter.setCbCheckListener(cb_ntfy);
		
//		if (mDialogLocalUuid.equals(filename.getText().toString())) btnOk.setEnabled(false);
		filename.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				if (mDialogSingleSelect) {
					if (s.length()!=0) {
						btnOk.setEnabled(true);
						putDlgMsg(dlg_msg, "");
					} else {
						btnOk.setEnabled(false);
						putDlgMsg(dlg_msg, context.getString(R.string.msgs_file_select_edit_dlg_filename_not_specified));
					}
				}
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
		});
	
        NotifyEvent ntfy_expand_close=new NotifyEvent(context);
        ntfy_expand_close.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				int idx=(Integer)o[0];
	    		final int pos=mTreeFilelistAdapter.getItem(idx);
	    		final TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(pos);
				if (tfi.getName().startsWith("---")) return;
				if (tfi.isDir()) processLocalDirTree(mDialogFileOnly, mDialogLocalUuid, pos,tfi,mTreeFilelistAdapter);
				else {
		  			mTreeFilelistAdapter.setDataItemIsSelected(pos);
		  			dir_name.setText(mTreeFilelistAdapter.getDataItem(pos).getPath());
					filename.setText(mTreeFilelistAdapter.getDataItem(pos).getName());
					if (mTreeFilelistAdapter.getDataItem(pos).isDir() && mDialogFileOnly) btnOk.setEnabled(false);
					else btnOk.setEnabled(true);
				}
			}
			@Override
			public void negativeResponse(Context c, Object[] o) {
			}
        });
        mTreeFilelistAdapter.setExpandCloseListener(ntfy_expand_close);
        mTreeFileListView.setOnItemClickListener(new OnItemClickListener(){
        	public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
	    		final int pos=mTreeFilelistAdapter.getItem(idx);
	    		final TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(pos);
				if (tfi.getName().startsWith("---")) return;
				if (tfi.isDir()) processLocalDirTree(mDialogFileOnly, mDialogLocalUuid, pos,tfi,mTreeFilelistAdapter);
				else {
		  			mTreeFilelistAdapter.setDataItemIsSelected(pos);
		  			dir_name.setText(mTreeFilelistAdapter.getDataItem(pos).getPath());
					filename.setText(mTreeFilelistAdapter.getDataItem(pos).getName());
					if (mTreeFilelistAdapter.getDataItem(pos).isDir() && mDialogFileOnly) btnOk.setEnabled(false);
					else btnOk.setEnabled(true);
				}
			}
        });

		mTreeFileListView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
				return true;
			}
		});
		
		btnHome.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dir_name.setText("/");
			}
		});
		
		//Create button
		btnCreate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				NotifyEvent ntfy=new NotifyEvent(context);
				// set file list thread response listener 
				ntfy.setListener(new NotifyEventListener() {
					@Override
					public void positiveResponse(Context c,Object[] o) {
//						btnRefresh.performClick();						
					}
					@Override
					public void negativeResponse(Context c,Object[] o) {}
	
				});
//				String mp="";
//				for(TreeFilelistItem tfi:mTreeFilelistAdapter.getDataList()) {
//					if (tfi.isChecked()) {
//						mp=tfi.getPath()+tfi.getName();
//						break;
//					}
//				}
				fileSelectEditDialogCreateBtn(activity, context, 
						dir_name.getText().substring(0,dir_name.getText().length()-1),"", mTreeFilelistAdapter, ntfy,mTreeFileListView);
				
			}
		});
		//Refresh button
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
		        ArrayList<TreeFilelistItem> tfl =createLocalFilelist(mDialogFileOnly,mDialogLocalUuid,"");//mDialogLocalUuid,"");
		        if (tfl.size()<1) 
		        	tfl.add(new TreeFilelistItem(context.getString(R.string.msgs_file_select_edit_dir_empty)));
		        mTreeFilelistAdapter.setDataList(tfl);
			}
		});
		//OK button
//		btnOk.setEnabled(false);
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mDialogSingleSelect) {
					String[] sl_array=new String[]{dir_name.getText().toString()+filename.getText().toString()};
					if (mNotifyEvent!=null) mNotifyEvent.notifyToListener(true, sl_array);
				} else {
					ArrayList<String> sl=new ArrayList<String>();
					ArrayList<TreeFilelistItem>tfl=mTreeFilelistAdapter.getDataList();
					for(TreeFilelistItem li:tfl) {
						if (li.isChecked()) {
							if (li.isDir()) sl.add(li.getPath()+li.getName());
							else sl.add(li.getPath()+li.getName());
						}
					}
					String[] sl_array=new String[sl.size()];
					for(int i=0;i<sl.size();i++) {
						if (mDialogSelectedFilePathWithMountPoint) sl_array[i]=sl.get(i);
						else sl_array[i]=sl.get(i);
//						Log.v("","sel="+sl_array[i]);
					}
					if (mNotifyEvent!=null) mNotifyEvent.notifyToListener(true, sl_array);
				}
//				mDialog.dismiss();
				mFragment.dismiss();
			}
		});
		// CANCELボタンの指定
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				mDialog.dismiss();
				mFragment.dismiss();
				if (mNotifyEvent!=null) mNotifyEvent.notifyToListener(false, null);
			}
		});

    };
    
    private void putDlgMsg(TextView msg, String txt) {
    	if (txt.equals("")) {
    		msg.setVisibility(TextView.GONE);
    		msg.setText("");
    	} else {
    		msg.setVisibility(TextView.VISIBLE);
    		msg.setText(txt);
    	}
    };

    public void showDialog(FragmentManager fm, Fragment frag, NotifyEvent ntfy) {
    	if (mDebugEnable) Log.v(APPLICATION_TAG,"showDialog");
    	mTerminateRequired=false;
    	mNotifyEvent=ntfy;
//    	Fragment prev=fm.findFragmentByTag(APPLICATION_TAG);
//    	if (prev!=null) {
//    		ft.remove(prev);
//    	}
//    	ft.addToBackStack(null);
	    FragmentTransaction ft = fm.beginTransaction();
	    ft.add(frag,null);
	    ft.commitAllowingStateLoss();
//    	show(fm, APPLICATION_TAG);
    };
    
    private void selectLocalDirTree(String sel_dir) {
		String[] a_dir=sel_dir.split("/");
    	for (int i=0;i<mTreeFilelistAdapter.getDataItemCount();i++) {
    		TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(i);
//        	Log.v("","pos="+i+", name="+tfi.getName()+", c="+sel_dir);
        	if (a_dir!=null) {
        		if (tfi.getName().equals(a_dir[0])) {
            		if (a_dir.length>1) {
        				processLocalDirTree(mDialogFileOnly, mDialogLocalUuid, i,tfi,mTreeFilelistAdapter);
        				selectLocalDirTree(sel_dir.replace(a_dir[0]+"/", ""));
            		} else {
            			processLocalDirTree(mDialogFileOnly, mDialogLocalUuid, i,tfi,mTreeFilelistAdapter);
            			mTreeFileListView.setSelection(i);
            			mTreeFilelistAdapter.setDataItemIsSelected(i);
//            			Log.v("","sel pos="+i);
            		}
        			break;
        		}
        	}
    	}
    };

    private void selectLocalDirTreeFile(String sel_file) {
    	for (int i=0;i<mTreeFilelistAdapter.getDataItemCount();i++) {
    		TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(i);
    		if (tfi.getName().equals(sel_file)) {
    			mTreeFileListView.setSelection(i);
    			mTreeFilelistAdapter.setDataItemIsSelected(i);
    			break;
    		}
    	}
    };
    
	private void processLocalDirTree (boolean fileOnly,String lclurl, final int pos, 
			final TreeFilelistItem tfi, final TreeFilelistAdapter tfa) {
//		tfi.dump();
//		Log.v("","pos="+pos);
		if (tfi.getSubDirItemCount()==0) return;
		if(tfi.isChildListExpanded()) {
			tfa.hideChildItem(tfi,pos);
		} else {
			if (tfi.isSubDirLoaded()) 
				tfa.reshowChildItem(tfi,pos);
			else {
				ArrayList<TreeFilelistItem> ntfl =
						createLocalFilelist(fileOnly,lclurl,tfi.getPath()+tfi.getName());
				tfa.addChildItem(tfi,ntfl,pos);
			}
		}
	};

	private ArrayList<TreeFilelistItem>  createLocalFilelist(boolean fileOnly, 
			String urlx, String dir) {
		
//		Log.v("","url="+url+", dir="+dir);
		
		ArrayList<TreeFilelistItem> tfl = new ArrayList<TreeFilelistItem>(); ;
		String tdir,fp;
		
		if (dir.equals("")) fp=tdir="/";
		else {
			tdir=dir;
			fp=dir+"/";
		}
		
		SafFile[]  ff=null;
		if (dir.equals("")) ff=mDialogSafRoot.listFiles();
		else {
			SafFile lf=mSafFileMgr.getSafFileBySdcardPath(mDialogSafRoot, dir, true);
			if (lf!=null) ff=lf.listFiles();
		}
		
		TreeFilelistItem tfi=null;
		if (ff!=null) {
			for (int i=0;i<ff.length;i++){
				if (ff[i].canRead()) {
					int dirct=0;
					if (ff[i].isDirectory()) {
						SafFile tlf=mSafFileMgr.getSafFileBySdcardPath(mDialogSafRoot, tdir+"/"+ff[i].getName(), true);
						SafFile[] lfl=tlf.listFiles();
						if (lfl!=null) {
							for (int j=0;j<lfl.length;j++) {
//								if (!fileOnly) {
//									if (lfl[j].isDirectory()) dirct++;
//								} else dirct++;
								dirct++;
							}
						}
					}
					tfi=buildTreeFileListItem(ff[i],fp);
					tfi.setSubDirItemCount(dirct);
//					if (!fileOnly) {
//						if (ff[i].isDirectory()) tfl.add(tfi);
//					} else tfl.add(tfi);
					tfl.add(tfi);
				}
			}
			Collections.sort(tfl);
		}
		return tfl;
	};

	private TreeFilelistItem buildTreeFileListItem(SafFile fl, String fp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault());
		String tfs=MiscUtil.convertFileSize(fl.length());
		TreeFilelistItem tfi=null;
		if (fl.isDirectory()) {
			tfi=new TreeFilelistItem(fl.getName(),
					sdf.format(fl.lastModified())+", ", true, 0,0,false,
					fl.canRead(),fl.canWrite(),
					false, fp,0);
		} else {
			tfi=new TreeFilelistItem(fl.getName(), sdf.format(fl
					.lastModified())+","+tfs, false, fl.length(), fl
					.lastModified(),false,
					fl.canRead(),fl.canWrite(),
					false, fp,0);
		}
//		TreeFilelistItem tfi=new TreeFilelistItem(fl.getName(),
//				""+", ", fl.isDirectory(), 0,0,false,
//				fl.canRead(),fl.canWrite(),
//				fl.isHidden(),fp,0);
		return tfi;
	};
	
	static private void setCheckedTextView(final CheckedTextView ctv) {
		ctv.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ctv.toggle();
			}
		});
	};

	private void fileSelectEditDialogCreateBtn(final Activity activity,
			final Context context,
			final String c_dir, String n_dir, 
			final TreeFilelistAdapter tfa,
			final NotifyEvent p_ntfy, final ListView lv) {
		// カスタムダイアログの生成
		mCreateDirDialog = new Dialog(activity);
		mCreateDirDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mCreateDirDialog.setContentView(R.layout.single_item_input_dlg);
		final TextView dlg_title = (TextView) mCreateDirDialog.findViewById(R.id.single_item_input_title);
		dlg_title.setText(context.getString(R.string.msgs_file_select_edit_dlg_create));
		final TextView dlg_msg = (TextView) mCreateDirDialog.findViewById(R.id.single_item_input_msg);
		final TextView dlg_cmp = (TextView) mCreateDirDialog.findViewById(R.id.single_item_input_name);
		final CheckedTextView dlg_type = (CheckedTextView) mCreateDirDialog.findViewById(R.id.single_item_input_type);
		setCheckedTextView(dlg_type);
		dlg_type.setVisibility(CheckedTextView.VISIBLE);
		dlg_type.setText(context.getString(R.string.msgs_file_select_edit_dlg_dir_create_file));
		final Button btnOk = (Button) mCreateDirDialog.findViewById(R.id.single_item_input_ok_btn);
		final Button btnCancel = (Button) mCreateDirDialog.findViewById(R.id.single_item_input_cancel_btn);
		final EditText etDir=(EditText) mCreateDirDialog.findViewById(R.id.single_item_input_dir);
		
		dlg_cmp.setText(context.getString(R.string.msgs_file_select_edit_parent_directory)+":"+c_dir);
		CommonDialog.setDlgBoxSizeCompact(mCreateDirDialog);
		etDir.setText(n_dir.replaceAll(c_dir, "")); 
		btnOk.setEnabled(false);
		etDir.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length()>0) {
//					SafFile lf=mSafFileMgr.getSafFileBySdcardPath(mDialogSafRoot, c_dir+"/"+s.toString(), true);
					SafFile lf=mSafFileMgr.getSafFileBySdcardPath(mDialogSafRoot, c_dir, true);
					SafFile[] c_fl=lf.listFiles();
					boolean found=false;
					for(SafFile item:c_fl) {
						if (item.getName().equals(s.toString())) {
							found=true;
							break;
						}
					}
//					Log.v("","fp="+lf.getPath());
					if (found) {
						btnOk.setEnabled(false);
						dlg_msg.setText(context.getString(R.string.msgs_single_item_input_dlg_duplicate_dir));
					} else {
						btnOk.setEnabled(true);
						dlg_msg.setText("");
					}
				}
			}
			
		});
		
		//OK button
		btnOk.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
//				NotifyEvent 
				final String creat_dir=etDir.getText().toString();
				final String n_path=c_dir+"/"+creat_dir;
				NotifyEvent ntfy=new NotifyEvent(context);
				ntfy.setListener(new NotifyEventListener(){
					@Override
					public void positiveResponse(Context c, Object[] o) {
						boolean rc_create=false;
						if (!dlg_type.isChecked()) {
							Log.v("","n_path="+n_path);
							SafFile sf=mSafFileMgr.getSafFileBySdcardPath(mSafFileMgr.getSdcardSafFile(), n_path, true);
							rc_create=sf.exists();
						} else {
							SafFile sf=mSafFileMgr.getSafFileBySdcardPath(mSafFileMgr.getSdcardSafFile(), n_path, false);
							rc_create=sf.exists();
						}
						if (!rc_create) {
							dlg_msg.setText(String.format(
									context.getString(R.string.msgs_file_select_edit_dlg_dir_not_created),
									etDir.getText()));
							return;
						} else {
//							String[] a_dir=creat_dir.startsWith("/")?creat_dir.substring(1).split("/"):creat_dir.split("/");
							String[] a_dir=n_path.startsWith("/")?n_path.substring(1).split("/"):n_path.split("/");
							String p_dir="/", sep="";
							for (int i=0;i<a_dir.length;i++) {
								if (a_dir[i]!=null && !a_dir[i].equals("")) {
//									Log.v("","p_dir="+p_dir);
									updateTreeFileList(p_dir,a_dir[i],c_dir,tfa,lv);
									p_dir+=sep+a_dir[i];
									sep="/";
								}
							}
							mCreateDirDialog.dismiss();
							mCreateDirDialog=null;
							p_ntfy.notifyToListener(true, 
									new Object[]{etDir.getText().toString()});
						}
					}
					@Override
					public void negativeResponse(Context c, Object[] o) {
					}
				});
				CommonDialog cd=new CommonDialog(context, getFragmentManager());
 				if (dlg_type.isChecked()) cd.showCommonDialog(true, "W", context.getString(R.string.msgs_file_select_edit_confirm_create_file), n_path, ntfy);
 				else cd.showCommonDialog(true, "W", context.getString(R.string.msgs_file_select_edit_confirm_create_directory), n_path, ntfy);
			}
		});
		// CANCELボタンの指定
		btnCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mCreateDirDialog.dismiss();
//				Log.v("","cancel create");
				mCreateDirDialog=null;
				p_ntfy.notifyToListener(false, null);
			}
		});
		mCreateDirDialog.show();
	};
	 
	private void updateTreeFileList(String p_dir,
			final String creat_dir,final String r_dir, 
			final TreeFilelistAdapter tfa, final ListView lv) {
		Log.v("","p_dir="+p_dir+", create_dir="+creat_dir+", r_dir="+r_dir);
		SafFile lf=mSafFileMgr.getSafFileBySdcardPath(mDialogSafRoot, r_dir+p_dir+creat_dir, true);
		if (!p_dir.equals("/")) {//not root
        	for (int i=tfa.getDataItemCount()-1;i>=0;i--) {
        		TreeFilelistItem tfi=tfa.getDataItem(i);
        		if (tfi.isDir()){
            		String fp=tfi.getPath()+tfi.getName();
            		if (fp.equalsIgnoreCase(p_dir)) {
            			if (tfi.isSubDirLoaded()) tfa.removeChildItem(tfi, i);
        				int sdc=tfa.getDataItem(i).getSubDirItemCount();
        				sdc++;
        				tfa.getDataItem(i).setSubDirItemCount(sdc);
        				processLocalDirTree(mDialogFileOnly,r_dir+p_dir, i,tfi,tfa);
        				lv.setSelection(i);
            		}
        		}
        	}
		} else {//root
			Log.v("","find="+findDirEntry(tfa,"/",creat_dir));
			if (findDirEntry(tfa,"/",creat_dir)>=0) return ;
			for (int i=tfa.getDataItemCount()-1;i>=0;i--) {
        		TreeFilelistItem tfi=tfa.getDataItem(i);
        		if (tfi.isDir()){
            		String fp=tfi.getPath()+tfi.getName();
            		if (fp.compareToIgnoreCase(p_dir+creat_dir)<0) {
            			tfi=buildTreeFileListItem(lf,"/");
            			tfi.setSubDirItemCount(0);
            			if ((i+1)>tfa.getDataItemCount()) {
            				tfa.addDataItem(tfi);
            			} else  {
            				int ip=findNextDirEntry(tfa,i+1);
            				if (ip>=0) tfa.insertDataItem(ip, tfi);
            				else {
            					ip=findLastDirEntry(tfa);
            					if (ip>=0) tfa.insertDataItem(ip+1, tfi);
            					else tfa.insertDataItem(0, tfi);
            				}
            			}
            			tfa.createShowList();
            			break;
            		}
        		}
			}
		}
	};

	private int findDirEntry(TreeFilelistAdapter tfa, String path, String dir) {
		for (int i=0;i<tfa.getDataItemCount();i++) {
			if (tfa.getDataItem(i).isDir()) {
				String lfp=tfa.getDataItem(i).getPath()+tfa.getDataItem(i).getName();
				String sfp=path+dir;
				if (lfp.equalsIgnoreCase(sfp)) return i;
			}
		}
		return -1;
	}
	
	private int findNextDirEntry(TreeFilelistAdapter tfa, int sp) {
		for (int j=sp;j<tfa.getDataItemCount();j++) {
			if (tfa.getDataItem(j).isDir()) {
				return j;
			}
		}
		return -1;
	};
	
	private int findLastDirEntry(TreeFilelistAdapter tfa) {
		for (int j=tfa.getDataItemCount()-1;j>=0;j--) {
			if (tfa.getDataItem(j).isDir()) return j;
		}
		return -1;
	}

}
