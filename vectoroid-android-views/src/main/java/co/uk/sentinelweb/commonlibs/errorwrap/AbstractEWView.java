package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public abstract class AbstractEWView extends View {

	public AbstractEWView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AbstractEWView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public AbstractEWView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			onEWDraw(canvas);
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,getContext(),null);
		}
	}
	protected abstract void onEWDraw(Canvas canvas) ;

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction,	Rect previouslyFocusedRect) {
		try {
			onEWFocusChanged( gainFocus,  direction,	 previouslyFocusedRect);
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,getContext(),null);
		}
	}
	protected abstract void onEWFocusChanged(boolean gainFocus, int direction,	Rect previouslyFocusedRect);
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			if (!onEWKeyDown(keyCode, event)) {
				return super.onKeyDown(keyCode, event);
			}
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,getContext(),null);
		}
		return false;
	}
	protected abstract boolean onEWKeyDown(int keyCode, KeyEvent event);
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		try {
			if (!onEWKeyUp(keyCode, event)) {
				return super.onKeyUp(keyCode, event);
			}
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,getContext(),null);
		}
		return false;
	}
	protected abstract boolean onEWKeyUp(int keyCode, KeyEvent event);
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,		int bottom) {
		try {
			 onEWLayout( changed,  left,  top,  right,		 bottom);
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,getContext(),null);
		}
	}
	protected abstract void onEWLayout(boolean changed, int left, int top, int right,		int bottom);
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		try {
			onEWMeasure( widthMeasureSpec,  heightMeasureSpec);
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,getContext(),null);
		}
	}
	protected abstract void onEWMeasure(int widthMeasureSpec, int heightMeasureSpec);
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			return onEWTouchEvent(event);
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,getContext(),null);
		}
		return false;
	}
	protected abstract boolean onEWTouchEvent(MotionEvent event);
}
