/*
The MIT License (MIT)
Copyright (c) 2011 Sentaroh

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


