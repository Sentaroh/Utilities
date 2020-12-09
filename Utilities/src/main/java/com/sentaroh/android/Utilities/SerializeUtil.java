package com.sentaroh.android.Utilities;

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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

final public class SerializeUtil {
	final static public String[] readArrayString(ObjectInput input) throws IOException{
		int lsz=input.readInt();
		String[] result=null;
		if (lsz!=-1) {
			result=new String[lsz];
			for (int i=0;i<lsz;i++) {
				if (input.readByte()==0x00) result[i]=null;
				else result[i]=input.readUTF();
			}
		}
		return result;
	};
	
	final static public void writeArrayString(ObjectOutput output, String[] al) throws IOException {
		int lsz=-1;
		if (al!=null) {
			if (al.length!=0) lsz=al.length;
			else lsz=0;
		}
		output.writeInt(lsz);
		if (lsz>0) {
			for (int i=0;i<lsz;i++) {
				if (al[i]==null) output.writeByte(0x00);
				else {
					output.writeByte(0x01);
					output.writeUTF(al[i]);
				}
			}
		}
	};

	final static public long[] readArrayLong(ObjectInput input) throws IOException{
		int lsz=input.readInt();
		long[] result=null;
		if (lsz!=-1) {
			result=new long[lsz];
			for (int i=0;i<lsz;i++) {
				result[i]=input.readLong();
			}
		}
		return result;
	};
	
	final static public void writeArrayLong(ObjectOutput output, long[] al) throws IOException {
		int lsz=-1;
		if (al!=null) {
			if (al.length!=0) lsz=al.length;
			else lsz=0;
		}
		output.writeInt(lsz);
		if (lsz>0) {
			for (int i=0;i<lsz;i++) {
				output.writeLong(al[i]);
			}
		}
	};

	final static public int[] readArrayInt(ObjectInput input) throws IOException{
		int lsz=input.readInt();
		int[] result=null;
		if (lsz!=-1) {
			result=new int[lsz];
			for (int i=0;i<lsz;i++) {
				result[i]=input.readInt();
			}
		}
		return result;
	};
	
	final static public void writeArrayInt(ObjectOutput output, int[] al) throws IOException {
		int lsz=-1;
		if (al!=null) {
			if (al.length!=0) lsz=al.length;
			else lsz=0;
		}
		output.writeInt(lsz);
		if (lsz>0) {
			for (int i=0;i<lsz;i++) {
				output.writeInt(al[i]);
			}
		}
	};

	final static public boolean[] readArrayBoolean(ObjectInput input) throws IOException{
		int lsz=input.readInt();
		boolean[] result=null;
		if (lsz!=-1) {
			result=new boolean[lsz];
			for (int i=0;i<lsz;i++) {
				result[i]=input.readBoolean();
			}
		}
		return result;
	};
	
	final static public void writeArrayBoolean(ObjectOutput output, boolean[] al) throws IOException {
		int lsz=-1;
		if (al!=null) {
			if (al.length!=0) lsz=al.length;
			else lsz=0;
		}
		output.writeInt(lsz);
		if (lsz>0) {
			for (int i=0;i<lsz;i++) {
				output.writeBoolean(al[i]);
			}
		}
	};

	final static public float[] readArrayFloat(ObjectInput input) throws IOException{
		int lsz=input.readInt();
		float[] result=null;
		if (lsz!=-1) {
			result=new float[lsz];
			for (int i=0;i<lsz;i++) {
				result[i]=input.readFloat();
			}
		}
		return result;
	};
	
	final static public void writeArrayFloat(ObjectOutput output, float[] al) throws IOException {
		int lsz=-1;
		if (al!=null) {
			if (al.length!=0) lsz=al.length;
			else lsz=0;
		}
		output.writeInt(lsz);
		if (lsz>0) {
			for (int i=0;i<lsz;i++) {
				output.writeFloat(al[i]);
			}
		}
	};

	final static public double[] readArrayDouble(ObjectInput input) throws IOException{
		int lsz=input.readInt();
		double[] result=null;
		if (lsz!=-1) {
			result=new double[lsz];
			for (int i=0;i<lsz;i++) {
				result[i]=input.readDouble();
			}
		}
		return result;
	};
	
	final static public void writeArrayDouble(ObjectOutput output, double[] al) throws IOException {
		int lsz=-1;
		if (al!=null) {
			if (al.length!=0) lsz=al.length;
			else lsz=0;
		}
		output.writeInt(lsz);
		if (lsz>0) {
			for (int i=0;i<lsz;i++) {
				output.writeDouble(al[i]);
			}
		}
	};

	final static public Object readArrayList(ObjectInput input) throws IOException, ClassNotFoundException{
		int lsz=input.readInt();
		ArrayList<Object> result=null;
		if (lsz!=-1) {
			result=new ArrayList<Object>();
			for (int i=0;i<lsz;i++) {
				Object tai=new Object();
				tai=input.readObject();
				result.add(tai);
			}
		}
		return result;
	};

	@SuppressWarnings("unchecked")
	final static public void writeArrayList(ObjectOutput output, Object io) throws IOException {
		ArrayList<Object> al=(ArrayList<Object>)io;
		int lsz=-1;
		if (al!=null) {
			if (al.size()!=0) lsz=al.size();
			else lsz=0;
		}
		output.writeInt(lsz);
		if (lsz>0) {
			for (int i=0;i<lsz;i++) {
				output.writeObject(al.get(i));
			}
		}
	};
	
	final static public String readUtf(ObjectInput input) throws IOException, ClassNotFoundException{
		if (input.readByte()==0x00) return null;
		else return input.readUTF();
	};
	
	final static public void writeUtf(ObjectOutput output, String str) throws IOException {
		if (str==null) output.writeByte(0x00);
		else {
			output.writeByte(0x01);
			output.writeUTF(str);
		}

	};
}
