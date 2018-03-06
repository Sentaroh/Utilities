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

import static com.sentaroh.android.Utilities.LogUtil.CommonLogConstants.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import com.sentaroh.android.Utilities.CommonGlobalParms;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

@SuppressLint("SdCardPath")
public class CommonLogReceiver extends BroadcastReceiver{

	private static PrintWriter printWriter=null;
//	private static BufferedWriter bufferedWriter;
	private static FileWriter fileWriter ;	
	private static String log_dir=null;
	private static int debug_level=1;
	private static File logFile=null;
	private static boolean shutdown_received=false;
	private static boolean log_enabled=true;
	
	private static final SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.getDefault());

	private static CommonGlobalParms mGp=null;
	
	private static String log_id="";
	
	@Override
	public void onReceive(Context c, Intent in) {
//		StrictMode.allowThreadDiskWrites();
//		StrictMode.allowThreadDiskReads();
		if (log_dir==null) {
			setLogId("LogReceiver");
			initParms(c);
			if (debug_level>0) {
				String line="initialized dir="+log_dir+", debug="+debug_level+", logEnabled="+log_enabled;
				Log.v(mGp.getApplicationTag(),"I "+log_id+line);
				putLogMsg(c,"M I "+sdfDateTime.format(System.currentTimeMillis())+" "+log_id+line);
			}
		}
		if (in.getAction().equals(Intent.ACTION_SHUTDOWN)) {
			if (printWriter!=null) {
				printWriter.flush();
				shutdown_received=true;
			}
		} else if (in.getAction().equals(mGp.getLogIntentSend())) {
			String line=in.getExtras().getString("LOG");
			putLogMsg(c,line);
//			if (printWriter!=null) {
//			}
		} else if (in.getAction().equals(mGp.getLogIntentClose())) {
			if (printWriter!=null) {
				printWriter.flush();
				closeLogFile();
			}
		} else if (in.getAction().equals(mGp.getLogIntentReset())) {
			initParms(c);
			closeLogFile();
			if (log_enabled) {
				openLogFile(c);
				if (debug_level>0) {
					String line="re-initialized dir="+log_dir+", debug="+debug_level+", log_enabled="+log_enabled;
					Log.v(mGp.getApplicationTag(),"I "+log_id+line);
					putLogMsg(c,"M I "+sdfDateTime.format(System.currentTimeMillis())+" "+log_id+line);
				}
			}else {
				rotateLogFileForce(c);
			}
		} else if (in.getAction().equals(mGp.getLogIntentDelete())) {
			if (printWriter!=null) {
				closeLogFile();
				logFile.delete();
			}
		} else if (in.getAction().equals(mGp.getLogIntentRotate())) {
			rotateLogFileForce(c);
		} else if (in.getAction().equals(mGp.getLogIntentFlush())) {
			if (printWriter!=null) printWriter.flush();
		}
//		StrictMode.enableDefaults();
	};

	private void setLogId(String li) {
		log_id=(li+"                 ").substring(0,16)+" ";
	};

	private void putLogMsg(Context c,String msg) {
//		Log.v("","log_option="+log_option+", mu="+mediaUsable+", pw="+printWriter);
		rotateLogFileConditional(c);
		if (printWriter==null) {
			openLogFile(c);
			if (printWriter!=null) {
				printWriter.println(msg);
				if (shutdown_received) printWriter.flush();
			}
		} else {
			printWriter.println(msg);
			if (shutdown_received) printWriter.flush();
		}
	}
	
	@SuppressLint("InlinedApi")
	private void initParms(Context context) {
		mGp=new CommonGlobalParms();
		setLogParms(context, mGp);
		log_dir=mGp.getLogDirName()+"/";
		debug_level=mGp.getDebugLevel();
		log_enabled=mGp.isLogEnabled();
		logFile=new File(log_dir+mGp.getLogFileName()+".txt");
	};
	
	public void setLogParms(Context c, CommonGlobalParms gp) {
		
	};
	
	private void rotateLogFileConditional(Context c) {
		if (printWriter!=null && logFile.length()>=mGp.getLogLimitSize()) {
			rotateLogFileForce(c);
		}
	};

	@SuppressLint("SimpleDateFormat")
	private void rotateLogFileForce(Context c) {
		if (printWriter!=null) {
			printWriter.flush();
			closeLogFile();
			SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
			File lf=new File(log_dir+mGp.getLogFileName()+"_"+sdf.format(System.currentTimeMillis())+".txt");
			logFile.renameTo(lf);
			openLogFile(c);
			logFile=new File(log_dir+mGp.getLogFileName()+".txt");
			if (debug_level>0) {
				String line="Logfile was rotated "+log_dir+mGp.getLogFileName()+"_"+sdf.format(System.currentTimeMillis())+".txt";
				Log.v(mGp.getApplicationTag(),"I "+log_id+line);
				putLogMsg(c,"M I "+sdfDateTime.format(System.currentTimeMillis())+" "+log_id+line);
			}
		} else if (printWriter==null) {
			File tlf=new File(log_dir+mGp.getLogFileName()+".txt");
			if (tlf.exists()) {
				SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
				File lf=new File(log_dir+mGp.getLogFileName()+"_"+sdf.format(System.currentTimeMillis())+".txt");
				tlf.renameTo(lf);
			}
		}
	};

	
	private void closeLogFile() {
		if (printWriter!=null) {
			printWriter.flush();
			printWriter.close(); 
			try {
//				bufferedWriter.close();
				fileWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			printWriter=null;
		}
	};
	
	private void openLogFile(Context c) { 
		if (printWriter==null && log_enabled) {
			BufferedWriter bw=null;
			try {
				File lf=new File(log_dir);
				if (!lf.exists()) lf.mkdirs();
				fileWriter=new FileWriter(log_dir+mGp.getLogFileName()+".txt",true);
				bw=new BufferedWriter(fileWriter,LOG_FILE_BUFFER_SIZE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (bw!=null) {
				printWriter=new PrintWriter(bw,false);
				houseKeepLogFile(c);
			} else {
				log_enabled=false;
			}
		}
	};
	
	private void houseKeepLogFile(Context c) {
		ArrayList<CommonLogFileListItem> lfml=CommonLogUtil.createLogFileList(mGp);
		Collections.sort(lfml,new Comparator<CommonLogFileListItem>(){
			@Override
			public int compare(CommonLogFileListItem arg0,
					CommonLogFileListItem arg1) {
				int result=0;
				long comp=arg0.log_file_last_modified-arg1.log_file_last_modified;
				if (comp==0) result=0;
				else if(comp<0) result=-1;
				else if(comp>0) result=1;
				return result;
			}
		});
		
		int l_epos=lfml.size()-(mGp.getLogMaxFileCount()+1);
		if (l_epos>0) {
			for (int i=0;i<l_epos;i++) {
				String line="Logfile was deleted "+lfml.get(0).log_file_path;
				Log.v(mGp.getApplicationTag(),"I "+log_id+line);
				putLogMsg(c,"M I "+sdfDateTime.format(System.currentTimeMillis())+" "+log_id+line);
				File lf=new File(lfml.get(0).log_file_path);
				lf.delete();
				lfml.remove(0);
			}
			
		}
	};

}
