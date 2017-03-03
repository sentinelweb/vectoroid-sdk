package co.uk.sentinelweb.views.draw.undo;

import java.util.ArrayList;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.TransformOperatorInOut;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class TransformUndoElement extends UndoElement {
	public TransformOperatorInOut _transform;
	public ArrayList<Integer> _selectionPositions;
	public TransformUndoElement() {
		_undoOpType = UndoOperationType.TRANSFORM;
		_undoType = UndoType.DELTA;
	}

	@Override
	public void makeRestorePointOps(DrawView _drawView) {
		_selectionPositions = new ArrayList<Integer>();
		for (DrawingElement de : _drawView._selectionOverlay._selection) {
			int index = -1;
			if (_drawView._currentLayer==null) {
				index = _drawView._drawing.elements.indexOf(de);
			} else {
				index = _drawView.getCurrentLayer().indexOf(de);
			}
			if (index>-1) {
				_selectionPositions.add(index);
			}
		}
		Log.d(DVGlobals.LOG_TAG, "TransformUndoElement.makeRestorePointOps: "+_selectionPositions.toString());
	}

	@Override
	public void restoreRestorePointOps(DrawView drawView,boolean isUndo) {
		drawView.setCurrentLayerInternal( _currentLayerIdx );
		drawView._selectionOverlay.clear();
		for ( Integer i : _selectionPositions ) {
			DrawingElement drawingElement = drawView.getLayerForIdx(_currentLayerIdx).get(i);
			drawView._selectionOverlay._selection.add( drawingElement );
		}
		Log.d(DVGlobals.LOG_TAG, "TransformUndoElement.restoreRestorePointOps: "+drawView._selectionOverlay._selection.toString());
		TransformOperatorInOut t = _transform;
		if (isUndo) {
			t = _transform.invert();
			
			RectF refBounds = new RectF();
			// TODO need to correct the anchor point so its the same as the original....
			drawView.getBoundsForElements(refBounds, drawView._selectionOverlay._selection);
			PointF refCOG = PointUtil.midpoint(refBounds);
			//t.anchor.set(refCOG);// will need to be the anchor
			PointF correct = _transform.getTranslationForCOGAndAnchor(t.get2x2(), refCOG);
			PointF combinedTrans = new PointF();
			PointUtil.subVector(correct, combinedTrans, refCOG);
			PointUtil.addVector(combinedTrans, combinedTrans, _transform.anchor);
			PointUtil.addVector(_transform.trans, combinedTrans, combinedTrans);
			t.correctTranslation(combinedTrans);
			//t.generate();
			Log.d(DVGlobals.LOG_TAG, "TransformUndoElement.restoreRestorePointOps: "+PointUtil.tostr(_transform.trans)+":"+PointUtil.tostr(t.trans));
		} 
		for (DrawingElement de : drawView._selectionOverlay._selection) {
			drawView._transformController.transform(de, de, t);
			de.update(true, drawView._renderer, UpdateFlags.ALL);
		}
	}

}
