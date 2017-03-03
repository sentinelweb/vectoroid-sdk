package co.uk.sentinelweb.views.draw.overlay;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.Group;
import co.uk.sentinelweb.views.draw.model.Layer;
import co.uk.sentinelweb.views.draw.model.PointVec;
import co.uk.sentinelweb.views.draw.model.Stroke;
import co.uk.sentinelweb.views.draw.model.Stroke.Type;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Mode;
import co.uk.sentinelweb.views.draw.model.internal.OnDrawTouchListener;
import co.uk.sentinelweb.views.draw.model.internal.PointSelection;
import co.uk.sentinelweb.views.draw.model.internal.TouchData;
import co.uk.sentinelweb.views.draw.render.ag.StrokeRenderer;
import co.uk.sentinelweb.views.draw.util.BoundsUtil;
import co.uk.sentinelweb.views.draw.util.DispUtil;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class SelectionOverlay extends Overlay {
	public static final int MIN_DIST_SELECT = 15;// TODO add external config for this setting can be device dependent
	public static final int MAX_TOUCH_DOWN_TIME_SELECT = 400;
	
	public static final float MIN_DIST_POINT_DEF=10;
	public static final float MIN_DIST_CENTRE_DEF=30;
	public static final float MIN_DIST_TEXT_CTR_DEF=50;
	
	float _minDistPoint = MIN_DIST_POINT_DEF;
	float _minDistCentre = MIN_DIST_CENTRE_DEF;
	float _minDistTxtCentre = MIN_DIST_TEXT_CTR_DEF;
	
	public enum SelectMode {ALL,NONE,INVERT,DELETE};
	public ArrayList<DrawingElement> _selection = new ArrayList<DrawingElement>();
	public RectF _selectionBounds = new RectF();
	public PointF _selectionCentre = new PointF();
	public HashSet<PointSelection> _pointsSelection = new HashSet<PointSelection>(); // TODO change for multiple PointVec ,the indexes of selected points
	public Stroke pointsEditStroke = null;
	//RectF selectionRect = new RectF(1000f,1000f, 0f,0f);
	Paint selectPaint;
	StrokeRenderer sr ;//= new StrokeRenderer();
	Paint borderPaint;
	public RectF _selRect = new RectF(-1,-1,-1,-1);
	public RectF _useRect = new RectF(-1,-1,-1,-1);
	public ArrayList<DrawingElement> _tmpElementBuffer = new ArrayList<DrawingElement>();
	public SelectionOverlay(DrawView d) {
		super(d);
		selectPaint = new Paint();
		selectPaint.setARGB(255, 150, 150, 150);
		selectPaint.setStyle(Style.STROKE);
		selectPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));
		selectPaint.setStrokeWidth(1);
		
		borderPaint=new Paint();
		borderPaint.setARGB(128, 128, 128,128);
		borderPaint.setStrokeWidth(3);
		borderPaint.setStyle(Style.STROKE);
		sr = new StrokeRenderer(_drawView._renderer);//d._viewPort  //d.getContext(),
	}

	@Override
	public void draw(Canvas c) {// // RM rewrite to work on screen coords
		for (int i=0;i<_selection.size();i++) {
			DrawingElement de = _selection.get(i);
			if (de instanceof Stroke) {
				Stroke de2 = (Stroke) de;
				_useRect.set(_drawView._viewPort.data.zoomSrcRectF);
				if (BoundsUtil.checkBoundsIntersect(_useRect, de.calculatedBounds)) {
					selectPaint.setStrokeWidth(((Stroke) de).pen.strokeWidth/1.3f/* *_drawView._viewPort.data.zoom */);
					sr.renderSeleceted(c, de2, selectPaint);//, _drawView._renderer
				}
			} else if (de instanceof Group) {
				for (Stroke s : ((Group)de).getAllStrokes()) {
					_useRect.set(_drawView._viewPort.data.zoomSrcRectF);
					if (BoundsUtil.checkBoundsIntersect(_useRect, s.calculatedBounds)) {
						selectPaint.setStrokeWidth((s.pen.strokeWidth/1.3f));
						sr.renderSeleceted(c, s, selectPaint);//, _drawView._renderer
					}
				}
			}
		}
		borderPaint.setStrokeWidth(3*_density/_drawView._viewPort.data.zoom);
		if (	(_drawView._mode.opState==Mode.EDIT && _selection.size()>0) || 
				(_drawView._mode.opState==Mode.POINTS_SEL && _pointsSelection.size()>0)
			) 	{
			//c.drawRect( selectionBounds, borderPaint );
			_useRect.set(_selectionBounds);
			//PointUtil.translate(_useRect, _drawView._viewPort.data.topLeft,1);
			//PointUtil.scale(_useRect, _drawView._viewPort.data.zoom);
			//PointUtil.translate(_useRect, _drawView._viewPort.data.drawAreaTopLeft,1);
			c.drawRect( _useRect, borderPaint );
		}
		
		//Log.d(Globals.LOG_TAG, "selBounds:"+PointUtil.tostr(_selectionBounds));
		if (_drawView._mode.opState==Mode.SELRECT && _selRect.left!=-1) {
			//Log.d(Globals.LOG_TAG, "drawselection:selrect:"+PointUtil.tostr(selRect) );
			//c.drawRect( _selRect, borderPaint );
			_useRect.set(_selRect);
			//PointUtil.translate(_useRect, _drawView._viewPort.data.topLeft,1);
			//PointUtil.scale(_useRect, _drawView._viewPort.data.zoom);
			//PointUtil.translate(_useRect, _drawView._viewPort.data.drawAreaTopLeft,1);
			c.drawRect( _useRect, borderPaint );
		}
	}

	@Override
	public boolean onTouch(TouchData td) {
		if (_drawView._mode.opState==Mode.EDIT) {
			//if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "_selectionOverlay:"+td._a);
			switch (td._a ) {
				case ACTION_DOWN: 
					
					return false;
				case ACTION_MOVE: 
								
					return false;
				case ACTION_UP: 
					//Log.d(DVGlobals.LOG_TAG, "seoerlay.onTouch:selectConditions: "+selectConditions( td ));
					if ( selectConditions( td ) ) {
						return checkForSelectDeselect( td );
					}
					return false;
				default:return false;
			}
		}
		return false;
	}
	
	public boolean selectConditions(TouchData td) {
		//Log.d(Globals.LOG_TAG, "selectConditions:"+PointUtil.dist( td.referenceOnScreen, td.touchPointOnScreen ));
		float dist = PointUtil.dist( td._referenceOnScreen, td._touchPointOnScreen );
		boolean distSelect = dist < MIN_DIST_SELECT*DrawView._density;
		// timeSelect can screw up if drawing is big (update/move/draw times too large)
		// modify to use history events i.e. if there is only a few history events or none and they are in mindist then likely a tap. if there a lot of history events then likely a drag
		boolean timeSelect = (td.getTouchDownTime()) < MAX_TOUCH_DOWN_TIME_SELECT;//-_drawView._updateTimes[DrawView.UPDATE_UNDO]

		if (_drawView._isDebug) Log.d(DVGlobals.LOG_TAG, "selectConditions:d:"+distSelect+"("+dist+"<"+(MIN_DIST_SELECT*_drawView._density)+") t:"+timeSelect+"("+td.getTouchDownTime()+"<"+MAX_TOUCH_DOWN_TIME_SELECT+")");
		if ( timeSelect) {//distSelect ||  <-- rm test this since getTouchDownTime() modified to use MotionEvent.eventTime()
			return 	true;
		}
		return false;
	}
	
	public ArrayList<DrawingElement> getSelection() {
		return _selection;
	}
	public boolean isSelected(DrawingElement select) {
		return _selection.contains(select);
	}
	public ArrayList<Stroke> getStrokeSelection() {
		ArrayList<Stroke> strokes = new ArrayList<Stroke>();
		for (int i=0;i<_selection.size();i++) {
			DrawingElement de = _selection.get(i);
			if (de instanceof Stroke) {
				strokes.add((Stroke)de);
			}
		}
		return strokes;
	}
	public ArrayList<Stroke> getAllStrokes(ArrayList<DrawingElement> elements) {
		return getAllStrokes( elements , null);
	}
	
	public ArrayList<Stroke> getAllStrokes(ArrayList<DrawingElement> elements,List<Stroke.Type> types) {
		ArrayList<Stroke> strokes = new ArrayList<Stroke>();
		for (DrawingElement de : elements) {
			if (de instanceof Stroke) {
				Stroke de2 = (Stroke)de;
				if (types!=null) {
					if (types.contains(de2.type)) strokes.add(de2);
				} else {
					strokes.add(de2);
				}
			} else {
				strokes.addAll(getAllStrokes(((Group)de).elements,types));
			}
		}
		return strokes;
	}
	
	public int getSelectionCount() {
		return _selection.size();
	}
	
	public void selectElements(SelectMode selected) { 
		_pointsSelection.clear();
		if (currentLayerLocked()) {	return;}
		ArrayList<DrawingElement> newSelection = new ArrayList<DrawingElement>();
		ArrayList<DrawingElement> remove=new ArrayList<DrawingElement>();
		ArrayList<DrawingElement> els = _drawView.getCurrentLayer();
		for (int i=0;i<els.size();i++) {
			DrawingElement stroke = els.get(i);
			switch (selected) {
				case ALL:if (!stroke.locked) newSelection.add(stroke);break;
				case NONE:break;
				case INVERT:if (!_selection.contains(stroke) && !stroke.locked) newSelection.add(stroke);break;
				case DELETE:if (_selection.contains(stroke) && !stroke.locked) remove.add(stroke);break;
			}
		}
		_selection.clear();
		//selection.addAll(newSelection);
		for (DrawingElement de : newSelection) {
			_selection.add(de);
		}
		els.removeAll(remove);
		updateBounds();
		_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
		_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
		_drawView._updateFlags[DrawView.UPDATE_UNDO]=selected==SelectMode.DELETE;// TODO lowmem
		_drawView.updateDisplay();
		//if (selected==SelectMode.DELETE) {saveUndo();}
	}
	
	public void selectElement(DrawingElement select) {
		selectElementInternal(select);
		updateAfterSelect();
	}

	

	public void selectElementInternal(DrawingElement select) {
		if (select instanceof Layer) {return;}
		if (currentLayerLocked()) {	return;}
		if (_selection.contains(select) || select.locked) {
			_selection.remove(select);
		} else {
			_selection.add(select);
			if (_drawView.getOnUpdateListener()!=null) {
				_drawView.getOnUpdateListener().itemSelected(_drawView, select);
			}
		}
		updateBounds();
	}
	
	public void updateAfterSelect() {
		_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
		_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
		//_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;// TODO lowmem
		_drawView.updateDisplay();
	}
	
	public void clear() {
		_selection.clear();
		_pointsSelection.clear();
		pointsEditStroke=null;
	}
	
	public boolean isStroke(int i) {
		return _selection.size()>i && _selection.get(0) instanceof Stroke;
	}
	public Stroke getStroke(int i) {
		if ( isStroke(i)) { 
			return (Stroke) _selection.get(0);
		} return null;
	}
	public boolean isGroup(int i) {
		return _selection.size()>i && _selection.get(0) instanceof Group;
	}
	public Group getGroup(int i) {
		if ( isGroup(i)) { 
			return (Group) _selection.get(0);
		} return null;
	}
	private void getBoundsForPointsSel() {
		if (_pointsSelection.size()>0) {
			boolean first = true;
			for (int i=0;i<pointsEditStroke.points.size();i++) {
				if (_pointsSelection.contains((Integer)i)) {
					PointF p = pointsEditStroke.currentVec.get(i);
					if (first) {_selectionBounds.set(p.x,p.y,p.x,p.y);first=false;}
					else {
						_selectionBounds.left = (float)Math.min(_selectionBounds.left,p.x);
						_selectionBounds.top = (float)Math.min(_selectionBounds.top,p.y);
						_selectionBounds.right = (float)Math.max(_selectionBounds.right,p.x);
						_selectionBounds.bottom = (float)Math.max(_selectionBounds.bottom,p.y);
					}
				}
			}
		}
	}
	
	public void updateBounds() {
		Mode state = _drawView._mode.opState;
		if (state!=Mode.POINTS_SEL) {
			_drawView.getBoundsForElements( _selectionBounds,_selection); 
		} else {
			getBoundsForPointsSel();
		}
		// PointUtil.scale(selectionBounds, _drawView._viewPort.data.zoom);
		_selectionCentre = PointUtil.midpoint(_selectionBounds);
	}
	
	public PointF pointsMidPoint() {
		if (_drawView._mode.opState!=Mode.POINTS_SEL || pointsEditStroke==null) {return null;}
		Stroke selectedStroke = pointsEditStroke;
		PointF accum = new PointF();
		int ctr=0;
		// add comments back if crash here...
		for (PointSelection  i : _pointsSelection) {
			//Log.d(Globals.TAG, "pointsMidPoint():"+selectedStroke.points.size()+":"+i);
			PointF p=selectedStroke.points.get(i.strokeIndex).get(i.pointIndex);
			accum.x+=p.x;accum.y+=p.y;
			ctr++;
		}
		if (ctr!=0) {
			accum.x/=ctr;accum.y/=ctr;
			return accum;
		} else  return null;
	}
	
	public boolean checkForSelectDeselect(TouchData td) {
		ArrayList<DrawingElement> s = selectByTouch( td);
		//if (s!=null) {
		//	selectElement(s);
		//} 
		if (s.size()==1) {
			selectElement(s.get(0));
			return true;
		} else if (s.size()==0 && _selection.size()>0) {
			selectElements(SelectMode.NONE);
			return true;
		}
		return false;
	}
	
	private ArrayList<DrawingElement> selectByTouch(TouchData td) {
		// test point closest to a point in the stroke
		if (currentLayerLocked()) {	return null;}
		float maxDistCtrBounds = _minDistPoint*_density/_drawView._viewPort.data.zoom;
		float maxDistCtnrTxt = _minDistTxtCentre*_density/_drawView._viewPort.data.zoom;
		float maxDistCtnr = _minDistCentre*_density/_drawView._viewPort.data.zoom;
		float minDist = 1E6f;
		DrawingElement selectedElement = null;
		ArrayList<DrawingElement> els = _drawView.getCurrentLayer();
		_tmpElementBuffer.clear();
		for (DrawingElement de : els) {
			if (de.locked || !de.visible) {continue;}
			boolean selected = _selection.contains(de);
			boolean isText=false;
			if (de instanceof Stroke) {
				Stroke s = (Stroke)de;
				for (int j=0;j<s.points.size();j++) {
					PointVec pv = s.points.get(j);
					for (int i=0;i<pv.size();i++) {
						float dist = PointUtil.dist(pv.get(i), td._touchPoint);
						if (dist<maxDistCtrBounds && dist<minDist) {
							minDist=dist;
							selectedElement = s;
						}
					}
				}
				isText=s.type==Type.TEXT_TTF;
			}
			//Log.d(Globals.LOG_TAG, "selectByTouch:cog:"+PointUtil.tostr(de.calculatedCOG) +"("+PointUtil.dist( de.calculatedCOG, td.touchPoint ) +") cc:"+PointUtil.tostr(de.calculatedCentre)+"("+PointUtil.dist( de.calculatedCOG, td.touchPoint ) +") tp:"+PointUtil.tostr(td.touchPoint));
			if ( PointUtil.checkBounds( de.calculatedBounds , td._touchPoint )) {
				float dist = PointUtil.dist( de.calculatedCOG, td._touchPoint );
				if ( PointUtil.checkBounds( de.calculatedBounds , td._touchPoint ) && dist < minDist && dist<maxDistCtnr){
					minDist=dist;
					selectedElement =  de;
				}
				dist = PointUtil.dist( de.calculatedCentre, td._touchPoint );
				if ( PointUtil.checkBounds( de.calculatedBounds , td._touchPoint ) && dist < minDist  && dist<(isText?maxDistCtnrTxt:maxDistCtnr)) {
					minDist=dist;
					selectedElement =  de;
				}
				_tmpElementBuffer.add(de);
			}
		}
		if (selectedElement==null) {
			//if (_tmpELementBuffer.size()==1) {
			//	selectedElement=_tmpELementBuffer.get(0);
			//}
		} else {
			_tmpElementBuffer.clear();
			_tmpElementBuffer.add(selectedElement);
		}
		return _tmpElementBuffer;
	}

	private boolean currentLayerLocked() {
		return _drawView._currentLayer!=null && _drawView._currentLayer.locked;
	}
	
	public void selectByRect(RectF rect) {
		Log.d(DVGlobals.LOG_TAG, "selectByRect:"+PointUtil.tostr(rect));
		if (currentLayerLocked()) {
			return ;
		}
		for (DrawingElement de : _drawView.getCurrentLayer()) {
			if (!de.locked) {
				if (PointUtil.checkBounds(rect, de.calculatedBounds)) {
					if (!isSelected(de)  ) {
						_selection.add(de);
					}
				}
			}
		}
	}
	
	public OnDrawTouchListener selrectTouchListener = new OnDrawTouchListener() {
		@Override
		public boolean onTouch(TouchData td) {
			Log.d(DVGlobals.LOG_TAG, "selrectTouchListener");
			switch (td._a ) {
				case ACTION_DOWN: 
					_selRect.set(
							td._reference.x,td._reference.y,td._reference.x,td._reference.y
					);
					return true;
				case ACTION_MOVE:
					if (_selRect.left!=-1) {
						_selRect.set(
								Math.min(td._reference.x, td._touchPoint.x),
								Math.min(td._reference.y, td._touchPoint.y),
								Math.max(td._reference.x, td._touchPoint.x),
								Math.max(td._reference.y, td._touchPoint.y)
						);
						_drawView.updateDisplay();
						return true;
					}
					return false;
				case ACTION_UP:
					if (_selRect.left!=-1) {
						if (PointUtil.dist(td._referenceOnScreen,td._touchPointOnScreen)>SelectionOverlay.MIN_DIST_SELECT) {
							/*
							selRect.set(
									Math.min(td._reference.x, td._touchPoint.x),
									Math.min(td._reference.y, td._touchPoint.y),
									Math.max(td._reference.x, td._touchPoint.x),
									Math.max(td._reference.y, td._touchPoint.y)
									
							);
							*/
							selectByRect(_selRect);
						}
						_selRect.left=-1;
						_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
						_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
						_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
						_drawView._mode.reverseState();// calls 
						_drawView.updateDisplay();
						
						return true;
					}
					return false;
				case ACTION_MULTI_DOWN: 
					_selRect.set(
							Math.min(td._touchPoint2.x, td._touchPoint.x),Math.min(td._touchPoint2.y, td._touchPoint.y),
							Math.max(td._touchPoint2.x, td._touchPoint.x),Math.min(td._touchPoint2.y, td._touchPoint.y)
					);
					return true;
				case ACTION_MULTI_MOVE:
					_selRect.set(
							Math.min(td._touchPoint2.x, td._touchPoint.x),
							Math.min(td._touchPoint2.y, td._touchPoint.y),
							Math.max(td._touchPoint2.x, td._touchPoint.x),
							Math.max(td._touchPoint2.y, td._touchPoint.y)
					);
					_drawView.updateDisplay();
					return true;
				case ACTION_MULTI_UP:
					if (PointUtil.dist(td._referenceOnScreen,td._touchPointOnScreen)>SelectionOverlay.MIN_DIST_SELECT) {
						selectByRect(_selRect);
					}
					_selRect.left=-1;
					_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
					_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
					_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
					_drawView._mode.reverseState();// calls 
					_drawView.updateDisplay();
					return true;
				
				default : return false;
			}
		}
	};
}
