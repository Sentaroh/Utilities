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
package com.sentaroh.android.Utilities.LogUtil;

import android.content.Context;
import android.util.Log;

import com.sentaroh.android.Utilities.CommonGlobalParms;
import com.sentaroh.android.Utilities.StringUtil;
import com.sentaroh.android.Utilities.ThreadCtrl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;

import static com.sentaroh.android.Utilities.LogUtil.CommonLogConstants.LOG_FILE_BUFFER_SIZE;

public class CommonLogWriter {
    private static PrintWriter printWriter=null;
    //	private static BufferedWriter bufferedWriter;
//    private static FileWriter fileWriter ;
    private static String log_dir=null;
    private static int debug_level=1;
    private static File logFile=null;
    private static boolean log_enabled=true;

//    private static final SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private static CommonGlobalParms mCgp=null;
    private static String log_id="";
    private static ArrayBlockingQueue<String[]> mLogMessageQueue =new ArrayBlockingQueue<String[]>(5000);
//    private static String threadCtrl="E";

    private static int queueHighWaterMark=0;
    private static Thread logThread=null;
    private static ThreadCtrl threadCtrl =new ThreadCtrl();
    private static boolean mLogWriterStopped =false;
    private static Context mContext=null;
    private static long mLastWriteTime =0L;
//    private static ReentrantReadWriteLock mThreadLock=new ReentrantReadWriteLock();
    private static boolean mThreadIsActive=false;

    static public void enqueue(final CommonGlobalParms cgp, final Context c, final String action, String msg, boolean force_notify) {
        mCgp=cgp;
        debug_level=cgp.getDebugLevel();
        mContext=c;
        if (log_id.equals("")) setLogId("LogReceiver");
        if (action!=null) {
            if (!mLogWriterStopped) {
                if (logThread==null) {
                    logThread=new Thread(){
                        @Override
                        public void run() {
                            if (!mThreadIsActive) {
                                mThreadIsActive=true;
                                if (debug_level>=2) writeLogDirect("CommonLogWriter thread was created. TID="+Thread.currentThread().getId());
                                processMessageQueue();
                            } else {
                                if (debug_level>=2) writeLogDirect("CommonLogWriter thread was terminated, because already active.");
                            }
                        }
                    };
                    logThread.setName("CommonLogWriter");
                    logThread.setPriority(Thread.MIN_PRIORITY);
                    logThread.start();
                }
                String[] msg_item=new String[]{action,msg};
                try {
                    mLogMessageQueue.put(msg_item);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mLogMessageQueue.size()>500 || force_notify)
                    synchronized(threadCtrl) {
                        threadCtrl.notify();
                    }
            } else {
                mLogMessageQueue.clear();
            }
        }
    }

    static private void processMessageQueue() {
        while(logThread!=null) {
            if (mLogMessageQueue.size()==0) {
                synchronized(threadCtrl) {//Wait until notified or after 10 sec
                    try {
                        threadCtrl.wait(1000);
                        if (mLogMessageQueue.size()==0) {
                            if (printWriter!=null) printWriter.flush();
                            if ((System.currentTimeMillis()- mLastWriteTime)>30*1000) {
                                if (printWriter!=null) printWriter.flush();
                                if (debug_level>=2) writeLogDirect("CommonLogWriter Thread was ended by idle timer. TID="+Thread.currentThread().getId()+", HWM="+queueHighWaterMark);
                                queueHighWaterMark=0;
                                mThreadIsActive=false;
                                logThread=null;
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        writeLogDirect("CommonLogWriter Wait error. error="+e.getMessage());
                        mLogWriterStopped =true;
                        logThread=null;
                    }
                }
            } else {
                if (queueHighWaterMark< mLogMessageQueue.size()) queueHighWaterMark= mLogMessageQueue.size();
                while(mLogMessageQueue.size()>0) {
                    String[] msg_item= mLogMessageQueue.poll();
                    writeLog(mContext, msg_item);
                }
                mLastWriteTime =System.currentTimeMillis();
            }
        }
    }

    static private void writeLogDirect(String line) {
        if (debug_level>0 && mCgp.isLogEnabled()) {
            if (mCgp.isLogcatEnabled()) Log.v(mCgp.getApplicationTag(),"I "+log_id+line);
            writeLog(mContext, new String[]{mCgp.getLogIntentSend(),
                    "D I "+ StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis())+" "+log_id+line});
        }
    }

    static public void writeLog(Context c, String[] msg_item) {
        if (log_dir==null) {
            setLogId("LogReceiver");
            initParms(c);
            if (debug_level>0) {
                String line="initialized dir="+log_dir+", debug="+debug_level+", logEnabled="+log_enabled;
                Log.v(mCgp.getApplicationTag(),"I "+log_id+line);
                putLogMsg(c,"M I "+ StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis())+" "+log_id+line);
            }
        }
        String msg_action=msg_item[0];
        String msg_data=msg_item[1];
//        if (mCgp.getDebugLevel()>=2) Log.v("SMBSync2","Action="+in.getAction());
        if (msg_action.equals(mCgp.getLogIntentSend())) {
            if (msg_data!=null) {
                putLogMsg(c,msg_data);
            }
        } else if (msg_action.equals(mCgp.getLogIntentClose())) {
            if (printWriter!=null) {
                printWriter.flush();
                closeLogFile();
            }
        } else if (msg_action.equals(mCgp.getLogIntentReset())) {
            initParms(c);
            closeLogFile();
            if (log_enabled) {
                openLogFile(c);
                if (debug_level>0) {
                    String line="re-initialized dir="+log_dir+", debug="+debug_level+", log_enabled="+log_enabled;
                    Log.v(mCgp.getApplicationTag(),"I "+log_id+line);
                    putLogMsg(c,"M I "+StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis())+" "+log_id+line);
                }
            }else {
                rotateLogFileForce(c);
            }
        } else if (msg_action.equals(mCgp.getLogIntentDelete())) {
            if (printWriter!=null) {
                closeLogFile();
                logFile.delete();
            }
        } else if (msg_action.equals(mCgp.getLogIntentRotate())) {
            rotateLogFileForce(c);
        } else if (msg_action.equals(mCgp.getLogIntentFlush())) {
            if (printWriter!=null) printWriter.flush();
        }
    };

    static private void setLogId(String li) {
        log_id=(li+"                 ").substring(0,16)+" ";
    };

    static private void putLogMsg(Context c,String msg) {
        rotateLogFileConditional(c);
        if (printWriter==null) {
            openLogFile(c);
            if (printWriter!=null) {
                synchronized(printWriter) {
                    printWriter.println(msg);
//                    if (mLogMessageQueue.size()==0)
//                        printWriter.flush();//debug
                }
            }
        } else {
            synchronized(printWriter) {
                printWriter.println(msg);
//                if (mLogMessageQueue.size()==0)
//                    printWriter.flush();//debug
            }
        }
    }

    static private void initParms(Context context) {
        log_dir=mCgp.getLogDirName()+"/";
        debug_level=mCgp.getDebugLevel();
        log_enabled=mCgp.isLogEnabled();
        logFile=new File(log_dir+mCgp.getLogFileName()+".txt");
    }

    static private void rotateLogFileConditional(Context c) {
        if (printWriter!=null && logFile.length()>=mCgp.getLogLimitSize()) {
            rotateLogFileForce(c);
        }
    }

    static private void rotateLogFileForce(Context c) {
        if (printWriter!=null) {
            printWriter.flush();
            closeLogFile();
            SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
            File lf=new File(log_dir+mCgp.getLogFileName()+"_"+sdf.format(System.currentTimeMillis())+".txt");
            logFile.renameTo(lf);
            openLogFile(c);
            logFile=new File(log_dir+mCgp.getLogFileName()+".txt");
            if (debug_level>0) {
                String line="Logfile was rotated "+log_dir+mCgp.getLogFileName()+"_"+sdf.format(System.currentTimeMillis())+".txt";
                Log.v(mCgp.getApplicationTag(),"I "+log_id+line);
                putLogMsg(c,"M I "+StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis())+" "+log_id+line);
            }
        } else if (printWriter==null) {
            File tlf=new File(log_dir+mCgp.getLogFileName()+".txt");
            if (tlf.exists()) {
                SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
                File lf=new File(log_dir+mCgp.getLogFileName()+"_"+sdf.format(System.currentTimeMillis())+".txt");
                tlf.renameTo(lf);
            }
        }
    }


    static private void closeLogFile() {
        if (printWriter!=null) {
            printWriter.flush();
            printWriter.close();
//            try {
//                fileWriter.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            printWriter=null;
        }
    }

    static private void openLogFile(Context c) {
        if (printWriter==null && log_enabled) {
            BufferedWriter bw=null;
            try {
                File lf=new File(log_dir);
                if (!lf.exists()) lf.mkdirs();
                FileWriter fw=new FileWriter(log_dir+mCgp.getLogFileName()+".txt",true);
                bw=new BufferedWriter(fw,LOG_FILE_BUFFER_SIZE);
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
    }

    static private void houseKeepLogFile(Context c) {
        ArrayList<CommonLogFileListItem> lfml=CommonLogUtil.createLogFileList(mCgp);
        Collections.sort(lfml,new Comparator<CommonLogFileListItem>(){
            @Override
            public int compare(CommonLogFileListItem arg0, CommonLogFileListItem arg1) {
                int result=0;
                long comp=arg0.log_file_last_modified-arg1.log_file_last_modified;
                if (comp==0) result=0;
                else if(comp<0) result=-1;
                else if(comp>0) result=1;
                return result;
            }
        });

        int l_epos=lfml.size()-(mCgp.getLogMaxFileCount()+1);
        if (l_epos>0) {
            for (int i=0;i<l_epos;i++) {
                String line="Logfile was deleted "+lfml.get(0).log_file_path;
                Log.v(mCgp.getApplicationTag(),"I "+log_id+line);
                putLogMsg(c,"M I "+StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(System.currentTimeMillis())+" "+log_id+line);
                File lf=new File(lfml.get(0).log_file_path);
                lf.delete();
                lfml.remove(0);
            }

        }
    }

}
