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

    SafFile(Context context, Uri uri) {
        mContext = context;
        mUri = uri;
        mDocName=queryForString(mContext, mUri, DocumentsContract.Document.COLUMN_DISPLAY_NAME, null);
        mPath="/"+uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
    }

    SafFile(Context context, Uri uri, String name) {
        mContext = context;
        mUri = uri;
        mDocName=name;
        mPath="/"+uri.getPath().substring(uri.getPath().lastIndexOf(":")+1);
    }

    void setParentFile(SafFile parent) {mParentFile=parent;}
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
            putDebugMessage("SafFile#createFile result="+result);
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
        putDebugMessage("SafFile#createFile SafFile="+saf);
        return saf;
    }

    public SafFile createDirectory(String displayName) {
        Uri result=null;
        if (Build.VERSION.SDK_INT>=28) {
            try {
                result=DocumentsContract.createDocument(mContext.getContentResolver(), mUri, DocumentsContract.Document.MIME_TYPE_DIR, displayName);
            } catch (Exception e) {
//                Log.w("SafFile", "Failed to create directory", e);
                StackTraceElement[] st=e.getStackTrace();
                String stm="";
                for (int i=0;i<st.length;i++) {
                    stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
                }
                putErrorMessage("SafFile#createDirectory Failed to create directory, Error="+e.getMessage()+stm);
            }
        } else {
            ContentProviderClient client = mContext.getContentResolver().acquireUnstableContentProviderClient(mUri.getAuthority());
            try {
                result=createDocument(client, mUri, DocumentsContract.Document.MIME_TYPE_DIR, displayName);
            } catch (Exception e) {
//                Log.w("SafFile", "Failed to create directory", e);
                StackTraceElement[] st=e.getStackTrace();
                String stm="";
                for (int i=0;i<st.length;i++) {
                    stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
                }
                putErrorMessage("SafFile#createDirectory Failed to create directory, Error="+e.getMessage()+stm);
            } finally {
                client.release();
            }
        }
        return (result != null) ? new SafFile(mContext, result) : null;
    }

    public static final String METHOD_CREATE_DOCUMENT = "android:createDocument";
    public static final String EXTRA_URI = "uri";

    public Uri createDocument(ContentProviderClient client, Uri parentDocumentUri,
                                     String mimeType, String displayName) throws RemoteException {
        final Bundle in = new Bundle();
        in.putParcelable(EXTRA_URI, parentDocumentUri);
        in.putString(DocumentsContract.Document.COLUMN_MIME_TYPE, mimeType);
        in.putString(DocumentsContract.Document.COLUMN_DISPLAY_NAME, displayName);

        final Bundle out = client.call(METHOD_CREATE_DOCUMENT, null, in);
        return out.getParcelable(EXTRA_URI);
    }

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
//            Log.w("SafFile", "SafFile#exists Failed query: " + e);
            StackTraceElement[] st=e.getStackTrace();
            String stm="";
            for (int i=0;i<st.length;i++) {
                stm+="\n at "+st[i].getClassName()+"."+ st[i].getMethodName()+"("+st[i].getFileName()+ ":"+st[i].getLineNumber()+")";
            }
            putErrorMessage("SafFile#exists Failed to Query, Error="+e.getMessage()+stm);
            return false;
        } finally {
            closeQuietly(c);
        }
    }

    public SafFile[] listFiles() {
        final ArrayList<SafFileListInfo> result = listDocUris(mContext, mUri);
        final SafFile[] resultFiles = new SafFile[result.size()];
        for (int i = 0; i < result.size(); i++) {
            resultFiles[i] = new SafFile(mContext, result.get(i).doc_uri, result.get(i).doc_name);
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

    public SafFile findFile(String name) {
        final ContentResolver resolver = mContext.getContentResolver();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(mUri, DocumentsContract.getDocumentId(mUri));

        SafFile result=null;

        Cursor c = null;
        try {
            c = resolver.query(childrenUri, new String[] {
                            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                            DocumentsContract.Document.COLUMN_DISPLAY_NAME
                    },
                    null,//"_display_name = ?",
                    null,//new String[]{name},
                    null);

            while (c.moveToNext()) {
                String doc_name=c.getString(1);
                if (doc_name.equals(name)) {
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
        return result;
    }

    private ArrayList<SafFileListInfo> listDocUris(Context context, Uri uri) {
        final ContentResolver resolver = context.getContentResolver();
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri,
                DocumentsContract.getDocumentId(uri));
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
                final Uri documentUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                        documentId);
                SafFileListInfo info=new SafFileListInfo();
                info.doc_name=documentName;
                info.doc_uri=documentUri;
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
            putDebugMessage("moveTo mUri="+mUri.getPath()+", to_file="+to_file.getUri().getPath());
            move_result = DocumentsContract.moveDocument(mContext.getContentResolver(), mUri, getParentFile().getUri(), to_file.getParentFile().getUri());
            mUri = move_result;
            if (mUri!=null) {
                putDebugMessage("moveTo result="+mUri.getPath());
                Uri rename_result=move_result;
                if (!getName().equals(to_file.getName())) {
                    if (to_file.exists()) to_file.delete();
                    rename_result=DocumentsContract.renameDocument(mContext.getContentResolver(), mUri, to_file.getName());
                    putDebugMessage("moveTo rename result="+rename_result.getPath());
                }
                return true;
            } else {
                putErrorMessage("moveTo move failed");
                return false;
            }
        } catch (FileNotFoundException e) {
            putErrorMessage("moveTo move failed, msg="+e.getMessage());
            return false;
        }
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
        public Uri doc_uri;
        public String doc_name, doc_type;
        public boolean can_read, can_write;
        public long doc_last_modified;
        public long doc_length;
    }

}

