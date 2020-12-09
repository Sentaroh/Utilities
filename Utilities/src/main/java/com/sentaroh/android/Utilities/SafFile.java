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
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class SafFile {
    private Context mContext;
    private Uri mUri;
    private String mPath="";
    private String mDocName;

    private SafFile mParentFile=null;

    private ArrayList<String> msg_array =new ArrayList<String>();

    private static Logger slf4jLog = LoggerFactory.getLogger(SafFile.class);

    private void putDebugMessage(String msg) {
        slf4jLog.debug(msg);
    }

    private void putInfoMessage(String msg) {
        slf4jLog.info(msg);
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

    public SafFile(Context context, Uri uri) {
        mContext = context;
        mUri = uri;
        mDocName=queryForString(mContext, mUri, DocumentsContract.Document.COLUMN_DISPLAY_NAME, null);
        mPath="/"+uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
    }

    public SafFile(Context context, Uri uri, String name) {
        mContext = context;
        mUri = uri;
        mDocName=name;
        mPath="/"+uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
    }

    public void setParentFile(SafFile parent) {mParentFile=parent;}
    public SafFile getParentFile() {return mParentFile;}

    public String getPath() { return mPath;}

    public static SafFile fromTreeUri(Context context, Uri treeUri) {
        return new SafFile(context, prepareTreeUri(treeUri));
    }

    public static Uri prepareTreeUri(Uri treeUri) {
        return DocumentsContract.buildDocumentUriUsingTree(treeUri,
                DocumentsContract.getTreeDocumentId(treeUri));
    }

    public String toString() {
        String result="Name="+mDocName+", Uri="+mUri.toString();
        if (mParentFile!=null) result+=", ParentUri="+mParentFile.getUri().toString();
        return result;
    }

    public SafFile createFile(String mimeType, String displayName) {
        Uri result=null;
        try {
            result=DocumentsContract.createDocument(mContext.getContentResolver(), mUri, mimeType, displayName);
//            putDebugMessage("SafFile#createFile result="+result);
        } catch (Exception e) {
            StackTraceElement[] st=e.getStackTrace();
            String stm="";
            for (int i=0;i<st.length;i++) {
                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
            }
            putErrorMessage("SafFile#createFile Failed to create file, Error="+e.getMessage()+stm);
        }
        SafFile saf=null;
        if (result != null) saf=new SafFile(mContext, result);
        return saf;
    }

    public SafFile createDirectory(String displayName) {
        Uri result=null;
        try {
            result=DocumentsContract.createDocument(mContext.getContentResolver(), mUri, DocumentsContract.Document.MIME_TYPE_DIR, displayName);
//            putDebugMessage("SafFile#createDirectory result="+result);
        } catch (Exception e) {
            StackTraceElement[] st=e.getStackTrace();
            String stm="";
            for (int i=0;i<st.length;i++) {
                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
            }
            putErrorMessage("SafFile#createDirectory Failed to create directory, Error="+e.getMessage()+stm);
        }
        return (result != null) ? new SafFile(mContext, result) : null;
    }

//    public static final String METHOD_CREATE_DOCUMENT = "android:createDocument";
//    public static final String EXTRA_URI = "uri";
//
//    public Uri createDocument(ContentProviderClient client, Uri parentDocumentUri,
//                                     String mimeType, String displayName) throws RemoteException {
//        final Bundle in = new Bundle();
//        in.putParcelable(EXTRA_URI, parentDocumentUri);
//        in.putString(DocumentsContract.Document.COLUMN_MIME_TYPE, mimeType);
//        in.putString(DocumentsContract.Document.COLUMN_DISPLAY_NAME, displayName);
//
//        final Bundle out = client.call(METHOD_CREATE_DOCUMENT, null, in);
//        return out.getParcelable(EXTRA_URI);
//    }

//    public Uri createDocument(ContentResolver cr, Uri parentDocumentUri,
//                              String mimeType, String displayName) throws RemoteException, FileNotFoundException {
//        return DocumentsContract.createDocument(cr, parentDocumentUri, mimeType, displayName);
//    }

    public Uri getUri() {
        return mUri;
    }

    public String getName() {
        return mDocName;
    }

    public String getType() {
        final String rawType = getRawType(mContext, mUri);
        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(rawType)) {
            return null;
        } else {
            return rawType;
        }
    }

    private String getRawType(Context context, Uri mUri) {
        return queryForString(context, mUri, DocumentsContract.Document.COLUMN_MIME_TYPE, null);
    }

    public boolean isDirectory() {
        return DocumentsContract.Document.MIME_TYPE_DIR.equals(getRawType(mContext, mUri));
    }

    public boolean isFile() {
        final String type = getRawType(mContext, mUri);
        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type) || TextUtils.isEmpty(type)) {
            return false;
        } else {
            return true;
        }
    }

    public long lastModified() {
        return queryForLong(mContext, mUri, DocumentsContract.Document.COLUMN_LAST_MODIFIED, 0);
    }

    public long length() {
        return queryForLong(mContext, mUri, DocumentsContract.Document.COLUMN_SIZE, 0);
    }

    public boolean canRead() {
        // Ignore if grant doesn't allow read
        if (mUri.getEncodedPath().endsWith(".android_secure")) return false;
        if (mContext.checkCallingOrSelfUriPermission(mUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        // Ignore documents without MIME
        if (TextUtils.isEmpty(getRawType(mContext, mUri))) {
            return false;
        }
        return true;
    }

    public boolean canWrite() {
        // Ignore if grant doesn't allow write
        if (mUri.getEncodedPath().endsWith(".android_secure")) return false;
        if (mContext.checkCallingOrSelfUriPermission(mUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        final String type = getRawType(mContext, mUri);
        final int flags = queryForInt(mContext, mUri, DocumentsContract.Document.COLUMN_FLAGS, 0);

        // Ignore documents without MIME
        if (TextUtils.isEmpty(type)) {
            return false;
        }

        // Deletable documents considered writable
        if ((flags & DocumentsContract.Document.FLAG_SUPPORTS_DELETE) != 0) {
            return true;
        }

        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type)
                && (flags & DocumentsContract.Document.FLAG_DIR_SUPPORTS_CREATE) != 0) {
            // Directories that allow create considered writable
            return true;
        } else if (!TextUtils.isEmpty(type)
                && (flags & DocumentsContract.Document.FLAG_SUPPORTS_WRITE) != 0) {
            // Writable normal files considered writable
            return true;
        }

        return false;
    }

    public boolean deleteIfExists() {
        boolean exists=false;
        boolean delete_success=false;
        ContentProviderClient client =null;
        try {
            client=mContext.getContentResolver().acquireContentProviderClient(getUri().getAuthority());
            deleteIfExists(client);
        } finally {
            if (client!=null) client.release();
        }
        return delete_success;
    }

    private boolean deleteIfExists(ContentProviderClient client) {
        boolean exists=false;
        boolean delete_success=false;
//        client=mContext.getContentResolver().acquireContentProviderClient(getUri().getAuthority());

        Cursor c = null;
        try {
            c = client.query(mUri, new String[] {DocumentsContract.Document.COLUMN_DOCUMENT_ID }, null, null, null);
            exists=c.getCount() > 0;
        } catch (Exception e) {
        } finally {
            closeQuietly(c);
        }
        if (exists) {
            try {
                final Bundle in = new Bundle();
                in.putParcelable(EXTRA_URI, getUri());
                client.call(METHOD_DELETE_DOCUMENT, null, in);
                delete_success=true;
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return delete_success;
    }


    public boolean delete() {
        try {
            return DocumentsContract.deleteDocument(mContext.getContentResolver(), mUri);
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public boolean exists() {
        final ContentResolver resolver = mContext.getContentResolver();

        Cursor c = null;
        try {
            c = resolver.query(mUri, new String[] {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID }, null, null, null);
            return c.getCount() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            closeQuietly(c);
        }
    }

    public SafFile[] listFiles() {
        final ArrayList<SafFileListInfo> result = listDocUris(mContext, mUri);
        final SafFile[] resultFiles = new SafFile[result.size()];
        for (int i = 0; i < result.size(); i++) {
            Uri childlen_uri = DocumentsContract.buildDocumentUriUsingTree(mUri, result.get(i).doc_id);
            resultFiles[i] = new SafFile(mContext, childlen_uri, result.get(i).doc_name);
            resultFiles[i].setParentFile(this);
        }
        return resultFiles;
    }

    public String[] list() {
        final ArrayList<SafFileListInfo> result = listDocUris(mContext, mUri);
        final String[] resultFiles = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {
            resultFiles[i] = this.getName()+"/"+result.get(i).doc_name;
        }
        return resultFiles;
    }

//    public SafFile findFile(String name) {
//        long b_time=System.currentTimeMillis();
//        final ContentResolver resolver = mContext.getContentResolver();
//        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(mUri, DocumentsContract.getDocumentId(mUri));
//
//        SafFile result=null;
//
//        Cursor c = null;
//        try {
//            c = resolver.query(childrenUri, new String[] {
//                            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
//                            DocumentsContract.Document.COLUMN_DISPLAY_NAME
//                    },
//                    DocumentsContract.Document.COLUMN_DISPLAY_NAME+"=?",
//                    new String[]{name},
//                    DocumentsContract.Document.COLUMN_DISPLAY_NAME+" ASC");
////            null,//"_display_name = ?",
////                    null,//new String[]{name},
////                    null);
//
////            putInfoMessage("SafFile#findFile name="+name+", count="+c.getCount());
//            while (c.moveToNext()) {
//                String doc_name=c.getString(1);
////                putInfoMessage("SafFile#findFile name="+doc_name+", key="+name);
//                if (doc_name.equals(name)) {
//                    String doc_id=c.getString(0);
//                    Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(mUri, doc_id);
//                    result=new SafFile(mContext,  documentUri, doc_name);
////                    result.setParent(this.getUri());
//                    break;
//                }
//            }
//        } catch (Exception e) {
////            Log.w("SafFile", "SafFile#findFile Failed query: " + e);
//            StackTraceElement[] st=e.getStackTrace();
//            String stm="";
//            for (int i=0;i<st.length;i++) {
//                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
//            }
//            putErrorMessage("SafFile#findFile Failed to Query, Error="+e.getMessage()+stm);
//        } finally {
//            closeQuietly(c);
//        }
////        putInfoMessage("SafFile#findFile elapased time="+(System.currentTimeMillis()-b_time));
//        return result;
//    }

    public SafFile findFile(ContentProviderClient cpc, String name) {
        long b_time=System.currentTimeMillis();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(mUri, DocumentsContract.getDocumentId(mUri));

        SafFile result=null;

        Cursor c = null;
        try {
            c = cpc.query(childrenUri, new String[] {
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME
                    },
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME+"=?",
                    new String[]{name},
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME+" ASC");
//            null,//"_display_name = ?",
//                    null,//new String[]{name},
//                    null);

//            putInfoMessage("SafFile#findFile name="+name+", count="+c.getCount());
            while (c.moveToNext()) {
                String doc_name=c.getString(1);
//                putInfoMessage("SafFile#findFile name="+doc_name+", key="+name);
                if (doc_name.equalsIgnoreCase(name)) {
                    String doc_id=c.getString(0);
                    Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(mUri, doc_id);
                    result=new SafFile(mContext,  documentUri, doc_name);
//                    result.setParent(this.getUri());
                    break;
                }
            }
        } catch (Exception e) {
//            Log.w("SafFile", "SafFile#findFile Failed query: " + e);
            StackTraceElement[] st=e.getStackTrace();
            String stm="";
            for (int i=0;i<st.length;i++) {
                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
            }
            putErrorMessage("SafFile#findFile Failed to Query, Error="+e.getMessage()+stm);
        } finally {
            closeQuietly(c);
        }
//        putInfoMessage("SafFile#findFile elapased time="+(System.currentTimeMillis()-b_time));
        return result;
    }

    private ArrayList<SafFileListInfo> listDocUris(Context context, Uri uri) {
        final ContentResolver resolver = context.getContentResolver();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getDocumentId(uri));
        final ArrayList<SafFileListInfo> results = new ArrayList<SafFileListInfo>();
        Cursor c = null;
        try {
            c = resolver.query(childrenUri, new String[] {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
            }, null, null, null);
            while (c.moveToNext()) {
                final String documentId = c.getString(0);
                final String documentName = c.getString(1);
                SafFileListInfo info=new SafFileListInfo();
                info.doc_name=documentName;
                info.doc_id=documentId;
                results.add(info);
            }
        } catch (Exception e) {
//            Log.w("SafFile", "SafFile#listDocUris Failed query: " + e);
            StackTraceElement[] st=e.getStackTrace();
            String stm="";
            for (int i=0;i<st.length;i++) {
                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
            }
            putErrorMessage("SafFile#listDocUris Failed to Query, Error="+e.getMessage()+stm);
        } finally {
            closeQuietly(c);
        }

        return results;
    }

    public boolean renameTo(String displayName) {
        Uri result=null;
        try {
            result = DocumentsContract.renameDocument(mContext.getContentResolver(), mUri, displayName);
            return true;
        } catch (FileNotFoundException e) {
            putErrorMessage("renameTo rename failed, msg="+e.getMessage());
            return false;
        }
    }

    public boolean moveTo(SafFile to_file) {
        Uri move_result=null;
        try {
            if (slf4jLog.isDebugEnabled()) putDebugMessage("moveTo mUri="+mUri.getPath()+", to_file="+to_file.getUri().getPath());
            move_result = DocumentsContract.moveDocument(mContext.getContentResolver(), mUri, getParentFile().getUri(), to_file.getParentFile().getUri());
            mUri = move_result;
            if (mUri!=null) {
                if (slf4jLog.isDebugEnabled()) putDebugMessage("moveTo result="+mUri.getPath());
                Uri rename_result=move_result;
                if (!getName().equalsIgnoreCase(to_file.getName())) {
                    if (to_file.exists()) to_file.delete();
                    try {
                        rename_result=DocumentsContract.renameDocument(mContext.getContentResolver(), mUri, to_file.getName());
                    } catch(Exception e) {
                        if (!to_file.exists()) {
                            if (slf4jLog.isDebugEnabled()) putDebugMessage("moveTo rename result="+rename_result.getPath());
                        }
                    }
                }
                return true;
            } else {
                putErrorMessage("moveTo move failed, to="+to_file);
                return false;
            }
        } catch (FileNotFoundException e) {
            putErrorMessage("moveTo move failed, msg="+e.getMessage());
            return false;
        }
    }

//    public boolean moveToCC(SafFile to_file) {
//        Uri move_result=null;
//        ContentProviderClient client =null;
//        try {
//            client=mContext.getContentResolver().acquireContentProviderClient(getUri().getAuthority());
//            moveToCC(client, to_file);
//            return true;
//        } finally {
//            if (client!=null) client.release();
//        }
////        return false;
//    }
//
//    public boolean moveToCC(ContentProviderClient client, SafFile to_file) {
//        Uri move_result=null;
//        try {
//            if (slf4jLog.isDebugEnabled()) putDebugMessage("moveTo mUri="+mUri.getPath()+", to_file="+to_file.getUri().getPath());
//            move_result = moveDocument(client, mUri, getParentFile().getUri(), to_file.getParentFile().getUri());
//            mUri = move_result;
//            if (mUri!=null) {
//                if (slf4jLog.isDebugEnabled()) putDebugMessage("moveTo result="+mUri.getPath());
//                Uri rename_result=move_result;
//                if (!getName().equals(to_file.getName())) {
//                    if (to_file.exists()) to_file.delete();
//                    rename_result=renameDocument(client, mUri, to_file.getName());
//                    if (slf4jLog.isDebugEnabled()) putDebugMessage("moveTo rename result="+rename_result.getPath());
//                }
//                return true;
//            } else {
//                putErrorMessage("moveTo move failed, to="+to_file);
//                return false;
//            }
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    private static final String METHOD_CREATE_DOCUMENT = "android:createDocument";
    private static final String METHOD_RENAME_DOCUMENT = "android:renameDocument";
    private static final String METHOD_DELETE_DOCUMENT = "android:deleteDocument";
    private static final String METHOD_COPY_DOCUMENT = "android:copyDocument";
    private static final String METHOD_MOVE_DOCUMENT = "android:moveDocument";
    private static final String METHOD_IS_CHILD_DOCUMENT = "android:isChildDocument";
    private static final String METHOD_REMOVE_DOCUMENT = "android:removeDocument";
    private static final String METHOD_UPLOAD_DOCUMENT = "android:uploadDocument";

    private static final String METHOD_COMPRESS_DOCUMENT = "android:compressDocument";
    private static final String METHOD_UNCOMPRESS_DOCUMENT = "android:uncompressDocument";

    private static final String EXTRA_PARENT_URI = "parentUri";
    private static final String EXTRA_URI = "uri";
    private static final String EXTRA_UPLOAD_URI = "upload_uri";
    private static final String EXTRA_THUMBNAIL_SIZE = "thumbnail_size";
    private static final String EXTRA_DOCUMENT_TO= "document_to";
    private static final String EXTRA_DELETE_AFTER = "delete_after";
    private static final String EXTRA_DOCUMENTS_COMPRESS = "documents_compress";
    private static final String EXTRA_DOCUMENTS_UNCOMPRESS = "documents_uncompress";

    private static final String EXTRA_TARGET_URI = "android.content.extra.TARGET_URI";

    private static final String PATH_ROOT = "root";
    private static final String PATH_RECENT = "recent";
    private static final String PATH_DOCUMENT = "document";
    private static final String PATH_CHILDREN = "children";
    private static final String PATH_SEARCH = "search";
    private static final String PATH_TREE = "tree";

    private static final String PARAM_QUERY = "query";
    private static final String PARAM_MANAGE = "manage";

    private static void deleteDocument(ContentProviderClient client, Uri documentUri)
            throws RemoteException {
        final Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, documentUri);
        client.call(METHOD_DELETE_DOCUMENT, null, in);
    }

    private static Uri moveDocument(ContentProviderClient client, Uri sourceDocumentUri,
                                   Uri sourceParentDocumentUri, Uri targetParentDocumentUri) throws RemoteException {
        final Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, sourceDocumentUri);
        in.putParcelable(EXTRA_PARENT_URI, sourceParentDocumentUri);
        in.putParcelable(EXTRA_TARGET_URI, targetParentDocumentUri);

        final Bundle out = client.call(METHOD_MOVE_DOCUMENT, null, in);
        return out.getParcelable(EXTRA_URI);
    }

    private static Uri renameDocument(ContentProviderClient client, Uri documentUri,
                                     String displayName) throws RemoteException {
        final Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, documentUri);
        in.putString(DocumentsContract.Document.COLUMN_DISPLAY_NAME, displayName);

        final Bundle out = client.call(METHOD_RENAME_DOCUMENT, null, in);
        final Uri outUri = out.getParcelable(EXTRA_URI);
        return (outUri != null) ? outUri : documentUri;
    }

    private String queryForString(Context context, Uri self, String column, String defaultValue) {
        final ContentResolver resolver = context.getContentResolver();

        Cursor c = null;
        try {
            c = resolver.query(self, new String[] { column }, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                return c.getString(0);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
//            StackTraceElement[] st=e.getStackTrace();
//            String stm="";
//            for (int i=0;i<st.length;i++) {
//                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
//            }
            putErrorMessage("SafFile#queryForString Failed to Query, Error="+e.getMessage());
            return defaultValue;
        } finally {
            closeQuietly(c);
        }
    }

    private int queryForInt(Context context, Uri self, String column, int defaultValue) {
        return (int) queryForLong(context, self, column, defaultValue);
    }

    private long queryForLong(Context context, Uri self, String column, long defaultValue) {
        final ContentResolver resolver = context.getContentResolver();

        Cursor c = null;
        try {
            c = resolver.query(self, new String[] { column }, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                return c.getLong(0);
            } else {
                return defaultValue;
            }
        } catch (Exception e) {
//            Log.w("SafFile", "SafFile#queryForLong Failed query: " + e);
//            StackTraceElement[] st=e.getStackTrace();
//            String stm="";
//            for (int i=0;i<st.length;i++) {
//                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
//            }
            putErrorMessage("SafFile#queryForLong Failed to Query, Error="+e.getMessage());
            return defaultValue;
        } finally {
            closeQuietly(c);
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    static class SafFileListInfo {
        public String doc_id;
        public String doc_name, doc_type;
        public boolean can_read, can_write;
        public long doc_last_modified;
        public long doc_length;
    }

}

