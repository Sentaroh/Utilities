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
package com.sentaroh.android.Utilities.Widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Scroller;

import java.lang.reflect.Field;

public class CustomViewPager extends ViewPager{
	private boolean mSwipeEnabled=true;
	
	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CustomViewPager(Context context) {
		super(context);
		init();
	}
	
	private void init() {
        setMyScroller();
//		setPageTransformer(false, new ViewPager.PageTransformer() {
//		    @Override
//		    public void transformPage(View page, float position) {
//		    	final float normalizedposition = Math.abs(Math.abs(position) - 1);
//
//		        page.setScaleX(normalizedposition / 2 + 0.5f);
//		        page.setScaleY(normalizedposition / 2 + 0.5f);
//		    } 
//		});
	}

    private void setMyScroller() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
//            if (mOriginalScroller==null) mOriginalScroller=scroller.get(this);
            scroller.setAccessible(true);
            scroller.set(this, new MyScroller(getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean mUseFastScroll=false;
    public void setUseFastScroll(boolean use) {
        mUseFastScroll=use;
    }

    public boolean isUseFastScroll() {
        return mUseFastScroll;
    }

    private class MyScroller extends Scroller {
        private MyScroller(Context context) {
            super(context, new FastOutSlowInInterpolator());
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
//            Log.v("","duraton="+duration);
            int new_duration=duration;
            if (isUseFastScroll()) {
                new_duration=0;
            }
            super.startScroll(startX, startY, dx, dy, new_duration);
        }
    }


    @Override
	protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
		if (v instanceof WebView) {
			return false;//((WebView) v).canScrollHor(-dx);
    	} else {
    		return super.canScroll(v, checkV, dx, x, y);
        }
	};

	public void setSwipeEnabled(boolean p) {mSwipeEnabled=p;}
	
	public boolean isSwipeEnabled() {return mSwipeEnabled;}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		if (mSwipeEnabled) return super.onInterceptTouchEvent(arg0);
		else return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mSwipeEnabled) return super.onTouchEvent(event);
		else return false;
	}

}
