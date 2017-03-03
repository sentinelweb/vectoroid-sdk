package co.uk.sentinelweb.views.draw.model.internal;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import co.uk.sentinelweb.commonlibs.Globals;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.Stroke;
import co.uk.sentinelweb.views.draw.model.ViewPortData;
import co.uk.sentinelweb.views.draw.model.Stroke.Type;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Mode;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class ViewPort {
	private DrawView _drawView;
	public  ViewPortData data = new ViewPortData();
	//new PointF(-1,-1), -1, 1, 1,
	//new PointF(0,0), new PointF(0,0), new RectF(), new Rect(0,0,0,0),
	//new RectF(0f,0f,0f,0f), new Rect(0,0,0,0), new Rect(), new RectF(),
	//new PointF(0,0), new PointF(0,0)
	
	public PointF _topLeftTest = new PointF(0,0);
	
	private boolean _layoutCalculated = false;
	//private boolean _reCalcTopLeftRotate = false;
	
	RectF _useRect = new RectF();
	PointF _usePoint = new PointF();
	PointF _useMidPoint = new PointF();
	
	public ViewPort(DrawView d) {
		super();
		this._drawView = d;
	}
	
	public void setSrcAndDestRects() {
		// TODO modify for drawBitmap differences
		/*
		this.zoomSrcRect = new Rect(
				(int)topLeft.x,
				(int)topLeft.y,
				(int)(topLeft.x+getMeasuredWidth()/zoom),
				(int)(topLeft.y+getMeasuredHeight()/zoom)
			);
		this.zoomTgtRect = new Rect(0,0,getMeasuredWidth(),getMeasuredHeight());
		*/
		this.data.zoomSrcRectF.set(
				data.topLeft.x,
				data.topLeft.y,
				(data.topLeft.x+_drawView.getMeasuredWidth()/data.zoom),
				(data.topLeft.y+_drawView.getMeasuredHeight()/data.zoom)
			);
		this.data.zoomCullingRectF.set(this.data.zoomSrcRectF);
		if (_drawView._drawing!=null) {
		this.data.drawArea.set(
				(-data.topLeft.x)*data.zoom-2,
				(-data.topLeft.y)*data.zoom-2,
				(-data.topLeft.x+_drawView._drawing.size.x)*data.zoom+2,
				(-data.topLeft.y+_drawView._drawing.size.y)*data.zoom+2
		);
		} else {this.data.drawArea.set(0,0,1,1);}
		
		this.data.drawAreaTopLeft.set(this.data.drawArea.left, this.data.drawArea.top);
		float tgtLeftOffset = 0;
		float tgtTopOffset = 0;
		float tgtRightOffset = 0;
		float tgtBottomOffset = 0;
		if (data.zoomSrcRectF.left<0) {tgtLeftOffset=0f-data.zoomSrcRectF.left;data.zoomSrcRectF.left=0;}
		if (data.zoomSrcRectF.top<0) {tgtTopOffset=0f-data.zoomSrcRectF.top;data.zoomSrcRectF.top=0;}
		
		if (_drawView._drawing!=null) {
			if (data.zoomSrcRectF.right>_drawView._drawing.size.x) {
				tgtRightOffset=data.zoomSrcRectF.right-_drawView._drawing.size.x;
				data.zoomSrcRectF.right=_drawView._drawing.size.x;
			}
			if (data.zoomSrcRectF.bottom>_drawView._drawing.size.y) {
				tgtBottomOffset=data.zoomSrcRectF.bottom-_drawView._drawing.size.y;
				data.zoomSrcRectF.bottom=_drawView._drawing.size.y;
			}
		} else {
			data.zoomSrcRectF.right=data.zoomSrcRectF.left+1;
			data.zoomSrcRectF.bottom=data.zoomSrcRectF.top+1;
			tgtRightOffset=0;
			tgtBottomOffset=0;
		}
		
		this.data.zoomSrcRect.set((int)data.zoomSrcRectF.left,(int)data.zoomSrcRectF.top,(int)data.zoomSrcRectF.right,(int)data.zoomSrcRectF.bottom);
		
		this.data.zoomTgtRect.set(
				(int)(tgtLeftOffset*data.zoom),
				(int)(tgtTopOffset*data.zoom),
				(int)(_drawView.getMeasuredWidth()-tgtRightOffset*data.zoom),
				(int)(_drawView.getMeasuredHeight()-tgtBottomOffset*data.zoom)
			);
		
		//Log.d(Globals.TAG, "setSrcAndDestRects():src:"+tostr(new RectF(this.zoomSrcRect)) +" tgt:"+tostr(new RectF(this.zoomTgtRect))+": zoom:"+zoom+": dims:"+dimensionWidth+"x"+dimensionHeight);
	}
	public  void resetLayout() {
		_layoutCalculated=false;
		data.topLeft.set(-1,-1);
		data.zoom=-1;
	}
	public  void reCalcLayout() {//boolean fromRotate
		//_reCalcTopLeftRotate = fromRotate;
		_layoutCalculated = false;
	}
	
	/**
	 * @return the layoutCalculated
	 */
	public boolean isLayoutCalculated() {
		return _layoutCalculated;
	}
	
	public  void calculateLayout() {
		_usePoint.set(data.topLeft);
		int dimensionWidth = (int)_drawView._drawing.size.x;
		int dimensionHeight = (int)_drawView._drawing.size.y;
		if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG,"calculateLayout()"+dimensionWidth+" x "+dimensionHeight +" measured:"+_drawView.getMeasuredWidth()+" x "+_drawView.getMeasuredHeight());
		float minZoomx = _drawView.getMeasuredWidth()/(float)dimensionWidth; 
		float minZoomy = _drawView.getMeasuredHeight()/(float)dimensionHeight;
		data.minZoom = Math.min(minZoomx, minZoomy);
		data.minZoom *=0.5;
		//data.minZoom = Math.min(data.minZoom, 1);
		//data.minZoom = Math.min(data.minZoom, 0.1f); 
		
		// rm : 111011: since minZoom has been set to small (0.1) just make minTopLeft calculate all the time.
		//if (dimensionWidth>drawView.getMeasuredWidth()){  
		//	data.minTopLeft.x = 0;
		//} else {
			data.minTopLeft.x = ((dimensionWidth-_drawView.getMeasuredWidth()/data.minZoom)/2);
		//}
		//if (dimensionHeight>drawView.getMeasuredHeight()){
		//	data.minTopLeft.y = 0;
		//} else {
			data.minTopLeft.y = ((dimensionHeight-_drawView.getMeasuredHeight()/data.minZoom)/2);
		//}
		/*
		if (dimensionWidth>drawView.getMeasuredWidth() && dimensionHeight>drawView.getMeasuredHeight()){  
			//if (dimensionAspect>1) {
				data.minTopLeft.y=((dimensionHeight-drawView.getMeasuredHeight()/data.minZoom)/2);
			//} else {
				data.minTopLeft.x = ((dimensionWidth-drawView.getMeasuredWidth()/data.minZoom)/2);
			//}
		}
		*/
		data.maxBottomRight.x=data.minTopLeft.x+_drawView.getMeasuredWidth()/data.minZoom;
		data.maxBottomRight.y=data.minTopLeft.y+_drawView.getMeasuredHeight()/data.minZoom;
		if (data.zoom==-1 || data.zoom<data.minZoom) {data.zoom=data.minZoom;}
		
		
		
		if (data.topLeft.x==-1 && data.topLeft.y==-1) {
			data.topLeft.set(data.minTopLeft);
		} else {
			if (data.screenWidthHeightReference.x>0) {
				boolean isLand = _drawView.getMeasuredWidth()> _drawView.getMeasuredHeight();
				boolean wasLand = data.screenWidthHeightReference.x>data.screenWidthHeightReference.y;
				//Log.d(DVGlobals.LOG_TAG,"calcLayout:"+isLand+":"+wasLand);
				//if (reCalcTopLeftRotate) {
					//Log.d(DVGlobals.LOG_TAG,"reCalcTopLeftRotate:");
	//				float minsize = Math.min(drawView.getMeasuredWidth(), drawView.getMeasuredHeight());
	//				float maxSize = Math.max(drawView.getMeasuredWidth(), drawView.getMeasuredHeight());
	//				float index = (maxSize-minsize)/2f;
	//				boolean wasPortrait = data.widthHeightReference.y>data.widthHeightReference.x;
	//				boolean isPortrait =  drawView.getMeasuredHeight()>drawView.getMeasuredWidth();
	//				if (wasPortrait && !isPortrait) {// chnanged landscape
	//					data.topLeft.set(usePoint.x-index/data.zoom, usePoint.y+index/data.zoom);
	//				} else if (!wasPortrait && isPortrait) {
	//					data.topLeft.set(usePoint.x+index/data.zoom, usePoint.y-index/data.zoom);
	//				}
					if (wasLand != isLand) {
						_usePoint.set((_drawView.getMeasuredWidth()-data.screenWidthHeightReference.x)/-2f/data.zoom,(_drawView.getMeasuredHeight()-data.screenWidthHeightReference.y)/-2f/data.zoom);
						//Log.d(DVGlobals.LOG_TAG, "draw:rot:"+PointUtil.tostr(_usePoint));
						PointUtil.addVector(data.topLeft, data.topLeft, _usePoint);
						//Log.d(DVGlobals.LOG_TAG,"calc rotate:"+PointUtil.tostr(_usePoint)+": tl:"+PointUtil.tostr(data.topLeft)+": zoom:"+data.zoom);
					} //else {
					//	data.topLeft.set(usePoint);
					//}
			}
			
		}
		data.screenWidthHeightReference.set(_drawView.getMeasuredWidth(), _drawView.getMeasuredHeight());
		startZooming();
		setZoom(data.zoom);
		data.topLeftReference.set(data.topLeft);
		_drawView._updateFlags[DrawView.UPDATE_SCOPE]=true;
		_layoutCalculated = true;
		//data.widthHeightReference.set(drawView.getMeasuredWidth(), drawView.getMeasuredHeight());
		if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "calculateLayout():tl:"+PointUtil.tostr(data.topLeft) +" mintl:"+PointUtil.tostr(data.topLeftReference));//+" minZoom:"+minZoom+" dw:"+dimensionWidth+" dh:"+dimensionHeight+" da:"+dimensionAspect +" mzx:"+minZoomx+" mzy:"+minZoomy+" ww:"+getMeasuredWidth()+" wh:"+getMeasuredHeight()
	}
	
	public void multiTouchPanAndZoom(TouchData td) {
		td._sme.midPoint(_useMidPoint, td._e);
		data.topLeft.x = data.topLeftReference.x+(td._referenceMidpoint.x-_useMidPoint.x)/data.zoom;
		data.topLeft.y = data.topLeftReference.y+(td._referenceMidpoint.y-_useMidPoint.y)/data.zoom;
		float tspc = td._sme.spacing(td._touchPointOnScreen,td._touchPointOnScreen2);
		float rspc = td._referenceSpacing;
		this.data.zoom = Math.max(data.minZoom, data.referenceZoom*tspc/rspc);
		
		//topLeftReference.set(topLeft);
		_topLeftTest.set(data.topLeft);// stop tl ref being modified
		_usePoint.set(_drawView.getMeasuredWidth(), _drawView.getMeasuredHeight());
		PointUtil.mulVector(_usePoint, _usePoint, 1/data.zoom);
		PointUtil.subVector(data.widthHeightReference, _usePoint, _usePoint);// new win size
		_useMidPoint.set(_useMidPoint.x/_drawView.getMeasuredWidth(),_useMidPoint.y/_drawView.getMeasuredHeight());// ratio of midpoint to screen dims - destroys useMidPoint!
		PointUtil.mulVector(_usePoint, _usePoint, _useMidPoint);// centering offset - rm 091011: this is not a vector multiply
		PointUtil.addVector(checkPanningBounds(data.zoom,_topLeftTest), _topLeftTest, _usePoint);
		// if (zoom<referenceZoom) {
			_topLeftTest = checkPanningBounds(data.zoom,_topLeftTest);
		// }
		data.topLeft.set(_topLeftTest);//TODO stop checkPanningBounds kicking
		
		//drawView._updateFlags[DrawView.UPDATE_ZOOM]=true;
		setSrcAndDestRects();
		//drawView._updateFlags[DrawView.UPDATE_SCOPE]=true;
		//drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
		//drawView.updateDisplay();
		_drawView.updateInternal();
	}
	
	public void startZooming() {
		data.referenceZoom=data.zoom;
		data.topLeftReference.set(data.topLeft);
		data.widthHeightReference.set(_drawView.getMeasuredWidth(), _drawView.getMeasuredHeight());
		PointUtil.mulVector(data.widthHeightReference, data.widthHeightReference, 1/data.zoom);
	}
	// NOTE: this zoom centres in the screen
	public void setZoom(float zoom) {
		this.data.zoom = zoom;
		_topLeftTest.set(data.topLeftReference);// stop tl ref being modified
		_usePoint.set(_drawView.getMeasuredWidth(), _drawView.getMeasuredHeight());
		PointUtil.mulVector(_usePoint, _usePoint, 1/zoom);
		PointUtil.subVector(data.widthHeightReference, _usePoint, _usePoint);// new win size
		PointUtil.mulVector(_usePoint, _usePoint, 0.5f);// centering offset
		PointUtil.addVector(checkPanningBounds(zoom,_topLeftTest), _topLeftTest, _usePoint);
		// if (zoom<referenceZoom) {
			_topLeftTest = checkPanningBounds(zoom,_topLeftTest);
		// }
		data.topLeft.set(_topLeftTest);//TODO stop checkPanningBounds kicking
		setSrcAndDestRects();
	}
	
	public void doZoom(float newZoom,boolean finished){//PointF touchPointOnScreen
		if (finished) { 
			_drawView.dropTouchCache(); 
		}
		setZoom(newZoom);
		if (finished) {
			data.referenceZoom=data.zoom;
			data.topLeftReference.set(data.topLeft);
		}
		_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
		_drawView._updateFlags[DrawView.UPDATE_SCOPE]=true;
		_drawView._updateFlags[DrawView.UPDATE_ZOOM]=true;
		_drawView.updateDisplay();
	}
	// WARNING: this modifies topLeft
	private PointF checkPanningBounds(float zoom, PointF topLeft) {
		int dimensionWidth = (int)_drawView._drawing.size.x; 
		int dimensionHeight = (int)_drawView._drawing.size.y;
		if (topLeft.x+_drawView.getMeasuredWidth()/zoom>dimensionWidth-data.minTopLeft.x) {topLeft.x=dimensionWidth-data.minTopLeft.x-_drawView.getMeasuredWidth()/zoom;};
		if (topLeft.y+_drawView.getMeasuredHeight()/zoom>dimensionHeight-data.minTopLeft.y) {topLeft.y=dimensionHeight-data.minTopLeft.y-_drawView.getMeasuredHeight()/zoom;};
		if (topLeft.x<data.minTopLeft.x) {topLeft.x=data.minTopLeft.x;	}
		if (topLeft.y<data.minTopLeft.y) {topLeft.y=data.minTopLeft.y;	}
		//Log.d(Globals.TAG, "checkPanningBounds():tl:"+tostr(topLeft) +" mintl:"+tostr(minTopLeft));
		return topLeft;
	}
	
	public void doPan(TouchData td) {
		//TODO prob here. panning kicks when dist from page edge the view edge is high
		data.topLeft.x = data.topLeftReference.x+(td._referenceOnScreen.x-td._touchPointOnScreen.x)/data.zoom;
		data.topLeft.y = data.topLeftReference.y+(td._referenceOnScreen.y-td._touchPointOnScreen.y)/data.zoom;
		data.topLeft = checkPanningBounds(data.zoom,data.topLeft);
		setSrcAndDestRects();
		//updateScopeRects();
		//updateSelectionIconRects();
		//drawView.updateFlags[DrawView.UPDATE_ANDCHOR]=true;
		_drawView._updateFlags[DrawView.UPDATE_SCOPE]=true;
		_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
	}
	
	public boolean panKeyPad(int horiz,int vert) {
		PointF result = new PointF();
		result.set(data.topLeft.x+horiz,data.topLeft.y+vert);
		_topLeftTest.set(result);
		checkPanningBounds(data.zoom, result);
		//Log.d(Globals.TAG, "panKeyPad:"+tostr(topLeftTest)+":"+tostr(result));
		if (result.x!=_topLeftTest.x && result.y!=_topLeftTest.y) {return false;}
		else {
			data.topLeft.set(result);
			setSrcAndDestRects();
			_drawView._updateFlags[DrawView.UPDATE_SCOPE]=true;
			_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
			_drawView.updateInternal();
			return true;
		}
	}
	
	public void startPanning() {
		_drawView._mode.setReversibleState(Mode.PAN);
		//setModeText();
		data.topLeftReference.set(data.topLeft);
		data.referenceZoom=data.zoom;
	}
	
	public void finishPanning() {
		//opState=lastOpState;
		//setModeText();
		data.topLeftReference.set(data.topLeft);
		data.referenceZoom=data.zoom;

	}
	
	public void cancelPanning() {
		data.topLeft.set(data.topLeftReference);
		//opState=lastOpState;
		//setModeText();
		_drawView._mode.reverseState();
	}
	// TODO not working :(
	public void setViewPortByElement(DrawingElement element,int margin) {
		element.update(true,_drawView._renderer,UpdateFlags.BOUNDSONLY);
		if (element instanceof Stroke && ((Stroke)element).type==Type.TEXT_TTF) {
			element.update(true,_drawView._renderer,UpdateFlags.PATHONLY);
		}
		setViewPortByBounds (element.calculatedBounds,margin);
	}
	public void setViewPortByElements(ArrayList<DrawingElement> elements,int margin) {
		if (elements.size()==0) {return;}
		RectF calculatedBounds = new RectF(1e8f,1e8f,-1e8f,-1e8f);
		//float ctr=0;
		for (DrawingElement de : elements) {
			de.update(true,_drawView._renderer,UpdateFlags.BOUNDSONLY);
			calculatedBounds.top=Math.min(calculatedBounds.top, de.calculatedBounds.top);
			calculatedBounds.left=Math.min(calculatedBounds.left, de.calculatedBounds.left);
			calculatedBounds.bottom=Math.max(calculatedBounds.bottom, de.calculatedBounds.bottom);
			calculatedBounds.right=Math.max(calculatedBounds.right, de.calculatedBounds.right);
			//ctr++;
		}
		setViewPortByBounds(calculatedBounds,margin);
	}

	public void setViewPortByBounds(RectF calculatedBounds,int margin) {
		int height = _drawView.getMeasuredHeight();
		//height=height/3;
		//if (height==devheight) {height/=3;}
		Log.d(DVGlobals.LOG_TAG, "setViewPortByBounds+"+PointUtil.tostr(calculatedBounds)+" : "+calculatedBounds.width()+"x"+calculatedBounds.height()+" measured:"+_drawView.getMeasuredWidth()+"x"+height);
		float newZoom = Math.min(
				(_drawView.getMeasuredWidth())/(calculatedBounds.width()+margin*2), 
				height/(calculatedBounds.height()+margin*2)
			);
		data.topLeft.set(calculatedBounds.left-margin/newZoom,calculatedBounds.top-margin/newZoom);//-margin
		
		this.data.zoom = newZoom;
		data.referenceZoom=data.zoom;
		data.topLeftReference.set(data.topLeft);
		setSrcAndDestRects();
		_drawView.dropTouchCache(); 
		_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
		_drawView._updateFlags[DrawView.UPDATE_SCOPE]=true;
		_drawView._updateFlags[DrawView.UPDATE_ZOOM]=true;
		_drawView.updateDisplay();
	}
}
