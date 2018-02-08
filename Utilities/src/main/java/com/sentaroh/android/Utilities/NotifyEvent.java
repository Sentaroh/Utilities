package com.sentaroh.android.Utilities;

/*
 * 下記サイトを参考に作成
 * http://techbooster.org/android/application/9054/
 */

import java.util.EventListener;

import android.content.Context;

public class NotifyEvent {

	public interface NotifyEventListener extends EventListener {
	    public void positiveResponse(Context c,Object[] o);
	    public void negativeResponse(Context c,Object[] o);
	}

	private NotifyEventListener listener = null;
	private Context context ;
	
	public NotifyEvent(Context c) {
		context=c;
	}

	public void notifyToListener(boolean isPositive, Object[] o) {
		if (isPositive) listener.positiveResponse(context,o);
		else listener.negativeResponse(context,o);
	}

	/**
	 * リスナーを追加する
	 * 
	 * @param listener
	 */
	public void setListener(NotifyEventListener listener) {
		this.listener = listener;
	}

	/**
	 * リスナーを削除する
	 */
	public void removeListener() {
		this.listener = null;
	}

}


