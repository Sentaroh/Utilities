package com.sentaroh.android.Utilities.Widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;

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
