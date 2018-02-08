package com.sentaroh.android.Utilities;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import jcifs.netbios.NbtAddress;
import jcifs.smb.NtStatus;
import jcifs.smb.SmbException;

public class NetworkUtil {
	
	final static public boolean isValidIpAddress(String in_addr) {
		boolean result=false;
		String strip_addr=in_addr;
		if (in_addr.indexOf(":")>=0) strip_addr=in_addr.substring(0,in_addr.indexOf(":")) ;
		String[] addr=strip_addr.split("\\.");
		if (addr.length==4) {
			boolean error=false;
			for (int i=0;i<4;i++) {
				try {
					int num=Integer.parseInt(addr[i]);
					if (num<0 || num>255) {
						error=true;
						break;
					} else {
						if (i>2) {
							if (addr[i].startsWith("0")) {
								error=true;
								break;
							}
						}
					}
				} catch(NumberFormatException e) {
					error=true;
					break;
				}
			}
			if (!error) result=true;
		}
		return result;
	};

//	final public static boolean ping(String addr){
//        Runtime runtime = Runtime.getRuntime();
//        Process proc = null;
//        int exitVal=-1;
//        try{
//            proc = runtime.exec("ping -c 1 -W 1 "+addr);
//            proc.waitFor();
//        }catch(Exception e){}
//        exitVal= proc.exitValue();
//        if(exitVal == 0)return true;
//        else return false;
//    }

	final static public String getSmbHostIpAddressFromName(String hn) {
		String ipAddress=null;
		try {
			NbtAddress nbtAddress = NbtAddress.getByName(hn);
			InetAddress address = nbtAddress.getInetAddress();
			ipAddress= address.getHostAddress();
		} catch (UnknownHostException e) {
//			e.printStackTrace();
		}
		return ipAddress;
	}
	
	@SuppressWarnings("unused")
	final static private byte[] mNbtData=new byte[]{
								0x00,0x00,0x00,0x10,0x00,0x01,
								0x00,0x00,0x00,0x00,0x00,0x00,0x20,0x43,
								0x4b,0x41,0x41,0x41,0x41,0x41,0x41,0x41,
								0x41,0x41,0x41,0x41,0x41,0x41,0x41,0x41,
								0x41,0x41,0x41,0x41,0x41,0x41,0x41,0x41,
								0x41,0x41,0x41,0x41,0x41,0x41,0x41,0x00,
								0x00,0x21,0x00,0x01};
	final static public boolean isIpAddressAndPortConnected(String address, int port, int timeout) {
		boolean reachable=false;
        Socket socket = new Socket();
        try {
        	socket.bind(null);
        	socket.connect((new InetSocketAddress(address, port)), timeout);
//            OutputStream os=socket.getOutputStream();
//            os.write(mNbtData);
//            os.flush();
//            os.close();
            reachable=true;
            socket.close();
        } catch (IOException e) {
//        	e.printStackTrace();
        } catch (Exception e) {
//        	e.printStackTrace();
		}
		return reachable;
	};

//	final static public String getSmbHostNameFromAddress(String address) {
//		String srv_name="";
//    	try {
//			UniAddress ua = UniAddress.getByName(address);
//			String cn;
//	        cn = ua.firstCalledName();
//	        while(cn!=null) {
//	            if (!cn.startsWith("*")) srv_name=cn; 
////            	Log.v("","getSmbHostName Address="+address+
////	            		", cn="+cn+", name="+srv_name+", host="+ua.getHostName());
//            	cn = ua.nextCalledName();
//	        } 
////	        while(( cn = ua.nextCalledName() ) != null );
//			
//		} catch (UnknownHostException e) {
////			e.printStackTrace();
//		}
//    	return srv_name;
// 	};
 	
	final static public String getSmbHostNameFromAddress(String address) {
		String srv_name="";
	   	try {
   			NbtAddress[] uax = NbtAddress.getAllByAddress(address);
   			if (uax!=null) {
	   			for(int i=0;i<uax.length;i++) {
					NbtAddress ua = uax[i];
					String hn;
			        hn = ua.firstCalledName();
//	            	Log.v("","getSmbHostName Address="+address+
//		            		", cn="+hn+", hn="+ua.getHostName()+", nametype="+ua.getNameType()+", nodetype="+ua.getNodeType());
	            	if (ua.getNameType()==32) {
	            		srv_name=hn;
	            		break;
	            	}
	   			}
   			}
		} catch (UnknownHostException e) {
		}
    	return srv_name;
 	};
 	

 	final static public boolean isNbtAddressActive(String address) {
		boolean result=false;
		try {
			NbtAddress na=NbtAddress.getByName(address);
			result=na.isActive();
		} catch (UnknownHostException e) {
		}
		return result;
	};

	final static public String[] analyzeNtStatusCode(SmbException e, Context c, 
			String url, String user) {
		String[] result=new String[4];
		
		String host_t1=url.replace("smb://", "");
		String host_id=host_t1;
		String share_name="";
		String file_path="";
		if (host_t1.indexOf("/")>=0) {
			host_id=host_t1.substring(0,host_t1.indexOf("/"));
			String share_t1=host_t1.replace(host_id+"/", "");
			if (share_t1.indexOf("/")>=0) {
				share_name=share_t1.substring(0,share_t1.indexOf("/"));
				String fpath_t1=share_t1.replace(share_name+"/", "");
				file_path=fpath_t1;
			}
		}
		
		result[1]=host_id;
		result[2]=share_name;
		result[3]=file_path;
		
		String msg_text=e.getMessage();
		
		switch(e.getNtStatus()) {
		    case NtStatus.NT_STATUS_OK:
		    	msg_text="";
		    	break;
		    case NtStatus.NT_STATUS_UNSUCCESSFUL:
		    	msg_text=c.getString(R.string.msgs_ntstatus_unsuccesful);
		    	break;
		    case NtStatus.NT_STATUS_NOT_IMPLEMENTED:
		    	break;
		    case NtStatus.NT_STATUS_INVALID_INFO_CLASS:
		    	break;
		    case NtStatus.NT_STATUS_ACCESS_VIOLATION:
		    	break;
		    case NtStatus.NT_STATUS_INVALID_HANDLE:
		    	break;
		    case NtStatus.NT_STATUS_INVALID_PARAMETER:
		    	break;
		    case NtStatus.NT_STATUS_NO_SUCH_DEVICE:
		    	break;
		    case NtStatus.NT_STATUS_NO_SUCH_FILE:
		    	break;
		    case NtStatus.NT_STATUS_MORE_PROCESSING_REQUIRED:
		    	break;
		    case NtStatus.NT_STATUS_ACCESS_DENIED:
		    	msg_text=c.getString(R.string.msgs_ntstatus_access_denied);
		    	break;
		    case NtStatus.NT_STATUS_BUFFER_TOO_SMALL:
		    	break;
		    case NtStatus.NT_STATUS_OBJECT_NAME_INVALID:
		    	break;
		    case NtStatus.NT_STATUS_OBJECT_NAME_NOT_FOUND:
		    	break;
		    case NtStatus.NT_STATUS_OBJECT_NAME_COLLISION:
		    	break;
		    case NtStatus.NT_STATUS_PORT_DISCONNECTED:
		    	break;
		    case NtStatus.NT_STATUS_OBJECT_PATH_INVALID:
		    	break;
		    case NtStatus.NT_STATUS_OBJECT_PATH_NOT_FOUND:
		    	break;
		    case NtStatus.NT_STATUS_OBJECT_PATH_SYNTAX_BAD:
		    	break;
		    case NtStatus.NT_STATUS_SHARING_VIOLATION:
		    	break;
		    case NtStatus.NT_STATUS_DELETE_PENDING:
		    	break;
		    case NtStatus.NT_STATUS_NO_LOGON_SERVERS:
		    	break;
		    case NtStatus.NT_STATUS_USER_EXISTS:
		    	break;
		    case NtStatus.NT_STATUS_NO_SUCH_USER:
		    	break;
		    case NtStatus.NT_STATUS_WRONG_PASSWORD:
		    	break;
		    case NtStatus.NT_STATUS_LOGON_FAILURE:
		    	msg_text=c.getString(R.string.msgs_ntstatus_logon_failure_unknown_user_name_or_bad_password);
		    	break;
		    case NtStatus.NT_STATUS_ACCOUNT_RESTRICTION:
		    	msg_text=c.getString(R.string.msgs_ntstatus_logon_failure_user_account_restriction);
		    	break;
		    case NtStatus.NT_STATUS_INVALID_LOGON_HOURS:
		    	msg_text=c.getString(R.string.msgs_ntstatus_logon_failure_account_logon_time_restriction_violation);
		    	break;
		    case NtStatus.NT_STATUS_INVALID_WORKSTATION:
		    	msg_text=c.getString(R.string.msgs_ntstatus_logon_failure_user_not_allowed_to_log_on_to_this_computer);
		    	break;
		    case NtStatus.NT_STATUS_PASSWORD_EXPIRED:
		    	msg_text=c.getString(R.string.msgs_ntstatus_logon_failure_the_specified_account_password_has_expired);
		    	break;
		    case NtStatus.NT_STATUS_ACCOUNT_DISABLED:
		    	msg_text=c.getString(R.string.msgs_ntstatus_logon_failure_account_currently_disabled);
		    	break;
		    case NtStatus.NT_STATUS_NONE_MAPPED:
		    	break;
		    case NtStatus.NT_STATUS_INVALID_SID:
		    	break;
		    case NtStatus.NT_STATUS_INSTANCE_NOT_AVAILABLE:
		    	break;
		    case NtStatus.NT_STATUS_PIPE_NOT_AVAILABLE:
		    	break;
		    case NtStatus.NT_STATUS_INVALID_PIPE_STATE:
		    	break;
		    case NtStatus.NT_STATUS_PIPE_BUSY:
		    	break;
		    case NtStatus.NT_STATUS_PIPE_DISCONNECTED:
		    	break;
		    case NtStatus.NT_STATUS_PIPE_CLOSING:
		    	break;
		    case NtStatus.NT_STATUS_PIPE_LISTENING:
		    	break;
		    case NtStatus.NT_STATUS_FILE_IS_A_DIRECTORY:
		    	break;
		    case NtStatus.NT_STATUS_DUPLICATE_NAME:
		    	break;
		    case NtStatus.NT_STATUS_NETWORK_NAME_DELETED:
		    	break;
		    case NtStatus.NT_STATUS_NETWORK_ACCESS_DENIED:
		    	break;
		    case NtStatus.NT_STATUS_BAD_NETWORK_NAME:
		    	msg_text=c.getString(R.string.msgs_ntstatus_bad_network_name);
		    	break;
		    case NtStatus.NT_STATUS_REQUEST_NOT_ACCEPTED:
		    	break;
		    case NtStatus.NT_STATUS_CANT_ACCESS_DOMAIN_INFO:
		    	break;
		    case NtStatus.NT_STATUS_NO_SUCH_DOMAIN:
		    	break;
		    case NtStatus.NT_STATUS_NOT_A_DIRECTORY:
		    	break;
		    case NtStatus.NT_STATUS_CANNOT_DELETE:
		    	break;
		    case NtStatus.NT_STATUS_INVALID_COMPUTER_NAME:
		    	break;
		    case NtStatus.NT_STATUS_PIPE_BROKEN:
		    	break;
		    case NtStatus.NT_STATUS_NO_SUCH_ALIAS:
		    	break;
		    case NtStatus.NT_STATUS_LOGON_TYPE_NOT_GRANTED:
		    	break;
		    case NtStatus.NT_STATUS_NO_TRUST_SAM_ACCOUNT:
		    	break;
		    case NtStatus.NT_STATUS_TRUSTED_DOMAIN_FAILURE:
		    	break;
		    case NtStatus.NT_STATUS_NOLOGON_WORKSTATION_TRUST_ACCOUNT:
		    	break;
		    case NtStatus.NT_STATUS_PASSWORD_MUST_CHANGE:
		    	break;
		    case NtStatus.NT_STATUS_NOT_FOUND:
		    	break;
		    case NtStatus.NT_STATUS_ACCOUNT_LOCKED_OUT:
		    	break;
		    case NtStatus.NT_STATUS_PATH_NOT_COVERED:
		    	break;
		    case NtStatus.NT_STATUS_IO_REPARSE_TAG_NOT_HANDLED:
		    	break;
		    default:
		    	break;
		}
		
		result[0]=msg_text+"\n"+
				String.format(c.getString(R.string.msgs_ntstatus_common_info),
						user,e.getNtStatus(), host_id,share_name,file_path);
		return result;
	};

}
