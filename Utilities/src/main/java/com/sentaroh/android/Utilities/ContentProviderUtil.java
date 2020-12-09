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

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContentProviderUtil {
    private static Logger slf4jLog = LoggerFactory.getLogger(ContentProviderUtil.class);
	@SuppressLint("NewApi")
	public static String getFilePath(Context c, String cache_dir, Uri content_uri) {
		if (content_uri==null) return null;
		final String[] column={MediaStore.MediaColumns._ID,  MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
		String cd=cache_dir;
		File tlf=null;
        slf4jLog.debug("getFilePath Uri="+content_uri.toString());
		if (ContentResolver.SCHEME_FILE.equals(content_uri.getScheme())) {
			//File
			tlf=new File(content_uri.getPath());
		} else if (content_uri.toString().startsWith("content://media/external/")) {
			//External 
			ContentResolver resolver = c.getContentResolver();
			Cursor cursor = resolver.query(content_uri, column, null, null, null);
		    if (cursor.moveToFirst()) {
		        String path=cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
		        tlf=new File(path);
		    }
		    cursor.close();
		} else if (content_uri.toString().startsWith("content://com.android.providers.downloads.documents")) {
			//Download
            final String id = getIdFromUri(content_uri);

            long uri_id=-1;
            try {
                uri_id=Long.valueOf(id);
            } catch(Exception e) {}
            if (uri_id==-1) return null;
            final Uri contentUri = ContentUris.withAppendedId(
	                    Uri.parse("content://downloads/public_downloads"), uri_id);
			
			String sel = MediaStore.Images.Media._ID + "=?";
			Cursor cursor = c.getContentResolver().query(contentUri, 
                    column, sel, new String[]{ id }, null);
			int columnIndex = cursor.getColumnIndex(column[1]);
			if (cursor.moveToFirst()) {
				String path = cursor.getString(columnIndex);
				tlf=new File(path);
			}
		    cursor.close();
        } else if (content_uri.toString().startsWith("content://downloads/all_downloads")) {
            //Download
            final String id = getIdFromUri(content_uri);
            long uri_id=-1;
            try {
                uri_id=Long.valueOf(id);
            } catch(Exception e) {}
            if (uri_id==-1) return null;

            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/all_downloads"), Long.valueOf(id));

            String sel = MediaStore.Images.Media._ID + "=?";
            Cursor cursor = c.getContentResolver().query(contentUri,
                    column, sel, new String[]{ id }, null);
            int columnIndex = cursor.getColumnIndex(column[1]);
            if (cursor.moveToFirst()) {
                try {
                    String path = cursor.getString(columnIndex);
                    tlf=new File(path);
                } catch(Exception e) {}
            }
            cursor.close();
//        } else if (content_uri.toString().startsWith("content://com.asus.filemanager.OpenFileProvider/file/storage/emulated/0/Android")) {
//            //Download
//            String tfp=content_uri.toString().replace("content://com.asus.filemanager.OpenFileProvider/file","");
//            tlf=new File(tfp);
        } else if (content_uri.toString().startsWith("content://")) {
            Cursor cursor = c.getContentResolver().query(content_uri, column, null, null, null);
            if (cursor!=null) {
                if (cursor.moveToFirst()) {
//				long id=cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
//    			Uri image_uri = ContentUris.withAppendedId(content_uri, Long.valueOf(id));
                    String t_file_name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                    String t_file_path = null;
                    try {
                        t_file_path =cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                    } catch(Exception e) {
                    }
//				String file_path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
//				Log.v("","data="+file_path);
                    if (t_file_path!=null && !t_file_path.equals("")) {
                        tlf=new File(t_file_path);
                    } else {
                        clearCacheFile(cd);
                        String file_name=t_file_name==null?"attachedFile":t_file_name;
                        tlf=new File(cd+file_name);
                        try {
                            InputStream is=c.getContentResolver().openInputStream(content_uri);//image_uri);
                            FileOutputStream fos=new FileOutputStream(tlf);
                            byte[] buff=new byte[1024*1024];
                            int rc=is.read(buff);
                            while(rc>0) {
                                fos.write(buff, 0, rc);
                                rc=is.read(buff);
                            }
                            fos.flush();
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                cursor.close();
            }
//        } else if (content_uri.toString().startsWith("content://com.fsck.k9.attachmentprovider") ||
//                content_uri.toString().startsWith("content://com.fsck.k9.tempfileprovider") ) {
//			Cursor cursor = c.getContentResolver().query(content_uri, column, null, null, null);
//			if (cursor.moveToFirst()) {
////				long id=cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
////    			Uri image_uri = ContentUris.withAppendedId(content_uri, Long.valueOf(id));
//				String file_name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
////				String file_path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
////				Log.v("","data="+file_path);
//				clearCacheFile(cd);
//				tlf=new File(cd+file_name);
//    			try {
//					InputStream is=c.getContentResolver().openInputStream(content_uri);//image_uri);
//					FileOutputStream fos=new FileOutputStream(tlf);
//					byte[] buff=new byte[1024*1024];
//					int rc=is.read(buff);
//					while(rc>0) {
//						fos.write(buff, 0, rc);
//						rc=is.read(buff);
//					}
//					fos.flush();
//					fos.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		    cursor.close();
//		} else if (content_uri.toString().startsWith("content://gmail")) {
//			Cursor cursor=c.getContentResolver().query(content_uri, column, null, null, null);
//			if (cursor.moveToFirst()) {
//				String file_name = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
//				clearCacheFile(cd);
//				tlf=new File(cd+file_name);
//    			try {
//					InputStream is=c.getContentResolver().openInputStream(content_uri);
//					FileOutputStream fos=new FileOutputStream(tlf);
//					byte[] buff=new byte[1024*1024];
//					int rc=is.read(buff);
//					while(rc>0) {
//						fos.write(buff, 0, rc);
//						rc=is.read(buff);
//					}
//					fos.flush();
//					fos.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
		}
        slf4jLog.debug("getFilePath Path="+tlf.getAbsolutePath());
		return tlf==null?null:tlf.getAbsolutePath();
	};

    private static String getIdFromUri(Uri uri) {
        String f_path=uri.getPath();
        String f_name=f_path.substring(f_path.lastIndexOf("/")+1);
        String id="";
        if (f_name.lastIndexOf(":")>=0) {
            id=f_name.substring(0,f_name.indexOf(":")-1);
        } else {
            id=f_name;
        }
//        Log.v("","path="+f_path+", name="+f_name+", id="+id);
        return id;
    }

    private static void clearCacheFile(String cd) {
		File df=new File(cd);
		File[] cf_list=df.listFiles();
		if (cf_list!=null && cf_list.length>0) {
			for(File ch_file:cf_list) {
				ch_file.delete();
			}
		}
		File dlf=new File(cd);
		dlf.mkdirs();
	};
}
