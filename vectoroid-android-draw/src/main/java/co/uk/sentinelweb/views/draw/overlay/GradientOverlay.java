package co.uk.sentinelweb.views.draw.overlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWAsyncListener;
import co.uk.sentinelweb.views.draw.model.Gradient.Type;
import co.uk.sentinelweb.views.draw.model.GradientData;
import co.uk.sentinelweb.views.draw.model.internal.TouchData;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Mode;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;
import co.uk.sentinelweb.views.draw.view.controller.FillController;

public class GradientOverlay extends Overlay{
	PointF _from = new PointF();
	PointF _to = new PointF();
	Paint _p;
	Paint _pbg;
	boolean _bgMode=false;
	//FillController _fc = null;
	OnEWAsyncListener<GradientData> _gradientDataListener;
	Type _type = Type.LINEAR;
	private GradientData _useGradData;
	private  PointF _usePoint1=new PointF();
	private  PointF _usePoint2=new PointF();
	
	public GradientOverlay(DrawView d) {
		super(d);
		_p = new Paint();
		_p.setColor(Color.WHITE);
		_p.setStyle(Style.STROKE);
		_p.setStrokeWidth(4*_density);
		_p.setAntiAlias(true);
		_pbg = new Paint(_p);
		_pbg.setStrokeWidth(8*_density);
		_pbg.setColor(Color.BLACK);
		_useGradData=new GradientData();
	}

	@Override
	public void draw(Canvas c) {
		if (_drawView._mode.opState==Mode.GRADIENT) {
			_usePoint1.set(_from);_drawView._renderer.convert(_usePoint1);
			_usePoint2.set(_to);_drawView._renderer.convert(_usePoint2);
			switch (_type) {
				case LINEAR:
					c.drawLine(_usePoint1.x, _usePoint1.y, _usePoint2.x, _usePoint2.y, _pbg);
					c.drawLine(_usePoint1.x, _usePoint1.y, _usePoint2.x, _usePoint2.y, _p);
					break;
				case RADIAL:
					c.drawCircle(_usePoint1.x, _usePoint1.y, PointUtil.dist(_usePoint1, _usePoint2), _pbg);
					c.drawCircle(_usePoint1.x, _usePoint1.y, PointUtil.dist(_usePoint1, _usePoint2), _p);
					break;
				case SWEEP:
					c.drawCircle(_usePoint1.x, _usePoint1.y, 15*_density, _pbg);
					c.drawCircle(_usePoint1.x, _usePoint1.y, 15*_density, _p);
					break;
			}
		}
	}

	@Override
	public boolean onTouch(TouchData td) {
		switch (td._a ) {
			case ACTION_DOWN: 
				_from.set(td._reference);
				return true;
			case ACTION_MOVE:
				_to.set(td._touchPoint);
				if (_type==Type.SWEEP) {
					_from.set(td._touchPoint);
				} 
				if (!_bgMode) {
					_drawView.setFillGradientPoints(_from, _to );
				} else {
					_drawView.setBackGroundGradientPoints(_from, _to);
				}
				return true;
			case ACTION_UP:
				if (!_bgMode) {
					_drawView.setFillGradientPoints(_from, _to );
				} else {
					_drawView.setBackGroundGradientPoints(_from, _to);
				}
				if (_gradientDataListener!=null) {
					//_fc.setCurrentGradientData(_from, _to);
					//_useGradData = new GradientData();
					_useGradData.set(_from,_to);
					_gradientDataListener.onAsync(_useGradData);
				}
				_drawView._mode.reverseState();
				return true;
			case ACTION_MULTI_DOWN: 
				_from.set(td._touchPoint);
				_to.set(td._touchPoint2);
				return true;
			case ACTION_MULTI_MOVE:
				_from.set(td._touchPoint);
				_to.set(td._touchPoint2);
				if (_type==Type.SWEEP) {
					_from.set(td._touchPoint);
				} 
				if (!_bgMode) {
					_drawView.setFillGradientPoints(_from, _to );
				} else {
					_drawView.setBackGroundGradientPoints(_from, _to);
				}
				return true;
			case ACTION_MULTI_UP:
				if (!_bgMode) {
					_drawView.setFillGradientPoints(_from, _to );
				} else {
					_drawView.setBackGroundGradientPoints(_from, _to);
				}
				if (_gradientDataListener!=null) {
					//_useGradData = new GradientData();
					_useGradData.set(_from,_to);
					_gradientDataListener.onAsync(_useGradData);
				}
				_drawView._mode.reverseState();
				return true;
			default : return false;
		}
	}
	
	public void initPoints(boolean bgMode, GradientData gd, Type type,OnEWAsyncListener<GradientData> gradientDataListener) {
		this._bgMode=bgMode;
		this._type=type;
		if (gd!=null) {
			_from.set(gd.p1);
			_to.set(gd.p2);
		} else {
			if (bgMode) {
				_from.set(0,0);
				_to.set(_drawView._drawing.size);
			} else {
				RectF selectionBounds = _drawView._selectionOverlay._selectionBounds;
				_from.set(selectionBounds.left,selectionBounds.top);
				_to.set(selectionBounds.bottom,selectionBounds.right);
			}
		}
		//_fc=fc;
		_gradientDataListener = gradientDataListener;
	}
}
