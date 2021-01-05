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

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SafManager {
    public static final String SDCARD_UUID_KEY ="removable_tree_uuid_key";

    public static final String USB_UUID_KEY ="usb_tree_uuid_key";

    private static final String APPLICATION_TAG="SafManager";

    private Context mContext=null;

    public final static String UNKNOWN_USB_DIRECTORY="/unknown_usb";
    private String usbRootDirectory=UNKNOWN_USB_DIRECTORY;
    private SafFile usbRootSafFile=null;
    private String usbRootUuid=null;

    public final static String UNKNOWN_SDCARD_DIRECTORY="/sdcard_unknown";
    private String sdcardRootDirectory=UNKNOWN_SDCARD_DIRECTORY;
    private SafFile sdcardRootSafFile=null;
    private String sdcardRootUuid=null;

    private static Logger slf4jLog = LoggerFactory.getLogger(SafManager.class);

    private ArrayList<String>usbUuidList=new ArrayList<String>();

    public void setUsbUuidList(ArrayList<String> list) {
        usbUuidList=list;
    }

    public ArrayList<String> getUsbUuidList() {
        return usbUuidList;
    }

    private void putDebugMessage(String msg) {
        slf4jLog.debug(msg);
    }

    private void putInfoMessage(String msg) {
        slf4jLog.debug(msg);
    }

    private String mLastErrorMessage="";
    private void putErrorMessage(String msg) {
        mLastErrorMessage=msg;
        slf4jLog.error(msg);
    }

    public String getLastErrorMessage() {
        String em=mLastErrorMessage;
        mLastErrorMessage="";
        return em;
    }

    public void setDebugEnabled(boolean enabled) {
        //NOP
    }

    public SafManager(Context c, boolean debug) {
        mContext=c;
        loadSafFile();
    }

    public SafManager(boolean usb_no_mp, Context c, boolean debug) {
        mContext=c;
        if (usb_no_mp) loadSafFileNoUsbMountPoint();
        else loadSafFile();
    }

    public boolean isSdcardMounted(){
        boolean result=false;
        if (sdcardRootDirectory.equals(UNKNOWN_SDCARD_DIRECTORY)) result=false;
        else result=true;
//        msg_array+="isSdcardMounted result="+result+"\n";
        return result;
    }

    public boolean isRootTreeUri(Uri uri) {
        boolean result=false;
        String uuid=getUuidFromUri(uri.toString());
        if (!uuid.startsWith("primary")) {
            if (uri.toString().endsWith("%3A") || uri.toString().endsWith(":")) result=true;
        }
//		Log.v("","uuid="+uuid+", uri="+uri.toString()+", result="+result);
//        msg_array+="isRootTreeUri result="+result+", uuid="+uuid+"\n";
        return result;
    }

    private String getExternalSdcardMountPoint() {
//        File[] fl= ContextCompat.getExternalFilesDirs(mContext, null);
        File[] fl= mContext.getExternalFilesDirs(null);
        String ld= LocalMountPoint.getExternalStorageDir();
        String esd=UNKNOWN_SDCARD_DIRECTORY;
        if (fl!=null) {
            for(File f:fl) {
                if (f!=null && f.getPath()!=null && !f.getPath().startsWith(ld)) {
                    esd=f.getPath().substring(0, f.getPath().indexOf("/Android/data"));
                    break;
                }
            }
        }
        if (esd.equals(UNKNOWN_SDCARD_DIRECTORY)) {
            if (isFilePathExists("/storage/MicroSD", true)) esd="/storage/MicroSD";
            else if (isFilePathExists("/storage/sdcard1", true)) esd="/storage/sdcard1";
            else if (isFilePathExists("/sdcard1", true)) esd="/sdcard1";
            else if (isFilePathExists("/mnt/extSdCard", true)) esd="/mnt/extSdCard";
            else if (isFilePathExists("/storage/extSdCard", true)) esd="/storage/extSdCard";
            else if (isFilePathExists("/mnt/SD1", true)) esd="/mnt/SD1";
        }
//        msg_array+="getExternalSdcardMountPoint path="+esd+"\n";
        return esd;
    }

    public boolean hasExternalMediaPath() {//  /storage/xxxx-xxxx形式のみチェック
//        File[] fl= ContextCompat.getExternalFilesDirs(mContext, null);
        File[] fl= mContext.getExternalFilesDirs(null);
        String ld= LocalMountPoint.getExternalStorageDir();
        String esd="";
        if (fl!=null) {
            for(File f:fl) {
                if (f!=null && f.getPath()!=null && !f.getPath().startsWith(ld)) {
                    String path=f.getPath().substring(0, f.getPath().indexOf("/Android/data"));
                    if (isFilePathExists(path, false)) esd=path;
                    break;
                }
            }
        }
        return esd.equals("")?false:true;
    }

    public boolean hasExternalSdcardPath() {
//        File[] fl= ContextCompat.getExternalFilesDirs(mContext, null);
        File[] fl= mContext.getExternalFilesDirs(null);
        String ld= LocalMountPoint.getExternalStorageDir();
        String esd="";
        if (fl!=null) {
            for(File f:fl) {
                if (f!=null && f.getPath()!=null && !f.getPath().startsWith(ld)) {
                    String path=f.getPath().substring(0, f.getPath().indexOf("/Android/data"));
                    if (isFilePathExists(path, false)) esd=path;
                    break;
                }
            }
        }
        if (esd.equals("")) {
            if (isFilePathExists("/storage/MicroSD", false)) esd="/storage/MicroSD";
            else if (isFilePathExists("/storage/sdcard1", false)) esd="/storage/sdcard1";
            else if (isFilePathExists("/sdcard1", false)) esd="/sdcard1";
            else if (isFilePathExists("/mnt/extSdCard", false)) esd="/mnt/extSdCard";
            else if (isFilePathExists("/storage/extSdCard", false)) esd="/storage/extSdCard";
        }
//        msg_array+="hasExternalSdcardPath path="+esd+"\n";
        return esd.equals("")?false:true;
    }

    private static boolean isFilePathExists(String fp, boolean read) {
        boolean result=false;
        File lf=new File(fp);
        if (read && lf.exists() && lf.canRead()) result=true;
        if (!read && lf.exists() ) result=true;
        return result;
    }

    public String getSdcardUuid() {
        return sdcardRootUuid;
    }

    public String getUsbUuid() {
        return usbRootUuid;
    }
    public void loadSafFile() {
        if (Build.VERSION.SDK_INT<=29) loadSafFileApi29();
        else loadSafFileApi30();
    }

    private void loadSafFileApi30() {
        sdcardRootDirectory=UNKNOWN_SDCARD_DIRECTORY;
        sdcardRootSafFile=null;
        sdcardRootUuid=null;
        ArrayList<String> sdcard_uuids=getSdcardUuidFromStorageManager(mContext, true);
        if (sdcard_uuids.size()>0) {
            for(String item:sdcard_uuids) {
                if (isFilePathExists("/storage/"+item, true)) {
                    sdcardRootDirectory="/storage/"+item;
                    sdcardRootUuid=item;
                }
            }
        }

        usbRootDirectory=UNKNOWN_USB_DIRECTORY;
        usbRootSafFile=null;
        usbRootUuid=null;
        ArrayList<String> usb_uuids=getUsbUuidFromStorageManager(mContext, true);
        if (usb_uuids.size()>0) {
            for(String item:usb_uuids) {
                if (isFilePathExists("/storage/"+item, true)) {
                    usbRootDirectory="/storage/"+item;
                    usbRootUuid=item;
                }
            }
        }
    }

    private void loadSafFileApi29() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String uuid_list=prefs.getString(SDCARD_UUID_KEY, "");
        sdcardRootDirectory=UNKNOWN_SDCARD_DIRECTORY;
        sdcardRootSafFile=null;
        sdcardRootUuid=null;

        if (!uuid_list.equals("")) {
            String[] uuid_array=uuid_list.split(",");
            for(String uuid:uuid_array) {
                if (!isUsbUuid(uuid)) {
                    SafFile sf= SafFile.fromTreeUri(mContext, Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"));
                    sdcardRootUuid=uuid;
                    if ((sf!=null && sf.getName()!=null)) {
                        String esd="";
                        if (Build.VERSION.SDK_INT>=23) {//for Huawei mediapad
                            esd="/storage/"+uuid;
                        } else {
                            esd= getExternalSdcardMountPoint();
                        }
                        if (esd!=null && !esd.equals("")) {
                            File mp=new File(esd);
                            if (mp.exists()) {
                                sdcardRootSafFile=sf;
                                sdcardRootDirectory=esd;
//                            msg_array+="locadSafFile SDCARD uuid found, uuid="+uuid+"\n";
                                break;
                            }
                        }
                    }
                }
            }
        }
        uuid_list=prefs.getString(USB_UUID_KEY, "");
        usbRootDirectory=UNKNOWN_USB_DIRECTORY;
        usbRootSafFile=null;
        usbRootUuid=null;
        if (!uuid_list.equals("")) {
            String[] uuid_array=uuid_list.split(",");
            for(String uuid:uuid_array) {
                if (isUsbUuid(uuid)) {
                    SafFile sf= SafFile.fromTreeUri(mContext, Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"));
                    usbRootUuid=uuid;
                    if (sf!=null && sf.getName()!=null) {
                        File ufp=new File("/storage/"+uuid);
                        if (ufp.exists()) {
                            usbRootDirectory="/storage/"+uuid;
                            usbRootSafFile=sf;
                            putInfoMessage("loadSafFile USB uuid found, uuid="+uuid);
                        } else {
                            putErrorMessage("loadSafFile USB uuid found but mount point does not exists, uuid="+uuid);
                        }

                        break;
                    }
                }
            }
        }
    }

    public void loadSafFileNoUsbMountPoint() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String uuid_list=prefs.getString(SDCARD_UUID_KEY, "");
        sdcardRootDirectory=UNKNOWN_SDCARD_DIRECTORY;
        sdcardRootSafFile=null;
        sdcardRootUuid=null;

        if (!uuid_list.equals("")) {
            String[] uuid_array=uuid_list.split(",");
            for(String uuid:uuid_array) {
                if (!isUsbUuid(uuid)) {
                    SafFile sf= SafFile.fromTreeUri(mContext, Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"));
                    if (sf!=null && sf.getName()!=null) {
                        sdcardRootUuid=uuid;
                        String esd="";
                        if (Build.VERSION.SDK_INT>=23) {//for Huawei mediapad
                            esd="/storage/"+uuid;
                        } else {
                            esd= getExternalSdcardMountPoint();
                        }
                        if (esd!=null && !esd.equals("")) {
                            File mp=new File(esd);
                            if (mp.exists()) {
                                sdcardRootSafFile=sf;
                                sdcardRootDirectory=esd;
//                            msg_array+="locadSafFile SDCARD uuid found, uuid="+uuid+"\n";
                                break;
                            }
                        }
                    }
                }
            }
        }
        uuid_list=prefs.getString(USB_UUID_KEY, "");
        usbRootDirectory=UNKNOWN_USB_DIRECTORY;
        usbRootSafFile=null;
        usbRootUuid=null;
        if (!uuid_list.equals("")) {
            String[] uuid_array=uuid_list.split(",");
            for(String uuid:uuid_array) {
                if (isUsbUuid(uuid)) {
                    SafFile sf= SafFile.fromTreeUri(mContext, Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"));
                    if (sf!=null && sf.getName()!=null) {
                        usbRootDirectory="/usbMedia/"+uuid;
                        usbRootSafFile=sf;
                        usbRootUuid=uuid;
                        putInfoMessage("loadSafFileNoUsbMountPoint USB uuid found, uuid="+uuid);
                        break;
                    }
                }
            }
        }
    }

    public SafFile getSdcardRootSafFile() {
        return sdcardRootSafFile;
    }

    public String getSdcardRootPath() {
        return sdcardRootDirectory;
    }

    public SafFile getUsbRootSafFile() {
        return usbRootSafFile;
    }

    public String getUsbRootPath() {
        return usbRootDirectory;
    }

    public boolean isSdcardFilePath(String file_path) {
        if (file_path.startsWith(getSdcardRootPath())) return true;
        else {
            if (file_path.equals(getSdcardRootPath())) return  true;
        }
        return false;
    }

    public boolean isUsbFilePath(String file_path) {
        if (file_path.startsWith(getUsbRootPath())) return true;
        else {
            if (file_path.equals(getUsbRootPath())) return  true;
        }
        return false;
    }

    public void saveSdcardUuidList(String uuid) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String sdcard_uuid_data=prefs.getString(SDCARD_UUID_KEY, uuid);
        if (!sdcard_uuid_data.equals("")) {
            ArrayList<String>sdcard_uuid_list=new ArrayList<String>();
            String[] sdcard_uuid_array=sdcard_uuid_data.split(",");
            for(String item:sdcard_uuid_array) {
                sdcard_uuid_list.add(item);
            }
            if (!sdcard_uuid_list.contains(uuid)) sdcard_uuid_list.add(uuid);
            String sd="", sep="";
            for(String item:sdcard_uuid_list) {
                sd+=sep+item;
                sep=",";
            }
            prefs.edit().putString(SDCARD_UUID_KEY, sd).commit();
//            msg_array+="saveSdcardUuidList successfull, uuids="+sd+"\n";
        } else {
            prefs.edit().putString(SDCARD_UUID_KEY, uuid).commit();
//            msg_array+="saveSdcardUuidList successfull, uuids="+uuid+"\n";
        }
    }

    public void saveUsbUuidList(String uuid) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String usb_uuid_data=prefs.getString(USB_UUID_KEY, "");
        if (!usb_uuid_data.equals("")) {
            ArrayList<String>usb_uuid_list=new ArrayList<String>();
            String[] usb_uuid_array=usb_uuid_data.split(",");
            for(String item:usb_uuid_array) {
                usb_uuid_list.add(item);
            }
            if (!usb_uuid_list.contains(uuid)) usb_uuid_list.add(uuid);
            String sd="", sep="";
            for(String item:usb_uuid_list) {
                sd+=sep+item;
                sep=",";
            }
            prefs.edit().putString(USB_UUID_KEY, sd).commit();
//            msg_array+="saveUsbUuidList successfull, uuids="+sd+"\n";
        } else {
            prefs.edit().putString(USB_UUID_KEY, uuid).commit();
//            msg_array+="saveUsbUuidList successfull, uuids="+uuid+"\n";
        }
    }

    public static String getUuidFromUri(String uri) {
        String result="";
        try {
            int semicolon = uri.lastIndexOf("%3A");
            if (semicolon>0) result=uri.substring(uri.lastIndexOf("/")+1,semicolon);
            else result=uri.substring(uri.lastIndexOf("/")+1,uri.length()-3);
        } catch(Exception e) {}
//		Log.v("","result="+result);
        return result;
    }

    public static String getFileNameFromPath(String fpath) {
        String result="";
        String[] st=fpath.split("/");
        if (st!=null) {
            if (st[st.length-1]!=null) result=st[st.length-1];
        }
        return result;
    }

    public boolean addSdcardUuid(Uri uri) {
        boolean result=true;
        String uuid=getUuidFromUri(uri.toString());
        if (uuid.length()>0) result=addSdcardUuid(uuid);
        return result;
    }

    public boolean isUsbUuid(String uuid) {
        boolean result=false;
        if (Build.VERSION.SDK_INT>=23) {
//            File usb=new File("/storage/"+uuid);
//            boolean exists=usb.exists();
//            boolean read=usb.canRead();
//            if ((exists && !read) || (!exists)) result=true;
//            else result=false;
            result=true;
//            ArrayList<String> sdcard_uuids=getSdcardUuidFromStorageManager(mContext, true);
//            if (sdcard_uuids.size()>0) {
//                if (sdcard_uuids.contains(uuid)) result=false;
//            }
            ArrayList<String> sdcard_uuids=getSdcardUuidFromStorageManager(mContext, true);
            if (sdcard_uuids.size()>0) {
                if (sdcard_uuids.contains(uuid)) result=false;
            }
            putInfoMessage("isUsbUuid uuid="+uuid+", result="+result);
            return result;
        } else {
            if (hasExternalSdcardPath()) result=false;
            else result=true;
        }
        return result;
    }

    private ArrayList<String> getSdcardUuidFromStorageManager(Context context, boolean debug) {
        ArrayList<String> uuids = new ArrayList<String>();
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList = sm.getClass().getDeclaredMethod("getVolumeList");
            Object[] volumeList = (Object[]) getVolumeList.invoke(sm);
            for (Object volume : volumeList) {
//                Method getPath = volume.getClass().getDeclaredMethod("getPath");
	            Method isRemovable = volume.getClass().getDeclaredMethod("isRemovable");
                Method isPrimary = volume.getClass().getDeclaredMethod("isPrimary");
                Method getUuid = volume.getClass().getDeclaredMethod("getUuid");
                Method toString = volume.getClass().getDeclaredMethod("toString");
                String desc=(String)toString.invoke(volume);
                Method getLabel = volume.getClass().getDeclaredMethod("getUserLabel");
                boolean primary=(boolean)isPrimary.invoke(volume);
                boolean removable=(boolean)isRemovable.invoke(volume);
                String uuid=(String) getUuid.invoke(volume);
                String label=(String) getLabel.invoke(volume);
//                String path = (String) getPath.invoke(volume);
                putInfoMessage("getSdcardUuidFromStorageManager uuid found="+uuid+", Label="+label);
//                if (uuid!=null && (!primary && !label.toLowerCase().contains("usb"))) {
                if (uuid!=null && removable && !primary) {
                    if (!label.toLowerCase().contains("usb")) {
                        boolean found=false;
                        for(String item:usbUuidList) {
                            if (uuid.equals(item)) {
                                found=true;
                                putInfoMessage("getSdcardUuidFromStorageManager SDCARD UUID ignored because USB UUID specified, UUID="+uuid);
                                break;
                            }
                        }
                        if (!found) {
                            uuids.add(uuid);
                            putInfoMessage("getSdcardUuidFromStorageManager added="+uuid);
                        }
                    }
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return uuids;
    }

    private ArrayList<String> getSdcardUuidFromStorageManagerOld(Context context, boolean debug) {
        ArrayList<String> uuids = new ArrayList<String>();
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList = sm.getClass().getDeclaredMethod("getVolumeList");
            Object[] volumeList = (Object[]) getVolumeList.invoke(sm);
            for (Object volume : volumeList) {
//                Method getPath = volume.getClass().getDeclaredMethod("getPath");
//	            Method isRemovable = volume.getClass().getDeclaredMethod("isRemovable");
                Method isPrimary = volume.getClass().getDeclaredMethod("isPrimary");
                Method getUuid = volume.getClass().getDeclaredMethod("getUuid");
                Method toString = volume.getClass().getDeclaredMethod("toString");
                String desc=(String)toString.invoke(volume);
                Method getLabel = volume.getClass().getDeclaredMethod("getUserLabel");
                String uuid=(String) getUuid.invoke(volume);
                String label=(String) getLabel.invoke(volume);
//                String path = (String) getPath.invoke(volume);
                putInfoMessage("getSdcardUuidFromStorageManager uuid found="+uuid+", Label="+label);
                if (uuid!=null && (label.contains("SD") || label.toLowerCase().contains("sdcard1") || label.equals("forZenPad") ||
                        label.contains("Speicherkarte") )) {
                    uuids.add(uuid);
                    putInfoMessage("getSdcardUuidFromStorageManager added="+uuid);
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return uuids;
    }

    private ArrayList<String> getUsbUuidFromStorageManager(Context context, boolean debug) {
        ArrayList<String> uuids = new ArrayList<String>();
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList = sm.getClass().getDeclaredMethod("getVolumeList");
            Object[] volumeList = (Object[]) getVolumeList.invoke(sm);
            for (Object volume : volumeList) {
//                Method getPath = volume.getClass().getDeclaredMethod("getPath");
	            Method isRemovable = volume.getClass().getDeclaredMethod("isRemovable");
                Method isPrimary = volume.getClass().getDeclaredMethod("isPrimary");
                Method getUuid = volume.getClass().getDeclaredMethod("getUuid");
                Method toString = volume.getClass().getDeclaredMethod("toString");
                String desc=(String)toString.invoke(volume);
                Method getLabel = volume.getClass().getDeclaredMethod("getUserLabel");
                String uuid=(String) getUuid.invoke(volume);
                String label=(String) getLabel.invoke(volume);
                boolean primary=(boolean)isPrimary.invoke(volume);
                boolean removable=(boolean)isRemovable.invoke(volume);
//                String path = (String) getPath.invoke(volume);
                putInfoMessage("getUsbUuidFromStorageManager uuid found="+uuid+", Label="+label);
                if (uuid!=null && removable && !primary) {
                    if (label.toLowerCase().contains("usb")) {
                        uuids.add(uuid);
                        putInfoMessage("getUsbUuidFromStorageManager added="+uuid);
                    } else {
                        for(String item:usbUuidList) {
                            if (uuid.equals(item)) {
                                uuids.add(uuid);
                                putInfoMessage("getUsbUuidFromStorageManager added by USB UUID list UUID="+uuid);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return uuids;
    }

    public boolean addSdcardUuid(final String uuid) {
        boolean result=true;
        putInfoMessage("addSdcardUuid uuif="+uuid);
        List<UriPermission> permissions = mContext.getContentResolver().getPersistedUriPermissions();
        for(UriPermission item:permissions) putInfoMessage(item.toString());
        if (isUsbUuid(uuid)) return result;
        try {
            mContext.getContentResolver().takePersistableUriPermission(
                    Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            saveSdcardUuidList(uuid);
            putInfoMessage("addSdcardUuid successfull");
            loadSafFile();
        } catch(Exception e) {
            putErrorMessage("addSdcardUuid error, uuid="+uuid+", Error="+e.getMessage());
            result=false;
        }
        return result;
    }

    public boolean isUsbMounted(){
        boolean result=false;
        if (getUsbRootSafFile()==null) result=false;
        else result=true;
//        msg_array+="isUsbMounted result="+result);
        return result;
    }

    public boolean addUsbUuid(Uri uri) {
        boolean result=true;
        String uuid=getUuidFromUri(uri.toString());
        if (uuid.length()>0) result=addUsbUuid(uuid);
        return result;
    }

    public boolean addUsbUuid(final String uuid) {
        boolean result=true;
        putInfoMessage("addUsbUuid uuif="+uuid);
        List<UriPermission> permissions = mContext.getContentResolver().getPersistedUriPermissions();
        for(UriPermission item:permissions) putInfoMessage(item.toString());
        try {
            mContext.getContentResolver().takePersistableUriPermission(
                    Uri.parse("content://com.android.externalstorage.documents/tree/"+uuid+"%3A"),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            saveUsbUuidList(uuid);
            putInfoMessage("addUsbUuid successfull");
            loadSafFile();
        } catch(Exception e) {
            putErrorMessage("addUsbUuid error, uuid="+uuid+", Error="+e.getMessage());
            result=false;
        }
        return result;
    }

    public SafFile createUsbItem(String target_path, boolean isDirectory) {
        return createItem(getUsbRootSafFile(), target_path, isDirectory);
    }
    public SafFile createUsbDirectory(String target_path) {
        return createItem(getUsbRootSafFile(), target_path, true);
    }
    public SafFile createUsbFile(String target_path) {
        return createItem(getUsbRootSafFile(), target_path, false);
    }

    public SafFile createSdcardItem(String target_path, boolean isDirectory) {
        return createItem(getSdcardRootSafFile(), target_path, isDirectory);
    }
    public SafFile createSdcardDirectory(String target_path) {
        return createItem(getSdcardRootSafFile(), target_path, true);
    }
    public SafFile createSdcardFile(String target_path) {
        return createItem(getSdcardRootSafFile(), target_path, false);
    }

    private SafFile createItem(SafFile rf, String target_path, boolean isDirectory) {
        ContentProviderClient client =null;
        SafFile saf=null;
        try {
            if (rf!=null) {
                client = mContext.getContentResolver().acquireContentProviderClient(rf.getUri().getAuthority());
                saf=createItem(client, rf, target_path, isDirectory);
            } else {
                putErrorMessage("createItem SafRoot file is null.");
            }
        } finally {
            if (client!=null) client.release();
        }
        return saf;
    }
    private SafFile createItem(ContentProviderClient client, SafFile rf, String target_path, boolean isDirectory) {
        SafFile parent=null;
//        clearMessages();
        if (slf4jLog.isDebugEnabled()) putDebugMessage("createItem target_path="+target_path+", isDirectory="+isDirectory);
//        List<UriPermission> permissions = mContext.getContentResolver().getPersistedUriPermissions();
//        for(UriPermission item:permissions) putDebugMessage(item.toString());

        if (rf==null) {
            putErrorMessage("createItem SafRoot file is null.");
            return null;
        }

        long b_time=System.currentTimeMillis();
        SafFile document=rf;

        String relativePath="";
        if (target_path.startsWith(sdcardRootDirectory)) {
            if (!target_path.equals(sdcardRootDirectory))
                relativePath=target_path.replace(sdcardRootDirectory+"/", "");
        } else {
            if (!target_path.equals(usbRootDirectory))
                relativePath=target_path.replace(usbRootDirectory+"/", "");
        }

        if (slf4jLog.isDebugEnabled()) putDebugMessage("rootUri="+rf.getUri()+", relativePath="+relativePath);

        try {
            if (!relativePath.equals("")) {
                String[] parts = relativePath.split("\\/");
                for (int i = 0; i < parts.length; i++) {
                    if (slf4jLog.isDebugEnabled()) putDebugMessage("parts="+parts[i]);
                    if (!parts[i].equals("")) {
                        SafFile nextDocument = document.findFile(client, parts[i]);
                        if (slf4jLog.isDebugEnabled()) putDebugMessage("findFile="+parts[i]+", result="+nextDocument);
                        if (nextDocument == null) {
                            if ((i < parts.length - 1) || isDirectory) {
                                String c_dir=parts[i];
                                nextDocument = document.createDirectory(c_dir);
                                if (slf4jLog.isDebugEnabled()) putDebugMessage("Directory was created name="+c_dir+", result="+nextDocument);
//                			Log.v("","saf="+document.getMsgArea());
                            } else {
                                nextDocument = document.createFile("", parts[i]);
                                if (slf4jLog.isDebugEnabled()) putDebugMessage("File was created name="+parts[i]+", result="+nextDocument);
                            }
                        }
                        parent=document;
                        document = nextDocument;
                        if (document!=null) {
                            document.setParentFile(parent);
                        }
                    }
                }
            }
        } catch(Exception e) {
            StackTraceElement[] st=e.getStackTrace();
            String stm="";
            for (int i=0;i<st.length;i++) {
                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
            }
            putErrorMessage("createItem Error="+e.getMessage()+stm);
        }
        if (slf4jLog.isDebugEnabled()) putDebugMessage("createItem elapsed="+(System.currentTimeMillis()-b_time));
        return document;
    }

    public SafFile findSdcardItem(String target_path) {
        return findItem(sdcardRootSafFile, target_path);
    }

    public SafFile findUsbItem(String target_path) {
        return findItem(usbRootSafFile, target_path);
    }

    private SafFile findItem(SafFile rf, String target_path) {
        SafFile parent=null;
        if (slf4jLog.isDebugEnabled()) putDebugMessage("findItem target_path="+target_path+", root name="+rf.getName());
//        List<UriPermission> permissions = mContext.getContentResolver().getPersistedUriPermissions();
//        for(UriPermission item:permissions) putDebugMessage(item.toString());

        long b_time=System.currentTimeMillis();
        SafFile document=rf;

        String relativePath="";
        if (target_path.startsWith(sdcardRootDirectory)) {
            if (!target_path.equals(sdcardRootDirectory))
                relativePath=target_path.replace(sdcardRootDirectory+"/", "");
        } else {
            if (!target_path.equals(usbRootDirectory))
                relativePath=target_path.replace(usbRootDirectory+"/", "");
        }

        if (slf4jLog.isDebugEnabled()) putDebugMessage("rootUri="+rf.getUri()+", relativePath="+relativePath);

        ContentProviderClient client =null;
        try {
            client = mContext.getContentResolver().acquireContentProviderClient(rf.getUri().getAuthority());
            if (!relativePath.equals("")) {
                String[] parts = relativePath.split("\\/");
                for (int i = 0; i < parts.length; i++) {
                    if (slf4jLog.isDebugEnabled()) putDebugMessage("parts="+parts[i]);
                    if (!parts[i].equals("")) {
                        SafFile nextDocument = document.findFile(client, parts[i]);
                        if (slf4jLog.isDebugEnabled()) putDebugMessage("findFile="+parts[i]+", result="+nextDocument);
                        if (nextDocument != null) {
                            parent=document;
                            document = nextDocument;
                            if (document!=null) {
                                document.setParentFile(parent);
                            }
                        } else {
                            document = null;
                            break;
                        }
                    }
                }
            }
        } catch(Exception e) {
            StackTraceElement[] st=e.getStackTrace();
            String stm="";
            for (int i=0;i<st.length;i++) {
                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
            }
            putErrorMessage("findItem Error="+e.getMessage()+stm);
        } finally {
            if (client!=null) client.release();
        }

        if (slf4jLog.isDebugEnabled()) putDebugMessage("findItem elapsed="+(System.currentTimeMillis()-b_time));
        return document;
    };

}

