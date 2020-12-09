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
package com.sentaroh.android.Utilities.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.sentaroh.android.Utilities.LocalMountPoint;
import com.sentaroh.android.Utilities.MiscUtil;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.R;
import com.sentaroh.android.Utilities.SafFile;
import com.sentaroh.android.Utilities.SafManager;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.ThemeUtil;
import com.sentaroh.android.Utilities.ThreadCtrl;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistAdapter;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistItem;
import com.sentaroh.android.Utilities.Widget.CustomSpinnerAdapter;
import com.sentaroh.android.Utilities.Widget.CustomTextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import static com.sentaroh.android.Utilities.Dialog.CommonDialog.setButtonEnabled;

/**
 * Created by sentaroh on 2018/03/04.
 */

public class CommonFileSelector extends DialogFragment {
    private boolean mDebugEnable=false;
    private final static String APPLICATION_TAG="FileSelectDialogFragment";

    private Dialog mDialog=null;
    private boolean mTerminateRequired=true;
    private Dialog mCreateDirDialog=null;
    private CommonFileSelector mFragment=null;

    private String mDialogTitle="", mDialogLocalMP ="", mDialogLocalDir="", mDialogFileName="";
    private boolean mDialogEnableCreate=true;
    public final static int DIALOG_SELECT_CATEGORY_UNSPECIFIED=0;
    public final static int DIALOG_SELECT_CATEGORY_DIRECTORY=1;
    public final static int DIALOG_SELECT_CATEGORY_FILE=2;
    private int mDialogSelectCat=DIALOG_SELECT_CATEGORY_UNSPECIFIED;
    private boolean mDialogHideMp=false;

    private boolean mDialogIncludeMp=false;

    private boolean mDialogSingleSelect=true;
    private boolean mDialogSelectedFilePathWithMountPoint=false;

    private boolean mDialogHideHiddenDirsFiles=false;
//    private CustomContextMenu mCcMenu=null;

    private NotifyEvent mNotifyEvent=null;

    private SafManager mSafFileMgr=null;
    private int mRestartStatus=0;

    private Handler mUiHandler=null;

    private static Logger slf4jLog = LoggerFactory.getLogger(CommonFileSelector.class);

    public static CommonFileSelector newInstance(boolean debug,
                                                 boolean enableCreate, boolean hideMp, int selectCat,
                                                 boolean singleSelect, boolean include_mp,
                                                 String lmp, String ldir, String file_name, String title) {
        slf4jLog.info("newInstance"+
                " debug="+debug+", enableCreate="+enableCreate+
                ", title="+title+", lmp="+lmp+", ldir="+ldir+", filename="+file_name+", singleSelect="+singleSelect+", include_mp="+include_mp);
        CommonFileSelector frag = new CommonFileSelector();
        Bundle bundle = new Bundle();
        bundle.putBoolean("debug", debug);
        bundle.putString("title", title);
        bundle.putString("filename", file_name);
        bundle.putString("lmp", lmp);
        bundle.putString("ldir", ldir);
        bundle.putBoolean("enableCreate", enableCreate);
        bundle.putInt("selectCat", selectCat);
        bundle.putBoolean("hideMp", hideMp);
        bundle.putBoolean("singleSelect", singleSelect);
        bundle.putBoolean("includeMp", include_mp);
        bundle.putBoolean("selectedFilePathWithMountPoint", false);
        bundle.putBoolean("hideHiddenDirsFiles", false);
        frag.setArguments(bundle);
        return frag;
    };

    public void setNotifyEvent(NotifyEvent ntfy) {mNotifyEvent=ntfy;}

    public CommonFileSelector() {
        slf4jLog.info("Constructor(Default)");
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        slf4jLog.info("onSaveInstanceState");
        if(outState.isEmpty()){
            outState.putBoolean("WORKAROUND_FOR_BUG_19917_KEY", true);
        }
        saveViewContents();
    };

    @Override
    final public void onConfigurationChanged(final Configuration newConfig) {
        // Ignore orientation change to keep activity from restarting
        super.onConfigurationChanged(newConfig);
        slf4jLog.info("onConfigurationChanged");
        reInitViewWidget();
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        slf4jLog.info("onCreateView");
        View view=super.onCreateView(inflater, container, savedInstanceState);
        CommonDialog.setDlgBoxSizeLimit(mDialog,true);
        return view;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUiHandler=new Handler();
        Bundle bd=getArguments();
        mDebugEnable=bd.getBoolean("debug");
        mDialogTitle=bd.getString("title");
        String w_fn=bd.getString("filename");
        if (w_fn.startsWith("/")) mDialogFileName=w_fn.substring(1);
        else mDialogFileName=w_fn;
        mDialogLocalMP =bd.getString("lmp");
        mDialogLocalDir=bd.getString("ldir");
        mDialogEnableCreate=bd.getBoolean("enableCreate");
        mDialogSelectCat=bd.getInt("selectCat");
        mDialogHideMp=bd.getBoolean("hideMp");
        mDialogSingleSelect=bd.getBoolean("singleSelect");
        mDialogIncludeMp=bd.getBoolean("includeMp");
        mDialogSelectedFilePathWithMountPoint=bd.getBoolean("selectedFilePathWithMountPoint");
        mDialogHideHiddenDirsFiles=bd.getBoolean("hideHiddenDirsFiles");
        slf4jLog.info("onCreate");

        if (savedInstanceState!=null) mRestartStatus=2;

        mSafFileMgr=new SafManager(getActivity().getApplicationContext(), mDebugEnable);

        mFragment=this;
        if (!mTerminateRequired) {
//            setRetainInstance(true);

            slf4jLog.info("Create="+mDialogEnableCreate+
                    ", SelectCat="+mDialogSelectCat+
                    ", SingleSelect="+mDialogSingleSelect+
                    ", Title="+mDialogTitle+", lurl="+ mDialogLocalMP +
                    ", ldir="+mDialogLocalDir+", file name="+mDialogFileName);

//        	mCcMenu=new CustomContextMenu(getActivity().getResources(), this.getFragmentManager());
        }
    }

    @Override
    final public void onResume() {
        super.onResume();
        slf4jLog.info("onResume restart="+mRestartStatus);
        if (mRestartStatus==1) {
        }
        mRestartStatus=1;
    };

    @Override
    final public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        slf4jLog.info("onActivityCreated");
    };
    @Override
    final public void onAttach(Activity activity) {
        super.onAttach(activity);
        slf4jLog.info("onAttach");
    };
    @Override
    final public void onDetach() {
        super.onDetach();
        slf4jLog.info("onDetach");
    };
    @Override
    final public void onStart() {
//    	CommonDialog.setDlgBoxSizeLimit(mDialog,true);
        super.onStart();
        slf4jLog.info("onStart");
        if (mTerminateRequired) mDialog.cancel();
    };
    @Override
    final public void onStop() {
        super.onStop();
        slf4jLog.info("onStop");
    };

    @Override
    public void onDestroyView() {
        slf4jLog.info("onDestroyView");
        if (getDialog() != null && getRetainInstance())
            getDialog().setDismissMessage(null);
//	    mCcMenu.cleanup();
        super.onDestroyView();
    }
    @Override
    public void onCancel(DialogInterface di) {
        slf4jLog.info("onCancel");
//	    super.onCancel(di);
        if (!mTerminateRequired) {
            Button btnCancel = (Button) mDialog.findViewById(R.id.common_file_selector_btn_cancel);
            btnCancel.performClick();
        }
        super.onCancel(di);
    }
    @Override
    public void onDismiss(DialogInterface di) {
        slf4jLog.info("onDismiss");
        super.onDismiss(di);
    }

    private ThemeColorList mThemeColorList=null;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        slf4jLog.info("onCreateDialog");

        mDialog=new Dialog(getActivity(), ThemeUtil.getAppTheme(getActivity()));

        mDialog.setCanceledOnTouchOutside(false);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        mThemeColorList=ThemeUtil.getThemeColorList(getActivity());

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

        public boolean okButtonEnabled=false;
    }

    private CommonFileSelector.SavedViewContentsValue mSavedViewContentsValue=null;
    private void resetSavedViewContents() {
        mSavedViewContentsValue=null;
    }

    private void saveViewContents() {
        slf4jLog.info("saveViewContents");
        mSavedViewContentsValue=new CommonFileSelector.SavedViewContentsValue();
        if (mCreateDirDialog!=null) {
            final EditText etDir=(EditText) mCreateDirDialog.findViewById(R.id.single_item_input_dir);
            mSavedViewContentsValue.createDialogEditText=etDir.getText().toString();
            mSavedViewContentsValue.createDialogEditTextSelStart=etDir.getSelectionStart();
            mSavedViewContentsValue.createDialogEditTextSelEnd=etDir.getSelectionEnd();
            mCreateDirDialog.dismiss();
        }
        EditText file_name = (EditText) mDialog.findViewById(R.id.common_file_selector_file_name);
        mSavedViewContentsValue.mainDialogFilename=file_name.getText().toString();
        mSavedViewContentsValue.mainDialogFilenameSelStart=file_name.getSelectionStart();
        mSavedViewContentsValue.mainDialogFilenameTextSelEnd=file_name.getSelectionEnd();

        final CustomTextView dir_name = (CustomTextView) mDialog.findViewById(R.id.common_file_selector_filepath);
        mSavedViewContentsValue.mainDialogDirName=dir_name.getText().toString();

        mSavedViewContentsValue.mainDailogListViewPos[0]=mTreeFileListView.getFirstVisiblePosition();
        if (mTreeFileListView.getChildAt(0)!=null)
            mSavedViewContentsValue.mainDailogListViewPos[1]=mTreeFileListView.getChildAt(0).getTop();
        mSavedViewContentsValue.mainDailogListItems=mTreeFilelistAdapter.getDataList();

        mSavedViewContentsValue.mainDialogSpinnerPos=mLocalMountPointSpinner.getSelectedItemPosition();

        final Button btnOk = (Button) mDialog.findViewById(R.id.common_file_selector_btn_ok);
        mSavedViewContentsValue.okButtonEnabled=btnOk.isEnabled();
    };

    private void restoreViewContents() {
        slf4jLog.info("restoreViewContents mSavedViewContentsValue="+mSavedViewContentsValue);
        if (mSavedViewContentsValue==null) return;
        final SavedViewContentsValue sv=mSavedViewContentsValue;
        Handler hndl=new Handler();
        hndl.postDelayed(new Runnable(){
            @Override
            public void run() {
                if (sv!=null && mCreateDirDialog!=null) {
                    Button btnCreate=(Button)mDialog.findViewById(R.id.common_file_selector_create_btn);
                    btnCreate.performClick();
                    final EditText etDir=(EditText) mCreateDirDialog.findViewById(R.id.single_item_input_dir);
                    etDir.setText(sv.createDialogEditText);
                    etDir.setSelection(sv.createDialogEditTextSelStart, sv.createDialogEditTextSelEnd);
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

        final Button btnOk = (Button) mDialog.findViewById(R.id.common_file_selector_btn_ok);
        if (mSavedViewContentsValue.okButtonEnabled) CommonDialog.setViewEnabled(getActivity(), btnOk, true);
        resetSavedViewContents();
    };

    private void reInitViewWidget() {
        slf4jLog.info("reInitViewWidget");
        if (!mTerminateRequired) {
            Handler hndl=new Handler();
            hndl.post(new Runnable(){
                @Override
                public void run() {
                    mDialog.hide();
                    saveViewContents();
                    if (mDialog.getWindow()!=null && mDialog.getWindow().getCurrentFocus()!=null) mDialog.getWindow().getCurrentFocus().invalidate();
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

    private void initViewWidget() {
        slf4jLog.info("initViewWidget");

        slf4jLog.info("Create="+mDialogEnableCreate+
                ", Title="+mDialogTitle+", lurl="+ mDialogLocalMP +
                ", ldir="+mDialogLocalDir+", file name="+mDialogFileName);


        mDialog.setContentView(R.layout.common_file_selector_dlg);
        LinearLayout title_view=(LinearLayout)mDialog.findViewById(R.id.common_file_selector_dlg_title_view);
        title_view.setBackgroundColor(mThemeColorList.title_background_color);
        TextView title=(TextView)mDialog.findViewById(R.id.common_file_selector_dlg_title);
        title.setTextColor(mThemeColorList.title_text_color);
        title.setText(mDialogTitle);
        final TextView dlg_msg = (TextView) mDialog.findViewById(R.id.common_file_selector_dlg_msg);
//        dlg_msg.setVisibility(TextView.GONE);
//        final Button btnHome = (Button) mDialog.findViewById(R.id.file_select_edit_dlg_home_dir_btn);
//        btnHome.setTextColor(mThemeColorList.text_color_primary);
//        btnHome.setVisibility(Button.VISIBLE);
        final Button btnCreate = (Button) mDialog.findViewById(R.id.common_file_selector_create_btn);
//        btnCreate.setTextColor(mThemeColorList.text_color_primary);
        final Button btnOk = (Button) mDialog.findViewById(R.id.common_file_selector_btn_ok);
//		btnOk.setTextColor(mThemeColorList.text_color_primary);
        final Button btnCancel = (Button)mDialog.findViewById(R.id.common_file_selector_btn_cancel);
//        btnCancel.setTextColor(mThemeColorList.text_color_primary);
        final Button btnRefresh = (Button) mDialog.findViewById(R.id.common_file_selector_refresh_btn);
//        btnRefresh.setTextColor(mThemeColorList.text_color_primary);
        final TextView tv_empty = (TextView) mDialog.findViewById(R.id.common_file_selector_empty);

        final Button btnTop = (Button)mDialog.findViewById(R.id.common_file_selector_top_btn);
//        btnTop.setTextColor(mThemeColorList.text_color_primary);
        if (ThemeUtil.isLightThemeUsed(getActivity())) btnTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_dark, 0, 0, 0);
        else btnTop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_top_light, 0, 0, 0);

        final Button btnUp = (Button)mDialog.findViewById(R.id.common_file_selector_up_btn);
//        btnUp.setTextColor(mThemeColorList.text_color_primary);
        if (ThemeUtil.isLightThemeUsed(getActivity())) btnUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_dark, 0, 0, 0);
        else btnUp.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_16_go_up_light, 0, 0, 0);


        LinearLayout ll_dlg_view=(LinearLayout) mDialog.findViewById(R.id.common_file_selector_dlg_view);
//        ll_dlg_view.setBackgroundColor(mThemeColorList.dialog_msg_background_color);


        final Activity activity=getActivity();

        if (mDialogEnableCreate) {
            btnCreate.setVisibility(TextView.VISIBLE);
        } else {
            btnCreate.setVisibility(TextView.GONE);
        }

        mLocalMountPointSpinner=(Spinner) mDialog.findViewById(R.id.common_file_selector_storage_spinner);
        setSpinnerBackground(activity, mLocalMountPointSpinner, ThemeUtil.isLightThemeUsed(getActivity()));
        mLocalMountPointSpinner.setVisibility(Spinner.VISIBLE);
        //	Root directory spinner
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(activity, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        mLocalMountPointSpinner.setPrompt(activity.getString(R.string.msgs_file_select_edit_local_mount_point));
        mLocalMountPointSpinner.setAdapter(adapter);

        int a_no=0;
        ArrayList<String> ml= new ArrayList<String>();
//        ArrayList<String> ml= LocalMountPoint.getLocalMountpointList2(context);
        SafManager sm=new SafManager(getActivity().getApplicationContext(), false);
        File[] fl=getActivity().getApplicationContext().getExternalFilesDirs(null);
        if (fl!=null && fl.length>=1) {
            for (int i=0;i<fl.length;i++) {
                if (fl[i]!=null) {
                    String fp=fl[i].getPath();
                    if (fp.indexOf("/Android/")>0) {
                        String mp=fp.substring(0,fp.indexOf("/Android/"));
                        if (mp.startsWith("/storage/emulated")) {
                            ml.add(mp);
                        } else {
                            if (mDialogEnableCreate) {
                                if (mp.equals(sm.getSdcardRootPath()) || mp.equals(sm.getUsbRootPath()))ml.add(mp);
                            } else {
                                ml.add(mp);
                            }
                        }
                    } else {
                        ml.add(fp);
                    }
                }
            }
        }
        mLocalMountPointSpinner.setOnItemSelectedListener(null);
        if (ml.size()==0 || mDialogHideMp) {
            adapter.add(mDialogLocalMP);
            mLocalMountPointSpinner.setEnabled(false);
            mLocalMountPointSpinner.setSelection(0);
        } else {
            mLocalMountPointSpinner.setEnabled(true);
            for (int i=0;i<ml.size();i++) {
                boolean dup=false;
                for(int j=0;j<adapter.getCount();j++) {
                    if (adapter.getItem(j).equals(ml.get(i))) {
                        dup=true;
                        break;
                    }
                }
                if (!dup) {
                    adapter.add(ml.get(i));
                    if (mDialogLocalMP.equals(ml.get(i))) a_no=adapter.getCount()-1;
                }
            }
//            Log.v("","sel="+mLocalMountPointSpinner.getSelectedItemPosition()+", a_no="+a_no);
            if (a_no<0 && (a_no+1)>adapter.getCount()) a_no=0;
            mLocalMountPointSpinner.setSelection(a_no);
        }
        if (mDialogHideMp) mLocalMountPointSpinner.setVisibility(LinearLayout.GONE);
        else mLocalMountPointSpinner.setVisibility(LinearLayout.VISIBLE);
//		ll_mp.setVisibility(LinearLayout.GONE);

        //	final TextView v_spacer=(TextView)mDialog.findViewById(R.id.file_select_edit_dlg_spacer);
        mTreeFileListView = (ListView) mDialog.findViewById(R.id.common_file_selector_list);
        final CustomTextView dir_path = (CustomTextView) mDialog.findViewById(R.id.common_file_selector_filepath);
//        dir_path.setTextColor(mThemeColorList.text_color_primary);
        final LinearLayout ll_dir_name = (LinearLayout) mDialog.findViewById(R.id.common_file_selector_dir_name_view);
        final LinearLayout ll_file_name = (LinearLayout) mDialog.findViewById(R.id.common_file_selector_file_name_view);
        final TextView hdr_file_name = (TextView) mDialog.findViewById(R.id.common_file_selector_hdr_file_name);
        final EditText et_dir_name = (EditText) mDialog.findViewById(R.id.common_file_selector_dir_name);
        final EditText et_file_name = (EditText) mDialog.findViewById(R.id.common_file_selector_file_name);
        hdr_file_name.setVisibility(TextView.GONE);
        ll_dir_name.setVisibility(LinearLayout.GONE);
        ll_file_name.setVisibility(LinearLayout.VISIBLE);
        if (!mDialogSingleSelect) {
        } else {
            if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_UNSPECIFIED) {
            } else if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_DIRECTORY) {
            } else if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE) {
            }
        }
        et_file_name.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View arg0, int keyCode, KeyEvent event) {
                if (//event.getAction() == KeyEvent.ACTION_DOWN &&
                        keyCode == KeyEvent.KEYCODE_ENTER) {
                    return true;
                }
                return false;
            }
        });

        mTreeFilelistAdapter= new TreeFilelistAdapter(activity, mDialogSingleSelect, true);
        mTreeFilelistAdapter.setDirectorySelectable(!(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE));
        mTreeFilelistAdapter.setSelectable(false);
        mTreeFileListView.setAdapter(mTreeFilelistAdapter);


        if (mDialogLocalMP.equals("")) mDialogLocalMP =ml.get(0);

        NotifyEvent ntfy_file_list=new NotifyEvent(activity);
        ntfy_file_list.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context c, Object[] o) {
                ArrayList<TreeFilelistItem> tfl =(ArrayList<TreeFilelistItem>)o[0];
                if (tfl.size()==0) {
                    tv_empty.setVisibility(TextView.VISIBLE);
                    mTreeFileListView.setVisibility(TextView.GONE);
                } else {
                    tv_empty.setVisibility(TextView.GONE);
                    mTreeFileListView.setVisibility(TextView.VISIBLE);
                    mTreeFilelistAdapter.setDataList(tfl);
                }
                mTreeFileListView.setScrollingCacheEnabled(false);
                mTreeFileListView.setScrollbarFadingEnabled(false);
            }

            @Override
            public void negativeResponse(Context c, Object[] o) {
                tv_empty.setVisibility(TextView.VISIBLE);
                mTreeFileListView.setVisibility(TextView.GONE);

                mTreeFileListView.setScrollingCacheEnabled(false);
                mTreeFileListView.setScrollbarFadingEnabled(false);
            }
        });
        createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE, mDialogLocalMP, mDialogLocalDir, ntfy_file_list, false);
        mTreeFileListView.setVisibility(TextView.INVISIBLE);
        tv_empty.setVisibility(TextView.GONE);

        if (mDialogLocalDir.equals("")) setTopUpButtonEnabled(false);
        else setTopUpButtonEnabled(true);

        if (mSavedViewContentsValue!=null && mSavedViewContentsValue.mainDialogFilename!=null) {
            et_file_name.setText(mSavedViewContentsValue.mainDialogFilename);
            et_file_name.setSelection(
                    mSavedViewContentsValue.mainDialogFilenameSelStart,
                    mSavedViewContentsValue.mainDialogFilenameTextSelEnd);
            dir_path.setText(mSavedViewContentsValue.mainDialogDirName);
        } else {
            dir_path.setText(mDialogLocalMP+"/");
            if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE) {
                et_file_name.setText(mDialogFileName);
            } else {
                et_file_name.setVisibility(EditText.GONE);
                mTreeFilelistAdapter.setDirectorySelectable(false);
//                if (mDialogLocalDir.length()>1) et_file_name.setText(mDialogLocalDir.substring(1));
//                else et_file_name.setText("");
            }
        }

        if (!mDialogSingleSelect) setButtonEnabled(activity, btnOk, false);

        final NotifyEvent cb_ntfy=new NotifyEvent(activity);
        // set file list thread response listener
        cb_ntfy.setListener(new NotifyEvent.NotifyEventListener() {
            @Override
            public void positiveResponse(Context c,Object[] o) {
                int p=(Integer) o[0];
                boolean p_chk=(Boolean) o[1];
                String turl=(String) mLocalMountPointSpinner.getSelectedItem();
                if (mDialogSingleSelect) {
                    if (mTreeFilelistAdapter.getDataItem(p).isChecked() && !p_chk) {
                        if (p!=-1) {
                            if (mTreeFilelistAdapter.getDataItem(p).isChecked()) {
                                et_file_name.setText((mTreeFilelistAdapter.getDataItem(p).getName()));
                            }
                        }
                    }
                    if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE) {
                        if(et_file_name.getText().length()>0) {
                            setButtonEnabled(activity, btnOk, true);
                            putDlgMsg(dlg_msg,"");
                        } else {
                            setButtonEnabled(activity, btnOk, false);
                            putDlgMsg(dlg_msg, activity.getString(R.string.msgs_file_select_edit_dlg_filename_not_specified));
                        }
                    } else {
                        if (mTreeFilelistAdapter.isDataItemIsSelected() || et_file_name.getText().length()>0) {
                            setButtonEnabled(activity, btnOk, true);
                            putDlgMsg(dlg_msg,"");
                        } else {
                            putDlgMsg(dlg_msg, activity.getString(R.string.msgs_file_select_edit_dlg_directory_not_selected));
                            setButtonEnabled(activity, btnOk, false);
                        }
                    }
                } else {
                    if (mTreeFilelistAdapter.getDataItem(p).isDir()) {
                        dir_path.setText(turl+mTreeFilelistAdapter.getDataItem(p).getPath()+
                                mTreeFilelistAdapter.getDataItem(p).getName()+"/");
//						file_name.setText("");
                    } else {
                        dir_path.setText(turl+mTreeFilelistAdapter.getDataItem(p).getPath());
                        et_file_name.setText(mTreeFilelistAdapter.getDataItem(p).getName());
                    }
                    putDlgMsg(dlg_msg,"");
                    setButtonEnabled(activity, btnOk, true);
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
                        if (checked) setButtonEnabled(activity, btnOk, true);
                        else setButtonEnabled(activity, btnOk, false);
                    }
                } else {
//					Log.v("","sel="+p_chk);
                    setButtonEnabled(activity, btnOk, false);
                    for (int i=0;i<mTreeFilelistAdapter.getDataItemCount();i++) {
                        if (mTreeFilelistAdapter.getDataItem(i).isChecked()) {
                            setButtonEnabled(activity, btnOk, true);
                            break;
                        }
                    }
                }
            }
        });
        mTreeFilelistAdapter.setCbCheckListener(cb_ntfy);

        if (mDialogLocalMP.equals(et_file_name.getText().toString())) setButtonEnabled(activity, btnOk, false);
        if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE && mDialogFileName.equals("")) {
            setButtonEnabled(activity, btnOk, false);
            putDlgMsg(dlg_msg, activity.getString(R.string.msgs_file_select_edit_dlg_filename_not_specified));
        }
        et_file_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (mDialogSingleSelect) {
                    if (s.length()!=0) {
                        setButtonEnabled(activity, btnOk, true);
                        putDlgMsg(dlg_msg, "");
                    } else {
                        setButtonEnabled(activity, btnOk, false);
                        putDlgMsg(dlg_msg, activity.getString(R.string.msgs_file_select_edit_dlg_filename_not_specified));
                    }
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before,int count) {}
        });

        mTreeFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> items, View view, int idx, long id) {
                final int pos=mTreeFilelistAdapter.getItem(idx);
                final TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(pos);
                final String turl=(String) mLocalMountPointSpinner.getSelectedItem();
                slf4jLog.info("TreeFileListView clicked pos="+pos+", name="+tfi.getName());
                if (tfi.isDir()) {
                    if (tfi.getSubDirItemCount()>=0) {
                        NotifyEvent ntfy=new NotifyEvent(activity);
                        ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                            @Override
                            public void positiveResponse(Context c, Object[] o) {
                                ArrayList<TreeFilelistItem> tfl =(ArrayList<TreeFilelistItem>)o[0];
                                dir_path.setText((turl+mDialogLocalDir+"/").replaceAll("//","/"));
                                if (tfl.size()==0) {
                                    tv_empty.setVisibility(TextView.VISIBLE);
                                    mTreeFileListView.setVisibility(TextView.GONE);
                                } else {
                                    tv_empty.setVisibility(TextView.GONE);
                                    mTreeFileListView.setVisibility(TextView.VISIBLE);
                                    mTreeFilelistAdapter.setDataList(tfl);
                                }
                                setTopUpButtonEnabled(true);
                            }

                            @Override
                            public void negativeResponse(Context c, Object[] o) {
                            }
                        });
                        mDialogLocalDir=tfi.getPath()+tfi.getName();
                        createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE, turl,mDialogLocalDir, ntfy, true);
                    }
                } else {
                    mTreeFilelistAdapter.setDataItemIsSelected(pos);
                    et_file_name.setText(mTreeFilelistAdapter.getDataItem(pos).getName());
                    if (mTreeFilelistAdapter.getDataItem(pos).isDir() && mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE) setButtonEnabled(activity, btnOk, false);
                    else setButtonEnabled(activity, btnOk, true);
                }
            }
        });

        mTreeFileListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            public boolean onItemLongClick(AdapterView<?> items, View view, int idx, long id) {
                slf4jLog.info("TreeFileListView long clicked idx="+idx);
                if (mTreeFilelistAdapter.isDirectorySelectable()) {
                    final int pos=mTreeFilelistAdapter.getItem(idx);
                    final TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(pos);
                    mTreeFilelistAdapter.setAllItemUnchecked();
                    tfi.setChecked(true);
                    mTreeFilelistAdapter.notifyDataSetChanged();
                    cb_ntfy.notifyToListener(tfi.isChecked(), new Object[]{pos, !tfi.isChecked()});
                }
                return true;
            }
        });

        btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slf4jLog.info("TreeFileListView top button clicked");
                NotifyEvent ntfy_file_list=new NotifyEvent(activity);
                ntfy_file_list.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        ArrayList<TreeFilelistItem> tfl =(ArrayList<TreeFilelistItem>)o[0];
                        mDialogLocalDir="";
                        dir_path.setText(mDialogLocalMP+mDialogLocalDir+"/");
                        if (tfl.size()==0) {
                            tv_empty.setVisibility(TextView.VISIBLE);
                            mTreeFileListView.setVisibility(TextView.GONE);
                        } else {
                            tv_empty.setVisibility(TextView.GONE);
                            mTreeFileListView.setVisibility(TextView.VISIBLE);
                            mTreeFilelistAdapter.setDataList(tfl);
                        }
                        setTopUpButtonEnabled(false);
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        tv_empty.setVisibility(TextView.VISIBLE);
                        mTreeFileListView.setVisibility(TextView.GONE);

                        mTreeFileListView.setScrollingCacheEnabled(false);
                        mTreeFileListView.setScrollbarFadingEnabled(false);
                    }
                });
                createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE, mDialogLocalMP, "", ntfy_file_list, false);
            }
        });

        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                slf4jLog.info("TreeFileListView up button clicked");
                String c_dir=dir_path.getText().toString().substring(0,dir_path.getText().length()-1);
                String new_dir=c_dir.substring(0,c_dir.lastIndexOf("/"));
                mDialogLocalDir=new_dir.replace(mDialogLocalMP,"") ;
                if (mDialogLocalDir.equals("")) {
                    dir_path.setText(mDialogLocalMP+"/");
                    setTopUpButtonEnabled(false);
                } else {
                    dir_path.setText(mDialogLocalMP+mDialogLocalDir+"/");
                    setTopUpButtonEnabled(true);
                }
                NotifyEvent ntfy_file_list=new NotifyEvent(activity);
                ntfy_file_list.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        ArrayList<TreeFilelistItem> tfl =(ArrayList<TreeFilelistItem>)o[0];
                        if (tfl.size()==0) {
                            tv_empty.setVisibility(TextView.VISIBLE);
                            mTreeFileListView.setVisibility(TextView.GONE);
                        } else {
                            tv_empty.setVisibility(TextView.GONE);
                            mTreeFileListView.setVisibility(TextView.VISIBLE);
                            mTreeFilelistAdapter.setDataList(tfl);
                        }
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        tv_empty.setVisibility(TextView.VISIBLE);
                        mTreeFileListView.setVisibility(TextView.GONE);

                        mTreeFileListView.setScrollingCacheEnabled(false);
                        mTreeFileListView.setScrollbarFadingEnabled(false);
                    }
                });
                createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE, mDialogLocalMP, mDialogLocalDir, ntfy_file_list, false);

            }
        });

        //Create button
        btnCreate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                slf4jLog.info("TreeFileListView create button clicked");
                NotifyEvent ntfy=new NotifyEvent(activity);
                // set file list thread response listener
                ntfy.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c,Object[] o) {
                        btnRefresh.performClick();
                    }
                    @Override
                    public void negativeResponse(Context c,Object[] o) {}

                });
                fileSelectEditDialogCreateBtn(activity, activity,
                        dir_path.getText().toString().substring(0,dir_path.getText().length()-1),"",
                        mLocalMountPointSpinner.getSelectedItem().toString(),
                        mTreeFilelistAdapter, ntfy,mTreeFileListView);

            }
        });
        //Refresh button
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                slf4jLog.info("TreeFileListView refresh button clicked");
                String mp=mLocalMountPointSpinner.getSelectedItem().toString();
                NotifyEvent ntfy_file_list=new NotifyEvent(activity);
                ntfy_file_list.setListener(new NotifyEvent.NotifyEventListener() {
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        ArrayList<TreeFilelistItem> tfl =(ArrayList<TreeFilelistItem>)o[0];
                        if (tfl.size()<1) {
                            tv_empty.setVisibility(TextView.VISIBLE);
                            mTreeFileListView.setVisibility(TextView.GONE);
                        } else {
                            tv_empty.setVisibility(TextView.GONE);
                            mTreeFileListView.setVisibility(TextView.VISIBLE);
                        }
                        mTreeFilelistAdapter.setDataList(tfl);
                    }

                    @Override
                    public void negativeResponse(Context c, Object[] o) {
                        tv_empty.setVisibility(TextView.VISIBLE);
                        mTreeFileListView.setVisibility(TextView.GONE);

                        mTreeFileListView.setScrollingCacheEnabled(false);
                        mTreeFileListView.setScrollbarFadingEnabled(false);
                    }
                });
                createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE, mp, mDialogLocalDir, ntfy_file_list, false);
            }
        });
        //OK button
//		btnOk.setEnabled(false);
        btnOk.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String mp=mLocalMountPointSpinner.getSelectedItem().toString();
                if (mDialogSingleSelect) {
                    String[] sl_array=new String[3];
                    sl_array[0]=mp; //
                    sl_array[1]=mDialogLocalDir; //
                    if (mDialogSelectCat==DIALOG_SELECT_CATEGORY_DIRECTORY) sl_array[2]="";
                    else sl_array[2]=et_file_name.getText().toString(); //
                    slf4jLog.info("TreeFileListView ok button clicked, name="+mDialogLocalDir+"/"+sl_array[2]);
                    if (mNotifyEvent!=null) mNotifyEvent.notifyToListener(true, sl_array);
                } else {
                    slf4jLog.info("TreeFileListView ok button clicked");
                }
                mFragment.dismiss();
            }
        });
        // CANCELボタンの指定
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                slf4jLog.info("TreeFileListView cancel button clicked");
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
                        ArrayList<TreeFilelistItem> tfl =createLocalFilelist(mDialogSelectCat==DIALOG_SELECT_CATEGORY_FILE,turl,"");
                        if (tfl.size()<1)
                            tfl.add(new TreeFilelistItem(activity.getString(R.string.msgs_file_select_edit_dir_empty)));
                        mTreeFilelistAdapter.setDataList(tfl);
                        mTreeFilelistAdapter.notifyDataSetChanged();
                        if (turl.startsWith(mSafFileMgr.getSdcardRootPath())) {
                            if ((mSafFileMgr.getSdcardRootSafFile() == null) || mSafFileMgr.getSdcardRootPath().equals(SafManager.UNKNOWN_SDCARD_DIRECTORY)) {
                                btnCreate.setEnabled(false);
                            } else {
                                btnCreate.setEnabled(true);
                            }
                        } else if (turl.startsWith(mSafFileMgr.getUsbRootPath())) {
                            if ((mSafFileMgr.getUsbRootSafFile()==null) || mSafFileMgr.getUsbRootPath().equals(SafManager.UNKNOWN_USB_DIRECTORY)) {
                                btnCreate.setEnabled(false);
                            } else {
                                btnCreate.setEnabled(true);
                            }

                        } else btnCreate.setEnabled(true);
                        dir_path.setText(turl+"/");
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

    private void setTopUpButtonEnabled(boolean p) {
        final Button btnTop = (Button)mDialog.findViewById(R.id.common_file_selector_top_btn);
        final Button btnUp = (Button)mDialog.findViewById(R.id.common_file_selector_up_btn);

        btnUp.setEnabled(p);
        btnTop.setEnabled(p);
        if (p) {
            btnUp.setAlpha(1);
            btnTop.setAlpha(1);
        } else {
            btnUp.setAlpha(0.4f);
            btnTop.setAlpha(0.4f);
        }
    };

    private void putDlgMsg(TextView msg, String txt) {
        if (txt.equals("")) {
//            msg.setVisibility(TextView.GONE);
            msg.setText("");
        } else {
//            msg.setVisibility(TextView.VISIBLE);
            msg.setText(txt);
        }
    };

    public void showDialog(FragmentManager fm, Fragment frag, NotifyEvent ntfy) {
        slf4jLog.info("showDialog");
        mTerminateRequired=false;
        mNotifyEvent=ntfy;
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(frag,null);
        ft.commitAllowingStateLoss();
    };

    private void  createLocalFilelist(final boolean fileOnly, final String url, final String dir, final NotifyEvent ntfy, final boolean show_pd_circle_delay) {
        final Dialog pd=CommonDialog.showProgressSpinIndicator(getActivity());
        if (show_pd_circle_delay) {
            mUiHandler.post(new Runnable(){
                @Override
                public void run() {
                    pd.show();
                }
            });
        } else {
            pd.show();
        }
        Thread th=new Thread(){
            @Override
            public void run() {
                slf4jLog.info("createLocalFilelist Thread started");
                final ArrayList<TreeFilelistItem> tfl=createLocalFilelist(fileOnly, url, dir);
                slf4jLog.info("createLocalFilelist Thread ended");
                mUiHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        ntfy.notifyToListener(true, new Object[]{tfl});
                        pd.dismiss();
                    }
                });
            }
        };
        th.start();
    }

    private ArrayList<TreeFilelistItem>  createLocalFilelist(final boolean fileOnly, final String url, final String dir) {
        slf4jLog.info("createLocalFilelist entered, url="+url+", dir="+dir);
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
        if (ff!=null) {
            slf4jLog.info("createLocalFilelist list file size="+ff.length);
            for (int i=0;i<ff.length;i++){
                if (!ff[i].isHidden() || (ff[i].isHidden() && !mDialogHideHiddenDirsFiles)) {
                    if (ff[i].canRead()) {
                        int dirct=0;
                        if (ff[i].isDirectory()) {
                            File tlf=new File(lf.getPath()+"/"+ff[i].getName());
                            File[] lfl=tlf.listFiles();
                            if (lfl!=null) {
                                slf4jLog.info("createLocalFilelist sub dir="+tlf.getPath()+", count="+lfl.length);
                                for (int j=0;j<lfl.length;j++) {
                                    if (!fileOnly) {
                                        if (lfl[j].isDirectory()) dirct++;
                                    } else dirct++;
//									dirct++;
                                }
                            }
                        }
                        TreeFilelistItem tfi=buildTreeFileListItem(ff[i],fp);
                        tfi.setSubDirItemCount(dirct);
                        if (!fileOnly) {
                            if (ff[i].isDirectory()) tfl.add(tfi);
                        } else tfl.add(tfi);
                    }
                }
            }
            Collections.sort(tfl);
        }
        slf4jLog.info("createLocalFilelist ended, file list size="+tfl.size());
        return tfl;
    };

    private TreeFilelistItem buildTreeFileListItem(File fl, String fp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        String tfs= MiscUtil.convertFileSize(fl.length());
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
        dlg_msg.setVisibility(TextView.VISIBLE);
        final TextView dlg_cmp = (TextView) mCreateDirDialog.findViewById(R.id.single_item_input_name);
        final Button btnOk = (Button) mCreateDirDialog.findViewById(R.id.single_item_input_ok_btn);
        final Button btnCancel = (Button) mCreateDirDialog.findViewById(R.id.single_item_input_cancel_btn);
        final EditText etDir=(EditText) mCreateDirDialog.findViewById(R.id.single_item_input_dir);

        dlg_cmp.setText(context.getString(R.string.msgs_file_select_edit_parent_directory)+":"+c_dir);
        CommonDialog.setDlgBoxSizeCompact(mCreateDirDialog);
        etDir.setText(n_dir.replaceAll(c_dir, ""));
        setButtonEnabled(activity, btnOk, false);
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
                        setButtonEnabled(activity, btnOk, false);
//                        dlg_msg.setVisibility(TextView.VISIBLE);
                        dlg_msg.setText(context.getString(
                                R.string.msgs_single_item_input_dlg_duplicate_dir));
                    } else {
                        setButtonEnabled(activity, btnOk, true);
//                        dlg_msg.setVisibility(TextView.GONE);
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
                ntfy.setListener(new NotifyEvent.NotifyEventListener(){
                    @Override
                    public void positiveResponse(Context c, Object[] o) {
                        File lf= new File(n_path);
                        boolean rc_create=false;
                        if (c_dir.startsWith(mSafFileMgr.getSdcardRootPath()) && Build.VERSION.SDK_INT<=29) {
                            SafFile sf=mSafFileMgr.createSdcardItem(n_path, true);
                            if (sf==null) {
                                CommonDialog cd=new CommonDialog(context, getFragmentManager());
                                String error_msg="SafRoot="+mSafFileMgr.getSdcardRootSafFile()+"\n"+mSafFileMgr.getLastErrorMessage();
                                cd.showCommonDialog(false, "W", "SdcardSafFile cretae error", error_msg, null);
                                dlg_msg.setText("SafFile create Error");
                                slf4jLog.info("fileSelectEditDialogCreateBtn SdcardSafFile cretae error+\n"+error_msg);
                                return;
                            }
                            rc_create=sf.exists();
                        } else if (c_dir.startsWith(mSafFileMgr.getUsbRootPath()) && Build.VERSION.SDK_INT<=29) {
                            SafFile sf=mSafFileMgr.createUsbItem(n_path, true);
                            if (sf==null) {
                                CommonDialog cd=new CommonDialog(context, getFragmentManager());
                                String error_msg="SafRoot="+mSafFileMgr.getUsbRootSafFile()+"\n"+mSafFileMgr.getLastErrorMessage();
                                cd.showCommonDialog(false, "W", "UsbSafFile cretae error", error_msg, null);
                                dlg_msg.setText("SafFile create Error");
                                slf4jLog.info("fileSelectEditDialogCreateBtn UsbSafFile cretae error+\n"+error_msg);
                                return;
                            }
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
                                    p_dir+=sep+a_dir[i];
                                    sep="/";
                                }
                            }
                            slf4jLog.info("fileSelectEditDialogCreateBtn Directory cretaed name="+n_path);
                            mCreateDirDialog.dismiss();
//                            mCreateDirDialog=null;
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
//                mCreateDirDialog=null;
                p_ntfy.notifyToListener(false, null);
            }
        });
        mCreateDirDialog.show();
    };


}
