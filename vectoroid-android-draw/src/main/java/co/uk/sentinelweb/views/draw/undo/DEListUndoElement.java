package co.uk.sentinelweb.views.draw.undo;

import java.util.ArrayList;

import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class DEListUndoElement extends UndoElement{
	public ArrayList<DrawingElement> _deList;
	public ArrayList<Integer> _deListIndexes;
	
	public DEListUndoElement() {
		_undoOpType = UndoOperationType.DE_LIST;
		_undoType = UndoType.SNAPSHOT;
		//initSelection(_drawView);
	}

	@Override
	public void makeRestorePointOps(DrawView _drawView) {
		// dont call here as it called after the operation - we want to get the selection before the operation is performed.
	}
	
	public void initList(DrawView _drawView,DrawingElement de) {
		ArrayList<DrawingElement> list = new ArrayList<DrawingElement>();
		list.add(de);
		initList(_drawView,list);
	}
	public void initList(DrawView _drawView,ArrayList<DrawingElement> deList) {
		for (DrawingElement de : deList) {
			int index = -1;
			index = _drawView.getCurrentLayer().indexOf(de);
			if (index>-1) {
				_deList.add(de.duplicate());
				_deListIndexes.add(index);
			}
		}
	}

	@Override
	public void restoreRestorePointOps(DrawView _drawView, boolean isUndo) {
		_drawView._selectionOverlay._selection.clear();
		for (int i=0;i<_deList.size();i++ ) {
			DrawingElement de = _deList.get(i);
			Integer idx = _deListIndexes.get(i);
			DrawingElement duplicate = de.duplicate();
			
			_drawView._renderer.removeFromCache(_drawView.getCurrentLayer().get(idx));
			_drawView.getCurrentLayer().set(idx, duplicate);
			duplicate.update(true, _drawView._renderer, UpdateFlags.ALL_NOLISTENERS);
			// will make selection - might be inconsistent fix at origin
			_drawView._selectionOverlay._selection.add(duplicate);
		}
	}
	
}
