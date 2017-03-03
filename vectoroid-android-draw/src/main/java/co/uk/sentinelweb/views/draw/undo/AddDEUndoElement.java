package co.uk.sentinelweb.views.draw.undo;

import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class AddDEUndoElement extends UndoElement {
	public DrawingElement _addedElement;
	int addIndex = -1;
	
	public AddDEUndoElement() {
		_undoOpType = UndoOperationType.ADD_ELEMENT;
		_undoType = UndoType.DELTA;
	}

	@Override
	public void makeRestorePointOps(DrawView d) {
		// TODO Auto-generated method stub
		addIndex = d.getCurrentLayer().indexOf(_addedElement);
		_addedElement=_addedElement.duplicate();
	}

	@Override
	public void restoreRestorePointOps(DrawView d, boolean isUndo) {
		if (isUndo) {
			DrawingElement de = d.getCurrentLayer().remove(addIndex);
			d._selectionOverlay._selection.remove(de);
		} else {
			DrawingElement de = _addedElement.duplicate();
			d.getCurrentLayer().add(de);//addIndex, - is the size of the array so throws : java.lang.IndexOutOfBoundsException: Invalid index 13, size is 13
			de.update(true, d._renderer, UpdateFlags.ALL);
			// not sure i want to do this - auto select elememnt on redo
			d._selectionOverlay._selection.clear();
			d._selectionOverlay._selection.add(de);
		}
		
	}

}
