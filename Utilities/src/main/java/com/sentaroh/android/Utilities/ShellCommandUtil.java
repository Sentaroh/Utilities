/*
The MIT License (MIT)
Copyright (c) 2015 Sentaroh

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
