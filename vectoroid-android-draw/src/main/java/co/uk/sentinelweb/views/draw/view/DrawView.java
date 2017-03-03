package co.uk.sentinelweb.views.draw.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;
import co.uk.sentinelweb.commonlibs.Globals;
import co.uk.sentinelweb.commonlibs.errorwrap.AbstractEWView;
import co.uk.sentinelweb.commonlibs.errorwrap.EWRunnable;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWAsyncListener;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.controller.TransformControllerDV;
import co.uk.sentinelweb.views.draw.gpc.DrawingPolyConvert;
import co.uk.sentinelweb.views.draw.gpc.SegmentGraph;
import co.uk.sentinelweb.views.draw.model.Drawing;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.Fill;
import co.uk.sentinelweb.views.draw.model.Gradient;
import co.uk.sentinelweb.views.draw.model.GradientData;
import co.uk.sentinelweb.views.draw.model.Group;
import co.uk.sentinelweb.views.draw.model.Layer;
import co.uk.sentinelweb.views.draw.model.Pen;
import co.uk.sentinelweb.views.draw.model.Pen.PenField;
import co.uk.sentinelweb.views.draw.model.PointVec;
import co.uk.sentinelweb.views.draw.model.Stroke;
import co.uk.sentinelweb.views.draw.model.Stroke.Type;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.model.internal.Magnifier;
import co.uk.sentinelweb.views.draw.model.internal.ModeData;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Draw;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Mode;
import co.uk.sentinelweb.views.draw.model.internal.OnDrawTouchListener;
import co.uk.sentinelweb.views.draw.model.internal.TouchData;
import co.uk.sentinelweb.views.draw.model.internal.TouchData.Action;
import co.uk.sentinelweb.views.draw.model.internal.ViewPort;
import co.uk.sentinelweb.views.draw.model.internal.ZoomScope;
import co.uk.sentinelweb.views.draw.model.path.PathData;
import co.uk.sentinelweb.views.draw.overlay.AnchorOverlay;
import co.uk.sentinelweb.views.draw.overlay.ControlsOverlay;
import co.uk.sentinelweb.views.draw.overlay.EditTextOverlay;
import co.uk.sentinelweb.views.draw.overlay.GradientOverlay;
import co.uk.sentinelweb.views.draw.overlay.OutlineOverlay;
import co.uk.sentinelweb.views.draw.overlay.Overlay;
import co.uk.sentinelweb.views.draw.overlay.SelectionOverlay;
import co.uk.sentinelweb.views.draw.overlay.TouchTestOverlay;
import co.uk.sentinelweb.views.draw.render.VecRenderer;
import co.uk.sentinelweb.views.draw.render.ag.AndGraphicsRenderer;
import co.uk.sentinelweb.views.draw.undo.AddDEUndoElement;
import co.uk.sentinelweb.views.draw.undo.UndoElement.UndoOperationType;
import co.uk.sentinelweb.views.draw.util.DebugUtil;
import co.uk.sentinelweb.views.draw.util.DispUtil;
import co.uk.sentinelweb.views.draw.util.MeanFilter;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.util.StrokeUtil;
import co.uk.sentinelweb.views.draw.view.controller.DrawingElementController;
import co.uk.sentinelweb.views.draw.view.controller.FontViewController;
import co.uk.sentinelweb.views.draw.view.controller.UndoController;

import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;

public class DrawView extends AbstractEWView {
	
	public static final int MAX_ADDPOINTS = 500;
	public static float _density = -1;
	public boolean  _isDebug = DVGlobals._isDebug || false;
	public enum CopyMode {CUT,COPY,PASTE,DUPLICATE}
	
	//update
	public static final int UPDATE_ANCHOR = 0;
	public static final int UPDATE_SCOPE = 1;
	public static final int UPDATE_CONTROLS = 2;
	public static final int UPDATE_CLIP = 3;
	public static final int UPDATE_UNDO=4;
	public static final int UPDATE_UNDO_OP=5;
	public static final int UPDATE_MODE=6;
	public static final int UPDATE_ZOOM=7;
	public static final int UPDATE_SIZE=8;
	
	public boolean[] _updateFlags = new boolean[UPDATE_SIZE];
	public long[] _updateTimes = new long[UPDATE_SIZE];
	
	//public UndoElement.UndoOperationType _updateUndoType= null;
	public ModeData _mode;
	public ViewPort _viewPort;
	public Magnifier _magnifier;
	//public Selection selection = new Selection();
	public ZoomScope _zoomScope;
	
	public Vector<OnDrawTouchListener> _touchListeners = new Vector<OnDrawTouchListener>();
	public HashMap<ModeData.Mode,OnDrawTouchListener> _modeTouchListeners = new HashMap<ModeData.Mode,OnDrawTouchListener>();
	public Vector<Overlay> _overlays = new Vector<Overlay>();
	
	public TouchData _touchData;
	public SelectionOverlay _selectionOverlay;
	public ControlsOverlay _controlsOverlay;
	public AnchorOverlay _anchorOverlay;
	public GradientOverlay _gradientOverlay;
	public EditTextOverlay _editTextOverlay;
	
	public TouchTestOverlay _ttOverlay;
	public OutlineOverlay _outOverlay;
	
	public TransformControllerDV _transformController;
	public UndoController _undoController;
	public FontViewController _fontController;
	public DrawingElementController _drawingElementController;
	public Drawing _drawing;
	
	Pen _currentPen;
	Fill _currentFill;
	Stroke _currentStroke;
	public Layer _currentLayer;
	Typeface _currentTTFont = Typeface.DEFAULT;
	String _currentTTFontName = null;
	float _currentFontHeight=Stroke.DEFAULT_FONT_HEIGHT;
	float _currentFontHeightDefault=Stroke.DEFAULT_FONT_HEIGHT;
	//int _ttfFontMinHeight = 50;
	
	public int _bgColor = Color.WHITE;
	
	
	Stroke _circleTemplate ;
	Stroke _shapeTemplate ;
	// copy variable
	public ArrayList<DrawingElement> _clipboard = new ArrayList<DrawingElement>();
	public Group _currentClipboardElement = null;
	int _maxClipboard = 10;
	
	public boolean _displayData = true;
	public boolean _displayBorder = true;
	
	
	
	Vibrator _vibrator ;
	boolean _isRendering=false;
	public boolean _dontDraw = false;
	//Bitmap _drawBitmap = null;
	//Bitmap _drawBitmapCache = null;
	//Canvas _drawCanvas = null;
	//Canvas _drawCanvasCache = null;
	Bitmap _touchBitmapCache = null;
	Bitmap _touchBitmapCacheBehind = null;
	
	Canvas _touchBitmapCacheBehindCanvas = null;
	boolean _touchBitmapCacheBehindIsWritten = false;
	
	boolean _isDrawing=false;
	boolean _dirty;
	Paint _bmpPaint;
	public Paint _modePaint;
	Paint _borderPaint;
	//Paint clearPaint;
	//private MeanFilter touchFilter = new MeanFilter( 5, 0.25f );
	private MeanFilter _touchFilter = new MeanFilter( 1, 0.25f );
	//StrokesRenderer _strokesRenderer;
	
	private OnUpdateListener _onUpdateListener = null;
	private DrawViewClient _drawViewClient = null;
	
	// tmp vars
	private PointF _usePoint = new PointF();
	private PointF _usePoint2 = new PointF();
	private PointF _usePoint3 = new PointF();
	private RectF _useRectF = new RectF();
	private RectF _useRectF2 = new RectF();
	private Rect _useRect = new Rect();
	private StringBuilder _useSb=new StringBuilder();
	
	Handler _updateHandler = new Handler();
	EWRunnable _updateRunnable;
	
	public AndGraphicsRenderer _renderer = null;
	
	OnEWAsyncListener<Boolean> _softKeyBoardListener;
	
	
	public DrawView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public DrawView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {//v.vibrate(GlobalsParent.VIBRATE_TIME_SILENT);
		
		if (_density==-1) {_density=DispUtil.getDensity(context);	}
		 
		_mode = new ModeData(this);
		_viewPort = new ViewPort(this);
		_magnifier = new Magnifier(this, 80);
		_zoomScope  = new ZoomScope(this);
		_touchData = new TouchData(getContext());
		_bmpPaint = new Paint();
		_bmpPaint.setFilterBitmap(true);
		_bmpPaint.setAntiAlias(true);
		_bmpPaint.setARGB(255, 0, 0, 0);
		
		_vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		
		_renderer = new AndGraphicsRenderer();//this.getContext()
		_renderer.setVpd(_viewPort.data);
		_renderer._feedBackLevelSnappingToModel=true;
		
		_selectionOverlay = new SelectionOverlay(this);
		_controlsOverlay = new ControlsOverlay(this);
		_anchorOverlay = new AnchorOverlay(this);
		_gradientOverlay = new GradientOverlay(this);
		_ttOverlay = new TouchTestOverlay(this);
		_editTextOverlay = new EditTextOverlay(this);
		//_outOverlay = new OutlineOverlay(this);
		
		_modePaint = new Paint();
		//_modePaint.setARGB(128, 255, 255,255);
		_modePaint.setARGB(128, 100, 100, 100);
		_modePaint.setStyle(Style.FILL_AND_STROKE);
		_modePaint.setTextSize(15*_density);
		_modePaint.setStrokeWidth(1*_density);
		_modePaint.setAntiAlias(true);
		_modePaint.setARGB(192, 0, 0, 0);
		_borderPaint = new Paint(_modePaint);
		_borderPaint.setStyle(Style.STROKE);
		
		//clearPaint = new Paint();
		//clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));//new PorterDuff(PorterDuff.Mode.CLEAR)
		
		_mode.setModeText();
		_mode.freeDrawOptions = ModeData.OPT_FREEDRAW_AUTOSMOOTH;
		_mode.freeDrawSmoothLevel = 5;
		_mode.lineDrawOptions = ModeData.OPT_LINEDRAW_SNAPPING;
		
		//addOnTouchListener(multiTouchListener);
		//addOnTouchListener(drawTouchListener);
		//addOnTouchListener(panTouchListener);
		addOnTouchListener(_zoomPanMultiTouchListener);
		//addOverlay(_outOverlay);
		addOverlay(_controlsOverlay);
		addOverlay(_selectionOverlay);
		addOnTouchListener(_modeTouchListener);
		
		_modeTouchListeners.put(Mode.DRAW, _drawTouchListener);
		_modeTouchListeners.put(Mode.EDIT, _editTouchListener);
		_modeTouchListeners.put(Mode.POINTS_SEL, _nullTouchListener);
		_modeTouchListeners.put(Mode.PAN, _panTouchListener);
		_modeTouchListeners.put(Mode.SELRECT, _selectionOverlay.selrectTouchListener);
		_modeTouchListeners.put(Mode.ZOOM, _nullTouchListener);
		_modeTouchListeners.put(Mode.PAN_AND_ZOOM, _nullTouchListener);
		_modeTouchListeners.put(Mode.EDIT_TEXT, _nullTouchListener);
		//_modeTouchListeners.put(Mode.EDIT_TEXT, _editTextOverlay);
		
		addOverlay(_anchorOverlay, Mode.SETANCHOR);
		addOverlay(_gradientOverlay, Mode.GRADIENT);
		
		_currentPen = new Pen();
		_currentFill = new Fill();
		
		//_strokesRenderer = new StrokesRenderer(this.getContext());//this._viewPort
		
		_transformController = new TransformControllerDV(this);
		_undoController = new UndoController(this);
		_fontController=new FontViewController(this);
		_drawingElementController = new DrawingElementController( this );
		
		_circleTemplate = new Stroke(_currentPen,_currentFill);
		//_circleTemplate.currentVec.pressure=new ArrayList<Float>();
		//_circleTemplate.currentVec.time=new ArrayList<Long>();
		makeCircleTemplate(_circleTemplate);
		
		_shapeTemplate = new Stroke(_currentPen,_currentFill);
		//_shapeTemplate.currentVec.pressure=new ArrayList<Float>();
		//_shapeTemplate.currentVec.time=new ArrayList<Long>();
		makeShapeTemplate(_shapeTemplate);
		//initTouchCache();
		//renderer.setFontController(_fontController);
		
		_updateRunnable = new EWRunnable(this.getContext()) {
			@Override
			public void doEWrun() {
				//if (_isDebug) if (_isDebug) Log.d(Globals.LOG_TAG, "..... running update later");
				if (!_isRendering) invalidate();
			}
		};
		
		_softKeyBoardListener = new OnEWAsyncListener<Boolean>(getContext()) {
			@Override
			public void onEWAsync(Boolean request) {
				if (_mode.opState==Mode.EDIT_TEXT ) {
					if (_currentStroke!=null) {
						//ArrayList<PointF> curVec = _currentStroke.currentVec;
						//_currentStroke.update(false, _renderer, UpdateFlags.ALL);
						if (testViewPort(_currentStroke)) {
						 	_viewPort.setViewPortByElement(_currentStroke,(int)(120*_density));
						} else {
							updateDisplay();
						}
						//updateCached();
					} else if (_selectionOverlay.getStrokeSelection().size()==1 ) {
						Stroke s = _selectionOverlay.getStrokeSelection().get(0);
						//s.update(false, _renderer, UpdateFlags.ALL);
						if (testViewPort(s)) {
							_viewPort.setViewPortByElement(s,(int)(120*_density));
						} else {
							updateDisplay();
						}
						//updateDisplay();
					}
				}
			}
		};
	}
	
	
	public void addOverlay (Overlay o) {
		_overlays.add(o);
		addOnTouchListener(o);
	}
	
	public void addOverlay (Overlay o, Mode m) {
		_overlays.add(o);
		_modeTouchListeners.put(m,o);
	}
	
	public void addOnTouchListener (OnDrawTouchListener odtl) {
		_touchListeners.add(odtl);
	}
	
	
	// TODO : Important : memory mgmt: 
	//    * dont create a bitmap each time 
	//    * re-use he existing bitmap and limit the dimensions while rendering.
	public void initTouchCache() {
		if (_touchBitmapCacheBehind!=null) {
			_touchBitmapCacheBehind.recycle();
			_touchBitmapCacheBehind=null;
			_touchBitmapCache=null;
			System.gc();
		}
		_calledFromUpdate=false;
		_touchBitmapCacheBehindIsWritten=false;
		int ctr = 0;
		while (ctr < 3 && _touchBitmapCacheBehind==null) {
			try {
				_touchBitmapCacheBehind = Bitmap.createBitmap(getMeasuredWidth(),getMeasuredHeight(), Bitmap.Config.ARGB_8888);//Config.RGB_565
				_touchBitmapCacheBehindCanvas = new Canvas();
				_touchBitmapCacheBehindCanvas.setBitmap(_touchBitmapCacheBehind);
				if (_isDebug) Log.d(DVGlobals.LOG_TAG, "---------------- _touchBitmapCacheBehind init:"+_touchBitmapCacheBehind.getWidth()+"X"+_touchBitmapCacheBehind.getHeight());
			} catch (OutOfMemoryError e) {
				try {
					DebugUtil.logCall("init cache bmp:", e);
					System.gc();
					Thread.sleep(300);
					
				} catch (InterruptedException e1) {
					
				}
				ctr++;
			}
		}
		//}
	}
	public void useTouchCache() {
		_touchBitmapCache = _touchBitmapCacheBehind;
	}
	
	public void dropTouchCache() {
			_touchBitmapCache=null;
			//_touchBitmapCacheBehindIsWritten=false;//RM: NOTE: test this to stop refreshing bugs 
	}
	public void setDrawing(Drawing d,boolean addUndo) {
		setDrawing( d, addUndo,true);
	}
	public void setDrawing(Drawing d,boolean addUndo,boolean resetLayout) {
		_selectionOverlay.clear();
		_undoController.clearUndos();
		_renderer.dropCache();
		if (this._drawing!=null) {
			setCurrentLayerInternal(-1);
		}
		this._drawing = d;
		if (this._drawing!=null) {
			if (_isDebug) Log.d(DVGlobals.LOG_TAG, "------------------load");
			this._drawing.update( true, _renderer, UpdateFlags.ALL );
			if (resetLayout) {
				_viewPort.resetLayout();
				if ( getMeasuredWidth()>0 ) {
					_viewPort.calculateLayout();
					_zoomScope.calculateLayout();
				}
			}
		}
		//initDrawBitmap();
		
		if ( _onUpdateListener!=null ) {
			_onUpdateListener.load(this);
		}
		//initTouchCache();
		dropTouchCache();
		if (addUndo) {
			//_updateUndoType=UndoOperationType.ALL;
			_undoController.initNextUndo(UndoOperationType.ALL);
			_updateFlags[ UPDATE_UNDO ]=true;
		}
		_updateFlags[ UPDATE_CONTROLS ]=true;
		_updateFlags[ UPDATE_ANCHOR ]=true;
		_updateFlags[ UPDATE_SCOPE ]=true;
		_updateFlags[ UPDATE_CLIP ]=true;
		updateDisplay();
		setDirty(false);
	}
	////////////////////////////////////////Touch Methods / Listeners ////////////////////////////////////////////////
	
	@Override
	protected boolean onEWTouchEvent(MotionEvent event) {
		
		_touchData.setEvent( event, _viewPort.data.topLeft, 1/_viewPort.data.zoom );
		
		/*
		_touchData._touchPointOnScreen = new PointF(event.getX(),event.getY());
		//Log.d(Globals.LOG_TAG, "touch: pressure:"+event.getPressure());;
		switch (_touchData._a ) {
			case ACTION_DOWN: 
				_touchData._touchDownTime = System.currentTimeMillis();
				break;
			case ACTION_MOVE: 
				break;
			case ACTION_UP:
				break;
		}
		*/
		/*
		switch (event.getAction() ) {
			case MotionEvent.ACTION_DOWN: 
				touchData.touchDownTime = System.currentTimeMillis();
				touchFilter.clear();
				//Log.d(Globals.TAG, "dn: pressure:"+event.getPressure()+": len:"+touchFilter.length+": actual:"+touchFilter.actualValues+": tch:"+tostr(touchPointOnScreen)+": actual:"+tostr(new PointF(event.getX(),event.getY())));
				break;
			case MotionEvent.ACTION_MOVE: 
				touchFilter.add(event.getX(),event.getY());
				//if (event.getPressure()<0.05) {return true;}
				if (event.getPressure()<0.25) {
					//touchPointOnScreen = touchFilter.get(touchPointOnScreen);
					touchFilter.get(touchData.touchPointOnScreen,event.getPressure());
					//Log.d(Globals.TAG, "mv: pressure:"+event.getPressure()+": len:"+touchFilter.length+": actual:"+touchFilter.actualValues+": tch:"+tostr(touchPointOnScreen)+": actual:"+tostr(new PointF(event.getX(),event.getY())));
				} 
				break;
			case MotionEvent.ACTION_UP:
				touchFilter.get(touchData.touchPointOnScreen,event.getPressure());
				//Log.d(Globals.TAG, "up: pressure:"+event.getPressure()+": len:"+touchFilter.length+": actual:"+touchFilter.actualValues +": tch:"+tostr(touchPointOnScreen)+": actual:"+tostr(new PointF(event.getX(),event.getY())));
				break;
		}
		*/
		//_touchData._touchPoint = new PointF(_viewPort.topLeft.x+_touchData._touchPointOnScreen.x/_viewPort.zoom,_viewPort.topLeft.y+_touchData._touchPointOnScreen.y/_viewPort.zoom);
		/*
		PointF touchPoint = new PointF(_viewPort.topLeft.x+_touchData._touchPointOnScreen.x/_viewPort.zoom,_viewPort.topLeft.y+_touchData._touchPointOnScreen.y/_viewPort.zoom);
		_touchData._touchPoint = touchPoint;
		if ( _touchData._a==Action.ACTION_DOWN ) {
			_touchData._reference = _touchData._touchPoint;
			_touchData._referenceOnScreen = _touchData._touchPointOnScreen;
		}
		if ( _touchData._isMulti ) {
			if ( _touchData._a==Action.ACTION_MULTI_DOWN ) {
				_touchData._reference2 = _touchData._touchPoint;
				_touchData._referenceOnScreen2 = _touchData._touchPointOnScreen;
			}
		} 
		*/
		//if (_isDebug) Log.d(DVGlobals.LOG_TAG, "touch: start event:"+ _touchData._a+":"+event.getAction()+":"+_mode.opState);;
		for ( OnDrawTouchListener odtl:_touchListeners ) {
			boolean finish = odtl.onTouch(_touchData);
			//if (_isDebug) Log.d(DVGlobals.LOG_TAG, "touch: break= "+ finish+":"+odtl.getClass().getCanonicalName());
			if ( finish ) {
				break;
			}
		}
		//if (_isDebug) Log.d(DVGlobals.LOG_TAG, "touch: end event:");;
		if ( _touchData._a==Action.ACTION_UP ) {// ||  _touchData._a==Action.ACTION_MULTI_UP
			_touchData.clean();//TODO sometimes this isnt called set a handelr to clean up
		}
		return true;
	}
	/*
	private OnDrawTouchListener multiTouchListener = new OnDrawTouchListener() {
		@Override
		public boolean onTouch(TouchData td) {
			return false;
		}
	};
	*/
	private OnDrawTouchListener _nullTouchListener = new OnDrawTouchListener() {
		@Override
		public boolean onTouch(TouchData td) {
			return false;
		}
	};
	private OnDrawTouchListener _modeTouchListener = new OnDrawTouchListener() {
		@Override
		public boolean onTouch(TouchData td) {
			//if (modeTouchListeners.get(mode.opState)!=null) {
				return _modeTouchListeners.get(_mode.opState).onTouch(td);
			//} return false;
		}
	};
	
	private OnDrawTouchListener _drawTouchListener = new OnDrawTouchListener() {
		int ctr = 0;
		long _startTime = -1;
		float _maxPressure = 0;
		@Override
		public boolean onTouch(TouchData td) {
			PathData pd=null;
			//if (mode.opState==ModeData.Mode.DRAW) {
				switch (td._e.getAction() ) {
					case MotionEvent.ACTION_DOWN: 
						_maxPressure=0;
						//if (!getCurrentLayer().contains(_currentStroke)) {//wtf unnessecary???
							if (_currentStroke!=null && _currentStroke.type!=Stroke.Type.LINE) {
								_currentStroke=null;// burn the current stroke if not a line type - possibly touchup has been missed
							}
							if (_currentStroke==null) {
								_currentStroke = new Stroke(_currentPen.duplicate(),_currentFill.duplicate());
							} 
							
							if( _mode.drawMode==ModeData.Draw.LINE) {
								pd=new PathData(td._touchPoint);
								_currentStroke.currentVec.add(pd);
							} else if(  _mode.drawMode==ModeData.Draw.TEXT ) {
								_currentStroke.fill.type=Fill.Type.COLOUR_STROKE;
								if (_currentStroke.pen.strokeWidth>0.1) {
									_currentStroke.pen.strokeWidth=0.1f;
								}
								pd=new PathData(td._touchPoint);
								_currentStroke.currentVec.add(pd);
								_currentStroke.type=Stroke.Type.TEXT_TTF;
								_currentStroke.fontName=_currentTTFontName;
								_currentFontHeight=_currentFontHeightDefault;
							}
							_startTime = System.currentTimeMillis();
							_currentStroke.currentVec.startTime = _startTime;
							//_currentStroke.currentVec.time=new ArrayList<Long>();
							//_currentStroke.currentVec.pressure=new ArrayList<Float>();
							//_currentStroke.currentVec.time.add(0l);
							//_currentStroke.currentVec.pressure.add(td._e.getPressure());
							if (pd!=null) {
								pd.timeDelta=0;
								pd.pressure=td._e.getPressure();
							}
							switch (_mode.drawMode) {
								case FREE:
									_currentStroke.type = Stroke.Type.FREE;
									break;
								case RECT:
								case CIRCLE:
								case SHAPE:
								case LINE:
									_currentStroke.type = Stroke.Type.LINE;
									break;
								case TEXT:
									//_currentStroke.renderObject.fontTTF = _currentTTFont;
									_currentStroke.type = Stroke.Type.TEXT_TTF;
									break;
							}
							useTouchCache();
						//}
						_isDrawing=true;
						return true;
					case MotionEvent.ACTION_MOVE: 
						if (_currentStroke==null) {return false;}
						if (td.underThreshold()) {return false;}
						_maxPressure=Math.max(_maxPressure, td._e.getPressure());
						switch(_mode.drawMode) {
							case FREE: 
								//if (td._e.geth) {
									
								//}
								if (_isDebug) Log.d(DVGlobals.LOG_TAG, 	"device: "+td._e.getDeviceId()+": HIST:"+td._e.getHistorySize());
								if (td._e.getHistorySize()>0) {
									for (int i=0;i<td._e.getHistorySize();i++ ){
										PointF histroicalPoint = td.getHistroicalPoint(0,i,  _viewPort.data.topLeft, 1/_viewPort.data.zoom);
										pd=new PathData(histroicalPoint);
										pd.timeDelta=(int)(System.currentTimeMillis()-_startTime);
										pd.pressure=td._e.getHistoricalPressure(0, i);
										_currentStroke.currentVec.add(pd);
										//_currentStroke.currentVec.time.add(System.currentTimeMillis()-_startTime);
										//_currentStroke.currentVec.pressure.add(td._e.getHistoricalPressure(0, i));
									}
								}
								pd=new PathData(td._touchPoint);
								pd.timeDelta=(int)(System.currentTimeMillis()-_startTime);
								pd.pressure=td._e.getPressure();
								_currentStroke.currentVec.add(pd);
								//_currentStroke.currentVec.time.add(System.currentTimeMillis()-_startTime);
								//_currentStroke.currentVec.pressure.add(td._e.getPressure());
								
								//_currentStroke.makePath();
								_currentStroke.update(false, _renderer, UpdateFlags.PATHONLY);
								break;
							case LINE:
								if (( (_mode.lineDrawOptions&ModeData.OPT_LINEDRAW_SNAPPING)==ModeData.OPT_LINEDRAW_SNAPPING )) {
									td._touchPoint.set(snapInStroke(_currentStroke,td._touchPoint));
								}
								if (_currentStroke.currentVec.size()>0) {
									pd=_currentStroke.currentVec.get(_currentStroke.currentVec.size()-1);
									pd.set(td._touchPoint);
									//_currentStroke.currentVec.set(_currentStroke.currentVec.size()-1,pd);
									//_currentStroke.currentVec.pressure.add(td._e.getPressure());
								} else {
									pd=new PathData(td._touchPoint);
									//_currentStroke.currentVec.add(td._touchPoint);
									_currentStroke.currentVec.add(pd);
								}
								pd.pressure=td._e.getPressure();
								pd.timeDelta=(int)(System.currentTimeMillis()-_startTime);
								
								//_currentStroke.makePath();
								_currentStroke.update(false, _renderer, UpdateFlags.PATHONLY);
								break;
							case CIRCLE: 
								_useRectF.set(td._reference.x,td._reference.y,td._touchPoint.x,td._touchPoint.y);
								makeCircle(_currentStroke,_useRectF );
								
								//_currentStroke.makePath();
								_currentStroke.update(false, _renderer, UpdateFlags.PATHONLY);
								break;
							case RECT: 
								_useRectF.set(td._reference.x,td._reference.y,td._touchPoint.x,td._touchPoint.y);
								makeRect(_currentStroke, _useRectF);
								//_currentStroke.makePath();
								_currentStroke.update(false, _renderer, UpdateFlags.PATHONLY);
								break;
							case SHAPE: 
								_useRectF.set(td._reference.x,td._reference.y,td._touchPoint.x,td._touchPoint.y);
								makeShape(_currentStroke, _useRectF);
								//_currentStroke.makePath();
								_currentStroke.update(false, _renderer, UpdateFlags.PATHONLY);
								break;
							case TEXT: 
								if (!getCurrentLayer().contains(_currentStroke)) {
									_useRectF.set(td._reference.x,td._reference.y,td._touchPoint.x,td._touchPoint.y);
									makeTextRec(_currentStroke,_useRectF);
									/*
									if (_currentStroke.currentVec.size()==1) {
										_currentStroke.currentVec.add(td._touchPoint);
										_currentStroke.currentVec.time.add(System.currentTimeMillis()-_startTime);
										
									} else {
										_currentStroke.currentVec.set(1,td._touchPoint);
										_currentStroke.currentVec.time.set(1,System.currentTimeMillis()-_startTime);
										
									}
									*/
								}
								
								//_currentStroke.updateBoundsAndCOG();
								_currentStroke.update(false, _renderer, UpdateFlags.BOUNDSONLY);
								break;
						}
						//();
						//ctr++;
						if (!_isRendering ) {_updateHandler.post(_updateRunnable);}//&& ctr%20==0
						return true;
					case MotionEvent.ACTION_UP:
						if (_currentStroke==null) {return false;}
						Stroke fireAfterDraw=null;
						//Log.d(DVGlobals.LOG_TAG, "draw up:"+_drawViewClient);
						if (_mode.drawMode==Draw.LINE ||_mode.drawMode==Draw.TEXT || PointUtil.dist(td._referenceOnScreen, td._touchPointOnScreen)>1) {
							switch(_mode.drawMode) {
								case TEXT: 
									if (!getCurrentLayer().contains(_currentStroke)) {
										if (_drawViewClient!=null) {
											_drawViewClient.showText(DrawView.this,_currentStroke);
										}
									}
									if (_currentStroke.currentVec.size()<=1 || 
											Math.abs(_currentStroke.currentVec.get(0).y-_currentStroke.currentVec.get(1).y)<20*_density //drAG WAS ONLY SMALL
										) {
										_useRectF.set(td._reference.x,td._reference.y,td._reference.x+200*_viewPort.data.zoom,td._reference.y-_currentFontHeight*_viewPort.data.zoom);
										makeTextRec(_currentStroke,_useRectF);
									} else {
										if (_currentStroke.calculatedBounds==null) {
											_currentStroke.update(false, _renderer, UpdateFlags.BOUNDSONLY);
										}
										_currentFontHeight=_currentStroke.calculatedBounds.height();
									}
									// RM: maybe modify strokewidth here depending on height?
									if (false){//testViewPort(_currentStroke,false)) {
										_viewPort.setViewPortByElement(_currentStroke,(int)(120*_density));
									}
									_mode.setState(Mode.EDIT_TEXT);
									//_currentStroke.updateBoundsAndCOG();
									//_currentStroke.update(false, _renderer, UpdateFlags.BOUNDSONLY);
									break;
								case LINE://TODO bug here??
									if (( (_mode.lineDrawOptions&ModeData.OPT_LINEDRAW_SNAPPING)==ModeData.OPT_LINEDRAW_SNAPPING )) {
										td._touchPoint.set(snapInStroke(_currentStroke,td._touchPoint));
									}
									/*
									if (_currentStroke.currentVec.size()>0) {
										_currentStroke.currentVec.set(_currentStroke.currentVec.size()-1,td._touchPoint);
									} else {
										_currentStroke.currentVec.add(td._touchPoint);
									}
									_currentStroke.currentVec.time.add(System.currentTimeMillis()-_startTime);
									_currentStroke.currentVec.pressure.add(_maxPressure);
									*/
									if (_currentStroke.currentVec.size()>0) {
										pd=_currentStroke.currentVec.get(_currentStroke.currentVec.size()-1);
										pd.set(td._touchPoint);
										//_currentStroke.currentVec.set(_currentStroke.currentVec.size()-1,pd);
										//_currentStroke.currentVec.pressure.add(td._e.getPressure());
									} else {
										pd=new PathData(td._touchPoint);
										//_currentStroke.currentVec.add(td._touchPoint);
										_currentStroke.currentVec.add(pd);
									}
									pd.pressure=td._e.getPressure();
									pd.timeDelta=(int)(System.currentTimeMillis()-_startTime);
									
									//Log.d(DVGlobals.LOG_TAG, "line up:"+_drawViewClient);
									if (_drawViewClient!=null) {
										_drawViewClient.showLine(DrawView.this);
									}
									//_currentStroke.updateBoundsAndCOG();
									//_currentStroke.makePath();
									_currentStroke.update(false, _renderer, UpdateFlags.PATHBOUNDSONLY);
									break;
								default:
									if (_currentStroke.currentVec.size()>1 ) {
										// TODO closes the stroke if end point close to start. do i want this? (think so)
										_currentStroke.update(false, _renderer, UpdateFlags.BOUNDSONLY);
										float maxDimension = Math.max(_currentStroke.calculatedDim.x, _currentStroke.calculatedDim.y);
										float startEndDist = PointUtil.dist(_currentStroke.currentVec.get(0),_currentStroke.currentVec.get(_currentStroke.currentVec.size()-1));
										if (!_currentStroke.currentVec.closed && startEndDist<maxDimension/50) {
											_currentStroke.currentVec.closed=true;
											//_currentStroke.currentVec.remove(_currentStroke.currentVec.size()-1);
										}
										if ( _mode.drawMode==Draw.FREE && ( (_mode.freeDrawOptions&ModeData.OPT_FREEDRAW_AUTOSMOOTH)==ModeData.OPT_FREEDRAW_AUTOSMOOTH )) {
											for (int i=0;i<_mode.freeDrawSmoothLevel;i++) {
												addPointsInternal(_currentStroke);
												smoothPointsInternal(_currentStroke, 0.9f, ModeData.Smooth.MIDPOINT);
											}
										}
										if ( _mode.drawMode==Draw.FREE){
											reducePointsAngleInternal(_currentStroke, 3);
											reducePointsDistInternal(_currentStroke, maxDimension/1000);
										}
										getCurrentLayer().add(_currentStroke);
										//_currentStroke.updateBoundsAndCOG();
										//_currentStroke.makePath();
										_currentStroke.update(false, _renderer, UpdateFlags.PATHBOUNDSONLY);
										_updateFlags[UPDATE_UNDO]=true;
										_undoController.initNextUndo(UndoOperationType.ADD_ELEMENT);
										((AddDEUndoElement)_undoController.getNextUndoElement())._addedElement=_currentStroke;
										
									}			
									fireAfterDraw = _currentStroke;
									_currentStroke = null;
									//fireAfterDraw=true;
									//if (onUpdatePreviewListener!=null) {
									//	onUpdatePreviewListener.update(DrawView.this);
									//}
									//addUndo();
									break;
							}
							_startTime=-1;
							dropTouchCache();
						}
						updateDisplay();
						if (fireAfterDraw!=null) {
							if (_drawViewClient!=null) {
								_drawViewClient.afterDraw(fireAfterDraw);
							}
						}
						_isDrawing=false;
						return true;
					default : return false;
				}
			//} else {
			//	return false;
			//}
		}
	};
	
	private OnDrawTouchListener _panTouchListener = new OnDrawTouchListener() {
		@Override
		public boolean onTouch(TouchData td) {
			switch (td._a ) {
				case ACTION_DOWN: // down will be caught by selection overlay
					return false;
				case ACTION_MOVE: // single touch panning on start if moved in editTouchListener (Action=EDIT)
					if (td.underThreshold()) {return false;}
					//float dist = PointUtil.dist( td._referenceOnScreen, td._touchPointOnScreen );
					//boolean doPan = dist > 10*_density || td.getTouchDownTime()>1000;
					//if (doPan) {
						useTouchCache();
						_viewPort.doPan(td);
						updateDisplay();
						return true;
					//} return false;
				case ACTION_UP:
					if (PointUtil.dist(td._referenceOnScreen,td._touchPointOnScreen)<SelectionOverlay.MIN_DIST_SELECT) {
						_viewPort.cancelPanning();
						_mode.reverseState();
						return false;
					} else {
						_viewPort.finishPanning();
						_mode.reverseState();
					}
					dropTouchCache();
					updateDisplay();
					return true;
				case ACTION_MULTI_DOWN:
				case ACTION_MULTI_MOVE:
				case ACTION_MULTI_UP:
						_viewPort.cancelPanning();
						return false;
				default : return false;
			}
		}
	};
	
	private OnDrawTouchListener _zoomPanMultiTouchListener = new OnDrawTouchListener() {
		@Override
		public boolean onTouch(TouchData td) {
			switch (td._a ) {
				case ACTION_DOWN: 
					return false;
				case ACTION_MOVE:
					if (_mode.opState==Mode.PAN_AND_ZOOM) {
						return _editTouchListener.onTouch(td);
					}
					return false;//// invoke pan 
				case ACTION_UP:
					return false;
				case ACTION_MULTI_DOWN:
					_useRectF.set(_viewPort.data.zoomCullingRectF);
					if (_mode.opState==Mode.DRAW || _mode.opState==Mode.EDIT_TEXT ||
							(_mode.opState==Mode.EDIT && 
								(	!_controlsOverlay.getUseMultiTouch() || 
									(	!_useRectF.intersect(_selectionOverlay._selectionBounds) || 
										(_selectionOverlay._selection.size()==0 || _controlsOverlay._fix.size()==0)
									)
								)
							)
						) {
						useTouchCache();
						if (_mode.opState==Mode.DRAW) {
							_currentStroke=null;
							_isDrawing=false;
						}
						_mode.setReversibleState(Mode.PAN_AND_ZOOM);
						_viewPort.startZooming();
						return true;
					} else if ( _mode.opState==Mode.PAN_AND_ZOOM) {
						_viewPort.startZooming();
						return true;
					}
					return false;
				case ACTION_MULTI_MOVE:
					if (td.underThreshold()) {return false;}
					
					if (_mode.opState==Mode.PAN_AND_ZOOM) {
						_viewPort.multiTouchPanAndZoom(td);
						return true;
					}
					return false;
				case ACTION_MULTI_UP:
					if (_mode.opState==Mode.PAN_AND_ZOOM) {
						//setDrawingCacheEnabled(false);
						dropTouchCache();
						_viewPort.multiTouchPanAndZoom(td);
						_viewPort.finishPanning();
						if (_mode.lastOpState!=Mode.PAN_AND_ZOOM) {_mode.reverseState();}
						//_mode.reverseState();
						_updateFlags[DrawView.UPDATE_ZOOM]=true;
						_updateFlags[DrawView.UPDATE_SCOPE]=true;
						_updateFlags[DrawView.UPDATE_CONTROLS]=true;
						updateDisplay();
						return true;
					}
					return false;
				default : return false;
			}
		}
	};
	
	private OnDrawTouchListener _editTouchListener = new OnDrawTouchListener() {
		@Override
		public boolean onTouch(TouchData td) {
			switch (td._a ) {
				case ACTION_MOVE: 
					if (panConditions(td) && !td._isMulti) {
						if (_isDebug) Log.d(DVGlobals.LOG_TAG, "pan - start");
						_viewPort.startPanning();
						updateDisplay();
						return true;
					}
					return false;
				}
			return false;
		}
		
		private boolean panConditions(TouchData td) {
			return ( PointUtil.dist( td._referenceOnScreen, td._touchPointOnScreen ) > SelectionOverlay.MIN_DIST_SELECT*2*_density  );//&& _viewPort.data.zoom > _viewPort.data.minZoom
			//return !_selectionOverlay.selectConditions(td);
		}
	};

	private OnDrawTouchListener tmplTouchListener = new OnDrawTouchListener() {
		@Override
		public boolean onTouch(TouchData td) {
			switch (td._a ) {
				case ACTION_DOWN: 
					return false;
				case ACTION_MOVE:
					return false;
				case ACTION_UP:
					return false;
				case ACTION_MULTI_DOWN:
					return false;
				case ACTION_MULTI_MOVE:
					return false;
				case ACTION_MULTI_UP:
					return false;
				default : return false;
			}
		}
	};
	////////////////////////////////////////drawing & rendering ////////////////////////////////////////////////
	long _drawMeasureTime = -1;
	@Override
	public void onEWDraw(Canvas canvas) {
		if (!_dontDraw && _drawing!=null) {//&& _drawCanvas!=null
			_isRendering=true;
			if (!_viewPort.isLayoutCalculated() || _touchBitmapCacheBehind==null || _touchBitmapCacheBehind.getWidth()!=getMeasuredWidth() || _touchBitmapCacheBehind.getHeight()!=getMeasuredHeight()) {
				_viewPort.calculateLayout();
				_zoomScope.calculateLayout();
				_updateFlags[UPDATE_CONTROLS]=true;
				updateFlags();
				_calledFromUpdate=false;// will need to regenrate cacche
				initTouchCache();
			}

			/* * test new rendering * */
			long strt=System.currentTimeMillis();
			_renderer.setCanvas(canvas);
			canvas.drawColor(_bgColor);
			String renderFrom = "none";
			boolean touchBitmapBehindValid = _touchBitmapCacheBehind!=null && _touchBitmapCacheBehindIsWritten ;
			boolean doLog =true;
			
			// NOTE re-order this when time allows. the top condition should be the catch all
			if  ( (/* mem error*/_touchBitmapCache==null && _touchBitmapCacheBehind==null) ) {//memory error - fallback to straight render.
				//Log.d(DVGlobals.LOG_TAG, "draw: no cache");
				_renderer.setupViewPort();
				_renderer.render(_drawing);
				_selectionOverlay.draw(canvas);// RM: move out of here
				_renderer.revertViewPort();
				renderFrom = "no cache";
			} else if ((_touchBitmapCache==null &&  _calledFromUpdate) || (!_calledFromUpdate && !touchBitmapBehindValid)  ) {
				//if (_isDebug) Log.d(DVGlobals.LOG_TAG, "draw: to cache");
				_renderer.setCanvas(_touchBitmapCacheBehindCanvas);
				_renderer.setupViewPort();
				_touchBitmapCacheBehindCanvas.drawColor(_bgColor);
				_renderer.render(_drawing);
				_selectionOverlay.draw(_touchBitmapCacheBehindCanvas);// RM: move out of here
				_renderer.revertViewPort();
				canvas.drawBitmap(_touchBitmapCacheBehind,0,0, _bmpPaint);
				_touchBitmapCacheBehindIsWritten=true;
				renderFrom = "to cache:" + _calledFromUpdate+":"+touchBitmapBehindValid;
			} else if (!_calledFromUpdate && touchBitmapBehindValid) {
				renderFromCache(canvas,_touchBitmapCacheBehind);
				renderFrom = "from cache - called upd:"+_calledFromUpdate;
				doLog=false;
			} else {
				renderFromCache(canvas,_touchBitmapCache);
				renderFrom = "from cache - called upd:"+_calledFromUpdate;
			}
			_renderer.setCanvas(canvas);
			_renderer.setupViewPort();
			if (_transformController.strokesTmp!=null) {
				_renderer.sr.drawStrokes( canvas,_transformController.strokesTmp);//, false, true  _viewPort.data
			}
			
			// RM: using canvas transform
//			if (_controlsOverlay._isTransforming) {
//				_renderer.sr.drawStrokes( canvas,_selectionOverlay._selection);//, false, true  _viewPort.data
//			}
			if (_currentStroke!=null) {
				_currentStroke.update(true,_renderer,UpdateFlags.ALL);
				_renderer.sr.drawStroke( canvas, _currentStroke );
				// magnifier
				_magnifier.drawScopeBg(_currentStroke, _touchBitmapCacheBehind);
			}
			_renderer.revertViewPort();
			/* * end test new rendering * */
			if (!_isDrawing) {_controlsOverlay.draw(canvas);}// && _transformController.strokesTmp==null
			if (_displayBorder) {canvas.drawRect(_viewPort.data.drawArea, _borderPaint);}
			if (_displayData) {
				canvas.drawText(_mode.modeText, 5*_density, 20*_density, _modePaint);
				canvas.drawText(_mode.opState==Mode.POINTS_SEL?_anchorOverlay.pointAnchorText:_anchorOverlay.anchorText, (5)*_density, (20+20)*_density, _modePaint);
				canvas.drawText(((int)(_viewPort.data.zoom*100))+" %", 5*_density, (40+20)*_density, _modePaint);
				canvas.drawText(_currentLayer!=null?_currentLayer.getId():"No Layer", 5*_density, (60+20)*_density, _modePaint);
				canvas.drawText(_drawing.getId(), 5*_density, (80+20)*_density, _modePaint);
			}
			if (_currentStroke!=null && _currentStroke.type==Type.TEXT_TTF && _currentStroke.currentVec!=null && _currentStroke.currentVec.size()==4) {
				PointF tl =_currentStroke.currentVec.get(3);
				PointF br =_currentStroke.currentVec.get(1);
				_useRectF.set(tl.x,tl.y,br.x,br.y);
				_renderer.convert(_useRectF);
				canvas.drawRect(_useRectF, _controlsOverlay._borderPaint);
			}
			_gradientOverlay.draw(canvas);
			_magnifier.drawScope(canvas);
			//if (_mode.opState==Mode.EDIT_TEXT) {
			//	_editTextOverlay.draw(canvas);
			//}
			if (doLog) {
				if (_isDebug) Log.d(DVGlobals.LOG_TAG, "!!---------------draw:"+renderFrom+" - time:"+(System.currentTimeMillis()-strt)+" renderObjects:"+_renderer.renderObjects.size());
			}
			_anchorOverlay.draw(canvas);
			//_ttOverlay.draw(canvas);
			_isRendering=false; 
			//logMemory("draw");
		} else {
			if (_isDebug) Log.d(DVGlobals.LOG_TAG, "!!---------------draw: wait");
			canvas.drawColor(_bgColor);
			//String text = _drawCanvas!=null?getContext().getString(R.string.drawview_nothing_loaded):getContext().getString(R.string.drawview_please_wait);
			String text = getContext().getString(R.string.drawview_please_wait);
			float waitWid = _modePaint.measureText(text, 0, text.length());
			canvas.drawText(text, (getMeasuredWidth()-waitWid)/2, (getMeasuredHeight()-_modePaint.getTextSize())/2, _modePaint);
		}
		_calledFromUpdate=false;
	}
	
	private void renderFromCache(Canvas canvas,Bitmap cacheBitmap) {
		_renderer.setCanvas(canvas);
		//_useRect.set( 0, 0, getMeasuredWidth(), getMeasuredHeight() );
		_useRect.set( 0, 0, cacheBitmap.getWidth(), cacheBitmap.getHeight() );
		float zoomMultiplier = _viewPort.data.zoom/_viewPort.data.referenceZoom;

		float newWidth = cacheBitmap.getWidth()*zoomMultiplier;
		float newHeight = cacheBitmap.getHeight()*zoomMultiplier;
		//float newWidth = getMeasuredWidth()*zoomMultiplier;
		//float newHeight = getMeasuredHeight()*zoomMultiplier;
		if (_isDebug) Log.d(DVGlobals.LOG_TAG, "draw: from cache:" +
				" topleftRef:"+PointUtil.tostr(_viewPort.data.topLeftReference)+
				" topleft:"+PointUtil.tostr(_viewPort.data.topLeft)+
				" bmpDim:"+cacheBitmap.getWidth()+"x"+cacheBitmap.getHeight());
		_usePoint.set(_viewPort.data.topLeftReference);
		PointUtil.mulVector(_usePoint, _usePoint, -_viewPort.data.referenceZoom);		
		_usePoint2.set(_viewPort.data.topLeft);
		
		PointUtil.mulVector(_usePoint2, _usePoint2, -_viewPort.data.zoom);	
		PointUtil.addVector(_usePoint, _usePoint, _usePoint2);	

		_usePoint2.set(_viewPort.data.topLeftReference);
		PointUtil.mulVector(_usePoint2, _usePoint2, 2f* _viewPort.data.zoom);	
		PointUtil.addVector(_usePoint, _usePoint, _usePoint2);	
		
		_usePoint2.set(_viewPort.data.topLeftReference);
		PointUtil.mulVector(_usePoint2, _usePoint2,  -_viewPort.data.zoom+_viewPort.data.referenceZoom);	
		PointUtil.addVector(_usePoint, _usePoint, _usePoint2);	
		
		_useRectF.set( _usePoint.x,	_usePoint.y,	(_usePoint.x+newWidth),	(_usePoint.y+newHeight)	);
		
		canvas.drawBitmap(cacheBitmap,_useRect, _useRectF,_bmpPaint);//_useRect,_useRectF, 
		//Log.d(DVGlobals.LOG_TAG, "draw: from cache: ");//"src:"+PointUtil.tostr(_useRect)+" tgt:"+PointUtil.tostr(_useRectF)
		/*
		// debugging 
		canvas.drawRect(_useRectF, _modePaint);
		_modePaint.setColor(Color.argb(255, 255, 0, 0));
		canvas.drawLine(0,0,-_viewPort.data.topLeftReference.x*_viewPort.data.referenceZoom, -_viewPort.data.topLeftReference.y*_viewPort.data.referenceZoom, _modePaint);
		_modePaint.setColor(Color.argb(255,  0,255, 0));
		canvas.drawLine(0,0,-_viewPort.data.topLeft.x*_viewPort.data.zoom, -_viewPort.data.topLeft.y*_viewPort.data.zoom, _modePaint);
		_modePaint.setColor(Color.argb(255, 0, 0, 255));
		canvas.drawLine(0,0,_usePoint.x,_usePoint.y, _modePaint);
		
		_modePaint.setColor(Color.argb(128, 64, 64, 64));
		*/
	}
	
	
	//////////////////////////////////////// update ////////////////////////////////////////////////
	/*
	public void runUpdate() {
		anchorOverlay.updateAnchor();
		controlsOverlay.updateSelectionIconRects();
		updateDisplay();
	}
	*/
	//public  void updateDisplay() {
	//	updateDisplay(true);
	//}
	
	public void updateDisplay() {//boolean saveUndo
		updateInternal();
		if (_onUpdateListener!=null) {
			_onUpdateListener.update(DrawView.this);
		}
		//if (saveUndo) {	addUndo();	}
	}
	private boolean _calledFromUpdate=false;
	public void updateInternal() {
		updateFlags();
		_calledFromUpdate=true;
		invalidate();
	}
	
	public void updateCached() {
		updateFlags();
		_calledFromUpdate=false;// should be false anyways
		invalidate();
	}
	
	
	public void updateFlags() {
		long strt=SystemClock.uptimeMillis();
		if (_onUpdateListener!=null && _updateFlags[UPDATE_UNDO]) {
			_updateTimes[UPDATE_UNDO]=SystemClock.uptimeMillis();
			//if (_updateUndoType==null) {_updateUndoType=UndoOperationType.LAYER_ALL;}
			_undoController.addUndo();//_updateUndoType
			//_updateUndoType=null;
			_updateFlags[UPDATE_UNDO]=false;
			_updateTimes[UPDATE_UNDO]=SystemClock.uptimeMillis()-_updateTimes[UPDATE_UNDO];
			_dirty=true;
			
		}
		if (_onUpdateListener!=null && _updateFlags[UPDATE_UNDO_OP]) {
			_updateTimes[UPDATE_UNDO_OP]=SystemClock.uptimeMillis();
			if (_onUpdateListener!=null) {
				_onUpdateListener.updateAfterUndo(DrawView.this);
			}
			_updateFlags[UPDATE_UNDO_OP]=false;
			_updateTimes[UPDATE_UNDO_OP]=SystemClock.uptimeMillis()-_updateTimes[UPDATE_UNDO_OP];
		}
		if (_updateFlags[UPDATE_ZOOM]) {// not used at the mo..
			if (_onUpdateListener!=null ) {
				_onUpdateListener.updateZoom(_viewPort.data.zoom);
			}
			_updateFlags[UPDATE_ZOOM]=false;
		}
		if (_updateFlags[UPDATE_ANCHOR]) {
			_updateTimes[UPDATE_ANCHOR]=SystemClock.uptimeMillis();
			_anchorOverlay.updateAnchor();
			_updateFlags[UPDATE_ANCHOR]=false;
			_updateTimes[UPDATE_ANCHOR]=SystemClock.uptimeMillis()-_updateTimes[UPDATE_ANCHOR];
		}
		if (_updateFlags[UPDATE_SCOPE]) {
			if (_drawing!=null) {
				_zoomScope.updateScopeRects();
			}
			_updateFlags[UPDATE_SCOPE]=false;
		}
		if (_updateFlags[UPDATE_CONTROLS]) {
			_updateTimes[UPDATE_CONTROLS]=SystemClock.uptimeMillis();
			_controlsOverlay.updateSelectionIconRects();
			_updateFlags[UPDATE_CONTROLS]=false;
			_updateTimes[UPDATE_CONTROLS]=SystemClock.uptimeMillis()-_updateTimes[UPDATE_CONTROLS];
			
		}
		if (_onUpdateListener!=null && _updateFlags[UPDATE_MODE]) {
			_updateTimes[UPDATE_MODE]=SystemClock.uptimeMillis();
			_onUpdateListener.updateMode(DrawView.this);
			_updateFlags[UPDATE_MODE]=false;
			_updateTimes[UPDATE_MODE]=SystemClock.uptimeMillis()-_updateTimes[UPDATE_MODE];
		}
		if (_onUpdateListener!=null && _updateFlags[UPDATE_CLIP]) {
			_updateTimes[UPDATE_CLIP]=SystemClock.uptimeMillis();
			_onUpdateListener.updateClipboard(DrawView.this);
			_updateFlags[UPDATE_CLIP]=false;
			_updateTimes[UPDATE_CLIP]=SystemClock.uptimeMillis()-_updateTimes[UPDATE_CLIP];
		}
		if (_isDebug) Log.d(DVGlobals.LOG_TAG, "++++++++++++++++++++++update: time:"+(SystemClock.uptimeMillis()-strt)+":"+" dirty:"+_dirty+
				" undoTime:"+_updateTimes[UPDATE_UNDO]+" undoOPTime:"+_updateFlags[UPDATE_UNDO_OP]+" controlsTime:"+_updateTimes[UPDATE_CONTROLS]+" updModeListenTime:"+_updateTimes[UPDATE_MODE]+" updClipListenTime:"+_updateTimes[UPDATE_CLIP]+" anchorTime:"+_updateTimes[UPDATE_ANCHOR]);
	}
	
	private String getUpdateFlags() {
		if (_useSb.length()>0) {_useSb.delete(0, _useSb.length()-1);}
		for (int i=0;i<_updateFlags.length;i++) {
			_useSb.append(i+"="+_updateFlags[i]+",");
		}
		return _useSb.toString();
	}

	public static interface OnUpdateListener {
		public void update(DrawView f);
		public void updateClipboard(DrawView f);
		public void updateAfterUndo(DrawView f);
		public void load(DrawView f);
		public void updateMode(DrawView f);
		public void itemSelected(DrawView f,DrawingElement de);
		public void updatePenAndFill(DrawView f);
		public void updateZoom(float zoom);
		
	}
	
	public OnUpdateListener getOnUpdateListener() {
		return _onUpdateListener;
	}
	public void setOnUpdatePreviewListener(OnUpdateListener onupl) {
		this._onUpdateListener = onupl;
	}
	
	public static interface DrawViewClient {
		//public File getTTFFontFile(String name);
		public void showText(DrawView f,Stroke s);
		public void showLine(DrawView f);
		public void layerChange(DrawView f);
		public void afterDraw(Stroke s);
	}
	public DrawViewClient getDrawViewClient() {
		return _drawViewClient;
	}
	public void setDrawViewClient(DrawViewClient drawViewClient) {
		this._drawViewClient = drawViewClient;
	}
	//////////////////////////////////////// view methods ////////////////////////////////////////////////
	@Override
	public boolean onEWKeyDown( int keyCode, KeyEvent event ) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:return _viewPort.panKeyPad(-10,0);
			case KeyEvent.KEYCODE_DPAD_UP:return _viewPort.panKeyPad(0,-10);
			case KeyEvent.KEYCODE_DPAD_RIGHT:return _viewPort.panKeyPad(10,0);
			case KeyEvent.KEYCODE_DPAD_DOWN:return _viewPort.panKeyPad(0,10);
			case KeyEvent.KEYCODE_DPAD_CENTER:clearFocus();return true;
			default : return false;
		}
	}
	
	public boolean onEWKeyUp( int keyCode, KeyEvent event ) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_LEFT:return _viewPort.panKeyPad(-10,0);
			case KeyEvent.KEYCODE_DPAD_UP:return _viewPort.panKeyPad(0,-10);
			case KeyEvent.KEYCODE_DPAD_RIGHT:return _viewPort.panKeyPad(10,0);
			case KeyEvent.KEYCODE_DPAD_DOWN:return _viewPort.panKeyPad(0,10);
			case KeyEvent.KEYCODE_DPAD_CENTER:clearFocus();return true;
			default : return false;
		}
	}
	@Override
	protected void onEWFocusChanged( boolean gainFocus, int direction,	Rect previouslyFocusedRect ) {
		if (gainFocus) {
			_mode.setReversibleState(Mode.PAN);
		} else {
			_mode.reverseState();
		}
	}
	@Override
	protected void onEWLayout(boolean changed, int left, int top, int right,int bottom) {
	}
	
	@Override
	protected void onEWMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = MeasureSpec.getSize(heightMeasureSpec);
		int width = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(width, height);
        Activity activity = (Activity)getContext();
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        int statusBarHeight = rect.top;
        int screenHeight = activity.getWindowManager().getDefaultDisplay().getHeight();
        if (_isDebug) Log.d(DVGlobals.LOG_TAG, "diff="+"("+screenHeight+" - "+statusBarHeight+") - "+height);
        int diff = (screenHeight - statusBarHeight) - height;
        if (_isDebug) Log.d(DVGlobals.LOG_TAG, "softKeyBdListener:"+_softKeyBoardListener+":"+diff);
        if (_softKeyBoardListener != null) {
        	_softKeyBoardListener.onAsync(diff>200); // assume all soft keyboards are at least 128 pixels high
        	
        }
        if (_isDebug) Log.d(DVGlobals.LOG_TAG, "drawview measuerheight:"+height+";"+screenHeight);
      
	}
	//////////////////////////////////////// point modification ////////////////////////////////////////////////
	private PointF snapInStroke(Stroke currStroke,final PointF touchPoint) {
		_usePoint.set(touchPoint);
		boolean xsnapped = false;
		boolean ysnapped = false;
		for (int i=0;i<currStroke.points.size();i++) {// dont check last point
			PointF testpoint = currStroke.currentVec.get(i);
			if (touchPoint==testpoint) {continue;}
			if (!xsnapped && _usePoint.x>testpoint.x-_mode.lineDrawSnapDist && _usePoint.x<testpoint.x+_mode.lineDrawSnapDist) {
				_usePoint.x=testpoint.x;
				xsnapped=true;
			}
			if (!ysnapped && _usePoint.y>testpoint.y-_mode.lineDrawSnapDist && _usePoint.y<testpoint.y+_mode.lineDrawSnapDist) {
				_usePoint.y=testpoint.y;
				ysnapped=true;
			}
			if (xsnapped && ysnapped) {break;}
		}
		return _usePoint;
	}
	////////////////////////////////////////shape generation ////////////////////////////////////////////////
	
	public void makeCircleTemplate(Stroke s) {
		s.type=Stroke.Type.LINE;
		PointVec curVec = s.currentVec;
		curVec.clear();
		curVec.closed=true;
		StrokeUtil.genCircle(curVec);
		s.update(false, _renderer, UpdateFlags.ALL);
	}
	
	
	public void makeCircle(Stroke s,RectF r) {
		//this can be pre computed and just scaled and translated as nessecary
		makeShape(s,r,_circleTemplate);
	}
	
	private void makeShapeTemplate(Stroke shapeTemplate) {
		StrokeUtil.generatePoly(shapeTemplate.currentVec, 5);
		alignTime(_shapeTemplate.currentVec);
		shapeTemplate.update(false, _renderer, UpdateFlags.ALL);
	}
	
	public void setShapeTemplate(Stroke s) {
		_shapeTemplate.currentVec.clear();
		_shapeTemplate.currentVec.closed = s.currentVec.closed;
		for (PointF p : s.currentVec) {
			_shapeTemplate.currentVec.add(new PathData(p.x,p.y));
		}
		alignTime(_shapeTemplate.currentVec);
		_shapeTemplate.update(false, _renderer, UpdateFlags.ALL);
	}
	
	public void makeShape(Stroke s,RectF r) {
		makeShape(s,r,_shapeTemplate);
	}
	
	public void makeShape(Stroke s,RectF r,Stroke template) {
		PointVec curVec = s.currentVec;
		curVec.clear();
		PointVec shapePV = template.points.get(0);
		curVec.closed=shapePV.closed;
		for (int i=0;i<shapePV.size();i++) {
			PathData p= new PathData();
			p.set(shapePV.get(i));
			curVec.add(p);
		}
		StrokeUtil.fitStroke(curVec,r);
		alignTime(curVec);
	}
	
	public void makeRect(Stroke s,RectF r) {
		s.type=Stroke.Type.LINE;
		PointVec curVec = s.currentVec;
		curVec.clear();
		curVec.closed=true;
		curVec.add(new PathData(r.left,r.top));
		curVec.add(new PathData(r.right,r.top));
		curVec.add(new PathData(r.right,r.bottom));
		curVec.add(new PathData(r.left,r.bottom));
		alignTime(curVec);
	}
	
	public void makeTextRec(Stroke s,RectF r) {
		s.type=Stroke.Type.TEXT_TTF;
		PointVec curVec = s.currentVec;
		curVec.clear();
		curVec.closed=true;
		float left = Math.min(r.left, r.right);
		float right = Math.max(r.left, r.right);
		float top = Math.min(r.top, r.bottom);
		float bottom = Math.max(r.top, r.bottom);
		curVec.add(new PathData(left,bottom));
		curVec.add(new PathData(right,bottom));
		curVec.add(new PathData(right,top));
		curVec.add(new PathData(left,top));
		alignTime(curVec);
	}
	
	private void alignTime(PointVec pv) {
		int timeTaken = 1000;
		//if (pv.time==null) {return;}
		//if (pv.time.size()>0) {timeTaken=pv.time.get(pv.time.size()-1);}
		/*
		for (int i=pv.time.size()-1;i>-1;i--) {
			if (pv.time.get(i)<timeTaken) {
				timeTaken=pv.time.get(i);
			}
		}
		*/
		//pv.time.clear();
		//pv.pressure.clear();
		for (int i=pv.size()-1;i>-1;i--) {
			PathData p = pv.get(i);
			p.timeDelta=timeTaken*i/pv.size();
			p.pressure=0.5f;
		}
	}  
	
	public void finishDrawLine() {
		if (_mode.drawMode==Draw.LINE && _currentStroke!=null) {
			//currStroke.select(true);
			dropTouchCache();
			PointVec curVec = _currentStroke.currentVec;
			if (PointUtil.dist(curVec.get(0),curVec.get(curVec.size()-1))<10) {
				curVec.closed=true;
			}
			if (!getCurrentLayer().contains(_currentStroke)) {
				getCurrentLayer().add(_currentStroke);
			}
			
			//_currentStroke.update(false,0,_viewPort.data);
			_currentStroke.update(false, _renderer, UpdateFlags.ALL);
			if (_drawViewClient!=null) {
				_drawViewClient.afterDraw(_currentStroke);
			}
			((AddDEUndoElement)_undoController.initNextUndo(UndoOperationType.ADD_ELEMENT))._addedElement=_currentStroke;;
			//((AddDEUndoElement)_undoController.getNextUndoElement())._addedElement=_currentStroke;
			_currentStroke = null;
			_updateFlags[UPDATE_ANCHOR]=true;
			_updateFlags[UPDATE_SCOPE]=true;
			_updateFlags[UPDATE_CONTROLS]=true;
			_updateFlags[UPDATE_UNDO]=true;
			updateDisplay();
		}
	}
	
	public void cancelDrawInternal() {
		if (_currentStroke!=null) {
			_currentStroke = null;
		}
	}
	
	public void cancelDraw() {
		if (_currentStroke!=null) {
			_currentStroke = null;
			updateDisplay();
		}
	}
	//////////////////////////////////////// text handling ////////////////////////////////////////////////
	private String originalText = null;
	public void cancelText() {
		if (_mode.opState==Mode.EDIT_TEXT) {
			cancelTextInternal();
			_mode.setState(_mode.lastState);
		}
	}
	public void cancelTextInternal() {
		if (_mode.opState==Mode.EDIT_TEXT ) {
			_currentStroke=null;
			if (originalText!=null && _selectionOverlay.getStrokeSelection().size()==1){
				Stroke stroke = _selectionOverlay.getStrokeSelection().get(0);
				if (stroke.type==Type.TEXT_TTF) {
					stroke.text=originalText;
					stroke.update(false, _renderer, UpdateFlags.ALL);
				}
			}
		}
		originalText=null;
	}
	public void finishText(String text) {
		updateText(  text,  null ,true);
		
	}
	
	public void updateText( String text, String fontName ) {
		updateText(  text,  fontName ,false);
	}
	public void updateTextHeight( float height ) {
		_currentFontHeight = height;
		if (_currentStroke!=null) {
			updateText(  _currentStroke.text,  null ,false);
		} else {
			Stroke s = _selectionOverlay.getStrokeSelection().get(0);
			if (s!=null) {
				updateText(  s.text,  null ,false);
			}
		}
	}
	public void updateText( String text, String fontName ,boolean finish) {
		if (_mode.opState==Mode.EDIT_TEXT ) {
			if (_currentStroke!=null) {
				Stroke addStroke = _currentStroke;
				if (finish) {_currentStroke=null;}
				addStroke.text = text;
				if (fontName!=null) {
					addStroke.fontName = fontName;
				} else {
					addStroke.fontName = this._currentTTFontName;
				}
				checkModifyTextHeight(addStroke);
				addStroke.update(false, _renderer, UpdateFlags.ALL);
				if (finish) {
					if (!getCurrentLayer().contains(addStroke)) {
						getCurrentLayer().add(addStroke);
					}
					_mode.setStateInternal(_mode.lastState,true);
				}
				if (finish) {
					((AddDEUndoElement)_undoController.initNextUndo(UndoOperationType.ADD_ELEMENT))._addedElement=addStroke;;
					//((AddDEUndoElement)_undoController.getNextUndoElement())._addedElement=addStroke;
					_updateFlags[UPDATE_ANCHOR]=true;
					_updateFlags[UPDATE_SCOPE]=true;
					_updateFlags[UPDATE_CONTROLS]=true;
					_updateFlags[UPDATE_UNDO]=true;
					if (_drawViewClient!=null) {
						_drawViewClient.afterDraw(addStroke);
					}
				}
				if (testViewPort(addStroke)) {
					_viewPort.setViewPortByElement(addStroke,(int)(120*_density));
				} else {
					//dropTouchCache(); 
					updateDisplay();
				}
			} else if (_selectionOverlay.getStrokeSelection().size()==1 ) {
				Stroke s = _selectionOverlay.getStrokeSelection().get(0);
				s.text = text;
				if (fontName!=null) {
					s.fontName=fontName;
				}
				checkModifyTextHeight(s);
				s.update(false, _renderer, UpdateFlags.ALL);
				if (finish) {
					_mode.setStateInternal(_mode.lastState,true);
				}
				if (testViewPort(s)) {
					_viewPort.setViewPortByElement(s,(int)(120*_density));
				} else {
					//dropTouchCache(); 
					updateDisplay();
				}
			}
		}
	}
	public void checkModifyTextHeight(Stroke addStroke) {
		int count = 1;
		for (int i=0;i<addStroke.text.length()-1;i++) {if (addStroke.text.charAt(i)=='\n') {count++;}}
		if (addStroke.calculatedBounds.height()!=count*_currentFontHeight) {//count*
			PointVec pointVec = addStroke.points.get(0);
			pointVec.get(0).set(pointVec.get(3).x,pointVec.get(3).y+(int)(count*_currentFontHeight));
			pointVec.get(1).set(pointVec.get(2).x,pointVec.get(2).y+(int)(count*_currentFontHeight));
		}
	}
	private boolean testViewPort(Stroke addStroke) { return testViewPort( addStroke, true) ;}
	private boolean testViewPort(Stroke addStroke,boolean useMinHeight) {
		Log.d(DVGlobals.LOG_TAG, "testViewPort: "+PointUtil.tostr(_viewPort.data.drawArea)+"===>"+addStroke.calculatedBounds);
		return !_viewPort.data.zoomSrcRectF.contains(addStroke.calculatedBounds);// || (useMinHeight&&addStroke.calculatedBounds.height()<50);
	}
	
	public void editText() {
		if (_selectionOverlay.getStrokeSelection().size()==1) {
			Stroke stroke = _selectionOverlay.getStrokeSelection().get(0);
			if (stroke.type==Type.TEXT_TTF) {
				_currentStroke=null;
				_currentFontHeight=stroke.calculatedBounds.height();
				originalText=stroke.text;
				_mode.setState(Mode.EDIT_TEXT);
				if (_drawViewClient!=null) {
					_drawViewClient.showText(this, stroke);
				}
				if (testViewPort(stroke)) {
					_viewPort.setViewPortByElement(stroke,(int)(120*_density));
				} else {
					//dropTouchCache(); 
					updateDisplay();
				}
				//updateDisplay();
			}
		}
	}
	//////////////////////////////////////// reducing ////////////////////////////////////////////////
	public void reducePointsAngle(ArrayList<Stroke> strokes,float thresholdAngle) {
		//TODO add undo if last was delta
		_undoController.addSnapshotIfLastWasDelta();
		for (Stroke s:strokes) {
			reducePointsAngleInternal( s, thresholdAngle);
		}
		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_SCOPE]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		_updateFlags[UPDATE_UNDO]=true;
		updateDisplay();
	}
	
	public int reducePointsAngle(Stroke stroke,float thresholdAngle){
		//TODO add undo if last was delta
		_undoController.addSnapshotIfLastWasDelta();
		int ret = reducePointsAngleInternal( stroke, thresholdAngle);
		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_SCOPE]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		_updateFlags[UPDATE_UNDO]=true;
		updateDisplay();
		return ret;
	}
	
	// reduce the number of points in a stroke by thresholding the angle made up by each set of 3 points
	private int reducePointsAngleInternal(Stroke stroke,float thresholdAngle) {//thresholdAngle in degrees
		thresholdAngle = (float)(thresholdAngle*Math.PI/180f);
		int pointsRemoved = 0;
		ArrayList<PointVec> segments= stroke.points; 
		int strokeCtr = 0;
		for (int j=0;j<segments.size();j++) {
			ArrayList<PathData> segment = segments.get(j);
			for (int i=1;i<segment.size()-1;i++) {
				float angle = PointUtil.calcAngle(segment.get(i-1),segment.get(i),segment.get(i+1));
				while (Math.abs(angle)<thresholdAngle) {
					segment.remove(i);
					pointsRemoved++;
					if (i>segment.size()-2) {break;}
					angle = PointUtil.calcAngle(segment.get(i-1),segment.get(i),segment.get(i+1));
				}
			}
			strokeCtr+=segment.size()+1;
		}
		stroke.update(false, _renderer, UpdateFlags.ALL);
		return pointsRemoved;
	}
	
	public void reducePointsDist(ArrayList<Stroke> strokes,float thresholdDist) {
		_undoController.addSnapshotIfLastWasDelta();
		for (Stroke s:strokes) {
			reducePointsDistInternal( s, thresholdDist);
		}
		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_SCOPE]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		_updateFlags[UPDATE_UNDO]=true;
		updateDisplay();
	}
	public void reducePointsDist(Stroke stroke,float thresholdDist) {
		_undoController.addSnapshotIfLastWasDelta();
		reducePointsDistInternal( stroke, thresholdDist);
		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_SCOPE]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		_updateFlags[UPDATE_UNDO]=true;
		updateDisplay();
	}
	
	private void reducePointsDistInternal(Stroke stroke,float thresholdDist) {//thresholdAngle in degrees
		//if (stroke.type==DRAWTYPE_FREELINE || stroke.type==DRAWTYPE_LINE) {
			ArrayList<PointVec> segments= stroke.points;
			int strokeCtr = 0;
			for (int j=0;j<segments.size();j++) {
				ArrayList<PathData> segment = segments.get(j);
				for (int i=0;i<segment.size()-1;i++) {
					float dist = PointUtil.dist(segment.get(i),segment.get(i+1));
					while (dist<thresholdDist && i>0) {
						segment.remove(i);
						//stroke.points.remove(strokeCtr+i);
						if (i>segment.size()-2) {break;}
						dist = PointUtil.dist(segment.get(i),segment.get(i+1));
					}
				}
				strokeCtr+=segment.size()+1;
			}
			//stroke.update(false,0,_viewPort.data);
			stroke.update(false, _renderer, UpdateFlags.ALL);
		//}
	}
	
	////////////////////////////////////////smoothing ////////////////////////////////////////////////
	public void smoothPoints(ArrayList<Stroke> strokes,float factor,ModeData.Smooth type) {
		_undoController.addSnapshotIfLastWasDelta();
		for (Stroke s:strokes) {
			smoothPointsInternal( s, factor, type);
		}
		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_SCOPE]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		_updateFlags[UPDATE_UNDO]=true;
		updateDisplay();
	}
	public void smoothPoints(Stroke stroke,float factor,ModeData.Smooth type) {
		_undoController.addSnapshotIfLastWasDelta();
		smoothPointsInternal( stroke, factor, type);
		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_SCOPE]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		_updateFlags[UPDATE_UNDO]=true;
		updateDisplay();
	}
	
	private void smoothPointsInternal(Stroke stroke,float factor,ModeData.Smooth type) {
		int width = 3;
		if (type==ModeData.Smooth.MEDIAN||type==ModeData.Smooth.MEAN) {
			width=(int)factor;
		}
		ArrayList<PointVec> segments = stroke.points;//split();
		int strokeCtr = 0;
		for (int k=0;k<segments.size();k++) {
			PointVec segment = segments.get(k);
			//Log.d(Globals.TAG, "smoothPoints: seg:"+segment.size());
			// TODO change to arraylist and test
			Vector<PathData> newpoints = new Vector<PathData>();
			int st = -width/2;int end = width/2;
			//Log.d(Globals.TAG, "smoothPoints:width:"+width+": st:"+st+": end:"+end);
			float[] xarr = new float[width];
			float[] yarr = new float[width];
			//float[] tarr = new float[width];
			//float[] parr = new float[width];
			int startIndex=0; int endIndex = stroke.points.size();
			if (!segment.closed) {
				startIndex=1;endIndex = segment.size()-1;//-2  ?
				newpoints.add(segment.get(0));
			}
			//Log.d(Globals.TAG, "smoothPoints:closed:"+stroke.closed+": sti:"+startIndex+": endi:"+endIndex);
			
			//StringBuffer s = new StringBuffer();
			for (int i=startIndex;i<endIndex;i++) {
				int ctr = 0;
				Integer tdCopy = null;
				Float pCopy = null;
				//s.delete(0, s.length());
				for (int j=st;j<=end;j++) {
					int thisPoint = i+j;
					if (!segment.closed) {
						if (thisPoint<0) continue;
						else if (thisPoint>segment.size()-1) continue;
					} else {
						if (thisPoint<0) thisPoint+=segment.size();
						else if (thisPoint>segment.size()-1) thisPoint-=segment.size();
					}
					PathData p = segment.get(thisPoint);
					xarr[ctr]=p.x;
					yarr[ctr]=p.y;
					tdCopy=p.timeDelta;
					pCopy=p.pressure;
					//tarr[ctr]=p.timeDelta;
					//parr[ctr]=p.pressure;
					//s.append("indexs:"+thisPoint+": thisPoint:"+xarr[ctr]+":"+yarr[ctr]+" _ ");
					ctr++;
				}
				//Log.d(Globals.TAG,"smooth: type"+type+": ctr"+ctr+":"+s.toString());
				PathData addedPD = null;
				if (type==ModeData.Smooth.MEDIAN) {
					if (ctr==3) {//??
						addedPD = new PathData(PointUtil.getMedian(xarr,ctr),PointUtil.getMedian(yarr,ctr));
						newpoints.add(addedPD);
					}
					else {newpoints.add(segment.get(i));}// cant do median if there arent 3 points;
				} else if (type==ModeData.Smooth.MEAN) {
					addedPD = new PathData(PointUtil.getMean(xarr,ctr),PointUtil.getMean(yarr,ctr));
					newpoints.add(addedPD);
				} else if (type==ModeData.Smooth.MIDPOINT) {
					addedPD = new PathData(PointUtil.getMidpoint(xarr,factor),PointUtil.getMidpoint(yarr,factor));
					newpoints.add(addedPD);
				}
				if (addedPD!=null) {
					addedPD.timeDelta=tdCopy;
					addedPD.pressure=pCopy;
				}
				//Log.d(Globals.TAG,"smooth:result:"+tostr(newpoints.get(newpoints.size()-1)));
			}
			if (!segment.closed) {newpoints.add(segment.get(segment.size()-1));}
			for (int i=0;i<newpoints.size();i++) {
				PathData pathData = newpoints.get(i);
				if (stroke.points.get(k).size()>strokeCtr+i) {
					stroke.points.get(k).set( strokeCtr+i, pathData );
				} else {
					stroke.points.get(k).add( pathData );
				}
			}
			strokeCtr+=segment.size()+1;
		}
		//stroke.update(false,0,_viewPort.data);
		stroke.update(false, _renderer, UpdateFlags.ALL);
	}
	
	/*
	public int addPoints() {
		if (pointsEditStroke!=null && pointsSelection.size()>0) {
			return addPoints( pointsEditStroke, new Vector<Integer>(pointsSelection));
		}
		updateDisplay();
		return 0;
	}
	*/
	public int addPoints(ArrayList<Stroke> strokes) {
		_undoController.addSnapshotIfLastWasDelta();
		int cnt = 0;
		for (Stroke s:strokes) {
			cnt+=addPointsInternal(s);
		}
		
		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_SCOPE]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		_updateFlags[UPDATE_UNDO]=true;
		updateDisplay();
		return cnt;
		
	}
	public int addPoints(Stroke s) {
		_undoController.addSnapshotIfLastWasDelta();
		int cnt = 0;
		cnt+=addPointsInternal(s);
		
		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_SCOPE]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		_updateFlags[UPDATE_UNDO]=true;
		updateDisplay();
		return cnt;
		
	}
	public int addPointsInternal(Stroke s) {
		int cnt = 0;
		for (int i=0;i<s.points.size();i++) {
			PointVec points = s.points.get(i);
			if (points.size()>MAX_ADDPOINTS) {continue;}// limit the number of points 
			//Vector<Integer> pts = new Vector<Integer>();
			//for (int j=0;j<s.points.size();j++) {if (s.points.get(j).x!=StrokeAPI.POINT_BREAK) pts.add(j);}
			cnt += addPointsInternal( points );//, s.closed && s.points.size()==1 //, pts
		}
		
		//s.update(false,0,_viewPort.data);
		s.update(false, _renderer, UpdateFlags.ALL);
		return cnt;
	}
	
	private int addPointsInternal(PointVec points) {// ,Vector<Integer> usePointsSel// insert points between two adjacent selected points
		int pointsAdded = 0;
		//Stroke s = getSelection().get(0);
		//Vector<Integer> usePointsSel= new Vector<Integer>(pointsSelection);
		//Collections.sort(usePointsSel);
		//boolean containsLast = usePointsSel.get(usePointsSel.size()-1)==points.size()-1;
		for (int i=points.size()-2;i>=0;i--) {
			//Integer idx = points.get(i);
			//Integer nextIdx = points.get(i+1);
			//if (nextIdx==idx+1) {
				PathData p1 = points.get(i);//idx
				PathData p2 = points.get(i+1);//nextIdx
				//if (p1.x==StrokeAPI.POINT_BREAK|| p2.x==StrokeAPI.POINT_BREAK) {continue;}
				PathData pathData = new PathData(PointUtil.midpoint(p1, p2));
				if (p1.timeDelta!=null) {
					pathData.timeDelta = p1.timeDelta+(p2.timeDelta-p1.timeDelta)/2;
				}
				if (p1.pressure!=null) {
					pathData.pressure = p1.pressure+(p2.pressure-p1.pressure)/2;
				}
				points.add(i+1,pathData );//nextIdx
				pointsAdded++;
			//}
		}
		// check for first last select and add point if nessecary.
		// not done for combined strokes 
		//Log.d(Globals.LOG_TAG,s.closed+":"+usePointsSel.get(0)+":"+usePointsSel.get(usePointsSel.size()-1));
		if (points.closed ) {
			PathData p1 = points.get(0);//idx
			PathData p2 = points.get(points.size()-1);//nextIdx
			PathData pathData = new PathData(PointUtil.midpoint(p1, p1));
			if (p2.timeDelta!=null) {
				int diff = 0;
				if (points.size()>1) {
					PathData p3= points.get(points.size()-3);
					if (p3.timeDelta!=null) {
						pathData.timeDelta = p2.timeDelta+(p2.timeDelta-p3.timeDelta);
					}
				}
				
			}
			if (p1.pressure!=null) {
				pathData.pressure = p1.pressure+(p2.pressure-p1.pressure)/2;
			}
			points.add(points.size(),pathData);
			if (_isDebug) Log.d(DVGlobals.LOG_TAG,"insert closed");
			pointsAdded++;
		}
		return pointsAdded;
	}
	////////////////////////////////////////combine ////////////////////////////////////////////////
	public static final int COMBINE_FLAG_NOBREAKS = 1;
	public static final int COMBINE_FLAG_REORDER = 2;
	
	public void combineSelection(int flags) {
		_undoController.addSnapshotIfLastWasDelta();
		ArrayList<Stroke> processVector = new ArrayList<Stroke>();
		for (int i=0;i<_selectionOverlay.getSelectionCount();i++) {
			DrawingElement de = (DrawingElement) _selectionOverlay._selection.get(i);
			if (de instanceof Stroke && ((Stroke)de).type!=Type.TEXT_TTF ) {
				processVector.add((Stroke)de);
			}
		}
		Stroke s = combineStrokes(processVector,flags);
		if ( s != null ){
			_selectionOverlay._selection.removeAll(processVector);
			_selectionOverlay._selection.add(s);
			_updateFlags[UPDATE_ANCHOR]=true;
			_updateFlags[UPDATE_SCOPE]=true;
			_updateFlags[UPDATE_UNDO]=true;
			updateDisplay();
		}
	}
	
	private Stroke combineStrokes(ArrayList<Stroke> strokes, int flags){
		boolean noBreaks = (flags & COMBINE_FLAG_NOBREAKS) == COMBINE_FLAG_NOBREAKS;
		boolean reorder = (flags & COMBINE_FLAG_REORDER) == COMBINE_FLAG_REORDER;
		if (strokes==null || strokes.size()<((noBreaks|reorder)?1:2)) {
			return null;
		}
		if (reorder) {
			return reorderCombineVector(strokes);
		}
		if (_isDebug) Log.d(DVGlobals.LOG_TAG,"combine:nobreaks"+noBreaks);
		Stroke mainStroke = strokes.get(0);
		if (noBreaks) {
			PointVec first = mainStroke.points.get(0);
			for (int i=1;i<mainStroke.points.size();i++) {
				PointVec p = mainStroke.points.get(i);
				first.addAll(p);
			}
		}
		if (strokes.size()>1) {
			for (int i=1;i<strokes.size();i++) {
				Stroke addStroke = strokes.get(i);
				for (int j=0;j<addStroke.points.size();j++) {
					PointVec pv = addStroke.points.get(j);
					if (noBreaks) {
						mainStroke.points.get(0).addAll(pv);
					} else {
						pv.isHole=true;
						mainStroke.points.add(pv);
					}
				}
				if (i>0) {this.getCurrentLayer().remove(addStroke);}
			}
			//mainStroke.update(false,0,_viewPort.data);
			mainStroke.update(false, _renderer, UpdateFlags.ALL);
		}
		return mainStroke;
	}
	
	private class StrokeEnds{
		PointF start;
		PointF end;
		PointVec pv;
		Stroke s;
	}
	
	private Stroke reorderCombineVector(ArrayList<Stroke> strokes){
		ArrayList<StrokeEnds> strokeEnds = new ArrayList<DrawView.StrokeEnds>();
		for (int i=0; i<strokes.size(); i++) {
			Stroke stroke = strokes.get(i);
			for (PointVec pv : stroke.points) {
				if (pv.closed) {continue;}
				StrokeEnds se=new StrokeEnds();
				se.start=pv.get(0);
				se.end=pv.get(pv.size()-1);
				se.pv=pv;
				se.s=stroke;
				strokeEnds.add(se); 
			}
		}
		//Stroke mainStroke = strokes.get(0);
		//PointVec mainPointVec = mainStroke.points.get(0);
		
		//for (StrokeEnds se : strokeEnds) {if (se.pv==mainPointVec) {foundChange=se;break;}}
		//strokeEnds.remove(foundChange);
		while (strokeEnds.size()>1) {
			float minDist = -1;
			StrokeEnds found = null;
			boolean changeStart = true;
			boolean foundStart = true;
			StrokeEnds changeFound = null;
			for (StrokeEnds seTest : strokeEnds) {
				//changeFound = null;
				//found = null;
				for (StrokeEnds se : strokeEnds) {
					if (se==seTest) continue;
					float dist = PointUtil.dist(seTest.pv.get(0),se.pv.get(0));
					if (minDist==-1) {minDist=dist;found=se;changeFound=seTest;changeStart=true;foundStart = true;}// initialiser
					if (minDist>dist) {minDist=dist;changeFound=seTest;found=se;changeStart=true;foundStart = true;}
					dist = PointUtil.dist(seTest.pv.get(seTest.pv.size()-1),se.pv.get(0));
					if (minDist>dist) {minDist=dist;changeFound=seTest;found=se;changeStart=false;foundStart = true;}
					dist = PointUtil.dist(seTest.pv.get(seTest.pv.size()-1),se.pv.get(se.pv.size()-1));
					if (minDist>dist) {minDist=dist;changeFound=seTest;found=se;changeStart=false;foundStart = false;}
					dist = PointUtil.dist(seTest.pv.get(0),se.pv.get(se.pv.size()-1));
					if (minDist>dist) {minDist=dist;changeFound=seTest;found=se;changeStart=true;foundStart = false;}
				}
			}
			found.s.points.remove(found.pv);
			if (found.s.points.size()==0) {getCurrentLayer().remove(found.s);}
			if ((changeStart && foundStart) || (!changeStart && !foundStart)) {found.pv.reverse();}
			if ((changeStart && !foundStart) || (changeStart && foundStart)) {
				changeFound.pv.addAll(0,found.pv);
			} else {
				changeFound.pv.addAll(found.pv);
			}
			changeFound.start=changeFound.pv.get(0);
			changeFound.end=changeFound.pv.get(changeFound.pv.size()-1);
			strokeEnds.remove(found);
		}
		Stroke mainStroke = strokeEnds.get(0).s;
		//mainStroke.update(false,0,_viewPort.data);
		mainStroke.update(false, _renderer, UpdateFlags.ALL);
		return mainStroke;
	}
	
	
	//////////////////////////////////////// split ////////////////////////////////////////////////
	public void split(Stroke s) {
		_undoController.addSnapshotIfLastWasDelta();
		if (splitInternal(s)) {
			_updateFlags[UPDATE_ANCHOR]=true;
			_updateFlags[UPDATE_SCOPE]=true;
			_updateFlags[UPDATE_UNDO]=true;
			_updateFlags[UPDATE_CONTROLS]=true;
			updateDisplay();
		}
	}
	
	public boolean splitInternal(Stroke s) {
		if (s.type==Type.TEXT_TTF) {
			return false;
		}
		Vector<PointVec> remove = new Vector<PointVec>();
		for (int i=1; i < s.points.size(); i++) { 
			Stroke s1 = new Stroke(s.pen,s.fill);
			s1.currentVec = s.points.get(i);
			s1.points.set(0, s1.currentVec);
			remove.add(s1.currentVec);
			getCurrentLayer().add(s1);
			//s1.update(false,0,_viewPort.data);
			s1.update(false, _renderer, UpdateFlags.ALL);
		}
		s.points.removeAll(remove);
		//s.update(false,0,_viewPort.data);
		s.update(false, _renderer, UpdateFlags.ALL);
		return true;
	}
	
    //////////////////////////////////////// Bitwise ops Elements ////////////////////////////////////////////////
	public enum Bitwise {UNION,DIFF,INTERSECTION,XOR}
	public void bitwise( Bitwise b ) {
		_undoController.addSnapshotIfLastWasDelta();
		ArrayList<Stroke> strokeSelection = _selectionOverlay.getStrokeSelection();
		int ctr=0;
		while (ctr<strokeSelection.size()) {
			Stroke s = strokeSelection.get(ctr);
			if (s.type==Type.TEXT_TTF) {
				strokeSelection.remove(s);
			} else {
				ctr++;
			}
		}
		if (strokeSelection.size()>1) {
			Stroke s = strokeSelection.remove(0);
			//Stroke s1 = strokeSelection.get(1);
			Stroke out = bitwiseInternal(s,strokeSelection,  b);
			if (out!=null) {
				int sindex = _selectionOverlay._selection.indexOf(s);
				_selectionOverlay._selection.remove(s);
				_selectionOverlay._selection.removeAll(strokeSelection);
				_selectionOverlay._selection.add(sindex,out);
				int dindex = getCurrentLayer().indexOf(s);
				if (getCurrentLayer().size()>0) {
					getCurrentLayer().add(dindex,out);
				} else {
					getCurrentLayer().add(out);
				}
				getCurrentLayer().remove(s);
				getCurrentLayer().removeAll(strokeSelection);
				_updateFlags[UPDATE_UNDO]=true;
			} else {
				Toast.makeText(getContext(), "Problem taking "+b+" - sorry.", 500).show();
			}
			_updateFlags[UPDATE_ANCHOR]=true;
			_updateFlags[UPDATE_SCOPE]=true;
			_updateFlags[UPDATE_CONTROLS]=true;
			updateDisplay();
		}
	}
	private Stroke bitwiseInternal(Stroke s,ArrayList<Stroke> rest ,  Bitwise b) {
			Stroke out=null;
			try {
				Poly outpoly = DrawingPolyConvert.toPoly(s);
				for  (Stroke s1:rest) {
					PolyDefault p1 = DrawingPolyConvert.toPoly(s1);
					if (_isDebug) Log.d(DVGlobals.LOG_TAG, "bitwiseInternal:"+p1.getNumPoints()+":"+outpoly.getNumPoints());
					switch (b) {
						case UNION:
							outpoly=p1.union(outpoly);
							break;
						case DIFF:
							outpoly=p1.diff(outpoly);
							break;
						case INTERSECTION:
							outpoly=p1.intersection(outpoly);
							break;
						case XOR:
							outpoly=p1.xor(outpoly);
							break;
					}
					//PolyDefault tmpoutpoly = new PolyDefault();
					//tmpoutpoly.add(outpoly);
					//outpoly=tmpoutpoly;
				}
				out = new Stroke(s.pen,s.fill);
				DrawingPolyConvert.toStroke(outpoly, out);
			} catch (Exception e) {
				Log.d(DVGlobals.LOG_TAG, "problem taking "+b,e);
			}catch (Error e) {
				Log.d(DVGlobals.LOG_TAG, "problem taking "+b,e);
			}
			//out.update(true,0,_viewPort.data);
			if (out!=null) {
				out.update(false, _renderer, UpdateFlags.ALL);
				return out;
			} else {
				return null;
			}
	}
	
	public void outline (Stroke s) {
		if (s!=null) {
			_undoController.addSnapshotIfLastWasDelta();
			/*
			SegmentGraph seg = new SegmentGraph();
			seg.construct(s.points.get(0));
			seg.findIntersections();
			seg.traceOutline();
			*/
			SegmentGraph seg = new SegmentGraph();
			seg.construct(s.points.get(0));
			seg.removeIntersections();
			seg.traceOutlineTurtle(s.points.get(0));
			//seg.extract(s.points.get(0));
			//s.update(true,0,_viewPort.data);
			s.update(false, _renderer, UpdateFlags.ALL);
			_updateFlags[UPDATE_ANCHOR]=true;
			_updateFlags[UPDATE_SCOPE]=true;
			_updateFlags[UPDATE_UNDO]=true;
			_updateFlags[UPDATE_CONTROLS]=true;
			//seg.removeIntersections();
			//_outOverlay._seg=seg;
			updateDisplay();
		} else {
			//_outOverlay._seg=null;
		}
		updateDisplay();
	}
	////////////////////////////////////////layers ////////////////////////////////////////////////
	public ArrayList<DrawingElement> getCurrentLayer() {
		ArrayList<DrawingElement> els =  null;
		if (_currentLayer!=null) {
			els = _currentLayer.elements;
		} else {
			els = _drawing.elements;
		}
		return els;
	}
	
	public int getCurrentLayerIdx() {
		return _drawing.layers.indexOf(_currentLayer);
	}
	public ArrayList<DrawingElement> getLayerForIdx(int idx) {
		return idx==-1?_drawing.elements:_drawing.layers.get(idx).elements;
	}
	public void setCurrentLayer(String id) {
		int index=-1;
		if (id!=null) {
			//index=_drawing.layers.indexOf(id);
			for (int i=0;i<_drawing.layers.size();i++) {
				Layer l = _drawing.layers.get(i);
				if (l.getId().equals(id)) {
					index=i;
					break;
				}
			}
		}
		setCurrentLayer(index);
	}
	
	public void setCurrentLayer(int which) {
		setCurrentLayerInternal(which);
		//_updateUndoType=UndoOperationType.LAYER_CHANGE;
		//_updateFlags[UPDATE_UNDO]=true;
		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		updateDisplay();
	}
	
	public void setCurrentLayerInternal(int which) {
		//DebugUtil.logCall("setCurrentLayerInternal:"+which, new Exception());
		if (_drawing!=null && which<_drawing.layers.size() && which>-1) {
			_currentLayer=_drawing.layers.get(which);
		} else {
			_currentLayer=null;
		}
		_selectionOverlay.clear();
		if (_drawViewClient!=null) {
			_drawViewClient.layerChange(this);
		}
	}
	
	public void addLayer(Layer l) {
		_drawing.layers.add(l);
		setCurrentLayer(_drawing.layers.size()-1);
		//_updateUndoType=UndoOperationType.ALL;
		_undoController.initNextUndo(UndoOperationType.ALL);
		_updateFlags[UPDATE_UNDO]=true;
		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		updateDisplay();
	}
	
	public void deleteLayer(Layer l) {
		if (l!=null) {
			_drawing.layers.remove(l);
			if (_currentLayer==l) {_currentLayer=null;}
			//_updateUndoType=UndoOperationType.ALL;
			_undoController.initNextUndo(UndoOperationType.ALL);
			_updateFlags[UPDATE_UNDO]=true;
			_updateFlags[UPDATE_ANCHOR]=true;
			_updateFlags[UPDATE_CONTROLS]=true;
			updateDisplay();
		}
	}
	
	public void raiseLayer(Layer l) {
		if (l!=null) {
			int i = _drawing.layers.indexOf(l);
			if (i>-1 && i<_drawing.layers.size()-1) {
				_drawing.layers.remove(i);
				_drawing.layers.add(i+1,l);
			}
			//_updateUndoType=UndoOperationType.ALL;
			_undoController.initNextUndo(UndoOperationType.ALL);
			_updateFlags[UPDATE_UNDO]=true;
			_updateFlags[UPDATE_ANCHOR]=true;
			_updateFlags[UPDATE_CONTROLS]=true;
			updateDisplay();
		}
	}
	
	public void lowerLayer(Layer l) {
		if (l!=null) {
			int i = _drawing.layers.indexOf(l);
			if (i>0) {
				_drawing.layers.remove(i);
				_drawing.layers.add(i,l);
			}
			//_updateUndoType=UndoOperationType.ALL;
			_undoController.initNextUndo(UndoOperationType.ALL);
			_updateFlags[UPDATE_UNDO]=true;
			_updateFlags[UPDATE_ANCHOR]=true;
			_updateFlags[UPDATE_CONTROLS]=true;
			updateDisplay();
		}
	}
	
	public void toggleVisibleLayer(Layer l) {
		if (l!=null) {
			l.visible=!l.visible;
			_updateFlags[UPDATE_ANCHOR]=true;
			_updateFlags[UPDATE_CONTROLS]=true;
			updateDisplay();
		}
	}
	public void toggleLockedLayer(Layer l) {
		if (l!=null) {
			l.locked=!l.locked;
			_updateFlags[UPDATE_ANCHOR]=true;
			_updateFlags[UPDATE_CONTROLS]=true;
			updateDisplay();
		}
	}
	////////////////////////////////////////lock Elements ////////////////////////////////////////////////
	public void lockUnlock(boolean lock) {
		_undoController.addSnapshotIfLastWasDelta();
		lockUnlockInternal(_selectionOverlay.getSelection(), lock) ;
		_selectionOverlay.clear();

		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_SCOPE]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		_updateFlags[UPDATE_UNDO]=true;
		updateDisplay();
	}
	public void lockUnlock(ArrayList<DrawingElement> strokes,boolean lock) {
		_undoController.addSnapshotIfLastWasDelta();
		lockUnlockInternal(strokes, lock);
		 _updateFlags[UPDATE_ANCHOR]=true;
		 _updateFlags[UPDATE_SCOPE]=true;
		 _updateFlags[UPDATE_CONTROLS]=true;
		 _updateFlags[UPDATE_UNDO]=true;
		 updateDisplay();

	}
	public void lockUnlockInternal(ArrayList<DrawingElement> strokes,boolean lock) {
		for (DrawingElement de : strokes) {
			de.locked=lock;
		}
	}
	//////////////////////////////////////// group Elements ////////////////////////////////////////////////
	//public static final int GROUP_FLAG_DESELECT = 1;
	public Group group(ArrayList<DrawingElement> elements, int flags){
		_undoController.addSnapshotIfLastWasDelta();
		Group g = groupInternal( elements,  flags);
		_updateFlags[UPDATE_UNDO]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		updateDisplay();
		return g;
	}
	
	private Group groupInternal(ArrayList<DrawingElement> elements, int flags){
		Group g = new Group();
		int maxIndex = -1;
		for (DrawingElement el:elements){maxIndex=Math.max(maxIndex, getCurrentLayer().indexOf(el));}
		Collections.sort(elements,new Comparator<DrawingElement>() {
			@Override
			public int compare(DrawingElement object1, DrawingElement object2) {
				int indexOf1 = getCurrentLayer().indexOf(object1);
				int indexOf2 = getCurrentLayer().indexOf(object2);
				
				return indexOf1-indexOf2;
			}
		});
		for (DrawingElement el:elements) {
			el.setUpdateListener(null);
			g.elements.add(el);
			getCurrentLayer().remove(el);
		}
		int insertIdx = maxIndex-g.elements.size();
		if (insertIdx>0 && insertIdx<getCurrentLayer().size()) {
			getCurrentLayer().add(insertIdx,g);
		} else {
			getCurrentLayer().add(g);
		}
		//g.update(true,0,_viewPort.data);
		g.update(false, _renderer, UpdateFlags.ALL);
		//if ((flags&GROUP_FLAG_DESELECT)==GROUP_FLAG_DESELECT) {selectionOverlay.selection.removeAll(elements);}
		return g;
	}
	
	public void unGroup (Group g) {
		_undoController.addSnapshotIfLastWasDelta();
		unGroupInternal ( g);
		_updateFlags[UPDATE_UNDO]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		updateDisplay();
	}
	
	//TODO : Bug: this doesnt keep strokes selected if groups was selected before.
	private void unGroupInternal (Group g) {
		int index = getCurrentLayer().indexOf(g);
		boolean selected = _selectionOverlay.isSelected(g);
		for (int i=g.elements.size()-1;i>=0;i--) {
			DrawingElement de = g.elements.get(i);
			if (index==-1) {
				getCurrentLayer().add(de);
			} else {
				getCurrentLayer().add(index, de );
			}
			
			if (selected) {
				_selectionOverlay._selection.add(de);
			}
			//de.update(true,0,_viewPort.data);
			de.update(false, _renderer, UpdateFlags.ALL);
		}
		if (selected) {
			_selectionOverlay._selection.remove(g);
		}
		getCurrentLayer().remove(g);
		//_selectionOverlay.update();// calls updateDisplay()
		_selectionOverlay.updateBounds();
	}
	
	public void flattenGroup (Group g) {
		_undoController.addSnapshotIfLastWasDelta();
		flattenGroupInternal ( g );
		_updateFlags[UPDATE_UNDO]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		updateDisplay();
	}
	
	private void flattenGroupInternal (Group g) {
		ArrayList<Stroke> strokes = g.getAllStrokes();
		g.elements.clear();
		g.elements.addAll(strokes);
		//g.clearCached();
		//g.update(true,0,_viewPort.data);
		g.update(false, _renderer, UpdateFlags.ALL);
	}
	
	//////////////////////////////////////// copy / paste / duplicate ////////////////////////////////////////////////
	public int clipboardOperation(CopyMode val ) {
		_undoController.addSnapshotIfLastWasDelta();
		int cnt = 0;
		Group clip = new Group();
		ArrayList<DrawingElement> _selection = getSortedSelection();
		for (DrawingElement de : _selection) {
			DrawingElement duplicate = de.duplicate();
			clip.elements.add(duplicate);
			cnt++;
		}
		//clip.update(true,0,_viewPort.data);
		clip.update(true, _renderer, UpdateFlags.PATHBOUNDSONLY);
		if (val==CopyMode.CUT) {
			getCurrentLayer().removeAll(_selection);
			_renderer.removeFromCache(_selection);
			_selection.clear();
		}
		if (clip.elements.size()>0) {
			if (val!=CopyMode.DUPLICATE) {_clipboard.add(0,clip);}
			_currentClipboardElement = clip;
		} else {
			_currentClipboardElement = null;
		}
		while (_clipboard.size()>_maxClipboard) {
			_clipboard.remove(_clipboard.size()-1);
		}
		if (val==CopyMode.DUPLICATE) {
			paste();
		} else {
			_updateFlags[UPDATE_CLIP]=true;
		}
		
		if (val==CopyMode.CUT) {
			_updateFlags[UPDATE_UNDO]=true;
			updateDisplay();
		} else if (val==CopyMode.COPY) {
			updateFlags();
		}
		//_renderer.renderObjects.remove(clip);
		_renderer.removeFromCache(clip.elements);
		return cnt;
	}
	
	public int paste() {
		int cnt = 0;
		if (_currentClipboardElement!=null ) {
			_undoController.addSnapshotIfLastWasDelta();
			_selectionOverlay._selection.clear();
			for (int i=0;i<_currentClipboardElement.elements.size();i++) {
				DrawingElement duplicate = _currentClipboardElement.elements.get(i).duplicate();
				//duplicate.update(true,0,_viewPort.data);
				duplicate.update(true, _renderer, UpdateFlags.ALL);
				getCurrentLayer().add(duplicate);
				_selectionOverlay._selection.add(duplicate);
				cnt++;
			}
			_updateFlags[UPDATE_CLIP]=true;
			_updateFlags[UPDATE_ANCHOR]=true;
			_updateFlags[UPDATE_CONTROLS]=true;
			_updateFlags[UPDATE_UNDO]=true;
			updateDisplay();
		}
		return cnt;
	}
	
	public int pasteElement(DrawingElement de) {
		int cnt = 0;
		if (de!=null ) {
			_undoController.addSnapshotIfLastWasDelta();
			_selectionOverlay._selection.clear();
			de.update(true, _renderer, UpdateFlags.ALL);
			getCurrentLayer().add(de);
			_updateFlags[UPDATE_UNDO]=true;
			_selectionOverlay.selectElement(de);// calls update
		}
		return cnt;
	}
	//////////////////////////////////////// delete Elements ////////////////////////////////////////////////
	/*
	public void deleteElements(Vector<DrawingElement> elements) {
		drawing.elements.removeAll(elements);
		selectionOverlay.selectElements(SelectMode.DELETE);
		updateFlags[UPDATE_SELICON]=true;
		updateFlags[UPDATE_ANDCHOR]=true;
		updateDisplay();
	}
	*/
	//////////////////////////////////////// utility mothods ////////////////////////////////////////////////
	public void getBoundsForElements(RectF bounds, ArrayList<DrawingElement> strokes) {
		for (int i=0;i<strokes.size();i++) {
			DrawingElement s = strokes.get(i);
			s.update(true, _renderer, UpdateFlags.BOUNDSONLY);
			if (i==0) {bounds.set(s.calculatedBounds);}
			else {
				bounds.left = (float)Math.min(bounds.left,s.calculatedBounds.left);
				bounds.top = (float)Math.min(bounds.top,s.calculatedBounds.top);
				bounds.right = (float)Math.max(bounds.right,s.calculatedBounds.right);
				bounds.bottom = (float)Math.max(bounds.bottom,s.calculatedBounds.bottom);
			}
		}
	}
	///////////////////////////////////////////////// change order (raise/lower) ////////////////////////////////////////////
	public static final int ORDER_RAISE = 0;
	public static final int ORDER_LOWER = 1;
	public void changeOrder(int orderFlag) {
		_undoController.addSnapshotIfLastWasDelta();
		changeOrderInternal( orderFlag) ;
		_updateFlags[UPDATE_ANCHOR]=true;
		_updateFlags[UPDATE_CONTROLS]=true;
		_updateFlags[UPDATE_UNDO]=true;
		updateDisplay();
	}
	
	public void changeOrderInternal(int orderFlag) {// TODO make a end flag (i.e. shift all top or bottom)
		ArrayList<DrawingElement> remove=new ArrayList<DrawingElement>();
		int index = -1;
		final ArrayList<DrawingElement> els = getCurrentLayer();
		ArrayList<DrawingElement> sel = getSortedSelection();
		if (orderFlag==ORDER_RAISE) {
			for (int i=els.size()-2;i>=0;i--) {
				DrawingElement elNext= els.get(i+1);
				DrawingElement el= els.get(i);
				boolean containsThis = sel.contains(el);
				boolean containsNext = sel.contains(elNext);
				if (containsThis && !containsNext) {
					els.remove(el);
					els.add(i+1,el);
				} 
			}
		} else if (orderFlag==ORDER_LOWER) {
			for (int i=1;i<els.size();i++) {
				DrawingElement elNext= els.get(i-1);
				DrawingElement el= els.get(i);
				boolean containsThis = sel.contains(el);
				boolean containsNext = sel.contains(elNext);
				if (containsThis && !containsNext) {
					els.remove(el);
					els.add(i-1,el);
				} 
			}
		}
		/*
		for (int i=0;i<els.size();i++) {
			DrawingElement stroke = els.get(i);
			if  (sel.contains(stroke)) {
				remove.add(stroke);
				index=i;
			}
		}
		if (els.size()==0) {return;}
		
		
		els.removeAll(remove);
		if (remove.size()>1) {
			
			if (orderFlag==ORDER_RAISE) {
				els.addAll(index<els.size()?index+1:els.size(),remove);
			} else if (orderFlag==ORDER_LOWER) {
				els.addAll(0,remove);
			}
			
			
		} else {
			if (orderFlag==ORDER_RAISE) {
				if (index==els.size()) {
					els.add(remove.get(0));
				} else {
					els.add(index+1,remove.get(0));
				}
			} else if (orderFlag==ORDER_LOWER ) {
				if (index>0) {
					els.add(index-1,remove.get(0));
				} else {
					els.add(0,remove.get(0));
				}
			}
		}
		*/
	}
	//move to selection overlay
	public ArrayList<DrawingElement> getSortedSelection() {
		//ArrayList<DrawingElement> sel = new ArrayList<DrawingElement>(_selectionOverlay._selection);
		final ArrayList<DrawingElement> elems = getCurrentLayer();
		Collections.sort(_selectionOverlay._selection, new Comparator<DrawingElement>() {
			@Override
			public int compare(DrawingElement lhs, DrawingElement rhs) {
				return elems.indexOf(lhs)-elems.indexOf(rhs);
			}
		});
		return _selectionOverlay._selection;
	}
	
	//////////////////////////////////////// pen utils ////////////////////////////////////////////////
	public void setPenSelection( PenField field, Object val ) {
		//this just doesn't save undo as is used from a slider - do manually from use point
		ArrayList<Stroke> strokeSelection = _selectionOverlay.getStrokeSelection();
		if (strokeSelection.size()>0) {
			for (DrawingElement de : strokeSelection) {//_selectionOverlay.getStrokeSelection()
				if (de instanceof Stroke) {
					Stroke stroke = (Stroke)de;
					stroke.pen.setField(field, val);
					stroke.update(false, _renderer, UpdateFlags.PAINTONLY);
				} else if (de instanceof Group) {// drill dow - test code
					ArrayList<Stroke> strokes = ((Group)de).getAllStrokes();
					for (Stroke s : strokes) {
						s.pen.setField(field, val);
						s.update(false, _renderer, UpdateFlags.PAINTONLY);
					}
				}
			}
			updateDisplay();
		}
	}
	public void setPenSelection( Pen p ) {
		//this just doesn't save undo as is used from a slider - do manually from use point
		ArrayList<Stroke> strokeSelection = _selectionOverlay.getStrokeSelection();
		if (strokeSelection.size()>0) {
			for (DrawingElement de : strokeSelection) {
				if (de instanceof Stroke) {
					Stroke stroke = (Stroke)de;
					stroke.pen.copyFrom(p);
					stroke.update(false, _renderer, UpdateFlags.PAINTONLY);
				} else if (de instanceof Group) {
					ArrayList<Stroke> strokes = ((Group)de).getAllStrokes();
					for (Stroke s : strokes) {
						s.pen.copyFrom(p);
						s.update(false, _renderer, UpdateFlags.PAINTONLY);
					}
				}
			}
			updateDisplay();
		}
	}
	public void copyPenToCurrent(Pen p) {
		_currentPen=p.duplicate();
		if (_onUpdateListener!=null) {
			_onUpdateListener.updatePenAndFill(this);
		}
	}
	public Pen getCurrentPen() {return _currentPen;}
	////////////////////////////////////////fill & bg utils ////////////////////////////////////////////////
	public void copyFillToCurrent(Fill fill ) {
		_currentFill = fill.duplicate();
		if (_onUpdateListener!=null) {
			_onUpdateListener.updatePenAndFill(this);
		}
	}
	public Fill getCurrentFill() {return _currentFill;}
	public void setFillSelection(Fill fill ) {
		// this just doesn't save undo as is used from a slider - do manually from use point
		ArrayList<Stroke> strokeSelection = _selectionOverlay.getStrokeSelection();
		if (strokeSelection.size()>0) {
			for (DrawingElement de : _selectionOverlay.getStrokeSelection()) {
				Stroke stroke = (Stroke)de;
				stroke.fill.copyFill(fill,true);// keep apply data
				//stroke.updatePainters(0,_viewPort.data);
				stroke.update(false, _renderer, UpdateFlags.ALL);
			}
			updateDisplay();
		}
	}
	
	public void setFillGradientPoints(PointF p1, PointF p2 ) {
		// this just doesn't save undo as is used from a slider - do manually from use point
		ArrayList<Stroke> strokeSelection = _selectionOverlay.getStrokeSelection();
		if (strokeSelection.size()>0) {
			for (Stroke de : _selectionOverlay.getStrokeSelection()) {
				if (de.fill.type==Fill.Type.GRADIENT) {
					Gradient gradient = de.fill._gradient;
					if (gradient.data!=null) {
						gradient.data.set(p1, p2);
					} else {
						gradient.data=new GradientData(p1,p2);
					}
					//de.updatePainters(0,_viewPort.data);
					de.update(false, _renderer, UpdateFlags.FILLPAINTONLY);
				}
			}
			updateDisplay();
		}
	}
	
	public Fill getBackGround() {return _drawing.background;}
	public void setBackGround(Fill fill ) {
		_drawing.background.copyFill(fill,true);// keep apply data
		//_drawing.updatePainters(DrawingMode.DISPLAY,_viewPort.data);
		_drawing.update(false, _renderer, UpdateFlags.FILLPAINTONLY);
		updateDisplay();
	}
	
	public void setBackGroundGradientPoints(PointF p1, PointF p2 ) {
		//this just doesn't save undo as is used from a slider - do manually from use point
		if (_drawing.background.type==Fill.Type.GRADIENT) {
			Gradient gradient = _drawing.background._gradient;
			if (gradient.data!=null) {
				gradient.data.set(p1, p2);
			} else {
				gradient.data=new GradientData(p1,p2);
			}
			//_drawing.updatePainters(DrawingMode.DISPLAY,_viewPort.data);
			_drawing.update(false, _renderer, UpdateFlags.FILLPAINTONLY);
		}
		updateDisplay();
	}
	
	public void setCurrentFont(String name, Typeface t) {
		_currentTTFont = t;
		_currentTTFontName=name;
	}
	public Typeface getCurrentFont() {
		return _currentTTFont;
	}
	public String getCurrentFontName() {
		return _currentTTFontName;
	}
	
	
	/**
	 * @return the _currentFontHeightDefault
	 */
	public float getCurrentFontHeightDefault() {
		return _currentFontHeightDefault;
	}
	/**
	 * @param _currentFontHeightDefault the _currentFontHeightDefault to set
	 */
	public void setCurrentFontHeightDefault(float _currentFontHeightDefault) {
		this._currentFontHeightDefault = _currentFontHeightDefault;
	}
	/* **************************************** OS Utility ****************************************************** */
	public int getTouchMode() {
		return _touchData._sme._mulitTouchType;
	}
	/* **************************************** Dirty ****************************************************** */
	/**
	 * @return the _dirty
	 */
	public boolean isDirty() {
		return _dirty;
	}
	/**
	 * @param _dirty the _dirty to set
	 */
	public void setDirty(boolean _dirty) {
		this._dirty = _dirty;
	}
	public AndGraphicsRenderer getRenderer() {
		return _renderer;
	}
	public void setRenderer(AndGraphicsRenderer renderer) {
		this._renderer = renderer;
	}
	
	
}
