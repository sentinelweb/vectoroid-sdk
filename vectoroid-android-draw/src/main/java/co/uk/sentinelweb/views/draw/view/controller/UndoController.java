package co.uk.sentinelweb.views.draw.view.controller;

import java.util.ArrayList;

import android.util.Log;
import android.widget.Toast;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.model.Drawing;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.Layer;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.undo.DEListUndoElement;
import co.uk.sentinelweb.views.draw.undo.LayerAllUndoElement;
import co.uk.sentinelweb.views.draw.undo.TransformUndoElement;
import co.uk.sentinelweb.views.draw.undo.UndoElement;
import co.uk.sentinelweb.views.draw.undo.UndoElement.UndoOperationType;
import co.uk.sentinelweb.views.draw.undo.UndoElement.UndoType;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class UndoController {
	public static final int MAX_UNDOS = 20;
	
	enum UndoOperation {CLEAR, ADD, UNDO, REDO}
	UndoOperation _lastUndoOperation = UndoOperation. CLEAR;  
	UndoElement _nextUndoElement = null;
	int _undoIndex = -1;
	
	private DrawView _drawView;
	ArrayList<UndoElement> _undoStack=new ArrayList<UndoElement>(MAX_UNDOS);
	
	public UndoController(DrawView _drawView) {
		super();
		this._drawView = _drawView;
	}

	private void initUndos() {
		_undoStack=new ArrayList<UndoElement>(MAX_UNDOS);
	}
	
	public void clearUndos() {	 initUndos();	}
	
	private UndoElement makeRestorePoint() {//UndoOperationType type
		/* hack to bypass new undo management */
		//if (_nextUndoElement!=null && _nextUndoElement._undoOpType!=UndoOperationType.ALL) {_nextUndoElement=null;}
		/* end hack */
		if (_nextUndoElement==null)	initNextUndo(UndoOperationType.LAYER_ALL);
		_nextUndoElement.makeRestorePoint(_drawView);
		return _nextUndoElement;
	}

	public UndoElement initNextUndo(UndoOperationType type) {
		_nextUndoElement = UndoElement.getUndoElement(type);
		return _nextUndoElement;
	}
	
	/**
	 * @return the _nextUndoElement
	 */
	public UndoElement getNextUndoElement() {
		return _nextUndoElement;
	}

	private void restoreRestorePoint(UndoElement ue, boolean isUndo) {
		ue.restoreRestorePoint(_drawView,isUndo);
	}
	
	public void addUndo() {//UndoOperationType type
		//Log.d(DVGlobals.LOG_TAG,"addUndo():b4:undoIndex:"+undoIndex+":"+this.undoStack.size());//+dumpUndoStack()   //,new Exception("trace")
		//burn ahead
		try {
			//dumpUndos("add");
			if (_lastUndoOperation==UndoOperation.REDO) {
				//undoIndex--;
			} else if (_lastUndoOperation==UndoOperation.UNDO)  {
				_undoIndex++;
			}
			while (_undoStack.size()>_undoIndex+1) {
				this._undoStack.remove(this._undoStack.size()-1);
				//Log.d(Globals.LOG_TAG,"addUndo():rem_end:undoIndex:"+undoIndex+":"+this.undoStack.size()+dumpUndoStack());
				
			}
			while (this._undoStack.size()>=MAX_UNDOS ) {
				//Log.d(Globals.TAG,"addUndo():rem_st1:undoIndex:"+undoIndex+":"+this.undoStack.size()+dumpUndoStack());
				this._undoStack.remove(0);
				_undoIndex--;
				//Log.d(Globals.LOG_TAG,"addUndo():rem_st2:undoIndex:"+undoIndex+":"+this.undoStack.size()+dumpUndoStack());
			}
			UndoElement ue = null;
			boolean was0 = this._undoStack.size()==0;
			while (ue==null && (was0 || this._undoStack.size()>0)) {
				try {
					ue = makeRestorePoint();//type
				} catch (OutOfMemoryError e) {
					if (this._undoStack.size()>0) {
						this._undoStack.remove(0);
						_undoIndex--;
						System.gc();
					}
				}
			}
			if (ue!=null) {
				_undoStack.add(ue);
				_undoIndex=_undoStack.size()-1;
				//if (ue instanceof TransformUndoElement) {
				//	_lastUndoOperation = UndoOperation.ADD_TRANS;
				//} else {
					_lastUndoOperation = UndoOperation.ADD;
				//}
			} else {
				Toast.makeText(_drawView.getContext(),"Couldnt add undo ...",500).show();
			}
			_nextUndoElement = null;
		} catch (Exception e) {
			Log.d(DVGlobals.LOG_TAG, "!!!!!!!!! UNDO:addUndo exception:",e);
		} 
		//Log.d(DVGlobals.LOG_TAG,"!!!!!!!!! UNDO:addUndo():end:undoIndex:"+undoIndex+":"+this.undoStack.size());
		//dumpUndos("add");
	}
	
	public void addSnapshotIfLastWasDelta(DrawingElement de) {
		ArrayList<DrawingElement> list = new ArrayList<DrawingElement>();
		list.add(de);
		addSnapshotIfLastWasDelta(list);
	}
	
	public void addSnapshotIfLastWasDelta(ArrayList<DrawingElement> deList) {
		UndoType undoType = getUndoType(_undoIndex-1);
		if (undoType!=null && undoType==UndoType.DELTA) {
			((DEListUndoElement)initNextUndo(UndoOperationType.DE_LIST)).initList(_drawView, deList);
			addUndo();
		}
	}
	
	public void addSnapshotIfLastWasDelta() {
		UndoType undoType = getUndoType(_undoIndex-1);
		if (undoType!=null && undoType==UndoType.DELTA) {
			initNextUndo(UndoOperationType.LAYER_ALL);
			addUndo();
		}
	}
	
	public boolean undo(boolean callUpdate) {
		//Log.d(DVGlobals.LOG_TAG,"!!!!!!!!! UNDO:undo():b4:undoIndex"+undoIndex+":"+this.undoStack.size());
		try {
			//int moveAhead=1;
			//dumpUndos("undo");
			if (_lastUndoOperation==UndoOperation.REDO) {
				_undoIndex--;
			} else if (_lastUndoOperation==UndoOperation.ADD)  {
				UndoType ut= getUndoType(_undoIndex);
				if (ut!=null && ut==UndoType.SNAPSHOT) {
					_undoIndex--;
				} 
			}// else if (_lastUndoOperation==UndoOperation.ADD_TRANS)  {}
			if (_undoIndex>-1) {
				UndoElement ue = _undoStack.get(_undoIndex);
				_undoIndex--;
				restoreRestorePoint(ue,true);
				if (callUpdate) callUpdate();
				//Log.d(DVGlobals.LOG_TAG,"!!!!!!!!! UNDO:undo():end:undoIndex"+undoIndex+":"+this.undoStack.size());
				_lastUndoOperation = UndoOperation.UNDO;
				//dumpUndos("undo");
				return true;
			} else if (callUpdate) {
				Toast.makeText(_drawView.getContext(), R.string.toast_no_more_undos, 500).show();
			}
		} catch (Exception e) {
			Log.d(DVGlobals.LOG_TAG, "!!!!!!!!! UNDO:undo exception:",e);
		}
		//dumpUndos("undo");
		return false;
	}

	public void callUpdate() {
		_drawView.dropTouchCache();
		_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
		_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
		_drawView._updateFlags[DrawView.UPDATE_UNDO_OP]=true;
		_drawView.updateDisplay();
	}
	
	public boolean redo(boolean callUpdate) {
		//Log.d(DVGlobals.LOG_TAG,"!!!!!!!!! UNDO:redo():b4:undoIndex:"+undoIndex+":"+this.undoStack.size()); 
		try {
			int moveAhead=1;
			//dumpUndos("redo");
			if (_lastUndoOperation==UndoOperation.UNDO ) { 
				UndoType ut= getUndoType(_undoIndex+moveAhead);
				if (ut!=null && ut==UndoType.SNAPSHOT) {
					moveAhead++;
				} 
			} else if (_lastUndoOperation==UndoOperation.ADD)  {
				moveAhead++;// this shouldnt be used
			} //else if (_lastUndoOperation==UndoOperation.ADD_TRANS)  {
				//moveAhead++;
				//moveAhead--;
			//}
			if (_undoIndex<_undoStack.size()-moveAhead) {
				
				_undoIndex+=moveAhead;
				UndoElement ue = _undoStack.get(_undoIndex);
				
				restoreRestorePoint(ue,false);
				if (callUpdate) callUpdate();
				//Log.d(DVGlobals.LOG_TAG,"!!!!!!!!! UNDO:redo():end:undoIndex:"+undoIndex+":"+this.undoStack.size());
				_lastUndoOperation = UndoOperation.REDO;
				//dumpUndos("redo");
				return true;
			} else if (callUpdate) {
				Toast.makeText(_drawView.getContext(), R.string.toast_no_more_redos, 500).show();
			}
		} catch (Exception e) {
			Log.d(DVGlobals.LOG_TAG, "!!!!!!!!! UNDO:redo exception:",e);
		}
		//dumpUndos("redo");
		return false;
	}
	
	public int getAvailableUndos() {
		return _undoIndex>-1?_undoIndex:0;
	}
	public int getAvailableRedos() {
		return (_undoStack.size()+1)-_undoIndex;
	}
	public UndoElement getLastUndo() {
		if (_undoStack.size()>0) {
			return _undoStack.get(_undoStack.size()-1);
		} else return null;
	}
	public UndoType getUndoType(int index) {
		if (index>-1 && _undoStack.size()>0 && _undoStack.size()>index) {
			return _undoStack.get(index)._undoType;
		} else return null;
	}
	public boolean hasUndos() {
		if (_lastUndoOperation==UndoOperation.REDO) {
			return _undoIndex>0;
		} else if (_lastUndoOperation==UndoOperation.ADD)  {
			return _undoIndex>0;
		}
		return _undoIndex>-1;
	}
	public void  dumpUndos(String tag) {
		Log.d(DVGlobals.LOG_TAG,"!!!!!!!!! UNDO:stack:"+tag); 
		for (int i=0;i<_undoStack.size();i++) {
			Log.d(DVGlobals.LOG_TAG,"!!!!!!!!! UNDO:stack:"+_undoStack.get(i).hashCode()+" "+(_undoIndex==i?"<!--":"")); 
		}
	}
	/*
	private UndoElement makeRestorePoint(UndoOperationType type) {
		UndoElement ue = //new UndoElement();
			UndoElement.getUndoElement(type);
		ue._undoOpType=type;
		ue._currentLayerIdx=_drawView.getCurrentLayerIdx();
		ue.selection=new ArrayList<DrawingElement>();
		switch (type) {
			case ALL:
				ue._copiedElement=(Drawing)_drawView._drawing.duplicate();// copied is a deep duplicate of the drawing.
				for (DrawingElement de : _drawView._selectionOverlay._selection) {
					Drawing copiedDrawing = (Drawing)ue._copiedElement;
					if (_drawView._currentLayer==null) {
						int i = _drawView._drawing.elements.indexOf(de);
						ue.selection.add(copiedDrawing.elements.get(i));
					} else {
						int i = _drawView.getCurrentLayer().indexOf(de);
						ue.selection.add(copiedDrawing.layers.get(ue._currentLayerIdx).elements.get(i));
					}
				}
				break;
			case LAYER_ALL:
				// copied is a shallow duplicate of the drawing.
				if (ue._currentLayerIdx==-1) {
					ue._copiedElement=(Drawing)_drawView._drawing.duplicate(true);
				} else {
					ue._copiedElement=(Layer)_drawView._drawing.layers.get(ue._currentLayerIdx).duplicate(true);
				}
				ue._copiedChildren = new ArrayList<DrawingElement>();
				for (DrawingElement de : _drawView.getCurrentLayer() ) {
					ue._copiedChildren.add(de.duplicate());
				}
				for (DrawingElement de : _drawView._selectionOverlay._selection) {
					int index = -1;
					if (_drawView._currentLayer==null) {
						index = _drawView._drawing.elements.indexOf(de);
					} else {
						index = _drawView.getCurrentLayer().indexOf(de);
					}
					ue.selection.add(ue._copiedChildren.get(index));
				}
				break;
			case LAYER_CHANGE:
				// just need current layer - which is saved above
				break;
		}
		
		ue.mode=_drawView._mode.opState;
		ue.drawMode=_drawView._mode.drawMode;
		//Log.d(DVGlobals.LOG_TAG, "!!!!!!!!! UNDO:  makeRestorePoint: type:"+ue._undoOpType+"_currentLayerIdx: "+ue._currentLayerIdx+" sz:"+_drawView.getCurrentLayer().size());
		
		ue.fill=_drawView.getCurrentFill().duplicate();
		ue.pen=_drawView.getCurrentPen().duplicate();
		return ue;
	}
	
	private void restoreRestorePoint(UndoElement ue) {
		_drawView._selectionOverlay._selection.clear();
		ArrayList<DrawingElement> newSelection = new ArrayList<DrawingElement>();
		switch (ue._undoOpType) {
			case ALL:
				
				_drawView._drawing.copyFrom((Drawing)ue._copiedElement.duplicate());//full copy
				if (ue.selection.size()>0) {
					ArrayList<DrawingElement> _layerArray = ue._currentLayerIdx==-1?_drawView._drawing.elements:_drawView._drawing.layers.get(ue._currentLayerIdx).elements;
					for (DrawingElement de:ue.selection) {
						int idx = ue.selection.indexOf(de);
						newSelection.add( _layerArray.get(idx) );
					}
				}
				_drawView._renderer.dropCache();
				_drawView._drawing.update(true, _drawView._renderer, UpdateFlags.ALL);
				break;
			case LAYER_ALL:
				DrawingElement elUpdated = null;
				if (ue._currentLayerIdx==-1) {
					Drawing d = _drawView._drawing;
					elUpdated=d;
					((Drawing)elUpdated).copyFrom((Drawing)ue._copiedElement.duplicate());// shallow copy
					_drawView._renderer.renderObjects.remove(d);
					_drawView._renderer.removeFromCache(d.elements);
				} else {
					Layer l = _drawView._drawing.layers.get(ue._currentLayerIdx);
					l.copyFrom((Layer)ue._copiedElement.duplicate());// shallow copy
					_drawView._renderer.removeFromCache(l);
					elUpdated=l;
				}
				ArrayList<DrawingElement> _layerElements = _drawView.getLayerForIdx(ue._currentLayerIdx);
				_layerElements.clear();
				for (DrawingElement de : ue._copiedChildren ) {
					DrawingElement duplicate = de.duplicate();
					_layerElements.add(duplicate);
					if (ue.selection.indexOf(de)>-1) {// NOTE : selection may not be in the same order
						newSelection.add(duplicate);
					}
				}
				elUpdated.update(true, _drawView._renderer, UpdateFlags.ALL);
				break;
			case LAYER_CHANGE:
				// just need current layer - which is set below
				// TODO will selection be preserved here?
				break;
		}
		//_drawView._drawing.copyFrom(ue.d);
		_drawView.setCurrentLayerInternal(ue._currentLayerIdx);
		//Log.d(DVGlobals.LOG_TAG, "!!!!!!!!! UNDO:  restoreRestorePoint: type:"+ue._undoOpType+" _currentLayerIdx: "+ue._currentLayerIdx+" ("+_drawView.getCurrentLayerIdx()+") sz:"+_drawView.getCurrentLayer().size());
		//_drawView._selectionOverlay._selection.clear();
		_drawView._selectionOverlay._selection.addAll(newSelection);
		
		//_drawView._mode.drawMode=ue.drawMode;
		//_drawView._mode.opState=ue.mode;
		
		//_drawView.copyFillToCurrent(ue.fill);
		//_drawView.copyPenToCurrent(ue.pen);
		//if ( ue._currentLayer > -1 && ue._currentLayer < _drawView._drawing.layers.size() ) {
			//_drawView._currentLayer = _drawView._drawing.layers.get(ue._currentLayer);
			
		//}
		//_drawView._drawing.update(_drawView._viewPort.data);
		
	}
	*/
}
