package co.uk.sentinelweb.views.draw.overlay;

import java.util.ArrayList;
import java.util.HashSet;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Toast;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWClickListener;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.controller.TransformController;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.Group;
import co.uk.sentinelweb.views.draw.model.Stroke;
import co.uk.sentinelweb.views.draw.model.Stroke.Type;
import co.uk.sentinelweb.views.draw.model.TransformOperatorInOut;
import co.uk.sentinelweb.views.draw.model.TransformOperatorInOut.Axis;
import co.uk.sentinelweb.views.draw.model.TransformOperatorInOut.Trans;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Mode;
import co.uk.sentinelweb.views.draw.model.internal.TouchData;
import co.uk.sentinelweb.views.draw.render.VecRenderer.Operator;
import co.uk.sentinelweb.views.draw.undo.TransformUndoElement;
import co.uk.sentinelweb.views.draw.undo.UndoElement.UndoOperationType;
import co.uk.sentinelweb.views.draw.util.BoundsUtil;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class ControlsOverlay extends Overlay {

	public static final int BNDS_NUM = 8;
	
	public static final int BNDS_MOVE_BUT = 0;
	public static final int BNDS_ROT_BUT = 1;
	public static final int BNDS_SHEAR_H_BUT = 2;
	public static final int BNDS_SHEAR_V_BUT = 3;
	public static final int BNDS_SCALE_H_BUT = 4;
	public static final int BNDS_SCALE_V_BUT = 5;
	public static final int BNDS_SCALE_BUT = 6;
	public static final int BNDS_MENU_BUT = 7;
	
	public RectF[] _boundsForControlObjects = new RectF[BNDS_NUM];

	public int[] _resForControlObjects = {
			R.drawable.i_move,R.drawable.i_rotate,R.drawable.ct_shear_h,//R.drawable.ct_rot ct_shear_h
			R.drawable.ct_shear_v,R.drawable.ct_scale_h,R.drawable.ct_scale_v,//ct_shear_v
			R.drawable.ct_scale_vh,R.drawable.i_menu};
	public boolean[] _ttfDispControlObjects = {
			true,true,false,
			false,true,true,
			true,true
		};
	public boolean[] _normalDispControlObjects = {
			true,true,true,
			true,true,true,
			true,true
		};
	public Bitmap[] _iconsForControlObjects = new Bitmap[BNDS_NUM];
	
	public RectF _controlsRect = new RectF();
	public PointF _controlsCentre = new PointF();
	
	float _defaultIconSize = 40;
	int _iconSize = (int) _defaultIconSize;
	public Rect _defaultRectForControls = new Rect(0,0,(int)_defaultIconSize,(int)_defaultIconSize);
	
	int _controlTouched = -1;
	
	public Trans _controlState = Trans.NONE;
	public Axis _controlAxis = Axis.NONE;
	public HashSet<Trans> _fix = new HashSet<Trans>();
	public Axis _fixAxis = Axis.NONE;
	public boolean _fine = false;
	
	public Paint _borderPaint;
	Paint _controlsPaint;
	private boolean _useMultiTouch;
	public RectF _useRectF = new RectF();
	public Rect _useRect = new Rect();
	//PointF _dropLastPoint = null;
	OnClickListener _menuClick = null;
	
	TransformOperatorInOut _lastTransform = null;
	// RM: using canvas transform
//	public boolean _isTransforming=false;
	public ControlsOverlay(DrawView d) {
		super(d);
		for (int i=0;i<_boundsForControlObjects.length;i++) {_boundsForControlObjects[i]=new RectF();}
		for (int i=0;i<_boundsForControlObjects.length;i++) {
			BitmapDrawable icondrawable = (BitmapDrawable)d.getContext().getResources().getDrawable(_resForControlObjects[i]); 
	        if (icondrawable!=null) {
	        	_iconsForControlObjects[i] = icondrawable.getBitmap(); 
	        }
		}
		_iconSize = (int)(_defaultIconSize*_density);
		int defIconSize=(int)(30*_density);
		_defaultRectForControls = new Rect(0,0,defIconSize,defIconSize);
		_borderPaint=new Paint();
		_borderPaint.setARGB(255, 125, 125,125);
		_borderPaint.setStrokeWidth(1);
		_borderPaint.setStyle(Style.STROKE);
		_borderPaint.setFilterBitmap(true);
		_controlsPaint=new Paint();
		_controlsPaint.setARGB(128, 0, 0,0);
		_controlsPaint.setStrokeWidth(1);
		_controlsPaint.setStyle(Style.FILL_AND_STROKE); 
		//_fix.add(Trans.ROTATE);
		_fix.add(Trans.SCALE);
		_fix.add(Trans.MOVE);
		_fixAxis = Axis.PRESERVE_ASPECT;
	}
	
	@Override
	public void draw(Canvas c) {
		if (isControlsActive()) {
			boolean[] disp = _normalDispControlObjects;
			if (_drawView._mode.opState==Mode.EDIT) {
				boolean anyttf  = anyttf(_drawView._selectionOverlay.getSelection());
				if (anyttf) {disp = _ttfDispControlObjects;}
			}
			for (int i=0;i<BNDS_NUM;i++) { 
				if (!disp[i]) {continue;}
				if (i==BNDS_MOVE_BUT) {continue;}// rm test
				_useRectF.set( _boundsForControlObjects[i]);
				if ( _controlTouched>-1 && i==_controlTouched) {
					_useRectF.top-=10;
					_useRectF.left-=10;
					_useRectF.bottom+=10;
					_useRectF.right+=10;
				}
				c.drawRect(_useRectF, _controlsPaint);
				c.drawRect(_useRectF, _borderPaint);
				_useRect.set(0,0,_iconsForControlObjects[i].getWidth(),_iconsForControlObjects[i].getHeight());
				//RectF rectCtl =  
				c.drawBitmap(_iconsForControlObjects[i], _useRect, _useRectF, _borderPaint);//_defaultRectForControls
			}
		}
	}

	private boolean anyttf(ArrayList<DrawingElement> selection) {
		for (DrawingElement s:selection) {
			if (s instanceof Stroke) {
				if (((Stroke)s).type==Type.TEXT_TTF) {return true;}
			} else if (s instanceof Group) {
				if (anyttf(((Group)s).elements)) {return true;}
			}
		}
		return false;
	}
	
	private boolean isControlsActive() {
		return (_drawView._mode.opState==Mode.EDIT && _drawView._selectionOverlay.getSelection().size()>0) || 
				(_drawView._mode.opState==Mode.POINTS_SEL && _drawView._selectionOverlay._pointsSelection.size()>0);
	}
	
	@Override
	public boolean onTouch(TouchData td) {
		//return super.onTouch( td);
		if (isControlsActive()) {
			//if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "_controlOverlay:"+td._a);
			switch (td._a) {
				case ACTION_DOWN: 
					if ((_drawView._mode.opState==Mode.EDIT || _drawView._mode.opState==Mode.POINTS_SEL )) {
						if ((_controlTouched = checkControls(td._touchPointOnScreen))>-1) {
							switch (_controlTouched) {
								case BNDS_MOVE_BUT:		_controlState=Trans.MOVE;break;
								case BNDS_ROT_BUT:		_controlState=Trans.ROTATE;break;
								case BNDS_SCALE_BUT:	_controlState=Trans.SCALE;_controlAxis=Axis.PRESERVE_ASPECT;break;
								case BNDS_SCALE_H_BUT:	_controlState=Trans.SCALE;_controlAxis=Axis.X;break;
								case BNDS_SCALE_V_BUT:	_controlState=Trans.SCALE;_controlAxis=Axis.Y;break;
								case BNDS_SHEAR_H_BUT:	_controlState=Trans.SHEAR;_controlAxis=Axis.X;break;
								case BNDS_SHEAR_V_BUT:	_controlState=Trans.SHEAR;_controlAxis=Axis.Y;break;
							}
							if ( _controlState!=Trans.NONE) {
								// RM: using canvas transform - com out
								_drawView._transformController.makeTmpSelection(_controlState!=Trans.SHEAR && _controlState!=Trans.ROTATE);// condition for using bitmap fill
								_drawView.invalidate();
							} 
							_drawView.useTouchCache();
							if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "_controlTouched:"+_controlTouched);
							return true;
						}
					} 
					return false;
				case ACTION_MOVE: 
					if ( _controlState!=Trans.NONE) {
						_lastTransform=_drawView._transformController.modifyTmpSelection(td,_controlState,_controlAxis,_drawView._mode.opState==Mode.POINTS_SEL,_fine);//td.touchPoint,td.touchPointOnScreen
						// RM: using canvas transform
//						_lastTransform = _drawView._transformController.getTransformOperatorSingle( td,_controlState,_controlAxis,_drawView._mode.opState==Mode.POINTS_SEL,_fine );
//						addTranformAnimObjects();
//						_isTransforming=true;
						_drawView.invalidate();
						return true;
					}
					return false;
				case ACTION_UP: 
				 	if (_controlTouched==BNDS_MENU_BUT) {
				 		//if (_drawView._isDebug) Log.d(DVGlobals.LOG_TAG, "ctloerlay.onTouch:_controlTouched:BNDS_MENU_BUT ");
				 		if (_menuClick==null) {
							Dialog selectionMenu = _drawView._drawingElementController.getSelectionMenu();
							if (selectionMenu!=null) {selectionMenu.show();}
							else {Toast.makeText(_drawView.getContext(), "No menu yet ..", 500).show();}
				 		} else {
				 			_menuClick.onClick(_drawView);
				 		}
						_controlTouched=-1;
						_drawView.dropTouchCache();
						_controlState=Trans.NONE;
						return true;//break;
					} 
				 	if (_controlState==Trans.MOVE) {
				 		if (_drawView._selectionOverlay.selectConditions(td)) {
				 			finishOperation(false,false);
				 			return false;
				 		} else {
				 			finishOperation(false,true);
							return true;
				 		}
				 	} else if (_controlState!=Trans.NONE) {
				 		finishOperation(false,true);
						return true;
				 	} 
				 	_controlTouched=-1;
					return false;// continue
				case ACTION_MULTI_DOWN: 
					// RM: using canvas transform - comment out
					if (_useMultiTouch) {
						_drawView._transformController.makeTmpSelection(!(_fix.contains(Trans.ROTATE)||_fix.contains(Trans.SHEAR)));
						_drawView.useTouchCache();
						_drawView.invalidate();
					}
					break;
				case ACTION_MULTI_MOVE: 
					if (_useMultiTouch && !td.underThreshold()) {
						_lastTransform=_drawView._transformController.modifyTmpSelectionMultiTouch( td, _drawView._mode.opState==Mode.POINTS_SEL, _fix, _fixAxis, _fine );
						// RM: using canvas transform
//						_lastTransform = _drawView._transformController.getTransformOperatorMulti( td, _drawView._mode.opState==Mode.POINTS_SEL, _fix, _fixAxis, _fine );
//						addTranformAnimObjects();
//						_isTransforming=true;
						_drawView.invalidate();
					}
					break;
				case ACTION_MULTI_UP:
					if (_useMultiTouch) {
						finishOperation(true, true);
					}
					break;
				default : return false;
			}
		}
		return false;
	}

	private void addTranformAnimObjects() {
		for (DrawingElement de:_drawView._selectionOverlay._selection) {
			Operator o = _drawView._renderer.animations.get(de);
			if (o==null) {
				o=new Operator();
				o.m=new Matrix();
			}
			//PointF _usePoint = new PointF();
			//PointUtil.addVector( _lastTransform.anchor, _usePoint, _drawView._viewPort.data.topLeft );
			//PointUtil.mulVector(_usePoint, _usePoint, _drawView._viewPort.data.zoom );
			//PointF anchorCorrect = _lastTransform.getTranslationForCOGAndAnchor( _lastTransform.matrix2, _usePoint );
			//matrix3[0][2]anchorCorrect.x+trans.x;
			//matrix3[1][2]=anchorCorrect.y+trans.y;
//			PointF topLeft = _drawView._viewPort.data.topLeft;
//			float zoom = _drawView._viewPort.data.zoom;
//			PointF anchorOnScreen = new PointF(
//					(_lastTransform.anchor.x-topLeft.x)/zoom,
//					(_lastTransform.anchor.y-topLeft.y)/zoom
//				);
//			_usePoint.set(anchorOnScreen);
			//PointUtil.subVector(anchorOnScreen, _usePoint, _drawView._viewPort.data.topLeft );
			//PointF anchorCorrect = _lastTransform.getTranslationForCOGAndAnchor( _lastTransform.matrix2, _usePoint );
			
			PointF anchorCorrect = new PointF(
					((float)_lastTransform.matrix3[0][2])*_drawView._viewPort.data.zoom,
					((float)_lastTransform.matrix3[1][2])*_drawView._viewPort.data.zoom
				);
				

			o.m.setValues(
					new float[]{
							(float)_lastTransform.matrix3[0][0],(float)_lastTransform.matrix3[0][1],anchorCorrect.x,//+_lastTransform.trans.x,//((float)_lastTransform.matrix3[0][2])*_drawView._viewPort.data.zoom,
							(float)_lastTransform.matrix3[1][0],(float)_lastTransform.matrix3[1][1],anchorCorrect.y,//+_lastTransform.trans.y,//((float)_lastTransform.matrix3[1][2])*_drawView._viewPort.data.zoom,
							(float)_lastTransform.matrix3[2][0],(float)_lastTransform.matrix3[2][1],(float)_lastTransform.matrix3[2][2]
					}
				);
			_drawView._renderer.animations.put(de, o);
		}
	}
	
	private void finishOperation(boolean multi,boolean commit) {
		if (_controlState!=Trans.NONE || multi) {
			// RM: using canvas transform
//			_isTransforming=false;
//			for (DrawingElement de:_drawView._selectionOverlay._selection) {
//				_drawView._renderer.animations.remove(de);
//				if (commit && _lastTransform!=null) {
//					_drawView._transformController.transform(de,de,_lastTransform);
//				}
//			}
			if (commit) {
				_drawView._transformController.commitTmpSelection();
			} else {
				_drawView._transformController.dropTmpSelection();
			}
			_drawView.dropTouchCache();
			_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
			_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
			_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
			((TransformUndoElement)_drawView._undoController.initNextUndo(UndoOperationType.TRANSFORM))._transform=_lastTransform;
			//_drawView._undoController.getNextUndoElement())
			_drawView.updateDisplay();
			_controlState=Trans.NONE;
			_controlAxis=Axis.NONE;
			_lastTransform=null;
			_controlTouched=-1;
		}
	}
	
	public void updateSelectionIconRects() {
		// TODO update for dims
		//float iconSize=_density;
		Mode state = _drawView._mode.opState; 
		// TODO this should not be here
		if (state == Mode.PAN || state == Mode.ZOOM) {state=_drawView._mode.lastOpState;}
		_drawView._selectionOverlay.updateBounds();
		/*
		controlsRect.set(
				selectionBounds.left<iconSize?iconSize:selectionBounds.left,
				selectionBounds.top<iconSize?iconSize:selectionBounds.top,
				selectionBounds.right>getMeasuredWidth()-iconSize?getMeasuredWidth()-iconSize:selectionBounds.right,
				selectionBounds.bottom>getMeasuredHeight()-iconSize?getMeasuredHeight()-iconSize:selectionBounds.bottom
		);
		*/
		RectF selectionBounds = _drawView._selectionOverlay._selectionBounds;
		PointF minTopLeft = _drawView._viewPort.data.minTopLeft;
		PointF maxBottomRight = _drawView._viewPort.data.maxBottomRight;
		float zoom =  _drawView._viewPort.data.zoom;
		PointF topLeft = _drawView._viewPort.data.topLeft;
		_controlsRect.set(
				selectionBounds.left<minTopLeft.x+_iconSize/zoom?minTopLeft.x+_iconSize/zoom:selectionBounds.left,
				selectionBounds.top<minTopLeft.y+_iconSize/zoom?minTopLeft.y+_iconSize/zoom:selectionBounds.top,
				selectionBounds.right>maxBottomRight.x-_iconSize/zoom?maxBottomRight.x-_iconSize/zoom:selectionBounds.right,
				selectionBounds.bottom>maxBottomRight.y-_iconSize/zoom?maxBottomRight.y-_iconSize/zoom:selectionBounds.bottom
		);
		//expand controls for icons 
		_controlsRect.set(
				_controlsRect.left-_iconSize/zoom,
				_controlsRect.top-_iconSize/zoom,
				_controlsRect.right+_iconSize/zoom,
				_controlsRect.bottom+_iconSize/zoom
		);
		// expand the controls rect if bounds too small
		if (_controlsRect.width()<_iconSize*3/zoom) {
			_controlsRect.left-=(_iconSize*3/zoom-_controlsRect.width())/2;
			_controlsRect.right+=(_iconSize*3/zoom-_controlsRect.width())/2;
		}
		if (_controlsRect.height()<_iconSize*3/zoom) {
			_controlsRect.top-=(_iconSize*3/zoom-_controlsRect.height())/2;
			_controlsRect.bottom+=(_iconSize*3/zoom-_controlsRect.height())/2; 
		}
		// apply zoom factor to conrols rect
		_controlsRect.set(
				(_controlsRect.left-topLeft.x)*zoom,
				(_controlsRect.top-topLeft.y)*zoom,
				(_controlsRect.right-topLeft.x)*zoom,
				(_controlsRect.bottom-topLeft.y)*zoom
			);

		_controlsCentre = PointUtil.midpoint(_controlsRect);
		float ctrStIconX = _controlsCentre.x-_iconSize/2;
		float ctrStIconY = _controlsCentre.y-_iconSize/2;
		float ctrEndIconX = _controlsCentre.x+_iconSize/2;
		float ctrEndIconY = _controlsCentre.y+_iconSize/2;
		//boundsForControlObjects[BNDS_MOVE_BUT].set(controlsRect.left, controlsRect.bottom-iconSize, controlsRect.left+iconSize,controlsRect.bottom);
		_boundsForControlObjects[BNDS_ROT_BUT].set(_controlsRect.left, _controlsRect.top, _controlsRect.left+_iconSize, _controlsRect.top+_iconSize);
		//_boundsForControlObjects[BNDS_MOVE_BUT].set(ctrStIconX,ctrStIconY,ctrEndIconX,ctrEndIconY);
		_boundsForControlObjects[BNDS_MOVE_BUT].set(_controlsRect);// rm test
		_boundsForControlObjects[BNDS_SCALE_H_BUT].set(_controlsRect.right-_iconSize, ctrStIconY, _controlsRect.right, ctrEndIconY);
		_boundsForControlObjects[BNDS_SCALE_V_BUT].set(ctrStIconX, _controlsRect.bottom-_iconSize, ctrEndIconX, _controlsRect.bottom);
		_boundsForControlObjects[BNDS_SCALE_BUT].set(_controlsRect.right-_iconSize, _controlsRect.bottom-_iconSize, _controlsRect.right, _controlsRect.bottom);
		_boundsForControlObjects[BNDS_SHEAR_H_BUT].set(ctrStIconX, _controlsRect.top, ctrEndIconX, _controlsRect.top+_iconSize);
		_boundsForControlObjects[BNDS_SHEAR_V_BUT].set(_controlsRect.left, ctrStIconY, _controlsRect.left+_iconSize, ctrEndIconY);
		_boundsForControlObjects[BNDS_MENU_BUT].set(_controlsRect.right-_iconSize, _controlsRect.top, _controlsRect.right, _controlsRect.top+_iconSize);
	}
	
	public int checkControls(PointF touchPointOnScreen) {
		for (int i=BNDS_NUM-1;i>=0;i--) {
			if (BoundsUtil.checkBounds(_boundsForControlObjects[i], touchPointOnScreen)) {
				return i;
			}
		}
		return -1;
	}
	
	public void toggleFixmode(Trans t) {
		if (_fix.contains(t)) {
			_fix.remove(t);
		} else {
			if (t==Trans.SHEAR) {
				_fix.remove(Trans.ROTATE);
				_fix.remove(Trans.SCALE);
			} else if (t==Trans.ROTATE ){
				//_fix.remove(Trans.SCALE);
				_fix.remove(Trans.SHEAR);
			} else 	if (t==Trans.SCALE) {
				//_fix.remove(Trans.ROTATE);
				_fix.remove(Trans.SHEAR);
			}
			_fix.add(t);
		}
	}
	
	public void setFixAxis(Axis a) {
		_fixAxis=a;
	}
	public void setFine(boolean b) {
		_fine=b;
	}
	public boolean getFine( ) {
		return _fine;
	}

	/**
	 * @return the _menuClick
	 */
	public OnClickListener getMenuClick() {
		return _menuClick;
	}

	/**
	 * @param _menuClick the _menuClick to set
	 */
	public void setMenuClick(OnClickListener _menuClick) {
		this._menuClick = _menuClick;
	}
	/**
	 * @return the _menuClick
	 */
	public boolean getUseMultiTouch() {
		return _useMultiTouch;
	}

	/**
	 * @param multi the _menuClick to set
	 */
	public void setUseMultiTouch(boolean multi) {
		this._useMultiTouch = multi;
	}
}
