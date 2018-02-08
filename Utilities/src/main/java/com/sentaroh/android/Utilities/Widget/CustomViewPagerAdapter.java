package com.sentaroh.android.Utilities.Widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

public class CustomViewPagerAdapter extends PagerAdapter {
	 
    @SuppressWarnings("unused")
	private Context mContext;
    private View[] mViewList;
     
    public CustomViewPagerAdapter(Context context, View[] vl) {
        mContext = context;
        mViewList=vl;
    }
 
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // コンテナに追加
        ((ViewPager)container).addView(mViewList[position]);
        return mViewList[position];
    }
 
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // コンテナから View を削除
    	((ViewPager)container).removeView((View) object);
    }
 
    @Override
    public int getCount() {
        // リストのアイテム数を返す
        return mViewList.length;
    }
 
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (View) object;
    }
 
}