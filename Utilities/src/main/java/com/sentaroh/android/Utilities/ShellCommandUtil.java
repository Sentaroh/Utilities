package com.sentaroh.android.Utilities;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ShellCommandUtil {
	public static boolean isSuperUserAvailable() {
		  Process p=null;
		  String out_msg="";
		  boolean result=false;
		  try {
			  p = Runtime.getRuntime().exec("su");//command);
			  DataOutputStream cmd_in=new DataOutputStream(p.getOutputStream());
			  cmd_in.writeBytes("id"+"\n");
			  cmd_in.flush();
			  cmd_in.writeBytes("exit\n");
			  cmd_in.flush();
			  BufferedReader std_out = new BufferedReader(new InputStreamReader(p.getInputStream()));
			  BufferedReader std_err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			  String line;
			  while ((line = std_out.readLine()) != null) {
		            out_msg += line + "\n";
			  }
//			  Log.v("","ret="+out_msg);
			  cmd_in.close();
			  std_out.close();
			  std_err.close();
			  p.waitFor();
			  p.destroy();
			  p=null;
			  if (out_msg.equals("")) {
				  //SU not granted
				  result=false;
			  } else {
				  result=true;
			  }
		  } catch (IOException e) {
			  if (p!=null) p.destroy();
			  e.printStackTrace();
		  } catch (InterruptedException e) {
			  if (p!=null) p.destroy();
			  e.printStackTrace();
		  }
		  return result;
	};
	
	public static String executeShellCommandWithSu(String cmd) throws IOException, InterruptedException {
		String result=null;
		Process p = Runtime.getRuntime().exec("su");//command);
		DataOutputStream cmd_in=new DataOutputStream(p.getOutputStream());
		cmd_in.writeBytes(cmd+"\n");
		cmd_in.flush();
//		cmd_in.writeBytes("exit\n");
//		cmd_in.flush();
	    cmd_in.close();
		BufferedReader std_out = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader std_err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String line="";
		String out_msg="";
	    while ((line = std_out.readLine()) != null) out_msg += line + "\n";
	    while ((line = std_err.readLine()) != null) out_msg += line + "\n";
	    std_out.close();
	    std_err.close();
	    p.waitFor();
	    p.destroy();
	    p=null;
	    result=out_msg;
	    return result;
	};

	public static String executeShellCommand(String cmd) throws IOException, InterruptedException {
		String result=null;
		Process p = Runtime.getRuntime().exec(cmd);
		BufferedReader std_out = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader std_err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String line;
		String out_msg="";
	    while ((line = std_out.readLine()) != null) out_msg += line + "\n";
	    while ((line = std_err.readLine()) != null) out_msg += line + "\n";
	    std_out.close();
	    p.waitFor();
	    p=null;
	    result=out_msg;
	    return result;
	};

}
