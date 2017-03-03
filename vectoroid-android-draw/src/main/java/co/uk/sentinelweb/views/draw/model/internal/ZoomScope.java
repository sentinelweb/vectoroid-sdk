package co.uk.sentinelweb.views.draw.model.internal;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import co.uk.sentinelweb.views.draw.util.DispUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class ZoomScope {
	private DrawView drawView;
	private float density = -1f;
	public float scopeSize=40;
	public RectF zoomScope = new RectF();
	public RectF zoomScopeViewArea = new RectF();
	public RectF zoomScopeBitmapArea = new RectF();
	public Rect zoomScopeSrcArea = new Rect();
	private PointF viewAreaSize = new PointF();
	Paint zoomToolPaint;
	Paint bmpPaint;
	Paint drawAreaPaint;
	
	
	public ZoomScope(DrawView d) {
		super();
		this.drawView = d;
		density = DispUtil.getDensity(d.getContext());
		drawAreaPaint=new Paint();
		drawAreaPaint.setARGB(128, 255, 255,255);
		drawAreaPaint.setStyle(Style.STROKE);
		drawAreaPaint.setTextSize(15*density);
		drawAreaPaint.setStrokeWidth(1);
		drawAreaPaint.setAntiAlias(true);
		zoomToolPaint=new Paint(drawAreaPaint);
		bmpPaint= new Paint();
		bmpPaint.setARGB(255, 0, 0,0);
		bmpPaint.setStyle(Style.FILL_AND_STROKE);
		scopeSize=80*density;
	}
	
	public void calculateLayout() {
		int dimensionWidth = (int)drawView._drawing.size.x;
		int dimensionHeight = (int)drawView._drawing.size.y;
		zoomScopeSrcArea.right=dimensionWidth;
		zoomScopeSrcArea.bottom=dimensionHeight;
		zoomScope.left = drawView.getMeasuredWidth()-scopeSize;
		zoomScope.top = 0;
		zoomScope.right = drawView.getMeasuredWidth();
		zoomScope.bottom=scopeSize;
		float scopeRatio = scopeSize/(float)drawView.getMeasuredWidth();
		zoomScopeBitmapArea.left = zoomScope.left-drawView._viewPort.data.minTopLeft.x*drawView._viewPort.data.minZoom*scopeRatio;
		zoomScopeBitmapArea.top = zoomScope.top-drawView._viewPort.data.minTopLeft.y*drawView._viewPort.data.minZoom*scopeRatio;
		zoomScopeBitmapArea.right = zoomScopeBitmapArea.left+dimensionWidth*drawView._viewPort.data.minZoom*scopeRatio;
		zoomScopeBitmapArea.bottom = zoomScopeBitmapArea.top+dimensionHeight*drawView._viewPort.data.minZoom*scopeRatio;
		updateScopeRects();
	}
	
	public void updateScopeRects(){
		int dimensionWidth = (int)drawView._drawing.size.x;
		int dimensionHeight = (int)drawView._drawing.size.y;
		int maxDimension = Math.max(dimensionHeight, dimensionWidth);
		maxDimension = Math.max(maxDimension, drawView.getMeasuredWidth());
		float viewAreaSize = scopeSize/(1+drawView._viewPort.data.zoom-drawView._viewPort.data.minZoom);
		zoomScopeViewArea.left = zoomScope.left+(drawView._viewPort.data.topLeft.x-drawView._viewPort.data.minTopLeft.x)/maxDimension*scopeSize;
		zoomScopeViewArea.top = (drawView._viewPort.data.topLeft.y-drawView._viewPort.data.minTopLeft.y)/maxDimension*scopeSize;
		zoomScopeViewArea.right = zoomScopeViewArea.left+viewAreaSize;
		zoomScopeViewArea.bottom = zoomScopeViewArea.top+viewAreaSize;
	}
	
	public void render(Canvas canvas,Bitmap drawBitmap) {
		canvas.drawRect(zoomScope, bmpPaint);
		canvas.drawRect(zoomScope, zoomToolPaint);
		canvas.drawBitmap(drawBitmap, this.zoomScopeSrcArea,this.zoomScopeBitmapArea,bmpPaint);
		canvas.drawRect(zoomScopeBitmapArea, zoomToolPaint);
		canvas.drawRect(zoomScopeViewArea, drawAreaPaint);
	}
}
