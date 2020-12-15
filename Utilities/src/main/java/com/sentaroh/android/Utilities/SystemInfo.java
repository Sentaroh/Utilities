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
package com.sentaroh.android.Utilities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.UriPermission;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SystemInfo {
    static public String getApplVersionName(Context c) {
        String vn = "Unknown";
        try {
            String packegeName = c.getPackageName();
            PackageInfo packageInfo = c.getPackageManager().getPackageInfo(packegeName, PackageManager.GET_META_DATA);
            vn = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            //
        }
        return vn;
    }
    static public String getApplVersionNameCode(Context c) {
        String vn = "Unknown";
        int vc=-1;
        try {
            String packegeName = c.getPackageName();
            PackageInfo packageInfo = c.getPackageManager().getPackageInfo(packegeName, PackageManager.GET_META_DATA);
            vn = packageInfo.versionName;
            vc = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            //
        }
        return vn+"("+vc+")";
    }

    static public ArrayList<String> listSystemInfo(Context c, SafManager safMgr) {
        ArrayList<String> out=new ArrayList<String>();
        out.add("System information Application="+ getApplVersionNameCode(c) + ", API=" + Build.VERSION.SDK_INT);

        out.add("  Manufacturer="+ Build.MANUFACTURER+", Model="+Build.MODEL);

        out.addAll(listsMountPoint());

        out.add("usbUuidList=" + safMgr.getUsbUuidList());

        out.add("getSdcardRootPath=" + safMgr.getSdcardRootPath());

        out.add("getUsbRootPath=" + safMgr.getUsbRootPath());

        out.add("getExternalStorageDirectory="+LocalMountPoint.getExternalStorageDir());

//        File[] fl = ContextCompat.getExternalFilesDirs(c, null);
        File[] fl = c.getExternalFilesDirs(null);
        out.add("ExternalFilesDirs :");
        if (fl != null) {
            for (File f : fl) {
                if (f != null) out.add("  " + f.getPath());
            }
        }
        if (safMgr.getSdcardRootSafFile() != null)
            out.add("getSdcardSafFile name=" + safMgr.getSdcardRootSafFile().getName());

        out.add("Uri permissions:");
        List<UriPermission> permissions = c.getContentResolver().getPersistedUriPermissions();
        for(UriPermission item:permissions) out.add("   "+ SafManager.getUuidFromUri(item.getUri().toString())+", read="+item.isReadPermission()+", write="+item.isWritePermission());

        out.addAll(getRemovableStoragePaths(c, true));
        out.add("Storage information end");

        if (Build.VERSION.SDK_INT >= 23) {
            String packageName = c.getPackageName();
            PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
            if (pm.isIgnoringBatteryOptimizations(packageName)) {
                out.add("isIgnoringBatteryOptimizations()=true");
            } else {
                out.add("isIgnoringBatteryOptimizations()=false");
            }
        } else {
            out.add("Battery optimization=false");
        }

        try {
            ContentResolver contentResolver = c.getContentResolver();
            int policy = Settings.System.getInt(contentResolver, Settings.Global.WIFI_SLEEP_POLICY);
            switch (policy) {
                case Settings.Global.WIFI_SLEEP_POLICY_DEFAULT:
                    // スリープ中のWiFi接続を維持しない
                    out.add("WIFI_SLEEP_POLICY_DEFAULT");
                    break;
                case Settings.Global.WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED:
                    // スリープ中のWiFi接続を電源接続時にのみ維持する
                    out.add("WIFI_SLEEP_POLICY_NEVER_WHILE_PLUGGED");
                    break;
                case Settings.Global.WIFI_SLEEP_POLICY_NEVER:
                    // スリープ中のWiFi接続を常に維持する
                    out.add("WIFI_SLEEP_POLICY_NEVER");
                    break;
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }

        return out;
    }

    static private ArrayList<String> getRemovableStoragePaths(Context context, boolean debug) {
        ArrayList<String> out=new ArrayList<String>();
        out.add("Storage Manager:");
        try {
            StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            if (Build.VERSION.SDK_INT>=24){// && Build.VERSION.SDK_INT<=28) {
                List<StorageVolume> svl=sm.getStorageVolumes();
                for(StorageVolume item:svl)
                    out.add("  "+item.getDescription(context)+", isPrimary="+item.isPrimary()+", isRemovable="+item.isRemovable()+", uuid="+item.getUuid());
            } else {
                Method getVolumeList = sm.getClass().getDeclaredMethod("getVolumeList");
                Object[] volumeList = (Object[]) getVolumeList.invoke(sm);
                for (Object volume : volumeList) {
                    Method getPath = volume.getClass().getDeclaredMethod("getPath");
//	            Method isRemovable = volume.getClass().getDeclaredMethod("isRemovable");
                    Method isPrimary = volume.getClass().getDeclaredMethod("isPrimary");
                    Method getUuid = volume.getClass().getDeclaredMethod("getUuid");
                    Method getId = volume.getClass().getDeclaredMethod("getId");
                    Method toString = volume.getClass().getDeclaredMethod("toString");
//                Method allowMassStorage = volume.getClass().getDeclaredMethod("allowMassStorage");
//                Method getStorageId = volume.getClass().getDeclaredMethod("getStorageId");
                    String path = (String) getPath.invoke(volume);
//	            boolean removable = (Boolean)isRemovable.invoke(volume);
//                mpi+="allowMassStorage="+(boolean) allowMassStorage.invoke(volume)+"\n";
//                mpi+="getStorageId="+String.format("0x%8h",((int) getStorageId.invoke(volume)))+"\n";
                    out.add("  "+((String)toString.invoke(volume)+", isPrimary="+(boolean)isPrimary.invoke(volume)));
//	            if ((String)getUuid.invoke(volume)!=null) {
//	            	paths.add(path);
//					if (debug) {
////						Log.v(APPLICATION_TAG, "RemovableStorages Uuid="+(String)getUuid.invoke(volume)+", removable="+removable+", path="+path);
//						mUtil.addLogMsg("I", (String)toString.invoke(volume));
//					}
//	            }
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
        return out;
    }

    static private ArrayList<String> listsMountPoint() {
        ArrayList<String> out=new ArrayList<String>();
        out.add("/ directory:");
        File[] fl = (new File("/")).listFiles();
        if (fl != null) {
            for (File item : fl) {
                if (item.isDirectory())
                    out.add("   /" + item.getName() + ", read=" + item.canRead()+", write="+item.canWrite());
            }
        }

        out.add("/mnt directory:");
        fl = (new File("/mnt")).listFiles();
        if (fl != null) {
            for (File item : fl) {
                if (item.isDirectory())
                    out.add("   /mnt/" + item.getName() + ", read=" + item.canRead()+", write="+item.canWrite());
            }
        }

        out.add("/storage directory:");
        fl = (new File("/storage")).listFiles();
        if (fl != null) {
            for (File item : fl) {
                if (item.isDirectory())
                    out.add("   /storage/" + item.getName() + ", read=" + item.canRead()+", write="+item.canWrite());
            }
        }

        out.add("/storage/emulated/0 directory:");
        File lf = new File("/storage/emulated/0");
        try {
            if (lf.exists()) out.add("   /storage/emulated/0" + ", read=" + lf.canRead()+", write="+lf.canWrite());
        } catch(Exception e) {}

        out.add("/Removable directory:");
        fl = (new File("/Removable")).listFiles();
        if (fl != null) {
            for (File item : fl) {
                if (item.isDirectory())
                    out.add("   /Removable/" + item.getName() + ", read=" + item.canRead()+", write="+item.canWrite());
            }
        }
        return out;
    }

}
