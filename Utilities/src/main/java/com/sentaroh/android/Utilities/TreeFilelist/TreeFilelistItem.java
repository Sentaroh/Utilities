package com.sentaroh.android.Utilities.TreeFilelist;

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

import java.io.Serializable;

import android.util.Log;


public class TreeFilelistItem 
		implements Serializable, Comparable<TreeFilelistItem>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String fileName;
	private String fileCap;
	private boolean isDir=false;
	private boolean isEncrypted=false;
	private long fileLength;
	private long lastModdate;
	private boolean isChecked=false;
	private boolean canRead=false;
	private boolean isHidden=false;
	private boolean canWrite=false;
	private String filePath;
	private boolean childListExpanded=false;
	private int listLevel=0;
	private boolean hideListItem=false;
	private boolean subDirLoaded=false;
	private int subDirItemCount=0;
	private boolean triState=false;
	private boolean enableItem=true;

	public void dump(String id) {
		String did=(id+"            ").substring(0,12);
		Log.v("TreeFileListItem",did+"FileName="+fileName+", Caption="+fileCap+", filePath="+filePath);
		Log.v("TreeFileListItem",did+"isDir="+isDir+", Length="+fileLength+
				", lastModdate="+lastModdate+", isChecked="+isChecked+
				", canRead="+canRead+",canWrite="+canWrite+", isHidden="+isHidden);
		Log.v("TreeFileListItem",did+"childListExpanded="+childListExpanded+
				", listLevel=="+listLevel+", hideListItem="+hideListItem+
				", subDirLoaded="+subDirLoaded+", subDirItemCount="+subDirItemCount+
				", triState="+triState+", enableItem="+enableItem);
	};
	
	public TreeFilelistItem(String fn){
		fileName = fn;
	}

	public TreeFilelistItem(String fn,String cp,
			boolean d, long fl,long lm, boolean ic, 
			boolean cr,boolean cw,boolean hd, String fp, int lvl)
	{
		fileName = fn;
		fileLength = fl;
		fileCap = cp;
		isDir=d;
		lastModdate=lm;
		isChecked =ic;
		canRead=cr;
		canWrite=cw;
		isHidden=hd;
		filePath=fp;
		listLevel=lvl;
	}
	public String getName(){return fileName;}
	public long getLength(){return fileLength;}
	public String getCap(){return fileCap;}
	public boolean isDir(){return isDir;}
	public long getLastModified(){return lastModdate;}
	public void setLastModified(long p){lastModdate=p;}
	public boolean isChecked(){return isChecked;}
	public void setChecked(boolean p){
		isChecked=p;
		if (p) triState=false;
	};
	public boolean canRead(){return canRead;}
	public boolean canWrite(){return canWrite;}
	public boolean isHidden(){return isHidden;}
	public String getPath(){return filePath;}
	public void setChildListExpanded(boolean p){childListExpanded=p;}
	public boolean isChildListExpanded(){return childListExpanded;}
	public void setListLevel(int p){listLevel=p;}
	public int getListLevel(){return listLevel;}
	public boolean isHideListItem(){return hideListItem;}
	public void setHideListItem(boolean p){hideListItem=p;}
	public void setSubDirItemCount(int p){subDirItemCount=p;}
	public int getSubDirItemCount(){return subDirItemCount;}
	public boolean isSubDirLoaded() {return subDirLoaded;}
	public void setSubDirLoaded(boolean p) {subDirLoaded=p;}
	public void setTriState(boolean p) {triState=p;}
	public boolean isTriState() {return triState;}
	public void setEnableItem(boolean p) {enableItem=p;}
	public boolean isEnableItem() {return enableItem;}
	
	public boolean isEncrypted() {return isEncrypted;}
	
//	public String toString() {
//		String result;
//		
//		if (hideListItem) result= "H";
//		else result= "V";
//		Log.v("","V="+result);
//		 
//		return result;
//	}

	
	@Override
	public int compareTo(TreeFilelistItem o) {
		if(this.fileName != null) {
			String cmp_c="F",cmp_n="F";
			if (isDir) cmp_c="D";
			if (o.isDir) cmp_n="D";
			cmp_c+=filePath;
			cmp_n+=o.getPath();
			if (!cmp_c.equals(cmp_n)) return cmp_c.compareToIgnoreCase(cmp_n);
			return fileName.compareToIgnoreCase(o.getName());
		} else 
			throw new IllegalArgumentException();
	}
}
