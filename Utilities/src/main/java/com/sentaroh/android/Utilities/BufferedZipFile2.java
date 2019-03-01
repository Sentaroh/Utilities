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

public class BufferedZipFile2 {
    private boolean closed =false;
    private boolean primary_file_changed=false;
    private ZipFile input_zip_file =null;
    private ZipFile add_zip_file =null;
    private File input_os_file =null;
    private File output_os_file =null;
    private File temp_os_file =null, add_os_file =null;
    private ZipModel input_zip_model =null;
    private ZipModel add_zip_model =null;
    private OutputStream temp_os_output_stream =null;
    private RandomAccessFile input_raf =null;
    private ArrayList<BzfFileHeaderItem> input_file_header_list =null;
    private ArrayList<BzfFileHeaderItem> add_file_header_list =null;
    private long primary_output_pos=0;
    private FileOutputStream primary_fos=null;
    private BufferedOutputStream primary_bos=null;

    private static final int IO_AREA_SIZE=1024*1024;
    private ZipOutputStream add_zip_output_stream =null;
    byte[] readBuff = new byte[IO_AREA_SIZE];

    private String file_name_encoding=DEFAULT_ZIP_FILENAME_ENCODING;
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
        this(new File(input_path), new File(output_path), null, DEFAULT_ZIP_FILENAME_ENCODING);
    }

    public BufferedZipFile2(String input_path, String output_path, String encoding) {
        this(new File(input_path), new File(output_path), null, encoding);
    }

    public BufferedZipFile2(String input_path, OutputStream fos, String encoding) {
        this(new File(input_path), null, fos, encoding);
    }

    public BufferedZipFile2(File input, File output, String encoding) {
        this(input, output, null, encoding);
    }

    private void putDebugMsg(String msg) {
        slf4jLog.debug(msg);
    }

    public BufferedZipFile2(File input_file, File output_file, OutputStream fos, String encoding) {
        file_name_encoding=encoding;
        input_os_file =input_file;
        output_os_file =output_file;
        if (output_file!=null) {
            temp_os_file =new File(output_file.getParent()+"/ziputility.tmp");
            add_os_file =new File(output_file.getParent()+"/ziputility.add");
        }
        temp_os_output_stream =fos;
        input_file_header_list =new ArrayList<BzfFileHeaderItem>();
        try {
            if (input_file==null || !input_file.exists()) {
            } else {
                input_zip_file =new ZipFile(input_file);
                input_zip_file.setFileNameCharset(encoding);
                input_raf =new RandomAccessFile(input_file,"r");
                HeaderReader header_reader=new HeaderReader(input_raf);
                try {
                    input_zip_model =header_reader.readAllHeaders(encoding);
                } catch (ZipException e) {
                    input_zip_model =new ZipModel();
                    input_zip_model.setEndCentralDirRecord(createEndOfCentralDirectoryRecord());
                    input_zip_model.setCentralDirectory(new CentralDirectory());
                    input_zip_model.getCentralDirectory().setFileHeaders(new ArrayList<FileHeader>());
                }
            }

            if (input_zip_model !=null && input_zip_model.getCentralDirectory()!=null) {
                @SuppressWarnings("unchecked")
                ArrayList<FileHeader>file_header_list=
                        (ArrayList<FileHeader>) input_zip_model.getCentralDirectory().getFileHeaders();
                for(FileHeader fh:file_header_list) {
                    BufferedZipFile2.BzfFileHeaderItem rfhli=new BufferedZipFile2.BzfFileHeaderItem();
                    rfhli.file_header=fh;
                    input_file_header_list.add(rfhli);
                }
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
        if (add_os_file!=null && temp_os_file!=null) {
            if (input.getPath().equals(add_os_file.getPath()) || input.getPath().equals(temp_os_file.getPath())) {
                return;
            }
        }
        if (add_zip_file ==null) {
            add_os_file.delete();
            add_zip_file =new ZipFile(add_os_file);
            add_zip_file.setFileNameCharset(file_name_encoding);
            add_file_header_list =new ArrayList<BzfFileHeaderItem>();
            add_zip_model = new ZipModel();
            add_zip_model.setZipFile(add_os_file.getPath());
            add_zip_model.setFileNameCharset(file_name_encoding);
            add_zip_model.setEndCentralDirRecord(createEndOfCentralDirectoryRecord());
            add_zip_model.setSplitArchive(false);
            add_zip_model.setSplitLength(-1);

            SplitOutputStream splitOutputStream = null;
            try {
                splitOutputStream = new SplitOutputStream(new File(add_zip_model.getZipFile()), add_zip_model.getSplitLength());
            } catch (FileNotFoundException e) {
            }
            add_zip_output_stream =new ZipOutputStream(splitOutputStream, add_zip_model);
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
        for(BufferedZipFile2.BzfFileHeaderItem added:add_file_header_list) {
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
                if (Zip4jUtil.getFileLengh(input)<100 ||
                        !fileParameters.isCompressFileExtention(input.getName())) {
                    fileParameters.setCompressionMethod(Zip4jConstants.COMP_STORE);
                }
            }

            add_zip_output_stream.putNextEntry(input, fileParameters);
            if (input.isDirectory()) {
                add_zip_output_stream.closeEntry();
            } else {
                inputStream = new BufferedInputStream(new FileInputStream(input),IO_AREA_SIZE*4);
                while ((readLen = inputStream.read(readBuff)) != -1) {
                    add_zip_output_stream.write(readBuff, 0, readLen);
                }
                add_zip_output_stream.closeEntry();

                inputStream.close();
            }

            @SuppressWarnings("unchecked")
            ArrayList<FileHeader>fhl= add_zip_model.getCentralDirectory().getFileHeaders();
            for(int i = add_file_header_list.size(); i<fhl.size(); i++) {
                FileHeader fh=fhl.get(i);
                BufferedZipFile2.BzfFileHeaderItem bfhi=new BufferedZipFile2.BzfFileHeaderItem();
                bfhi.file_header=fh;
                add_file_header_list.add(bfhi);
                putDebugMsg("addItemInternal added name="+fh.getFileName());
            }
        } catch (ZipException e) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }
            if (add_zip_output_stream != null) {
                try {
                    add_zip_output_stream.close();
                } catch (IOException ex) {
                }
            }
            throw e;
        } catch (Exception e) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }
            if (add_zip_output_stream != null) {
                try {
                    add_zip_output_stream.close();
                } catch (IOException ex) {
                }
            }
            throw new ZipException(e);
        }

    }

    private void checkClosed() throws ZipException {
        if (closed) throw new ZipException("BufferedZipFile2 is closed.");
    }

    private void removeItemIfExistst() {
        if (add_file_header_list !=null && add_file_header_list.size()>0) {
            ArrayList<BzfFileHeaderItem>sort_list=new ArrayList<BzfFileHeaderItem>();
            sort_list.addAll(add_file_header_list);
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

            for(BufferedZipFile2.BzfFileHeaderItem added_item: add_file_header_list) {
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
            for(BufferedZipFile2.BzfFileHeaderItem primary_item: input_file_header_list) {
                if (!primary_item.isRemovedItem) {
                    for(BufferedZipFile2.BzfFileHeaderItem removed_item: add_file_header_list) {
                        if (primary_item.file_header.getFileName().equals(removed_item.file_header.getFileName())) {
                            primary_item.isRemovedItem=true;
                            primary_file_changed=true;
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
        if (temp_os_output_stream!=null) temp_os_output_stream.close();
        if (input_raf!=null) input_raf.close();
        if (primary_bos!=null) primary_bos.close();
        if (add_zip_output_stream!=null) add_zip_output_stream.close();

        if (temp_os_file!=null && temp_os_file.exists()) temp_os_file.delete();
        if (add_os_file!=null && add_os_file.exists()) add_os_file.delete();

    }

    public void close() throws ZipException, Exception {
        checkClosed();
        closed =true;
//        closeFull();
        if (input_os_file!=null && input_os_file.length()==0) closeAdd();
        else closeUpdate();
    }

    private void closeAdd() throws ZipException, Exception {
        try {
            primary_output_pos=0;
            if (add_zip_file !=null) {
                if (add_zip_model !=null && add_zip_model.getEndCentralDirRecord()!=null) {
                    dumpRemoveList("WriteHeader", add_file_header_list);

                    add_zip_output_stream.finish();

                    add_zip_output_stream.flush();;
                    add_zip_output_stream.close();
//                    input_raf.close();

                    add_os_file.renameTo(output_os_file);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new ZipException(e.getMessage());
        }
    }

    private void closeUpdate() throws ZipException, Exception {
        try {
            primary_output_pos=0;

            removeItemIfExistst();

            if (primary_file_changed) writePrimaryZipFile();
            else {
                if (add_zip_file !=null) {
                    writePrimaryZipFile();
                }
            }

            if (add_zip_file !=null) writeAddZipFile();

            if (primary_output_pos>0 || primary_file_changed) {
                if (input_zip_model !=null && input_zip_model.getEndCentralDirRecord()!=null) {
                    input_zip_model.getEndCentralDirRecord().setOffsetOfStartOfCentralDir(primary_output_pos);

                    dumpRemoveList("WriteHeader", input_file_header_list);
                    dumpRemoveList("WriteHeader", add_file_header_list);

                    HeaderWriter hw=new HeaderWriter();
                    hw.finalizeZipFile(input_zip_model, primary_bos);

                    primary_bos.flush();
                    primary_bos.close();
                    input_raf.close();
                }
                if (add_zip_file !=null) add_zip_file.getFile().delete();
                temp_os_file.renameTo(output_os_file);
            } else {
                if (primary_bos!=null) {
                    primary_bos.flush();
                    primary_bos.close();
                }
                input_raf.close();
                temp_os_file.delete();
                if (add_zip_file !=null) add_zip_file.getFile().delete();
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new ZipException(e.getMessage());
        }
    }

    public void closeUpdateX() throws ZipException, Exception {
        try {
            primary_output_pos=0;

            removeItemIfExistst();

            if (primary_file_changed) writePrimaryZipFile();
            else {
                if (add_zip_file !=null) {
                    writePrimaryZipFile();
                }
            }

            if (add_zip_file !=null) writeAddZipFile();

            if (primary_output_pos>0 || primary_file_changed) {
                if (input_zip_model !=null && input_zip_model.getEndCentralDirRecord()!=null) {
                    input_zip_model.getEndCentralDirRecord().setOffsetOfStartOfCentralDir(primary_output_pos);

                    dumpRemoveList("WriteHeader", input_file_header_list);
                    dumpRemoveList("WriteHeader", add_file_header_list);

                    HeaderWriter hw=new HeaderWriter();
                    hw.finalizeZipFile(input_zip_model, primary_bos);

                    primary_bos.flush();
                    primary_bos.close();
                    input_raf.close();
                }
                if (add_zip_file !=null) add_zip_file.getFile().delete();
                input_zip_file.getFile().delete();
                temp_os_file.renameTo(output_os_file);
            } else {
                if (primary_bos!=null) {
                    primary_bos.flush();
                    primary_bos.close();
                }
                input_raf.close();
                temp_os_file.delete();
                if (add_zip_file !=null) add_zip_file.getFile().delete();
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new ZipException(e.getMessage());
        }
    }

    private void writePrimaryZipFile() throws IOException, Exception {
        input_raf.seek(0);
        if (primary_fos==null) {
            primary_fos=new FileOutputStream(temp_os_file);
            primary_bos=new BufferedOutputStream(primary_fos,IO_AREA_SIZE*4);
        }
        dumpZipModel("WriteRemoveFile", input_zip_model);
        dumpRemoveList("WriteRemoveFile", input_file_header_list);
        if (primary_file_changed) {
            for(int i = 0; i< input_file_header_list.size(); i++) {
                BufferedZipFile2.BzfFileHeaderItem rfhli= input_file_header_list.get(i);
                if (!rfhli.isRemovedItem) {
                    long primary_file_start_pos=rfhli.file_header.getOffsetLocalHeader();
                    rfhli.file_header.setOffsetLocalHeader(primary_output_pos);
                    long end_pos=0;
                    if (i==(input_file_header_list.size()-1)) {//end pos=startCentralRecord-1
                        long offsetStartCentralDir = input_zip_model.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
                        if (input_zip_model.isZip64Format()) {
                            if (input_zip_model.getZip64EndCentralDirRecord() != null) {
                                offsetStartCentralDir = input_zip_model.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
                            }
                        }
                        end_pos=offsetStartCentralDir-1;
                    } else {
                        end_pos= input_file_header_list.get(i+1).file_header.getOffsetLocalHeader()-1;
                    }
                    primary_output_pos+=copyZipFile(rfhli.file_header.getFileName(),
                            primary_bos, input_raf, primary_file_start_pos, end_pos);
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
                primary_output_pos+=copyZipFile("**copy_all_local_record", primary_bos, input_raf, 0, end_pos);
            }
        }
        input_raf.close();
    };

    @SuppressWarnings("unchecked")
    private void writeAddZipFile() throws ZipException, Exception {
        try {
            add_zip_output_stream.flush();;
            add_zip_output_stream.close();
            long offsetStartCentralDir= add_zip_file.getFile().length();
            if (input_zip_model !=null && input_zip_model.getCentralDirectory()!=null) {
                dumpZipModel("WriteAddFile", add_zip_model);
                if (primary_fos==null) {
                    primary_fos=new FileOutputStream(temp_os_file);
                    primary_bos=new BufferedOutputStream(primary_fos,IO_AREA_SIZE*4);
                }
                RandomAccessFile raf=new RandomAccessFile(add_zip_file.getFile(),"r");
                long base_pointer=primary_output_pos;
                for(int i = 0; i< add_file_header_list.size(); i++) {
                    BufferedZipFile2.BzfFileHeaderItem fh= add_file_header_list.get(i);
                    fh.file_header.setOffsetLocalHeader(primary_output_pos);
                    long end_pos=0;
                    if (i==(add_file_header_list.size()-1)) {//end pos=startCentralRecord-1
                        end_pos=offsetStartCentralDir;
                    } else {
                        end_pos= add_file_header_list.get(i+1).file_header.getOffsetLocalHeader()-1;
                    }
                    primary_output_pos+=copyZipFile(fh.file_header.getFileName(),
                            primary_bos, raf, fh.file_header.getOffsetLocalHeader()-base_pointer, end_pos);

                    input_zip_model.getCentralDirectory().getFileHeaders().add(fh.file_header);
                }
            } else {
                temp_os_file.delete();
                add_os_file.renameTo(temp_os_file);
                primary_output_pos++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private long copyZipFile(String name, BufferedOutputStream bos, RandomAccessFile input_file, long start_pos, long end_pos)
            throws IOException, Exception {
        putDebugMsg("CopyZipFile output="+String.format("%#010x",primary_output_pos)+
                ", start="+String.format("%#010x",start_pos)+", end="+String.format("%#010x",end_pos)+", Name="+name);
        int item_size=(int) (end_pos-start_pos)+1;
        byte[] buff=null;
        if (item_size>IO_AREA_SIZE) buff=new byte[IO_AREA_SIZE];
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

    public void removeItem(String[] remove_list)
            throws ZipException {
        checkClosed();
        ArrayList<FileHeader>fhl=new ArrayList<FileHeader>();
        for(String item:remove_list) {
            FileHeader fh=null;
            try {
                fh= input_zip_file.getFileHeader(item);
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
        removeItemInternal(fhl, input_file_header_list);
        if (add_file_header_list !=null && add_file_header_list.size()>0) removeItemInternal(fhl, add_file_header_list);
    }

    public void removeItem(ArrayList<FileHeader> remove_list)
            throws ZipException {
        checkClosed();
        removeItemInternal(remove_list, input_file_header_list);
        if (add_file_header_list !=null && add_file_header_list.size()>0) removeItemInternal(remove_list, add_file_header_list);
    }

    @SuppressLint("NewApi")
    private void removeItemInternal(ArrayList<FileHeader> remove_item_list,
                                    ArrayList<BzfFileHeaderItem>bzf_file_header_list) throws ZipException {
        checkClosed();
        for(FileHeader fh:remove_item_list) putDebugMsg("removeItem selected name="+fh.getFileName());
        for(int i=0;i<bzf_file_header_list.size();i++) {
            BufferedZipFile2.BzfFileHeaderItem rfhli=bzf_file_header_list.get(i);
            if (!rfhli.isRemovedItem) {
                for(FileHeader remove_item:remove_item_list) {
                    if (rfhli.file_header.getFileName().equals(remove_item.getFileName())) {
                        rfhli.isRemovedItem=true;
                        primary_file_changed=true;
                    }
                }
            }
        }
        dumpRemoveList("AfterDeleted", bzf_file_header_list);
    }

    @SuppressWarnings("unchecked")
    private void dumpZipModel(String id, ZipModel zm) {
//        if (!slf4jLog.isDebugEnabled() ||zm==null || zm.getEndCentralDirRecord()==null) return;
//        long offsetStartCentralDir = zm.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
//        if (zm.isZip64Format()) {
//            if (zm.getZip64EndCentralDirRecord() != null) {
//                offsetStartCentralDir = zm.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
//            }
//        }
//
//        putDebugMsg(id+" offsetStartCentralDir="+String.format("%#010x", offsetStartCentralDir));
//        ArrayList<FileHeader> fhl=zm.getCentralDirectory().getFileHeaders();
//        for(FileHeader fh:fhl) {
//            putDebugMsg(id+" FileHeader comp size="+fh.getCompressedSize()+
//                    ", header offset="+String.format("%#010x",fh.getOffsetLocalHeader())+
//                    ", crc32="+String.format("%#010x",fh.getCrc32())+
//                    ", name="+fh.getFileName());
//        }
    }

    private void dumpRemoveList(String id, ArrayList<BzfFileHeaderItem>bzf_file_header_list) {
//        if (!slf4jLog.isDebugEnabled() || bzf_file_header_list==null) return;
//        for(int i=0;i<bzf_file_header_list.size();i++) {
//            BufferedZipFile2.BzfFileHeaderItem rfhli=bzf_file_header_list.get(i);
//            putDebugMsg(id+" BzFileHeader comp size="+rfhli.file_header.getCompressedSize()+
//                    ", header offset="+String.format("%#010x",rfhli.file_header.getOffsetLocalHeader())+
//                    ", crc32="+String.format("%#010x",rfhli.file_header.getCrc32())+
//                    ", removed="+rfhli.isRemovedItem+
//                    ", name="+rfhli.file_header.getFileName());
//        }
    }

}
