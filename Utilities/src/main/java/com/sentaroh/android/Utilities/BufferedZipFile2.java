/*
The MIT License (MIT)
Copyright (c) 2017 Sentaroh

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

import net.lingala.zip4j.core.HeaderReader;
import net.lingala.zip4j.core.HeaderWriter;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.SplitOutputStream;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.CentralDirectory;
import net.lingala.zip4j.model.EndCentralDirRecord;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.CRCUtil;
import net.lingala.zip4j.util.InternalZipConstants;
import net.lingala.zip4j.util.Zip4jConstants;
import net.lingala.zip4j.util.Zip4jUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BufferedZipFile2 {
    private boolean closed =false;
    private boolean mInpuZipFileItemRemoved =false;
    private ZipFile mInputZipFile =null;
    private ZipFile mAddZipFile =null;
    private File mInputOsFile =null;
    private File mOutputOsFile =null;
    private File mTempOsFile =null, mAddOsFile =null;
    private ZipModel input_zip_model =null;
    private ZipModel add_zip_model =null;
    private ArrayList<BzfFileHeaderItem> mInputZipFileHeaderList =null;
    private ArrayList<BzfFileHeaderItem> mAddZipFileHeaderList =null;
    private OutputStream mOutputOsFileStream =null;
    private long mOutputZipFilePosition =0;
    private BufferedOutputStream mOutputZipFileStream =null;

    private static final int IO_AREA_SIZE=1024*1024;
    private ZipOutputStream mAddZipOutputStream =null;

    private String mEncoding =DEFAULT_ZIP_FILENAME_ENCODING;
    private static final String DEFAULT_ZIP_FILENAME_ENCODING="UTF-8";

    private static Logger slf4jLog = LoggerFactory.getLogger(BufferedZipFile2.class);

    class BzfFileHeaderItem {
        public FileHeader file_header;
        public boolean isRemovedItem=false;
        public long start_pos=0;
        public long end_pos=0;
        public boolean removed_entry=false;
    }

    public BufferedZipFile2(String input_path, String output_path) {
        File of=new File(output_path);
        init(new File(input_path), of, null, DEFAULT_ZIP_FILENAME_ENCODING, of.getParent());
    }

    public BufferedZipFile2(String input_path, String output_path, String encoding) {
        File of=new File(output_path);
        init(new File(input_path), of, null, encoding, of.getParent());
    }

    public BufferedZipFile2(File input_file, File output_file) {
        init(input_file, output_file, null, DEFAULT_ZIP_FILENAME_ENCODING, output_file.getParent());
    }

    public BufferedZipFile2(File input_file, File output_file, String encoding) {
        init(input_file, output_file, null, encoding, output_file.getParent());
    }

    public BufferedZipFile2(File input_file, OutputStream os, String wfp) {
        init(input_file, null, os, DEFAULT_ZIP_FILENAME_ENCODING, wfp);
    }

    public BufferedZipFile2(String input_path, OutputStream os, String encoding, String wfp) {
        init(new File(input_path), null, os, encoding, wfp);
    }

    public BufferedZipFile2(File input_file, OutputStream os, String encoding, String wfp) {
        init(input_file, null, os, encoding, wfp);
    }

    private boolean mGpfBit3On=false;
    public void setGpfBit3On(boolean on) {
        mGpfBit3On=on;
    }
    public Boolean isGpfBit3On() {
        return mGpfBit3On;
    }

    private boolean mEmptyInputZipFile=true;
    private void init(File input_file, File output_file, OutputStream os, String encoding, String work_file_path) {
        slf4jLog.debug("<init> Input="+input_file+", Output="+output_file+", Encoding="+encoding+", wfp="+work_file_path);
        mOutputOsFileStream=os;
        mEncoding =encoding;
        mInputOsFile =input_file;
        mOutputOsFile =output_file;
        mTempOsFile =new File(work_file_path+"/ziputility.tmp");
        mAddOsFile =new File(work_file_path+"/ziputility.add");
        mInputZipFileHeaderList =new ArrayList<BzfFileHeaderItem>();
        try {
            if (mInputOsFile==null || !mInputOsFile.exists()) {
            } else {
                mInputZipFile =new ZipFile(mInputOsFile);
                mInputZipFile.setFileNameCharset(encoding);
                RandomAccessFile raf =new RandomAccessFile(mInputOsFile,"r");
                HeaderReader header_reader=new HeaderReader(raf);
                try {
                    input_zip_model =header_reader.readAllHeaders(encoding);
                    ArrayList<FileHeader>file_header_list=(ArrayList<FileHeader>) input_zip_model.getCentralDirectory().getFileHeaders();
                    for(FileHeader fh:file_header_list) {
                        BufferedZipFile2.BzfFileHeaderItem rfhli=new BufferedZipFile2.BzfFileHeaderItem();
                        rfhli.file_header=fh;
                        mInputZipFileHeaderList.add(rfhli);
                        mEmptyInputZipFile=false;
                    }
                } catch (ZipException e) {
                    input_zip_model =new ZipModel();
                    input_zip_model.setEndCentralDirRecord(createEndOfCentralDirectoryRecord());
                    input_zip_model.setCentralDirectory(new CentralDirectory());
                    input_zip_model.getCentralDirectory().setFileHeaders(new ArrayList<FileHeader>());
                }
                try {raf.close();} catch(Exception e) {}
            }
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        dumpZipModel("Init", input_zip_model);
    }

    public void addItem(String input, ZipParameters zp) throws ZipException {
        addItem(new File(input), zp);
    }

    public void addItem(File input, ZipParameters zp) throws ZipException {
        checkClosed();
        if (mAddOsFile !=null && mTempOsFile !=null) {
            if (input.getPath().equals(mAddOsFile.getPath()) || input.getPath().equals(mTempOsFile.getPath())) {
                return;
            }
        }
        if (mAddZipFile ==null) {
            mAddOsFile.delete();
            mAddZipFile =new ZipFile(mAddOsFile);
            mAddZipFile.setFileNameCharset(mEncoding);
            mAddZipFileHeaderList =new ArrayList<BzfFileHeaderItem>();
            add_zip_model = new ZipModel();
            add_zip_model.setZipFile(mAddOsFile.getPath());
            add_zip_model.setFileNameCharset(mEncoding);
            add_zip_model.setEndCentralDirRecord(createEndOfCentralDirectoryRecord());
            add_zip_model.setSplitArchive(false);
            add_zip_model.setSplitLength(-1);

            SplitOutputStream splitOutputStream = null;
            try {
                splitOutputStream = new SplitOutputStream(new File(add_zip_model.getZipFile()), add_zip_model.getSplitLength());
            } catch (FileNotFoundException e) {
            }
            mAddZipOutputStream =new ZipOutputStream(splitOutputStream, add_zip_model);
        }
        addItemInternal(input, zp);
    };

    private EndCentralDirRecord createEndOfCentralDirectoryRecord() {
        EndCentralDirRecord endCentralDirRecord = new EndCentralDirRecord();
        endCentralDirRecord.setSignature(InternalZipConstants.ENDSIG);
        endCentralDirRecord.setNoOfThisDisk(0);
        endCentralDirRecord.setTotNoOfEntriesInCentralDir(0);
        endCentralDirRecord.setTotNoOfEntriesInCentralDirOnThisDisk(0);
        endCentralDirRecord.setOffsetOfStartOfCentralDir(0);
        return endCentralDirRecord;
    }

    private ZipModel readZipInfo(String file, String encoding) throws ZipException {
        ZipModel zipModel=null;
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(new File(file), InternalZipConstants.READ_MODE);
            HeaderReader headerReader = new HeaderReader(raf);
            zipModel = headerReader.readAllHeaders(encoding);
            if (zipModel != null) {
                zipModel.setZipFile(file);
            }
        } catch (FileNotFoundException e) {
            throw new ZipException(e);
        } finally {
            if (raf != null) {
                try {
                    raf.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
        return zipModel;
    }

    private boolean isDuplicateEntry(File input) {
        boolean result=false;
        for(BufferedZipFile2.BzfFileHeaderItem added: mAddZipFileHeaderList) {
//            String fn=added.file_header.getFileName();
            if (input.isFile()) {
                if (!added.isRemovedItem && added.file_header.getFileName().equals(input.getPath())) {
                    result=true;
                    break;
                }
            } else {
                if (!added.isRemovedItem && added.file_header.getFileName().equals(input.getPath()+"/")) {
                    result=true;
                    break;
                }
            }
        }
        return result;
    }

    private void addItemInternal(File input, ZipParameters parameters) throws ZipException {
        if (isDuplicateEntry(input)) throw new ZipException("BufferedZipFile2 Already added, name="+input.getPath());
        BufferedInputStream inputStream =null;
        try {
            byte[] readBuff = new byte[IO_AREA_SIZE];
            int readLen = -1;
            ZipParameters fileParameters = (ZipParameters) parameters.clone();

            if (!input.isDirectory()) {
                if (fileParameters.isEncryptFiles() && fileParameters.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_STANDARD) {
                    fileParameters.setSourceFileCRC((int) CRCUtil.computeFileCRC(input.getAbsolutePath(), null));
                }
                //Add no compress function 2016/07/22 F.Hoshino
                if (Zip4jUtil.getFileLengh(input)<100 || !fileParameters.isCompressFileExtention(input.getName())) {
                    fileParameters.setCompressionMethod(Zip4jConstants.COMP_STORE);
                }
            }

            mAddZipOutputStream.putNextEntry(input, fileParameters);
            if (input.isDirectory()) {
                mAddZipOutputStream.closeEntry();
            } else {
                inputStream = new BufferedInputStream(new FileInputStream(input),IO_AREA_SIZE*4);
                while ((readLen = inputStream.read(readBuff)) != -1) {
                    mAddZipOutputStream.write(readBuff, 0, readLen);
                }
                mAddZipOutputStream.closeEntry();

                inputStream.close();
            }

            ArrayList<FileHeader>fhl= add_zip_model.getCentralDirectory().getFileHeaders();
            List<LocalFileHeader> lfhl= add_zip_model.getLocalFileHeaderList();
            slf4jLog.debug("addItemInternal Central FileHeader size="+fhl.size()+", Local Fileheader size="+lfhl.size());
            for(int i = mAddZipFileHeaderList.size(); i<fhl.size(); i++) {
                FileHeader fh=fhl.get(i);
                BzfFileHeaderItem bfhi=new BzfFileHeaderItem();
                bfhi.file_header=fh;
                mAddZipFileHeaderList.add(bfhi);
                byte[] gpf=fh.getGeneralPurposeFlag();
                if (mGpfBit3On) {
                    if (gpf[0]>=0x08) gpf[1]=0x08;//For Windows Explorer
                }
                LocalFileHeader lfh=lfhl.get(i);
                slf4jLog.info("addItemInternal added name="+fh.getFileName()+", gpflags="+StringUtil.getHexString(gpf,0, gpf.length)+
                        ", GPF bit3 on="+mGpfBit3On+", Local file header name="+lfh.getFileName()+", UTF8 Encoding="+lfh.isFileNameUTF8Encoded());
//                slf4jLog.info("addItemInternal Central File header compressed size="+fh.getCompressedSize()+", Uncompressed size="+fh.getUncompressedSize()+", Crc32="+fh.getCrc32());
//                slf4jLog.info("addItemInternal Local File header compressed size="+lfh.getCompressedSize()+", Uncompressed size="+lfh.getUncompressedSize()+", Crc32="+lfh.getCrc32());
            }
        } catch (ZipException e) {
            if (inputStream != null) try {inputStream.close();} catch (IOException ex) {}
            if (mAddZipOutputStream != null) try {mAddZipOutputStream.close();} catch (IOException ex) {}
            throw e;
        } catch (Exception e) {
            if (inputStream != null) try {inputStream.close();} catch (IOException ex) {}
            if (mAddZipOutputStream != null) try {mAddZipOutputStream.close();} catch (IOException ex) {}
            throw new ZipException(e);
        }

    }

    private void checkClosed() throws ZipException {
        if (closed) throw new ZipException("BufferedZipFile2 is closed.");
    }

    private void removeItemIfExistst() {
        if (mAddZipFileHeaderList !=null && mAddZipFileHeaderList.size()>0) {
            ArrayList<BzfFileHeaderItem>sort_list=new ArrayList<BzfFileHeaderItem>();
            sort_list.addAll(mAddZipFileHeaderList);
            Collections.sort(sort_list, new Comparator<BzfFileHeaderItem>(){
                @Override
                public int compare(BufferedZipFile2.BzfFileHeaderItem lhs, BufferedZipFile2.BzfFileHeaderItem rhs) {
                    if (!lhs.file_header.getFileName().equalsIgnoreCase(rhs.file_header.getFileName()))
                        return lhs.file_header.getFileName().compareToIgnoreCase(rhs.file_header.getFileName());
                    return (int) (rhs.file_header.getOffsetLocalHeader()-lhs.file_header.getOffsetLocalHeader());
                }
            });

            //Check duplicate entry
            String prev_name="";
            ArrayList<BzfFileHeaderItem>removed_list_for_add=new ArrayList<BzfFileHeaderItem>();
            for(BufferedZipFile2.BzfFileHeaderItem item:sort_list) {
                if (!prev_name.equals(item.file_header.getFileName())) {
                    prev_name=item.file_header.getFileName();
                } else {
                    removed_list_for_add.add(item);
                }
            }

            for(BufferedZipFile2.BzfFileHeaderItem added_item: mAddZipFileHeaderList) {
                if (!added_item.isRemovedItem) {
                    for(BufferedZipFile2.BzfFileHeaderItem removed_item:removed_list_for_add) {
                        if (added_item.file_header.getFileName().equals(removed_item.file_header.getFileName()) &&
                                added_item.file_header.getOffsetLocalHeader()==removed_item.file_header.getOffsetLocalHeader()) {
                            added_item.isRemovedItem=true;
                            break;
                        }
                    }
                }
            }
            for(BufferedZipFile2.BzfFileHeaderItem primary_item: mInputZipFileHeaderList) {
                if (!primary_item.isRemovedItem) {
                    for(BufferedZipFile2.BzfFileHeaderItem removed_item: mAddZipFileHeaderList) {
                        if (primary_item.file_header.getFileName().equals(removed_item.file_header.getFileName())) {
                            primary_item.isRemovedItem=true;
                            mInpuZipFileItemRemoved =true;
                            break;
                        }
                    }
                }
            }
        }
    };

    public void destroy() throws ZipException, IOException {
        checkClosed();
        closed =true;
        if (mOutputZipFileStream !=null) mOutputZipFileStream.close();
        if (mAddZipOutputStream !=null) mAddZipOutputStream.close();

        if (mTempOsFile !=null && mTempOsFile.exists()) mTempOsFile.delete();
        if (mAddOsFile !=null && mAddOsFile.exists()) mAddOsFile.delete();

    }

    public void close() throws ZipException, Exception {
        checkClosed();
        closed =true;
//        closeFull();
        if (mInputOsFile !=null && mInputOsFile.length()==0) closeAddOnly();
        else closeUpdate();
    }

    private void closeAddOnly() throws ZipException, Exception {
        slf4jLog.debug("closeAddOnly entered");
        long b_time=System.currentTimeMillis();
        try {
            mOutputZipFilePosition =0;
            if (mAddZipFile !=null) {
                if (add_zip_model !=null && add_zip_model.getEndCentralDirRecord()!=null) {
                    dumpFileHeaderList("WriteHeader", mAddZipFileHeaderList);

                    mAddZipOutputStream.finish();

                    mAddZipOutputStream.flush();;
                    mAddZipOutputStream.close();

                    if (mOutputOsFile!=null) {
                        mAddOsFile.renameTo(mOutputOsFile);
                    } else {
                        FileInputStream fis=new FileInputStream(mAddOsFile);
                        byte[] buff=new byte[IO_AREA_SIZE*4];
                        int rc=0;
                        while((rc=fis.read(buff))>0) {
                            mOutputOsFileStream.write(buff,0,rc);
                        }
                        mOutputOsFileStream.flush();
                        mOutputOsFileStream.close();
                        mAddOsFile.delete();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ZipException(e.getMessage());
        }
        slf4jLog.debug("closeAddOnly elapsed time="+(System.currentTimeMillis()-b_time));
    }

    private boolean mZipOutputFinalyzeRequired=false;
    private void closeUpdate() throws ZipException, Exception {
        slf4jLog.debug("closeUpdate entered");
        long b_time=System.currentTimeMillis();
        try {
            removeItemIfExistst();

            mOutputZipFilePosition =0;
            if (mOutputOsFile!=null) {
                OutputStream os=new FileOutputStream(mTempOsFile);
                mOutputZipFileStream=new BufferedOutputStream(os,IO_AREA_SIZE*4);
            } else {
                mOutputZipFileStream=new BufferedOutputStream(mOutputOsFileStream,IO_AREA_SIZE*4);
            }

            if (!mEmptyInputZipFile) copyInputZipFile();

            if (mAddZipFile !=null) {
                mAddZipOutputStream.flush();;
                mAddZipOutputStream.close();

                appendAddZipFile();
            }

            if (mZipOutputFinalyzeRequired) {
                input_zip_model.getEndCentralDirRecord().setOffsetOfStartOfCentralDir(mOutputZipFilePosition);

                dumpFileHeaderList("WriteHeader", mInputZipFileHeaderList);
                dumpFileHeaderList("WriteHeader", mAddZipFileHeaderList);

                HeaderWriter hw=new HeaderWriter();
                hw.finalizeZipFile(input_zip_model, mOutputZipFileStream);

                mOutputZipFileStream.flush();
                mOutputZipFileStream.close();
            }
            if (mAddZipFile !=null) mAddZipFile.getFile().delete();

            if (mOutputOsFile!=null) {
                if (mOutputOsFile.exists()) mOutputOsFile.delete();
                mTempOsFile.renameTo(mOutputOsFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new ZipException(e.getMessage());
        }
        slf4jLog.debug("closeUpdate elapsed time="+(System.currentTimeMillis()-b_time));
    }

    private void copyInputZipFile() throws IOException, Exception {
        slf4jLog.debug("copyInputZipFile entered");
        long b_time=System.currentTimeMillis();
        if (mEmptyInputZipFile) return;

        dumpZipModel("WriteRemoveFile", input_zip_model);
        dumpFileHeaderList("WriteRemoveFile", mInputZipFileHeaderList);
        RandomAccessFile raf =null;
        try {
            raf=new RandomAccessFile(mInputOsFile,"r");
            if (mInpuZipFileItemRemoved) {
                for(int i = 0; i< mInputZipFileHeaderList.size(); i++) {
                    BufferedZipFile2.BzfFileHeaderItem rfhli= mInputZipFileHeaderList.get(i);
                    if (!rfhli.isRemovedItem) {
                        long primary_file_start_pos=rfhli.file_header.getOffsetLocalHeader();
                        rfhli.file_header.setOffsetLocalHeader(mOutputZipFilePosition);
                        long end_pos=0;
                        if (i==(mInputZipFileHeaderList.size()-1)) {//end pos=startCentralRecord-1
                            long offsetStartCentralDir = input_zip_model.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
                            if (input_zip_model.isZip64Format()) {
                                if (input_zip_model.getZip64EndCentralDirRecord() != null) {
                                    offsetStartCentralDir = input_zip_model.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
                                }
                            }
                            end_pos=offsetStartCentralDir-1;
                        } else {
                            end_pos= mInputZipFileHeaderList.get(i+1).file_header.getOffsetLocalHeader()-1;
                        }
                        mOutputZipFilePosition +=copyZipFile(rfhli.file_header.getFileName(),
                                mOutputZipFileStream, raf, primary_file_start_pos, end_pos);
                        mZipOutputFinalyzeRequired=true;
                    } else {
                        input_zip_model.getCentralDirectory().getFileHeaders().remove(rfhli.file_header);
                    }
                }
            } else {
                long end_pos=0;
                long offsetStartCentralDir = input_zip_model.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
                if (input_zip_model.isZip64Format()) {
                    if (input_zip_model.getZip64EndCentralDirRecord() != null) {
                        offsetStartCentralDir = input_zip_model.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
                    }
                }
                if (offsetStartCentralDir>1) {
                    end_pos=offsetStartCentralDir-1;
                    mOutputZipFilePosition +=copyZipFile("**copy_all_local_record", mOutputZipFileStream, raf, 0, end_pos);
                }
            }
        } finally {
            if (raf!=null) try {raf.close();} catch(Exception e){};
        }
        slf4jLog.debug("copyInputZipFile elapsed time="+(System.currentTimeMillis()-b_time));
    };

    private void appendAddZipFile() throws ZipException, Exception {
        slf4jLog.debug("appendAddZipFile entered");
        long b_time=System.currentTimeMillis();

        long offsetStartCentralDir= mAddZipFile.getFile().length();
        if (mEmptyInputZipFile) {
            mTempOsFile.delete();
            mAddOsFile.renameTo(mTempOsFile);
            mOutputZipFilePosition++;
        } else {
            dumpZipModel("WriteAddZipFile", add_zip_model);
            RandomAccessFile raf=null;
            try {
                raf=new RandomAccessFile(mAddZipFile.getFile(),"r");
                long base_pointer= mOutputZipFilePosition;
                for(int i = 0; i< mAddZipFileHeaderList.size(); i++) {
                    BzfFileHeaderItem fh= mAddZipFileHeaderList.get(i);
                    fh.file_header.setOffsetLocalHeader(mOutputZipFilePosition);
                    long end_pos=0;
                    if (i==(mAddZipFileHeaderList.size()-1)) {//end pos=startCentralRecord-1
                        end_pos=offsetStartCentralDir;
                    } else {
                        end_pos= mAddZipFileHeaderList.get(i+1).file_header.getOffsetLocalHeader()-1;
                    }
                    mOutputZipFilePosition +=copyZipFile(fh.file_header.getFileName(),
                            mOutputZipFileStream, raf, fh.file_header.getOffsetLocalHeader()-base_pointer, end_pos);
                    input_zip_model.getCentralDirectory().getFileHeaders().add(fh.file_header);

                    mZipOutputFinalyzeRequired=true;
                }
            } finally {
                if (raf!=null) try {raf.close();} catch(Exception e){};
            }
        }
        slf4jLog.debug("appendAddZipFile elapsed time="+(System.currentTimeMillis()-b_time));
    }

    private long copyZipFile(String name, BufferedOutputStream bos, RandomAccessFile input_file, long start_pos, long end_pos)
            throws IOException, Exception {
        if (slf4jLog.isTraceEnabled())
            slf4jLog.trace("CopyZipFile output="+String.format("%#010x", mOutputZipFilePosition)+
                ", start="+String.format("%#010x",start_pos)+", end="+String.format("%#010x",end_pos)+", Name="+name);
        int item_size=(int) (end_pos-start_pos)+1;
        byte[] buff=null;
        if (item_size>(IO_AREA_SIZE)) buff=new byte[IO_AREA_SIZE];
        else {
            if (item_size<1) throw(new Exception("Buffer size error. size="+item_size));
            buff=new byte[item_size];
        }
        int bufsz=buff.length;

        long output_size=0;
        int read_size=buff.length;
        try {
            input_file.seek(start_pos);
            int rc=input_file.read(buff,0,bufsz);
            while(rc>0) {
                bos.write(buff, 0, rc);
                output_size+=rc;
                if (item_size>output_size) {
                    if ((item_size-output_size)>0) {
                        read_size=(int) ((item_size-output_size)>bufsz?bufsz:(item_size-output_size));
                        rc=input_file.read(buff,0,read_size);
                    } else break;
                } else break;
            }
        } catch (IOException e) {
            throw new IOException(e);
        }

        return output_size;
    };

    public void removeItem(String[] remove_list) throws ZipException {
        checkClosed();
        ArrayList<FileHeader>fhl=new ArrayList<FileHeader>();
        for(String item:remove_list) {
            FileHeader fh=null;
            try {
                fh= mInputZipFile.getFileHeader(item);
            } catch (ZipException ze) {
            }
            if (fh!=null) fhl.add(fh);
        }
        if (fhl.size()>0) removeItem(fhl);
    }

    public void removeItem(FileHeader del_fh)
            throws ZipException {
        checkClosed();
        ArrayList<FileHeader>fhl=new ArrayList<FileHeader>();
        fhl.add(del_fh);
        removeItemInternal(fhl, mInputZipFileHeaderList);
        if (mAddZipFileHeaderList !=null && mAddZipFileHeaderList.size()>0) removeItemInternal(fhl, mAddZipFileHeaderList);
    }

    public void removeItem(ArrayList<FileHeader> remove_list)
            throws ZipException {
        checkClosed();
        removeItemInternal(remove_list, mInputZipFileHeaderList);
        if (mAddZipFileHeaderList !=null && mAddZipFileHeaderList.size()>0) removeItemInternal(remove_list, mAddZipFileHeaderList);
    }

    @SuppressLint("NewApi")
    private void removeItemInternal(ArrayList<FileHeader> remove_item_list,
                                    ArrayList<BzfFileHeaderItem>bzf_file_header_list) throws ZipException {
        checkClosed();
        for(FileHeader fh:remove_item_list) if (slf4jLog.isDebugEnabled()) slf4jLog.debug("removeItem selected name="+fh.getFileName());
        for(int i=0;i<bzf_file_header_list.size();i++) {
            BufferedZipFile2.BzfFileHeaderItem rfhli=bzf_file_header_list.get(i);
            if (!rfhli.isRemovedItem) {
                for(FileHeader remove_item:remove_item_list) {
                    if (rfhli.file_header.getFileName().equals(remove_item.getFileName())) {
                        rfhli.isRemovedItem=true;
                        mInpuZipFileItemRemoved =true;
                    }
                }
            }
        }
        dumpFileHeaderList("AfterDeleted", bzf_file_header_list);
    }

    private void dumpZipModel(String id, ZipModel zm) {
        if (!slf4jLog.isTraceEnabled() ||zm==null || zm.getEndCentralDirRecord()==null) return;
//        long offsetStartCentralDir = zm.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
//        if (zm.isZip64Format()) {
//            if (zm.getZip64EndCentralDirRecord() != null) {
//                offsetStartCentralDir = zm.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
//            }
//        }
//
//        slf4jLog.trace(id+" offsetStartCentralDir="+String.format("%#010x", offsetStartCentralDir));
//        ArrayList<FileHeader> fhl=zm.getCentralDirectory().getFileHeaders();
//        for(FileHeader fh:fhl) {
//            slf4jLog.trace(id+" FileHeader comp size="+fh.getCompressedSize()+
//                    ", header offset="+String.format("%#010x",fh.getOffsetLocalHeader())+
//                    ", crc32="+String.format("%#010x",fh.getCrc32())+
//                    ", name="+fh.getFileName());
//        }
    }

    private void dumpFileHeaderList(String id, ArrayList<BzfFileHeaderItem>bzf_file_header_list) {
        if (!slf4jLog.isTraceEnabled() || bzf_file_header_list==null) return;
//        for(int i=0;i<bzf_file_header_list.size();i++) {
//            BufferedZipFile2.BzfFileHeaderItem rfhli=bzf_file_header_list.get(i);
//            slf4jLog.trace(id+" BzFileHeader comp size="+rfhli.file_header.getCompressedSize()+
//                    ", header offset="+String.format("%#010x",rfhli.file_header.getOffsetLocalHeader())+
//                    ", crc32="+String.format("%#010x",rfhli.file_header.getCrc32())+
//                    ", removed="+rfhli.isRemovedItem+
//                    ", name="+rfhli.file_header.getFileName());
//        }
    }

}
