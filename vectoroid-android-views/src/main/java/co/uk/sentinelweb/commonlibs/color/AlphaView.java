package co.uk.sentinelweb.commonlibs.color;

import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import co.uk.sentinelweb.commonlibs.R;
import co.uk.sentinelweb.commonlibs.errorwrap.AbstractEWView;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWAsyncListener;

public class AlphaView extends AbstractEWView {
        
        private Paint paint;
        Shader alphaShader;
        private static Paint _noImgPaint;
        private Paint _marker;
        private int _color;
        private float _alpha;
        private OnEWAsyncListener<Float> _alphaListener;
        /**
         * @param context
         */
        public AlphaView(Context context) {
                super(context);
                init();
        }

        /**
         * @param context
         * @param attrs
         */
        public AlphaView(Context context, AttributeSet attrs) {
                super(context, attrs);
                init();
        }

        /**
         * @param context
         * @param attrs
         * @param defStyle
         */
        public AlphaView(Context context, AttributeSet attrs, int defStyle) {
                super(context, attrs, defStyle);
                init();
        }
        private void init() {
        	if (_noImgPaint==null) {
    			_noImgPaint = new Paint();
    			BitmapDrawable tspdrawable = (BitmapDrawable) getContext().getResources().getDrawable(R.drawable.ge_tsp_bg); 
    			_noImgPaint.setShader(new BitmapShader(tspdrawable.getBitmap(), TileMode.REPEAT, TileMode.REPEAT));
    			_noImgPaint.setStyle(Style.FILL);
    			_noImgPaint.setStrokeWidth(0);
    			_noImgPaint.setAntiAlias(true);
    		}
        	_marker=new Paint();
        	_marker.setARGB(255, 0, 0, 0);
        	_marker.setStyle(Style.STROKE);
        	_marker.setAntiAlias(true);
        	_marker.setStrokeWidth(1);
        }
        
        /* (non-Javadoc)
         * @see android.view.View#onDraw(android.graphics.Canvas)
         */
        @Override
        protected void onEWDraw(Canvas canvas) {
            if (paint==null) {
            	paint = new Paint();
            	alphaShader = new LinearGradient(0, 0, 0, getMeasuredHeight(), _color, 0x00000000, TileMode.CLAMP);
           	 	paint.setShader(alphaShader);
            }
            canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), _noImgPaint);
            canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
            float y = (1-_alpha) * getMeasuredHeight();
            _marker.setARGB(255, 0, 0, 0);
            canvas.drawRect(0, y-2, getMeasuredWidth(), y+2, _marker);
            _marker.setARGB(255, 255, 255, 255);
            canvas.drawRect(1, y-1, getMeasuredWidth()-1, y+1, _marker);
        }
        
        public void setColor(int color) {
        	//_alpha = Color.alpha(color)/255f;
        	_color = Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
        	if (getMeasuredHeight()>0) {
	        	paint = new Paint();
	       	 	alphaShader = new LinearGradient(0, 0, 0, getMeasuredHeight(), _color, 0x00000000, TileMode.CLAMP);
	        	paint.setShader(alphaShader);
        	}
        	invalidate();
        }
        
        public void setAlpha(float alpha) {
        	if (alpha>1) {alpha/=255;}
        	_alpha = alpha;
        	//paint = new Paint();
       	 	//alphaShader = new LinearGradient(0, 0, 0, getMeasuredHeight(), _color, 0x00000000, TileMode.CLAMP);
        	//paint.setShader(alphaShader);
        	invalidate();
        }
        public float getAlpha() {
        	return _alpha;
        }
		@Override
		protected void onEWFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
			
		}

		@Override
		protected boolean onEWKeyDown(int keyCode, KeyEvent event) {
			return false;
		}

		@Override
		protected boolean onEWKeyUp(int keyCode, KeyEvent event) {
			return false;
		}

		@Override
		protected void onEWLayout(boolean changed, int left, int top, int right, int bottom) {
			
		}

		@Override
		protected void onEWMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			
		}

		@Override
		protected boolean onEWTouchEvent(MotionEvent event) {
			 int action = event.getAction();
             switch (action) {
	             case MotionEvent.ACTION_DOWN:
	            	 attemptClaimDrag();
	             case MotionEvent.ACTION_UP:
	             case MotionEvent.ACTION_MOVE:
                     float y = event.getY();
                     if (y < 0) {
                             y = 0;
                     }
                     if (y > getMeasuredHeight()) {
                             y = getMeasuredHeight() - 0.1f;
                     }
                     
                     _alpha = 1-(y  / getMeasuredHeight());
                     invalidate();
                     if (_alphaListener!=null) {
                    	 _alphaListener.onAsync(_alpha);
                     }
                     return true;
             }
             return false;
		}
		private void attemptClaimDrag() {
	        getParent().requestDisallowInterceptTouchEvent(true);
		}
		/**
		 * @return the _colorListener
		 */
		public OnEWAsyncListener<Float> getAlphaListener() {
			return _alphaListener;
		}

		/**
		 * @param _colorListener the _colorListener to set
		 */
		public void setAlphaListener(OnEWAsyncListener<Float> l) {
			this._alphaListener = l;
		}
}