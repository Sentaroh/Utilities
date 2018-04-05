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

import com.sentaroh.android.Utilities.NotifyEvent;
import android.app.Dialog;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.ViewGroup.LayoutParams;

public class CommonDialog {
	private FragmentManager mFragMgr =null;
	
	public CommonDialog(Context c, FragmentManager fm) {
		mFragMgr =fm;
	};
	
	public void showCommonDialog(
			final boolean negative, String type, String title, String msgtext,
			final NotifyEvent ntfy) {
        MessageDialogFragment cdf =MessageDialogFragment.newInstance(
        		negative, type, title, msgtext);
        cdf.showDialog(mFragMgr,cdf,ntfy);
	};
	
	@SuppressWarnings("deprecation")
	static public void setDlgBoxSizeCompact(Dialog dlg) {
		if (dlg==null) return;
		int w=dlg.getWindow().getWindowManager().getDefaultDisplay().getWidth();
		int h=dlg.getWindow().getWindowManager().getDefaultDisplay().getHeight();
		int nw=0;
		
		if (w>h) {//Landscape
//			Log.v("","Landscape");
			if (w>800) {
				if (w>=1200) nw=(w/3)*2;
				else nw=800;
			} else nw=LayoutParams.FILL_PARENT;
		} else {//Portrait
//			Log.v("","Portlait");
			nw=LayoutParams.FILL_PARENT;
		}
		dlg.getWindow().setLayout(nw, LayoutParams.WRAP_CONTENT);
//		Log.v("","w="+w+", h="+h+", nw="+nw);
		
	};
	
	@SuppressWarnings("deprecation")
	static public void setDlgBoxSizeLimit(Dialog dlg,boolean set_max) {
		if (dlg==null) return;
//		int w=dlg.getWindow().getWindowManager().getDefaultDisplay().getWidth();
//		int h=dlg.getWindow().getWindowManager().getDefaultDisplay().getHeight();
		if (!set_max) {// W=fill_parent H=fill_parent
			setDlgBoxSizeCompact(dlg);
		} else {// W=fill_parent H=wrap_content
			dlg.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		}
	};
	
	@SuppressWarnings("deprecation")
	static public void setDlgBoxSizeHeightMax(Dialog dlg) {
		if (dlg==null) return;
			dlg.getWindow().setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
	};

//	public void fileSelectWithCreate(String lurl, String ldir, 
//			String file_name,String dlg_title, NotifyEvent ntfy) {
//		fileSelect(true,false,false,lurl,ldir,file_name,dlg_title,ntfy);
//	};
//	public void fileSelectWithCreateMP(String lurl, String ldir, 
//			String file_name,String dlg_title, NotifyEvent ntfy) {
//		fileSelect(true,false,true,lurl,ldir,file_name,dlg_title,ntfy);
//	};
//	public void fileSelectWithoutCreate(String lurl, String ldir, 
//			String file_name,String dlg_title, NotifyEvent ntfy) {
//		fileSelect(false,false,false,lurl,ldir,file_name,dlg_title,ntfy);
//	};
//	public void fileSelectWithoutCreateMP(String lurl, String ldir, 
//			String file_name,String dlg_title, NotifyEvent ntfy) {
//		fileSelect(false,false,true,lurl,ldir,file_name,dlg_title,ntfy);
//	};
	public void fileOnlySelectWithCreate(String lurl, String ldir, 
			String file_name,String dlg_title, NotifyEvent ntfy) {
		fileSelect(true,true,false,lurl,ldir,file_name,dlg_title,ntfy);
	};
	public void fileOnlySelectWithCreateLimitMP(String lurl, String ldir, 
			String file_name,String dlg_title, NotifyEvent ntfy) {
		FileSelectDialogFragment fsdf=
				FileSelectDialogFragment.newInstance(false, true, true, false, false, true, true, lurl, ldir, 
					file_name, dlg_title);
		fsdf.showDialog(mFragMgr, fsdf, ntfy);
	};
	public void fileOnlySelectWithCreateHideMP(String lurl, String ldir, 
			String file_name,String dlg_title, NotifyEvent ntfy) {
		fileSelect(true,true,true,lurl,ldir,file_name,dlg_title,ntfy);
	};
	public void fileOnlySelectWithoutCreate(String lurl, String ldir, 
			String file_name,String dlg_title, NotifyEvent ntfy) {
		fileSelect(false,true,false,lurl,ldir,file_name,dlg_title,ntfy);
	};
	public void fileOnlySelectWithoutCreateHideMP(String lurl, String ldir, 
			String file_name,String dlg_title, NotifyEvent ntfy) {
		fileSelect(false,true,true,lurl,ldir,file_name,dlg_title,ntfy);
	};
//	public void fileSelect(boolean enableCreate,final String lurl, final String ldir, 
//			String file_name,String dlg_title, final NotifyEvent ntfy) {
//		fileSelect(false,false,false,lurl,ldir,file_name,dlg_title,ntfy);
//	}
//	public void fileSelectHideMP(boolean enableCreate,final String lurl, final String ldir, 
//			String file_name,String dlg_title, final NotifyEvent ntfy) {
//		fileSelect(false,false,true,lurl,ldir,file_name,dlg_title,ntfy);
//	}
	public void fileSelect(boolean enableCreate,boolean fileOnly, boolean hideMp, final String lurl, 
			final String ldir, String file_name,String dlg_title, final NotifyEvent ntfy) {
		
		boolean include_root=false;
		FileSelectDialogFragment fsdf=
				FileSelectDialogFragment.newInstance(false, enableCreate, fileOnly, hideMp, include_root, 
						true, lurl, ldir, file_name, dlg_title);
		fsdf.showDialog(mFragMgr, fsdf, ntfy);
	}


    public void fileSelectorFileOnlySelectWithCreate(Boolean inc_mp, String mount_point, String dir_name, String file_name, String title, NotifyEvent ntfy) {
        boolean include_root=false;
        CommonFileSelector fsdf=
                CommonFileSelector.newInstance(false, true, true, CommonFileSelector.DIALOG_SELECT_CATEGORY_FILE,
                        true, inc_mp, mount_point, dir_name, file_name, title);
        fsdf.showDialog(mFragMgr, fsdf, ntfy);
    };

    public void fileSelectorDirOnlySelectWithCreate(Boolean inc_mp, String mount_point, String dir_name, String title, NotifyEvent ntfy) {
        CommonFileSelector fsdf=
                CommonFileSelector.newInstance(false, true, false, CommonFileSelector.DIALOG_SELECT_CATEGORY_DIRECTORY,
                        true, inc_mp, mount_point, dir_name, "", title);
        fsdf.showDialog(mFragMgr, fsdf, ntfy);
    };

    public void fileSelectorFileOnlySelectWithCreateHideMP(Boolean inc_mp, String mount_point, String dir_name, String file_name, String title, NotifyEvent ntfy) {
        CommonFileSelector fsdf=
                CommonFileSelector.newInstance(false, true, true, CommonFileSelector.DIALOG_SELECT_CATEGORY_FILE,
                        true, inc_mp, mount_point, dir_name, file_name, title);
        fsdf.showDialog(mFragMgr, fsdf, ntfy);
    };

    public void fileSelectorDirOnlySelectWithCreateHideMP(Boolean inc_mp, String mount_point, String dir_name, String title, NotifyEvent ntfy) {
        CommonFileSelector fsdf=
                CommonFileSelector.newInstance(false, true, false, CommonFileSelector.DIALOG_SELECT_CATEGORY_DIRECTORY,
                        true, inc_mp, mount_point, dir_name, "", title);
        fsdf.showDialog(mFragMgr, fsdf, ntfy);
    };

}
