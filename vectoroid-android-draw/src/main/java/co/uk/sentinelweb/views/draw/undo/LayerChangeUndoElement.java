package co.uk.sentinelweb.views.draw.undo;

import co.uk.sentinelweb.views.draw.view.DrawView;

public class LayerChangeUndoElement extends UndoElement {
	
	public LayerChangeUndoElement() {
		_undoOpType = UndoOperationType.LAYER_CHANGE;
		_undoType = UndoType.DELTA;
	}

	@Override
	public void makeRestorePointOps(DrawView d) {
		
		
	}

	@Override
	public void restoreRestorePointOps(DrawView d,boolean isUndo) {
		d.setCurrentLayerInternal(_currentLayerIdx);
	}

}
