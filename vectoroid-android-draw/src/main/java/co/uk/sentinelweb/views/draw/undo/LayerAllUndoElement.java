package co.uk.sentinelweb.views.draw.undo;

import java.util.ArrayList;

import co.uk.sentinelweb.views.draw.model.Drawing;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.Layer;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class LayerAllUndoElement extends UndoElement {
	public DrawingElement _copiedElement;
	public ArrayList<DrawingElement> _copiedChildren;
	public ArrayList<DrawingElement> selection;
	
	public LayerAllUndoElement() {
		_undoOpType = UndoOperationType.LAYER_ALL;
		_undoType = UndoType.SNAPSHOT;
	}

	@Override
	public void makeRestorePointOps(DrawView drawView) {
		selection=new ArrayList<DrawingElement>();
		if (_currentLayerIdx==-1) {
			_copiedElement=(Drawing)drawView._drawing.duplicate(true);
		} else {
			_copiedElement=(Layer)drawView._drawing.layers.get(_currentLayerIdx).duplicate(true);
		}
		_copiedChildren = new ArrayList<DrawingElement>();
		for (DrawingElement de : drawView.getCurrentLayer() ) {
			_copiedChildren.add(de.duplicate());
		}
		for (DrawingElement de : drawView._selectionOverlay._selection) {
			int index = -1;
			if (drawView._currentLayer==null) {
				index = drawView._drawing.elements.indexOf(de);
			} else {
				index = drawView.getCurrentLayer().indexOf(de);
			}
			// TODO replace with
			//index = drawView.getCurrentLayer().indexOf(de);
			selection.add(_copiedChildren.get(index));
		}
	}

	@Override
	public void restoreRestorePointOps(DrawView drawView,boolean isUndo) {
		drawView._selectionOverlay._selection.clear();
		ArrayList<DrawingElement> newSelection = new ArrayList<DrawingElement>();
		
		DrawingElement elUpdated = null;
		if (_currentLayerIdx==-1) {
			Drawing d = drawView._drawing;
			elUpdated=d;
			((Drawing)elUpdated).copyFrom((Drawing)_copiedElement.duplicate());// shallow copy
			drawView._renderer.renderObjects.remove(d);
			drawView._renderer.removeFromCache(d.elements);
		} else {
			Layer l = drawView._drawing.layers.get(_currentLayerIdx);
			l.copyFrom((Layer)_copiedElement.duplicate());// shallow copy
			drawView._renderer.removeFromCache(l);
			elUpdated=l;
		}
		ArrayList<DrawingElement> _layerElements = drawView.getLayerForIdx(_currentLayerIdx);
		_layerElements.clear();
		for (DrawingElement de : _copiedChildren ) {
			DrawingElement duplicate = de.duplicate();
			_layerElements.add(duplicate);
			if (selection.indexOf(de)>-1) {// NOTE : selection may not be in the same order
				newSelection.add(duplicate);
			}
		}
		elUpdated.update(true, drawView._renderer, UpdateFlags.ALL);
		
		drawView.setCurrentLayerInternal(_currentLayerIdx);
		drawView._selectionOverlay._selection.addAll(newSelection);
	}

}