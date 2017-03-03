package co.uk.sentinelweb.commonlibs.slider;

import java.math.BigDecimal;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import co.uk.sentinelweb.commonlibs.Globals;
import co.uk.sentinelweb.commonlibs.R;
import co.uk.sentinelweb.commonlibs.errorwrap.AbstractEWEventListener;
import co.uk.sentinelweb.commonlibs.errorwrap.AbstractEWView;
import co.uk.sentinelweb.commonlibs.util.DispUtil;

public class Slider extends AbstractEWView{
	private static float _density = -1;
	private Drawable _thumb;
	private Rect _thumbRect = new Rect();
	private float _thumbWidth=40;
	private float _thumbHeight=40;
	private int _thumbColor = Color.RED;
	
	public enum State {NORMAL,START,STOP,SLIDING};
	private State _state=State.NORMAL;
	
	private OnSliderChangeListener _onSeekBarChangeListener;
	
	private float _position;
	private Float _displayValue = null;
	private boolean _movingFwd = true;
	
	private int _defWidth = 100;
	private int _defHeight = 30;
	
	private float _max=100;
	private float _min=0;
	
	private float _textSize=13;
	
	private float _trackHeight = 8;
	private int _trackColor = Color.LTGRAY;
	private String _label = null;
	private float _labelPos = -1;
	Paint _trackPaint;
	Paint _thumbPaint;
	Paint _textPaint;
	private boolean _forceInt=false;
	
	public Slider( Context context ) {
		super(context);
		init(context,null);
	}
	
	public Slider(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context,attrs); 
	}

	public Slider(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
	}
	
	private void init(Context context, AttributeSet attrs) {
		if (_density ==-1) {
			_density = DispUtil.getDensity(this.getContext());
		}
		//setDefaults
		_thumbWidth=40*_density;
		_thumbHeight=40*_density;
		_trackHeight = 8*_density;
		_textSize=13*_density;
		if (attrs!=null) {
			TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.Slider);
			_thumb = (Drawable)a.getDrawable(R.styleable.Slider_thumb); 
			_thumbWidth = a.getDimension(R.styleable.Slider_thumbWidth,40*_density); 
			_thumbHeight = a.getDimension(R.styleable.Slider_thumbHeight,40*_density); 
			_thumbColor = a.getColor(R.styleable.Slider_thumbColor, Color.RED); 
			_trackColor = a.getColor(R.styleable.Slider_trackColor, Color.argb(255,0,0,128)); 
			_trackHeight = a.getDimension(R.styleable.Slider_trackHeight,4*_density); 
			_label = a.getString(R.styleable.Slider_label); 
	        _max = a.getInt(R.styleable.Slider_max,100); 
	        _min = a.getInt(R.styleable.Slider_min,0); 
	        _textSize = a.getDimension(R.styleable.Slider_textSize,13*_density); 
	        _forceInt = a.getBoolean(R.styleable.Slider_forceInt,false); 
	        _position = a.getFloat(R.styleable.Slider_valuef,_min); 
		}
		_trackPaint = new Paint();
		_trackPaint.setColor(_trackColor);
		_trackPaint.setStyle(Style.FILL);
		_trackPaint.setShadowLayer(1*_density, 0, 0, _trackColor);
		_thumbPaint = new Paint();
		_thumbPaint.setColor(Color.RED);
		_thumbPaint.setStyle(Style.FILL);
		_thumbPaint.setStrokeWidth(2);
		CornerPathEffect cpe = new CornerPathEffect(_thumbWidth/5f);
		_thumbPaint.setPathEffect(cpe);
		_thumbPaint.setAntiAlias(true);
		_textPaint = new Paint();
		_textPaint.setColor(Color.WHITE);
		_textPaint.setStyle(Style.FILL_AND_STROKE);
		_textPaint.setTextSize(_textSize);
		_textPaint.setTypeface(Typeface.DEFAULT_BOLD);
		_textPaint.setAntiAlias(true);
		setFocusableInTouchMode(true);
		setFocusable(true);
	}
	
	@Override
	protected void onEWDraw( Canvas canvas ) {
		//Log.d("Slider","drawing:");
		float sliderLeft = getPaddingLeft()+_thumbWidth/2;
		float sliderWidth = getMeasuredWidth()-getPaddingRight()-getPaddingLeft()-_thumbWidth;
		float xpos = toCoord( _position );
		
		// track
		_trackPaint.setColor( _trackColor );
		float ttop = ( getMeasuredHeight() - _trackHeight )/2f;
		canvas.drawRect( sliderLeft, ttop, sliderLeft+sliderWidth, ttop+_trackHeight, _trackPaint );
		_trackPaint.setColor( Color.argb(255,0,0,255) );
		canvas.drawRect( sliderLeft+2, getMeasuredHeight()/2f-1, xpos, getMeasuredHeight()/2f+1, _trackPaint );
		
		// thumb
		_thumbRect.set((int)(xpos-_thumbWidth/2), 0, (int)(xpos+_thumbWidth/2), (int)_thumbHeight);
		if (_thumb==null ) {
			_thumbPaint.setColor( _thumbColor );
			_thumbPaint.setStyle( Style.FILL );
			canvas.drawRect(_thumbRect , _thumbPaint );
			_thumbPaint.setColor( Color.DKGRAY );
			_thumbPaint.setStyle( Style.STROKE );
			canvas.drawRect(_thumbRect, _thumbPaint );
		} else {
			//canvas.drawBitmap(_thumb, xpos-_thumbWidth/2,0, _trackPaint);
			//Log.d(Globals.LOG_TAG, _thumbRect.left+":"+_thumbRect.right+":"+_thumbRect.top+":"+_thumbRect.bottom+":"+_thumbRect.width()+"x"+_thumbRect.height());
			_thumb.setBounds( _thumbRect );
			_thumb.draw( canvas );
		}
		
		// thumb notch
		_trackPaint.setColor( Color.GRAY );
		canvas.drawRect( xpos-1,_thumbHeight/6f,xpos+1,_thumbHeight/6f*5f, _trackPaint );
		
		//value
		float posVal = _displayValue!=null? _displayValue : _position;
		String text = dp1( posVal );
		float textWid = _textPaint.measureText(text);
		if (_state==State.SLIDING  || _state==State.START) {
			/*
			if (!_movingFwd) {
				canvas.drawText(text,xpos+_thumbWidth,_textSize-4,_textPaint);
			} else {
				canvas.drawText(text,xpos-_thumbWidth-textSz,_textSize-4,_textPaint);
			}
			*/
			canvas.drawText(text,sliderLeft,_textSize-4,_textPaint);
			canvas.drawText(text,sliderLeft+sliderWidth-textWid,_textSize-4,_textPaint);
		} else {
			canvas.drawText(text,xpos-textWid/2,(_thumbHeight+_textSize)/2f-4,_textPaint);
		}
		if (_label!=null) {
			if (_labelPos==-1) {_labelPos = sliderLeft+(sliderWidth-_textPaint.measureText(_label))/2f;}
			canvas.drawText(_label,_labelPos,getMeasuredHeight(),_textPaint);
		}
	}

	@Override
	protected void onEWFocusChanged( boolean gainFocus, int direction, Rect previouslyFocusedRect ) {
		
	}

	@Override
	protected boolean onEWKeyDown( int keyCode, KeyEvent event ) {
		return false;
	}

	@Override
	protected boolean onEWKeyUp( int keyCode, KeyEvent event ) {
		return false;
	}

	@Override
	protected void onEWLayout( boolean changed, int left, int top, int right, int bottom ) {
		_labelPos=-1;
	}
	
	@Override
	protected void onEWMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
        int dw = _defWidth;
        int dh = _defHeight;
        /*
        if (d != null) {
            dw = Math.max(mMinWidth, Math.min(mMaxWidth, d.getIntrinsicWidth()));
            dh = Math.max(mMinHeight, Math.min(mMaxHeight, d.getIntrinsicHeight()));
            dh = Math.max(thumbHeight, dh);
        }
        */
        dh = (int)Math.max( _thumbHeight, dh );
        /*
        switch (_defWidth) {
        	
        }
        switch (_defHeight) {
    	
        }
        */
        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();
        
        setMeasuredDimension(resolveSize(dw, widthMeasureSpec), resolveSize(dh, heightMeasureSpec));
	}

	@Override
	protected boolean onEWTouchEvent( MotionEvent event ) {
		//Log.d("Slider","touch:");
		float position = toPosition(event);
		position = Math.min(position, _max);
		position = Math.max(position, _min);
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				_state = State.START;
				//if (false) {
				//	
				//}
				if (_onSeekBarChangeListener!=null) {
            		_onSeekBarChangeListener.onProgressChanged(this, _position, true);
            	}
				break;
            case MotionEvent.ACTION_MOVE:
            	_state = State.SLIDING;
            	if (_position!=position) {_movingFwd=_position<position;}
            	_position=position;
            	attemptClaimDrag();
            	
            	if (_onSeekBarChangeListener!=null) {
            		_onSeekBarChangeListener.onProgressChanged(this, _position, true);
            	}
            	break;
            case MotionEvent.ACTION_UP:
            	_state = State.STOP;
        	     _position=position;
            	if (_onSeekBarChangeListener!=null) {
            		_onSeekBarChangeListener.onProgressChanged(this, _position, true);
            	}
            	_state = State.NORMAL;
            	break;
		}
		invalidate();
		return true;
	}

	private float toPosition(MotionEvent event) {
		float sliderLeft = getPaddingLeft()+_thumbWidth/2;
		float sliderWidth = getWidth()-getPaddingRight()-getPaddingLeft()-_thumbWidth;
		float position = (event.getX()-sliderLeft)/(sliderWidth)*(_max-_min)+_min;
		if (_forceInt) {position=Math.round(position);}
		return position;
	}
	
	private float toCoord(float position) {
		float sliderLeft = getPaddingLeft()+_thumbWidth/2;
		float sliderWidth = getWidth()-getPaddingRight()-getPaddingLeft()-_thumbWidth;
		float x = (position-_min)/(_max-_min)*(sliderWidth)+sliderLeft;
		return x;
	}
	
	private void boundsPos() {
		_position =  Math.min(Math.max(_position, _min), _max);
	}
	
	public Drawable getThumb() {
		return _thumb;
	}

	public void setThumb(Drawable mThumb) {
		this._thumb = mThumb;
	}
	
	public void setThumbRes(int res) {
		this._thumb = getContext().getResources().getDrawable(res);
	}
	
	
	/**
	 * @return the _thumbWidth
	 */
	public float getThumbWidth() {
		return _thumbWidth;
	}

	/**
	 * @param _thumbWidth the _thumbWidth to set
	 */
	public void setThumbWidth(float thumbWidth) {
		this._thumbWidth = thumbWidth;
	}

	/**
	 * @return the ThumbHeight
	 */
	public float getThumbHeight() {
		return _thumbHeight;
	}

	/**
	 * @param _thumbHeight the _thumbHeight to set
	 */
	public void setThumbHeight(float thumbHeight) {
		this._thumbHeight = thumbHeight;
	}

	/**
	 * @return the _textSize
	 */
	public float getTextSize() {
		return _textSize;
	}

	/**
	 * @param _textSize the _textSize to set
	 */
	public void setTextSize(float _textSize) {
		this._textSize = _textSize;
		_labelPos=-1;
	}

	/**
	 * @return the _trackHeight
	 */
	public float getTrackHeight() {
		return _trackHeight;
	}

	/**
	 * @param _trackHeight the _trackHeight to set
	 */
	public void setTrackHeight(int _trackHeight) {
		this._trackHeight = _trackHeight;
	}

	/**
	 * @return the _position
	 */
	public float getPosition() {
		return _position;
	}

	/**
	 * @param _position the _position to set
	 */
	public void setPosition(float _position) {
		this._position = _position;
		if (_forceInt) {this._position=Math.round(_position);}
		boundsPos();
		invalidate();
	}

	/**
	 * @return the _max
	 */
	public float getMax() {
		return _max;
	}

	/**
	 * @param _max the _max to set
	 */
	public void setMax(float _max) {
		this._max = _max;
		boundsPos();
		invalidate();
	}

	/**
	 * @return the _min
	 */
	public float getMin() {
		return _min;
	}

	/**
	 * @param _min the _min to set
	 */
	public void setMin(float _min) {
		this._min = _min;
		boundsPos();
		invalidate();
	}
	
	private String dp1(float f) {
		return BigDecimal.valueOf(f).setScale(1,BigDecimal.ROUND_HALF_EVEN).toString();
	}
	
	private void attemptClaimDrag() {
      if (getParent() != null) {
    	  getParent().requestDisallowInterceptTouchEvent(true);
      }
    }
	
	public OnSliderChangeListener getOnSeekBarChangeListener() {
		return _onSeekBarChangeListener;
	}

	public void setOnSeekBarChangeListener(OnSliderChangeListener onSeekBarChangeListener) {
		this._onSeekBarChangeListener = onSeekBarChangeListener;
	}
	
	public static interface OnSliderChangeListener {
		public void onProgressChanged(Slider s, float _position, boolean fromUser) ;
	}
	public static class OnEWSliderChangeListener  extends AbstractEWEventListener implements OnSliderChangeListener{
		
		public OnEWSliderChangeListener(Context c) {
			super(c);
		}

		public void onProgressChanged(Slider s, float position, boolean fromUser) {
			try {
				onEWProgressChanged( s,  position, fromUser);
			} catch (Exception e){
				performErrorProcessing(e,this,c);
			}
		}
		public void onEWProgressChanged(Slider s, float position, boolean fromUser) {
			
		}
	}
	/**
	 * @return the _label
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * @param _label the _label to set
	 */
	public void setLabel(String label) {
		this._label = label;
		_labelPos=-1;
		invalidate();
	}

	/**
	 * @return the _displayValue
	 */
	public Float getDisplayValue() {
		return _displayValue;
	}

	/**
	 * @param _displayValue the _displayValue to set
	 */
	public void setDisplayValue(Float _displayValue) {
		this._displayValue = _displayValue;
	}

	/**
	 * @return the _state
	 */
	public State getState() {
		return _state;
	}

	/**
	 * @param _state the _state to set
	 */
	public void setState(State _state) {
		this._state = _state;
	}
	
}
