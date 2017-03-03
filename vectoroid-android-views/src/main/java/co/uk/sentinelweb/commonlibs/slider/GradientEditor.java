package co.uk.sentinelweb.commonlibs.slider;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import co.uk.sentinelweb.commonlibs.R;
import co.uk.sentinelweb.commonlibs.colorpicker.ColorPickerDialog;
import co.uk.sentinelweb.commonlibs.errorwrap.AbstractEWView;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWDialogClickListener;
import co.uk.sentinelweb.commonlibs.util.DispUtil;

public class GradientEditor extends AbstractEWView{
	public static final int INDEX_NONE = -1;
	public static final int SHORT_PRESS_INTERVAL = 100;
	public static final int LONG_PRESS_INTERVAL = 500;
	
	private static float _density = -1;
	
	private Drawable _thumb;
	private Rect _thumbRect = new Rect();
	private Rect _thumbRectInner = new Rect();
	private float _thumbWidth=40;
	private float _thumbHeight=40;
	private int _thumbBorderSize = 3;
	
	private OnSliderChangeListener _onSeekBarChangeListener;
	
	//private float _position;
	public static class GradientElement {
		public int color = 0;
		public float position = 0;
		
		public GradientElement(int color, float position) {
			super();
			this.color = color;
			this.position = position;
		}
		
	}
	Vector<GradientElement> _gradientElements = null;
	private int _dragIndex = INDEX_NONE;
	private int _selIndex = INDEX_NONE;
	private float _pressPosX = -1;
	private float _pressPosY = -1;
	private long _pressTime = -1;
	private boolean _wasMoved = false;
	
	private int _defWidth = 100;
	private int _defHeight = 30;
	
	private float _textSize=13;
	
	private float _trackHeight = 20;
	private String _label = null;
	private float _labelPos = -1;
	
	Paint _trackPaint;
	Paint _thumbPaint;
	Paint _thumbOutlinePaint;
	Paint _textPaint;
	Paint _gradPaint;
	
	Handler _ifHandler;
	
	ColorPickerDialog cpd = null;
	
	AlertDialog _optionsDialog = null;
	
	public GradientEditor( Context context ) {
		super(context);
		init(context,null);
	}
	
	public GradientEditor(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context,attrs); 
	}

	public GradientEditor(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
	}
	
	private void init(Context context, AttributeSet attrs) {
		if (_density ==-1) {
			_density = DispUtil.getDensity(this.getContext());
		}
		_gradientElements = new Vector<GradientEditor.GradientElement>();
		_gradientElements.add(new GradientElement(Color.RED,0));
		//_gradientElements.add(new GradientElement(Color.GREEN,0.3f));
		//_gradientElements.add(new GradientElement(Color.CYAN,0.7f));
		_gradientElements.add(new GradientElement(Color.YELLOW,1));
		
		//setDefaults
		_thumbWidth=40*_density;
		_thumbHeight=40*_density;
		_trackHeight = 40*_density;
		_textSize=13*_density;
		_thumbBorderSize=(int)(10*_density);
		if (attrs!=null) {
			TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.GradientEdit);
			_thumb = (StateListDrawable)a.getDrawable(R.styleable.GradientEdit_thumb); 
			_thumbWidth = a.getDimension(R.styleable.GradientEdit_thumbWidth,40*_density); 
			_thumbHeight = a.getDimension(R.styleable.GradientEdit_thumbHeight,40*_density); 
			_trackHeight = a.getDimension(R.styleable.GradientEdit_trackHeight,20*_density); 
			_label = a.getString(R.styleable.GradientEdit_label); 
	        _textSize = a.getDimension(R.styleable.GradientEdit_textSize,13*_density); 
		}
		if (cpd == null) {
			cpd = new ColorPickerDialog(getContext(), _colourPickerListener);
		}
		_trackPaint = new Paint();
		_trackPaint.setStyle(Style.FILL);
		BitmapDrawable tspdrawable = (BitmapDrawable)context.getResources().getDrawable(R.drawable.ge_tsp_bg); 
		_trackPaint.setShader(new BitmapShader(tspdrawable.getBitmap(), TileMode.REPEAT, TileMode.REPEAT));
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
		
		_gradPaint = new Paint();
		_gradPaint.setStyle(Style.FILL_AND_STROKE);
		_gradPaint.setAntiAlias(true);
		updateGradient();
		
		_thumbOutlinePaint = new Paint();
		_thumbOutlinePaint.setStyle(Style.STROKE);
		_thumbOutlinePaint.setAntiAlias(true);
		_thumbOutlinePaint.setColor(Color.WHITE);
		_thumbOutlinePaint.setPathEffect(cpe);
		
		_ifHandler = new Handler();
		
		setFocusableInTouchMode(true);
		setFocusable(true);
	}
	
	private void updateGradient() {
		//Collections.sort(_gradientElements,_geCmp);
		Vector<GradientElement> copy = new Vector<GradientElement>(_gradientElements);
		Collections.sort(copy,_geCmp);
		int[] colors = new int[copy.size()];
		for (int i=0;i<_gradientElements.size();i++) {colors[i]=copy.get(i).color;}
		float[] positions = new float[copy.size()];
		for (int i=0;i<_gradientElements.size();i++) {positions[i]=copy.get(i).position;}
		LinearGradient linearGradient = new LinearGradient(0, 0, getWidth(), 0, colors, positions, TileMode.CLAMP);
		_gradPaint.setShader(linearGradient);
	}
	
	@Override
	protected void onEWDraw( Canvas canvas ) {
		Log.d("Slider","drawing:");
		float sliderLeft = getPaddingLeft()+_thumbWidth/2;
		float sliderWidth = getMeasuredWidth()-getPaddingRight()-getPaddingLeft()-_thumbWidth;
		
		// track
		_trackPaint.setColor( Color.GRAY );
		float ttop = 0;//3*_density;
		canvas.drawRect( sliderLeft, 0, sliderLeft+sliderWidth, _trackHeight, _trackPaint );
		canvas.drawRect( sliderLeft, 0, sliderLeft+sliderWidth, _trackHeight, _gradPaint );
		
		
		for (int i =0;i<_gradientElements.size();i++) {
			GradientElement ge = _gradientElements.get(i);
			float xpos = toCoord( ge.position );
			boolean selected = _selIndex==1;
			_thumbOutlinePaint.setColor(selected?Color.RED:Color.GRAY);
			canvas.drawRect( xpos, _trackHeight, xpos, _trackHeight+5, _thumbOutlinePaint );
			// thumb
			_thumbRect.set((int)(xpos-_thumbWidth/2), (int)(_trackHeight+5), (int)(xpos+_thumbWidth/2), (int)(_trackHeight+5+_thumbHeight));
			_thumbRectInner.set(_thumbRect.left+_thumbBorderSize,_thumbRect.top+_thumbBorderSize,_thumbRect.right-_thumbBorderSize,_thumbRect.bottom-_thumbBorderSize);
			if (_thumb==null ) {
				_thumbPaint.setColor( ge.color );
				_thumbPaint.setStrokeWidth(0);
				_thumbPaint.setStyle( Style.FILL );
				canvas.drawRect(_thumbRectInner, _thumbPaint );
				canvas.drawRect(_thumbRect , _thumbOutlinePaint );
			} else {
				//canvas.drawBitmap(_thumb, xpos-_thumbWidth/2,0, _trackPaint);
				_thumb.setBounds( _thumbRect );
				_thumb.draw( canvas );
				_thumbPaint.setColor( ge.color );
				_thumbPaint.setStrokeWidth(5);
				_thumbPaint.setStyle( Style.FILL );
				canvas.drawRect(_thumbRectInner, _thumbPaint );
			}
			// thumb notch
			//_trackPaint.setColor( Color.GRAY );
			//canvas.drawRect( xpos-1,_thumbHeight/6f,xpos+1,_thumbHeight/6f*5f, _trackPaint );
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
		updateGradient();
	}
	
	@Override
	protected void onEWMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
        int dw = _defWidth;
        int dh = _defHeight;
        
        dh = (int)Math.max( _thumbHeight+_trackHeight+5, dh );
        
        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();
        
        setMeasuredDimension(resolveSize(dw, widthMeasureSpec), resolveSize(dh, heightMeasureSpec));
	}
	
	@Override
	protected boolean onEWTouchEvent( MotionEvent event ) {
		Log.d("Slider","touch:");
		float position = boundsPos(toPosition(event));
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				_pressTime=System.currentTimeMillis();
				_pressPosX=event.getX();
				_pressPosY=event.getY();
				_wasMoved=false;
				if (_pressPosY>_trackHeight) {
					float minDist = 1;
					int minDistIndex = -1;
					for (int i=0;i<_gradientElements.size();i++) {
						GradientElement gradientElement = _gradientElements.get(i);
						float dist = Math.abs(position-gradientElement.position);
						if ( dist<=minDist ) {
							minDist = dist;
							minDistIndex=i;
						}
					}
					if (minDist*getSliderWidth() <=_thumbWidth/2) {
						_dragIndex = minDistIndex;
					}
					//Log.d(Globals.LOG_TAG, "down:"+_dragIndex+":"+minDist+":"+_thumbWidth/2);
				}
				break;
            case MotionEvent.ACTION_MOVE:{
	            	if (_dragIndex>INDEX_NONE) {
	            		_wasMoved = _wasMoved || (((int)Math.abs(event.getX()-_pressPosX))>5);
		            	if (_dragIndex>0 && _dragIndex<_gradientElements.size()-1 && _wasMoved) {
		            		_gradientElements.get(_dragIndex).position = position;
		            		updateGradient();
			            	attemptClaimDrag();
			            	if (_onSeekBarChangeListener!=null) {;
			            		_onSeekBarChangeListener.onProgressChanged(this, true);
			            	}
		            	}
	            	}
	            }
            break;
            case MotionEvent.ACTION_UP:{
            		_wasMoved = _wasMoved || (((int)Math.abs(event.getX()-_pressPosX))>5);
	            	long pressTime = System.currentTimeMillis()-_pressTime;
	            	if (_dragIndex>INDEX_NONE) {
		            	if (_dragIndex>0 && _dragIndex<_gradientElements.size()-1 && _wasMoved) {
		            		if (position==0) {
		            			_gradientElements.get(0).color = _gradientElements.get( _dragIndex ).color;
		            			_gradientElements.remove( _dragIndex );
		            		} if (position==1) {
		            			_gradientElements.get(_gradientElements.size()-1).color = _gradientElements.get( _dragIndex ).color;
		            			_gradientElements.remove( _dragIndex );
		            		} else {
		            			_gradientElements.get(_dragIndex).position = position;
		            		}
		            		updateGradient();
			            	if (_onSeekBarChangeListener!=null) {
			            		_onSeekBarChangeListener.onProgressChanged(this, true);
			            	}
		            	} else {
		            		//Log.d(Globals.LOG_TAG, "up:"+_dragIndex);
		            		GradientElement ge = _gradientElements.get( _dragIndex );
		            		if (pressTime>LONG_PRESS_INTERVAL) {
		            			showOptionsDialog();
		            		} else if (pressTime>SHORT_PRESS_INTERVAL) {
		            			cpd.show();
		            			cpd.setColor(ge.color);
		            		}
		            	}
		            	_selIndex=_dragIndex;//!=_selIndex?_dragIndex:-1;
	            	} else if (_pressPosY<_trackHeight && pressTime>LONG_PRESS_INTERVAL) {
	            		int index = getClosestIndex(position);
	            		if (index==0) {
	            			index++;
	            		} 
	            		addPoint(_gradientElements.get(index).color, position, index);
	            		updateGradient();
	            		if (_onSeekBarChangeListener!=null) {
		            		_onSeekBarChangeListener.onProgressChanged(this,  true);
		            	}
	            		_selIndex=-1;
	            	} else {
	            		_selIndex=-1;
	            		
	            	}
	            	_pressPosX = -1;
	            	_pressTime = -1;
	            	_dragIndex=INDEX_NONE;
	            	_wasMoved = false;
	            }
            	break;
		}
		invalidate();
		return true;
	}

	private float toPosition(MotionEvent event) {
		float sliderLeft = getPaddingLeft()+_thumbWidth/2;
		float sliderWidth = getSliderWidth();
		float position = (event.getX()-sliderLeft)/(sliderWidth);
		return position;
	}

	private float getSliderWidth() {
		return getWidth()-getPaddingRight()-getPaddingLeft()-_thumbWidth;
	}
	
	private float toCoord(float position) {
		float sliderLeft = getPaddingLeft()+_thumbWidth/2;
		float sliderWidth = getSliderWidth();
		float x = position*(sliderWidth)+sliderLeft;
		return x;
	}
	
	private float boundsPos(float pos) {
		return Math.min(Math.max(pos, 0), 1);
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
	
	public static class OnSliderChangeListener {
		public void onProgressChanged(GradientEditor s,  boolean fromUser) {
			
		}
	}
	public void showOptionsDialog() {
		if (_optionsDialog==null) {
			final String[] opts =new String[]{
					this.getContext().getString(R.string.ge_opt_add_r),  
					this.getContext().getString(R.string.ge_opt_add_l),  
					this.getContext().getString(R.string.ge_opt_del)
				};
			_optionsDialog = new AlertDialog.Builder(this.getContext())
					.setTitle(R.string.ge_opt_title)
					.setIcon(R.drawable.col_dia_i_colour)
					.setItems(opts,new OnEWDialogClickListener(this.getContext()) {
						public void onEWClick(DialogInterface dialog, int which) {
							GradientElement ge = _gradientElements.get(_selIndex);
							switch (which) {
								case 0: if ( _selIndex < _gradientElements.size()-1 ) {
											addPoint(ge.color, ge.position+0.1f,_selIndex+1);
										}
										break;
								case 1: if ( _selIndex > 0 ) {
											addPoint(ge.color, ge.position-0.1f,_selIndex);
										}
										break;
								case 2: if (_selIndex>0 && _selIndex<_gradientElements.size()-1) {
											_gradientElements.remove(ge);
										}
										break;
							}
							updateGradient();
							invalidate();
							if (_onSeekBarChangeListener!=null) {
			            		_onSeekBarChangeListener.onProgressChanged(GradientEditor.this,  true);
			            	}
						}

						
					})
					.create();
		}
		_optionsDialog.show();
	}
	
	private void addPoint(int col,float pos,int idx) {
		GradientElement newGe = new GradientElement(col, boundsPos(pos));
		_gradientElements.insertElementAt(newGe, idx);
	}
	
	private int getClosestIndex(float pos) {
		int closestIndex = -1;
		float minDist = 1;
		for (int i=0;i<_gradientElements.size();i++) {
			float dist = Math.abs(pos-_gradientElements.get(i).position);
			if (dist<minDist ) {
				minDist = dist;
				closestIndex=i;
			}
		}
		return closestIndex;
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
	
	ColorPickerDialog.OnColorChangedListener _colourPickerListener = new ColorPickerDialog.OnColorChangedListener() {
  		@Override 
		public void colorChanged(int color) {
  			GradientElement ge = _gradientElements.get(_selIndex);
  			ge.color = color;
  			updateGradient();
  			invalidate();
  			if (_onSeekBarChangeListener!=null) {
        		_onSeekBarChangeListener.onProgressChanged(GradientEditor.this,  true);
        	}
  		}
	};
	
	Comparator<GradientElement> _geCmp = new Comparator<GradientEditor.GradientElement>() {
		@Override
		public int compare(GradientElement object1, GradientElement object2) {
			
			return object1.position>object2.position?1:-1;
		}
	};	
	
	public void getGradient(Vector<GradientElement> copy) {
		copy.clear();
		copy.addAll(_gradientElements);
		Collections.sort(copy,_geCmp);
	}
	public void setGradient(int[] color,float[] pos) {
		// assumes color and position are both filled in
		_gradientElements.clear();
		if (pos==null) {
			pos=new float[color.length];
			for (int i=0;i<color.length-1;i++) {
				pos [i] = i/(color.length-1);
			}
			pos[color.length-1]=1;
		}
		for (int i =0;i<color.length;i++ ) {
			GradientElement ge = new GradientElement(color[i], pos[i]);
			_gradientElements.add(ge);
		}
		updateGradient();
		invalidate();
	}
}
