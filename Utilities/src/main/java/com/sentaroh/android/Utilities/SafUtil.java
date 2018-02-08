package com.sentaroh.android.Utilities;

//下記サイトの情報を参考にし作成
//How to use the new SD-Card access API presented for Lollipop?
//http://stackoverflow.com/questions/26744842/how-to-use-the-new-sd-card-access-api-presented-for-lollipop

import java.io.File;
import java.util.ArrayList;

import com.sentaroh.android.Utilities.LocalMountPoint;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;


@SuppressLint("SdCardPath")
public class SafUtil {
	private static final String SAF_EXTERNAL_SDCARD_TREE_URI_KEY="external_sdcard_tree_uri_key";
	public static void initWorkArea(Context c, SafCommonArea swa, boolean debug) {
//		prefs = PreferenceManager.getDefaultSharedPreferences(c);
		swa.context=c;
		swa.debug_enabled=debug;
		swa.pkg_name=swa.context.getPackageName();
		swa.app_spec_dir="/android/data/"+swa.pkg_name;
		String uri_string=getSafExternalSdcardRootTreeUri(swa);
		if (!uri_string.equals(""))
			swa.rootDocumentFile=SafFile.fromTreeUri(swa.context, Uri.parse(uri_string));
		buildSafExternalSdcardDirList(swa, swa.external_sdcard_dir_list);
	};

	private static void buildSafExternalSdcardDirList(SafCommonArea swa, ArrayList<String> al) {
		File[] fl=ContextCompat.getExternalFilesDirs(swa.context, null);
		String ld=LocalMountPoint.getExternalStorageDir();
		al.clear();
		if (Build.VERSION.SDK_INT>=21) {
			if (fl!=null) {
				for(File f:fl) {
					if (f!=null && f.getPath()!=null && !f.getPath().startsWith(ld)) {
						String esd=f.getPath().substring(0, f.getPath().indexOf("/Android/data"));
						if (swa.debug_enabled) Log.v("SafUtil","buildSafExternalSdcardDirList dir="+esd);
						al.add(esd);
					}
				}
			}
		}
	};

	final static public String UNKNOWN_SDCARD_DIRECTORY="/sdcard_unknown";
	public static String getSafExternalSdcardDir(Context c) {
		File[] fl=ContextCompat.getExternalFilesDirs(c, null);
		String ld=LocalMountPoint.getExternalStorageDir();
		if (Build.VERSION.SDK_INT>=21) {
			if (fl!=null) {
				for(File f:fl) {
					if (f!=null && f.getPath()!=null && !f.getPath().startsWith(ld)) {
						String esd=f.getPath().substring(0, f.getPath().indexOf("/Android/data"));
						return esd;
					}
				}
			}
		}
		return UNKNOWN_SDCARD_DIRECTORY;
	};

	public static String getSafExternalSdcardRootTreeUri(SafCommonArea swa) {
		long b_time=System.currentTimeMillis();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(swa.context);
		String uri_string=prefs.getString(SAF_EXTERNAL_SDCARD_TREE_URI_KEY, "");
		if (swa.debug_enabled) Log.v("SafUtil","getSafExternalSdcardRootTreeUri elapsed="+(System.currentTimeMillis()-b_time));
		return uri_string;
	}
	
	public static boolean hasSafExternalSdcard(SafCommonArea swa) {
		boolean result=false;
//		File lf=new File("/storage/sdcard1");
//		if (lf.exists() && lf.canWrite()) result=true;
//		else {
//			lf=new File("/sdcard1");
//			if (lf.exists() && lf.canWrite()) result=true;
//		}
		ArrayList<String>al=new ArrayList<String>();
		buildSafExternalSdcardDirList(swa, al);
		if (al.size()>0) result=true;
		if (swa.debug_enabled) Log.v("SafUtil","hasSafExternalSdcard result="+result);		
		return result;
	};

	public static boolean isSafExternalSdcardRootTreeUri(SafCommonArea swa, Uri tree_uri) {
		long b_time=System.currentTimeMillis();
		boolean result=false;
		String tree_uri_string=tree_uri.toString(), edit_string="";
        if (tree_uri_string.endsWith("%3A")) {
        	if (!tree_uri_string.endsWith("/tree/primary%3A")) edit_string=tree_uri_string;
        } else if (tree_uri_string.endsWith(":")) {
        	if (!tree_uri_string.endsWith("/tree/primary:")) edit_string=tree_uri_string;
        }
//        Log.v("","str="+tree_uri_string);
		
        if (!edit_string.equals("")) {
    		SafFile document=SafFile.fromTreeUri(swa.context, tree_uri);
//    		if (document.getName().startsWith("sdcard1")) result=true;
    		if (document.getName()!=null) result=true;
        } else {
        	result=false;
        }
		if (swa.debug_enabled) Log.v("SafUtil","isSafExternalSdcardRootTreeUri result="+result+", elapsed="+(System.currentTimeMillis()-b_time));
		return result;
	};

	public static boolean isSafExternalSdcardPath(SafCommonArea swa, String fpath) {
		boolean result=false;
		if (Build.VERSION.SDK_INT>=21) {
			if (fpath.startsWith("/sdcard1")) {
				if (!fpath.startsWith("/sdcard1"+swa.app_spec_dir)) result=true;
			} else if (fpath.startsWith("/mnt/extSdCard")) {
				if (!fpath.startsWith("/mnt/extSdCard"+swa.app_spec_dir)) result=true;
			} else if (fpath.startsWith("/storage/extSdCard")) {
				if (!fpath.startsWith("/storage/extSdCard"+swa.app_spec_dir)) result=true;
//			} else if (Build.VERSION.SDK_INT>=23 && fpath.startsWith("/storage/")) {
//				String t_devid=fpath.replace("/storage/", "");
//				String devid="";
//				if (t_devid.indexOf("/")>=0) devid=t_devid.substring(0,t_devid.indexOf("/"));
//				else devid=t_devid;
//				if (devid.length()==9 && devid.indexOf("-")==4) {
//					if (!fpath.startsWith("/storage/"+devid+"/"+swa.app_spec_dir)) result=true;
//				}
//				if (swa.debug_enabled) Log.v("SafUtil","isSafExternalSdcardPath result="+result+
//						", fp="+fpath+", devid_pre="+t_devid+", devid="+devid);
			} else {
				for(String esd:swa.external_sdcard_dir_list) {
					if (swa.debug_enabled) Log.v("SafUtil","isSafExternalSdcardPath esd="+esd+
							", fp="+fpath);
					if (fpath.startsWith(esd)) {
						if (!fpath.startsWith(esd+swa.app_spec_dir)) {
							result=true;
							if (swa.debug_enabled) Log.v("SafUtil","isSafExternalSdcardPath result="+result);
							break;
						}
					}
				}
			}
		}
		return result;
	};
	
	public static boolean isValidSafExternalSdcardRootTreeUri(SafCommonArea swa) {
		long b_time=System.currentTimeMillis();
		String uri_string=getSafExternalSdcardRootTreeUri(swa);
		boolean result=true; 
		if (uri_string.equals("")) result=false;
		else {
			SafFile docf=SafFile.fromTreeUri(swa.context, Uri.parse(uri_string));
			if (docf.getName()==null) result=false;
		}
		if (swa.debug_enabled) Log.v("SafUtil","isValidSafExternalSdcardRootTreeUri elapsed="+(System.currentTimeMillis()-b_time));
		return result;
	}
	@SuppressLint("NewApi")
	public static String saveSafExternalSdcardRootTreeUri(SafCommonArea swa, 
			String tree_uri_string) {
        String edit_string="";
//        if (tree_uri_string.length()>(tree_uri_string.indexOf("%3A")+3)) {
//        	edit_string=tree_uri_string.substring(0,(tree_uri_string.indexOf("%3A")+3));
//        } else {
//        	edit_string=tree_uri_string;
//        }
        if (tree_uri_string.endsWith("%3A")) {
        	if (!tree_uri_string.endsWith("/tree/primary%3A")) edit_string=tree_uri_string;
        } else if (tree_uri_string.endsWith(":")) {
        	if (!tree_uri_string.endsWith("/tree/primary:")) edit_string=tree_uri_string;
        }
//      Log.v("","edit_string="+edit_string);
        if (!edit_string.equals("")) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(swa.context);
    		prefs.edit().putString(SAF_EXTERNAL_SDCARD_TREE_URI_KEY, edit_string).commit();
    		
    		swa.context.getContentResolver().takePersistableUriPermission(Uri.parse(edit_string),
    			      Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
		return edit_string;
	};
	
	public static String getFileNameFromPath(String fpath) {
		String result="";
		String[] st=fpath.split("/");
		if (st!=null) {
			if (st[st.length-1]!=null) result=st[st.length-1];
		}
		return result;
	}

	public static SafFile getSafDocumentFileByPath(SafCommonArea swa,  
			String target_path, boolean isDirectory) {
		if (swa.debug_enabled) Log.v("SafUtil","target_path="+target_path);
		long b_time=System.currentTimeMillis();
    	SafFile document=swa.rootDocumentFile;
    	
    	String relativePath = null;
    	String baseFolder="";
    	if (target_path.startsWith("/sdcard1")) baseFolder="/sdcard1";
		else if (target_path.startsWith("/mnt/extSdCard")) baseFolder="/mnt/extSdCard";
		else if (target_path.startsWith("/storage/extSdCard")) baseFolder="/storage/extSdCard";
		else {
			String t_devid=target_path.replace("/storage/", "");
			String devid="";
			if (t_devid.indexOf("/")>=0) devid=t_devid.substring(0,t_devid.indexOf("/"));
			else devid=t_devid;
			if (devid.length()==9 && devid.indexOf("-")==4) {
				baseFolder="/storage/"+devid;
			} else {
	    		for(String esd:swa.external_sdcard_dir_list) {
	    			if (swa.debug_enabled) Log.v("SafUtil","esd="+esd);
					if (target_path.startsWith(esd)) 
						baseFolder=esd+"/";
				}
	    		if (baseFolder.equals("")) return null;
			}
    	}
    	
    	relativePath=target_path.replace(baseFolder, "");
    	
    	if (swa.debug_enabled) Log.v("SafUtil","relativePath="+relativePath+", base="+baseFolder);
        String[] parts = relativePath.split("\\/");
        for (int i = 0; i < parts.length; i++) {
        	if (swa.debug_enabled) Log.v("SafUtil","parts="+parts[i]);
        	if (!parts[i].equals("")) {
                SafFile nextDocument = document.findFile(parts[i]);
//                DCFile nextDocument = findFile(document, parts[i]);
                if (nextDocument == null) {
                    if ((i < parts.length - 1) || isDirectory) {
                    	if (swa.debug_enabled) Log.v("SafUtil","dir created name="+parts[i]);
                   		nextDocument = document.createDirectory(parts[i]);
                    } else {
                    	if (swa.debug_enabled) Log.v("SafUtil","file created name="+parts[i]);
                        nextDocument = document.createFile("", parts[i]);
                    }
                }
                document = nextDocument;
        	}
        }
//    	c.getContentResolver().releasePersistableUriPermission(treeUri,
//                Intent.FLAG_GRANT_READ_URI_PERMISSION |
//                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (swa.debug_enabled) Log.v("SafUtil","getSafDocumentFileByPath elapsed="+(System.currentTimeMillis()-b_time));
        return document;
	};
	
//	private static DCFile findFile(DCFile df, String name) {
//		DCFile result=null;
//		
//		DCFile[]lf=df.listFiles();
//		if (lf!=null) {
//			for(DCFile sdf:lf) {
//				if (sdf.canRead()) {
//					if (sdf.getName()!=null) {
//						if (sdf.getName().equals(name)) {
//							result=sdf;
//							break;
//						}
//					}
//				}
//			}
//		}
//		return result;
//	}

}


