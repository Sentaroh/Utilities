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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;

public class EncryptUtil {
	private static final byte[] KEY_PREFIX = new byte[] {
		-47, 66, 32, -127, -102, -51, 79, -69, 57, 85, -91, -42, 74, -116};

	final static public String makeSHA1Hash(String input)
            throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        byte[] buffer = input.getBytes();
        md.update(buffer);
        byte[] digest = md.digest();

        String hexStr = "";
        for (int i = 0; i < digest.length; i++) {
            hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return hexStr;
    };
    
	public static class CipherParms {
		public IvParameterSpec iv=null;
		public SecretKey key=null;
	}

	public static CipherParms initEncryptEnv(String passwd) {
		CipherParms ep=new CipherParms();
		ep.iv=generateInitializationVector(passwd);
		ep.key=generateKey(passwd);
		return ep;
	}

	public static CipherParms initDecryptEnv(String passwd) {
		CipherParms ep=new CipherParms();
		ep.iv=generateInitializationVector(passwd);
		ep.key=generateKey(passwd);
		return ep;
	}

	private static IvParameterSpec generateInitializationVector(String seed) {
		IvParameterSpec iv=null;
		try {
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        md.reset();
	        byte[] buffer = seed.getBytes();
	        md.update(buffer);
	        byte[] digest = md.digest();
	        iv=new IvParameterSpec(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return iv;
	}
	
	/**
	 * 共通鍵の生成
	 */
	private static SecretKey generateKey(String passWordStr) { 
		SecretKey secretKey=null;
		if (passWordStr!=null) {
			try {
				char[] password = passWordStr.toCharArray();
				SecretKeyFactory factory;
				KeySpec keySpec = new PBEKeySpec(password, KEY_PREFIX, 1024, 256);
				factory = SecretKeyFactory.getInstance("PBEWITHSHAAND256BITAES-CBC-BC");
				secretKey = factory.generateSecret(keySpec);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
			}
		}
		return secretKey;
	};

	private static final String ALGORITM = "AES/CBC/PKCS5Padding";

	/**
	 * 暗号化した文字列を取得する
	 */
	public static byte[] encrypt(final String inStr, CipherParms ep){
		byte[] encrypted=null;
		if (inStr!=null && ep!=null) {
			try {
		        MessageDigest md = MessageDigest.getInstance("MD5");
		        md.reset();
		        String time=Long.toString(System.currentTimeMillis());
		        byte[] buffer = time.getBytes();
		        md.update(buffer);
		        byte[] digest = md.digest();
		        String md_str=StringUtil.getHexString(digest, 0, 16).substring(0, 8)
		        		+inStr;

				Cipher cipher=Cipher.getInstance(ALGORITM);
				cipher.init(Cipher.ENCRYPT_MODE, ep.key,ep.iv);
				encrypted = cipher.doFinal(md_str.getBytes());
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (InvalidAlgorithmParameterException e) {
				e.printStackTrace();
			}
		}
		return encrypted;
	};

	/** 
	 * 復号した文字列を取得する 
	 */
	public static String decrypt(final byte[] encrypted_array, CipherParms ep){
		String decrypted_data=null;
		if (encrypted_array!=null && ep!=null) { 
			try {
				Cipher cipher=Cipher.getInstance(ALGORITM);
				cipher.init(Cipher.DECRYPT_MODE, ep.key,ep.iv);
				byte[] decrypted_array = cipher.doFinal(encrypted_array);
				if (decrypted_array!=null) {
					decrypted_data = (new String(decrypted_array)).substring(8);
				}
			} catch (IllegalBlockSizeException e) {
				e.printStackTrace();
			} catch (BadPaddingException e) {
//				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (InvalidAlgorithmParameterException e) {
				e.printStackTrace();
			}
		}
		return decrypted_data;
	};
}
