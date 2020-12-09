package com.sentaroh.android.Utilities.LogUtil;

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

import android.content.Context;
import android.util.Log;

import com.sentaroh.android.Utilities.CommonGlobalParms;
import com.sentaroh.android.Utilities.MiscUtil;
import com.sentaroh.android.Utilities.StringUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CommonLogUtil {
	private Context mContext=null;
	
	private CommonGlobalParms mGp=null;

	private String mLogIdent="";
	
	public CommonLogUtil(Context c, String li, CommonGlobalParms gp) {
		mContext=c;
		setLogId(li);
		mGp=gp;
	};

	final public void setLogId(String li) {
		mLogIdent=(li+"                 ").substring(0,16)+" ";
	};

    final public String getLogId() {
        return mLogIdent;
    };

    final public void closeLog() {
		closeLog(mContext, mGp);
	};

	final public static void closeLog(Context c, CommonGlobalParms mGp) {
        CommonLogWriter.enqueue(mGp, c, mGp.getLogIntentClose(), "", true);
	};

	final public void resetLogReceiver() {
		resetLogReceiver(mContext, mGp);
	};

	final public static void resetLogReceiver(Context c, CommonGlobalParms mGp) {
        CommonLogWriter.enqueue(mGp, c, mGp.getLogIntentReset(), "",  true);
	};

	final public void flushLog() {
		flushLog(mContext, mGp);
	};

	final public static void flushLog(Context c, CommonGlobalParms mGp) {
        CommonLogWriter.enqueue(mGp, c, mGp.getLogIntentFlush(), "", true);
	};

	final public void rotateLogFile() {
		rotateLogFile(mContext, mGp);
	};

	final public static void rotateLogFile(Context c, CommonGlobalParms mGp) {
        CommonLogWriter.enqueue(mGp, c, mGp.getLogIntentRotate(), "", true);
	};

    final public void deleteLogFile() {
        deleteLogFile(mContext, mGp);
	};

    final public static void deleteLogFile(Context c, CommonGlobalParms mGp) {
        CommonLogWriter.enqueue(mGp, c, mGp.getLogIntentDelete(), "", true);
    };

    final public void addLogMsg(String cat, String... msg) {
//		Log.v("","lvl="+mGp.getDebugLevel()+", ena="+mGp.isLogEnabled());
		if (mGp.getDebugLevel()>0 || mGp.isLogEnabled() || cat.equals("E")) {
			addLogMsg(mGp, mContext, mLogIdent, cat, msg);
		}
	};

	final public void addDebugMsg(int lvl, String cat, String... msg) {
		if (mGp.getDebugLevel()>=lvl ) {
			addDebugMsg(mGp, mContext, mLogIdent, lvl, cat, msg);
		}
	};

	final public String buildLogCatMsg(String log_id, String cat, String... msg) {
		StringBuilder log_msg=new StringBuilder(512);
		for (int i=0;i<msg.length;i++) log_msg.append(msg[i]);
		return cat+" "+log_id+log_msg.toString();
	};

	final public String buildPrintLogMsg(String cat, String... msg) {
		return buildPrintLogMsg(mLogIdent, cat, msg);
	};
	final public String buildPrintLogMsg(String log_id, String cat, String... msg) {
		StringBuilder log_msg=new StringBuilder(512);
		for (int i=0;i<msg.length;i++) log_msg.append(msg[i]);
		StringBuilder print_msg=new StringBuilder(512);
		print_msg
		.append("M ")
		.append(cat)
		.append(" ")
		.append(StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis()))
		.append(" ")
		.append(log_id)
		.append(log_msg.toString());
		return print_msg.toString();
	};

	final static private void addLogMsg(CommonGlobalParms gp, 
		Context context, String log_id, String cat, String... msg) {
		StringBuilder log_msg=new StringBuilder(512);
		for (int i=0;i<msg.length;i++) log_msg.append(msg[i]);
		if (gp.isLogEnabled()) {
			StringBuilder print_msg=new StringBuilder(512);
			print_msg
			.append("M ")
			.append(cat)
			.append(" ")
			.append(StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis()))
			.append(" ")
			.append(log_id)
			.append(log_msg.toString());
            CommonLogWriter.enqueue(gp, context, gp.getLogIntentSend(), print_msg.toString(), false);
		}
		if (gp.isLogcatEnabled()) Log.v(gp.getApplicationTag(),cat+" "+log_id+log_msg.toString());
	};

	final static private void addDebugMsg(CommonGlobalParms gp,
		Context context, String log_id, int lvl, String cat, String... msg) {
		StringBuilder print_msg=new StringBuilder(512);
			print_msg.append("D ");
			print_msg.append(cat);
		StringBuilder log_msg=new StringBuilder(512);
		for (int i=0;i<msg.length;i++) log_msg.append(msg[i]);
		if (gp.isLogEnabled()) {
			print_msg.append(" ")
			.append(StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis()))
			.append(" ")
			.append(log_id)
			.append(log_msg.toString());
            CommonLogWriter.enqueue(gp, context, gp.getLogIntentSend(), print_msg.toString(), false);
		}
		if (gp.isLogcatEnabled()) Log.v(gp.getApplicationTag(), cat+" "+log_id+log_msg.toString());
	};

	final public boolean isLogFileExists() {
		boolean result = false;
		result=isLogFileExists(mGp);
		if (mGp.getDebugLevel()>=3) addDebugMsg(3,"I","Log file exists="+result);
		return result;
	};

	final static public boolean isLogFileExists(CommonGlobalParms cgp) {
		boolean result = false;
		File lf = new File(getLogFilePath(cgp));
		result=lf.exists();
		return result;
	};

//	final public boolean getSettingsLogOption() {
//		boolean result = false;
////		result=getPrefMgr().getBoolean(mContext.getString(R.string.settings_main_log_option), false);
//		if (mGlblParms.getDebugLevel()>=3) addDebugMsg(3,"I","LogOption="+result);
//		return result;
//	};
//
//	final public boolean setSettingsLogOption(boolean enabled) {
//		boolean result = false;
////		getPrefMgr().edit().putBoolean(mContext.getString(R.string.settings_main_log_option), enabled).commit();
//		if (mGlblParms.getDebugLevel()>=3) addDebugMsg(3,"I","setLLogOption="+result);
//		return result;
//	};
//
	final public String getLogFilePath() {
		return getLogFilePath(mGp);
	};
	final static public String getLogFilePath(CommonGlobalParms cgp) {
		return cgp.getLogDirName()+"/"+cgp.getLogFileName()+".txt";
	};
	
    final static public ArrayList<CommonLogFileListItem> createLogFileList(CommonGlobalParms gp) {
    	ArrayList<CommonLogFileListItem> lfm_fl=new ArrayList<CommonLogFileListItem>();
    	
    	File lf=new File(gp.getLogDirName());
    	File[] file_list=lf.listFiles();
    	if (file_list!=null) {
    		for (int i=0;i<file_list.length;i++) {
    			if (file_list[i].getName().startsWith(gp.getLogFileName())) {
//    				Log.v("","fn="+file_list[i].getName());
    				if (file_list[i].getName().startsWith(gp.getLogFileName()+"_20")) {
        		    	CommonLogFileListItem t=new CommonLogFileListItem();
        		    	t.log_file_name=file_list[i].getName();
        		    	t.log_file_path=file_list[i].getPath();
        		    	t.log_file_size=MiscUtil.convertFileSize(file_list[i].length());
        		    	t.log_file_last_modified=file_list[i].lastModified();
        		    	String lm_date=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(file_list[i].lastModified());
//        		    	if (file_list[i].getPath().equals(gp.getLogDirName()+gp.getLogFileName()+".txt"))
//        		    		t.isCurrentLogFile=true;
        		    	t.log_file_last_modified_date=lm_date.substring(0,10);
        		    	t.log_file_last_modified_time=lm_date.substring(11);
        		    	lfm_fl.add(t);
//    				} else if (file_list[i].getName().equals(gp.getLogFileName()+".txt")){
//        		    	CommonLogFileListItem t=new CommonLogFileListItem();
//        		    	t.log_file_name=file_list[i].getName();
//        		    	t.log_file_path=file_list[i].getPath();
//        		    	t.log_file_size=MiscUtil.convertFileSize(file_list[i].length());
//        		    	t.log_file_last_modified=file_list[i].lastModified();
//        		    	if (file_list[i].getPath().equals(gp.getLogDirName()+gp.getLogFileName()+".txt"))
//        		    		t.isCurrentLogFile=true;
//        		    	String lm_date=StringUtil.convDateTimeTo_YearMonthDayHourMinSec(file_list[i].lastModified());
//        		    	t.log_file_last_modified_date=lm_date.substring(0,10);
//        		    	t.log_file_last_modified_time=lm_date.substring(11);
//        		    	lfm_fl.add(t);
    				}
    			}
    		}
    		Collections.sort(lfm_fl,new Comparator<CommonLogFileListItem>(){
				@Override
				public int compare(CommonLogFileListItem arg0,
						CommonLogFileListItem arg1) {
					int result=0;
					long comp=arg1.log_file_last_modified-arg0.log_file_last_modified;
					if (comp==0) result=0;
					else if(comp<0) result=-1;
					else if(comp>0) result=1;
					return result;
				}
    			
    		});
    	}
    	if (lfm_fl.size()==0) {
    		CommonLogFileListItem t=new CommonLogFileListItem();
    		lfm_fl.add(t);
    	}
    	return lfm_fl;
    };

}
