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
package com.sentaroh.android.Utilities.Widget;

import com.sentaroh.android.Utilities.R;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

public class OverScrollEffectViewPager extends ViewPager {

	/**
	 * maximum z distance to translate child view in dip/dp
	 */
	final static int DEFAULT_OVERSCROLL_TRANSLATION = 300;
	
	/**
	 * duration of overscroll animation in ms
	 */
	final private static int DEFAULT_OVERSCROLL_ANIMATION_DURATION = 400;

	private boolean LOG_ENABLED=false;
	private final static String DEBUG_TAG = "OverScrollEffectViewPager";
	private final static int INVALID_POINTER_ID = -1;
	
	/**
	 * 
	 * Rename and extended by F.Hoshino(2014/01/18)
	 *    BounceBackViewPager -> OverScrollEffectViewPager
	 * 
	 * @author renard, extended by Piotr Zawadzki
	 * 
	 * http://stackoverflow.com/questions/13759862/android-viewpager-how-to-achieve-the-bound-effect
	 * 
	 */
	private class OverscrollEffect {
	    private float mOverscroll;
	    private Animator mAnimator;
	    
	    
		public OverscrollEffect() {
			LOG_ENABLED=false;
	    }
	    
	    /**
	     * @param deltaDistance [0..1] 0->no overscroll, 1>full overscroll
	     */
		final public void setPull(final float deltaDistance) {
			if (LOG_ENABLED) 
				Log.v(DEBUG_TAG,"OverscrollEffect setPull="+deltaDistance);
	        mOverscroll = deltaDistance;
	        invalidateVisibleChilds();
	    }
	
	    /**
	     * called when finger is released. starts to animate back to default position
	     */
		final private void onRelease() {
	        if (mAnimator != null && mAnimator.isRunning()) {
	            mAnimator.addListener(new AnimatorListener() {
	
	                @Override
	                public void onAnimationStart(Animator animation) {
	                	if (LOG_ENABLED) 
	                		Log.v(DEBUG_TAG,"OverscrollEffect onRelase.onAnimationStart");
	                }
	
	                @Override
	                public void onAnimationRepeat(Animator animation) {
	                	if (LOG_ENABLED) 
	                		Log.v(DEBUG_TAG,"OverscrollEffect onRelase.onAnimationRepeat");
	                }
	
	                @Override
	                public void onAnimationEnd(Animator animation) {
	                	if (LOG_ENABLED) 
	                		Log.v(DEBUG_TAG,"OverscrollEffect onRelase.onAnimationEnd");
	                    startAnimation(0);
	                }
	
	                @Override
	                public void onAnimationCancel(Animator animation) {
	                	if (LOG_ENABLED) 
	                		Log.v(DEBUG_TAG,"OverscrollEffect onRelase.onAnimationCancel");
	                }
	            });
	            mAnimator.cancel();
	        } else {
	            startAnimation(0);
	        }
	    }
	
		final private void startAnimation(final float target) {
			if (LOG_ENABLED) 
				Log.v(DEBUG_TAG,"OverscrollEffect startAnimation="+target);
	        mAnimator = ObjectAnimator.ofFloat(this, "pull", mOverscroll, target);
	        mAnimator.setInterpolator(new DecelerateInterpolator());
	        
	        final float scale = Math.abs(target - mOverscroll);
	        mAnimator.setDuration((long) (mOverscrollAnimationDuration * scale));
	        mAnimator.start();
	    }
	
		final private boolean isOverscrolling() {
	        final boolean isLast = (getAdapter().getCount() - 1) == mScrollPosition;
	        boolean result=false;
	        if (mScrollPosition == 0 && mOverscroll < 0) {
	        	result=true;
	        }
	        if (isLast && mOverscroll > 0) {
	        	result=true;
	        }
			if (LOG_ENABLED) 
				Log.v(DEBUG_TAG,"OverscrollEffect isOverscrolling mScrollPosition="+mScrollPosition+", mOverscroll="+mOverscroll+
						", isLast="+isLast+", result="+result);
	        return result;
	    }
	
	}
	
	private OverscrollEffect mOverscrollEffect =null;
	final private Camera mCamera = new Camera();
	
	private OnPageChangeListener mScrollListener=null;
	private float mLastMotionX;
	private int mActivePointerId;
	private int mScrollPosition;
	private float mScrollPositionOffset;
	private int mTouchSlop;
	
	private float mOverscrollTranslation=DEFAULT_OVERSCROLL_TRANSLATION;
	private int mOverscrollAnimationDuration=DEFAULT_OVERSCROLL_ANIMATION_DURATION;
	
	
//	private Context mContext=null;
	public OverScrollEffectViewPager(Context context) throws Exception {
	    super(context);
//	    mContext=context;
	    initCommon(context, null);
	}

	public OverScrollEffectViewPager(Context context, AttributeSet attrs) 
			throws Exception {
	    super(context, attrs);
//	    mContext=context;
	    initCommon(context,attrs);
	}

	@SuppressWarnings("deprecation")
	private void initCommon(Context c, AttributeSet attrs) {
//	    Resources res=c.getResources();
	    setStaticTransformationsEnabled(true);

	    final ViewConfiguration configuration = ViewConfiguration.get(c);
	    mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
	    super.setOnPageChangeListener(new MyOnPageChangeListener());

		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.OverscrollEffectViewPager);

		mOverscrollTranslation= 
	    		a.getDimension(R.styleable.OverscrollEffectViewPager_translation, 
	    				DEFAULT_OVERSCROLL_TRANSLATION);
	    mOverscrollAnimationDuration =
	    		a.getInt(R.styleable.OverscrollEffectViewPager_animation_duration, 
	    				DEFAULT_OVERSCROLL_ANIMATION_DURATION); 

	    mOverscrollEffect =new OverscrollEffect();

	    a.recycle();
	}
	
	final public int getOverscrollAnimationDuration() {
	    return mOverscrollAnimationDuration;
	}
	
	final public void setOverscrollAnimationDuration(int mOverscrollAnimationDuration) {
	    this.mOverscrollAnimationDuration = mOverscrollAnimationDuration;
	}
	
	final public float getOverscrollTranslation() {
	    return mOverscrollTranslation;
	}
	
	final public void setOverscrollTranslation(int mOverscrollTranslation) {
	    this.mOverscrollTranslation = mOverscrollTranslation;
	}
	
	@Override
	final public void setOnPageChangeListener(OnPageChangeListener listener) {
	    mScrollListener = listener;
	};
	
	final private void invalidateVisibleChilds() {
		if (LOG_ENABLED) 
			Log.v(DEBUG_TAG,"invalidateVisibleChilds");
		for (int i = 0; i < getChildCount(); i++) {
			getChildAt(i).invalidate();
		}
	}
	
//	private int mLastPosition = 0;
	
	final private class MyOnPageChangeListener implements OnPageChangeListener {
	
	    @Override
	    final public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	    	if (LOG_ENABLED) 
	    		Log.v(DEBUG_TAG,"MyOnPageChangeListener onPageScrolled pos="+position);
	        if (mScrollListener != null) {
	            mScrollListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
	        }
	        mScrollPosition = position;
	        mScrollPositionOffset = positionOffset;
//	        mLastPosition = position;
	        invalidateVisibleChilds();
	    }
	
	    @Override
	    final public void onPageSelected(int position) {
	    	if (LOG_ENABLED) 
	    		Log.v(DEBUG_TAG,"MyOnPageChangeListener onPageSelected pos="+position);
	        if (mScrollListener != null) {
	            mScrollListener.onPageSelected(position);
	        }
	    }
	
	    @Override
	    final public void onPageScrollStateChanged(final int state) {
	    	if (LOG_ENABLED) 
	    		Log.v(DEBUG_TAG,"MyOnPageChangeListener onPageScrollStateChanged state="+state);
	        if (mScrollListener != null) {
	            mScrollListener.onPageScrollStateChanged(state);
	        }
	        if (state == SCROLL_STATE_IDLE) {
	            mScrollPositionOffset = 0;
	        }
	    }
	}
	
	@Override
	final public boolean onInterceptTouchEvent(MotionEvent ev) {
	    try {
	        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
	        switch (action) {
	        case MotionEvent.ACTION_DOWN: {
	            mLastMotionX = ev.getX();
	            mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
	            break;
	        }
	        case MotionEventCompat.ACTION_POINTER_DOWN: {
	            final int index = MotionEventCompat.getActionIndex(ev);
	            final float x = MotionEventCompat.getX(ev, index);
	            mLastMotionX = x;
	            mActivePointerId = MotionEventCompat.getPointerId(ev, index);
	            break;
	        }
	        }
	        return super.onInterceptTouchEvent(ev);
	    } catch (IllegalArgumentException e) {
	        e.printStackTrace();
	        return false;
	    } catch (ArrayIndexOutOfBoundsException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	@Override
	final public boolean dispatchTouchEvent(MotionEvent ev) {
	    try {
	        return super.dispatchTouchEvent(ev);
	    } catch (IllegalArgumentException e) {
	        e.printStackTrace();
	        return false;
	    } catch (ArrayIndexOutOfBoundsException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	final public boolean onTouchEvent(MotionEvent ev) {
		if (LOG_ENABLED)
			Log.v(DEBUG_TAG,"onTouchEvent entered");
	    boolean callSuper = false;
	    final int action = ev.getAction();
	    switch (action) {
	    case MotionEvent.ACTION_DOWN: {
	        callSuper = true;
	        mLastMotionX = ev.getX();
	        mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
	        break;
	    }
	    case MotionEventCompat.ACTION_POINTER_DOWN: {
	        callSuper = true;
	        final int index = MotionEventCompat.getActionIndex(ev);
	        final float x = MotionEventCompat.getX(ev, index);
	        mLastMotionX = x;
	        mActivePointerId = MotionEventCompat.getPointerId(ev, index);
	        break;
	    }
	    case MotionEvent.ACTION_MOVE: {
	    	if (LOG_ENABLED) 
	    		Log.v(DEBUG_TAG,"onTouchEvent.ACTION_MOVE mActivePointerId="+mActivePointerId);
	        if (mActivePointerId != INVALID_POINTER_ID) {
	            // Scroll to follow the motion event
	            final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
	            final float x = MotionEventCompat.getX(ev, activePointerIndex);
	            final float deltaX = mLastMotionX - x;
	            final float oldScrollX = getScrollX();
	            final int width = getWidth();
	            final int widthWithMargin = width + getPageMargin();
	            final int lastItemIndex = getAdapter().getCount() - 1;
	            final int currentItemIndex = getCurrentItem();
	            final float leftBound = Math.max(0, (currentItemIndex - 1) * widthWithMargin);
	            final float rightBound = Math.min(currentItemIndex + 1, lastItemIndex) * widthWithMargin;
	            final float scrollX = oldScrollX + deltaX;
	            if (mScrollPositionOffset == 0) {
	                if (scrollX < leftBound) {
	                    if (leftBound == 0) {
	                        final float over = deltaX + mTouchSlop;
	                        mOverscrollEffect.setPull(over / width);
	                    }
	                } else if (scrollX > rightBound) {
	                    if (rightBound == lastItemIndex * widthWithMargin) {
	                        final float over = scrollX - rightBound - mTouchSlop;
	                        mOverscrollEffect.setPull(over / width);
	                    }
	                }
	            } else {
	                mLastMotionX = x;
	            }
	        } else {
	            mOverscrollEffect.onRelease();
	        }
	        break;
	    }
	    case MotionEvent.ACTION_UP:
	    case MotionEvent.ACTION_CANCEL: {
	        callSuper = true;
	        mActivePointerId = INVALID_POINTER_ID;
	        mOverscrollEffect.onRelease();
	        break;
	    }
	    case MotionEvent.ACTION_POINTER_UP: {
	        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
	        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
	        if (pointerId == mActivePointerId) {
	            // This was our active pointer going up. Choose a new
	            // active pointer and adjust accordingly.
	            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
	            mLastMotionX = ev.getX(newPointerIndex);
	            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
	            callSuper = true;
	        }
	        break;
	    }
	    }
	
	    if (mOverscrollEffect.isOverscrolling() && !callSuper) {
	        return true;
	    } else {
	        return super.onTouchEvent(ev);
	    }
	}
	
	@Override
	final protected boolean getChildStaticTransformation(
			View child, Transformation t) {
	    if (child.getWidth() == 0) return false;
	    final int position = child.getLeft() / child.getWidth();
	    final boolean isFirstOrLast = position == 0 || (position == getAdapter().getCount() - 1);
	    if (mOverscrollEffect.isOverscrolling() && isFirstOrLast) {
	        final float dx = getWidth() / 2;
	        final int dy = getHeight() / 2;
	        t.getMatrix().reset();
	        final float translateX =(float) mOverscrollTranslation * (mOverscrollEffect.mOverscroll > 0 ? Math.min(mOverscrollEffect.mOverscroll, 1) : Math.max(mOverscrollEffect.mOverscroll, -1));
	        mCamera.save();
	        mCamera.translate(-translateX, 0, 0);
	        mCamera.getMatrix(t.getMatrix());
	        mCamera.restore();
	        t.getMatrix().preTranslate(-dx, -dy);
	        t.getMatrix().postTranslate(dx, dy);
	        if (getChildCount() == 1) {
	            this.invalidate();
	        } else {
	            child.invalidate();
	        }
	        return true;
	    }
	    return false;
	}
}