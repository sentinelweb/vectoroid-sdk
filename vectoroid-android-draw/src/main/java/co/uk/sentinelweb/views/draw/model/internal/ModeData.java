package co.uk.sentinelweb.views.draw.model.internal;

import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class ModeData {
	public static final int LINEDRAW_SNAP_DISTANCE_DEF = 10;
	public static final int OPT_LINEDRAW_SNAPPING = 1;
	public static final int OPT_LINEDRAW_VERTEX_TIME=500;
	
	public static final int OPT_POINT_SNAPPING = 1;
	
	public static final int OPT_FREEDRAW_AUTOSMOOTH = 1;
	
	public enum Mode { NONE, EDIT, DRAW, POINTS_SEL, SETANCHOR, PAN, ZOOM, SELRECT, GRADIENT, PAN_AND_ZOOM,EDIT_TEXT};
	public enum Draw { FREE, LINE, CIRCLE, RECT, TEXT, SHAPE };
	//public enum Operation { MOVE, SCALE_X, SCALE_Y, SCALE_BOTH, SCALE_BOTH_FIX_ASPECT, SHEAR_X,SHEAR_Y, ROTATE, PROJ };
	
	public enum Smooth {MEDIAN, MEAN , MIDPOINT};
	
	DrawView drawView;
	
	public Mode opState = Mode.DRAW;
	public Mode lastOpState = Mode.DRAW;
	public Mode lastState = Mode.DRAW;
	//public Operation pointsOpState = Operation.MOVE;
	public Draw drawMode = Draw.FREE;
	public String modeText = "";
	public Mode pointsEditLastMode =  Mode.EDIT;
	
	
	public int freeDrawOptions = 0;
	public int freeDrawSmoothLevel = 0;
	
	public int lineDrawOptions = 0;
	public int lineDrawSnapDist = LINEDRAW_SNAP_DISTANCE_DEF;
	
	public int pointsDrawOptions = 0;
	
	public ModeData(DrawView d) {
		super();
		this.drawView = d;
	}

	public void setModeText() {
		StringBuffer s = new StringBuffer();
		switch(opState) {
			case DRAW: 
				s.append(drawView.getContext().getString(R.string.drawview_draw));
				s.append(" - ");
				switch(drawMode) {
					case FREE: s.append(drawView.getContext().getString(R.string.drawview_free));break;
					case LINE: s.append(drawView.getContext().getString(R.string.drawview_line));break;
					case CIRCLE: s.append(drawView.getContext().getString(R.string.drawview_circle));break;
					case RECT: s.append(drawView.getContext().getString(R.string.drawview_rect));break;
					case TEXT: s.append(drawView.getContext().getString(R.string.drawview_text));break;
					case SHAPE: s.append(drawView.getContext().getString(R.string.drawview_shape));break;
					
				}
				break;
			case POINTS_SEL: s.append(drawView.getContext().getString(R.string.drawview_edit_points));	break;
			case PAN: s.append(drawView.getContext().getString(R.string.drawview_pan));break;
			case ZOOM: s.append(drawView.getContext().getString(R.string.drawview_zoom));break;
			case SETANCHOR: s.append(drawView.getContext().getString(R.string.drawview_set_anchor));break;
			case SELRECT: s.append(drawView.getContext().getString(R.string.drawview_select_area));break;
			case EDIT: s.append(drawView.getContext().getString(R.string.drawview_edit_strokes));break;
			case GRADIENT: s.append(drawView.getContext().getString(R.string.drawview_gradient));break;
			case PAN_AND_ZOOM: s.append(drawView.getContext().getString(R.string.drawview_panzoom));break;
			case EDIT_TEXT: s.append(drawView.getContext().getString(R.string.drawview_edit_text));break;
		}
		modeText=s.toString();
		//updateSelectionIconRects();
		//invalidate();
	}
	
	public void setDrawMode( Draw i ) {
		if (drawMode==Draw.LINE && i==Draw.LINE) {drawView.finishDrawLine();return;}// doesnt need to update mode
		else if (drawMode==Draw.LINE) {drawView.cancelDrawInternal();}
		if (drawMode==Draw.TEXT) {drawView.cancelTextInternal();}
		drawMode = i;
		setModeText();
		//updateAnchor();
		//updateSelectionIconRects();
		drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
		drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
		setState( Mode.DRAW );
	}
	
	public void setState( Mode i ) {
		setStateInternal(i,false);
		drawView.updateDisplay();
	}

	public void setStateInternal(Mode i,boolean skipTextCheck) {
		lastState = opState;
		if (!skipTextCheck && opState==Mode.EDIT_TEXT) {
			drawView.cancelTextInternal();
		}
		opState=i;
		if (opState==Mode.DRAW || opState==Mode.EDIT|| opState==Mode.EDIT_TEXT || opState==Mode.PAN_AND_ZOOM) {
			lastOpState=i;
		}
		if (opState==Mode.DRAW || opState==Mode.EDIT || opState==Mode.POINTS_SEL) {
			drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
			drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
		}
		drawView._updateFlags[DrawView.UPDATE_MODE]=true;
		setModeText();
	}
	
	public void setReversibleState( Mode i ) {
		opState=i;
		setModeText();
		drawView._updateFlags[DrawView.UPDATE_MODE]=true;
		drawView.updateDisplay();
	}
	
	public void reverseState( ) {
		opState=lastOpState;
		setModeText();
		drawView._updateFlags[DrawView.UPDATE_MODE]=true;
		drawView.updateDisplay();
	}
	
	
}
