package com.sentaroh.android.Utilities;

/*
The MIT License (MIT)
Copyright (c) 2011 Sentaroh

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

import android.util.Log;

public class ThreadCtrl {

	final private static boolean DEBUG =false; 
	final public static String THREAD_ENABLED="1";
	final public static String THREAD_DISABLED="0";
	private String threadEnable ="0";   //0:disable, "1" enable
	
	private String threadMessage ="";   //message
	
	final public static String THREAD_RESULT_SUCCESS="0";
	final public static String THREAD_RESULT_CANCELLED="C";
	final public static String THREAD_RESULT_ERROR="E";
	private String threadStatus = "U"; //E:Error, 0:No error, C:cancel, U:not set
	
	private boolean threadActive=false;
	
	private boolean activityForeGround=true;
	
	private int extra_id=0;
	
	private Object[] extra_object=null;
	
	public ThreadCtrl() {
		threadEnable="1"; 
		threadStatus = "0";
		threadMessage="";
		threadActive=false;
		if(DEBUG) Log.v("threadCtrl","INIT entered. s="+threadEnable);
		
	}
	
	final public String toString() {
		return "threadEnabled="+threadEnable+",threadStatus="+
						threadStatus+", threadMessage="+threadMessage;
	}
	
	final public void setThreadActive(boolean p) {threadActive=p;}
	final public boolean isThreadActive() {return threadActive;}
	
	final public void setActivityForeGround(boolean p) {activityForeGround=p;}
	
	final public boolean isActivityForeGround() {return activityForeGround;}
	
	final public boolean setThreadMessage(String msg) { // set OK 
		synchronized(threadMessage) {
			if(DEBUG) Log.v("threadCtrl","setThreadMessage entered. msg="+msg);
			if (msg!=null) threadMessage = msg;
		}
		return true;
	}
	
	final public String getThreadMessage() { // set OK
		String msg="";
		synchronized(threadMessage) {
			if(DEBUG) Log.v("threadCtrl","setThreadMessage entered. msg="+msg);
			msg=threadMessage;
		}
		return msg;
	}
	
	final public boolean setThreadResultSuccess() { // set OK 
		synchronized(threadStatus) {
			if(DEBUG) Log.v("threadCtrl","setThreadResultOk entered.");
			threadStatus = THREAD_RESULT_SUCCESS;
		}
		return true;
	}
	
	final public boolean isThreadResultSuccess() {
		boolean rc=false;
		synchronized(threadStatus) {
			if(DEBUG) Log.v("threadCtrl","isThreadResultOk entered. result="+threadStatus);
			if (threadStatus.equals(THREAD_RESULT_SUCCESS)) rc=true;
			else rc=false; 
		}
		return rc;
	}
	
	final public boolean setThreadResultError() { // set Error 
		synchronized(threadStatus) {
			if(DEBUG) Log.v("threadCtrl","setThreadResultError entered.");
			threadStatus = THREAD_RESULT_ERROR;
		}
		return true;
	}
	
	final public boolean isThreadResultError() {
		boolean rc=false;
		synchronized(threadStatus) {
			if(DEBUG) Log.v("threadCtrl","isThreadResultError entered. result="+threadStatus);
			if (threadStatus.equals(THREAD_RESULT_ERROR)) rc=true;
			else rc=false; 
		}
		return rc;
	}
	
	final public boolean setThreadResultCancelled() { // set Cancelled 
		synchronized(threadStatus) {
			if(DEBUG) Log.v("threadCtrl","setThreadResultError entered.");
			threadStatus = THREAD_RESULT_CANCELLED;
		}
		return true;
	}
	
	final public boolean isThreadResultCancelled() {
		boolean rc=false;
		synchronized(threadStatus) {
			if(DEBUG) Log.v("threadCtrl","isThreadResultCancelled entered. result="+threadStatus);
			if (threadStatus.equals(THREAD_RESULT_CANCELLED)) rc=true;
			else rc=false; 
		}
		return rc;
	}

	final public String getThreadResult() { //E:Error, 1:No error, C:cancelled
		String rc="";
		synchronized(threadStatus) {
			if(DEBUG) Log.v("threadCtrl","getThreadResult entered. result="+threadStatus);
			rc=threadStatus; 
		}
		return rc;
	}
	
	final public void initThreadCtrl() {
		synchronized(threadStatus) {
			if(DEBUG) Log.v("threadCtrl","iniThreadCtrl entered.");
			threadStatus=THREAD_RESULT_SUCCESS; //set to error for initial
			threadEnable=THREAD_ENABLED; 
			threadMessage="";
			extra_id=0;
			threadActive=false;
			extra_object=null;
			return ;
		}
	}

	final public boolean isEnabled() {
		synchronized(threadEnable) {
			if(DEBUG) Log.v("threadCtrl","isEnabled entered. s="+threadEnable);
			if (threadEnable.equals(THREAD_ENABLED)) return true;
			else return false;
		}
	}
	
	final public boolean setEnabled() {
		synchronized(threadEnable) {
			if(DEBUG) Log.v("threadCtrl","setEnabled entered.");
			threadEnable=THREAD_ENABLED;
			return true;
		}
	}
	
	final public boolean setDisabled() {
		synchronized(threadEnable) {
			if(DEBUG) Log.v("threadCtrl","setDisabled entered.");
			threadEnable=THREAD_DISABLED;
			return true;
		}
	}
	
	final public void setExtraDataInt(int p) {
		synchronized(threadEnable) {
			if(DEBUG) Log.v("threadCtrl","setDExtraDataInt entered.");
			extra_id=p;
		}
	}
	
	final public int getExtraDataInt() {
		synchronized(threadEnable) {
			if(DEBUG) Log.v("threadCtrl","getDExtraDataInt entered.");
			return extra_id;
		}
	}
	
	final public void setExtraDataObject(Object[] p) {
		synchronized(threadEnable) {
			if(DEBUG) Log.v("threadCtrl","setDExtraDataObject entered.");
			extra_object=p;
		}
	}
	
	final public Object[] getExtraDataObject() {
		synchronized(threadEnable) {
			if(DEBUG) Log.v("threadCtrl","getDExtraDataObject entered.");
			return extra_object;
		}
	}

}
