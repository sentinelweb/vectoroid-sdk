package co.uk.sentinelweb.views.draw.model.internal;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.model.Stroke;
import co.uk.sentinelweb.views.draw.model.ViewPortData;
import co.uk.sentinelweb.views.draw.model.Stroke.Type;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class Magnifier {
	private DrawView drawView;
	
	private static final int MAG_TOP=50;
	private static final int MAG_SIZE=60;
	//int _scopeSize=60;
	Bitmap _scopeBitmap;
	Canvas _scopeCanvas;
	private RectF _scopeRectF = new RectF();
	private PointF _scopeRectOffestF = new PointF(0,0);
	
	private RectF _useRectF = new RectF();
	private RectF _useRectF2 = new RectF();
	private Rect _useRect = new Rect();
	
	Paint _bmpPaint;
	
	boolean show = false;

	public Magnifier(DrawView drawView, int _scopeSize) {
		super();
		this.drawView = drawView;
		//this._scopeSize = (int)(_scopeSize*DrawView._density);
		//this._scopeTop=(int)(_scopeTop*DrawView._density);
		_scopeRectF.set(0,0,MAG_SIZE*DrawView._density,(MAG_SIZE)*DrawView._density);
		_scopeRectOffestF.set(0, MAG_TOP*DrawView._density);
		_bmpPaint = new Paint();
		_bmpPaint.setFilterBitmap(true);
		_bmpPaint.setAntiAlias(true);
		
		_scopeBitmap=Bitmap.createBitmap(_scopeSize,_scopeSize, Bitmap.Config.RGB_565);
		_scopeCanvas=new Canvas();
		_scopeCanvas.setBitmap(_scopeBitmap);
		
	}
	
	public void drawScopeBg( Stroke s,Bitmap _touchBitmapCacheBehind) {
		TouchData _touchData = drawView._touchData;
		ViewPortData vpd =drawView._viewPort.data;
		if (showConditions(s, _touchData) ) {
			_useRect.set(
					(int)(_touchData._touchPointOnScreen.x-_scopeRectF.width()/2),
					(int)(_touchData._touchPointOnScreen.y-_scopeRectF.height()/2),
					(int)(_touchData._touchPointOnScreen.x+_scopeRectF.width()/2),
					(int)(_touchData._touchPointOnScreen.y+_scopeRectF.height()/2)
				);
			_useRectF.set(0,0,_scopeRectF.width(),_scopeRectF.height());
			_scopeCanvas.drawColor(Color.WHITE);
			if (_touchBitmapCacheBehind!=null) {
				_scopeCanvas.drawBitmap(_touchBitmapCacheBehind, _useRect,_useRectF, _bmpPaint);
			} else {
				Log.d(DVGlobals.LOG_TAG,"drawingCache is null");
			}
			//
			_scopeCanvas.save();
			_scopeCanvas.scale(vpd.zoom, vpd.zoom);
			_scopeCanvas.translate(
					-_touchData._touchPoint.x+_scopeRectF.width()/2/vpd.zoom,
					-_touchData._touchPoint.y+_scopeRectF.height()/2/vpd.zoom
			);// real coords
			//_scopeCanvas.translate(,_touchData._touchPoint.y);//-_scopeSize*_viewPort.data.zoom
			drawView._renderer.sr.getStrokeRenderer().render( _scopeCanvas, s );
			_scopeCanvas.restore();
			show=true;
		}
	}
	
	private boolean showConditions(Stroke s, TouchData _touchData) {
		return _touchData._touchPointOnScreen!=null && !_touchData._isMulti &&(s!=null && s.type!=Type.TEXT_TTF);
	}
	
	public void drawScope(Canvas c) {
		if ( show ) {
			_useRect.set(0,0,(int)_scopeRectF.width(),(int)_scopeRectF.height());//src
			_useRectF.set(
					drawView.getMeasuredWidth()-_scopeRectF.width()+_scopeRectOffestF.x,
					_scopeRectF.top+_scopeRectOffestF.y,
					drawView.getMeasuredWidth()+_scopeRectOffestF.x,
					_scopeRectF.top+_scopeRectF.height()+_scopeRectOffestF.y
				);
			//c.drawBitmap(_scopeBitmap, _useRect,_useRectF, _bmpPaint);
			c.drawBitmap(_scopeBitmap, _useRect,_useRectF, _bmpPaint);
			c.drawRect( _useRectF, drawView._controlsOverlay._borderPaint);
			show=false;
		}
	}
	
	public void setScopeRectOffset(PointF p){
		_scopeRectOffestF.set(p);
	}
	public void setScopeRectSize(PointF p){
		_scopeRectOffestF.set(p);
	}
}
