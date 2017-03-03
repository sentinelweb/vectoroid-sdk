package co.uk.sentinelweb.views.draw.overlay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWDialogClickListener;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Mode;
import co.uk.sentinelweb.views.draw.model.internal.TouchData;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class AnchorOverlay extends Overlay {
	public enum Anchor {
		SELECTION_CENTER,
		POINTS_CENTER,
		SET,
		SELECTION_CENTROID,
		SELECTION_TOPLEFT,
		SELECTION_TOPRIGHT,
		SELECTION_BOTTOMLEFT,
		SELECTION_BOTTOMRIGHT,
		LOCKED,
		PAGECENTER
	};
	static List<Anchor> _typesLookup = Arrays.asList(Anchor.values());
	public enum Type {SEL,POINT};
	public static final String[] ANCHOR_STRINGS=new String[]{
		"Selection centre",
		"Points centre",
		"Set manually",
		"Selection centroid",
		"Selection top left",
		"Selection top right",
		"Selection bottom left",
		"Selection bottom right",
		"Lock to current position",
		"Page centre"
	};
	public static final String[] ANCHOR_STRINGS_SHORT=new String[]{
		"Sel ctr",
		"Pt ctr",
		"Manual",
		"Sel cent",
		"Sel TL",
		"Sel TR",
		"Sel BL",
		"Sel BR",
		"Locked",
		"Pg ctr"
	};
	/*
	public static final int Anchor.SELECTION_CENTER = 0;
	public static final int Anchor.POINTS_CENTER = 1;
	public static final int Anchor.SET = 2;
	public static final int Anchor.SELECTION_CENTROID = 3;
	public static final int Anchor.SELECTION_TOPLEFT = 4;
	public static final int Anchor.SELECTION_TOPRIGHT = 5;
	public static final int Anchor.SELECTION_BOTTOMLEFT = 6;
	public static final int Anchor.SELECTION_BOTTOMRIGHT = 7;
	public static final int Anchor.LOCKED = 8;
	public static final int Anchor.PAGECENTER = 9;
	*/
	public PointF anchor = new PointF(0,0);
	public PointF anchorNeg = new PointF(0,0);
	Anchor anchorType = Anchor.SELECTION_CENTER;
	Anchor pointAnchorType = Anchor.POINTS_CENTER;
	public String anchorText;
	public String pointAnchorText; 
	
	Paint anchorPaint;
	
	public AnchorOverlay(DrawView d) {
		super(d);
		anchorPaint=new Paint();
		anchorPaint.setStrokeWidth(1);
		anchorPaint.setARGB(255, 255, 255,255);
		anchorPaint.setStyle(Style.STROKE);
		anchorPaint.setAntiAlias(true);
		setAnchoring(anchorType, Type.SEL);
		setAnchoring(pointAnchorType, Type.POINT);
	}
	@Override
	public void draw(Canvas c) {
		float anchorSize=10*_density;
		float zoom = _drawView._viewPort.data.zoom;
		PointF topLeft = _drawView._viewPort.data.topLeft;
		float cx=(anchor.x-topLeft.x)*zoom;
		float cy=(anchor.y-topLeft.y)*zoom;
		if (cx>-anchorSize && cy>-anchorSize && cx<_drawView.getMeasuredWidth()+anchorSize && cy<_drawView.getMeasuredHeight()+anchorSize) {
			c.drawLine(cx-anchorSize, cy-anchorSize, cx+anchorSize, cy+anchorSize, anchorPaint);
			c.drawLine(cx-anchorSize, cy+anchorSize, cx+anchorSize, cy-anchorSize, anchorPaint);
			c.drawCircle(cx, cy, anchorSize, anchorPaint);
		}
		if (_drawView._mode.opState==Mode.SETANCHOR) {// draw crosshairs for setting anchor point - as it's under finger
			c.drawLine(0, -topLeft.y+anchor.y, _drawView.getMeasuredWidth(), -topLeft.y+anchor.y, anchorPaint);
			c.drawLine(-topLeft.x+anchor.x,0,  -topLeft.x+anchor.x, _drawView.getMeasuredHeight(), anchorPaint);
		}
	}

	@Override
	public boolean onTouch(TouchData td) {
		switch (td._e.getAction() ) {
			case MotionEvent.ACTION_DOWN: 
				anchor.set(td._touchPoint);
				_drawView.updateDisplay();
				return true;
			case MotionEvent.ACTION_MOVE: 
				anchor.set(td._touchPoint);
				_drawView.updateDisplay();
				return true;
			case MotionEvent.ACTION_UP: 
				anchor.set(td._touchPoint);
				_drawView._mode.reverseState();
				_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
				_drawView.updateDisplay();
				return true;
			default : return false;
		}
	}

	// calculate Centre of gravity for a set of strokes
	public PointF getAnchor(ArrayList<DrawingElement> ss,Anchor type) {
		PointF accum = new PointF(0,0);
		float ctr=0;
		for (int i=0;i<ss.size();i++) {
			DrawingElement s = ss.get(i);
			switch(type) {
//				case Anchor.SELECTION_CENTER:// this is wrong, should get bounds for set and take midpoint
//					accum.x += s.calcualtedCentre.x;
//					accum.y += s.calcualtedCentre.y;
//					ctr++;
//					break;
				case SELECTION_CENTROID:
					accum.x += s.calculatedCOG.x;
					accum.y += s.calculatedCOG.y;
					ctr++;
					break;
				case SELECTION_TOPLEFT:
					if (i==0) {accum.x=s.calculatedBounds.left;}
					else accum.x = (float)Math.min(accum.x,s.calculatedBounds.left);
					if (i==0) {accum.y=s.calculatedBounds.top;}
					else accum.y = (float)Math.min(accum.y,s.calculatedBounds.top);
					ctr=1;
					break;
				case SELECTION_TOPRIGHT:
					accum.x = (float)Math.max(accum.x,s.calculatedBounds.right);
					if (i==0) { accum.y=s.calculatedBounds.top;}
					else accum.y = (float)Math.min(accum.y,s.calculatedBounds.top);
					ctr=1;			
					break;
				case SELECTION_BOTTOMLEFT:
					if (i==0) {accum.x=s.calculatedBounds.left;}
					else accum.x = (float)Math.min(accum.x,s.calculatedBounds.left);
					accum.y = (float)Math.max(accum.y,s.calculatedBounds.bottom);
					ctr=1;
					break;
				case SELECTION_BOTTOMRIGHT:
					accum.x = (float)Math.max(accum.x,s.calculatedBounds.right);
					accum.y = (float)Math.max(accum.y,s.calculatedBounds.bottom);
					ctr=1;
					break; 
			}
		}
		accum.x/=ctr;accum.y/=ctr; 
		return accum;
	}
	
	public void updateAnchor() {
		ArrayList<DrawingElement> selection = _drawView._selectionOverlay._selection;
		Anchor atype = _drawView._mode.opState==Mode.POINTS_SEL?pointAnchorType:anchorType;
		switch (atype) {
			case SELECTION_CENTER:
				//anchor = getAnchor(getSelection(),SELECTION_CENTER);
				RectF r = new RectF();
				_drawView.getBoundsForElements(r, selection);
				anchor = PointUtil.midpoint(r);
				break;
			case SELECTION_CENTROID:
				anchor = getAnchor(selection,Anchor.SELECTION_CENTROID);
				break;
			case POINTS_CENTER:
				PointF p = _drawView._selectionOverlay.pointsMidPoint();
				if (p!=null) {
					anchor = p;
				} else {return;}
				break;
			case SELECTION_TOPLEFT:
				anchor = getAnchor(selection,Anchor.SELECTION_TOPLEFT);
				break;
			case SELECTION_TOPRIGHT:
				anchor = getAnchor(selection,Anchor.SELECTION_TOPRIGHT);
				break;
			case SELECTION_BOTTOMLEFT:
				anchor = getAnchor(selection,Anchor.SELECTION_BOTTOMLEFT);
				break;
			case SELECTION_BOTTOMRIGHT:
				anchor = getAnchor(selection,Anchor.SELECTION_BOTTOMRIGHT);
				break;
			case SET:return;
			case LOCKED:return;
			case PAGECENTER:
				if (_drawView._drawing!=null) {
					int dimensionWidth = (int)_drawView._drawing.size.x;
					int dimensionHeight = (int)_drawView._drawing.size.y;
					anchor = new PointF(dimensionWidth/2,dimensionHeight/2);
				}
				break;
		}
		anchorNeg.x = -anchor.x;
		anchorNeg.y = -anchor.y;
	}
	
	public void setAnchoring(Anchor i,Type type) {
		if (type==Type.SEL) { 
			anchorType=i;
			anchorText=ANCHOR_STRINGS_SHORT[_typesLookup.indexOf(i)];
		} else if  (type==Type.POINT) {
			pointAnchorType=i;
			pointAnchorText=ANCHOR_STRINGS_SHORT[_typesLookup.indexOf(i)];
		}
		updateAnchor();
	}
	
	public Dialog getAnchorDilaog (Activity a, final boolean point) {
		return new AlertDialog.Builder(a)
		.setTitle(point?R.string.dialog_title_points_anchor:R.string.dialog_title_shapes_anchor)
		.setIcon(R.drawable.i_anchor)
		.setSingleChoiceItems((CharSequence[]) ANCHOR_STRINGS, _typesLookup.indexOf(point?pointAnchorType:anchorType),new OnEWDialogClickListener(a) {
						public void onEWClick(DialogInterface dialog,	int which) {
							if (which != _typesLookup.indexOf(Anchor.SET)) {
								setAnchoring(_typesLookup.get(which),	point?Type.POINT:Type.SEL);
								_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
								//showMessage(getString(R.string.msg_ok_set_selection_anchoring_to_)	+ ANCHOR_STRINGS[which],	MSG_TYPE_OK);
							} else {
								_drawView._mode.setReversibleState(Mode.SETANCHOR);
								setAnchoring(Anchor.SET,	point?Type.POINT:Type.SEL);
								//showMessage(getString(R.string.msg_tip_press_the_screen_to_set_the_points_anchor_location),MSG_TYPE_TIP);
							}
							_drawView.updateDisplay();
							dialog.dismiss();
						}
					}).create();
	}
}
