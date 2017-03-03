package co.uk.sentinelweb.commonlibs.color;

import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import co.uk.sentinelweb.commonlibs.R;
import co.uk.sentinelweb.commonlibs.colorpicker.ColorPickerDialog.OnColorChangedListener;
import co.uk.sentinelweb.commonlibs.errorwrap.AbstractEWView;
import co.uk.sentinelweb.commonlibs.errorwrap.EWRunnable;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWAsyncListener;
import co.uk.sentinelweb.commonlibs.util.DispUtil;

public class HueCircleView extends AbstractEWView {
		float _density = -1;
    	
    	private static final int CENTER_X_DEF = 75;
        private static final int CENTER_Y_DEF = 75;
        private static final int CENTER_RADIUS_DEF = 32;
         
        private Paint _paint;
        private Paint _centerPaint;
        //private Paint _centerAlphaPaint;
        //public Paint _noImgPaint = new Paint();
        
        private static final int[] _colors = new int[] {
           0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00, 0xFFFFFF00, 0xFFFF0000
        };
        boolean dimInit = false;
        int _centerX = CENTER_X_DEF;
        int _centerY = CENTER_Y_DEF;
        int _centerRad = CENTER_RADIUS_DEF;
        RectF _boundary ;
        float _currentHue = 0;
        OnEWAsyncListener<Float>  _hueChangedListener;
        
        public HueCircleView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			init(context);
		}

		public HueCircleView(Context context, AttributeSet attrs) {
			super(context, attrs);
			init(context);
		}

		public HueCircleView(Context context) {
			super(context);
			init(context);
		}
		private void init(Context context) {
			Shader s = new SweepGradient(0, 0, _colors, null);

            _paint = new Paint();
            _paint.setAntiAlias(true);
            _paint.setShader(s);
            _paint.setStyle(Paint.Style.STROKE);
            //_paint.setStrokeWidth(32*_density);
            
            //BitmapDrawable tspdrawable = (BitmapDrawable)getContext().getResources().getDrawable(R.drawable.ge_tsp_bg); 
    		//_noImgPaint.setShader(new BitmapShader(tspdrawable.getBitmap(), TileMode.REPEAT, TileMode.REPEAT));
    		//_noImgPaint.setStyle(Style.FILL_AND_STROKE);
    		
            _centerPaint = new Paint();
            _centerPaint.setAntiAlias(true);
            _centerPaint.setStyle(Paint.Style.FILL);
            _centerPaint.setStrokeWidth(0);
		}
		
		private void initDim(Context context) {
			_density = DispUtil.getDensity(context);
			int dim = Math.min(getMeasuredWidth(), getMeasuredHeight());
			_centerX = (int) (getMeasuredWidth()/2);
			_centerY = (int) (getMeasuredHeight()/2);

			_centerRad = (int)(_centerX/2.5f);
			
			_paint.setStrokeWidth(_centerRad);
			 
			float r = dim/2 - _paint.getStrokeWidth()*0.5f;
	        _boundary = new RectF(-r, -r, r, r);
	        dimInit=true;
		}
		
		public void setColor( int colorRGBA) {
 	        float[] hsv = new float[3];
 	        Color.colorToHSV(colorRGBA, hsv);
 	        _currentHue = hsv[0];
 	        _centerPaint.setColor(getHueColor());
	        _centerPaint.setAlpha(255);
	        invalidate();
        }
		
		public float getHue() {
			return _currentHue;
		}
		
		public int getHueColor() {
			float[] hsv = {1,1,1};
	    	hsv[0] = _currentHue;
	    	return Color.HSVToColor(hsv);
		}
        
        private boolean mTrackingCenter;
        private boolean mHighlightCenter;
        
        @Override 
        protected void onEWDraw(Canvas canvas) {
        	if (!dimInit) {
        		initDim(getContext());
        	}
            
            canvas.translate(_centerX, _centerY);
            
			canvas.drawOval(_boundary, _paint); 
            //canvas.drawCircle(0, 0, _centerRad , _noImgPaint);
            canvas.drawCircle(0, 0, _centerRad , _centerPaint);
            //canvas.drawCircle(0, 0, _centerRad/2, _centerPaint);
            
            if (mTrackingCenter) {
                canvas.drawCircle(0, 0, _centerRad + _centerPaint.getStrokeWidth(), _centerPaint);
            }
        }
        
        //@Override
        //protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //    setMeasuredDimension(_centerX*2, _centerY*2);
        //}

       
        
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
        /*
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
		*/
        private static final float PI = 3.1415926f;
        Handler _h = new Handler();
        boolean isChangePosted=false;
        Runnable _changedRunnable = new EWRunnable(getContext()){
			@Override
			public void doEWrun() {
				setCurrentHue();
				isChangePosted=false;
			}
        };
        @Override
        protected boolean onEWTouchEvent(MotionEvent event) {
            float x = event.getX() - _centerX;
            float y = event.getY() - _centerY;
            boolean inCenter = java.lang.Math.sqrt(x*x + y*y) <= _centerRad;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTrackingCenter = inCenter;
                    attemptClaimDrag();
					
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
                        int interpColor = interpColor(_colors, unit);
						setColor(interpColor);
						//_h.removeCallbacks(_changedRunnable);
						if (!isChangePosted) {
							_h.postDelayed(_changedRunnable, 200);
							isChangePosted=true;
						}
                        //invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTrackingCenter) {
                        //if (inCenter) {
                        //    mListener.colorChanged(mCenterAlphaPaint.getColor());
                        //}
                        mTrackingCenter = false;    // so we draw w/o halo
                        invalidate();
                    }
                    _h.removeCallbacks(_changedRunnable);
                    isChangePosted=false;
                    //_h.postDelayed(_changedRunnable, 100);
                    setCurrentHue();
                    break;
            }
            return true;
        }

		@Override
		protected void onEWFocusChanged(boolean gainFocus, int direction,Rect previouslyFocusedRect) {	}
		@Override
		protected boolean onEWKeyDown(int keyCode, KeyEvent event) {return false;	}
		@Override
		protected boolean onEWKeyUp(int keyCode, KeyEvent event) {	return false;}
		@Override
		protected void onEWLayout(boolean changed, int left, int top,int right, int bottom) {}
		@Override
		protected void onEWMeasure(int widthMeasureSpec, int heightMeasureSpec) {}
		
		private void attemptClaimDrag() {
		        getParent().requestDisallowInterceptTouchEvent(true);
		}
		
		private void setCurrentHue() {
			invalidate();
			if (_hueChangedListener!=null) {
            	_hueChangedListener.onAsync(_currentHue);
            }
		}
		
		/**
		 * @return the _hueChangedListener
		 */
		public OnEWAsyncListener<Float> getHueChangedListener() {
			return _hueChangedListener;
		}

		/**
		 * @param _hueChangedListener the _hueChangedListener to set
		 */
		public void setHueChangedListener(OnEWAsyncListener<Float> _hueChangedListener) {
			this._hueChangedListener = _hueChangedListener;
		}
		
		
    }