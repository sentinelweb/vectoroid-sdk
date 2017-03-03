/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.uk.sentinelweb.commonlibs.colorpicker;

import java.util.Vector;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import co.uk.sentinelweb.commonlibs.Globals;
import co.uk.sentinelweb.commonlibs.R;
import co.uk.sentinelweb.commonlibs.slider.Slider;
import co.uk.sentinelweb.commonlibs.slider.Slider.OnEWSliderChangeListener;
import co.uk.sentinelweb.commonlibs.util.DispUtil;


public class ColorPickerDialog extends Dialog  {
	public int index = 0;
    private OnColorChangedListener mListener;
    //private int mInitialColor;
	private ColorPickerView colorPickerView;
    private Vector<Integer> picked = new Vector<Integer>();
    Slider satSeek;
   // TextView satVal;
    Slider valSeek;
    //TextView valVal;
    Slider alphaSeek;
    //TextView alphaVal;
    float density = -1;
    
	public ColorPickerDialog(Context context,OnColorChangedListener listener ) {
        super(context);
        picked.add(Color.BLACK);
        picked.add(Color.WHITE);
        mListener = listener;
        density = DispUtil.getDensity(context);
        //mInitialColor = initialColor;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	requestWindowFeature(Window.FEATURE_LEFT_ICON);
        super.onCreate(savedInstanceState);
        
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int color) {
                mListener.colorChanged(color);
                dismiss();
                addPicked(color);
            } 
        };
        colorPickerView = new ColorPickerView(getContext(), l);
        setContentView(R.layout.colorpick);
        ((FrameLayout)findViewById(R.id.cpick_wheel_ctnr)).addView(colorPickerView);
        setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.col_dia_i_colour);
        satSeek = (Slider) findViewById(R.id.cpick_sat_sldr);
        satSeek.setOnSeekBarChangeListener(new OnEWSliderChangeListener(this.getContext()) {
			@Override
			public void onEWProgressChanged(Slider seekBar, float progress,boolean fromUser) {
				if (fromUser) {
					colorPickerView.setColorHSV(colorPickerView.currentHue, progress/satSeek.getMax(), valSeek.getPosition()/valSeek.getMax());
				}
				//satVal.setText(""+progress);
			}
			
        });
        //satVal = (TextView) findViewById(R.id.cpick_sat_lbl_val);
        valSeek = (Slider) findViewById(R.id.cpick_value_sldr);
        valSeek.setOnSeekBarChangeListener(new OnEWSliderChangeListener(this.getContext()) {
			@Override
			public void onEWProgressChanged(Slider seekBar, float progress,boolean fromUser) {
				if (fromUser) {
					colorPickerView.setColorHSV(colorPickerView.currentHue, satSeek.getPosition()/satSeek.getMax(), progress/valSeek.getMax());
				}
				//valVal.setText(""+progress);
			}
        });
        //valVal = (TextView) findViewById(R.id.cpick_value_lbl_val);
        alphaSeek = (Slider) findViewById(R.id.cpick_alpha_sldr);
        alphaSeek.setOnSeekBarChangeListener(new OnEWSliderChangeListener(this.getContext()) {
			@Override
			public void onEWProgressChanged(Slider seekBar, float progress,boolean fromUser) {
				if (fromUser) {
					colorPickerView.setAlpha(alphaSeek.getPosition());
				}
				//alphaVal.setText(""+progress);
			}
			
        });
       // alphaVal = (TextView) findViewById(R.id.cpick_alpha_lbl_val);
		//setContentView(colorPickerView);
        setTitle("Pick a Color");
    }
    
	@Override
	protected void onStart() {
		super.onStart();
		buildPickedList();
	}

	private void buildPickedList() {
		LinearLayout lastList = ((LinearLayout)findViewById(R.id.cpick_swatch_ctnr));
		lastList.removeAllViews();
		int swatchSize = (int)(30*density);
        for (int i=0;i<picked.size();i++) {
        	View v = new View(getContext());
        	final int color = picked.get(i);
        	v.setBackgroundColor(color);
        	v.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					colorPickerView.setColor(color);
				}
        	});
        	
			lastList.addView(v, new LinearLayout.LayoutParams(swatchSize,swatchSize));
        }
	}
	    
    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    public void setColor( int colorRGBA) { 
    	Log.d(Globals.LOG_TAG, Color.red(colorRGBA)+":"+Color.green(colorRGBA)+":"+Color.blue(colorRGBA)+":"+Color.alpha(colorRGBA));
    	colorPickerView.setColor(colorRGBA);
    	//colorPickerView.setAlpha(Color.alpha(colorRGBA));
    }
    
    public void addPicked( int color) {
    	boolean found =false;
    	for (int col : picked) { if (col == color) {found=true;}  }
    	if (!found) {picked.add(color);	}
    }
    
    private class ColorPickerView extends View {
    	 private static final int CENTER_X = 100;
         private static final int CENTER_Y = 100;
         private static final int CENTER_RADIUS = 32;
         
        private Paint mPaint;
        private Paint mCenterPaint;
        private Paint mCenterAlphaPaint;
        private final int[] mColors;
        private OnColorChangedListener mListener;
        
        int centerX = CENTER_X;
        int centerY = CENTER_Y;
        int centerRad = CENTER_RADIUS;
        
        float currentHue = 0;
        
        
        ColorPickerView(Context c, OnColorChangedListener l) {
            super(c);
            mListener = l;
            centerX = (int) (centerX*density);
			centerY = (int) (centerY*density);
			centerRad = (int) (centerRad*density);
            mColors = new int[] {
                0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000
            };
            Shader s = new SweepGradient(0, 0, mColors, null);

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(32*density);

            mCenterPaint = new Paint();
            mCenterPaint.setAntiAlias(true);
            mCenterPaint.setStyle(Paint.Style.FILL);
            mCenterPaint.setStrokeWidth(0);
            mCenterAlphaPaint = new Paint(mCenterPaint);
        }
        
        public void setColor( int colorRGB) {
 	        float[] hsv = new float[3];
 	        Color.colorToHSV(colorRGB, hsv);
 	        currentHue = hsv[0];
 	        ColorPickerDialog.this.satSeek.setPosition((int)(hsv[1]*255));
	        ColorPickerDialog.this.valSeek.setPosition((int)(hsv[2]*255));
	        ColorPickerDialog.this.alphaSeek.setPosition(Color.alpha(colorRGB));
	        mCenterPaint.setColor(colorRGB);
	        mCenterPaint.setAlpha(255);
	        int alpha = mCenterAlphaPaint.getAlpha();
	        mCenterAlphaPaint.setColor(colorRGB);
	        mCenterAlphaPaint.setAlpha(alpha);
	        invalidate();
        }
        
        public void setColorH( int colorH) {// only the hue component is valid
	        float[] hsv = new float[3];
	        Color.colorToHSV(colorH, hsv);
	        currentHue = hsv[0];
	        setColorHSV( hsv[0], ColorPickerDialog.this.satSeek.getPosition()/255f, ColorPickerDialog.this.valSeek.getPosition()/255f );
        }
        
        public void setColorHSV( float h,float s,float v) {
        	float[] hsv = new float[]{h,s,v};
        	int colorRGB = Color.HSVToColor(hsv);
        	mCenterPaint.setColor(colorRGB);
        	int alpha = mCenterAlphaPaint.getAlpha();
        	mCenterAlphaPaint.setColor(colorRGB);
        	mCenterAlphaPaint.setAlpha(alpha);
        	invalidate();
        }
        
        public void setAlpha( float a ) {// a[0..255]
        	mCenterAlphaPaint.setAlpha((int)a);
        	ColorPickerDialog.this.alphaSeek.setPosition(a);
        	invalidate();
        }
        
        private boolean mTrackingCenter;
        private boolean mHighlightCenter;
        @Override 
        protected void onDraw(Canvas canvas) {
            float r = centerX - mPaint.getStrokeWidth()*0.5f;

            canvas.translate(centerX, centerX);

            canvas.drawOval(new RectF(-r, -r, r, r), mPaint);            
            canvas.drawCircle(0, 0, centerRad , mCenterAlphaPaint);
            canvas.drawCircle(0, 0, centerRad/2, mCenterPaint);
            
            if (mTrackingCenter) {
                //int c = mCenterPaint.getColor();
                //mCenterPaint.setStyle(Paint.Style.STROKE);

                //if (mHighlightCenter) {
                //    mCenterPaint.setAlpha(0xFF);
                //} else {
                //    mCenterPaint.setAlpha(0x80);
                //}
                canvas.drawCircle(0, 0, centerRad + mCenterPaint.getStrokeWidth(), mCenterPaint);
               
                //
               // mCenterPaint.setColor(c);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(centerX*2, centerY*2);
        }

       

        private int floatToByte(float x) {
            int n = java.lang.Math.round(x);
            return n;
        }
        private int pinToByte(int n) {
            if (n < 0) {
                n = 0;
            } else if (n > 255) {
                n = 255;
            }
            return n;
        }

        private int ave(int s, int d, float p) {
            return s + java.lang.Math.round(p * (d - s));
        }

        private int interpColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }

            float p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);

            return Color.argb(a, r, g, b);
        }

        private int rotateColor(int color, float rad) {
            float deg = rad * 180 / 3.1415927f;
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);

            ColorMatrix cm = new ColorMatrix();
            ColorMatrix tmp = new ColorMatrix();

            cm.setRGB2YUV();
            tmp.setRotate(0, deg);
            cm.postConcat(tmp);
            tmp.setYUV2RGB();
            cm.postConcat(tmp);

            final float[] a = cm.getArray();

            int ir = floatToByte(a[0] * r +  a[1] * g +  a[2] * b);
            int ig = floatToByte(a[5] * r +  a[6] * g +  a[7] * b);
            int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);

            return Color.argb(Color.alpha(color), pinToByte(ir),
                              pinToByte(ig), pinToByte(ib));
        }

        private static final float PI = 3.1415926f;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - centerX;
            float y = event.getY() - centerY;
            boolean inCenter = java.lang.Math.sqrt(x*x + y*y) <= centerRad;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTrackingCenter = inCenter;
                    if (inCenter) {
                        mHighlightCenter = true;
                        invalidate();
                        break;
                    }
                case MotionEvent.ACTION_MOVE:
                    if (mTrackingCenter) {
                        if (mHighlightCenter != inCenter) {
                            mHighlightCenter = inCenter;
                            invalidate();
                        }
                    } else {
                        float angle = (float)java.lang.Math.atan2(y, x);
                        // need to turn angle [-PI ... PI] into unit [0....1]
                        float unit = angle/(2*PI);
                        if (unit < 0) {
                            unit += 1;
                        }
                        int interpColor = interpColor(mColors, unit);
						setColorH(interpColor);
						attemptClaimDrag();
                        //invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTrackingCenter) {
                        if (inCenter) {
                            mListener.colorChanged(mCenterAlphaPaint.getColor());
                        }
                        mTrackingCenter = false;    // so we draw w/o halo
                        invalidate();
                    }
                    break;
            }
            return true;
        }
    }
    private void attemptClaimDrag() {
        if (colorPickerView!=null && colorPickerView.getParent() != null) {
        	colorPickerView.getParent().requestDisallowInterceptTouchEvent(true);
        }
      }
   
}