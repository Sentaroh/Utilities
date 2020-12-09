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

/*
The MIT License (MIT)
Copyright (c) 2011-2013 Sentaroh

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

@SuppressLint("SdCardPath")
public class LocalMountPoint {
	
	
	static public String[] convertFilePathToMountpointFormat(Context c, String fp) {
		ArrayList<String>ml=getLocalMountPointList(c);
		String ldir="/",lfile="";
		String lurl=getExternalStorageDir();
		for (int i=0;i<ml.size();i++) {
			if (fp.startsWith(ml.get(i))) {
				lurl=ml.get(i);
				break;
			}
		}
		File lf=new File(fp);
		if (lf.isDirectory()) {
			ldir=fp.replace(lurl, "");
		} else {
			if (fp.lastIndexOf("/")>=0) {
				lfile=fp.substring(fp.lastIndexOf("/")+1);
				ldir=fp.replace(lurl, "").replace("/"+lfile, "");
			} else lfile=fp;
		}
		return new String[] {lurl,ldir,lfile};
	};

	public final static String UNKNOWN_USB_DIRECTORY="/usb_unknown";
	static public String getUsbStorageDir() {
		String usb=UNKNOWN_USB_DIRECTORY;
		if (isMountPointReadable("/storage/usbdisk")) usb="/storage/usbdisk";
		else if (isMountPointReadable("/Removable/USBdisk1/Drive1")) usb="/Removable/USBdisk1/Drive1";
		else if (isMountPointReadable("/Removable/USBdisk2/Drive1")) usb="/Removable/USBdisk2/Drive1";
		else if (isMountPointReadable("/storage/emulated/UsbDriveA")) usb="/storage/emulated/UsbDriveA";
		else if (isMountPointReadable("/storage/emulated/UsbDriveB")) usb="/storage/emulated/UsbDriveB";
		return usb;
	};
	
	static private boolean isMountPointReadable(String mp) {
		File lf =new File(mp);
		boolean result=false;
		if (lf.exists() && lf.canRead()) result=true;
		else result=false;
		return result;
	};
	
	static public String getExternalStorageDir() {
		String ext_dir=Environment.getExternalStorageDirectory().toString();
		if (ext_dir!=null) return ext_dir;
		else return "/";
//    	String status   = Environment.getExternalStorageState();  
//		if (!status.equals(Environment.MEDIA_MOUNTED)) {  
//		// media is not mounted  
//    		return "/";
//    	} else  {  
//        // Media is mounted  
////    		ArrayList<String>ml=buildLocalMountPointList();
//    		String ext_dir=Environment.getExternalStorageDirectory().toString();
////    		for (int i=0;i<ml.size();i++) {
////    			if (ml.get(i).equals("/mnt/sdcard")) {
////    				ext_dir="/mnt/sdcard";
////    				break;
////    			}
////    		}
//			return ext_dir;
//    	}
	};

	static public boolean isExternalStorageAvailable() {
    	String status   = Environment.getExternalStorageState();  
		if (!status.equals(Environment.MEDIA_MOUNTED)) return false;  
		else return true;
	};

	static public boolean isExternal2MountPioint(String fp) {
		boolean result=false;
		String env=System.getenv("EXTERNAL_STORAGE2");
		if (env!=null) {
			if (fp.startsWith(env)) result=true;
			else result=false;
		} else result=false;
		return result;
	};

	static public boolean isExternalMountPoint(Context c, String fp) {
		boolean result=false;
		if (isExternalStorageAvailable()) {
			ArrayList<String>ml=getLocalMountPointList(c);
			for (int i=0;i<ml.size();i++) {
				if (fp.startsWith(ml.get(i))||isExternal2MountPioint(fp)) {
					result=true;
					break;
				}
			}
		} else result=false;
		return result;
	};
	
	@SuppressLint("SdCardPath")
	static public boolean isMountPointAvailable(Context c, String fp) {
		boolean result=false;
		if (fp.startsWith("/sdcard")) {
			File lf=new File("/sdcard");
			if (lf.canRead()) result=true;
		} else {
			ArrayList<String> ml=getLocalMountPointList(c);
			for (int i=0;i<ml.size();i++) {
				String t_path=fp.replace(ml.get(i), "");
				if (t_path.length()==0) result=true;
				else {
					if (!t_path.equals(fp)) {
						if (t_path.startsWith("/")) result=true;
					}
				}
			}
		}
		return result;
	};
	
	static public boolean isAppSpecificDirectory(Context c, String lmp, String dir) {
		String fp="";
		if (dir.equals("")) fp=lmp;
		else fp=lmp+"/"+dir;
		return isAppSpecificDirectory(c,fp);
	};

	static public boolean isAppSpecificDirectory(Context c, String fp) {
		boolean result=false;
		ArrayList<String>ml=getLocalMountPointList(c);
		for(int i=0;i<ml.size();i++) {
			if (ml.get(i).contains("/Android/data")) {
				if (fp.startsWith(ml.get(i))) result=true;
				else if(fp.equals(ml.get(i))) result=true;
			} else {
				String nfp=ml.get(i)+"/Android/data";
				if (fp.startsWith(nfp)) result=true;
				else if (fp.equals(nfp)) result=true;
			}
			if (result) break;			
		}
//		Log.v("","result="+result+", fp="+fp);
		return result;
	}

//	@SuppressLint("SdCardPath")
//	static public ArrayList<String> buildLocalMountPointListOld() {
//		ArrayList<String> ml =new ArrayList<String>();
//		File lf =new File("/storage");
//		File[] fl_1=lf.listFiles();
//		if (fl_1!=null) {
//			for (int i=0;i<fl_1.length;i++) {
//				if (fl_1[i].canRead()) ml.add(fl_1[i].getPath());
//			}
//		}
//
//		lf =new File("/mnt");
//		fl_1=lf.listFiles();
//		if (fl_1!=null) {
//			for (int i=0;i<fl_1.length;i++) {
//				if (!fl_1[i].getPath().equals("/mnt/obb") && 
//						!fl_1[i].getPath().equals("/mnt/asec")) {
//					if (fl_1[i].canRead()) ml.add(fl_1[i].getPath());
//				}
//			}
//		}
//		
//		lf =new File("/sdcard");
//		if (lf.canRead()) ml.add("/sdcard");
//		
//		lf=new File("/Removable/SD");
//		if (lf.canRead()) ml.add("/Removable/SD");
//		
//		lf=new File("/Removable/MicroSD");
//		if (lf.canRead()) ml.add("/Removable/MicroSD");
//		
//		lf=new File("/Removable/USBdisk1/Drive1");
//		if (lf.canRead()) ml.add("Removable/USBdisk1/Drive1");
//		
//		lf=new File("/Removable/USBdisk2/Drive1");
//		if (lf.canRead()) ml.add("/Removable/USBdisk2/Drive1");
//
//		lf=new File("/mnt/sdcard/usbStorage/sda1");
//		if (lf.canRead()) ml.add("/mnt/sdcard/usbStorage/sda1");
//
//		lf=new File("/mnt/sdcard/usbStorage/sdb1");
//		if (lf.canRead()) ml.add("/mnt/sdcard/usbStorage/sda1");
//
//		lf=new File("/mnt/sdcard/usbStorage/sdc1");
//		if (lf.canRead()) ml.add("/mnt/sdcard/usbStorage/sda1");
//
//		lf=new File("/mnt/sdcard/usbStorage/sdd1");
//		if (lf.canRead()) ml.add("/mnt/sdcard/usbStorage/sda1");
//
//		String env=System.getenv("EXTERNAL_STORAGE2");
//		if (env!=null) {
//			lf=new File(env);
//			if (lf.canRead()) ml.add(env);
//		}
//		
//		Collections.sort(ml);
//		return ml;
//	};

    static public boolean isMountPointCanWrite(String mp) {
	    boolean result=false;
        File lf=new File(mp+"/isLocalMountPointWritable_temp.tmp");
        if (lf.exists()) lf.delete();
        try {
            result=lf.createNewFile();
            lf.delete();
        } catch (IOException e) {
        }
        return result;
    }

    static public ArrayList<String> getLocalMountpointList2(Context c) {
        ArrayList<String>ml=LocalMountPoint.getLocalMountPointList(c);
        ArrayList<String>new_ml=new ArrayList<String>();
        for (int i=0;i<ml.size();i++) {
            if (ml.get(i).indexOf("/Android/data")<0 &&
                    !ml.get(i).equals("/sdcard") &&
                    !ml.get(i).equals("/mnt/sdcard") &&
                    !ml.get(i).equals("/mnt/user") &&
                    !ml.get(i).equals("/mnt/media_rw") &&
                    !ml.get(i).equals("/mnt/sdcard2") &&
                    !ml.get(i).equals("/storage/emulated/legacy")
                    ) {

                boolean exclude=false;
                String sd_id=ml.get(i).replace("/storage/","");
                if (ml.get(i).startsWith("/storage/")) {
                    if (sd_id.matches("....-....")) {
                        exclude=true;
                    }
                }
                if (!exclude) {
                    boolean dup=false;
                    for(int j=0;j<new_ml.size();j++) {
                        if (new_ml.get(j).equals(ml.get(i))) {
                            dup=true;
                            break;
                        }
                    }
                    if (!dup) new_ml.add(ml.get(i));
                }
            }
        }
        return new_ml;
    }

    @SuppressLint("SdCardPath")
	static public ArrayList<String> getLocalMountPointList(Context c) {
		String pkg_name=c.getClass().getPackage().getName();
		String primary_esd=getExternalStorageDir();
		ArrayList<String> ml =new ArrayList<String>();
		addMountPointPrimary(primary_esd, ml);
		File lf =new File("/storage");
		File[] fl_1=lf.listFiles();
		if (fl_1!=null) {
			for (int i=0;i<fl_1.length;i++) {
				if (fl_1[i].getName().equals("emulated")) {
					File tlf=new File("/storage/emulated");
					File[] tlist=tlf.listFiles();
					if (tlist!=null) {
						for (int j=0;j<tlist.length;j++) {
							addMountPointPrimary(tlist[j].getPath(), ml);
						}
					}
				} else {
					if (fl_1[i].getName().equals("sdcard0")) addMountPointPrimary(fl_1[i].getPath(), ml);
					else if (fl_1[i].getName().equals("sdcard1")) addMountPointPrimary(fl_1[i].getPath(), ml);
					else if (fl_1[i].getName().equals("remote")) {}//nop;
					else if (fl_1[i].getName().equals("uicc0")) addMountPointPrimary(fl_1[i].getPath(), ml);
					else addMountPointPrimaryAndSecondary(fl_1[i].getPath(), ml, pkg_name);
				}
			}
		}

		lf =new File("/mnt");
		fl_1=lf.listFiles();
		if (fl_1!=null) {
			for (int i=0;i<fl_1.length;i++) {
				if (!fl_1[i].getPath().equals("/mnt/obb") && 
						!fl_1[i].getPath().equals("/mnt/asec")) {
					addMountPointPrimary(fl_1[i].getPath(), ml);
				}
			}
		}

        lf =new File("/storage/remote");
        fl_1=lf.listFiles();
        if (fl_1!=null) {
            for (int i=0;i<fl_1.length;i++) {
                addMountPointPrimary(fl_1[i].getPath(), ml);
            }
        }

        addMountPointPrimary("/sdcard", ml);
		addMountPointPrimaryAndSecondary("/Removable/SD", ml, pkg_name);
		addMountPointPrimaryAndSecondary("/Removable/MicroSD", ml, pkg_name);
		
		addMountPointPrimary("/Removable/USBdisk1/Drive1",ml);
		addMountPointPrimary("/Removable/USBdisk2/Drive1",ml);
		addMountPointPrimary("/mnt/sdcard/usbStorage/sda1",ml);
		addMountPointPrimary("/mnt/sdcard/usbStorage/sdb1",ml);
		addMountPointPrimary("/mnt/sdcard/usbStorage/sdc1",ml);
		addMountPointPrimary("/mnt/sdcard/usbStorage/sdd1",ml);

		addMountPointPrimary(primary_esd+"/usbStorage/sda1",ml);
		addMountPointPrimary(primary_esd+"/usbStorage/sdb1",ml);
		addMountPointPrimary(primary_esd+"/usbStorage/sdc1",ml);
		addMountPointPrimary(primary_esd+"/usbStorage/sdd1",ml);

		String env=System.getenv("EXTERNAL_STORAGE2");
		if (env!=null) addMountPointPrimaryAndSecondary(env, ml, pkg_name);

		addMountPointPrimaryAndSecondary("/storage/extSdCard", ml, pkg_name);
		addMountPointPrimaryAndSecondary("/mnt/extSdCard", ml, pkg_name);
        addMountPointPrimaryAndSecondary("/sdcard/external_sd", ml, pkg_name);
		addMountPointPrimaryAndSecondary("/storage/external_SD", ml, pkg_name);

//		File[] ext_dirs =ContextCompat.getExternalFilesDirs(c, null);
        File[] ext_dirs =c.getExternalFilesDirs(null);
		if (ext_dirs!=null) {
			for (int i=0;i<ext_dirs.length;i++) {
				if (ext_dirs[i]!=null && ext_dirs[i].getPath()!=null) {
					if (!ext_dirs[i].getPath().startsWith(primary_esd)) {
						boolean found=false;
						for(int j=0;j<ml.size();j++) {
							if (ext_dirs[i].getPath().equals(ml.get(j))) {
								found=true;
								break;
							}
						}
						if (!found) ml.add(ext_dirs[i].getPath());
					}
				}
			}
		}
		
		Collections.sort(ml);
		return ml;
	};
	
	private static void addMountPointPrimaryAndSecondary(String mp, ArrayList<String>ml, String pkg_name) {
		File lf =new File(mp);
		if (lf.canRead()) {
			ml.add(mp);
			ml.add(mp+"/Android/data/"+pkg_name+"/files");
//			Log.v("","added p&s="+mp);
		}
	};

	@SuppressWarnings("unused")
	private static void addMountPointPrimaryAndSecondary(boolean force, String mp, ArrayList<String>ml, String pkg_name) {
		if (force) {
			ml.add(mp);
			ml.add(mp+"/Android/data/"+pkg_name+"/files");
//			Log.v("","added p&s="+mp);
		}
	};

	private static void addMountPointPrimary(String mp, ArrayList<String>ml) {
		File lf =new File(mp);
		if (lf.canRead()) {
			ml.add(mp);
//			Log.v("","added p="+mp);
		}
	};

//	private String[] getApplSpecificExternalDirectoryList() {
//	    File[] extDirs =ContextCompat.getExternalFilesDirs(mContext, null);
//	    String[] dl=null;
//	    if (extDirs!=null) {
//	    	util.addDebugLogMsg(1,"I","Application specific external directory list size="+extDirs.length);
//		    dl=new String[extDirs.length];
//		    for (int i=0;i<extDirs.length;i++) {
//		    	String fp=extDirs[i].getPath();
////		    	String nfp="";
////		    	if (fp.lastIndexOf("/")>0) {
////		    		nfp=fp.substring(0, fp.lastIndexOf("/"));
////		    	}
//				util.addDebugLogMsg(1,"I","  No="+(i+1)+", path="+fp);
//				dl[i]=fp;
//		    }
//	    }
//	    return dl;
//	};
//	

	final public static ArrayList<LocalMountPointListItem> getLocalMountPointListWithLink(Context c) {
		ArrayList<LocalMountPointListItem>list=new ArrayList<LocalMountPointListItem>();
		ArrayList<String>mp_list=getLocalMountPointList(c);
		for (int i=0;i<mp_list.size();i++) {
			File lf=null;
			String abs_path="", can_path="";
			lf=new File(mp_list.get(i));
			abs_path=lf.getAbsolutePath();
			try {
				can_path=lf.getCanonicalPath();
				LocalMountPointListItem lmpe=new LocalMountPointListItem();
				if (abs_path.equals(can_path)) {
					lmpe.mount_point_name=abs_path;
					lmpe.isSynbolicLink=false;
				} else {
					lmpe.mount_point_name=abs_path;
					lmpe.link_name=can_path;
					lmpe.isSynbolicLink=true;
				}
//				Log.v("","link="+lmpe.isSynbolicLink+", mp="+lmpe.mount_point_name+", link="+lmpe.link_name);
				list.add(lmpe);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return list;
	};
	static public class LocalMountPointListItem {
		public boolean isSynbolicLink=false;
		public String mount_point_name=null;
		public String link_name=null;
		
		public LocalMountPointListItem() {};
	};

}

