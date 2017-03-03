package co.uk.sentinelweb.views.draw.model.internal;

//import com.htc.pen.PenEvent;

import android.content.Context;
import android.graphics.PointF;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.util.SafeMotionEvent;

public class TouchData {
	public enum Action {NONE,ACTION_DOWN,ACTION_MOVE,ACTION_UP,ACTION_MULTI_DOWN,ACTION_MULTI_MOVE,ACTION_MULTI_UP};
	public static final float PRESSURE_THRESHOLD_FACTOR = 0.5f;
	public MotionEvent _e;
	public PointF _touchPoint;
	public PointF _touchPointOnScreen;
	public PointF _touchPoint2;
	public PointF _touchPointOnScreen2;
	public PointF _reference = null;// the reference point for move , scale rotate, etc
	public PointF _referenceOnScreen = null;// the reference point for move , scale rotate, etc
	public PointF _reference2 = null;// the reference point for move , scale rotate, etc
	public PointF _referenceOnScreen2 = null;// the reference point for move , scale rotate, etc
	public float _referenceAngle=0;
	public float _referenceSpacing=0;
	public PointF _referenceMidpoint=new PointF();
	
	
	public long _touchDownTime = -1;
	public SafeMotionEvent _sme;
	public boolean _isMulti=false;
	public Action _a=Action.NONE;
	float _maxPressure=0;
	float _maxPressureThresholdFactor=PRESSURE_THRESHOLD_FACTOR;
	
	public PointF _usePoint = new PointF();// the reference point for move , scale rotate, etc
	
	
	public TouchData(Context c) {
		super();
		_sme=new SafeMotionEvent(c);
	}

	public void clean() {
		_e=null;
		_a=Action.NONE;
		_touchPoint = null;
		_touchPointOnScreen = null;
		_touchPoint2 = null;
		_touchPointOnScreen2 = null;
		_reference = null;
		_referenceOnScreen = null;
		_reference2 = null;
		_referenceOnScreen2 = null;
		_referenceAngle=0;
		_referenceSpacing=0;
		_referenceMidpoint=null;
		_isMulti=false;
		_touchDownTime=-1;
	}
	
	public long getTouchDownTime() {
		long time = _e.getEventTime();
		//if (_e.getHistorySize()>0) {
		//	time = _e.getHistoricalEventTime(_e.getHistorySize()-1);
		//}
		return time - _touchDownTime;
	}
	
	public boolean isMultiTouch() {
		return _sme._mulitTouchType>DVGlobals.TOUCHTYPE_SINGLE && _sme.getPointerCount(_e)>1;
	}
	
	public boolean isAction(int action) {
		int actionMask = _e.getAction() & SafeMotionEvent.ACTION_MASK;
		return _sme.ver>DVGlobals.MULTITOUCH_MINVER && actionMask==action;
	}
	
	public void setAction() {
		boolean multi=isMultiTouch();
		int actionMask = _e.getAction() & SafeMotionEvent.ACTION_MASK;
		if (actionMask==MotionEvent.ACTION_DOWN) {
			_a=multi?Action.ACTION_MULTI_DOWN:Action.ACTION_DOWN;
			
		} else if ( actionMask==MotionEvent.ACTION_UP){
			_a=multi?Action.ACTION_MULTI_UP:Action.ACTION_UP;
			//if (_a==Action.ACTION_UP) {_isMulti=false;}
			_maxPressure=0;
		} else if ( actionMask==MotionEvent.ACTION_MOVE){
			_a=multi?Action.ACTION_MULTI_MOVE:Action.ACTION_MOVE;
		} else if (actionMask==SafeMotionEvent.ACTION_POINTER_DOWN){
			_a=multi?Action.ACTION_MULTI_DOWN:Action.ACTION_DOWN;
			if (_a==Action.ACTION_MULTI_DOWN) {_isMulti=true;}
		} else if (actionMask==SafeMotionEvent.ACTION_POINTER_UP){
			_a=multi?Action.ACTION_MULTI_UP:Action.ACTION_UP;
			
		}
		//Log.d(Globals.LOG_TAG, _a+":"+_isMulti);
	}

	/**
	 * @param _e the _e to set
	 */
	public void setEvent(MotionEvent e,PointF offset,float scale) {
		_e = e;
		_maxPressure=Math.max(_maxPressure, e.getPressure());
		try {
			setAction();// replaces pen check
			/*
			if (PenEvent.isPenEvent(e)) {
				_isMulti=false;
			    int action = PenEvent.PenAction(e);
			    int button = PenEvent.PenButton(e);
			    switch (action) {
			        case PenEvent.PEN_ACTION_DOWN:
			            //handleAction(Ac, event.getX(), event.getY());
			        	_a = Action.ACTION_DOWN;
			            break;
			        case PenEvent.PEN_ACTION_UP:
			            //handleAction(ACTION_UP, event.getX(), event.getY());
			        	_a = Action.ACTION_UP;
			            break;
			        case PenEvent.PEN_ACTION_MOVE:
			        	_a = Action.ACTION_MOVE;
			        	//handleAction(ACTION_MOVE, event.getX(), event.getY());
			            break;
			    }
			} else {
				setAction();
			}
			*/
		} catch (NoClassDefFoundError e1) {
			setAction();
		}
		if (_a==Action.ACTION_UP || _a==Action.ACTION_MULTI_UP) {
			//_sme.getPointer(_usePoint, 0, _e);
			//Log.d(DVGlobals.LOG_TAG,"Action:"+_a+" isMulti"+_isMulti+": type:"+ _sme._mulitTouchType+" @"+_sme.getPointerCount(_e));//+PointUtil.tostr(_usePoint)+":"
			//_sme.getPointer(_usePoint, 1, _e);
			//Log.d(Globals.LOG_TAG,"2nd  @"+PointUtil.tostr(_usePoint));
		}

		if (!_isMulti) {
			_touchPointOnScreen = new PointF(e.getX(),e.getY());
			_touchPoint = new PointF(offset.x+_touchPointOnScreen.x*scale,offset.y+_touchPointOnScreen.y*scale);
			//_touchPointOnScreen=touchPointOnScreen;
			//_touchPoint=touchPoint;
			if (_a==Action.ACTION_DOWN || _touchDownTime==-1) {
				_touchDownTime = _e.getEventTime();
				_referenceOnScreen=new PointF(_touchPointOnScreen.x,_touchPointOnScreen.y);
				_reference=new PointF(_touchPoint.x,_touchPoint.y);
				_referenceOnScreen2=new PointF();
				_touchPointOnScreen2=new PointF();
				_touchPoint2=new PointF();
				_referenceMidpoint=new PointF();
			} 
		} else {
			if (_a!=Action.ACTION_MOVE) {
				_sme.getPointer(_touchPointOnScreen, 0, _e);
				_sme.getPointer(_touchPointOnScreen2, 1, _e);
				_touchPoint.set(offset.x+_touchPointOnScreen.x*scale,offset.y+_touchPointOnScreen.y*scale);
				_touchPoint2.set(offset.x+_touchPointOnScreen2.x*scale,offset.y+_touchPointOnScreen2.y*scale);
				if (_a==Action.ACTION_MULTI_DOWN) {
					_referenceOnScreen2.set(_touchPointOnScreen2);
					_referenceAngle=_sme.angle(_e);
					_referenceSpacing=_sme.spacing(_e);
					_sme.midPoint(_referenceMidpoint,_e);
				}
			}
		}
	}
	
	public PointF getHistroicalPoint(int ptr,int i,PointF offset,float scale) {
		return new PointF(offset.x+_e.getHistoricalX(ptr, i)*scale,offset.y+_e.getHistoricalY(ptr, i)*scale);
	}
	
	public boolean underThreshold() {
		boolean res = _e.getPressure()<_maxPressure*_maxPressureThresholdFactor;
		//Log.d(Globals.LOG_TAG, "underThreshold():"+_e.getPressure()+":"+(_maxPressure/1.5)+":"+res);
		return res;
	}
	/*
	public float getReferenceSpacing() {
		return _sme.spacing(_reference2, _reference);
	}
	public float getReferenceAngle() {
		return _sme.angle(_reference2, _reference);
	}
	public float getReferenceMidpoint() {
		return _sme.angle(_reference2, _reference);
	}
	*/
}
