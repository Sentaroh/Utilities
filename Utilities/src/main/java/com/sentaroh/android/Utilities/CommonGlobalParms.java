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
package com.sentaroh.android.Utilities;

import android.app.Application;

import java.io.PrintWriter;

public class CommonGlobalParms {
	private int debug_level=0;
	private boolean log_enabled=true;
	private boolean logcat_enabled=true;
	private String log_dir_name="";
	private String log_file_name="";

	public PrintWriter logWriter=null;

	public void setDebugLevel(int level) {debug_level=level;}
	public int getDebugLevel() {return debug_level;}

	private int log_limit_size=2048*1024*1024;
	public void setLogLimitSize(int size) {log_limit_size=size;}
	public int getLogLimitSize() {return log_limit_size;}

	private int log_max_file_count=10;
	public void setLogMaxFileCount(int size) {log_max_file_count=size;}
	public int getLogMaxFileCount() {return log_max_file_count;}

	public void setLogEnabled(boolean p) {log_enabled=p;}
	public boolean isLogEnabled() {return log_enabled;}

	public void setLogcatEnabled(boolean p) {logcat_enabled=p;}
	public boolean isLogcatEnabled() {return logcat_enabled;}

	public void setLogDirName(String p) {
	    if (p.endsWith("/")) log_dir_name=p.substring(0,p.length()-1);
	    else log_dir_name=p;
	}
	public String getLogDirName() {return log_dir_name;}
	
	public void setLogFileName(String p) {log_file_name=p;}
	public String getLogFileName() {return log_file_name;}

	private String appl_tag="";
	public void setApplicationTag(String p) {appl_tag=p;}
	public String getApplicationTag() {return appl_tag;}

	private String log_reset="RESET", log_delete="DELETE", log_flush="FLUSH", log_rotate="ROTATE", log_send="SEND", log_close="CLOSE";
//	public void setLogIntent(String reset, String delete, String flush, String rotate, String send, String close) {
//		log_reset=reset;
//		log_delete=delete;
//		log_send=send;
//		log_flush=flush;
//		log_rotate=rotate;
//		log_close=close;
//	};
	public String getLogIntentReset() {return log_reset;}
	public String getLogIntentDelete() {return log_delete;}
	public String getLogIntentSend() {return log_send;}
	public String getLogIntentFlush() {return log_flush;}
	public String getLogIntentRotate() {return log_rotate;}
	public String getLogIntentClose() {return log_close;}

//	private Class log_receiver_class=null;
//	public void setLogReceiverClass(Class recv) {log_receiver_class=recv;};
//    public Class getLogReceiverClass() {return log_receiver_class;};
}
