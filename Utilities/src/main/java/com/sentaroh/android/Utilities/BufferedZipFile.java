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

import android.annotation.SuppressLint;
import android.util.Log;

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
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class BufferedZipFile {

	private boolean isClosed=false;
	private boolean primary_file_changed=false;
	private ZipFile primary_zip_file=null;
	private ZipFile add_zip_file=null;
	private ZipModel primary_zip_model=null;
	private ZipModel add_zip_model=null;
	private RandomAccessFile primary_raf=null;
	private ArrayList<BzfFileHeaderItem> primary_file_header_list=null;
	private ArrayList<BzfFileHeaderItem> add_file_header_list=null;
	private long primary_output_pos=0;
	private File primary_output_file=null;
	private FileOutputStream primary_fos=null;
	private BufferedOutputStream primary_bos=null;

	private static final int IO_AREA_SIZE=1024*1024;
	private ZipOutputStream add_fos=null;
	byte[] readBuff = new byte[IO_AREA_SIZE];

	private String file_name_encoding=DEFAULT_ZIP_FILENAME_ENCODING;
	private static final String DEFAULT_ZIP_FILENAME_ENCODING="UTF-8";
	
	private boolean debug_enabled=false;

    private static Logger slf4jLog = LoggerFactory.getLogger(BufferedZipFile.class);
	
	class BzfFileHeaderItem {
		public FileHeader file_header;
		public boolean isRemovedItem=false;
		public long start_pos=0;
		public long end_pos=0;
		public boolean removed_entry=false;
	}
	public BufferedZipFile(String input_path, boolean debug) {
		this(new File(input_path), DEFAULT_ZIP_FILENAME_ENCODING, debug);
	}
	public BufferedZipFile(String input_path, String encoding, boolean debug) {
		this(new File(input_path), encoding, debug);
	}
	private void putDebugMsg(String id, String msg) {
        if (debug_enabled) slf4jLog.debug(msg);
//		if (debug_enabled) {
//			String w_id=(id+"                ").substring(0,16);
//			String m_text=w_id+" "+msg;
//			Log.v("BufferedZipFile", m_text);
//		}
	}
	public BufferedZipFile(File input, String encoding, boolean debug) {
		debug_enabled=debug;
		file_name_encoding=encoding;
        primary_file_header_list=new ArrayList<BzfFileHeaderItem>();
        try {
			if (!input.exists()) {
				input.createNewFile();
				primary_zip_file=new ZipFile(input);
				primary_zip_file.setFileNameCharset(encoding);
				primary_raf=new RandomAccessFile(input,"r");
				primary_zip_model=new ZipModel();
				primary_zip_model.setEndCentralDirRecord(createEndOfCentralDirectoryRecord());
				primary_zip_model.setCentralDirectory(new CentralDirectory());
				primary_zip_model.getCentralDirectory().setFileHeaders(new ArrayList<FileHeader>());
			} else {
				primary_zip_file=new ZipFile(input);
				primary_zip_file.setFileNameCharset(encoding);
				primary_raf=new RandomAccessFile(input,"r");
				HeaderReader header_reader=new HeaderReader(primary_raf);
				try {
					primary_zip_model=header_reader.readAllHeaders(encoding);
				} catch (ZipException e) {
					primary_zip_model=new ZipModel();
					primary_zip_model.setEndCentralDirRecord(createEndOfCentralDirectoryRecord());
					primary_zip_model.setCentralDirectory(new CentralDirectory());
					primary_zip_model.getCentralDirectory().setFileHeaders(new ArrayList<FileHeader>());
				}
			}
			
			if (primary_zip_model!=null && primary_zip_model.getCentralDirectory()!=null) {
				@SuppressWarnings("unchecked")
				ArrayList<FileHeader>file_header_list=
						(ArrayList<FileHeader>)primary_zip_model.getCentralDirectory().getFileHeaders();
				for(FileHeader fh:file_header_list) {
					BzfFileHeaderItem rfhli=new BzfFileHeaderItem();
					rfhli.file_header=fh;
					primary_file_header_list.add(rfhli);
				}
			}
		} catch (ZipException e) {
            slf4jLog.debug("<init>",e);
//			e.printStackTrace();
		} catch (FileNotFoundException e) {
            slf4jLog.debug("<init>",e);
//			e.printStackTrace();
		} catch (IOException e) {
            slf4jLog.debug("<init>",e);
//            e.printStackTrace();
		}
		primary_output_file=new File(primary_zip_file.getFile().getPath()+".wrk");

		dumpZipModel("Init", primary_zip_model);
	};
	
	public void addItem(String input, ZipParameters zp) throws ZipException {
		addItem(new File(input), zp);
	};
	
	public void addItem(File input, ZipParameters zp) throws ZipException {
		checkClosed();
		if (add_zip_file==null) {
			File lf=new File(primary_zip_file.getFile().getPath()+".addwrk");
			lf.delete();
			add_zip_file=new ZipFile(lf);
			add_zip_file.setFileNameCharset(file_name_encoding);
			add_file_header_list=new ArrayList<BzfFileHeaderItem>();
			add_zip_model = new ZipModel();
			add_zip_model.setZipFile(lf.getPath());
			add_zip_model.setFileNameCharset(file_name_encoding);
			add_zip_model.setEndCentralDirRecord(createEndOfCentralDirectoryRecord());
			add_zip_model.setSplitArchive(false);
			add_zip_model.setSplitLength(-1);
			
			SplitOutputStream splitOutputStream = null;
			try {
				splitOutputStream = new SplitOutputStream(new File(add_zip_model.getZipFile()), 
						add_zip_model.getSplitLength());
			} catch (FileNotFoundException e) {
			}
			add_fos=new ZipOutputStream(splitOutputStream, add_zip_model);
//			new BufferedOutputStream(add_fos, IO_AREA_SIZE*4);

		}
//		String file_name = Zip4jUtil.getRelativeFileName(input.getAbsolutePath(), 
//				zp.getRootFolderInZip(), zp.getDefaultFolderPath());
//		removeItemPrimaryIfExists(input, zp, file_name);
//		removeItemAddIfExists(input, zp, file_name);
		
//		if (input.isDirectory()) add_zip_file.addFolder(input, zp);
//		else add_zip_file.addFile(input, zp);
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

	private void addItemInternal(File input, ZipParameters parameters) throws ZipException {
		BufferedInputStream inputStream =null;
		try {
//			checkParameters(parameters);
//			
//			removeFilesIfExists(fileList, parameters, progressMonitor);
//			
//			boolean isZipFileAlreadExists = Zip4jUtil.checkFileExists(zipModel.getZipFile());
			
			byte[] readBuff = new byte[IO_AREA_SIZE];
			int readLen = -1;
			ZipParameters fileParameters = (ZipParameters) parameters.clone();
			
			if (!input.isDirectory()) {
				if (fileParameters.isEncryptFiles() && fileParameters.getEncryptionMethod() == Zip4jConstants.ENC_METHOD_STANDARD) {
					fileParameters.setSourceFileCRC((int)CRCUtil.computeFileCRC(input.getAbsolutePath(), null));
				}
				//Add no compress function 2016/07/22 F.Hoshino
				if (Zip4jUtil.getFileLengh(input)<100 || 
						!fileParameters.isCompressFileExtention(input.getName())) {
					fileParameters.setCompressionMethod(Zip4jConstants.COMP_STORE);
				}
			}
			
			add_fos.putNextEntry(input, fileParameters);
			if (input.isDirectory()) {
				add_fos.closeEntry();
			} else {
				inputStream = new BufferedInputStream(new FileInputStream(input),IO_AREA_SIZE*4);
				while ((readLen = inputStream.read(readBuff)) != -1) {
					add_fos.write(readBuff, 0, readLen);
				}
				add_fos.closeEntry();
				
				inputStream.close();
			}
			
			@SuppressWarnings("unchecked")
			ArrayList<FileHeader>fhl=add_zip_model.getCentralDirectory().getFileHeaders();
			for(int i=add_file_header_list.size();i<fhl.size();i++) {
				FileHeader fh=fhl.get(i);
				BzfFileHeaderItem bfhi=new BzfFileHeaderItem();
				bfhi.file_header=fh;
				add_file_header_list.add(bfhi);
				if (debug_enabled) putDebugMsg("addItemInternal","added name="+fh.getFileName());
			}
			
			
//			add_fos.finish();
		} catch (ZipException e) {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ex) {
				}
			}
			if (add_fos != null) {
				try {
					add_fos.close();
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
			if (add_fos != null) {
				try {
					add_fos.close();
				} catch (IOException ex) {
				}
			}
			throw new ZipException(e);
		}
		
	}
	
	
//	private void removeItemPrimaryIfExists(File input, ZipParameters zp, String file_name) {
//		ArrayList<FileHeader>delete_list=new ArrayList<FileHeader>();
//		try {
//			for(BzfFileHeaderItem bfh:primary_file_header_list) {
//				if (!bfh.isRemovedItem) {
//					if (input.isDirectory()) {
//						if (bfh.file_header.getFileName().startsWith(file_name)) {
//							delete_list.add(bfh.file_header);
//						}
//					} else {
//						if (bfh.file_header.getFileName().equals(file_name)) {
//							delete_list.add(bfh.file_header);
//							break;
//						}
//					}
//				}
//			}
//			if(delete_list.size()>0) removeItem(delete_list);
////			Log.v("","name="+item);
//		} catch (ZipException ze) {
//		}
//
//	};
//
//	private void removeItemAddIfExists(File input, ZipParameters zp, String file_name) {
//		ArrayList<FileHeader>delete_list=new ArrayList<FileHeader>();
//		try {
//			for(BzfFileHeaderItem bfh:add_file_header_list) {
//				if (!bfh.isRemovedItem) {
//					if (input.isDirectory()) {
//						if (bfh.file_header.getFileName().startsWith(file_name)) {
//							delete_list.add(bfh.file_header);
//						}
//					} else {
//						if (bfh.file_header.getFileName().equals(file_name)) {
//							delete_list.add(bfh.file_header);
//							break;
//						}
//					}
//				}
//			}
//			if(delete_list.size()>0) removeItemInternal(delete_list, add_file_header_list);
////			Log.v("","name="+item);
//		} catch (ZipException ze) {
//		}
//
//	};

	private void checkClosed() throws ZipException {
		if (isClosed) throw new ZipException("BufferedZipFile is closed.");
	};
	
	private void removeItemIfExistst() {
		if (add_file_header_list!=null && add_file_header_list.size()>0) {
			ArrayList<BzfFileHeaderItem>sort_list=new ArrayList<BzfFileHeaderItem>();
			sort_list.addAll(add_file_header_list);
			Collections.sort(sort_list, new Comparator<BzfFileHeaderItem>(){
				@Override
				public int compare(BzfFileHeaderItem lhs, BzfFileHeaderItem rhs) {
					if (!lhs.file_header.getFileName().equalsIgnoreCase(rhs.file_header.getFileName()))
						return lhs.file_header.getFileName().compareToIgnoreCase(rhs.file_header.getFileName());
					return (int) (rhs.file_header.getOffsetLocalHeader()-lhs.file_header.getOffsetLocalHeader());
				}
			});
			
//			Log.v("","create delete list from added list");
			String prev_name="";
			ArrayList<BzfFileHeaderItem>removed_list_for_add=new ArrayList<BzfFileHeaderItem>();
			for(BzfFileHeaderItem item:sort_list) {
				if (!prev_name.equals(item.file_header.getFileName())) {
					prev_name=item.file_header.getFileName();
				} else {
					removed_list_for_add.add(item);
				}
			}
			
//			Log.v("","remove add file entry");
			for(BzfFileHeaderItem added_item:add_file_header_list) {
				if (!added_item.isRemovedItem) {
					for(BzfFileHeaderItem removed_item:removed_list_for_add) {
						if (added_item.file_header.getFileName().equals(removed_item.file_header.getFileName()) &&
								added_item.file_header.getOffsetLocalHeader()==removed_item.file_header.getOffsetLocalHeader()) {
							added_item.isRemovedItem=true;
//							Log.v("", "removed add name="+removed_item.file_header.getFileName());
							break;
						}
					}
				}
			}
//			Log.v("","remove add file entry");
			for(BzfFileHeaderItem primary_item:primary_file_header_list) {
				if (!primary_item.isRemovedItem) {
					for(BzfFileHeaderItem removed_item:add_file_header_list) {
						if (primary_item.file_header.getFileName().equals(removed_item.file_header.getFileName())) {
							primary_item.isRemovedItem=true;
							primary_file_changed=true;
//							Log.v("", "removed add name="+removed_item.file_header.getFileName());
							break;
						}
					}
				}
			}
//			Log.v("","remove end");
		}
	};
	
	public void close() throws ZipException, Exception {
		checkClosed();
		isClosed=true;
		try {
			primary_output_pos=0;

			removeItemIfExistst();
			
			if (primary_file_changed) writePrimaryZipFile();
			else {
				if (add_zip_file!=null) {
					writePrimaryZipFile();
				}
			}
			
			if (add_zip_file!=null) writeAddZipFile();

			if (primary_output_pos>0 || primary_file_changed) {
				if (primary_zip_model!=null && primary_zip_model.getEndCentralDirRecord()!=null) {
					primary_zip_model.getEndCentralDirRecord().setOffsetOfStartOfCentralDir(primary_output_pos);

					dumpRemoveList("WriteHeader", primary_file_header_list);
					dumpRemoveList("WriteHeader", add_file_header_list);
					
					HeaderWriter hw=new HeaderWriter();
					hw.finalizeZipFile(primary_zip_model, primary_bos);
					
					primary_bos.flush();
					primary_bos.close();
					primary_raf.close();
				}
				if (add_zip_file!=null) add_zip_file.getFile().delete();
				primary_zip_file.getFile().delete();
				primary_output_file.renameTo(primary_zip_file.getFile());
			} else {
				if (primary_bos!=null) {
					primary_bos.flush();
					primary_bos.close();
				}
				primary_raf.close();
				primary_output_file.delete();
				if (add_zip_file!=null) add_zip_file.getFile().delete();
			}

		} catch (IOException e) {
            slf4jLog.debug("close",e);
//			e.printStackTrace();
			throw new ZipException(e.getMessage());
		}
	};
	
	private void writePrimaryZipFile() throws IOException, Exception {
		primary_raf.seek(0);
		if (primary_fos==null) {
			primary_fos=new FileOutputStream(primary_output_file);
			primary_bos=new BufferedOutputStream(primary_fos,IO_AREA_SIZE*4);
		}
		dumpZipModel("WriteRemoveFile", primary_zip_model);
		dumpRemoveList("WriteRemoveFile", primary_file_header_list);
		if (primary_file_changed) {
			for(int i=0;i<primary_file_header_list.size();i++) {
				BzfFileHeaderItem rfhli=primary_file_header_list.get(i);
				if (!rfhli.isRemovedItem) {
					long primary_file_start_pos=rfhli.file_header.getOffsetLocalHeader();
					rfhli.file_header.setOffsetLocalHeader(primary_output_pos);
					long end_pos=0;
					if (i==(primary_file_header_list.size()-1)) {//end pos=startCentralRecord-1
						long offsetStartCentralDir = primary_zip_model.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
						if (primary_zip_model.isZip64Format()) {
							if (primary_zip_model.getZip64EndCentralDirRecord() != null) {
								offsetStartCentralDir = primary_zip_model.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
							}
						}
						end_pos=offsetStartCentralDir-1;
					} else {
						end_pos=primary_file_header_list.get(i+1).file_header.getOffsetLocalHeader()-1;
					}
					primary_output_pos+=copyZipFile(rfhli.file_header.getFileName(), 
							primary_bos, primary_raf, primary_file_start_pos, end_pos);
				} else {
					primary_zip_model.getCentralDirectory().getFileHeaders().remove(rfhli.file_header);
				}
			}
		} else {
			long end_pos=0;
			long offsetStartCentralDir = primary_zip_model.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
			if (primary_zip_model.isZip64Format()) {
				if (primary_zip_model.getZip64EndCentralDirRecord() != null) {
					offsetStartCentralDir = primary_zip_model.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
				}
			}
            if (offsetStartCentralDir>1) {
                end_pos=offsetStartCentralDir-1;
                primary_output_pos+=copyZipFile("**copy_all_local_record", primary_bos, primary_raf, 0, end_pos);
            }
		}
		primary_raf.close();
	};
	
	@SuppressWarnings("unchecked")
	private void writeAddZipFile() throws ZipException, Exception {
		try {
			add_fos.flush();;
//			add_fos.finish();
			add_fos.close();
			long offsetStartCentralDir=add_zip_file.getFile().length();
//			String npe=null;
//			npe.length();
			if (primary_zip_model!=null && primary_zip_model.getCentralDirectory()!=null) {
				dumpZipModel("WriteAddFile",add_zip_model);
				if (primary_fos==null) {
					primary_fos=new FileOutputStream(primary_output_file);
					primary_bos=new BufferedOutputStream(primary_fos,IO_AREA_SIZE*4);
				}
				RandomAccessFile raf=new RandomAccessFile(add_zip_file.getFile(),"r");
				long base_pointer=primary_output_pos;
				for(int i=0;i<add_file_header_list.size();i++) {
					BzfFileHeaderItem fh=add_file_header_list.get(i);
					fh.file_header.setOffsetLocalHeader(primary_output_pos);
					long end_pos=0;
					if (i==(add_file_header_list.size()-1)) {//end pos=startCentralRecord-1
						end_pos=offsetStartCentralDir;
					} else {
						end_pos=add_file_header_list.get(i+1).file_header.getOffsetLocalHeader()-1;
					}
					primary_output_pos+=copyZipFile(fh.file_header.getFileName(), 
							primary_bos, raf, fh.file_header.getOffsetLocalHeader()-base_pointer, end_pos);
					
					primary_zip_model.getCentralDirectory().getFileHeaders().add(fh.file_header);
				}
			} else {
				primary_output_file.delete();
				add_zip_file.getFile().renameTo(primary_output_file);
				primary_output_pos++;
			}
			
		} catch (FileNotFoundException e) {
            slf4jLog.debug("writeAddZipFile",e);
//			e.printStackTrace();
		} catch (IOException e) {
            slf4jLog.debug("writeAddZipFile",e);
//			e.printStackTrace();
		}
	}
	
	@SuppressLint("NewApi")
	private long copyZipFile(String name, BufferedOutputStream bos, RandomAccessFile input_file, long start_pos, long end_pos)
			throws IOException, Exception {
		if (debug_enabled) putDebugMsg("CopyZipFile", "output="+String.format("%#010x",primary_output_pos)+
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
//			e.printStackTrace();
            slf4jLog.debug("copyZipFile",e);
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
				fh=primary_zip_file.getFileHeader(item);
//				Log.v("","name="+item);
			} catch (ZipException ze) {
			}
			if (fh!=null) fhl.add(fh);
		}
		if (fhl.size()>0) removeItem(fhl); 
	};

	public void removeItem(FileHeader del_fh) 
			throws ZipException {
		checkClosed();
		ArrayList<FileHeader>fhl=new ArrayList<FileHeader>();
		fhl.add(del_fh);
		removeItemInternal(fhl, primary_file_header_list);
		if (add_file_header_list!=null && add_file_header_list.size()>0) removeItemInternal(fhl, add_file_header_list);
	};

	public void removeItem(ArrayList<FileHeader> remove_list) 
			throws ZipException {
		checkClosed();
		removeItemInternal(remove_list, primary_file_header_list); 
		if (add_file_header_list!=null && add_file_header_list.size()>0) removeItemInternal(remove_list, add_file_header_list);
	};

	@SuppressLint("NewApi")
	private void removeItemInternal(ArrayList<FileHeader> remove_item_list,
			ArrayList<BzfFileHeaderItem>bzf_file_header_list) throws ZipException {
		checkClosed();
		if (debug_enabled) for(FileHeader fh:remove_item_list) putDebugMsg("removeItem","selected name="+fh.getFileName());
		for(int i=0;i<bzf_file_header_list.size();i++) {
			BzfFileHeaderItem rfhli=bzf_file_header_list.get(i);
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
		if (!debug_enabled ||zm==null || zm.getEndCentralDirRecord()==null) return;
		long offsetStartCentralDir = zm.getEndCentralDirRecord().getOffsetOfStartOfCentralDir();
		if (zm.isZip64Format()) {
			if (zm.getZip64EndCentralDirRecord() != null) {
				offsetStartCentralDir = zm.getZip64EndCentralDirRecord().getOffsetStartCenDirWRTStartDiskNo();
			}
		}
//		HeaderReader header_reader=new HeaderReader(primary_raf);

		putDebugMsg(id, "offsetStartCentralDir="+String.format("%#010x", offsetStartCentralDir));
		ArrayList<FileHeader>fhl=zm.getCentralDirectory().getFileHeaders();
		for(FileHeader fh:fhl) {
			putDebugMsg(id, "FileHeader comp size="+fh.getCompressedSize()+
					", header offset="+String.format("%#010x",fh.getOffsetLocalHeader())+
					", crc32="+String.format("%#010x",fh.getCrc32())+
					", name="+fh.getFileName());
//			try {
//				LocalFileHeader lh=header_reader.readLocalFileHeader(fh);
//				Log.v(id,"LocalFileHeaderList data offset="+String.format("%x",lh.getOffsetStartOfData()));
//			} catch (ZipException e) {
//				e.printStackTrace();
//			}
		}
	}
	
	private void dumpRemoveList(String id, ArrayList<BzfFileHeaderItem>bzf_file_header_list) {
		if (!debug_enabled || bzf_file_header_list==null) return;
		for(int i=0;i<bzf_file_header_list.size();i++) {
			BzfFileHeaderItem rfhli=bzf_file_header_list.get(i);
			putDebugMsg(id, "BzFileHeader comp size="+rfhli.file_header.getCompressedSize()+
					", header offset="+String.format("%#010x",rfhli.file_header.getOffsetLocalHeader())+
					", crc32="+String.format("%#010x",rfhli.file_header.getCrc32())+
					", removed="+rfhli.isRemovedItem+
					", name="+rfhli.file_header.getFileName());
		}
	}

}
