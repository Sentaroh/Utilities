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
package com.sentaroh.android.Utilities.Preference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import com.sentaroh.android.Utilities.R;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
/**
 * PasswordDialogPreference
 */
public class PasswordInputPreference extends DialogPreference{
	
	final static boolean DEBUG_ENABLED=true;
	final static String APPLICATION_TAG="PasswordInputPreference";
	
	private EditText newPasswdEditText;
	private EditText confirmPasswdEditText;
	private TextView mDlgMsg;
	
	private Context mContext;

	public PasswordInputPreference(Context context){
		      super(context,null);
		      mContext=context;
		   }
   public PasswordInputPreference(Context context,AttributeSet attrs){
      super(context,attrs);
      mContext=context;
   }
   public PasswordInputPreference(Context context,AttributeSet attrs,int defStyle){
      super(context,attrs,defStyle);
      mContext=context;
   }
   
   @Override
   protected Parcelable onSaveInstanceState() {
	   if (DEBUG_ENABLED) Log.v(APPLICATION_TAG,"onSaveInstanceState entered");
       final Parcelable superState = super.onSaveInstanceState();
       final MySavedState myState = new MySavedState(superState);
       if (newPasswdEditText!=null) myState.pswd=newPasswdEditText.getText().toString();
       if (confirmPasswdEditText!=null) myState.conf_pswd=confirmPasswdEditText.getText().toString();
       return myState;
   };

   private static class MySavedState extends BaseSavedState {
       public String pswd="", conf_pswd="";
		public MySavedState(Parcel source) {
           super(source);
			try {
	            byte[] buf=null;
	            source.readByteArray(buf);
	            ByteArrayInputStream bis = new ByteArrayInputStream(buf);
	            ObjectInputStream ois=new ObjectInputStream(bis);
	            pswd=ois.readUTF();
	            conf_pswd=ois.readUTF();
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
       }
       @Override
       public void writeToParcel(Parcel dest, int flags) {
           super.writeToParcel(dest, flags);
           byte[] buf;
           ByteArrayOutputStream bos = new ByteArrayOutputStream(100000);
           try {
				ObjectOutputStream oos=new ObjectOutputStream(bos);
				oos.writeUTF(pswd);
				oos.writeUTF(conf_pswd);
				oos.close();
				buf=bos.toByteArray();
				dest.writeByteArray(buf);
			} catch (IOException e) {
				e.printStackTrace();
			}
       }
       public MySavedState(Parcelable superState) {
           super(superState);
       }
       @SuppressWarnings("unused")
		public static final Parcelable.Creator<MySavedState> CREATOR =
               new Parcelable.Creator<MySavedState>() {
           public MySavedState createFromParcel(Parcel in) {
               return new MySavedState(in);
           }
           public MySavedState[] newArray(int size) {
               return new MySavedState[size];
           }
       };
   };

   private String mPswd="", mConfPswd="";
   @Override
   protected void onRestoreInstanceState(Parcelable state) {
	   if (DEBUG_ENABLED) Log.v(APPLICATION_TAG,"onRestoreInstanceState entered, state="+state);
       if (state == null) {
           super.onRestoreInstanceState(state);
           return;
       }
       MySavedState myState = (MySavedState) state;
       
       mPswd=myState.pswd;
       mConfPswd=myState.conf_pswd;
       
       super.onRestoreInstanceState(myState.getSuperState());
   };

   @Override
   protected View onCreateDialogView(){
	   if (DEBUG_ENABLED) Log.v(APPLICATION_TAG,"onCreateDialogView entered");
       LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
       View layout = inflater.inflate(R.layout.password_input_preference, null);
       mDlgMsg=(TextView)layout.findViewById(R.id.password_input_preference_msg);
       newPasswdEditText=(EditText)layout.findViewById(R.id.password_input_preference_new_pswd);
       confirmPasswdEditText=(EditText)layout.findViewById(R.id.password_input_preference_conf_pswd);
       newPasswdEditText.setText(mPswd);
       confirmPasswdEditText.setText(mConfPswd);
       newPasswdEditText.setFocusable(true);
       mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_specify_new_password));
       return layout;
   }
   @Override
   protected void onDialogClosed(boolean positiveResult){
	   if (positiveResult){
		   String newPwd = newPasswdEditText.getText().toString();
		   persistString(newPasswdEditText.getText().toString());
		   this.
		   setSummary(createSummaryString(newPwd));
	   }
	   super.onDialogClosed(positiveResult);
   }
   
   @SuppressLint("NewApi")
   @Override
   protected void showDialog(Bundle state) {
	   	if (DEBUG_ENABLED) Log.v(APPLICATION_TAG,"showDialog entered");
	   	super.showDialog(state);
	   	if (Build.VERSION.SDK_INT>10) setIcon(null);
	    final Button btn_ok=((AlertDialog)getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
	    btn_ok.setEnabled(false);
	    checkPassword(btn_ok);	   
	    newPasswdEditText.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				checkPassword(btn_ok);
			}
	    });
	    confirmPasswdEditText.addTextChangedListener(new TextWatcher(){
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				checkPassword(btn_ok);
			}
	    });
   }

   private void checkPassword(Button btn_ok) {
		btn_ok.setEnabled(false);
		if (newPasswdEditText.getText().length()>0 && confirmPasswdEditText.getText().length()>0) {
			if (newPasswdEditText.getText().toString().equals(confirmPasswdEditText.getText().toString())) {
				btn_ok.setEnabled(true);
				mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_match));
			} else {
				mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_unmatch));
			}
		} else {
			if (newPasswdEditText.getText().length()==0) {
				mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_new_not_specified));
			} else {
				mDlgMsg.setText(mContext.getString(R.string.msgs_password_input_preference_conf_not_specified));
			}
		}
   }
   
   @SuppressWarnings("unused")
private AlertDialog.Builder mBuilder=null;
   @Override
   protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
	   mBuilder=builder;
   }

   private String createSummaryString(String newPwd){
      StringBuffer sb = new StringBuffer();
      int x = newPwd.length();
      for(int i=0;i < x;i++){
         sb.append("*");
      }
      return sb.toString();
   }
}