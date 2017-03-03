package co.uk.sentinelweb.views.draw.undo;

import java.util.ArrayList;

import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.Fill;
import co.uk.sentinelweb.views.draw.model.Pen;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Draw;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Mode;
import co.uk.sentinelweb.views.draw.view.DrawView;
import co.uk.sentinelweb.views.draw.view.controller.UndoController;

public abstract class UndoElement {
	public enum UndoOperationType {
		ALL, 
		LAYER_ALL, 
		LAYER_CHANGE, 
		TRANSFORM, // store transform and selected indexes
		// TODO rest
		ADD_ELEMENT,// store added element (for redo) and index
		DE_LIST, // selection is backed up, replace with selected - need a selection array backup and index mapping.
		GROUP_UNGROUP,
		//MODIFY_PEN,   //? maybe just sel_modify
		//MODIFY_FILL   //? maybe just sel_modify
		
		
	} //RM : 021211: added
	public enum UndoType {
		SNAPSHOT, DELTA
	}
	public UndoType _undoType= UndoType.SNAPSHOT;
	public UndoOperationType _undoOpType = UndoOperationType.LAYER_ALL;
	//public DrawingElement _copiedElement;
	//public ArrayList<DrawingElement> _copiedChildren;
	public Mode mode;
	public Draw drawMode;
	public Fill fill;
	public Pen pen;
	public Integer _currentLayerIdx=-1;
	
	public abstract void makeRestorePointOps(DrawView d) ;
	public abstract void restoreRestorePointOps(DrawView d,boolean isUndo) ;
	
	public static UndoElement getUndoElement(UndoOperationType type){
		switch (type) {
			case ALL: return new AllUndoElement();
			case LAYER_ALL: return new LayerAllUndoElement();
			case LAYER_CHANGE: return new LayerChangeUndoElement();
			case TRANSFORM: return new TransformUndoElement();
			case ADD_ELEMENT: return new AddDEUndoElement();
			case DE_LIST: return new DEListUndoElement();
			default: return new LayerAllUndoElement();
		}
	}
	
	public void makeRestorePoint(DrawView drawView) {
		_currentLayerIdx=drawView.getCurrentLayerIdx();
		//selection=new ArrayList<DrawingElement>();
		
		makeRestorePointOps(drawView);
		
		mode=drawView._mode.opState;
		drawMode=drawView._mode.drawMode;
		fill=drawView.getCurrentFill().duplicate();
		pen=drawView.getCurrentPen().duplicate();
	}
	
	public void restoreRestorePoint(DrawView _drawView,boolean isUndo) {
		//_drawView._selectionOverlay._selection.clear();
		//ArrayList<DrawingElement> newSelection = new ArrayList<DrawingElement>();
		
		restoreRestorePointOps(_drawView,isUndo);
		
		//_drawView._selectionOverlay._selection.addAll(newSelection);
		//_drawView.setCurrentLayerInternal(_currentLayerIdx);
		//Log.d(DVGlobals.LOG_TAG, "!!!!!!!!! UNDO:  restoreRestorePoint: type:"+ue._undoOpType+" _currentLayerIdx: "+ue._currentLayerIdx+" ("+_drawView.getCurrentLayerIdx()+") sz:"+_drawView.getCurrentLayer().size());
		//_drawView._selectionOverlay._selection.clear();
		
	}
}

