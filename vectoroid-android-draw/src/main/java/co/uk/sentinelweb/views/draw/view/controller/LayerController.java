package co.uk.sentinelweb.views.draw.view.controller;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWClickListener;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWDialogClickListener;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.model.Layer;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class LayerController {
	
	DrawView _drawView;
	
	View.OnClickListener layerAddClick;
	View.OnClickListener layerDelClick;
	View.OnClickListener layerInfoClick;
	View.OnClickListener layerRaiseClick;
	View.OnClickListener layerLowerClick;
	View.OnClickListener layerVisibleClick;
	View.OnClickListener layerLockedClick;
	
	public LayerController(DrawView d) {
		super();
		this._drawView = d;
		initListeners(d.getContext());
	}
	
	public Dialog getLayerSelectDialog(Context c, OnClickListener setClick) {
		if (setClick==null) {
			setClick = new OnEWDialogClickListener(c) {
				public void onEWClick(DialogInterface dialog, int which) {
					int ls = _drawView._drawing.layers.size();
					if (which < ls+1) {
						_drawView.setCurrentLayer(which);
					} else if (which == ls+1) {
						layerAddClick.onClick(null);
					}
				}
			};
		}
		final String layerNames[] = new String[_drawView._drawing.layers.size()+2];
		
		int ctr=0;
		for (Layer l : _drawView._drawing.layers) {
			layerNames[ctr++]=l.getId();
		}
		layerNames[ctr++] = c.getString(R.string.dialog_layer_opt_drawing);
		layerNames[ctr++] = c.getString(R.string.dialog_layer_add);
		Dialog fontsDialog=new AlertDialog.Builder(c)
			.setTitle(R.string.dialog_title_select_layer)
			.setIcon(R.drawable.i_layers) 
			.setItems(layerNames,setClick).create();
		
		return fontsDialog;
	}  
	
	public Dialog getLayerDialog(Context c) {
		final ArrayList<String> opts = new ArrayList<String>();
		final ArrayList<View.OnClickListener> optsListeners = new ArrayList<View.OnClickListener>();
		
		Layer l = _drawView._currentLayer;
		if (l!=null) {
			opts.add(c.getString(R.string.dialog_layer_del_current));
			optsListeners.add(layerAddClick);
			opts.add(c.getString(R.string.dialog_layer_raise_current));
			optsListeners.add(layerRaiseClick);
			opts.add(c.getString(R.string.dialog_layer_lower_current));
			optsListeners.add(layerLowerClick);
			opts.add(c.getString(l.visible?R.string.dialog_layer_hide_current:R.string.dialog_layer_unhide_current));
			optsListeners.add(layerVisibleClick);
			opts.add(c.getString(l.locked?R.string.dialog_layer_unlock_current:R.string.dialog_layer_lock_current));
			optsListeners.add(layerLockedClick);
			opts.add(c.getString(R.string.dialog_layer_info));
			optsListeners.add(layerInfoClick);
		} else {
			opts.add(c.getString(R.string.dialog_title_drawing_info));
			optsListeners.add(layerInfoClick);
		}
		final String optionNames[] =  opts.toArray(new String[opts.size()]);
		Dialog fontsDialog=new AlertDialog.Builder(c)
			.setTitle(R.string.dialog_title_layer_operations)
			.setIcon(R.drawable.i_layers) 
			.setItems(optionNames,new OnEWDialogClickListener(c) {
				public void onEWClick(DialogInterface dialog, int which) {
					optsListeners.get(which).onClick(null);
				}
			}).create();
		
		return fontsDialog;
	}
	
	public Dialog getLayerInfo(Layer l) {
		StringBuilder sb = new StringBuilder();
		sb.append("Elements : ");
		sb.append(l.elements.size());
		sb.append("\n");
		sb.append(l.getInfo());
		return _drawView._drawingElementController.getInfoDialog(sb,R.string.dialog_title_layer_info);
	}
	
	private void initListeners(Context c) {
		layerAddClick = new OnEWClickListener(c) {
			public void onEWClick(View dialog) {
				Layer l = new Layer();
				_drawView.addLayer(l);
			}
		};
		layerDelClick = new OnEWClickListener(c) {
			public void onEWClick(View dialog) {
				_drawView.deleteLayer(_drawView._currentLayer);
				
			}
		};
		layerInfoClick = new OnEWClickListener(c) {
			public void onEWClick(View dialog) {
				Layer l = _drawView._currentLayer;
				if (l!=null) {
					getLayerInfo(l).show();
				} else {
					_drawView._drawingElementController.getDrawingInfo(_drawView._drawing).show();
				}
			}
		};
		layerRaiseClick = new OnEWClickListener(c) {
			public void onEWClick(View dialog) {
				_drawView.raiseLayer(_drawView._currentLayer);
				
			}
		};
		layerLowerClick = new OnEWClickListener(c) {
			public void onEWClick(View dialog) {
				_drawView.lowerLayer(_drawView._currentLayer);
				
			}
		};
		layerVisibleClick = new OnEWClickListener(c) {
			public void onEWClick(View dialog) {
				Layer l = _drawView._currentLayer;
				if (l!=null) {
					_drawView.toggleVisibleLayer(l);
				}
			}
		};
		layerLockedClick = new OnEWClickListener(c) {
			public void onEWClick(View dialog) {
				Layer l = _drawView._currentLayer;
				if (l!=null) {
					_drawView.toggleLockedLayer(l);
				}
			}
		};
	}
}
