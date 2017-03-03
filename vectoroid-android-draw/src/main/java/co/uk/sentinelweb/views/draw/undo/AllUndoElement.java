package co.uk.sentinelweb.views.draw.undo;

import java.util.ArrayList;

import co.uk.sentinelweb.views.draw.model.Drawing;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class AllUndoElement extends UndoElement {
	public DrawingElement _copiedElement;
	//public ArrayList<DrawingElement> _copiedChildren;
	public ArrayList<DrawingElement> selection;
	
	public AllUndoElement() {
		_undoOpType = UndoOperationType.ALL;
		_undoType = UndoType.SNAPSHOT;
	}

	@Override
	public void makeRestorePointOps(DrawView _drawView) {
		selection=new ArrayList<DrawingElement>();
		_copiedElement=(Drawing)_drawView._drawing.duplicate();// copied is a deep duplicate of the drawing.
		for (DrawingElement de : _drawView._selectionOverlay._selection) {
			Drawing copiedDrawing = (Drawing)_copiedElement;
			if (_drawView._currentLayer==null) {
				int i = _drawView._drawing.elements.indexOf(de);
				selection.add(copiedDrawing.elements.get(i));
			} else {
				int i = _drawView.getCurrentLayer().indexOf(de);
				selection.add(copiedDrawing.layers.get(_currentLayerIdx).elements.get(i));
			}
		}
		
	}

	@Override
	public void restoreRestorePointOps(DrawView _drawView,boolean isUndo) {
		_drawView._selectionOverlay._selection.clear();
		ArrayList<DrawingElement> newSelection = new ArrayList<DrawingElement>();
		
		_drawView._drawing.copyFrom((Drawing)_copiedElement.duplicate());//full copy
		if (selection.size()>0) {
			ArrayList<DrawingElement> _layerArray = _currentLayerIdx==-1?_drawView._drawing.elements:_drawView._drawing.layers.get(_currentLayerIdx).elements;
			for (DrawingElement de:selection) {
				int idx = selection.indexOf(de);
				newSelection.add( _layerArray.get(idx) );
			}
		}
		_drawView._renderer.dropCache();
		_drawView._drawing.update(true, _drawView._renderer, UpdateFlags.ALL);
		_drawView.setCurrentLayerInternal(_currentLayerIdx);
		_drawView._selectionOverlay._selection.addAll(newSelection);
	}

}
