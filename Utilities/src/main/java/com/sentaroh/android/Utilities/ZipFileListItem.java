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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import net.lingala.zip4j.model.FileHeader;
import android.util.Log;

public class ZipFileListItem implements Serializable, Externalizable{
	private String file_name="", parent_directory="";
	private boolean isDirectory=false;
	private boolean isEncrypted=false;
	private boolean isUtf8Encoding=false;
	private long file_uncomp_length=0;
	private long file_comp_length=0;
	private long file_last_mod_time=0;
	
	public static final String ZIP_TYPE_GZ="gz";
	public static final String ZIP_TYPE_ZIP="zip";
	public static final String ZIP_TYPE_TGZ="tgz";

	private String zip_file_type=ZIP_TYPE_ZIP;
	public String getZipFileType() {return zip_file_type;}
	public void setZipFileType(String p) {zip_file_type=p;}
	
	private int zipCompMethod=0;
	public void setCompressionMethod(int p) {zipCompMethod=p;}
	public int getCompressionMethod() {return zipCompMethod;}

	public ZipFileListItem() {};
	
	public ZipFileListItem(String file_name, String parent_dir, boolean isDir, boolean is_enc, 
			long file_length, long last_mod_time, long comp_size, int zip_comp_method, boolean utf8_encoding) {
		this.file_name=file_name;
		this.isDirectory=isDir;
		this.isEncrypted=is_enc;
		this.parent_directory=parent_dir;
		this.file_uncomp_length=file_length;
		this.file_last_mod_time=last_mod_time;
		isUtf8Encoding=utf8_encoding;
		file_comp_length=comp_size;
		zipCompMethod=zip_comp_method;
	}
	
	public void dump() {
		Log.v("ZipFileListItem","File name="+file_name+", Parent="+parent_directory+", isDirectory="+isDirectory+
				", length="+file_uncomp_length+
				", lastModified="+StringUtil.convDateTimeTo_YearMonthDayHourMinSecMili(file_last_mod_time)+
				", UTF8="+isUtf8Encoding);
	}
	public String getFileName() {return file_name;}
	public String getParentDirectory() {return parent_directory;}
	public String getPath() {
		if (parent_directory.equals("")) return file_name;
		else return parent_directory+"/"+file_name;
	}
	public long getFileLength() {return file_uncomp_length;}
	public long getLastModifiedTime() {return file_last_mod_time;}
	public long getCompressedFileLength() {return file_comp_length;}
	public boolean isDirectory() {return isDirectory;}
	public boolean isEncrypted() {return isEncrypted;}
	public boolean isUtf8Encoding() {return isUtf8Encoding;}

	@Override
	public void readExternal(ObjectInput input) throws IOException,
			ClassNotFoundException {
		file_name=input.readUTF();
		parent_directory=input.readUTF();
		isDirectory=input.readBoolean();
		isEncrypted=input.readBoolean();
		isUtf8Encoding=input.readBoolean();
		file_uncomp_length=input.readLong();
		file_last_mod_time=input.readLong();
		zip_file_type=input.readUTF();
		file_comp_length=input.readLong();
		zipCompMethod=input.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeUTF(file_name);
		output.writeUTF(parent_directory);
		output.writeBoolean(isDirectory);
		output.writeBoolean(isEncrypted);
		output.writeBoolean(isUtf8Encoding);
		output.writeLong(file_uncomp_length);
		output.writeLong(file_last_mod_time);
		output.writeUTF(zip_file_type);
		output.writeLong(file_comp_length);
		output.writeInt(zipCompMethod);
	}

}
