package co.uk.sentinelweb.commonlibs.color;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import co.uk.sentinelweb.commonlibs.errorwrap.AbstractEWView;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWAsyncListener;

public class SVView extends AbstractEWView {
	 Paint paint;
	 Paint circlePaint;
     //Shader valueShader;
     float color[] = {0, 1, 1};
     float satValX,satValY=0;
     int rgb = 0;
     boolean firstTime = true;
     private OnEWAsyncListener<Integer> _svListener;
     
	public SVView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public SVView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SVView(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		 paint = new Paint();
         circlePaint = new Paint();
         circlePaint.setARGB(255, 255, 255, 255);
         circlePaint.setAntiAlias(true);
         circlePaint.setStrokeWidth(1);
         circlePaint.setStyle(Style.STROKE);
	}
	
	@Override
	protected void onEWDraw(Canvas canvas) {
		if (firstTime) {
			setColorInternal();
			firstTime=false;
		}
	    canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), paint);
	    circlePaint.setARGB(255, 255, 255, 255);
	    canvas.drawCircle(satValX, satValY, 10, circlePaint);
	    circlePaint.setARGB(255, 0, 0, 0);
	    canvas.drawCircle(satValX, satValY, 11, circlePaint);
	}

	@Override
	protected void onEWFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		// TODO Auto-generated method stub

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
                float x = event.getX();
                if (x < 0) {
                        x = 0;
                }
                if (x > getMeasuredWidth()) {
                        x = getMeasuredWidth() - 0.1f;
                }
                satValX = x;
                satValY = y;
                color[1] = satValX / getMeasuredWidth();
        		color[2] = 1 - satValY / getMeasuredHeight();
        		
                updateColor();
                invalidate();
                if (_svListener!=null) {
                	_svListener.onEWAsync(rgb);
                }
                return true;
        }
        return false;
	}

	private void updateColor() {
		
		rgb = Color.HSVToColor(color);
		
		 float[] hsv = { color[0], 1, 1 };
 		 int rgbhue = Color.HSVToColor(hsv);
 		 Shader valueShader = new LinearGradient(0, 0, 0, getMeasuredHeight(), 0xffffffff, 0xff000000, TileMode.CLAMP);
 	     Shader saturationShader = new LinearGradient(0, 0, getMeasuredWidth(), 0, 0xffffffff, rgbhue, TileMode.CLAMP);
 	     Shader composedShader = new ComposeShader(saturationShader, valueShader, PorterDuff.Mode.MULTIPLY);
 	     paint.setShader(composedShader);
	}
	
	public void setHue(float hue) {
         color[0] = hue;
         updateColor();
         invalidate();
	}
	public void setColor(int colorARGB) {
		Color.colorToHSV(colorARGB, color);
		setColorInternal();
	}

	private void setColorInternal() {
		satValX =  color[1] * getMeasuredWidth();
		satValY = (1 - color[2]) * getMeasuredHeight();
		updateColor();
        invalidate();
	}
	public float getSat() {
		return color[1];
	}
	public float getVal() {
		return color[2];
	}
	private void attemptClaimDrag() {
        getParent().requestDisallowInterceptTouchEvent(true);
	}
	/**
	 * @return the _colorListener
	 */
	public OnEWAsyncListener<Integer> getSVListener() {
		return _svListener;
	}

	/**
	 * @param _colorListener the _colorListener to set
	 */
	public void setSVListener(OnEWAsyncListener<Integer> _colorListener) {
		this._svListener = _colorListener;
	}
	
	
}
