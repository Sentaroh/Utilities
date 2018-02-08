package com.sentaroh.android.Utilities;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.TypedValue;

public class ThemeUtil {
	public static ThemeColorList getThemeColorList(Context a, boolean theme_is_light) {
		ThemeColorList tcd=new ThemeColorList();
//    	TypedValue ov = new TypedValue();

    	tcd.theme_is_light=theme_is_light;
    	
//    	a.getTheme().resolveAttribute(android.R.attr.textColorPrimary, ov, true);
//    	tcd.text_color_primary=a.getResources().getColor(ov.resourceId);
//    	
//    	a.getTheme().resolveAttribute(android.R.attr.textColorPrimaryInverse, ov, true);
//    	tcd.text_color_primary_inverse=a.getResources().getColor(ov.resourceId);
//
//    	if (!theme_is_light) {
//    		tcd.text_color_primary_inverse=tcd.text_color_primary;
//    	}
//
////    	a.getTheme().resolveAttribute(android.R.attr.textColorPrimaryDisableOnly, ov, true);
////    	tcd.text_color_disabled=a.getResources().getColor(ov.resourceId);
//    	if (tcd.theme_is_light) tcd.text_color_disabled=Color.GRAY; 
//    	else tcd.text_color_disabled=Color.GRAY;
////    	tcd.text_color_disabled=Color.DKGRAY;
//
//    	a.getTheme().resolveAttribute(android.R.attr.colorBackground, ov, true);
//    	tcd.window_color_background=a.getResources().getColor(ov.resourceId);
//
//    	if (Build.VERSION.SDK_INT>=21) {
//        	a.getTheme().resolveAttribute(android.R.attr.colorPrimary, ov, true);
//        	tcd.window_color_primary=a.getResources().getColor(ov.resourceId);
//
//        	a.getTheme().resolveAttribute(android.R.attr.colorPrimaryDark, ov, true);
//        	tcd.window_color_primary_dark=a.getResources().getColor(ov.resourceId);
//    	} else {
//        	a.getTheme().resolveAttribute(android.R.color.background_dark, ov, true);
//        	tcd.window_color_primary_dark=a.getResources().getColor(ov.resourceId);
//    		tcd.window_color_primary=tcd.window_color_primary_dark;
//    	}

//    	if (Build.VERSION.SDK_INT>=14) {
//        	if (theme_is_light) {
//        		tcd.text_color_disabled=Color.GRAY;
//        		tcd.text_color_primary=0xde000000;
//        		tcd.text_color_dialog_title=0xde000000;//0xffffffff;
//        		if (Build.VERSION.SDK_INT>=21) {
//            		tcd.dialog_title_background_color=0xff808080;
////            		tcd.dialog_msg_background_color=0xffa0a0a0;
////            		tcd.window_background_color_content=0xffeeeeee;
//            		tcd.dialog_msg_background_color=0xffc0c0c0;
//            		tcd.window_background_color_content=0xffe0e0e0;
//        		} else {
//            		tcd.dialog_title_background_color=0xff808080;
//            		tcd.dialog_msg_background_color=0xffb0b0b0;
//            		tcd.window_background_color_content=0xffd0d0d0;
//        		}
//        	} else {
//        		tcd.text_color_disabled=Color.GRAY;
//        		tcd.text_color_primary=0xffffffff;
//        		tcd.text_color_dialog_title=0xffffffff;
//        		tcd.dialog_title_background_color=0xff303030;//515151;
//        		tcd.dialog_msg_background_color=0xff303030;
//        		tcd.window_background_color_content=0xff303030;
//        	}
//    	} else {
//    		tcd.text_color_disabled=Color.GRAY;
//    		tcd.text_color_primary=0xffffffff;
//    		tcd.text_color_dialog_title=0xffffffff;
//    		tcd.dialog_title_background_color=0xff000000;
//    		tcd.dialog_msg_background_color=0xff303030;
//    		tcd.window_background_color_content=0xff000000;
//    	}

    	if (theme_is_light) {
    		tcd.text_color_disabled=Color.GRAY;
    		tcd.text_color_primary=0xde000000;
//    		tcd.text_color_dialog_title=0xde000000;//0xffffffff;
    		tcd.text_color_dialog_title=0xffffffff;
    		if (Build.VERSION.SDK_INT>=21) {
//        		tcd.dialog_title_background_color=0xff808080;
        		tcd.dialog_title_background_color=0xff303030;//515151;
//        		tcd.dialog_msg_background_color=0xffa0a0a0;
//        		tcd.window_background_color_content=0xffeeeeee;
        		tcd.dialog_msg_background_color=0xffc0c0c0;
        		tcd.window_background_color_content=0xffe0e0e0;
    		} else {
//        		tcd.dialog_title_background_color=0xff808080;
//        		tcd.dialog_msg_background_color=0xffb0b0b0;
//        		tcd.window_background_color_content=0xffd0d0d0;
        		tcd.dialog_title_background_color=0xff303030;//515151;
        		tcd.dialog_msg_background_color=0xffc0c0c0;
        		tcd.window_background_color_content=0xffe0e0e0;
    		}
    	} else {
    		tcd.text_color_disabled=Color.GRAY;
    		tcd.text_color_primary=0xffffffff;
    		tcd.text_color_dialog_title=0xffffffff;
    		tcd.dialog_title_background_color=0xff303030;//515151;
    		tcd.dialog_msg_background_color=0xff303030;
    		tcd.window_background_color_content=0xff303030;
    	}

    	if (tcd.theme_is_light) {
    		tcd.text_color_warning=Color.argb(255, 192, 0, 255);//Color.argb(255, 192, 158, 0);
    	} else {
    		tcd.text_color_warning=Color.YELLOW;
    	}
    	tcd.text_color_info=tcd.text_color_dialog_title;
    	tcd.text_color_error=Color.RED;
//    	Thread.currentThread().dumpStack();
//    	Log.v("ThemeColorList","theme_is_light="+tcd.theme_is_light+
//    			", text_color_primary="+String.format("0x%08x", tcd.text_color_primary)+
//    			", text_color_primary_inverse="+String.format("0x%08x", tcd.text_color_dialog_title)+
//    			", text_color_disabled="+String.format("0x%08x", tcd.text_color_disabled)+
//    			", text_color_info="+String.format("0x%08x", tcd.text_color_info)+
//    			", text_color_warning="+String.format("0x%08x", tcd.text_color_warning)+
//    			", text_color_errpr="+String.format("0x%08x", tcd.text_color_error)+
//    			", window_color_dialog_title="+String.format("0x%08x", tcd.window_color_dialog_title)+
//    			", window_color_dialog_content="+String.format("0x%08x", tcd.window_color_dialog_content)
//    			);
    	return tcd;
	}
	
	public static ThemeColorList getThemeColorList(Context a) {
    	TypedValue outValue = new TypedValue();
    	boolean theme_is_light=
    			a.getTheme().resolveAttribute(R.attr.isLightTheme, outValue, true) && outValue.data != 0;
    	return getThemeColorList(a,theme_is_light);
	}

	public static int getAppTheme(Context a) {
		int theme=0;
    	TypedValue outValue = new TypedValue();
    	boolean theme_is_light=
    			a.getTheme().resolveAttribute(R.attr.isLightTheme, outValue, true) && outValue.data != 0;
    	if (theme_is_light) theme=R.style.MainLight; 
    	else theme=R.style.Main;
    	return theme;
	}

}
