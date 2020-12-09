package com.sentaroh.android.Utilities.Preference;

/*
The MIT License (MIT)
Copyright (c) 2016 Sentaroh

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import com.sentaroh.android.Utilities.MiscUtil;
import com.sentaroh.android.Utilities.LocalMountPoint;
import com.sentaroh.android.Utilities.NotifyEvent;
import com.sentaroh.android.Utilities.NotifyEvent.NotifyEventListener;
import com.sentaroh.android.Utilities.R;
import com.sentaroh.android.Utilities.ThemeColorList;
import com.sentaroh.android.Utilities.ThemeUtil;
import com.sentaroh.android.Utilities.Dialog.CommonDialog;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistAdapter;
import com.sentaroh.android.Utilities.TreeFilelist.TreeFilelistItem;
import com.sentaroh.android.Utilities.Widget.CustomSpinnerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class DirectorySelectPreference extends DialogPreference{
	private final static boolean DEBUG_ENABLE=false;
	private final static String APPLICATION_TAG="DirectorySelectPreference";

    private boolean showMountpointSelector=false;
    public void setShowMountpointSelector(boolean show) {
        showMountpointSelector=show;
    }
    public boolean isShowMountpointSelector() {
        return showMountpointSelector;
    }

    private boolean showMountpointWritable=false;
    public void setShowMountpointWritable(boolean show) {
        showMountpointWritable=show;
    }
    public boolean isShowMountpointWritable() {
        return showMountpointWritable;
    }

    private boolean showRootDirectory=false;
    public void setShowRootDirectory(boolean show) {
        showRootDirectory=show;
    }
    public boolean isShowRootDirectory() {
        return showRootDirectory;
    }

    private boolean showReadOnlyMountPoint=false;
    public void setShowReadOnlyMountPoint(boolean show) {
        showReadOnlyMountPoint=show;
    }
    public boolean isShowReadOnlyMountPoint() {
        return showReadOnlyMountPoint;
    }

    public DirectorySelectPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"DirectorySelectPreference");
	}
 
	public DirectorySelectPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"DirectorySelectPreference style");
	}
 
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onGetDefaultValue");
		return a.getString(index);
	}

    @Override
    protected void onBindDialogView(View view) {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onBindDialogView");
    	super.onBindDialogView(view);
    }
    
    @Override
    public void onActivityDestroy() {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onActivityDestroy");
    	super.onActivityDestroy();
    };

    @Override
    protected Parcelable onSaveInstanceState() {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onSaveInstanceState");
        final Parcelable superState = super.onSaveInstanceState();
        final MySavedState myState = new MySavedState(superState);
        if (mTreeFilelistAdapter!=null) {
            myState.tree_file_list = (ArrayList<TreeFilelistItem>) mTreeFilelistAdapter.getDataList();
            myState.mount_point_selection=mLocalMountPointSpinner.getSelectedItemPosition();
            myState.tfl_pos=mTreeFileListView.getFirstVisiblePosition();
        }
        return myState;
    };

    private static class MySavedState extends BaseSavedState {
        public ArrayList<TreeFilelistItem> tree_file_list=null;
        public int mount_point_selection=0, tfl_pos=0;
        @SuppressWarnings("unchecked")
		public MySavedState(Parcel source) {
            super(source);
			try {
	            byte[] buf=null;
	            source.readByteArray(buf);
	            ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	            ObjectInputStream ois=new ObjectInputStream(bis);
	            tree_file_list=(ArrayList<TreeFilelistItem>) ois.readObject();
	            mount_point_selection=source.readInt();
	            tfl_pos=source.readInt();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            byte[] buf;
            ByteArrayOutputStream bos = new ByteArrayOutputStream(100000);
            try {
				ObjectOutputStream oos=new ObjectOutputStream(bos);
				oos.writeObject(tree_file_list);
				oos.close();
				buf=bos.toByteArray();
				dest.writeByteArray(buf);
				dest.writeInt(mount_point_selection);
				dest.writeInt(tfl_pos);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        public MySavedState(Parcelable superState) {
            super(superState);
        }
        @SuppressWarnings("unused")
		public static final Parcelable.Creator<MySavedState> CREATOR =
                new Parcelable.Creator<MySavedState>() {
            public MySavedState createFromParcel(Parcel in) {
                return new MySavedState(in);
            }
            public MySavedState[] newArray(int size) {
                return new MySavedState[size];
            }
        };
    };

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onRestoreInstanceState state="+state);
        if (state == null) {
            super.onRestoreInstanceState(state);
            return;
        }
        MySavedState myState = (MySavedState) state;
        
        mLocalMountPointSpinnerSelectedPos=myState.mount_point_selection;
        mTreeFilelistArrayList=myState.tree_file_list;
        mTreeFileListViewPos=myState.tfl_pos;
        
        super.onRestoreInstanceState(myState.getSuperState());
    };

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onSetInitialValue");
		if (restorePersistedValue) {
			mDialogDirName = getPersistedString(mDialogDirName);
		} else {
			mDialogDirName = (String) defaultValue;
			persistString(mDialogDirName);
		}
	};
 
	@Override
	protected View onCreateDialogView() {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onCreateDialogView");
		mDirectoryListView=initViewWidget();
		return mDirectoryListView;
	};
 
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"onDialogClosed positiveResult="+positiveResult);
		if (positiveResult) {
			EditText et_dir=(EditText)mDirectoryListView.findViewById(R.id.directory_select_preference_filename);
            String lmp=mLocalMountPointSpinner.getSelectedItem().toString();
            if (showRootDirectory)  persistString(et_dir.getText().toString());
            else persistString(lmp+et_dir.getText().toString());
		}
		super.onDialogClosed(positiveResult);
	};

	@Override
    protected void showDialog(Bundle state) {
		if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"showDialog");
		super.showDialog(state);
		CommonDialog.setDlgBoxSizeLimit(getDialog(), true);
	};

	@SuppressWarnings("deprecation")
	public static void setSpinnerBackground(Context c, Spinner spinner, boolean theme_is_light) {
		if (theme_is_light) spinner.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.spinner_color_background_light));
		else spinner.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.spinner_color_background));
	};

    private ListView mTreeFileListView=null;
    private int mTreeFileListViewPos=0;
    private TreeFilelistAdapter mTreeFilelistAdapter=null;
    private Spinner mLocalMountPointSpinner=null;
    private int mLocalMountPointSpinnerSelectedPos=0;
    private ArrayList<TreeFilelistItem> mTreeFilelistArrayList=null;
    private View mDirectoryListView=null;
	private String mDialogDirName = "";

    @SuppressLint("InflateParams")
	private View initViewWidget() {
    	if (DEBUG_ENABLE) Log.v(APPLICATION_TAG,"initViewWidget");
		final Context context=getContext();

		mDialogDirName = getPersistedString(mDialogDirName);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View file_select_view = inflater.inflate(R.layout.directory_select_preference, null);
        mDirectoryListView=file_select_view;

        ThemeColorList tcl=ThemeUtil.getThemeColorList(context);
//		LinearLayout ll_dlg_view=(LinearLayout) mDirectoryListView.findViewById(R.id.directory_select_preference_view);
//		ll_dlg_view.setBackgroundColor(tcl.dialog_msg_background_color);

		mLocalMountPointSpinner=(Spinner) file_select_view.findViewById(R.id.directory_select_preference_rdir);
		setSpinnerBackground(context, mLocalMountPointSpinner, ThemeUtil.isLightThemeUsed(context));
		mLocalMountPointSpinner.setVisibility(Spinner.VISIBLE);
		//	Root directory spinner
	    CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(context, android.R.layout.simple_spinner_item);
	//    CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(context, R.layout.custom_simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
	    mLocalMountPointSpinner.setPrompt(
	    		context.getString(R.string.msgs_file_select_edit_local_mount_point));
	    mLocalMountPointSpinner.setAdapter(adapter);
	//    adapter.setTextColor(Color.BLACK);
	
	    mLocalMountPointSpinner.setOnItemSelectedListener(null);
//        File[] ext_dirs = ContextCompat.getExternalFilesDirs(context, null);
        File[] ext_dirs = context.getExternalFilesDirs(null);
        int sel_no=0;
        if (ext_dirs==null) {
            adapter.add("/mnt/sdcard");
            mLocalMountPointSpinner.setEnabled(false);
        } else {
            for (File item:ext_dirs) {
                if (item!=null) {
                    String path=item.getPath().substring(0, item.getPath().indexOf("/Android/data/"));
                    if (showMountpointWritable) {
                        File lf=new File(path+"/work_temp.tmp");
                        try {
                            lf.createNewFile();
                            if (lf.exists()) adapter.add(path);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            lf.delete();
                        }
                    } else {
                        adapter.add(path);
                    }
                }
            }
        }
        if (adapter.getCount()==0) adapter.add("/mnt/sdcard");
	    mLocalMountPointSpinner.setOnItemSelectedListener(null);
	    mLocalMountPointSpinner.setSelection(mLocalMountPointSpinnerSelectedPos);
        if (showMountpointSelector) mLocalMountPointSpinner.setVisibility(Spinner.VISIBLE);
        else mLocalMountPointSpinner.setVisibility(Spinner.GONE);
//        if (adapter.getCount()>=2) mLocalMountPointSpinner.setEnabled(true);
//        else mLocalMountPointSpinner.setEnabled(false);
	    
	//	final TextView v_spacer=(TextView)mDialog.findViewById(R.id.file_select_edit_dlg_spacer);
		mTreeFileListView = (ListView) file_select_view.findViewById(android.R.id.list);
		final EditText filename = (EditText) file_select_view.findViewById(R.id.directory_select_preference_filename);
	//    if (dirs.size()<=2)	v_spacer.setVisibility(TextView.VISIBLE);
		
		mTreeFilelistAdapter= new TreeFilelistAdapter(context,true,true,false);
		
	    mTreeFileListView.setAdapter(mTreeFilelistAdapter);
	    String lmp=mLocalMountPointSpinner.getSelectedItem().toString();
	    if (mTreeFilelistArrayList==null) mTreeFilelistArrayList =createLocalFilelist(true,lmp,"");
        if (mTreeFilelistArrayList.size()==0) {
        	mTreeFilelistArrayList.add(new TreeFilelistItem(context.getString(R.string.msgs_file_select_edit_dir_empty)));
        } else {
	        mTreeFilelistAdapter.setDataList(mTreeFilelistArrayList);
	        mTreeFilelistArrayList=null;
    		
    		String sel_dir=mDialogDirName.replace(lmp,"");
    		String n_dir="", e_dir="";
    		if (sel_dir.startsWith("/")) n_dir=sel_dir.substring(1);
    		else n_dir=sel_dir;
    		if (n_dir.endsWith("/")) e_dir=n_dir.substring(0,n_dir.length()-1);
    		else e_dir=n_dir;
    		Log.v("","mp="+lmp+", dir="+mDialogDirName+", sel="+e_dir);
    		selectLocalDirTree(e_dir);
        }
	    mTreeFileListView.setScrollingCacheEnabled(false);
	    mTreeFileListView.setScrollbarFadingEnabled(false);
//	    mTreeFileListView.setFastScrollEnabled(true);
	    mTreeFileListView.setSelection(mTreeFileListViewPos);

	    if (showRootDirectory) filename.setText(mDialogDirName);
        else filename.setText(mDialogDirName.replace(lmp,""));
		filename.setSelection(filename.getText().toString().length());

//		CommonDialog.setDlgBoxSizeLimit(file_select_view,true);
	//	setDlgBoxSize(dialog,0,0,false);
	
		NotifyEvent cb_ntfy=new NotifyEvent(context);
		// set file list thread response listener 
		cb_ntfy.setListener(new NotifyEventListener() {
			@Override
			public void positiveResponse(Context c,Object[] o) {
				int p=(Integer) o[0];
				boolean p_chk=(Boolean) o[1];
				if (mTreeFilelistAdapter.getDataItem(p).isChecked() && !p_chk) {
					String turl=(String) mLocalMountPointSpinner.getSelectedItem();
					if (p!=-1) {
						if (mTreeFilelistAdapter.getDataItem(p).isChecked()) {
                            if (showRootDirectory) {
                                filename.setText((turl+mTreeFilelistAdapter.getDataItem(p).getPath()+
                                        mTreeFilelistAdapter.getDataItem(p).getName()).replaceAll("//","/"));
                            } else {
                                filename.setText((mTreeFilelistAdapter.getDataItem(p).getPath()+
                                        mTreeFilelistAdapter.getDataItem(p).getName()).replaceAll("//","/"));
                            }
							filename.setSelection(filename.getText().toString().length());
						}
					} else {
                        if (showRootDirectory) filename.setText((turl+mTreeFilelistAdapter.getDataItem(0).getPath()).replaceAll("//","/"));
                        else filename.setText((mTreeFilelistAdapter.getDataItem(0).getPath()).replaceAll("//","/"));
						filename.setSelection(filename.getText().toString().length());
					}
				}
			}
			@Override
			public void negativeResponse(Context c,Object[] o) {
			}
		});
		mTreeFilelistAdapter.setCbCheckListener(cb_ntfy);
		
        NotifyEvent ntfy_expand_close=new NotifyEvent(context);
        ntfy_expand_close.setListener(new NotifyEventListener(){
			@Override
			public void positiveResponse(Context c, Object[] o) {
				int idx=(Integer)o[0];
	    		final int pos=mTreeFilelistAdapter.getItem(idx);
	    		final TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(pos);
				if (tfi.getName().startsWith("---")) return;
				String turl=(String) mLocalMountPointSpinner.getSelectedItem();
				processLocalDirTree(true,turl, pos,tfi,mTreeFilelistAdapter);
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
//				mTreeFilelistAdapter.setDataItemIsSelected(pos);
//				mTreeFilelistAdapter.notifyDataSetChanged(); 
				if (tfi.getName().startsWith("---")) return;
				String turl=(String) mLocalMountPointSpinner.getSelectedItem();
				processLocalDirTree(true,turl, pos,tfi,mTreeFilelistAdapter);
			}
        });
//		
//		mTreeFileListView.setOnItemClickListener(new OnItemClickListener() {
//			public void onItemClick(AdapterView<?> items, View view, int idx,long id) {
//				// リストアイテムを選択したときの処理
//	    		final int pos=mTreeFilelistAdapter.getItem(idx);
//	    		final TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(pos);
//				if (tfi.getName().startsWith("---")) return;
//				String turl=(String) mLocalMountPointSpinner.getSelectedItem();
//				processLocalDirTree(true,turl, pos,tfi,mTreeFilelistAdapter);
//			}
//		});
		mTreeFileListView.setOnItemLongClickListener(new OnItemLongClickListener(){
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int position, long arg3) {
//	  			final int t_pos=mTreeFilelistAdapter.getItem(position);
//	    		final TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(t_pos);
//				if (tfi.getName().startsWith("---")) return true;
//	  			mTreeFilelistAdapter.setDataItemIsSelected(t_pos);
//	  			mTreeFilelistAdapter.notifyDataSetChanged();
//	  			
//				String turl=(String) mLocalMountPointSpinner.getSelectedItem();
//				filename.setText((turl+tfi.getPath()+tfi.getName()+"/").replaceAll("//","/"));
//				filename.setSelection(filename.getText().toString().length());
//	  			
				return true;
			}
		});
//		//Create button
//		btnCreate.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				NotifyEvent ntfy=new NotifyEvent(context);
//				// set file list thread response listener 
//				ntfy.setListener(new ListenerInterface() {
//					@Override
//					public void eventPositiveResponse(Context c,Object[] o) {
//					}
//					@Override
//					public void eventNegativeResponse(Context c,Object[] o) {}
//	
//				});
//				fileSelectEditDialogCreateBtn(activity,mDialogLocalUrl,
//						mDialogLocalDir, mTreeFilelistAdapter, ntfy,mTreeFileListView);
//			}
//		});
	    Handler hndl=new Handler();
	    hndl.postDelayed(new Runnable(){
			@Override
			public void run() {
			    mLocalMountPointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			        @Override
			        public void onItemSelected(AdapterView<?> parent, View view,
			                int position, long id) {
			        	Log.v("","item selected="+position);
			            Spinner spinner = (Spinner) parent;
						String turl=(String) spinner.getSelectedItem();
			
						ArrayList<TreeFilelistItem> tfl = createLocalFilelist(true,turl,"");
				        if (tfl.size()<1) 
				        	tfl.add(new TreeFilelistItem(context.getString(R.string.msgs_file_select_edit_dir_empty)));
				        mTreeFilelistAdapter.setDataList(tfl);
				        mTreeFilelistAdapter.createShowList();
				        mTreeFileListView.setSelection(0);
			        }
			        @Override
			        public void onNothingSelected(AdapterView<?> arg0) {}
			    });
			}
	    },100);
		return file_select_view;
    };
    
    private void selectLocalDirTree(String sel_dir) {
		String[] a_dir=sel_dir.split("/");
    	for (int i=0;i<mTreeFilelistAdapter.getDataItemCount();i++) {
    		TreeFilelistItem tfi=mTreeFilelistAdapter.getDataItem(i);
//        	Log.v("","name="+tfi.getName()+", c="+sel_dir);
        	if (a_dir!=null) {
        		if (tfi.getName().equals(a_dir[0])) {
            		if (a_dir.length>1) {
        				String turl=(String) mLocalMountPointSpinner.getSelectedItem();
        				processLocalDirTree(true,turl, i,tfi,mTreeFilelistAdapter);
        				selectLocalDirTree(sel_dir.replace(a_dir[0]+"/", ""));
            		} else {
            			mTreeFileListViewPos=i;
            			mTreeFilelistAdapter.setDataItemIsSelected(i);
            		}
        			break;
        		}
        	}
    	}
    };
    
	private void processLocalDirTree (boolean dironly,String lclurl, final int pos, 
			final TreeFilelistItem tfi, final TreeFilelistAdapter tfa) {
		if (tfi.getSubDirItemCount()==0) return;
		if(tfi.isChildListExpanded()) {
			tfa.hideChildItem(tfi,pos);
		} else {
			if (tfi.isSubDirLoaded()) 
				tfa.reshowChildItem(tfi,pos);
			else {
				if (tfi.isSubDirLoaded()) tfa.reshowChildItem(tfi,pos);
				else {
					ArrayList<TreeFilelistItem> ntfl =
							createLocalFilelist(dironly,lclurl,tfi.getPath()+tfi.getName());
					tfa.addChildItem(tfi,ntfl,pos);
				}
			}
		}
	};

	private ArrayList<TreeFilelistItem>  createLocalFilelist(boolean dironly, 
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
				if (ff[i].canRead()) {
					int dirct=0;
					if (ff[i].isDirectory()) {
						File tlf=new File(url+tdir+"/"+ff[i].getName());
						File[] lfl=tlf.listFiles();
						if (lfl!=null) {
							for (int j=0;j<lfl.length;j++) {
								if (dironly) {
									if (lfl[j].isDirectory()) dirct++;
								} else dirct++;
							}
						}
					}
					tfi=buildTreeFileListItem(ff[i],fp);
					tfi.setSubDirItemCount(dirct);
					if (dironly) {
						if (ff[i].isDirectory()) tfl.add(tfi);
					} else tfl.add(tfi);
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
					fl.isHidden(), fp, 0);
		} else {
			tfi=new TreeFilelistItem(fl.getName(), sdf.format(fl
					.lastModified())+","+tfs, false, fl.length(), fl
					.lastModified(),false,
					fl.canRead(),fl.canWrite(),
					fl.isHidden(), fp, 0);
		}
//		TreeFilelistItem tfi=new TreeFilelistItem(fl.getName(),
//				""+", ", fl.isDirectory(), 0,0,false,
//				fl.canRead(),fl.canWrite(),
//				fl.isHidden(),fp,0);
		return tfi;
	};
	
}
