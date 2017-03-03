package co.uk.sentinelweb.views.draw.view;

import android.content.Context;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import co.uk.sentinelweb.commonlibs.errorwrap.AbstractEWView;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.model.Fill;
import co.uk.sentinelweb.views.draw.model.Pen;
import co.uk.sentinelweb.views.draw.model.Stroke;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.model.ViewPortData;
import co.uk.sentinelweb.views.draw.render.ag.AndGraphicsRenderer;
import co.uk.sentinelweb.views.draw.render.ag.StrokeRenderObject;
import co.uk.sentinelweb.views.draw.util.DispUtil;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.util.StrokeUtil;

public class ShapeView extends AbstractEWView {
	float _density =-1;
	private static Paint _noImgPaint;
	Stroke _stroke;
	Stroke _strokeInternal;
	AndGraphicsRenderer agr;
	RectF _boundsRect = new RectF();
	ViewPortData vpd = new ViewPortData();
	float[] hsv = new float[3];
	public boolean _noBitmaps = false;
	private boolean firstTime = true;
	public ShapeView(Context context, AttributeSet attrs, int defStyle) {
		super(context,attrs,defStyle);
		init(context);
	}
	
	public ShapeView(Context context, AttributeSet attrs) {
		super(context,attrs);
		init(context);
	}
	
	public ShapeView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		//if (DVGlobals._isDebug) Log.d(Globals.LOG_TAG,"ShapeView():init");
		if (_density==-1) {_density=DispUtil.getDensity(context);	}
		_stroke = new Stroke(new Pen(),new Fill());
		_stroke.pen.strokeWidth=3;
		this._stroke.pen.strokeColour=Color.YELLOW;
		_stroke.pen.glowWidth=0;
		agr = new AndGraphicsRenderer();//context
		if (_noImgPaint==null) {
			_noImgPaint = new Paint();
			BitmapDrawable tspdrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.ge_tsp_bg_gr); 
			_noImgPaint.setShader(new BitmapShader(tspdrawable.getBitmap(), TileMode.REPEAT, TileMode.REPEAT));
			_noImgPaint.setStyle(Style.FILL_AND_STROKE);
		}
		//vpd.topLeft.set(0,0);
		//vpd.zoom=1;
		//agr.setVpd(vpd);
		//agr.sr._debug=true;
	}
	
	@Override
	public void onEWDraw(Canvas canvas) {
		/*
		if (getBackground()!=null) {
			getBackground().setBounds(0,0,getMeasuredWidth(),getMeasuredHeight());
			getBackground().draw(canvas);
		}
		*/
		if (firstTime) {
			firstTime=false;
			update();
			return;
		}
		Color.colorToHSV(_stroke.pen.strokeColour, hsv);
		//int bgcolor=Color.BLACK;
		//if (hsv[2]<0.25) {
		//	bgcolor=Color.WHITE;
		//}
		//canvas.drawColor(bgcolor);
		canvas.drawRect(0,0,getMeasuredWidth(),getMeasuredHeight(), _noImgPaint);
		if (_stroke!=null && agr.getVpd()!=null) {
			agr.setCanvas(canvas);
			agr.setupViewPort();
			agr.render(_strokeInternal);
			agr.revertViewPort();
		}
	}

	@Override
	public void onEWFocusChanged(boolean gainFocus, int direction,	Rect previouslyFocusedRect) {
		
		
	}

	@Override
	public boolean onEWKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public boolean onEWKeyUp(int keyCode, KeyEvent event) {
		return false;
	}

	@Override
	public void onEWLayout(boolean changed, int left, int top, int right,int bottom) {
		
		
	}

	@Override
	public void onEWMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		
	}

	@Override
	public boolean onEWTouchEvent(MotionEvent event) {
		if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "shapeview:ontouch:"+event.getAction()+":"+isClickable());
		if (event.getAction()==MotionEvent.ACTION_UP && isClickable()) {
			performClick();
			return true;
		}
		return isClickable();
	}
	
	public Stroke getStroke() {
		return _stroke;
	}
	
	public void setStroke(Stroke stroke) {
		this._stroke = stroke;
		//StrokeUtil.centreOnOrigin( _stroke );
		update();
		invalidate();
	}

	public void update() {
		/*
		if (_stroke.fill.type==Fill.Type.BITMAP) {
			_stroke.fill.type=Fill.Type.NONE;
		}
		this._stroke.update( false, agr, UpdateFlags.ALL ); 
		ViewPortData vpd = ViewPortData.getFragmentViewPort(_stroke);
		vpd.zoom=Math.abs(Math.min((getMeasuredWidth()-getPaddingLeft()-getPaddingRight())/_stroke.calculatedBounds.width(),
				(getMeasuredHeight()-getPaddingTop()-getPaddingBottom())/_stroke.calculatedBounds.height())
		);
		vpd.topLeft.set(_stroke.calculatedBounds.left-getPaddingLeft()/vpd.zoom,_stroke.calculatedBounds.top-getPaddingTop()/vpd.zoom);
		agr.setVpd(vpd);
		//if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG,"sw:"+this._stroke.pen.strokeWidth+":"+ColorUtil.toColorString(this._stroke.pen.strokeColour));
		// need to modify stroke width as calcbounds havent been modified
		StrokeRenderObject sro = (StrokeRenderObject)agr.getObject(_stroke);
		sro.fgInner.setStrokeWidth(sro.fgInner.getStrokeWidth()/vpd.zoom);
		if (sro.fgOuter.getStrokeWidth()>0) {
			sro.fgOuter.setStrokeWidth(sro.fgOuter.getStrokeWidth()/vpd.zoom);
			float blurRadius = sro.fgOuter.getStrokeWidth();
			BlurMaskFilter mBlur = new BlurMaskFilter(blurRadius/vpd.zoom, BlurMaskFilter.Blur.NORMAL);
			sro.fgOuter.setMaskFilter(mBlur);
		}
		sro.fgFill.setStrokeWidth(sro.fgFill.getStrokeWidth()/vpd.zoom);
		*/
		if (_stroke.fill.type==Fill.Type.BITMAP) {
			_stroke.fill.type=Fill.Type.NONE;
		}
		this._stroke.update( false, agr, UpdateFlags.ALL ); 
		_strokeInternal = (Stroke)_stroke.duplicate();
		float scale=Math.abs(
				Math.min(
						(getMeasuredWidth()-getPaddingLeft()-getPaddingRight())/_stroke.calculatedBounds.width(),
						(getMeasuredHeight()-getPaddingTop()-getPaddingBottom())/_stroke.calculatedBounds.height()
				)
		);
		PointF p = new PointF(_stroke.calculatedBounds.left*scale,_stroke.calculatedBounds.top*scale);
		_strokeInternal.update(false, agr, UpdateFlags.ALL);
		StrokeUtil.scaleAndTrans(_strokeInternal, scale, p, agr);
		ViewPortData vpd = ViewPortData.getFragmentViewPort(_strokeInternal);
		_strokeInternal.pen=_stroke.pen;
		_strokeInternal.fill=_stroke.fill;
		_strokeInternal.update(false, agr, UpdateFlags.ALL);
		//_strokeInternal.pen.strokeWidth*=vpd.zoom;
		//_strokeInternal.pen.glowWidth*=vpd.zoom;
		//_strokeInternal.pen.embRadius*=vpd.zoom;
		
		//vpd.zoom=1;
		//vpd.topLeft.set(-(_strokeInternal.calculatedBounds.left+getPaddingLeft()),-(_strokeInternal.calculatedBounds.top+getPaddingTop()));
		vpd.topLeft.set(vpd.topLeft.x-getPaddingLeft(),vpd.topLeft.y-getPaddingTop());
		Log.d(DVGlobals.LOG_TAG, vpd.zoom+" : "+PointUtil.tostr(_strokeInternal.calculatedBounds)+
				" : "+this._stroke.pen.strokeWidth+" : "+
				this._strokeInternal.pen.strokeWidth+":"+
				PointUtil.tostr(vpd.topLeft)
			);
		agr.setVpd(vpd);
		invalidate();
	}
	
	public void setPen(Pen pen) {
		this._stroke.pen=pen.duplicate();
		//this._stroke.update(false, agr, UpdateFlags.ALL);
		update();
		invalidate();
	}
	
	public void setFill(Fill fill) {
		this._stroke.fill = fill.duplicate();
		if (_stroke.fill.type==Fill.Type.BITMAP) {
			_stroke.fill.type=Fill.Type.NONE;
		}
		//this._stroke.update(false, agr, UpdateFlags.ALL);
		update();
		invalidate();
	}
	
	public Pen getPen() {
		return this._stroke.pen.duplicate();
	}
	
	public Fill getFill() {
		return this._stroke.fill.duplicate();
	}
	
	public AndGraphicsRenderer getRenderer() {
		return agr;
	}
}
