package com.sentaroh.android.Utilities.LogUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sentaroh.android.Utilities.CommonGlobalParms;

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
import java.util.concurrent.ArrayBlockingQueue;

import static com.sentaroh.android.Utilities.LogUtil.CommonLogConstants.LOG_FILE_BUFFER_SIZE;

public class CommonLogWriter {
    private static PrintWriter printWriter=null;
    //	private static BufferedWriter bufferedWriter;
    private static FileWriter fileWriter ;
    private static String log_dir=null;
    private static int debug_level=1;
    private static File logFile=null;
    private static boolean log_enabled=true;

    private static final SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private static CommonGlobalParms mGp=null;

    private static String log_id="";

    private static ArrayBlockingQueue<Intent> log_msg_queue=new ArrayBlockingQueue<Intent>(1000);

    private static String threadCtrl="E";

    private static int queueHighWaterMark=0;

    static public void enqueue(final CommonGlobalParms cgp, final Context c, final Intent in) {
        if (in!=null) {
            mGp=cgp;
            debug_level=cgp.getDebugLevel();
            log_msg_queue.add(in);
//            if (queueHighWaterMark<log_msg_queue.size()) {
//                queueHighWaterMark=log_msg_queue.size();
//                Log.v("CommonLogWriter","Log queue high water mark="+queueHighWaterMark);
//            }
            synchronized(threadCtrl) {
                if (log_msg_queue.size()>0 && threadCtrl.equals("E")) {
//                    if (cgp.getDebugLevel()>=2) Log.v("SMBSync2","Log dequeue scheduled");
                    Thread th=new Thread(){
                        @Override
                        public void run() {
                            String tid=Thread.currentThread().getName();
                            int cnt=0;
                            synchronized(threadCtrl) {
                                if (threadCtrl.equals("E")) {
//                                if (cgp.getDebugLevel()>=2) Log.v("SMBSync2","Log dequeue started, size="+log_msg_queue.size()+", tid="+tid);
                                    threadCtrl="D";
                                    while(log_msg_queue.size()>0) {
                                        Intent in=log_msg_queue.poll();
                                        writeLog(c, in);
                                        cnt++;
                                    }
                                    threadCtrl="E";
//                                if (cgp.getDebugLevel()>=2) Log.v("SMBSync2","Log dequeue ended"+", processed="+cnt+", tid="+tid);
                                } else {
//                                if (cgp.getDebugLevel()>=2) Log.v("SMBSync2","Log dequeue bypassed"+", tid="+tid);
                                }
                            }
                        }
                    };
                    th.setName("CommonLogWriter");
                    th.setPriority(Thread.MIN_PRIORITY);
                    th.start();
                } else {
//                if (cgp.getDebugLevel()>=2) Log.v("SMBSync2","Log dequeue not scheduled");
                }
            }
        }
    }

    static public void writeLog(Context c, Intent in) {
        if (log_dir==null) {
            setLogId("LogReceiver");
            initParms(c);
            if (debug_level>0) {
                String line="initialized dir="+log_dir+", debug="+debug_level+", logEnabled="+log_enabled;
                Log.v(mGp.getApplicationTag(),"I "+log_id+line);
                putLogMsg(c,"M I "+sdfDateTime.format(System.currentTimeMillis())+" "+log_id+line);
            }
        }
//        if (mGp.getDebugLevel()>=2) Log.v("SMBSync2","Action="+in.getAction());
        if (in.getAction().equals(mGp.getLogIntentSend())) {
            if (in.getExtras()!=null) {
                String line=in.getExtras().getString("LOG");
                putLogMsg(c,line);
            }
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
                    if (log_msg_queue.size()==0)
                        printWriter.flush();//debug
                }
            }
        } else {
            synchronized(printWriter) {
                printWriter.println(msg);
                if (log_msg_queue.size()==0)
                    printWriter.flush();//debug
            }
        }
    }

    static private void initParms(Context context) {
        log_dir=mGp.getLogDirName()+"/";
        debug_level=mGp.getDebugLevel();
        log_enabled=mGp.isLogEnabled();
        logFile=new File(log_dir+mGp.getLogFileName()+".txt");
    }

    static private void rotateLogFileConditional(Context c) {
        if (printWriter!=null && logFile.length()>=mGp.getLogLimitSize()) {
            rotateLogFileForce(c);
        }
    }

    static private void rotateLogFileForce(Context c) {
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
    }


    static private void closeLogFile() {
        if (printWriter!=null) {
            printWriter.flush();
            printWriter.close();
            try {
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            printWriter=null;
        }
    }

    static private void openLogFile(Context c) {
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
    }

    static private void houseKeepLogFile(Context c) {
        ArrayList<CommonLogFileListItem> lfml=CommonLogUtil.createLogFileList(mGp);
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
    }

}
