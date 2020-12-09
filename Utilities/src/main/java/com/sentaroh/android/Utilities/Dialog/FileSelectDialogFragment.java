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
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
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
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sentaroh.android.Utilities.LocalMountPoint;
import com.sentaroh.android.Utilities.MiscUtil;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities.R;
import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.android.Utilities.SafManager;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.ThemeUtil;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistAdapter;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistItem;
import com.sentaroh.android.Utilities.Widget.CustomSpinnerAdapter;
import com.sentaroh.android.Utilities.Widget.CustomTextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class FileSelectDialogFragment extends DialogFragment {

	private boolean mDebugEnable=false;
	private final static String APPLICATION_TAG="FileSelectDialogFragment";

	private Dialog mDialog=null;
	private boolean mTerminateRequired=true;
	private Dialog mCreateDirDialog=null;
	private FileSelectDialogFragment mFragment=null;

    private String mDialogTitle="", mDialogLocalUrl="", mDialogLocalDir="", mDialogFileName="";
    private boolean mDialogEnableCreate=true;
    private boolean mDialogFileOnly=false;
    private boolean mDialogDirectoryOnly=false;
    private boolean mDialogIncludeRoot=false;
    private boolean mDialogHideMp=false;
    
    private boolean mDialogSingleSelect=true;
    private boolean mDialogMpLimitInternalAndSdcard=false;
    private boolean mDialogSelectedFilePathWithMountPoint=false;
    
    private boolean mDialogHideHiddenDirsFiles=false;
//    private CustomContextMenu mCcMenu=null;
	
	private NotifyEvent mNotifyEvent=null;
	
	private SafManager mSafFileMgr=null;
	private int mRestartStatus=0;

	public static FileSelectDialogFragment newInstance(boolean debug,
			boolean enableCreate, boolean fileOnly, boolean hideMp, boolean includeRoot, 
			boolean singleSelect, String lurl, String ldir, String file_name,String title) {
		if (debug) Log.v(APPLICATION_TAG,"newInstance"+
			" debug="+debug+", enableCreate="+enableCreate+", fileOnly="+fileOnly+
			", title="+title+", lurl="+lurl+", ldir="+ldir+", filename="+file_name+", singleSelect="+singleSelect);
        FileSelectDialogFragment frag = new FileSelectDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("debug", debug);
        bundle.putString("title", title);
        bundle.putString("filename", file_name);
        bundle.putString("lurl", lurl);
        bundle.putString("ldir", ldir);
        bundle.putBoolean("enableCreate", enableCreate);
        bundle.putBoolean("fileOnly", fileOnly);
        bundle.putBoolean("directoryOnly", false);
        bundle.putBoolean("includeRoot", includeRoot);
        bundle.putBoolean("hideMp", hideMp);
        bundle.putBoolean("singleSelect", singleSelect);
        bundle.putBoolean("mpInternalSdcard", false);
        bundle.putBoolean("selectedFilePathWithMountPoint", false);
        bundle.putBoolean("hideHiddenDirsFiles", false);
        frag.setArguments(bundle);
        return frag;
    };

    public static FileSelectDialogFragment newInstance(boolean debug,
			boolean enableCreate, boolean fileOnly, boolean hideMp, boolean includeRoot, 
			boolean singleSelect, boolean mp_internal_and_sdcard, String lurl, String ldir, String file_name,String title) {
		if (debug) Log.v(APPLICATION_TAG,"newInstance"+
			" debug="+debug+", enableCreate="+enableCreate+", fileOnly="+fileOnly+
			", title="+title+", lurl="+lurl+", ldir="+ldir+", filename="+file_name+", singleSelect="+singleSelect);
        FileSelectDialogFragment frag = new FileSelectDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("debug", debug);
        bundle.putString("title", title);
        bundle.putString("filename", file_name);
        bundle.putString("lurl", lurl);
        bundle.putString("ldir", ldir);
        bundle.putBoolean("enableCreate", enableCreate);
        bundle.putBoolean("fileOnly", fileOnly);
        bundle.putBoolean("directoryOnly", false);
        bundle.putBoolean("includeRoot", includeRoot);
        bundle.putBoolean("hideMp", hideMp);
        bundle.putBoolean("singleSelect", singleSelect);
        bundle.putBoolean("mpInternalSdcard", mp_internal_and_sdcard);
        bundle.putBoolean("selectedFilePathWithMountPoint", false);
        bundle.putBoolean("hideHiddenDirsFiles", false);
        frag.setArguments(bundle);
        return frag;
    };

    public static FileSelectDialogFragment newInstance(String lurl, String ldir, String file_name,String title) {
        FileSelectDialogFragment frag = new FileSelectDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("debug", false);
        bundle.putString("title", title);
        bundle.putString("filename", file_name);
        bundle.putString("lurl", lurl);
        bundle.putString("ldir", ldir);
        bundle.putBoolean("enableCreate", true);
        bundle.putBoolean("fileOnly", false);
        bundle.putBoolean("directoryOnly", false);
        bundle.putBoolean("includeRoot", false);
        bundle.putBoolean("hideMp", false);
        bundle.putBoolean("singleSelect", true);
        bundle.putBoolean("mpInternalSdcard", false);
        bundle.putBoolean("selectedFilePathWithMountPoint", false);
        bundle.putBoolean("hideHiddenDirsFiles", false);
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
    public void setOptionDirectoryOnly(boolean directoryOnly) {
    	Bundle bundle=getArguments();
    	bundle.putBoolean("directoryOnly", directoryOnly);
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
    public void setHideHiddenDirsFiles(boolean hide_hidden) {
    	Bundle bundle=getArguments();
    	bundle.putBoolean("hideHiddenDirsFiles", hide_hidden);
    	setArguments(bundle);
    };

	public void setNotifyEvent(NotifyEvent ntfy) {mNotifyEvent=ntfy;}

	public FileSelectDialogFragment() {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        Bundle bd=getArguments();
        mDebugEnable=bd.getBoolean("debug");
    	mDialogTitle=bd.getString("title");
    	String w_fn=bd.getString("filename");
    	if (w_fn.startsWith("/")) mDialogFileName=w_fn.substring(1);
    	else mDialogFileName=w_fn;
    	mDialogLocalUrl=bd.getString("lurl");
    	mDialogLocalDir=bd.getString("ldir");
    	mDialogEnableCreate=bd.getBoolean("enableCreate");
    	mDialogFileOnly=bd.getBoolean("fileOnly");
    	mDialogDirectoryOnly=bd.getBoolean("directoryOnly");
    	mDialogIncludeRoot=bd.getBoolean("includeRoot");
    	mDialogHideMp=bd.getBoolean("hideMp");
    	mDialogSingleSelect=bd.getBoolean("singleSelect");
    	mDialogMpLimitInternalAndSdcard=bd.getBoolean("mpInternalSdcard");
    	mDialogSelectedFilePathWithMountPoint=bd.getBoolean("selectedFilePathWithMountPoint");
    	mDialogHideHiddenDirsFiles=bd.getBoolean("hideHiddenDirsFiles");
        if (mDebugEnable) Log.v(APPLICATION_TAG,"onCreate");
        
        if (savedInstanceState!=null) mRestartStatus=2;

        mSafFileMgr=new SafManager(getActivity().getApplicationContext(), mDebugEnable);
        
    	mFragment=this;
    	if (!mTerminateRequired) {
//            setRetainInstance(true);
        	
        	if (mDebugEnable) Log.v(APPLICATION_TAG,"Create="+mDialogEnableCreate+
        			", FileOnly="+mDialogFileOnly+
        			", IncludeRoot="+mDialogIncludeRoot+
        			", SingleSelect="+mDialogSingleSelect+
        			", Title="+mDialogTitle+", lurl="+mDialogLocalUrl+
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

        mDialog=new Dialog(getActivity(), ThemeUtil.getAppTheme(getActivity()));

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
        public String mainDialogDirName=null;
        public int mainDialogFilenameSelStart=0,mainDialogFilenameTextSelEnd=0;
        public int mainDialogDirNameSelStart=0,mainDialogDirNameTextSelEnd=0;
        
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
		EditText file_name = (EditText) mDialog.findViewById(R.id.file_select_edit_dlg_file_name); 
		mSavedViewContentsValue.mainDialogFilename=file_name.getText().toString();
		mSavedViewContentsValue.mainDialogFilenameSelStart=file_name.getSelectionStart();
		mSavedViewContentsValue.mainDialogFilenameTextSelEnd=file_name.getSelectionEnd();

		final CustomTextView dir_name = (CustomTextView) mDialog.findViewById(R.id.file_select_edit_dlg_dir_name);
		mSavedViewContentsValue.mainDialogDirName=dir_name.getText().toString();

		mSavedViewContentsValue.mainDailogListViewPos[0]=mTreeFileListView.getFirstVisiblePosition();
		if (mTreeFileListView.getChildAt(0)!=null)
			mSavedViewContentsValue.mainDailogListViewPos[1]=mTreeFileListView.getChildAt(0).getTop();
		mSavedViewContentsValue.mainDailogListItems=mTreeFilelistAdapter.getDataList();
		
		mSavedViewContentsValue.mainDialogSpinnerPos=mLocalMountPointSpinner.getSelectedItemPosition();
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
    	if (mSavedViewContentsValue.mainDialogSpinnerPos!=-1) 
    		mLocalMountPointSpinner.setSelection(mSavedViewContentsValue.mainDialogSpinnerPos);

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
		    		if (mDialog.getWindow()!=null && mDialog.getWindow().getCurrentFocus()!=null)mDialog.getWindow().getCurrentFocus().invalidate();
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
    private Spinner mLocalMountPointSpinner=null;
    private ThemeColorList mThemeColorList;
	private void initViewWidget() {
    	if (mDebugEnable) Log.v(APPLICATION_TAG,"initViewWidget");

    	if (mDebugEnable) Log.v(APPLICATION_TAG,"Create="+mDialogEnableCreate+
    			", Title="+mDialogTitle+", lurl="+mDialogLocalUrl+
    			", ldir="+mDialogLocalDir+", file name="+mDialogFileName);

    	
    	mThemeColorList=ThemeUtil.getThemeColorList(getActivity());
    	
		mDialog.setContentView(R.layout.file_select_edit_dlg);
		LinearLayout title_view=(LinearLayout)mDialog.findViewById(R.id.file_select_edit_dlg_title_view);
		title_view.setBackgroundColor(mThemeColorList.title_background_color);
		TextView title=(TextView)mDialog.findViewById(R.id.file_select_edit_dlg_title);
		title.setTextColor(mThemeColorList.title_text_color);
		title.setText(mDialogTitle);
		final TextView dlg_msg = (TextView) mDialog.findViewById(R.id.file_select_edit_dlg_msg);
		final Button btnHome = (Button) mDialog.findViewById(R.id.file_select_edit_dlg_home_dir_btn);
//		btnHome.setTextColor(mThemeColorList.text_color_primary);
		btnHome.setVisibility(Button.VISIBLE);
		final Button btnCreate = (Button) mDialog.findViewById(R.id.file_select_edit_dlg_create_btn);
//		btnCreate.setTextColor(mThemeColorList.text_color_primary);
		final Button btnOk = (Button) mDialog.findViewById(R.id.file_select_edit_dlg_ok_btn);
//		btnOk.setTextColor(mThemeColorList.text_color_primary);
		final Button btnCancel = (Button)mDialog.findViewById(R.id.file_select_edit_dlg_cancel_btn);
//		btnCancel.setTextColor(mThemeColorList.text_color_primary);
		final Button btnRefresh = (Button) mDialog.findViewById(R.id.file_select_edit_dlg_refresh_btn);
//		btnRefresh.setTextColor(mThemeColorList.text_color_primary);

		LinearLayout ll_dlg_view=(LinearLayout) mDialog.findViewById(R.id.file_select_edit_dlg_view);
//		ll_dlg_view.setBackgroundColor(mThemeColorList.dialog_msg_background_color);
		
		
		final Activity activity=getActivity();
		final Context context=activity;

		if (mDialogEnableCreate) {
			btnCreate.setVisibility(TextView.VISIBLE);
		}
		
		mLocalMountPointSpinner=(Spinner) mDialog.findViewById(R.id.file_select_edit_dlg_rdir);
		setSpinnerBackground(context, mLocalMountPointSpinner, ThemeUtil.isLightThemeUsed(getActivity()));
		mLocalMountPointSpinner.setVisibility(Spinner.VISIBLE);
		//	Root directory spinner
	    CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(activity, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
	    mLocalMountPointSpinner.setPrompt(context.getString(R.string.msgs_file_select_edit_local_mount_point));
	    mLocalMountPointSpinner.setAdapter(adapter);
	
	    int a_no=-1;
	    ArrayList<String>ml=LocalMountPoint.getLocalMountPointList(context);
	    mLocalMountPointSpinner.setOnItemSelectedListener(null);
	    if (mDialogMpLimitInternalAndSdcard) {
	    	adapter.add(Environment.getExternalStorageDirectory().toString());
//			File[] fl=ContextCompat.getExternalFilesDirs(context, null);
            File[] fl= context.getExternalFilesDirs(null);
			for(File item:fl) {
			    if (item!=null && item.getPath()!=null) {
                    if (!item.getPath().startsWith(Environment.getExternalStorageDirectory().toString())) {
                        adapter.add(item.getPath().substring(0,item.getPath().indexOf("/Android")));
                        break;
                    }
                }
			}
	    	mLocalMountPointSpinner.setEnabled(true);
	        for (int i=0;i<adapter.getCount();i++) { 
					if (adapter.getItem(i).equals(mDialogLocalUrl)){
						a_no=i;
						break;
					} 
			}
	    	mLocalMountPointSpinner.setSelection(a_no);
	    } else {
		    if (ml.size()==0) {
		    	if (mDialogIncludeRoot) adapter.add("/");
		    	adapter.add(mDialogLocalUrl);
		    	mLocalMountPointSpinner.setEnabled(false);
		    } else {
		    	mLocalMountPointSpinner.setEnabled(true);
		    	if (mDialogIncludeRoot) adapter.add("/");
		    	for (int i=0;i<ml.size();i++) adapter.add(ml.get(i));
		        for (int i=0;i<ml.size();i++) { 
						if (mDialogLocalUrl.startsWith(ml.get(i))){
							a_no=i;
							break;
						} 
				}
		    	mLocalMountPointSpinner.setSelection(a_no);
		    }
	    }
	    LinearLayout ll_mp=(LinearLayout)mDialog.findViewById(R.id.file_select_edit_dlg_mp_view);
		if (mDialogHideMp) ll_mp.setVisibility(LinearLayout.GONE);
		else ll_mp.setVisibility(LinearLayout.VISIBLE);
//		ll_mp.setVisibility(LinearLayout.GONE);
		
	//	final TextView v_spacer=(TextView)mDialog.findViewById(R.id.file_select_edit_dlg_spacer);
		mTreeFileListView = (ListView) mDialog.findViewById(android.R.id.list);
		final CustomTextView dir_name = (CustomTextView) mDialog.findViewById(R.id.file_select_edit_dlg_dir_name);
//		dir_name.setTextColor(mThemeColorList.text_color_primary);
		final EditText file_name = (EditText) mDialog.findViewById(R.id.file_select_edit_dlg_file_name);
		if (!mDialogSingleSelect) file_name.setVisibility(EditText.GONE);
		else file_name.setVisibility(EditText.VISIBLE);
		if (mDialogFileOnly) {
			file_name.setVisibility(EditText.VISIBLE);
			file_name.setEnabled(true);
			dir_name.setVisibility(EditText.VISIBLE);
		} else {
			if (mDialogDirectoryOnly) {
				file_name.setVisibility(EditText.GONE);
				file_name.setEnabled(false);
				dir_name.setVisibility(EditText.VISIBLE);
				btnHome.setVisibility(EditText.VISIBLE);
			} else {
				file_name.setVisibility(EditText.VISIBLE);
				file_name.setEnabled(true);
				dir_name.setVisibility(EditText.VISIBLE);
				btnHome.setVisibility(EditText.VISIBLE);
			}
		}
		file_name.setOnKeyListener(new OnKeyListener(){
			@Override
			public boolean onKey(View arg0, int keyCode, KeyEvent event) {
	             if (//event.getAction() == KeyEvent.ACTION_DOWN &&
	                       keyCode == KeyEvent.KEYCODE_ENTER) {
	            	 return true;
	             }
	             return false;
			}
        });
	//    if (dirs.size()<=2)	v_spacer.setVisibility(TextView.VISIBLE);
		
		mTreeFilelistAdapter= new TreeFilelistAdapter(activity, mDialogSingleSelect, true);
	    mTreeFileListView.setAdapter(mTreeFilelistAdapter);
	    
		if (mDialogLocalUrl.equals("")) mDialogLocalUrl=ml.get(0);
		ArrayList<TreeFilelistItem> tfl = createLocalFilelist(mDialogFileOnly, mDialogLocalUrl,"");
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
//        		Log.v("","mp="+mDialogLocalUrl+", se;_dir="+sel_dir+", e_dir="+e_dir);
        		selectLocalDirTree(e_dir);
        	}
    		if (!mDialogFileName.equals("")) selectLocalDirTreeFile(mDialogFileName);
        }
	    mTreeFileListView.setScrollingCacheEnabled(false);
	    mTreeFileListView.setScrollbarFadingEnabled(false);

	    if (mSavedViewContentsValue!=null && mSavedViewContentsValue.mainDialogFilename!=null) {
			file_name.setText(mSavedViewContentsValue.mainDialogFilename);
			file_name.setSelection(
					mSavedViewContentsValue.mainDialogFilenameSelStart,
					mSavedViewContentsValue.mainDialogFilenameTextSelEnd);
			dir_name.setText(mSavedViewContentsValue.mainDialogDirName);
	    } else {
	    	String d_name=mDialogLocalDir.equals("")?mDialogLocalUrl+"/":mDialogLocalUrl+mDialogLocalDir+"/";
			dir_name.setText(d_name);
			file_name.setText(mDialogFileName);
	    }

	    if (!mDialogSingleSelect) btnOk.setEnabled(false);
	    	
		NotifyEvent cb_ntfy=new NotifyEvent(context);
		// set file list thread response listener 
		cb_ntfy.setListener(new NotifyEventListener() {
			@Override
			public void positiveResponse(Context c,Object[] o) {
				int p=(Integer) o[0];
				boolean p_chk=(Boolean) o[1];
				String turl=(String) mLocalMountPointSpinner.getSelectedItem();
				if (mDialogSingleSelect) {
					if (mTreeFilelistAdapter.getDataItem(p).isChecked() && !p_chk) {
						if (p!=-1) {
							if (mTreeFilelistAdapter.getDataItem(p).isChecked()) {
								if (mTreeFilelistAdapter.getDataItem(p).isDir()) {
									dir_name.setText(turl+mTreeFilelistAdapter.getDataItem(p).getPath()+
											mTreeFilelistAdapter.getDataItem(p).getName()+"/");
//									file_name.setText("");
								} else {
									dir_name.setText(turl+mTreeFilelistAdapter.getDataItem(p).getPath());
									file_name.setText(mTreeFilelistAdapter.getDataItem(p).getName());
								}
							}
						}
					}
					if (mDialogFileOnly) {
						if(file_name.getText().length()>0) {
							btnOk.setEnabled(true);
							putDlgMsg(dlg_msg,"");
						} else {
							btnOk.setEnabled(false);
							putDlgMsg(dlg_msg, context.getString(R.string.msgs_file_select_edit_dlg_filename_not_specified));
						}
					} else {
						if (mTreeFilelistAdapter.isDataItemIsSelected() || file_name.getText().length()>0) {
							btnOk.setEnabled(true);
							putDlgMsg(dlg_msg,"");
						} else {
							putDlgMsg(dlg_msg,context.getString(R.string.msgs_file_select_edit_dlg_directory_not_selected));
							btnOk.setEnabled(false);
						}
					}
				} else {
					if (mTreeFilelistAdapter.getDataItem(p).isDir()) {
						dir_name.setText(turl+mTreeFilelistAdapter.getDataItem(p).getPath()+
								mTreeFilelistAdapter.getDataItem(p).getName()+"/");
//						file_name.setText("");
					} else {
						dir_name.setText(turl+mTreeFilelistAdapter.getDataItem(p).getPath());
						file_name.setText(mTreeFilelistAdapter.getDataItem(p).getName());
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
		
		if (mDialogLocalUrl.equals(file_name.getText().toString())) btnOk.setEnabled(false);
		if (mDialogFileOnly && mDialogFileName.equals("")) {
			btnOk.setEnabled(false);
			putDlgMsg(dlg_msg, context.getString(R.string.msgs_file_select_edit_dlg_filename_not_specified));
		}
		file_name.addTextChangedListener(new TextWatcher() {
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
				String turl=(String) mLocalMountPointSpinner.getSelectedItem();
				if (tfi.isDir()) processLocalDirTree(mDialogFileOnly, turl, pos,tfi,mTreeFilelistAdapter);
				else {
		  			mTreeFilelistAdapter.setDataItemIsSelected(pos);
		  			dir_name.setText((turl+mTreeFilelistAdapter.getDataItem(pos).getPath()));
					file_name.setText(mTreeFilelistAdapter.getDataItem(pos).getName());
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
				String turl=(String) mLocalMountPointSpinner.getSelectedItem();
				if (tfi.isDir()) processLocalDirTree(mDialogFileOnly, turl, pos,tfi,mTreeFilelistAdapter);
				else {
		  			mTreeFilelistAdapter.setDataItemIsSelected(pos);
		  			dir_name.setText((turl+mTreeFilelistAdapter.getDataItem(pos).getPath()));
					file_name.setText(mTreeFilelistAdapter.getDataItem(pos).getName());
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
			    if (mLocalMountPointSpinner!=null && mLocalMountPointSpinner.getSelectedItem()!=null) {
                    dir_name.setText(mLocalMountPointSpinner.getSelectedItem().toString()+"/");
                }
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
				fileSelectEditDialogCreateBtn(activity, context,
						dir_name.getText().toString().substring(0,dir_name.getText().length()-1),"",
						mLocalMountPointSpinner.getSelectedItem().toString(), 
						mTreeFilelistAdapter, ntfy,mTreeFileListView);
				
			}
		});
		//Refresh button
		btnRefresh.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String mp=mLocalMountPointSpinner.getSelectedItem().toString();
		        ArrayList<TreeFilelistItem> tfl =createLocalFilelist(mDialogFileOnly, mp, "");//mDialogLocalUrl,"");
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
					String[] sl_array=new String[]{dir_name.getText()+file_name.getText().toString()};
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
					String mp=mLocalMountPointSpinner.getSelectedItem().toString();
					for(int i=0;i<sl.size();i++) {
						if (mDialogSelectedFilePathWithMountPoint) sl_array[i]=mp+sl.get(i);
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

	    Handler hndl=new Handler();
	    hndl.postDelayed(new Runnable(){
			@Override
			public void run() {
			    mLocalMountPointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			        @Override
			        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//			        	Log.v("","MP selected="+position);
			            Spinner spinner = (Spinner) parent;
						String turl=(String) spinner.getSelectedItem();
						ArrayList<TreeFilelistItem> tfl =createLocalFilelist(mDialogFileOnly,turl,"");
				        if (tfl.size()<1) 
				        	tfl.add(new TreeFilelistItem(context.getString(R.string.msgs_file_select_edit_dir_empty)));
				        mTreeFilelistAdapter.setDataList(tfl);
				        mTreeFilelistAdapter.notifyDataSetChanged();
				        if (turl.startsWith(mSafFileMgr.getSdcardRootPath())) {
				            if (mSafFileMgr.getSdcardRootSafFile()==null) btnCreate.setEnabled(false);
				            else btnCreate.setEnabled(true);
				        } else btnCreate.setEnabled(true);
						dir_name.setText(turl+"/");
				        Handler hndl_sel=new Handler();
				        hndl_sel.post(new Runnable(){
							@Override
							public void run() {
						        mTreeFileListView.setSelection(0);
							}
				        });
			        }
			        @Override
			        public void onNothingSelected(AdapterView<?> arg0) {}
			    });
			}
	    }, 100);
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
	    FragmentTransaction ft = fm.beginTransaction();
	    ft.add(frag,null);
	    ft.commitAllowingStateLoss();
    };
    
    private void selectLocalDirTree(String sel_dir) {
		String[] a_dir=sel_dir.split("/");
		String turl=(String) mLocalMountPointSpinner.getSelectedItem();
    	for (int i=0;i<mTreeFilelistAdapter.getDataItemCount();i++) {
    		TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(i);
//        	Log.v("","pos="+i+", name="+tfi.getName()+", c="+sel_dir);
        	if (a_dir!=null) {
        		if (tfi.getName().equals(a_dir[0])) {
            		if (a_dir.length>1) {
        				processLocalDirTree(mDialogFileOnly, turl, i,tfi,mTreeFilelistAdapter);
        				selectLocalDirTree(sel_dir.replace(a_dir[0]+"/", ""));
            		} else {
            			processLocalDirTree(mDialogFileOnly, turl, i,tfi,mTreeFilelistAdapter);
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
    
	private void processLocalDirTree (boolean fileOnly, String lclurl, final int pos, 
			final TreeFilelistItem tfi, final TreeFilelistAdapter tfa) {
//		tfi.dump();
//		Log.v("","lurl="+lclurl+", pos="+pos+", path="+tfi.getPath()+tfi.getName());
		if (tfi.getSubDirItemCount()==0) return;
		if(tfi.isChildListExpanded()) {
//			Log.v("","hide path="+tfi.getPath()+tfi.getName());
			tfa.hideChildItem(tfi,pos);
		} else {
			if (tfi.isSubDirLoaded()) {
//				Log.v("","reshow path="+tfi.getPath()+tfi.getName());
				tfa.reshowChildItem(tfi,pos);
			} else {
//				Log.v("","reload path="+tfi.getPath()+tfi.getName());
				ArrayList<TreeFilelistItem> ntfl =
						createLocalFilelist(fileOnly, lclurl, tfi.getPath()+tfi.getName());
				tfa.addChildItem(tfi,ntfl,pos);
			}
		}
	};

	private ArrayList<TreeFilelistItem>  createLocalFilelist(boolean fileOnly, 
			String url, String dir) {
//		Log.v("","url="+url+", dir="+dir);
		ArrayList<TreeFilelistItem> tfl = new ArrayList<TreeFilelistItem>(); ;
		String tdir,fp;
		
		if (dir.equals("")) fp=tdir="/";
		else {
			tdir=dir;
			fp=dir+"/";
		}
		File lf = new File(url+tdir);
		final File[]  ff = lf.listFiles();
		TreeFilelistItem tfi=null;
		if (ff!=null) {
			for (int i=0;i<ff.length;i++){
				if (!ff[i].isHidden() || (ff[i].isHidden() && !mDialogHideHiddenDirsFiles)) {
					if (ff[i].canRead()) {
						int dirct=0;
						if (ff[i].isDirectory()) {
							File tlf=new File(url+tdir+"/"+ff[i].getName());
							File[] lfl=tlf.listFiles();
							if (lfl!=null) {
								for (int j=0;j<lfl.length;j++) {
									if (!fileOnly) {
										if (lfl[j].isDirectory()) dirct++;
									} else dirct++;
//									dirct++;
								}
							}
						}
						tfi=buildTreeFileListItem(ff[i],fp);
						tfi.setSubDirItemCount(dirct);
						if (!fileOnly) {
							if (ff[i].isDirectory()) tfl.add(tfi);
						} else tfl.add(tfi);
					}
				}
			}
			Collections.sort(tfl);
		}
		return tfl;
	};

	private TreeFilelistItem buildTreeFileListItem(File fl, String fp) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.getDefault());
		String tfs=MiscUtil.convertFileSize(fl.length());
		TreeFilelistItem tfi=null;
		if (fl.isDirectory()) {
			tfi=new TreeFilelistItem(fl.getName(),
					sdf.format(fl.lastModified())+", ", true, 0,0,false,
					fl.canRead(),fl.canWrite(),
					fl.isHidden(), fp,0);
		} else {
			tfi=new TreeFilelistItem(fl.getName(), sdf.format(fl
					.lastModified())+","+tfs, false, fl.length(), fl
					.lastModified(),false,
					fl.canRead(),fl.canWrite(),
					fl.isHidden(), fp,0);
		}
		return tfi;
	};
	
//	static private void setCheckedTextView(final CheckedTextView ctv) {
//		ctv.setOnClickListener(new OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				ctv.toggle();
//			}
//		});
//	};
//
	private void fileSelectEditDialogCreateBtn(final Activity activity,
			final Context context,
			final String c_dir, final String n_dir, final String mp,
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
					File lf=new File(c_dir+"/"+s.toString());
//					Log.v("","fp="+lf.getPath());
					if (lf.exists()) {
						btnOk.setEnabled(false);
						dlg_msg.setText(context.getString(
								R.string.msgs_single_item_input_dlg_duplicate_dir));
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
						File lf= new File(n_path);
						boolean rc_create=false;
						if (c_dir.startsWith(mSafFileMgr.getSdcardRootPath())) {
							SafFile sf=mSafFileMgr.createSdcardItem(n_path, true);
							rc_create=sf.exists();
						} else {
							rc_create=lf.mkdirs();
						}
						if (!rc_create) {
							dlg_msg.setText(String.format(
									context.getString(R.string.msgs_file_select_edit_dlg_dir_not_created),
									etDir.getText()));
							return;
						} else {
//							String[] a_dir=creat_dir.startsWith("/")?creat_dir.substring(1).split("/"):creat_dir.split("/");
							String t_path=n_path.replace(mp,"");
							String[] a_dir=t_path.startsWith("/")?t_path.substring(1).split("/"):t_path.split("/");
							String p_dir="/", sep="";
							for (int i=0;i<a_dir.length;i++) {
								if (a_dir[i]!=null && !a_dir[i].equals("")) {
//									Log.v("","p_dir="+p_dir);
//									updateTreeFileList(p_dir,a_dir[i],c_dir.replace(mp,"")+"/",tfa,lv);
									updateTreeFileList(p_dir,a_dir[i],mp,tfa,lv);
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
 				cd.showCommonDialog(true, "W", context.getString(R.string.msgs_file_select_edit_confirm_create_directory), n_path, ntfy);
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
//		Log.v("","p_dir="+p_dir+", create_dir="+creat_dir+", r_dir="+r_dir);
		File lf=new File(r_dir+p_dir+creat_dir);
		if (!p_dir.equals("/")) {//not root
        	for (int i=tfa.getDataItemCount()-1;i>=0;i--) {
        		TreeFilelistItem tfi=tfa.getDataItem(i);
        		if (tfi.isDir()){
            		String fp=tfi.getPath()+tfi.getName();
//            		Log.v("","fp="+fp);
            		if (fp.equalsIgnoreCase(p_dir)) {
            			if (tfi.isSubDirLoaded()) tfa.removeChildItem(tfi, i);
        				int sdc=tfa.getDataItem(i).getSubDirItemCount();
        				sdc++;
        				tfa.getDataItem(i).setSubDirItemCount(sdc);
        				processLocalDirTree(mDialogFileOnly,r_dir, i,tfi,tfa);
        				lv.setSelection(i);
            		}
        		}
        	}
		} else {//root
			if (findDirEntry(tfa,"/",creat_dir)>=0) return ;
			boolean found=false;
			for (int i=tfa.getDataItemCount()-1;i>=0;i--) {
        		TreeFilelistItem tfi=tfa.getDataItem(i);
        		String fp=tfi.getPath()+tfi.getName();
//        		Log.v("","tfi name="+tfi.getPath()+", comp="+fp.compareToIgnoreCase(p_dir+creat_dir));
        		if (tfi.isDir()){
            		if (fp.compareToIgnoreCase(p_dir+creat_dir)<0) {
            			found=true;
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
			if (!found) {
				TreeFilelistItem tfi=buildTreeFileListItem(lf,"/");
				tfa.insertDataItem(0, tfi);
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
