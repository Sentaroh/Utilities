package com.sentaroh.android.Utilities.Widget;

/*
The MIT License (MIT)
Copyright (c) 2011-2013 Sentaroh

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

import com.sentaroh.android.Utilities.R;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

public class CustomTextView extends View {

	private static final boolean debug_enabled=false;
	
	public static final int LINE_BREAK_NOTHING=0;
	public static final int LINE_BREAK_NO_WORD_WRAP=1;
	public static final int LINE_BREAK_WORD_WRAP=2;
	
	class CommonParms {
		public Context context=null;
		
		public int viewLineBreak=1;
		public int viewHorizontalPos=0;
		
		public String viewTextData="";
		public int viewTextColor=Color.LTGRAY;
		public int viewTextSize=15;
		public Resources resources;
		public int viewWidth=500,viewHeight=20;
		public int drawWidthOffset=0;
		public Typeface viewTypeFace;
		public long view_id=0;
		
		public String[] viewSplitText=new String[1024];
		public int viewSplitTextSize=0;
		
		public Paint text_paint;
		public int viewVertOffset=2;
		public int viewLineSpacing=10;
		
		public int view_measured_width=0,view_measured_height=0;
	}
	
	private CommonParms cparms=new CommonParms();
	
	public CustomTextView(Context context) {
		super(context);
		setFocusable(true);
		cparms.context=context;
		cparms.resources=context.getResources();
		this.setWillNotDraw(false);

		cparms.viewTextData="";
		initResources(null, cparms);
		cparms.view_id=System.currentTimeMillis();
		
	}
	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		cparms.resources=context.getResources();
		this.setWillNotDraw(false);
		cparms.viewTextData="";
		cparms.view_id=System.currentTimeMillis();
		initResources(attrs,cparms);
        
	}
	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		cparms.resources=context.getResources();
		this.setWillNotDraw(false);
		cparms.viewTextData="";
		cparms.view_id=System.currentTimeMillis();
		initResources(attrs,cparms);
	}

	final private void initResources(AttributeSet attrs, CommonParms cparms) {
		cparms.drawWidthOffset=(int)toPixel(cparms.resources,0);
		cparms.viewWidth=cparms.resources.getDisplayMetrics().widthPixels;
//		viewHeight=(int)toPixel(resources,viewTextSize)+viewLineSpacing;

		cparms.text_paint=new Paint();

		cparms.viewTypeFace=Typeface.create(Typeface.MONOSPACE,Typeface.NORMAL);
		cparms.text_paint.setSubpixelText(true);
		cparms.text_paint.setColor(cparms.viewTextColor);
		cparms.text_paint.setTextSize(cparms.viewTextSize);
		cparms.text_paint.setTypeface(cparms.viewTypeFace);
		cparms.text_paint.setAntiAlias(true);
		
		cparms.viewLineSpacing=(int)(int)toPixel(cparms.resources,6);
		cparms.viewVertOffset=(int)(int)toPixel(cparms.resources,2);

		
//		setTextSize(getCvParmsInt(cparms,attrs, "cvTextSize",10));
//		setText(getCvParmsString(cparms,attrs, "cvText",""));
		
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomTextView);
		float text_size= 
	    		a.getDimension(R.styleable.CustomTextView_textSize, 15);
		String text= a.getString(R.styleable.CustomTextView_text);
	    a.recycle();
		setTextSizePx(text_size);
		if (text!=null) setText(text);
		
		if (debug_enabled) {
			debugMsg(cparms,"initResources","text size="+cparms.viewTextSize+
					". view width="+cparms.viewWidth+", viewHeight="+cparms.viewHeight+
					", draw width offset="+cparms.drawWidthOffset);
			debugMsg(cparms,"initResources","line_break="+cparms.viewLineBreak);
		}
	};
	
	final public void setTextSize(int dp) {
		cparms.viewTextSize=(int)toPixel(cparms.resources,dp);
//		viewHeight=(int)toPixel(resources,dp)+viewLineSpacing;
		cparms.text_paint.setTextSize(cparms.viewTextSize);
		if (debug_enabled) debugMsg(cparms,"setTextSize","width="+cparms.viewWidth+", height="+cparms.viewHeight+
				", text size="+cparms.viewTextSize+", draw wwidth="+cparms.drawWidthOffset);
		
	};
	final public void setTextSizePx(float px) {
		cparms.viewTextSize=(int)px;
//		viewHeight=(int)toPixel(resources,dp)+viewLineSpacing;
		cparms.text_paint.setTextSize(cparms.viewTextSize);
		if (debug_enabled) debugMsg(cparms,"setTextSize","width="+cparms.viewWidth+", height="+cparms.viewHeight+
				", text size="+cparms.viewTextSize+", draw wwidth="+cparms.drawWidthOffset);
		
	};
	
//	final static private int getCvParmsInt(CommonParms cparms,AttributeSet attrs,String key, int defval) {
//		String val="10";
//		if (attrs!=null) {
//			val= attrs.getAttributeValue(null, key);
//			if (val==null) return defval;
//		} else return defval;
//		return Integer.valueOf(val);
//	};
//
//	final static private String getCvParmsString(CommonParms cparms, AttributeSet attrs,String key, String defval) {
//		String val="";
//		if (attrs!=null) {
//			val= attrs.getAttributeValue(null, key);
//			if (val==null) return defval;
//			else {
//				if (val.startsWith("@string/")) {
//					int rid=attrs.getAttributeResourceValue(null, key, -1);
//					if (rid!=-1) {
//						val=cparms.resources.getString(rid);
//					}
//				}  
//			}
//		} else return defval;
//		return val;
//	};

	final static private void debugMsg(CommonParms cparms, String m, String msg) {
		Log.v("CustomeView","id="+cparms.view_id+" "+m+" "+msg);
	};
	
	final public void setCVHorizontalPosition(int p) {
		cparms.viewHorizontalPos=p;
	};
	final public int getCVHorizontalPosition() {
		return cparms.viewHorizontalPos;
	}
	@Override
	final protected void onDraw(Canvas canvas) {
//		createDrawTextInfo();
		if (debug_enabled) debugMsg(cparms,"onDraw","textlength="+cparms.viewTextData.length()+
				", width="+cparms.viewWidth+", height="+cparms.viewHeight+
				", text size="+cparms.viewTextSize+", horizontal pos="+cparms.viewHorizontalPos);
		int vert_pos=0;
		for (int i=0;i<cparms.viewSplitTextSize;i++) {
			vert_pos=cparms.viewTextSize*(i+1)+cparms.viewLineSpacing*i+cparms.viewVertOffset;
			if (cparms.viewSplitText[i]!=null && cparms.viewSplitText[i].length()!=0) {
				if (cparms.viewLineBreak==0) {//No line break
					int s_pos=0,e_pos=0;
					if (cparms.viewSplitText[i].length()>=cparms.viewHorizontalPos) {
						s_pos=cparms.viewHorizontalPos;
						int nc=cparms.text_paint.breakText(cparms.viewSplitText[i],
								true, cparms.view_measured_width-10, null);
						if ((nc+s_pos)>cparms.viewSplitText[i].length())
							e_pos=cparms.viewSplitText[i].length();
						else e_pos=s_pos+nc;
						canvas.drawText(cparms.viewSplitText[i],
								s_pos,e_pos,0,vert_pos, cparms.text_paint);
					} else {
						canvas.drawText(" ",0,1,0,vert_pos, cparms.text_paint);
					}
				} else {//Line break
					canvas.drawText( cparms.viewSplitText[i],0, vert_pos, cparms.text_paint);
				}
			} else {
				canvas.drawText( " ", 0, vert_pos, cparms.text_paint);
			}
		}
	};

	@Override
	final protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (debug_enabled) debugMsg(cparms,"onSizeChanged","textlength="+cparms.viewTextData.length()+
        		", new width="+w+", new height="+h+
        		", old width="+oldw+", old height="+oldh);
    };
	
    final static private int createDrawTextInfo(CommonParms cparms) {
		int number_of_lines=0;
		cparms.text_paint.setColor(cparms.viewTextColor);
		cparms.text_paint.setTextSize(cparms.viewTextSize);
		cparms.text_paint.setTypeface(cparms.viewTypeFace);
		
		if (cparms.viewTextData!=null) {
			if (cparms.viewLineBreak==0) {//Non line break
//				viewSplitText=viewTextData.split("\n");
				cparms.viewSplitText[0]=cparms.viewTextData;
				cparms.viewSplitTextSize=1;
				number_of_lines=1;
//				number_of_lines=1;
//				viewSplitText=new String[]{viewTextData};
			} else if (cparms.viewLineBreak==1) {//Line break with Non word wrap 
				number_of_lines=splitTExtNoWordWrap(cparms);
			} else if (cparms.viewLineBreak==2) {//Line break by right edge with word wrap
				number_of_lines=splitTExtWordWrap(cparms);
			}
		}
        if (debug_enabled) 
        	debugMsg(cparms,"createDrawTextInfo","textlength="+cparms.viewTextData.length()+
        		", No of lines="+number_of_lines);
		return number_of_lines;
	};
	
	final static private int getNumberOfCharsPerLine(CommonParms cparms,String text) {
		int nc=cparms.text_paint.breakText(text, true, cparms.viewWidth-cparms.drawWidthOffset, null);
//		if (debug_enabled) debugMsg("getNumberOfCharsPerLine","textlength="+viewTextData.length()+",No of chars="+nc);
		return nc;
	};
	
	final static private int splitTExtNoWordWrap(CommonParms cparms) {
		final String f_string=cparms.viewTextData;
		int number_of_lines=0;
		int t_nc=0, s_nc=0;
		cparms.viewSplitTextSize=0;
		int f_end=f_string.length();
		while (s_nc<f_end) {
			int t_end=s_nc+getNumberOfCharsPerLine(cparms,f_string.substring(s_nc));
			if (t_end>=f_end) t_nc=f_end;
			else t_nc=t_end;
//			Log.v("","incre="+getNumberOfCharsPerLine(cparms,f_string.substring(s_nc)));
//			Log.v("","f_str="+f_string.length()+", s_nc="+s_nc+", t_nc="+t_nc);
			if (f_string.substring(s_nc, t_nc).indexOf("\n")>=0) {
				t_nc=s_nc+f_string.substring(s_nc, t_nc).indexOf("\n");
			}
			if (t_nc==s_nc) {//Ignore NL
//				number_of_lines++;
//				t_string+=sep+"";
				s_nc++;
			} else {
				if (f_string.length()>s_nc) {
					cparms.viewSplitText[number_of_lines]=f_string.substring(s_nc,t_nc);
					s_nc=t_nc;
					number_of_lines++;
				} else {
					cparms.viewSplitText[number_of_lines]=f_string.substring(s_nc);
					number_of_lines++;
					s_nc=t_nc;
					break;
				}
			}
		}
		cparms.viewSplitTextSize =number_of_lines;
		return number_of_lines;
	};
	
	final static private int splitTExtWordWrap(CommonParms cparms) {
		String f_string=cparms.viewTextData;
		String tmp_out;
		int number_of_lines=0;
		int t_nc=0;
		int nl=0;
		cparms.viewSplitTextSize=0;
		while (f_string.length()>0) {
			t_nc=getNumberOfCharsPerLine(cparms,f_string);
			if (f_string.substring(0, t_nc).indexOf("\n")>=0) {
				t_nc=f_string.substring(0, t_nc).indexOf("\n");
			}
			if (t_nc==0) {//Ignore NL
//				number_of_lines++;
//				t_string+=sep+"";
				f_string=f_string.substring(1);
			} else {
				if (f_string.length()>t_nc) {
					tmp_out=f_string.substring(0,t_nc);
					if (tmp_out.endsWith(" ")) {
						cparms.viewSplitText[number_of_lines]=tmp_out;
						number_of_lines++;
						f_string=f_string.substring(t_nc);
					} else {
						if (tmp_out.lastIndexOf(" ")<0) nl=t_nc;
						else nl=tmp_out.lastIndexOf(" ");
						cparms.viewSplitText[number_of_lines]=f_string.substring(0,nl);
						number_of_lines++;
						f_string=f_string.substring(nl).trim();
					}
				} else {
					cparms.viewSplitText[number_of_lines]=f_string.substring(0);
					number_of_lines++;
					break;
				}
			}
		}
		cparms.viewSplitTextSize=number_of_lines;
		return number_of_lines;
	};
	
	@Override
	final protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
//		if (view_measured_width==0) view_measured_width=100;
		if (cparms.view_measured_width==0 || cparms.view_measured_height==0 || 
				cparms.view_measured_width != MeasureSpec.getSize(w) ||
						cparms.view_measured_height != MeasureSpec.getSize(h)) {
			cparms.view_measured_width = MeasureSpec.getSize(w);
			cparms.view_measured_height = MeasureSpec.getSize(h);
			if (cparms.viewLineBreak==LINE_BREAK_NOTHING) {
				if (cparms.view_measured_width<(int)cparms.text_paint.measureText(cparms.viewTextData))
					cparms.viewWidth=(int)cparms.text_paint.measureText(cparms.viewTextData);
				else cparms.viewWidth=cparms.view_measured_width+10;
			} else {
				cparms.viewWidth=cparms.view_measured_width;
			}
			int number_of_lines=createDrawTextInfo(cparms);
			if (number_of_lines==0) number_of_lines=1;
			cparms.viewHeight=(cparms.viewTextSize+cparms.viewLineSpacing)*number_of_lines;
		}
		if (debug_enabled) 
			debugMsg(cparms,"onMeasure","text length="+cparms.viewTextData.length()+
				", view width="+cparms.viewWidth+", text size="+cparms.viewTextSize+
				", measured width=="+cparms.view_measured_width+
				", measured height="+cparms.view_measured_height+
				", drawWidthOffset="+cparms.drawWidthOffset);
		setMeasuredDimension( cparms.viewWidth, cparms.viewHeight);
	};
	
//    @Override
//    protected int computeHorizontalScrollRange() {
//		if (debug_enabled) 
//			debugMsg("computeHorizontalScrollRange","width="+viewWidth);
//        return viewWidth;
//    };

//    @Override
//    protected int computeVerticalScrollRange() {
//		if (debug_enabled) debugMsg("computeVerticalScrollRange","height="+viewHeight);
//        return viewHeight;
//    };
	
	final public void setText(String t) {
		if (debug_enabled) 
			debugMsg(cparms,"setText","text="+t);
		cparms.viewTextData=t;//.replaceAll("\t", "     ");
		this.requestLayout();
		this.invalidate();
//		this.postInvalidate();
	};

	final public String getText() {
		return cparms.viewTextData;
	};

	final public int getCVViewWidth() {return cparms.viewWidth;}
	final public int getCVMeasuredWidth() {return cparms.view_measured_width;}
	final public int getCVMeasuredHeight() {return cparms.view_measured_height;}

	final public void setTypeface(Typeface tf) {
		if (debug_enabled) debugMsg(cparms,"setTypeface","text="+tf.getStyle());
		cparms.viewTypeFace=tf;
	};
	final public void setLineBreak(int lb) {
		if (debug_enabled) debugMsg(cparms,"setLineBreak","text="+lb);
		cparms.viewLineBreak=lb;
	}
	final public void setTextColor(int color) {
		cparms.viewTextColor=color;
		cparms.text_paint.setColor(cparms.viewTextColor);
	}
	@Override
	final public void setBackgroundColor(int color) {
		super.setBackgroundColor(color);
	}
	final static private float toPixel(Resources res, int dip) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, res.getDisplayMetrics());
		return px;
	};
}
