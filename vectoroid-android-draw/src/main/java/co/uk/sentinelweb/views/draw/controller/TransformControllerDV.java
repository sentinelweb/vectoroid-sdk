package co.uk.sentinelweb.views.draw.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWClickListener;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWDialogClickListener;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.model.Drawing;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.Fill;
import co.uk.sentinelweb.views.draw.model.Group;
import co.uk.sentinelweb.views.draw.model.PointVec;
import co.uk.sentinelweb.views.draw.model.Stroke;
import co.uk.sentinelweb.views.draw.model.TransformOperatorInOut;
import co.uk.sentinelweb.views.draw.model.TransformOperatorInOut.Axis;
import co.uk.sentinelweb.views.draw.model.TransformOperatorInOut.Trans;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Mode;
import co.uk.sentinelweb.views.draw.model.internal.PointSelection;
import co.uk.sentinelweb.views.draw.model.internal.TouchData;
import co.uk.sentinelweb.views.draw.render.ag.StrokeRenderObject;
import co.uk.sentinelweb.views.draw.util.DispUtil;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class TransformControllerDV extends TransformController{
	/*
	 * Notes:
	 * - this works on the selection only at the moment ...
	 * - for points edit it only works for a stroke too.
	 * - modify for point doesnt work yet. will need to lookup by strokeIndex pointIndex...
	 */
	float _density = -1;
	DrawView _drawView ;
	public ArrayList<DrawingElement> strokesTmp;
	ArrayList<Integer> strokesTmpIndexes;
	RectF refBounds;
	PointF refCOG =  new PointF();
	PointF negRefCOG =  new PointF();
	
	PointF usePoint =  new PointF();
	
	boolean distReverse = false;
	boolean distNoReorder = true;

	ArrayList<Stroke> processVec = new ArrayList<Stroke>();
	ArrayList<Stroke> processVecTmp = new ArrayList<Stroke>();
	
	ArrayList<PointF> extraProcessPts = new ArrayList<PointF>();
	ArrayList<PointF> extraProcessPtsTmp = new ArrayList<PointF>();
	
	Dialog alignDialog = null;
	Dialog flipDialog = null;
	Dialog rotateDialog = null;
	
	UpdateFlags noFillBitmapUpdFlags ;
	UpdateFlags fillGradientOnlyUpdFlags;
	public TransformControllerDV(DrawView d) {
		super();
		if (_density==-1) {_density=DispUtil.getDensity(d.getContext());	}
		this._drawView = d;
		noFillBitmapUpdFlags = UpdateFlags.ALL.copy();
		noFillBitmapUpdFlags.fillTypes.remove(Fill.Type.BITMAP);
		//noPathAandFillBitmapUpdFlags.updateTypes.remove(UpdateType.PATH);
		
		fillGradientOnlyUpdFlags = UpdateFlags.ALL.copy();
		fillGradientOnlyUpdFlags.fillTypes.clear();
		fillGradientOnlyUpdFlags.fillTypes.add(Fill.Type.GRADIENT);
	}

	public DrawView getDrawView() {
		return _drawView;
	}

	public void setDrawView(DrawView d) {
		this._drawView = d;
	}

	public void makeTmpSelection(boolean copyBmpFill) {
		strokesTmp=new ArrayList<DrawingElement>();
		strokesTmpIndexes=new ArrayList<Integer>();
		for (DrawingElement de : _drawView._selectionOverlay._selection) {
			DrawingElement d1 = de.duplicate();
			d1.update( true, _drawView._renderer, noFillBitmapUpdFlags) ;
			if (copyBmpFill) {// RM: 131011: could also pass through the matrix (in modifyTmpSel) to the renderobject for rotate and skew bitmaps.
				if (de instanceof Stroke && ((Stroke)de).fill.type==co.uk.sentinelweb.views.draw.model.Fill.Type.BITMAP) {
					StrokeRenderObject sro  = (StrokeRenderObject)_drawView._renderer.getObject(de);
					StrokeRenderObject sro1  = (StrokeRenderObject)_drawView._renderer.getObject(d1);
					sro1.bitmapMask = sro.bitmapMask;
				}
			}
			strokesTmp.add(d1);
			int indexOf = _drawView.getCurrentLayer().indexOf(de);
			//Log.d(DVGlobals.LOG_TAG, "makeTmpSelection():strokeIdx:"+indexOf+":"+_drawView.getCurrentLayer().hashCode()+" = "+_drawView._drawing.hashCode());
			strokesTmpIndexes.add(indexOf);
		}
		Log.d(DVGlobals.LOG_TAG, "makeTmpSelection():b4:"+_drawView.getCurrentLayerIdx());
		_drawView._anchorOverlay.updateAnchor(); 
		refBounds = new RectF();
		_drawView.getBoundsForElements(refBounds, strokesTmp);
		refCOG = PointUtil.midpoint(refBounds);
		negRefCOG = new PointF(-refCOG.x,-refCOG.y);
	}
	
	public void dropTmpSelection() {  
		_drawView._renderer.removeFromCache(strokesTmp);
		strokesTmpIndexes=null;
		strokesTmp=null;
	}

	public void commitTmpSelection() {
		if (strokesTmp!=null) {
			ArrayList<DrawingElement> strokes = _drawView.getCurrentLayer();
			for (int i=0;i<strokesTmpIndexes.size();i++) {
				DrawingElement stroke = strokesTmp.get(i);
				// this isnt needed (yet?)...
				if (stroke instanceof Stroke && strokes.get(strokesTmpIndexes.get(i))==_drawView._selectionOverlay.pointsEditStroke) {
					_drawView._selectionOverlay.pointsEditStroke = (Stroke)stroke;
				}
				DrawingElement oldStroke = strokes.get( strokesTmpIndexes.get(i));
				_drawView._renderer.removeFromCache(oldStroke);
				//Log.d(Globals.LOG_TAG,""+_drawView._renderer.renderObjects.containsKey(oldStroke));
				strokes.set(strokesTmpIndexes.get(i), stroke);
				stroke.update(true, _drawView._renderer, UpdateFlags.ALL);
			}
			_drawView._selectionOverlay._selection.clear();
			for (DrawingElement de : strokesTmp) {
				_drawView._selectionOverlay._selection.add(de);
			}
			Log.d(DVGlobals.LOG_TAG, "commitTmpSelection():after:"+strokesTmp.size()+":"+_drawView._selectionOverlay._selection.size());
		}
		
		strokesTmpIndexes=null;
		strokesTmp=null;
	}
	
	public TransformOperatorInOut modifyTmpSelection(TouchData td, Trans modType, Axis controlAxis, boolean points,boolean fine) {//PointF touchPoint,PointF touchPointOnScreen
		if (strokesTmp==null) return null;
		TransformOperatorInOut t = getTransformOperatorSingle( td,  modType,  controlAxis,  points, fine);
		transform(t);
		return t;
	}

	public TransformOperatorInOut getTransformOperatorSingle(TouchData td, Trans modType, Axis controlAxis, boolean points,boolean fine) {
		//////////// MATRIX SETUP ////////////////////////////////////////////
		HashSet<PointSelection> pointsSelection = _drawView._selectionOverlay._pointsSelection;
		float zoom = _drawView._viewPort.data.zoom;
		PointF anchor = _drawView._anchorOverlay.anchor;
		PointF trans=null;// calculated translation for pivot;
		float fineDivisor = fine?20:1;
		TransformOperatorInOut t = new TransformOperatorInOut();
		t.ops.add(modType);
		t.axis=controlAxis;
		t.anchor.set(anchor);
		if (modType==Trans.MOVE ) {
			trans  = new PointF((td._touchPointOnScreen.x-td._referenceOnScreen.x)/zoom/fineDivisor,(td._touchPointOnScreen.y-td._referenceOnScreen.y)/zoom/fineDivisor); 

			t.trans.set(trans);
		} else if (modType==Trans.ROTATE ) {
			usePoint.set(anchor.x,anchor.y-10);
			double intitalAngle = PointUtil.calcAngle(td._reference, anchor, usePoint);
			double touchAngle = PointUtil.calcAngle(td._touchPoint, anchor, usePoint);
			touchAngle=((anchor.x-td._touchPoint.x<0 && anchor.x-td._reference.x>0)||
						(anchor.x-td._touchPoint.x>0 && anchor.x-td._reference.x<0))?2*Math.PI-touchAngle:touchAngle;
			double rads = (intitalAngle-touchAngle)/fineDivisor ;
			t.rotateValue=rads;
		} else if (modType==Trans.SCALE ) {
			
			double xFactor = (td._touchPointOnScreen.x/td._referenceOnScreen.x);
			double yFactor = (td._touchPointOnScreen.y/td._referenceOnScreen.y);
			if (fine) {
				xFactor=1+(xFactor-1)/fineDivisor;
				yFactor=1+(yFactor-1)/fineDivisor;
			}
			switch (controlAxis) {
				case X:yFactor=1;break;
				case Y:xFactor=1;break;
				case BOTH:break;
				case PRESERVE_ASPECT:yFactor=yFactor/2+xFactor/2;xFactor=yFactor; break;
			}
			double scaleValue = (xFactor==1||yFactor==1)?1:(float)(xFactor+yFactor)/2f;
			t.scaleValue=scaleValue;
			t.scaleXValue=xFactor;
			t.scaleYValue=yFactor;
			//Log.d(Globals.TAG, "scale:tch:"+tostr(touchPoint)+": ref:"+tostr(reference)+": scale:"+touchPoint.x/reference.x+":"+touchPoint.y/reference.y );
		}else if (modType==Trans.SHEAR ) {//|| controlAxis==STATE_SHEAR
			double xFactor = (td._referenceOnScreen.x-td._touchPointOnScreen.x)/td._referenceOnScreen.x*_density;
			double yFactor = (td._referenceOnScreen.y-td._touchPointOnScreen.y)/td._referenceOnScreen.y*_density;
			switch (controlAxis) {
				case X:yFactor=0;break;
				case Y:xFactor=0;break;
				case BOTH:break;
				case PRESERVE_ASPECT:yFactor=Math.max(xFactor, yFactor);xFactor=Math.max(xFactor, yFactor); break;
			}
			t.skewXValue = xFactor;
			t.skewYValue = yFactor;
		}else if (modType==Trans.PROJ ) {//|| controlAxis==STATE_PROJ
			// this doesnt work
			/*
			PointF vec = new PointF(
					(td._touchPointOnScreen.x-anchor.x)/anchor.x,
					(td._touchPointOnScreen.y-anchor.y)/anchor.y
				);
			float norm = PointUtil.norm(vec);
			float invNormSq = 1/(norm*norm); 
			matrix[0][0] = invNormSq*vec.x*vec.x;
			matrix[0][1] = invNormSq*vec.x*vec.y;
			matrix[1][0] = invNormSq*vec.x*vec.y;
			matrix[1][1] = invNormSq*vec.y*vec.y;
			//trans=new PointF((float)-matrix[0][0],(float)-matrix[1][1]);
			PointF transt = trans;// THIS WILL BE NULL;
			double[][] matrixt=matrix;
			t= new TransformOperatorInOut() {
				@Override
				public void operate(PointF pin, PointF pout) {
					PointUtil.subVector(pin, pout, cog);
					PointUtil.mulMatrix(pout, pout, matrix);
					PointUtil.addVector(pout, pout, cog);
					PointUtil.addVector(pout, pout, transOut);
				}
			};
			t.transOut.set(transt);
			t.cog.set(refCOG);
			t.matrix=matrixt;
			*/
			//Log.d(Globals.TAG, "scale:tch:"+tostr(touchPoint)+": ref:"+tostr(reference)+": scale:"+touchPoint.x/reference.x+":"+touchPoint.y/reference.y );
		}
		//////////// TRANSORMING ////////////////////////////////////////////
		t.generate();
		return t;
	}
	
	public TransformOperatorInOut modifyTmpSelectionMultiTouch(TouchData td, boolean points,HashSet<Trans> fix,Axis fixAxis,boolean fine) {
		if (strokesTmp==null) {return null;}
		TransformOperatorInOut t = getTransformOperatorMulti( td,  points, fix, fixAxis, fine);
		transform(t);
		return t;
	}
	public void modifyTmpSelectionMultiTouch(TransformOperatorInOut t) {
		//if (strokesTmp==null) {return null;}
		//TransformOperatorInOut t = getTransformOperatorMulti( td,  points, fix, fixAxis, fine);
		transform(t);
		//return t;
	}
	public TransformOperatorInOut getTransformOperatorMulti(TouchData td, boolean points,HashSet<Trans> fix,Axis fixAxis,boolean fine) {
		float zoom = _drawView._viewPort.data.zoom;
		TransformOperatorInOut t=null;
		PointF move=new PointF(0,0);
		PointF anchor = _drawView._anchorOverlay.anchor;
		float fineDivisor = fine?20:1;
		t = new TransformOperatorInOut() ;
		t.anchor.set(anchor);
		if ( fix.contains(Trans.ROTATE)) {
			float angle = td._sme.angle(td._e)-td._referenceAngle;
			double rads = angle*Math.PI/180/fineDivisor;
			t.rotateValue=rads;
		}
		if ( fix.contains(Trans.SCALE)) {
			float scale =  td._sme.spacing(td._touchPointOnScreen,td._touchPointOnScreen2)/td._referenceSpacing;
			float rexfDistx = Math.abs(td._referenceOnScreen.x-td._referenceOnScreen2.x);
			float rexfDisty = Math.abs(td._referenceOnScreen.y-td._referenceOnScreen2.y);
			float touchDistx = Math.abs(td._touchPointOnScreen.x-td._touchPointOnScreen2.x);
			float touchDisty = Math.abs(td._touchPointOnScreen.y-td._touchPointOnScreen2.y);
			double xFactor =  touchDistx/rexfDistx;
			double yFactor =  touchDisty/rexfDisty;
			if (fine) {
				xFactor=1+(xFactor-1)/fineDivisor;
				yFactor=1+(yFactor-1)/fineDivisor;
				scale = 1+(scale-1)/fineDivisor;
			}
			switch (fixAxis) {
				case X:
					yFactor=1;
					break;
				case Y:
					xFactor=1;
					break;
				case PRESERVE_ASPECT:
					xFactor=scale;
					yFactor=scale;
					break;
			
			}
			t.scaleValue =  (xFactor==1||yFactor==1)?1:(float)(xFactor+yFactor)/2f;
			t.scaleXValue = xFactor;
			t.scaleYValue = yFactor;
		}
		if (fix.contains(Trans.SHEAR)) {
			boolean reversex = (	(td._referenceOnScreen.x<td._referenceOnScreen2.x && td._referenceOnScreen.y<td._referenceOnScreen2.y)||
									(td._referenceOnScreen.x>td._referenceOnScreen2.x && td._referenceOnScreen.y<td._referenceOnScreen2.y)
								);
			boolean reversey = (	(td._referenceOnScreen.x<td._referenceOnScreen2.x && td._referenceOnScreen.y<td._referenceOnScreen2.y)||
							(td._referenceOnScreen.x<td._referenceOnScreen2.x && td._referenceOnScreen.y>td._referenceOnScreen2.y)
				);
			float touchDistx1 = (td._touchPointOnScreen.x-td._referenceOnScreen.x);
			float touchDistx2 = (td._touchPointOnScreen2.x-td._referenceOnScreen2.x);
			float touchDisty1 = (td._touchPointOnScreen.y-td._referenceOnScreen.y);
			float touchDisty2 = (td._touchPointOnScreen2.y-td._referenceOnScreen2.y);
			float shearx = ((touchDistx1-touchDistx2))/100*_density*(reversex?-1:1)/fineDivisor;//*(dirx^diry?1:-1);///rexfDisty   //Math.abs(rexfDistx)/
			float sheary = ((touchDisty1-touchDisty2))/100*_density*(reversey?-1:1)/fineDivisor;//*(dirx^diry?1:-1);///rexfDistx   //Math.abs(rexfDisty)/
			
			double xFactor = shearx;
			double yFactor = sheary;
			switch (fixAxis) {
				case X:
					xFactor=shearx;
					yFactor=0;
					break;
				case Y:
					xFactor=0;
					yFactor=sheary;
					break;
				case PRESERVE_ASPECT:
					xFactor=Math.max(shearx,sheary);
					yFactor=Math.max(shearx,sheary);
					break;
			
			}
			t.skewXValue = xFactor;
			t.skewYValue = yFactor;
		}
		if (fix.contains(Trans.MOVE)) {
			td._sme.midPoint(usePoint, td._e);
			float mx = usePoint.x-td._referenceMidpoint.x;
			float my = usePoint.y-td._referenceMidpoint.y;
			move.set(mx/fineDivisor/zoom,my/fineDivisor/zoom);
			switch (fixAxis) {
				case X:move.set(mx,0);break;
				case Y:move.set(0,my);break;
			}
			t.trans.set(move);
		}
		t.axis=fixAxis;
		t.ops.addAll(fix);
		t.generate();
		return t;
	}
	
	
	private void transform(TransformOperatorInOut t) {
		for (int i=0;i<strokesTmpIndexes.size();i++) {
			processVec.clear();
			processVecTmp.clear();
			//Log.d(DVGlobals.LOG_TAG, "transform():"+_drawView.getCurrentLayerIdx());
			DrawingElement originalde = _drawView.getCurrentLayer().get( strokesTmpIndexes.get(i) );
			DrawingElement tmpde = strokesTmp.get(i);
			if (originalde instanceof Stroke) {
				processVec.add((Stroke)originalde);
				processVecTmp.add((Stroke)tmpde);
			} else {
				processVec.addAll(((Group)originalde).getAllStrokes());
				processVecTmp.addAll(((Group)tmpde).getAllStrokes());
			}
			int ctr=0;
			for (int l=processVec.size()-1;l>=0;l--) {
				Stroke original = processVec.get(l);
				Stroke tmp = processVecTmp.get(l);
				transform(original, tmp, t);
				tmp.update(false, _drawView._renderer, fillGradientOnlyUpdFlags);
			}
			//Log.d(Globals.LOG_TAG, "modifytmp:"+modType+":"+ctr);
		}
	}
	
	public void transform(DrawingElement de,DrawingElement de1,  TransformOperatorInOut t) {
		if (!de.getClass().equals(de1.getClass())) {throw new RuntimeException("Only matching types please.");}
		processVec.clear();
		processVecTmp.clear();
		if (de instanceof Drawing) {
			processVec.addAll(((Drawing)de).getAllStrokes());
			processVecTmp.addAll(((Drawing)de1).getAllStrokes());
		} else if (de instanceof Group){
			processVec.addAll(((Group)de).getAllStrokes());
			processVecTmp.addAll(((Group)de1).getAllStrokes());
		} else if (de instanceof Stroke) {
			processVec.add((Stroke)de);
			processVecTmp.add((Stroke)de);
		}
		for (int l=processVec.size()-1;l>=0;l--) {
			Stroke original = processVec.get(l);
			Stroke tmp = processVecTmp.get(l);
			transform(original,tmp, t);
			tmp.update(false, _drawView._renderer, fillGradientOnlyUpdFlags);
		}
	}
	
	private void transformPoints(HashSet<PointSelection> ps, Stroke sout, TransformOperatorInOut t) {
		PointVec pv = null;
		PointVec pvtmp = null;
		for (PointSelection p:ps) {
				
			pv = p.s.points.get(p.pointIndex);
			pvtmp = sout.points.get(p.pointIndex);
			transform(pv, pvtmp, t);
			/*
			if (pv.beizer1!=null) {
				transform(pv.beizer1, pvtmp.beizer1, t);
			}
			if (pv.beizer2!=null) {
				transform(pv.beizer2, pvtmp.beizer2, t);
			}
			*/
		}
	}
	
	
	///////////////////////////////////////////////// Align / dist / flip / rotate //////////////////////////////////////////////////////
	
	public void doAlign(int v, int h) {
		doAlignDist(v, h, AD_ALIGN, 0);
	}

	public void setReverse(View v) {
		distReverse = ((CheckBox) v).isChecked();
	}

	public void setNoReorder(View v) {
		distNoReorder = ((CheckBox) v).isChecked();
	}

	public void doDist(int v, int h) {
		int flags = distReverse ? AD_FLAG_DIST_REVERSE : 0;
		flags += distNoReorder ? AD_FLAG_DIST_DONTREORDER : 0;
		doAlignDist(v, h, AD_DIST, flags);
	}

	public void doAlignDist(int v, int h, int alignOrDist, int flags) {
		//if (adfPointStrokeSelection > -1) {
			alignAndDistribute(v, h, alignOrDist, _drawView._mode.opState==Mode.POINTS_SEL?ADF_MODE_POINTS:ADF_MODE_STROKE, flags);
		//}
		//dismissDialog(DIALOG_ALIGN);
		alignDialog.dismiss();
	}
	
//////////////////////////////////////////////////align distribute ////////////////////////////////////////////
	public static final int AD_V_TOP = 0;
	public static final int AD_V_CTR = 1;
	public static final int AD_V_BOT = 2;
	public static final int AD_V_NONE = 3;
	
	public static final int AD_H_LEFT = 0;
	public static final int AD_H_CTR = 1;
	public static final int AD_H_RIGHT = 2;
	public static final int AD_H_NONE = 3;
	
	public static final int AD_ALIGN = 0;
	public static final int AD_DIST = 1;
	
	public static final int AD_FLAG_DIST_DONTREORDER = 1;// for dist dont reorder the points - just space them evenly
	public static final int AD_FLAG_DIST_REVERSE = 2;// reverse order of dist
	
	public static final int ADF_MODE_STROKE = 0;
	public static final int ADF_MODE_POINTS = 1;

	public void alignAndDistribute(int vert,int horiz,int alignOrDist,int mode,int flags){
		RectF bounds = new RectF();
		int number = 0;
		if (mode == ADF_MODE_STROKE) {
			ArrayList<DrawingElement> strokes = _drawView._selectionOverlay._selection;
			if (strokes.size()==0) {return;}
			_drawView.getBoundsForElements(bounds, strokes);
			number = strokes.size();
		} else if (mode == ADF_MODE_POINTS){
			if (_drawView._mode.opState!=Mode.POINTS_SEL || _drawView._selectionOverlay.pointsEditStroke==null) {return;}
			Stroke selectedStroke = _drawView._selectionOverlay.pointsEditStroke;
			PointF accum = new PointF();
			int ctr=0;
			for (PointSelection  i : _drawView._selectionOverlay._pointsSelection) {
				PointF p=i.getPoint();
				if (ctr==0) {bounds.set(p.x,p.y,p.x,p.y);}
				else {
					bounds.left = (float)Math.min(bounds.left,p.x);
					bounds.top = (float)Math.min(bounds.top,p.y);
					bounds.right = (float)Math.max(bounds.right,p.x);
					bounds.bottom = (float)Math.max(bounds.bottom,p.y);
				}
				ctr++;
			}
			if (ctr==0) {return;}  
			number = ctr;
		}
		PointF centre = PointUtil.midpoint(bounds);
		Log.d(DVGlobals.LOG_TAG,"and: bounds:"+PointUtil.tostr(bounds)+" mp:"+PointUtil.tostr(centre));
		if (alignOrDist == AD_DIST) {
			boolean dontreorder = (flags&AD_FLAG_DIST_DONTREORDER)==AD_FLAG_DIST_DONTREORDER;
			boolean reverse = (flags&AD_FLAG_DIST_REVERSE)==AD_FLAG_DIST_REVERSE;
			if (mode == ADF_MODE_STROKE) {
				ArrayList<DrawingElement> strokes = _drawView._selectionOverlay._selection;
				if (horiz!=AD_H_NONE) {
					Log.d(DVGlobals.LOG_TAG,"dist horizontal");
					final int hmode = horiz;
					if (dontreorder) {
						Collections.sort(strokes, new Comparator<DrawingElement>() {
							@Override
							public int compare(DrawingElement object1, DrawingElement object2) {
								switch (hmode){
									case AD_H_LEFT: return object1.calculatedBounds.top<object2.calculatedBounds.top?-1:1;
									case AD_H_CTR: return object1.calculatedDim.y<object2.calculatedDim.y?-1:1;
									case AD_H_RIGHT: return object1.calculatedBounds.bottom<object2.calculatedBounds.bottom?-1:1;
								}
								return 0;
							}
						});
					}
					DrawingElement leftStroke = strokes.get(0);
					DrawingElement rightStroke = strokes.get(strokes.size()-1);
					float width = bounds.right - bounds.left;
					float widthReduced = width;// the reduced width to keep all strokes in original bounds
					switch (hmode){
						case AD_H_LEFT: 
							widthReduced-=(rightStroke.calculatedDim.x);break;
						case AD_H_CTR: 
							widthReduced-=(leftStroke.calculatedDim.x/2)+(rightStroke.calculatedDim.x/2);
							break;
						case AD_H_RIGHT: 
							widthReduced-=(leftStroke.calculatedDim.x);
							break;
					}
					float spacing = widthReduced/(strokes.size()-1);
					for (int i=0;i<strokes.size();i++) {
						DrawingElement s = strokes.get(reverse?(strokes.size()-1-i):i);
						float leftForObject = bounds.left;
						switch (hmode){
							case AD_H_LEFT: 
								leftForObject += i*spacing;
								break;
							case AD_H_CTR: 
								leftForObject +=(leftStroke.calculatedDim.x/2)+ i*spacing-(s.calculatedDim.x/2);
								break;
							case AD_H_RIGHT: 
								leftForObject +=(leftStroke.calculatedDim.x)+ i*spacing-(s.calculatedDim.x);
								break;
						}
						final PointF trans = new PointF(leftForObject-s.calculatedBounds.left,0);
						TransformOperatorInOut o = new TransformOperatorInOut();
						o.trans=trans;
						o.axis=Axis.X;
						o.ops.add(Trans.MOVE);
						o.generate();
						transform(s,s, o);

					}
				}
				if (vert!=AD_V_NONE) {
					Log.d(DVGlobals.LOG_TAG,"dist vertical");
					final int vmode = vert;

					if (dontreorder) {
						Collections.sort(strokes, new Comparator<DrawingElement>() {
							@Override
							public int compare(DrawingElement object1, DrawingElement object2) {
								switch (vmode){
									case AD_V_TOP:	return object1.calculatedBounds.left<object2.calculatedBounds.left?-1:1;
									case AD_V_CTR: return object1.calculatedDim.x<object2.calculatedDim.x?-1:1;
									case AD_V_BOT: return object1.calculatedBounds.right<object2.calculatedBounds.right?-1:1;
								}
								return 0;
							}
						});
					}
					DrawingElement topStroke = strokes.get(0);
					DrawingElement bottomStroke = strokes.get(strokes.size()-1);
					float height = bounds.bottom - bounds.top;
					float heightReduced = height; // the reduced width to keep all strokes in original bounds
					switch (vmode){
						case AD_V_TOP: 
							heightReduced-=(bottomStroke.calculatedDim.y);break;
						case AD_V_CTR: 
							heightReduced-=(topStroke.calculatedDim.y/2)+(bottomStroke.calculatedDim.y/2);
							break;
						case AD_V_BOT: 
							heightReduced-=(topStroke.calculatedDim.y);
							break;
					}
					float spacing = heightReduced/(strokes.size()-1);
					for (int i=0;i<strokes.size();i++) {
						DrawingElement s = strokes.get(reverse?(strokes.size()-1-i):i);
						float topForObject = bounds.top;
						switch (vmode){
							case AD_V_TOP: 
								topForObject += i*spacing;
								break;
							case AD_V_CTR: 
								topForObject +=(topStroke.calculatedDim.y/2)+ i*spacing-(s.calculatedDim.y/2);
								break;
							case AD_V_BOT: 
								topForObject +=(topStroke.calculatedDim.y)+ i*spacing-(s.calculatedDim.y);
								break;
						}
						final PointF trans = new PointF(0,topForObject-s.calculatedBounds.top);
						TransformOperatorInOut o = new TransformOperatorInOut();
						o.trans=trans;
						o.axis=Axis.Y;
						o.ops.add(Trans.MOVE);
						o.generate();
						transform(s,s, o);
					}
				}
				
			} else if (mode == ADF_MODE_POINTS) {// needs work ...
				Stroke s = _drawView._selectionOverlay.pointsEditStroke;
				ArrayList<PointF> pts = new ArrayList<PointF>();
				ArrayList<PointSelection> indexes = new ArrayList<PointSelection>(_drawView._selectionOverlay._pointsSelection);
				Collections.sort(indexes);// make comparator
				for (PointSelection i:indexes) {
					pts.add(i.getPoint());
				}
				ArrayList<PointF> ptsUnordered = new ArrayList<PointF>(pts);
				ArrayList<PointF> usepts = dontreorder?pts:ptsUnordered;
				if (horiz!=AD_H_NONE) {
					if (dontreorder) {
						Collections.sort(pts,new Comparator<PointF>() {
							@Override
							public int compare(PointF object1, PointF object2) {
								return object1.x<object2.x?-1:1;
							}
						});
					}
					float hspacing  = ( bounds.right-bounds.left)/(number-1);
					for (int i=0;i<usepts.size();i++) {
						PointF pointF = usepts.get(reverse?i:(usepts.size()-1-i));
						pointF.x=bounds.left+i*hspacing;
					}
				}
				if (vert!=AD_V_NONE) {
					if (dontreorder) {
						Collections.sort(pts,new Comparator<PointF>() {
							@Override
							public int compare(PointF object1, PointF object2) {
								return object1.y<object2.y?-1:1;
							}
						});
					}
					float vspacing  = ( bounds.bottom-bounds.top)/(number-1);//space points across whole bounds
					//sb.append(" bounds hgt:"+(bounds.bottom-bounds.top)+" spacing: "+vspacing);
					
					for (int i=0;i<usepts.size();i++) {
						PointF pointF = usepts.get(reverse?i:(usepts.size()-1-i));
						pointF.y=bounds.top+i*vspacing;
					}
				}

			}
		} else if (alignOrDist == AD_ALIGN) {
			if (mode == ADF_MODE_STROKE) {
				ArrayList<DrawingElement> strokes = _drawView._selectionOverlay._selection;
				for (int i=0;i<strokes.size();i++) {
					DrawingElement s = strokes.get(i);
					final PointF diff = getDiffrenceVector(vert, horiz, bounds, centre, s.calculatedBounds, s.calculatedCentre);
					/*
					TransformOperatorInOut o = new TransformOperatorInOut() {
						@Override
						public void operate(PointF p,PointF po) {
							PointUtil.addVector(p,po, transOut);
						}
					};
					*/
					TransformOperatorInOut o = new TransformOperatorInOut();
					o.axis=Axis.PRESERVE_ASPECT;//vert==AD_V_NONE?Axis.X:Axis.Y;
					o.ops.add(Trans.MOVE);
					o.trans=diff;
					o.generate();
					transform(s,s, o);
				}
			} else if (mode == ADF_MODE_POINTS){
				for ( PointSelection i : _drawView._selectionOverlay._pointsSelection) {
					PointF p = i.getPoint();
					switch (vert) {
						case AD_V_TOP:
							p.y = bounds.top;
							break;
						case AD_V_CTR:
							p.y = centre.y;
							break;
						case AD_V_BOT:
							p.y=bounds.bottom;
							break;
					}
					switch (horiz) {
						case AD_H_LEFT:
							p.x = bounds.left;
							break;
						case AD_H_CTR:
							p.x = centre.x;
							break;
						case AD_H_RIGHT:
							p.x=bounds.right;
							break;
					}
				}
			}
		}
		_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
		_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
		_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
		_drawView.updateDisplay();
	}
	private PointF getDiffrenceVector(int vert, int horiz, RectF bounds,
			PointF centre, RectF calcualtedBounds, PointF calcualtedCentre) {
		PointF diff = new PointF(0,0);
		switch (vert) {
			case AD_V_TOP:
				diff.y = bounds.top-calcualtedBounds.top;
				break;
			case AD_V_CTR:
				diff.y = centre.y-calcualtedCentre.y;
				break;
			case AD_V_BOT:
				diff.y=bounds.bottom-calcualtedBounds.bottom;
				break;
		}
		switch (horiz) {
			case AD_H_LEFT:
				diff.x = bounds.left-calcualtedBounds.left;
				break;
			case AD_H_CTR:
				diff.x = centre.x-calcualtedCentre.x;
				break;
			case AD_H_RIGHT:
				diff.x=bounds.right-calcualtedBounds.right;
				break;
		}
		return diff;
	}
	
	public static final int FLIP_H = 0;
	public static final int FLIP_V = 1;
	private FrameLayout fl;
	public void flip(int axis,int mode,boolean useanchor) {
		final double[][] matrix = new double[2][2];
		matrix[0][0] = axis==FLIP_H?-1:1;
		matrix[0][1] = 0;
		matrix[1][0] = 0;
		matrix[1][1] = axis==FLIP_V?-1:1;
		PointF anchor = new PointF();
		if (!useanchor) {
			if (mode==ADF_MODE_STROKE) {
				RectF r = new RectF();
				_drawView.getBoundsForElements(r, _drawView._selectionOverlay._selection);
				anchor.set(PointUtil.midpoint(r));
			} else if (mode==ADF_MODE_POINTS){
				
			}
		} else {
			anchor.set(_drawView._anchorOverlay.anchor);
		}
		TransformOperatorInOut o = new TransformOperatorInOut();
		o.scaleValue=1;
		o.scaleXValue=axis==FLIP_H?-1:1;
		o.scaleYValue=axis==FLIP_V?-1:1;
		o.axis=axis==FLIP_H?Axis.X:Axis.Y;
		o.ops.add(Trans.SCALE);
		o.anchor.set(anchor);
		o.generate();
		ArrayList<DrawingElement> selection = _drawView._selectionOverlay._selection;
		if (mode == ADF_MODE_STROKE) {
			if (selection.size()>0) {
				for (int i=0;i<selection.size();i++) {
					DrawingElement s = _drawView._selectionOverlay._selection.get(i);
					transform(s,s, o);
					s.update(false, _drawView._renderer, UpdateFlags.ALL);
				}
			}
		} else if (mode == ADF_MODE_POINTS){
			Stroke s = _drawView._selectionOverlay.pointsEditStroke;
			transformPoints(_drawView._selectionOverlay._pointsSelection, s, o);
			s.update(false, _drawView._renderer, UpdateFlags.ALL);
		}
		_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
		_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
		_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
		_drawView.updateDisplay();
	}
	
	
	public void rotate(ArrayList<DrawingElement> strokes,HashSet<PointSelection> indexes,float angle) {//TODO points mode
		double rads = angle*Math.PI/180f;
		TransformOperatorInOut t = new TransformOperatorInOut();
		t.rotateValue=rads;
		t.ops.add(Trans.ROTATE);
		t.axis=Axis.PRESERVE_ASPECT;
		t.generate();
		for (int i=0;i<strokes.size();i++) {
			DrawingElement s = strokes.get(i);
			transform(s, s, t);
			s.update(true,  _drawView._renderer, UpdateFlags.ALL);
		}
		_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
		_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
		_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
		_drawView.updateDisplay();
	}

	
	/*************************************************************************** DIALOGS ****************************************************/
	public Dialog getAlignDialog(Context c,int icon) {
		if (alignDialog!=null && c!=alignDialog.getContext()) {
			Log.d(DVGlobals.LOG_TAG,"alignDialog: context changed");
			alignDialog=null;
		}
		if (alignDialog==null) {
			fl = new FrameLayout(c);
			FrameLayout.inflate(c, R.layout.align, fl);
			alignDialog= new AlertDialog.Builder(c)
				.setIcon(icon)
				.setView(fl)
				.setNegativeButton(	R.string.dialog_but_cancel, new OnEWDialogClickListener(c) {
							public void onEWClick(DialogInterface dialog,	int which) {
								dialog.dismiss();
							}
						}).create();
			((LinearLayout)fl.findViewById(R.id.align_left)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doAlign(AD_V_NONE, AD_H_LEFT);
				}
			});
			((LinearLayout)fl.findViewById(R.id.align_center_h)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doAlign(AD_V_NONE, AD_H_CTR);
				}
			});
			((LinearLayout)fl.findViewById(R.id.align_right)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doAlign(AD_V_NONE, AD_H_RIGHT);
				}
			});
			((LinearLayout)fl.findViewById(R.id.align_top)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doAlign(AD_V_TOP, AD_H_NONE);
				}
			});
			((LinearLayout)fl.findViewById(R.id.align_center_v)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doAlign(AD_V_CTR, AD_H_NONE);
				}
			});
			((LinearLayout)fl.findViewById(R.id.align_bottom)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doAlign(AD_V_BOT, AD_H_NONE);
				}
			});
			((LinearLayout)fl.findViewById(R.id.dist_l_h)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doDist(AD_V_NONE, AD_H_LEFT);
				}
			});
			((LinearLayout)fl.findViewById(R.id.dist_c_h)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doDist(AD_V_NONE, AD_H_CTR);
				}
			});
			((LinearLayout)fl.findViewById(R.id.dist_r_h)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doDist(AD_V_NONE, AD_H_RIGHT);
				}
			});
			((LinearLayout)fl.findViewById(R.id.dist_t_v)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doDist(AD_V_TOP, AD_H_NONE);
				}
			});
			((LinearLayout)fl.findViewById(R.id.dist_c_v)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doDist(AD_V_CTR, AD_H_NONE);
				}
			});
			((LinearLayout)fl.findViewById(R.id.dist_b_v)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					doDist(AD_V_BOT, AD_H_NONE);
				}
			});
			((CheckBox)fl.findViewById(R.id.align_dist_noreorder_chk)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					distNoReorder = ((CheckBox) v).isChecked();
				}
			});
			((CheckBox)fl.findViewById(R.id.align_dist_reverse_chk)).setOnClickListener(new OnEWClickListener(c) {
				@Override
				public void onEWClick(View v) {
					distReverse = ((CheckBox) v).isChecked();
				}
			});
		}
		((CheckBox)fl.findViewById(R.id.align_dist_noreorder_chk)).setChecked(distNoReorder);
		((CheckBox)fl.findViewById(R.id.align_dist_reverse_chk)).setChecked(distReverse);
		alignDialog.setTitle(c.getString(R.string.dialog_title_align_distribute)+ ( _drawView._mode.opState==Mode.EDIT ? c.getString(R.string.strokes): c.getString(R.string.points)));
		return alignDialog;
	}

	public Dialog getFlipDialog(Context c,int icon) {
		if (flipDialog!=null && c!=flipDialog.getContext()) {
			Log.d(DVGlobals.LOG_TAG,"flipDialog: context changed");
			flipDialog=null;
		}
		if (flipDialog==null) {
			final String[] flipitems = {    c.getString(R.string.dialog_item_flip_horizontally),  
											c.getString(R.string.dialog_item_flip_vertically),  
											c.getString(R.string.dialog_item_flip_horizontally_anchor),  
											c.getString(R.string.dialog_item_flip_vertically_anchor) };
			return new AlertDialog.Builder(c)
							.setIcon(icon)
							.setItems(flipitems,new OnEWDialogClickListener(c) {
								public void onEWClick(DialogInterface dialog,
										int which) {
									int adfPointStrokeSelection=  _drawView._mode.opState==Mode.POINTS_SEL?ADF_MODE_POINTS:ADF_MODE_STROKE;
									switch (which) {
									case 0:
										flip(FLIP_H,adfPointStrokeSelection, false);
										break;
									case 1:
										flip(FLIP_V,adfPointStrokeSelection, false);
										break;
									case 2:
										flip(FLIP_H,adfPointStrokeSelection, true);
										break;
									case 3:
										flip(FLIP_V,adfPointStrokeSelection, true);
										break;
									}
								}
							}).create();
		}
		flipDialog.setTitle(c.getString(R.string.dialog_title_flip)	+ (_drawView._mode.opState!=Mode.POINTS_SEL ? c.getString(R.string.strokes): c.getString(R.string.points)));
		return flipDialog;
	}
	public Dialog getRotateDialog(Context c) {
		if (rotateDialog!=null && c!=rotateDialog.getContext()) {
			Log.d(DVGlobals.LOG_TAG,"flipDialog: context changed");
			rotateDialog=null;
		}
		if (rotateDialog==null) {
		final String[] rotateitems = {  
				c.getString(R.string.dialog_item_rot_90_degrees),  
				c.getString(R.string.dialog_item_rot_180_degrees),  
				c.getString(R.string.dialog_item_rot_270_degrees) 
			};
		rotateDialog = new AlertDialog.Builder(c)
				.setTitle(R.string.dialog_title_rotate_strokes)
				.setIcon(R.drawable.i_rotate)
				.setItems(rotateitems,	new OnEWDialogClickListener(c) {
							public void onEWClick(DialogInterface dialog,	int which) {
								float degrees = (which+1)*90;
								if (_drawView._mode.opState==Mode.POINTS_SEL) {
									ArrayList<DrawingElement> sel = new ArrayList<DrawingElement>();
									sel.add(_drawView._selectionOverlay.pointsEditStroke);
									rotate(sel,_drawView._selectionOverlay._pointsSelection,degrees);
								} else {
									rotate(_drawView._selectionOverlay._selection,null,degrees);
								}
								
							}
						}).create();
		}
		return rotateDialog;
	}
}
