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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;

public class MiscUtil{

	final static public String convertRegExp(String filter) {

		if (filter==null || filter.equals("")) return "";
		
		// 正規表現に変換
		String out = "";

		for (int i = 0; i < filter.length(); i++) {
			String temp = filter.substring(i, i + 1);
			if (temp.equals(";")) {// 区切り文字
				if ((i + 1) > filter.length()) {
					// 終了
					break;
				} else {
					out = out + "|";
				}
			} else if (temp.equals("\\")) {
				out = out + "\\\\";
			} else if (temp.equals("*")) {
				out = out + ".*";
			} else if (temp.equals(".")) {
				out = out + "\\.";
			} else if (temp.equals("?")) {
				out = out + ".";
			} else if (temp.equals("+")) {
				out = out + "\\+";
			} else if (temp.equals("{")) {
				out = out + "\\{";
			} else if (temp.equals("}")) {
				out = out + "\\}";
			} else if (temp.equals("(")) {
				out = out + "\\(";
			} else if (temp.equals(")")) {
				out = out + "\\)";
			} else if (temp.equals("[")) {
				out = out + "\\[";
			} else if (temp.equals("]")) {
				out = out + "\\]";
			} else if (temp.equals("^")) {
				out = out + "\\^";
			} else if (temp.equals("$")) {
				out = out + "\\$";
			} else if (temp.equals("[")) {
				out = out + "\\[";
			} else
				out = out + temp;
		}
		return out;
	};

	final static public String convertFileSize(long fs) {
	    String tfs;
		if (fs>(1024*1024*1024)) {//GB
		    BigDecimal dfs1 = new BigDecimal(fs);
		    BigDecimal dfs2 = new BigDecimal(1024*1024*1024);
		    BigDecimal dfs3 = new BigDecimal("0.00");
		    dfs3=dfs1.divide(dfs2,1, BigDecimal.ROUND_HALF_UP);
			tfs=dfs3+" GiB";
		} else if (fs>(1024*1024)) {//MB
		    BigDecimal dfs1 = new BigDecimal(fs*1.00);
		    BigDecimal dfs2 = new BigDecimal(1024*1024*1.00);
		    BigDecimal dfs3 = new BigDecimal("0.00");
		    dfs3=dfs1.divide(dfs2,1, BigDecimal.ROUND_HALF_UP);
			tfs=dfs3+" MiB";
		} else if (fs>(1024)) {//KB
		    BigDecimal dfs1 = new BigDecimal(fs);
		    BigDecimal dfs2 = new BigDecimal(1024);
		    BigDecimal dfs3 = new BigDecimal("0.00");
		    dfs3=dfs1.divide(dfs2,1, BigDecimal.ROUND_HALF_UP);
			tfs=dfs3+" KiB";
		} else tfs=""+fs+" B";
		
		return tfs;
	};

	final static public String getStackTraceString(Exception e) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        pw.close();
        return sw.toString();
    }

}
