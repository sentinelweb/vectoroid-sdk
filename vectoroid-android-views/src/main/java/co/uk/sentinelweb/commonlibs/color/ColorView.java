package co.uk.sentinelweb.commonlibs.color;

import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup.MarginLayoutParams;
import co.uk.sentinelweb.commonlibs.R;
import co.uk.sentinelweb.commonlibs.errorwrap.AbstractEWView;
import co.uk.sentinelweb.commonlibs.util.ColorUtil;
import co.uk.sentinelweb.commonlibs.util.DispUtil;

public class ColorView extends AbstractEWView{
	private static float _density = -1;
	private static Paint _noImgPaint;
	private static Paint _textBgPaint;
	
	private Paint _colorPaint;
	private Paint _textPaint;
	private boolean _showWebColour;
	private float _textSize=16f;
	private String _webColourText = null;
	private float _webColourTextXPos=0;
	private boolean _noColor = false;
	
    public ColorView(Context context) {
		super(context);	
		init(context);
	}
    
	public ColorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public ColorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		if (_density==-1) {
			_density=DispUtil.getDensity(context);
		}
		if (_noImgPaint==null) {
			_noImgPaint = new Paint();
			BitmapDrawable tspdrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.ge_tsp_bg); 
			_noImgPaint.setShader(new BitmapShader(tspdrawable.getBitmap(), TileMode.REPEAT, TileMode.REPEAT));
			_noImgPaint.setStyle(Style.FILL_AND_STROKE);
		}
		if (_textBgPaint==null) {
			_textBgPaint = new Paint();
			_textBgPaint.setColor(Color.argb(128,0,0,0));
		}
		_colorPaint = new Paint();
        //_centerPaint.setAntiAlias(true);
		_colorPaint.setStyle(Paint.Style.FILL);
		_colorPaint.setStrokeWidth(0);
		
		_textPaint = new Paint();
		_textPaint.setColor(Color.WHITE);
		_textPaint.setTextSize(_textSize*_density);
		_textPaint.setAntiAlias(true);
		
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
	@Override
	protected void onEWDraw(Canvas canvas) {
		if (!_noColor) {
			MarginLayoutParams mp = (MarginLayoutParams)getLayoutParams();
			canvas.drawRect(mp.leftMargin,mp.topMargin,getMeasuredWidth()-mp.rightMargin,getMeasuredHeight()-mp.bottomMargin, _noImgPaint);
			canvas.drawRect(mp.leftMargin,mp.topMargin,getMeasuredWidth()-mp.rightMargin,getMeasuredHeight()-mp.bottomMargin, _colorPaint);
			if (_showWebColour) {
				canvas.drawRect(mp.leftMargin,getMeasuredHeight()-_textSize*_density-2-mp.bottomMargin,getMeasuredWidth()-mp.rightMargin,getMeasuredHeight()-mp.bottomMargin, _textBgPaint);
				float len = _textPaint.measureText(_webColourText);
				_webColourTextXPos = (getMeasuredWidth()-len)/2;
				canvas.drawText(_webColourText, _webColourTextXPos, getMeasuredHeight()-1-mp.bottomMargin, _textPaint);
			}
		} else {
			canvas.drawColor(Color.BLACK);
			canvas.drawLine(0,0,getMeasuredWidth(),getMeasuredHeight(), _textPaint);
			canvas.drawLine(0,getMeasuredHeight(),getMeasuredWidth(),0, _textPaint);
		}
	}
	@Override
	protected boolean onEWTouchEvent(MotionEvent event) {
		if (event.getAction()==MotionEvent.ACTION_UP && isClickable()) {
			performClick();
			return true;
		}
		return isClickable();
	}
	
	public void setColor(int color){
		_noColor=false;
		_colorPaint.setColor(color);
		setWebColourText(color);
		invalidate();
	}

	private void setWebColourText(int color) {
		_webColourText=ColorUtil.toColorStringNoAlpha(color);
	}
	
	public int getColor() {return _colorPaint.getColor();}
	@Override
	public void setBackgroundColor(int color){
		_noColor=false;
		_colorPaint.setColor(color);
		setWebColourText(color);
		invalidate();
	}
	public int getBackgroundColor() {return _colorPaint.getColor();}

	/**
	 * @return the webColourTextSize
	 */
	public float getTextSize() {
		return _textSize;
	}

	/**
	 * @param webColourTextSize the webColourTextSize to set
	 */
	public void setTextSize(float webColourTextSize) {
		this._textSize = webColourTextSize;
		_textPaint.setTextSize(_textSize*_density);
		setWebColourText(_colorPaint.getColor());
	}

	/**
	 * @return the showWebColour
	 */
	public boolean isShowWebColour() {
		return _showWebColour;
	}

	/**
	 * @param showWebColour the showWebColour to set
	 */
	public void setShowWebColour(boolean showWebColour) {
		this._showWebColour = showWebColour;
		invalidate();
	}

	/**
	 * @return the noColor
	 */
	public boolean isNoColor() {
		return _noColor;
	}

	/**
	 * @param noColor the noColor to set
	 */
	public void setNoColor(boolean noColor) {
		this._noColor = noColor;
		invalidate();
	}
	
}