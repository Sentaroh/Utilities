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

import java.util.Calendar;
import java.util.GregorianCalendar;

public class StringUtil {

    final static public String convDateTimeTo_HourMinSecMili(long time) {
    	final Calendar gcal = new GregorianCalendar();
    	gcal.setTimeInMillis(time);
    	final int hours=gcal.get(Calendar.HOUR_OF_DAY);
    	final int minutes=gcal.get(Calendar.MINUTE);
    	final int second=gcal.get(Calendar.SECOND);
    	final int ms=gcal.get(Calendar.MILLISECOND);
    	
    	final String s_hours=String.valueOf(hours);
    	final String s_minutes=String.valueOf(minutes);
    	final String s_second=String.valueOf(second);
    	final String s_ms=String.valueOf(ms);
    	final StringBuilder sb=new StringBuilder(64);
    	
    	if (hours>9) sb.append(s_hours);
    	else sb.append("0").append(s_hours);
    	sb.append(":");
    	if (minutes>9) sb.append(s_minutes);
    	else sb.append("0").append(s_minutes);
    	sb.append(":");

    	if (second>9) sb.append(s_second);
    	else sb.append("0").append(s_second);
    	sb.append(".");
    	
    	if (ms>99) sb.append(s_ms);
    	else {
    		if (ms>9) sb.append("0").append(s_ms);
    		else sb.append("00").append(s_ms);
    	}
    	return sb.toString();
    };

    final static public String convDateTimeTo_YearMonthDayHourMinSecMili(long time) {
    	final Calendar gcal = new GregorianCalendar();
    	gcal.setTimeInMillis(time);
    	final int yyyy=gcal.get(Calendar.YEAR);
    	final int month=gcal.get(Calendar.MONTH)+1;
    	final int day=gcal.get(Calendar.DATE);
    	final int hours=gcal.get(Calendar.HOUR_OF_DAY);
    	final int minutes=gcal.get(Calendar.MINUTE);
    	final int second=gcal.get(Calendar.SECOND);
    	final int ms=gcal.get(Calendar.MILLISECOND);
    	
    	final String s_yyyy=String.valueOf(yyyy);
    	final String s_month=String.valueOf(month);
    	final String s_day=String.valueOf(day);
    	final String s_hours=String.valueOf(hours);
    	final String s_minutes=String.valueOf(minutes);
    	final String s_second=String.valueOf(second);
    	final String s_ms=String.valueOf(ms);
    	
    	final StringBuilder sb=new StringBuilder(64)
    		.append(s_yyyy).append("/");
    	
    	if (month>9) sb.append(s_month);
    	else sb.append("0").append(s_month);
    	sb.append("/");
    	
    	if (day>9) sb.append(s_day);
    	else sb.append("0").append(s_day);
    	sb.append(" ");
    	
    	if (hours>9) sb.append(s_hours);
    	else sb.append("0").append(s_hours);
    	sb.append(":");
    	
    	if (minutes>9) sb.append(s_minutes);
    	else sb.append("0").append(s_minutes);
    	sb.append(":");

    	if (second>9) sb.append(s_second);
    	else sb.append("0").append(s_second);
    	sb.append(".");
    	
    	if (ms>99) sb.append(s_ms);
    	else {
    		if (ms>9) sb.append("0").append(s_ms);
    		else sb.append("00").append(s_ms);
    	}
    	return sb.toString();
    };

    final static public String convDateTimeTo_YearMonthDayHourMinSec(long time) {
    	final Calendar gcal = new GregorianCalendar();
    	gcal.setTimeInMillis(time);
    	final int yyyy=gcal.get(Calendar.YEAR);
    	final int month=gcal.get(Calendar.MONTH)+1;
    	final int day=gcal.get(Calendar.DATE);
    	final int hours=gcal.get(Calendar.HOUR_OF_DAY);
    	final int minutes=gcal.get(Calendar.MINUTE);
    	final int second=gcal.get(Calendar.SECOND);
    	
    	final String s_yyyy=String.valueOf(yyyy);
    	final String s_month=String.valueOf(month);
    	final String s_day=String.valueOf(day);
    	final String s_hours=String.valueOf(hours);
    	final String s_minutes=String.valueOf(minutes);
    	final String s_second=String.valueOf(second);
    	final StringBuilder sb=new StringBuilder(64)
    		.append(s_yyyy)
    		.append("/");
    	if (month>9) sb.append(s_month);
    	else sb.append("0").append(s_month);
    	sb.append("/");
    	
    	if (day>9) sb.append(s_day);
    	else sb.append("0").append(s_day);
    	sb.append(" ");
    	
    	if (hours>9) sb.append(s_hours);
    	else sb.append("0").append(s_hours);
    	sb.append(":");
    	
    	if (minutes>9) sb.append(s_minutes);
    	else sb.append("0").append(s_minutes);
    	sb.append(":");

    	if (second>9) sb.append(s_second);
    	else sb.append("0").append(s_second);

    	return sb.toString();
    };
    
    final static public String convDateTimeTo_YearMonthDayHourMin(long time) {
    	final Calendar gcal = new GregorianCalendar();
    	gcal.setTimeInMillis(time);
    	final int yyyy=gcal.get(Calendar.YEAR);
    	final int month=gcal.get(Calendar.MONTH)+1;
    	final int day=gcal.get(Calendar.DATE);
    	final int hours=gcal.get(Calendar.HOUR_OF_DAY);
    	final int minutes=gcal.get(Calendar.MINUTE);
    	
    	final String s_yyyy=String.valueOf(yyyy);
    	final String s_month=String.valueOf(month);
    	final String s_day=String.valueOf(day);
    	final String s_hours=String.valueOf(hours);
    	final String s_minutes=String.valueOf(minutes);
    	final StringBuilder sb=new StringBuilder(64)
    		.append(s_yyyy)
    		.append("/");
    	if (month>9) sb.append(s_month);
    	else sb.append("0").append(s_month);
    	sb.append("/");
    	
    	if (day>9) sb.append(s_day);
    	else sb.append("0").append(s_day);
    	sb.append(" ");
    	if (hours>9) sb.append(s_hours);
    	else sb.append("0").append(s_hours);
    	sb.append(":");
    	if (minutes>9) sb.append(s_minutes);
    	else sb.append("0").append(s_minutes);

    	return sb.toString();
    };
    final static public String convDateTimeTo_MonthDayHourMin(long time) {
    	final Calendar gcal = new GregorianCalendar();
    	gcal.setTimeInMillis(time);
    	final int month=gcal.get(Calendar.MONTH)+1;
    	final int day=gcal.get(Calendar.DATE);
    	final int hours=gcal.get(Calendar.HOUR_OF_DAY);
    	final int minutes=gcal.get(Calendar.MINUTE);
    	
    	final String s_month=String.valueOf(month);
    	final String s_day=String.valueOf(day);
    	final String s_hours=String.valueOf(hours);
    	final String s_minutes=String.valueOf(minutes);
    	final StringBuilder sb=new StringBuilder(64);
    	
    	if (month>9) sb.append(s_month);
    	else sb.append("0").append(s_month);
    	sb.append("/");
    	if (day>9) sb.append(s_day);
    	else sb.append("0").append(s_day);
    	sb.append(" ");
    	if (hours>9) sb.append(s_hours);
    	else sb.append("0").append(s_hours);
    	sb.append(":");
    	if (minutes>9) sb.append(s_minutes);
    	else sb.append("0").append(s_minutes);

    	return sb.toString();
    };

	@SuppressWarnings("unused")
	final static public boolean isNumericString(String str) {
		boolean result=true;
		try {
			long l=Long.parseLong(str);
		} catch(NumberFormatException nfe) {
			result=false;
		}
		return result;
	};
    

	final public static String getDumpFormatHexString(byte[]in, int offset, int count) {
		String str = "";
		for(int i=offset; i<offset+count; i++) {
			if ((i%16)==0) {
				if (i!=0) {
					str+="\n";
				}
				str+=String.format("%08x ", i);
				str += String.format("%02x ", in[i]);	
			} else {
				str += String.format("%02x ", in[i]);
			}
		}
		return str;
	};

	final public static String getHexString(byte[]in, int offset, int count) {
		String str = "";
		for(int i=offset; i<offset+count; i++) {
			str += String.format("%02x", in[i]);	
		}
		return str;
	};

}
