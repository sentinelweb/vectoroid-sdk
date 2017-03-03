package co.uk.sentinelweb.views.draw.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.math.BigDecimal;

import co.uk.sentinelweb.commonlibs.errorwrap.AbstractEWView;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.util.DispUtil;
import co.uk.sentinelweb.views.draw.util.OnEWAsyncListener;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.util.SafeMotionEvent;

public class PictureView extends AbstractEWView {
	private float _zoomScale = 1f;

	private static float density = -1;

	public enum Mode {
		NONE,SINGLE,MULTI
	}

    Mode mode = Mode.NONE;
	
	public enum Fix {
		NONE, ROTATE, SCALE, PAN, SHEAR, ZOOM
	}

    Fix fixMode = Fix.NONE;

	float _zoom = 1;
	float _zoomReference = _zoom;
	String _zoomText = "1x";
	String _dimText = "100 x 100";
	boolean _multiTouchRotateAllowed = false;

	boolean _whinitialised = false;
	private final PointF _cropDim = new PointF();
	// public SoftReference<Bitmap> picture = null;
	public Bitmap _picture = null;
	private final RectF _cropEdge = new RectF();
	private RectF _zoomCropEdge = new RectF();

	PointF _start = new PointF();
	PointF _midPointOp = null;
	PointF _usePoint = new PointF();
	PointF _usePoint2 = new PointF();
	
	PointF _midPointView = null;
	PointF _oldMidPointView = null;
	
	float _oldDist = 1f;
	float _startAngle = 0f;

	Matrix _matrix = new Matrix();
	Matrix _tmpMatrix = new Matrix();
	Matrix _zoommatrix = new Matrix();
	private SafeMotionEvent _sme = null;

	Paint _cropBgPaint;
	Paint _picPaint;
	Paint _txtPaint;
	
	int _initialRotation = 0;
	
	OnEWAsyncListener<Canvas> _picDrawListener;
	OnEWAsyncListener<Fix> _picFixModeListener;

	public PictureView(final Context context) {
		super(context);
		init(context);
		//setCropRect();
	}

	public PictureView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		//setCropRect();

	}

	public PictureView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init(context);
		//setCropRect();
	}

	private void init(final Context context) {
		if (density == -1) {
			density = DispUtil.getDensity(context);
		}
		_sme = new SafeMotionEvent(context);
		_cropBgPaint = new Paint();
		_cropBgPaint.setARGB(128, 0, 0, 0);
		_cropBgPaint.setStyle(Style.FILL);
		_picPaint = new Paint();
		_picPaint.setARGB(255, 255, 255, 255);
		_picPaint.setStyle(Style.STROKE);
		_picPaint.setAntiAlias(true);
		_picPaint.setFilterBitmap(true);
		_txtPaint = new Paint(_picPaint);
		_txtPaint.setStrokeWidth(1 * density);
		_txtPaint.setStyle(Style.STROKE);
		_txtPaint.setTextSize(15 * density);
		_txtPaint.setAntiAlias(true);
		final SafeMotionEvent sme = new SafeMotionEvent(getContext());
		if (sme._mulitTouchType == DVGlobals.TOUCHTYPE_MULTI_DISTINCT || sme._mulitTouchType == DVGlobals.TOUCHTYPE_MULTI_FULL) {
			_multiTouchRotateAllowed = true;
		}
	}

	private void setCropRect(final boolean initZoom) {
		_cropEdge.left = (getMeasuredWidth() - _cropDim.x) / 2;
		_cropEdge.right = _cropEdge.left + _cropDim.x;
		_cropEdge.top = (getMeasuredHeight() - _cropDim.y) / 2;
		_cropEdge.bottom = _cropEdge.top + _cropDim.y;
		// Log.d(DVGlobals.LOG_TAG, "croprect:"+PointUtil.tostr(cropEdge));
		if (initZoom) initZoom();
		setZoom();
	}

	private void initZoom() {
		if (getMeasuredWidth() > 0) {
			if (_cropDim.x * _zoom > getMeasuredWidth()/_zoomScale) {
				_zoom = getMeasuredWidth()/_zoomScale / _cropDim.x;
			}
			if (_cropDim.y * _zoom > getMeasuredHeight()/_zoomScale) {
				_zoom = getMeasuredHeight()/_zoomScale / _cropDim.y;
			}
		}
		
	}

	private void setZoom() {
		_zoomCropEdge.left = (getMeasuredWidth() - _cropDim.x * _zoom) / 2;
		_zoomCropEdge.right = _zoomCropEdge.left + _cropDim.x * _zoom;
		_zoomCropEdge.top = (getMeasuredHeight() - _cropDim.y * _zoom) / 2;
		_zoomCropEdge.bottom = _zoomCropEdge.top + _cropDim.y * _zoom;
		// Log.d(DVGlobals.LOG_TAG,
		// "zoomcroprect:"+PointUtil.tostr(zoomCropEdge));
		_zoomText = BigDecimal.valueOf(_zoom).setScale(2, BigDecimal.ROUND_HALF_EVEN).toPlainString() + " x";
	}

	@Override
	protected void onEWDraw(final Canvas canvas) {
		if (!_whinitialised) {
			setCropRect(false);
			_whinitialised=true;
		}
		if (_picture != null) {
			if (_midPointView == null) {
				_midPointView = new PointF(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
				final boolean isLand = _midPointView.x>_midPointView.y;
				if (_oldMidPointView!=null ) {
					final boolean wasLand = _oldMidPointView.x>_oldMidPointView.y;
					if (wasLand != isLand) {
						_usePoint.set(_midPointView.x-_oldMidPointView.x,_midPointView.y-_oldMidPointView.y);
						//Log.d(DVGlobals.LOG_TAG, "draw:rot:"+PointUtil.tostr(_usePoint));
						_matrix.postTranslate(_usePoint.x, _usePoint.y);
					}
				}
				if (_oldMidPointView==null) {_oldMidPointView = new PointF();}
				_oldMidPointView.set(_midPointView.x,_midPointView.y);
			}
			//Log.d(DVGlobals.LOG_TAG, "draw:"+_matrix.toShortString());
			_zoommatrix.set(_matrix);
			_zoommatrix.postScale(_zoom, _zoom, _midPointView.x, _midPointView.y);
			canvas.drawBitmap(_picture, _zoommatrix, _picPaint);
		}

		canvas.drawRect(0, 0, _zoomCropEdge.left, getMeasuredHeight(), _cropBgPaint);
		canvas.drawRect(_zoomCropEdge.left, 0, _zoomCropEdge.right, _zoomCropEdge.top, _cropBgPaint);
		canvas.drawRect(_zoomCropEdge.left, _zoomCropEdge.bottom, _zoomCropEdge.right, getMeasuredHeight(), _cropBgPaint);
		canvas.drawRect(_zoomCropEdge.right, 0, getMeasuredWidth(), getMeasuredHeight(), _cropBgPaint);
		canvas.drawRect(_zoomCropEdge, _picPaint);
		if (_picDrawListener != null) {
			_picDrawListener.onAsync(canvas);
		}
		canvas.drawText(_zoomText, 60*density,  60*density+  _txtPaint.getTextSize(), _txtPaint);
		canvas.drawText(_dimText, 60*density,  60*density+2* _txtPaint.getTextSize(), _txtPaint);
	}

	public Bitmap snap() {
		// TODO problem when snap bound is negative (i.e. for large pic zoomed
		// out)
		_tmpMatrix.set(_matrix);
		if (DVGlobals._isDebug)
			Log.d(DVGlobals.LOG_TAG, ("snap():" + getMeasuredWidth()) + "x" + (getMeasuredHeight()) + ":" + (_cropEdge.left) + "x" + (_cropEdge.top) + ":" + _cropDim.x + "x" + _cropDim.y);
		if (_picture != null) {// .get()
			try {
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "snap():1");
				final Bitmap tgt = Bitmap.createBitmap((int) _cropDim.x, (int) _cropDim.y, Bitmap.Config.ARGB_8888);
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "snap():2");
				final Canvas c = new Canvas();
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "snap():3");
				c.setBitmap(tgt);
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "snap():4");
				_matrix.postTranslate(-_cropEdge.left, -_cropEdge.top);
				// c.drawBitmap(picture.get(), matrix, picPaint);
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "snap():5");
				c.drawBitmap(_picture, _matrix, _picPaint);
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "snap():6");
				final Bitmap snap = Bitmap.createBitmap(tgt, 0, 0, (int) _cropDim.x, (int) _cropDim.y);
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "snap():7");
				_matrix.set(_tmpMatrix);
				return snap;
			} catch (final OutOfMemoryError e) {
				return null;
			}
		} else {
			// Toast.makeText(getContext(),
			// R.string.err_could_not_get_the_picture, 500).show();
			return null;
		}
	}

	public Bitmap getPicture() {
		return _picture;// .get();
	}

	public void setPicture(final Bitmap picture) {
		// this.picture = new SoftReference<Bitmap>(picture);
		this._picture = picture;
		_initialRotation = 0;
		// float aspect = getMeasuredWidth()/(float)getMeasuredHeight();
		initPicMatrix(picture);
		//initFromCropDim();
		invalidate();
	}

	private void initPicMatrix(final Bitmap picture) {
		if (this._picture != null) {
			final PointF scale = new PointF(1, 1);
			final float sw = Math.max(_cropDim.x, getMeasuredWidth()) * 0.9f;
			if (picture.getWidth() > sw) {
				scale.x = sw / (float) this._picture.getWidth();
				scale.y = scale.x;
			}
			final float sh = Math.max(_cropDim.y, getMeasuredHeight()) * 0.9f;
			if (picture.getHeight() * scale.y > sh) {
				scale.y = sh / (float) this._picture.getHeight();
				scale.x = scale.y;
			}
			_matrix.setTranslate(_cropEdge.left, _cropEdge.top);
			_matrix.setScale(scale.x, scale.y);
		}
	}

	public void cleanReference() {
		if (this._picture != null) {
			this._picture.recycle();
		}
		this._picture = null;
		System.gc();
	}

	public void reset() {
		try {
			_matrix = new Matrix();
			_tmpMatrix = new Matrix();
			_zoommatrix = new Matrix();
			_zoom = 1;
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void initFromCropDim() {
		if (_picture!=null) {
			final boolean isRotated = _initialRotation==90 || _initialRotation==-90 || _initialRotation==270;
			final float picwid = (float) _picture.getWidth();
			final float pichgt = (float) _picture.getHeight();
			
			final float scale = Math.max(_cropEdge.width() / (!isRotated?picwid:pichgt), _cropEdge.height() / (!isRotated?pichgt:picwid));
			initMatrix(new PointF(_cropEdge.left / scale, _cropEdge.top / scale), scale, _initialRotation);
		}
	}

	public void initMatrix(final PointF tl, final float scale, final float rot) {
		Log.d(DVGlobals.LOG_TAG,"initMatrix:tl:"+PointUtil.tostr(tl)+":"+scale);
		_matrix.reset();
		_matrix.postTranslate(tl.x, tl.y);
		_matrix.postScale(scale, scale);
		_matrix.postRotate(rot, getMeasuredWidth()/2, getMeasuredHeight()/2);
		setZoom();
	}
	
	public void initMatrixRot(final int rot) {
		_initialRotation = rot;
		_matrix.postRotate(rot, getMeasuredWidth()/2, getMeasuredHeight()/2);
	}
	public Fix getFixMode() {
		return fixMode;
	}

	public void setFixMode(final Fix fixMode) {
		this.fixMode = fixMode;
		if (_picFixModeListener!=null) {
			_picFixModeListener.onAsync(fixMode);
		}
	}
	
	public void rotate(final int rot) {
		final boolean doRotate = rot==90 || rot==-90 || rot==270;
		if (doRotate) {
			setCropDimensions(_cropDim.y,_cropDim.x);
		}
		_matrix.postRotate(rot, getMeasuredWidth()/2, getMeasuredHeight()/2);
	}
	
	public void setCropDimensions(final float w, final float h) {
		_cropDim.set(w, h);
		_dimText = w + " x " + h;
		_whinitialised=false;
		setCropRect(true);
		invalidate();
	}

	@Override
	protected void onEWFocusChanged(final boolean arg0, final int arg1, final Rect arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	protected boolean onEWKeyDown(final int arg0, final KeyEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean onEWKeyUp(final int arg0, final KeyEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onEWLayout(final boolean arg0, final int arg1, final int arg2, final int arg3, final int arg4) {
		Log.d(DVGlobals.LOG_TAG, "onEWLayout:" + arg0 + ":" + arg1 + ":" + arg2 + ":" + arg3 + ":" + arg4);
		_midPointView = null;
		_whinitialised=false;
	}

	@Override
	protected void onEWMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int height = MeasureSpec.getSize(heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(width, height);
		//setCropRect();
		_whinitialised=false;
		//invalidate();
	}

	@Override
	protected boolean onEWTouchEvent(final MotionEvent event) {
		// dumpEvent(event);
		final int actionMask = event.getAction() & SafeMotionEvent.ACTION_MASK;
		if (DVGlobals._isDebug)
			Log.d(DVGlobals.LOG_TAG, "actionMask:" + actionMask);
		if (fixMode == Fix.NONE) {
			if (actionMask == MotionEvent.ACTION_DOWN) {
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "down:");
				_tmpMatrix.set(_matrix);
				_start.set(event.getX(), event.getY());
				// if (DVGlobals._isDebug) Log.d(LOG_TAG, "mode=DRAG");
				mode = Mode.SINGLE;
			} else if (actionMask == MotionEvent.ACTION_UP) {
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "up:");
			} else if (actionMask == MotionEvent.ACTION_MOVE) {
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "move:");
				if (mode == Mode.SINGLE) {
					_matrix.set(_tmpMatrix);
					if (DVGlobals._isDebug)
						Log.d(DVGlobals.LOG_TAG, "mv pointercount:" + _sme.getPointerCount(event));
					_matrix.postTranslate(event.getX() - _start.x, event.getY() - _start.y);
				} else if (mode == Mode.MULTI) {
					final float newDist = _sme.spacing(event);
					if (newDist > 10f) {
						_matrix.set(_tmpMatrix);
						final float scale = newDist / _oldDist;
						_sme.midPoint(_usePoint, event);
						_matrix.postScale(scale, scale, _midPointOp.x, _midPointOp.y);
						_matrix.postTranslate(_usePoint.x - _midPointOp.x, _usePoint.y - _midPointOp.y);
						if (_multiTouchRotateAllowed) {
							final float endAngle = _sme.angle(event);
							_matrix.postRotate(_startAngle - endAngle, _midPointOp.x, _midPointOp.y);
						}
					}
				}
			} else if (_sme.ver > DVGlobals.MULTITOUCH_MINVER && actionMask == SafeMotionEvent.ACTION_POINTER_DOWN) {
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "pdown:");
				_oldDist = _sme.spacing(event);
				_startAngle = _sme.angle(event);
				if (_oldDist > 10f) {
					_tmpMatrix.set(_matrix);
					if (_midPointOp == null) {
						_midPointOp = new PointF();
					}
					_sme.midPoint(_midPointOp, event);
					mode = Mode.MULTI;
				}
			} else if (_sme.ver > DVGlobals.MULTITOUCH_MINVER && actionMask == SafeMotionEvent.ACTION_POINTER_UP) {
				if (DVGlobals._isDebug)
					Log.d(DVGlobals.LOG_TAG, "pup:");
				mode = Mode.NONE;
			}
		} else {
			if (actionMask == MotionEvent.ACTION_DOWN) {
				_tmpMatrix.set(_matrix);
				_start.set(event.getX(), event.getY());
				if (_midPointOp == null) {
					_midPointOp = new PointF();
				}
				_midPointOp.set(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
				_startAngle = _sme.angle(_midPointOp, event);
				_zoomReference = _zoom;
			} else if (actionMask == MotionEvent.ACTION_MOVE) {
				switch (fixMode) {
				case PAN:
					_matrix.set(_tmpMatrix);
					_matrix.postTranslate(event.getX() - _start.x, event.getY() - _start.y);
					break;
				case ROTATE:
					_matrix.set(_tmpMatrix);
					final float endAngle = _sme.angle(_midPointOp, event);
					_matrix.postRotate(_startAngle - endAngle, _midPointOp.x, _midPointOp.y);
					break;
				case SCALE:
					_matrix.set(_tmpMatrix);
					final float scaleX = event.getX() / _start.x;
					final float scaleY = event.getY() / _start.y;
					_matrix.postScale(scaleX, scaleY, _midPointOp.x, _midPointOp.y);
					break;
				case SHEAR:
					_matrix.set(_tmpMatrix);
					final float shearX = (event.getX() - _start.x) / _start.x * density;
					final float shearY = (event.getY() - _start.y) / _start.y * density;
					_matrix.postSkew(shearX, shearY, _midPointOp.x, _midPointOp.y);
					break;
				case ZOOM:
					_zoom = _zoomReference + .2f*((event.getY() - _start.y) / _start.y) * density;
					if (_zoom * _cropDim.x < 20) {
						_zoom = 20 / _cropDim.x;
					}
					if (_zoom * _cropDim.y < 20) {
						_zoom = 20 / _cropDim.y;
					}
					setZoom();
					break;
				}
			} else if (actionMask == MotionEvent.ACTION_UP) {
			}
		}
		if (DVGlobals._isDebug)
			Log.d(DVGlobals.LOG_TAG, _matrix.toShortString());
		invalidate();
		return true;
	}

	private float dist(final PointF start, final MotionEvent event) {
		final float x = start.x - _sme.getX(0, event);
		final float y = start.y - _sme.getY(0, event);
		return (float) Math.sqrt(x * x + y * y);
	}

	/**
	 * @param _picDrawListener
	 *            the _picDrawListener to set
	 */
	public void setPicDrawListener(final OnEWAsyncListener<Canvas> _picDrawListener) {
		this._picDrawListener = _picDrawListener;
	}
	public void setFixModeListener(final OnEWAsyncListener<Fix> _picFixListener) {
		this._picFixModeListener = _picFixListener;
	}
	/**
	 * @return the zoom
	 */
	public float getZoom() {
		return _zoom;
	}

	/**
	 * @return the zoomCropEdge
	 */
	public RectF getZoomCropEdge() {
		return _zoomCropEdge;
	}

	/**
	 * @param zoomCropEdge
	 *            the zoomCropEdge to set
	 */
	public void setZoomCropEdge(final RectF zoomCropEdge) {
		this._zoomCropEdge = zoomCropEdge;
	}

	/**
	 * @return the zoomScale
	 */
	public float getZoomScale() {
		return _zoomScale;
	}

	/**
	 * @param zoomScale the zoomScale to set
	 */
	public void setZoomScale(final float zoomScale) {
		this._zoomScale = zoomScale;
	}


	/* Show an event in the LogCat view, for debugging */
	/*
	 * 
	 * private void dumpEvent(MotionEvent event) { String names[] = { "DOWN",
	 * "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?",
	 * "8?", "9?" }; StringBuilder sb = new StringBuilder(); int action =
	 * event.getAction(); int actionCode = action & SafeMotionEvent.ACTION_MASK;
	 * sb.append("event ACTION_").append(names[actionCode]); if (actionCode ==
	 * SafeMotionEvent.ACTION_POINTER_DOWN || actionCode ==
	 * SafeMotionEvent.ACTION_POINTER_UP) { sb.append("(pid ").append( action >>
	 * SafeMotionEvent.ACTION_POINTER_ID_SHIFT); sb.append(")"); }
	 * sb.append("["); for (int i = 0; i < sme.getPointerCount(event); i++) {
	 * sb.append("#").append(i);
	 * sb.append("(pid ").append(sme.getPointerId(i,event));
	 * sb.append(")=").append((int) sme.getX(i,event));
	 * sb.append(",").append((int) sme.getY(i,event)); if (i + 1 <
	 * sme.getPointerCount(event)) sb.append(";"); } sb.append("]");
	 * Log.d(DVGlobals.LOG_TAG, sb.toString()); }
	 * 
	 * private String tostr(RectF r) { return
	 * "rect["+r.left+","+r.top+","+r.bottom+","+r.right+"]"; }
	 * 
	 * private String tostr(PointF pt) { return "pt["+pt.x+","+pt.y+"]"; }
	 */

}
