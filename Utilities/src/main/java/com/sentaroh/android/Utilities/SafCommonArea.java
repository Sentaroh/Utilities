package com.sentaroh.android.Utilities;

import java.util.ArrayList;

import android.content.Context;

public class SafCommonArea {
//	private SharedPreferences prefs=null;
	public boolean debug_enabled=false;
	public Context context=null;
	public String pkg_name="";
	public String app_spec_dir="";
	public SafFile rootDocumentFile=null;
	public ArrayList<String> external_sdcard_dir_list=new ArrayList<String>();
}
