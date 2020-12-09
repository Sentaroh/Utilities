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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipOutputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import android.content.Context;

import com.sentaroh.android.Utilities.Dialog.ProgressBarDialogFragment;

public class ZipUtil {
	
	public static final String DEFAULT_ZIP_FILENAME_ENCODING="UTF-8";

    private static Logger slf4jLog = LoggerFactory.getLogger(ZipUtil.class);
	
	static public String detectFileNameEncoding(String zip_path) {
		String encoding="";
	    String result=null;
	    UniversalDetector detector = new UniversalDetector(null);
//	    long b_time=System.currentTimeMillis();
		try {
		    ZipFile zf=new ZipFile(zip_path);
			@SuppressWarnings("unchecked")
			List<FileHeader> fhl=zf.getFileHeaders();
			if (fhl.size()>0) {
				StringBuilder enc_det=new StringBuilder(1024*1024*8);
				boolean fileNameUTF8Encoded=false;
				for(FileHeader fh:fhl) {
					if (fh.isFileNameUTF8Encoded()) {
						fileNameUTF8Encoded=true;
						break;
					}
					enc_det.append(fh.getFileName());
					if (enc_det.length()>=(1024*256)) {
//						Log.v("","length="+enc_det.length());
						break;
					}
				}
//				Log.v("","length="+enc_det.length()+", fileNameUTF8Encoded="+fileNameUTF8Encoded);
//				Log.v("","enc="+enc_det.toString());
				if (!fileNameUTF8Encoded) {
					while(enc_det.length()<(1024*1024*1)) {
						enc_det.append(enc_det.toString());
					}
//					Log.v("","length="+enc_det.length());
					byte[] det_buff=enc_det.toString().getBytes("Cp850");
				    detector.handleData(det_buff, 0, det_buff.length);
				    detector.dataEnd();
				    encoding = detector.getDetectedCharset();
				    detector.reset();
				    if (encoding!=null) {
				    	try {
				    		if (!Charset.isSupported(encoding)) result=null;
				    		else result=encoding;
				    	} catch (IllegalCharsetNameException e) {
//				    		e.printStackTrace();
				    		result=null;
				    	}
				    }
				} else {
					result=DEFAULT_ZIP_FILENAME_ENCODING;
				}
			}
		}catch(ZipException e) {
		    slf4jLog.debug("detectFileNameEncoding",e);
			result=null;
        }catch(Exception e) {
            slf4jLog.debug("detectFileNameEncoding",e);
            result=null;
		}
        slf4jLog.trace("detectFileNameEncoding result="+result);
		return result;
	};

	private static class DirectoryListItem {
		public String name="";
		public String parent="";
		public boolean added_directory=false;
		
		DirectoryListItem() {}
		
		DirectoryListItem(String entry, String parent, boolean added) {
			this.name=entry;
			this.parent=parent;
			added_directory=added;
		}
	};

	static private int findDirectoryList(ArrayList<DirectoryListItem>dir_list, String entry, String parent) {
		int idx=Collections.binarySearch(dir_list, new DirectoryListItem(entry, parent, false),
				new Comparator<DirectoryListItem>(){
			@Override
			public int compare(DirectoryListItem lhs, DirectoryListItem rhs) {
				String l_key=lhs.parent+lhs.name;
				String r_key=rhs.parent+rhs.name;
				return l_key.compareToIgnoreCase(r_key);
			}
		});
//		Log.v("","result="+idx+", name="+entry+", parent="+parent);
		return idx;
	}

	static private void addDirectory(StringBuilder sb, ArrayList<DirectoryListItem> dir_list,
			ArrayList<ZipFileListItem> zfl, String path, boolean isUtf8Enc) {
//		if (path.equals("")) return;
		String p_dir="", dir_name="";
		String[] dir_array=path.split("/");
		
		if (dir_array.length==1) {
			int idx=findDirectoryList(dir_list, dir_array[0], "");
			if (idx<0) {
				dir_list.add(new DirectoryListItem(dir_array[0], "", true));
				sortDirectoryList(dir_list);
			}
		} else {
			sb.setLength(0);
			for(int i=0;i<(dir_array.length-1);i++) {
				if (i==0) sb.append(dir_array[i]);
				else sb.append("/").append(dir_array[i]);
			}
			p_dir=sb.toString();
			dir_name=dir_array[dir_array.length-1];
			int idx=findDirectoryList(dir_list, dir_name, p_dir);
			if (idx<0) {
				dir_list.add(new DirectoryListItem(dir_name, p_dir, true));
				sortDirectoryList(dir_list);
				addDirectory(sb, dir_list, zfl, p_dir, isUtf8Enc);
			}
		}
	};

	static public boolean isZipFile(String zip_path) {
		boolean result=false;
		try {
			ZipFile zf=new ZipFile(new File(zip_path));
			@SuppressWarnings({ "unchecked", "unused" })
			List<FileHeader> fhl=zf.getFileHeaders();
			result=true;
		}catch(Exception e) {
		}
		return result;
	};

	static private void sortDirectoryList(ArrayList<DirectoryListItem>dir_list) {
		Collections.sort(dir_list, new Comparator<DirectoryListItem>(){
			@Override
			public int compare(DirectoryListItem lhs, DirectoryListItem rhs) {
				String l_key=lhs.parent+lhs.name;
				String r_key=rhs.parent+rhs.name;
				return l_key.compareToIgnoreCase(r_key);
			}
		});
	};
	
	static public ArrayList<ZipFileListItem> buildZipFileList(String zip_path, String encoding) {
		ArrayList<ZipFileListItem> tfl=new ArrayList<ZipFileListItem>();
		try {
			ZipFile zf=new ZipFile(new File(zip_path));
			zf.setFileNameCharset(encoding);
//			Log.v("","create start");
//			long b_time=System.currentTimeMillis();
//			@SuppressWarnings("unchecked")
//			List<FileHeader> fhl=zf.getFileHeaders();
			@SuppressWarnings("unchecked")
			List<FileHeader> fhl=zf.getFileHeaders();
			if (fhl.size()>0) {
				for(FileHeader fh:fhl) {
					String tfp=fh.getFileName();
					String t_path="", t_name="";//, w_t_name="";
					String w_path="";
					if (fh.isDirectory()) {
						w_path=tfp.endsWith("/")?tfp.substring(0,tfp.length()-1):tfp;
						t_name=w_path.lastIndexOf("/")>=0?w_path.substring(w_path.lastIndexOf("/")+1):w_path;
						t_path=w_path.substring(0,w_path.length()-t_name.length());
						if (t_path.endsWith("/")) t_path=t_path.substring(0,t_path.length()-1);
					} else {
						w_path=tfp;
						t_name=w_path.lastIndexOf("/")>=0?w_path.substring(w_path.lastIndexOf("/")+1):w_path; 
						t_path=w_path.substring(0,w_path.length()-t_name.length());
						if (t_path.endsWith("/")) t_path=t_path.substring(0,t_path.length()-1);
					}
					ZipFileListItem tfli=new ZipFileListItem(t_name, t_path,
							fh.isDirectory(), fh.isEncrypted(), fh.getUncompressedSize(), 
							dosToJavaTme(fh.getLastModFileTime()), fh.getCompressedSize(),
							fh.getCompressionMethod(),
							fh.isFileNameUTF8Encoded());
//					tfli.dump();
					tfl.add(tfli);
				}
//				Log.v("","create list elapsed="+(System.currentTimeMillis()-b_time));
				
				Collections.sort(tfl,new Comparator<ZipFileListItem>(){
					@Override
					public int compare(ZipFileListItem lhs, ZipFileListItem rhs) {
						return lhs.getParentDirectory().compareToIgnoreCase(rhs.getParentDirectory());
					}
				});
//				Log.v("","sort list elapsed="+(System.currentTimeMillis()-b_time));
				
				ArrayList<DirectoryListItem> dir_list=new ArrayList<DirectoryListItem>();
				for(ZipFileListItem zfli:tfl) {
					if (zfli.isDirectory()) {
						DirectoryListItem dli=new DirectoryListItem();
						dli.name=zfli.getFileName();
						dli.parent=zfli.getParentDirectory();
						dir_list.add(dli);
					}
				}
				sortDirectoryList(dir_list);

				ArrayList<String> pdir_name_list=new ArrayList<String>();
				ArrayList<Boolean> pdir_utf8_list=new ArrayList<Boolean>();
				String prev_parent="";
				for(ZipFileListItem zfli:tfl) {
					if (!prev_parent.equalsIgnoreCase(zfli.getParentDirectory()) && !zfli.getParentDirectory().equals("")) {
						prev_parent=zfli.getParentDirectory();
						pdir_name_list.add(zfli.getParentDirectory());
						pdir_utf8_list.add(zfli.isUtf8Encoding());
					}
				}
//				Log.v("","parent list elapsed="+(System.currentTimeMillis()-b_time)+", size="+pdir_name_list.size());
				
				StringBuilder sb=new StringBuilder(1024);
				for(int i=0;i<pdir_name_list.size();i++) {
					addDirectory(sb, dir_list, tfl, pdir_name_list.get(i), pdir_utf8_list.get(i));
				}

				for(DirectoryListItem dli:dir_list) {
					if (dli.added_directory) {
						ZipFileListItem zfli=new ZipFileListItem(dli.name, dli.parent, true, false, 0, 0, 0, 0, false);
						tfl.add(zfli);
					}
				}
				
//				Log.v("","add dir elapsed="+(System.currentTimeMillis()-b_time));
				Collections.sort(tfl, new Comparator<ZipFileListItem>(){
					@Override
					public int compare(ZipFileListItem lhs, ZipFileListItem rhs) {
						String r_key=rhs.getParentDirectory();
						String l_key=lhs.getParentDirectory();
						if (l_key.equalsIgnoreCase(r_key)) {
							return lhs.getFileName().compareToIgnoreCase(rhs.getFileName());
						} else {
							return l_key.compareToIgnoreCase(r_key);
						}
					}
					
				});
			}
		}catch(ZipException e) {
            slf4jLog.debug("buildZipFileList",e);
        }catch(Exception e) {
            slf4jLog.debug("buildZipFileList",e);
		}
		return tfl;
	};

	/**
	 * Converts input time from Java to DOS format
	 * @param time
	 * @return time in DOS format 
	 */
	public static long javaToDosTime(long time) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		
		int year = cal.get(Calendar.YEAR);
		if (year < 1980) {
		    return (1 << 21) | (1 << 16);
		}
		return (year - 1980) << 25 | (cal.get(Calendar.MONTH) + 1) << 21 |
	               cal.get(Calendar.DATE) << 16 | cal.get(Calendar.HOUR_OF_DAY) << 11 | cal.get(Calendar.MINUTE) << 5 |
	               cal.get(Calendar.SECOND) >> 1;
	}
	
	/**
	 * Converts time in dos format to Java format
	 * @param dosTime
	 * @return time in java format
	 */
	public static long dosToJavaTme(int dosTime) {
		int sec = 2 * (dosTime & 0x1f);
	    int min = (dosTime >> 5) & 0x3f;
	    int hrs = (dosTime >> 11) & 0x1f;
	    int day = (dosTime >> 16) & 0x1f;
	    int mon = ((dosTime >> 21) & 0xf) - 1;
	    int year = ((dosTime >> 25) & 0x7f) + 1980;
	    
	    Calendar cal = Calendar.getInstance();
		cal.set(year, mon, day, hrs, min, sec);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime().getTime();
	}


	public static boolean createZipFile(Context c, ThreadCtrl tc, 
			ProgressBarDialogFragment pbdf, String output_file_path, 
			String default_root_folder, String... input_file_path) {
		return createEncZipFile(c, tc, pbdf, output_file_path, 
				"", Zip4jConstants.ENC_NO_ENCRYPTION, -1, 
				Zip4jConstants.DEFLATE_LEVEL_MAXIMUM, default_root_folder, input_file_path);
	};

	public static boolean createStandardEncZipFile(Context c, ThreadCtrl tc, 
			ProgressBarDialogFragment pbdf, String output_file_path, 
			String default_root_folder, String pswd, String... input_file_path) {
		return createEncZipFile(c, tc, pbdf, output_file_path,
				pswd, Zip4jConstants.ENC_METHOD_STANDARD, -1, 
				Zip4jConstants.DEFLATE_LEVEL_MAXIMUM, default_root_folder, input_file_path);
	};

	public static boolean createAes128EncZipFile(Context c, ThreadCtrl tc, 
			ProgressBarDialogFragment pbdf, String output_file_path, 
			String default_root_folder, String pswd, String... input_file_path) {
		return createEncZipFile(c, tc, pbdf, output_file_path, 
				pswd, Zip4jConstants.ENC_METHOD_AES, Zip4jConstants.AES_STRENGTH_128, 
				Zip4jConstants.DEFLATE_LEVEL_MAXIMUM, default_root_folder, input_file_path);

	};
	public static boolean createAes256EncZipFile(Context c, ThreadCtrl tc, 
			ProgressBarDialogFragment pbdf, String output_file_path, 
			String default_root_folder, String pswd, String... input_file_path) {
		return createEncZipFile(c, tc, pbdf, output_file_path,
				pswd, Zip4jConstants.ENC_METHOD_AES, Zip4jConstants.AES_STRENGTH_256, 
				Zip4jConstants.DEFLATE_LEVEL_MAXIMUM, default_root_folder, input_file_path);

	};

	public static boolean createEncZipFile(Context c, ThreadCtrl tc, 
			ProgressBarDialogFragment pbdf, String output_file_path,
			String pswd, int enc_method, int aes_strength, int comp_level, 
			String default_root_folder, String... input_file_path) {
		InputStream is = null;
		ZipOutputStream zos = null;
		try {
		    zos = new ZipOutputStream(new FileOutputStream(output_file_path));
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
		ZipParameters zp = new ZipParameters();
		zp.setDefaultFolderPath(default_root_folder);
		zp.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		zp.setCompressionLevel(comp_level);
		
		if (enc_method!=Zip4jConstants.ENC_NO_ENCRYPTION) {
			zp.setEncryptFiles(true);
			zp.setEncryptionMethod(enc_method);
			if (enc_method==Zip4jConstants.ENC_METHOD_AES) zp.setAesKeyStrength(aes_strength);
			zp.setPassword(pswd);
		}
		
		long total_size=0;
		for (int i=0; i<input_file_path.length; i++) {
			File lf=new File(input_file_path[i]);
			total_size+=lf.length();
		}
		try {
			long process_size=0;
			byte[] readBuff = new byte[1024*1024*2];
			for (int i=0; i<input_file_path.length; i++) {
				File file = new File(input_file_path[i]);
				
				zos.putNextEntry(file,zp);
				
				if (file.isDirectory()) {
					zos.closeEntry();
					continue;
				}

				if (pbdf!=null) pbdf.updateMsgText(input_file_path[i]);
				
				is = new FileInputStream(file);
				int read_length = -1;
				
				while ((read_length = is.read(readBuff)) != -1) {
					if (tc!=null && !tc.isEnabled()) break; 
					else {
						zos.write(readBuff, 0, read_length);
						process_size+=read_length;
						if (pbdf!=null) {
							int progress=(int)((process_size*100)/total_size);
							pbdf.updateProgress(progress);
						}
					}
				}
				zos.closeEntry();
				is.close();
			}
			zos.finish();
		    zos.close();
		    
		} catch (IOException e) {
            slf4jLog.debug("createEncZipFile",e);
//			e.printStackTrace();
			return false;
		} catch (ZipException e) {
            slf4jLog.debug("createEncZipFile",e);
//			e.printStackTrace();
			return false;
		}
		return true;
	};

}
