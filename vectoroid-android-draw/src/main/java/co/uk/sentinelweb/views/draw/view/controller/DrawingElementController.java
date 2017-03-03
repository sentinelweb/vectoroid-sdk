package co.uk.sentinelweb.views.draw.view.controller;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PointF;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import co.uk.sentinelweb.commonlibs.Globals;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWAsyncListener;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWClickListener;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWDialogClickListener;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.controller.FontController;
import co.uk.sentinelweb.views.draw.file.FileRepository;
import co.uk.sentinelweb.views.draw.model.Drawing;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.Group;
import co.uk.sentinelweb.views.draw.model.PointVec;
import co.uk.sentinelweb.views.draw.model.Stroke;
import co.uk.sentinelweb.views.draw.model.Stroke.Type;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.model.internal.ModeData;
import co.uk.sentinelweb.views.draw.model.path.PathData;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;
import co.uk.sentinelweb.views.draw.view.DrawView.Bitwise;

public class DrawingElementController { 
	static final ArrayList<Type> SEL_TEXT_TYPES = new ArrayList<Type>();
	static {
		SEL_TEXT_TYPES.add(Stroke.Type.TEXT_TTF);
	}
	
	public class Option<Param> {
		public int _icon=-1;//R.drawable.i_none;
		public String _label;
		public OnEWAsyncListener<Param> _listener;
		public OnClickListener _clicklistener;
		
		public Option(int _icon, String _label, OnEWAsyncListener<Param> _listener) {
			super();
			this._icon = _icon;
			this._label = _label;
			this._listener = _listener;
		}
		
		public Option( String _label, OnEWAsyncListener<Param> _listener) {
			super();
			this._label = _label;
			this._listener = _listener;
		}
		
		public Option( String _label, OnClickListener _listener) {
			super();
			this._label = _label;
			this._clicklistener = _listener;
		}
	}
	
	public class OptionSelection<Param> {
		public Param _selection;
		public ArrayList<Option<Param>> _options;
		public int _titleRes = -1;
		
		public OptionSelection(Param _selection, ArrayList<Option<Param>> _options) {
			super();
			this._selection = _selection;
			this._options = _options;
		}
		
		public Option<Param> addOption (int idx,String text,OnClickListener _clicklistener ) {
			return addOption ( idx, text, -1,  _clicklistener );
			 
		}
		
		public Option<Param> addOption (int idx,String text,int icon, OnClickListener _clicklistener ) {
			Option<Param> option = new Option<Param>(text, _clicklistener);
			if (icon>-1) {
				option._icon = icon;
			}
			
			if (idx>-1) {
				_options.add(idx,option);
			} else {
				_options.add(option);
			}
			return option;
		}
		
		private Option<Param> findOption(View v) {
			Option<Param> found = null;
			for (Option<Param> o:_options) {
				if (o==v.getTag()) {found=o;}
			}
			return found;
		}
		
		public Dialog getMenu(int icon) {
			final String[] bgitems = getFromOpts(_options);
			return new AlertDialog.Builder(_act)
					.setTitle(_titleRes>-1?_titleRes:R.string.dialog_cxt_menu_title)
					.setIcon(icon)
					.setItems(bgitems,	new OnEWDialogClickListener(_act) {
						public void onEWClick(DialogInterface dialog,	int which) {
							_options.get(which)._listener.onAsync(_selection);
						}
					}).create();
		}
		
		public LinearLayout getView(int layout, LinearLayout ll, final OnClickListener _after,LinearLayout.LayoutParams llp) {
			if (layout==-1) {layout=R.layout.cxt_option;}
			//LinearLayout ll = new LinearLayout(_act);
			//ll.setOrientation(LinearLayout.VERTICAL);
			ll.removeAllViews();
			OnClickListener cl = new OnEWClickListener(_act) {
				public void onEWClick(View v) {
					//optsClick.get(which).onAsync(selection);
					//_options.get((Integer)v.getTag())._listener.onAsync(_selection);
					Option<Param> found = findOption(v);
					if (found!=null) {
						if (found._listener!=null) {
							found._listener.onAsync(_selection);
						} else if (found._clicklistener!=null) {
							found._clicklistener.onClick(v);
						}
					}
					if (_after!=null) {
						_after.onClick(v);
					}
				}
			};
			for (int i=0;i<_options.size();i++) {
				Option<Param> o = _options.get(i);
				FrameLayout fl = new FrameLayout(_act);
				FrameLayout.inflate(_act, layout, fl);
				Button b = (Button)fl.findViewById(R.id.cxt_menu_but);
				if (o._icon>-1) {
					b.setCompoundDrawablesWithIntrinsicBounds(o._icon, 0, 0, 0);
				}
				b.setText(o._label);
				b.setOnClickListener(cl);
				b.setTag(o);
				ll.addView(fl,new LinearLayout.LayoutParams(llp));
			}
			return ll;
		}
		
		public LinearLayout getView(int layout, LinearLayout ll, final OnClickListener _after,
				LinearLayout.LayoutParams llp,	int textId,int imgId
			) {
			if (layout==-1) {layout=R.layout.cxt_option;}
			//LinearLayout ll = new LinearLayout(_act);
			//ll.setOrientation(LinearLayout.VERTICAL);
			ll.removeAllViews();
			OnClickListener cl = new OnEWClickListener(_act) {
				public void onEWClick(View v) {
					//optsClick.get(which).onAsync(selection);
					//_options.get((Integer)v.getTag())._listener.onAsync(_selection);
					Option<Param> found = findOption(v);
					if (found!=null) {
						if (found._listener!=null) {
							found._listener.onAsync(_selection);
						} else if (found._clicklistener!=null) {
							found._clicklistener.onClick(v);
						}
					}
					if (_after!=null) {
						_after.onClick(v);
					}
				}

				
			};
			for (int i=0;i<_options.size();i++) {
				Option<Param> o = _options.get(i);
				FrameLayout fl = new FrameLayout(_act);
				FrameLayout.inflate(_act, layout, fl);
				if (o._icon>-1) {
					ImageView im = (ImageView) fl.findViewById(imgId);
					if (im!=null)im.setImageResource(o._icon);
				}
				TextView t = (TextView) fl.findViewById(textId);
				if (t!=null)t.setText(o._label);
				/*
				Button b = (Button)fl.findViewById(R.id.cxt_menu_but);
				if (o._icon>-1) {
					b.setCompoundDrawablesWithIntrinsicBounds(o._icon, 0, 0, 0);
				}
				b.setText(o._label);
				b.setOnClickListener(cl);
				b.setTag(i);
				*/
				View b = fl.findViewById(R.id.cxt_menu_but);
				b.setOnClickListener(cl);
				b.setTag(o);
				ll.addView(fl,new LinearLayout.LayoutParams(llp));
			}
			return ll;
		}
	}
	
	Context _act;
	DrawView _drawView;
	
	OnEWAsyncListener<Stroke> _strokeInfoListener;
	OnEWAsyncListener<Stroke> _strokeTextListener;
	OnEWAsyncListener<Stroke> _strokeTextFixWidListener;
	OnEWAsyncListener<Stroke> _strokeEditPointsListener;
	OnEWAsyncListener<Stroke> _strokeCloseListener;
	OnEWAsyncListener<Stroke> _strokeOutlineListener;
	OnEWAsyncListener<Stroke> _strokeUnCloseListener;
	OnEWAsyncListener<Stroke> _strokeHolesListener;
	OnEWAsyncListener<Stroke> _strokeFontListener;
	OnEWAsyncListener<Stroke> _strokeLockListener;
	OnEWAsyncListener<Stroke> _strokeUnlockListener;
	OnEWAsyncListener<Stroke> _strokeSmoothListener;
	
	OnEWAsyncListener<ArrayList<DrawingElement>> _strokesFontListener;
	OnEWAsyncListener<ArrayList<DrawingElement>> _groupGroupListener;
	OnEWAsyncListener<ArrayList<Stroke>> _strokesTextPathListener;
	
	OnEWAsyncListener<Group> _grpInfoListener;
	OnEWAsyncListener<Group> _grpUngroupListener;
	OnEWAsyncListener<Group> _grpFlattenListener;
	OnEWAsyncListener<Group> _grpLockListener;
	OnEWAsyncListener<Group> _grpUnlockListener;
	OnEWAsyncListener<Group> _groupFontListener;
	
	PointF _usePointF = new PointF();
	
	public DrawingElementController(DrawView drawView) {//Activity act,
		super();
		this._act = drawView.getContext();
		this._drawView = drawView;
		setupListeners();
		
	}
	
	public Dialog getSelectionMenu() {
		OptionSelection<?> result = getOptionSelectionMenu();
		if (result!=null) {
			return result.getMenu( R.drawable.i_menu );
		}
		return null;
	}
	
	public OptionSelection<?> getOptionSelectionMenu() {
		ArrayList<DrawingElement> selection = _drawView._selectionOverlay.getSelection();
		int strokes = 0;
		int groups =0;
		for (DrawingElement d:selection) {
			if (d instanceof Group) groups++;
			else if (d instanceof Stroke) strokes++;
		}
		Log.d(Globals.LOG_TAG, "selmenu: str: "+strokes+"grp:"+groups);
		OptionSelection<?> result = null;
		if (strokes==1 && groups==0) {
			Stroke s = (Stroke)selection.get(0);
			OptionSelection<Stroke> optionSelection = new OptionSelection<Stroke>(s,getStrokeOpts(s));
			optionSelection._titleRes = R.string.dialog_stroke_title;
			return optionSelection;
		} else if (groups==1 && strokes==0) {
			Group group = (Group)selection.get(0);
			OptionSelection<Group> optionSelection = new OptionSelection<Group>(group,getGroupOpts(group));
			optionSelection._titleRes = R.string.dialog_group_title;
			return optionSelection;
		} else if (groups==0 && strokes>1) {
			// show combine options, locking
			ArrayList<Stroke> sel1 =  _drawView._selectionOverlay.getAllStrokes(selection);
			OptionSelection<ArrayList<Stroke>> optionSelection = new OptionSelection<ArrayList<Stroke>>(sel1,getCombineOpts());
			optionSelection._titleRes = R.string.dialog_combine_title;
			return optionSelection;
		} else if ((groups>=1 && strokes>0) ||(groups>0 && strokes>=1)) {
			// show combine options, locking
			OptionSelection<ArrayList<DrawingElement>> optionSelection = new OptionSelection<ArrayList<DrawingElement>>(selection,getCombinedOpts(selection));
			optionSelection._titleRes = R.string.dialog_combined_title;
			return optionSelection;
		} else {
			// allow grouping,locking
		}
		return null;
	}
	public OptionSelection<Stroke> getOptionSelectionMenu(Stroke s) {
		OptionSelection<Stroke> optionSelection = new OptionSelection<Stroke>(s,getStrokeOpts(s));
		optionSelection._titleRes = R.string.dialog_stroke_title;
		return optionSelection;
	}
	public OptionSelection<Group> getOptionSelectionMenu(Group s) {
		OptionSelection<Group> optionSelection = new OptionSelection<Group>(s,getGroupOpts(s));
		optionSelection._titleRes = R.string.dialog_group_title;
		return optionSelection;
	}
	private String[] getFromOpts(ArrayList<?> opts) {
		String[] sa = new String[opts.size()];
		for (int i=0;i<opts.size();i++) {
			Option<?> o = (Option<?>)opts.get(i);
			sa[i]=o._label;
		}
		return sa;
	}
	
	public ArrayList<Option<Stroke>> getStrokeOpts(Stroke s) {
		ArrayList<Option<Stroke>> opts = new ArrayList<Option<Stroke>>();
		if (!s.locked) {
			//opts.add(_act.getString(R.string.dialog_stroke_opt_edit_points));
			//optsClick.add(_strokeEditPointsListener);
			if (s.type==Type.TEXT_TTF) {
				opts.add(new Option<Stroke>(R.drawable.i_font,_act.getString(R.string.dialog_stroke_opt_change_font), _strokeFontListener));
				opts.add(new Option<Stroke>(R.drawable.i_text,_act.getString(R.string.dialog_stroke_opt_edit_text), _strokeTextListener));
				opts.add(new Option<Stroke>(_act.getString(s.textXScale<0?R.string.dialog_stroke_opt_varwidth:R.string.dialog_stroke_opt_fixwidth), _strokeTextFixWidListener));
				
			} else {
				opts.add(new Option<Stroke>(_act.getString(R.string.dialog_combine_outline ), _strokeOutlineListener));
			}
			opts.add(new Option<Stroke>(_act.getString(R.string.dialog_stroke_opt_close), _strokeCloseListener));
			opts.add(new Option<Stroke>(_act.getString(R.string.dialog_stroke_opt_unclose), _strokeUnCloseListener));
			opts.add(new Option<Stroke>(_act.getString(!s.holesEven?R.string.dialog_stroke_opt_holes_even:R.string.dialog_stroke_opt_holes_winding), _strokeHolesListener));
			opts.add(new Option<Stroke>(R.drawable.i_smooth_stroke,_act.getString(R.string.dialog_stroke_opt_smooth), _strokeSmoothListener));
		}
		if (!s.locked) {
			opts.add(new Option<Stroke>(R.drawable.i_lock, _act.getString(R.string.dialog_stroke_opt_lock), _strokeLockListener));
		} else {
			opts.add(new Option<Stroke>(R.drawable.i_unlock, _act.getString(R.string.dialog_stroke_opt_unlock), _strokeUnlockListener));
		}
		opts.add(new Option<Stroke>(R.drawable.i_info, _act.getString(R.string.dialog_stroke_opt_info), _strokeInfoListener));
		return opts;
	}
	
	public ArrayList<Option<Group>> getGroupOpts(Group g) {
		ArrayList<Option<Group>> opts = new ArrayList<Option<Group>>();
		if (!g.locked) {
			opts.add(new Option<Group>(_act.getString(R.string.dialog_grp_opt_ungroup),_grpUngroupListener));
			opts.add(new Option<Group>(_act.getString(R.string.dialog_grp_opt_flatten),_grpFlattenListener));
		}
		if (!g.locked) {
			opts.add(new Option<Group>(R.drawable.i_lock, _act.getString(R.string.dialog_stroke_opt_lock), _grpLockListener));
		} else {
			opts.add(new Option<Group>(R.drawable.i_unlock, _act.getString(R.string.dialog_stroke_opt_unlock), _grpUnlockListener));
		}
		opts.add(new Option<Group>(R.drawable.i_info,_act.getString(R.string.dialog_grp_opt_info),_grpInfoListener));
		//opts.add(new Option<Group>(_act.getString(g.locked?R.string.dialog_stroke_opt_unlock:R.string.dialog_stroke_opt_lock),g.locked?_grpUnlockListener:_grpLockListener));
		return opts;
	}

	public ArrayList<Option<ArrayList<DrawingElement>>> getCombinedOpts(ArrayList<DrawingElement> selection) {
		ArrayList<Option<ArrayList<DrawingElement>>> opts = new ArrayList<Option<ArrayList<DrawingElement>>>();
		opts.add(new Option<ArrayList<DrawingElement>>(_act.getString(R.string.dialog_combine_group),_groupGroupListener));
		
		ArrayList<Stroke> allTextStrokes = _drawView._selectionOverlay.getAllStrokes(selection,SEL_TEXT_TYPES);
		if (allTextStrokes.size()>0) {
			opts.add(new Option<ArrayList<DrawingElement>>(R.drawable.i_font,_act.getString(R.string.dialog_stroke_opt_change_font),_strokesFontListener));
		}
		return opts;
	}
	
	public ArrayList<Option<ArrayList<Stroke>>> getCombineOpts() {
		ArrayList<Option<ArrayList<Stroke>>> opts = new ArrayList<Option<ArrayList<Stroke>>>();
		opts.add(new Option<ArrayList<Stroke>>(_act.getString(R.string.dialog_combine_join),
				new OnEWAsyncListener<ArrayList<Stroke>>(_act) {
					@Override
					public void onEWAsync(ArrayList<Stroke> request) {
						_drawView.combineSelection(DrawView.COMBINE_FLAG_NOBREAKS );
					}
				}));
		opts.add(new Option<ArrayList<Stroke>>(_act.getString(R.string.dialog_combine_join_reorder),
				new OnEWAsyncListener<ArrayList<Stroke>>(_act) {
					@Override
					public void onEWAsync(ArrayList<Stroke> request) {
						_drawView.combineSelection(DrawView.COMBINE_FLAG_REORDER );
					}
				}));
		opts.add(new Option<ArrayList<Stroke>>(_act.getString(R.string.dialog_combine_combine),
				new OnEWAsyncListener<ArrayList<Stroke>>(_act) {
					@Override
					public void onEWAsync(ArrayList<Stroke> request) {
						_drawView.combineSelection(0 );
					}
				}));
		opts.add(new Option<ArrayList<Stroke>>(_act.getString(R.string.dialog_combine_group),
				new OnEWAsyncListener<ArrayList<Stroke>>(_act) {
					@Override
					public void onEWAsync(ArrayList<Stroke> request) {
						ArrayList<DrawingElement> selection = new ArrayList<DrawingElement>(_drawView._selectionOverlay._selection);
						Group g = _drawView.group(selection,0);
						_drawView._selectionOverlay._selection.clear();
						_drawView._selectionOverlay.selectElement(g);
					}
				}));
		opts.add(new Option<ArrayList<Stroke>>(_act.getString(R.string.dialog_combine_union),
				new OnEWAsyncListener<ArrayList<Stroke>>(_act) {
					@Override
					public void onEWAsync(ArrayList<Stroke> request) {
						_drawView.bitwise(Bitwise.UNION);		
					}
				}));
		opts.add(new Option<ArrayList<Stroke>>(_act.getString(R.string.dialog_combine_diff),
				new OnEWAsyncListener<ArrayList<Stroke>>(_act) {
					@Override
					public void onEWAsync(ArrayList<Stroke> request) {
						_drawView.bitwise(Bitwise.DIFF);		
					}
				}));
		opts.add(new Option<ArrayList<Stroke>>(_act.getString(R.string.dialog_combine_xor),
				new OnEWAsyncListener<ArrayList<Stroke>>(_act) {
					@Override
					public void onEWAsync(ArrayList<Stroke> request) {
						_drawView.bitwise(Bitwise.XOR);		
					}
				}));
		opts.add(new Option<ArrayList<Stroke>>(_act.getString(R.string.dialog_combine_inter),
				new OnEWAsyncListener<ArrayList<Stroke>>(_act) {
					@Override
					public void onEWAsync(ArrayList<Stroke> request) {
						_drawView.bitwise(Bitwise.INTERSECTION);		
					}
				}));
		ArrayList<Stroke> textStrokes = _drawView._selectionOverlay.getAllStrokes(_drawView._selectionOverlay.getSelection(), Arrays.asList(new Type[]{Type.TEXT_TTF}));
		if (textStrokes.size()>0) {
			opts.add(new Option<ArrayList<Stroke>>(_act.getString(R.string.dialog_stroke_opt_textpath),	_strokesTextPathListener));
		}
		return opts;
	}

	public Dialog getStrokeInfo(final Stroke s) {
		StringBuilder sb = new StringBuilder();
		sb.append("Strokes : ");
		sb.append(s.points.size());
		sb.append("\n");
		int ctr = 0;
		boolean allClosed = true;
		boolean someClosed = false;
		for (PointVec pv:s.points) {
			ctr+=pv.size();
			allClosed = allClosed && pv.closed;
			someClosed = someClosed ||  pv.closed;
		}
		sb.append("Total points : ");
		sb.append(ctr);
		sb.append("\n");
		sb.append("All closed : ");
		sb.append(allClosed);
		sb.append("\n");
		sb.append("Some closed : ");
		sb.append(someClosed);
		sb.append("\n");
		sb.append("Type : ");
		sb.append(s.type);
		sb.append(" [Fill:");
		sb.append(s.fill.type);
		sb.append("]");
		sb.append("\n");
		sb.append(s.getInfo());
		return getInfoDialog(sb,R.string.dialog_title_stroke_info);
	}

	public Dialog getGroupInfo(final Group g) {
		StringBuilder sb = new StringBuilder();
		sb.append("Elements : ");
		sb.append(g.elements.size());
		sb.append("\n");
		sb.append("Depth : ");
		sb.append(g.getDepth());
		sb.append("\n");
		int ctr = 0;
		for (Stroke s:g.getAllStrokes()) {
			for (PointVec pv:s.points) {
				ctr+=pv.size();
			}
		}
		sb.append("Total points : ");
		sb.append(ctr);
		sb.append("\n");
		sb.append(g.getInfo());
		return getInfoDialog(sb,R.string.dialog_title_group_info);
	}
	
	public Dialog getDrawingInfo(final Drawing d) {
		StringBuilder sb = new StringBuilder();
		sb.append("Elements : ");
		sb.append(d.elements.size());
		sb.append("\n");
		sb.append("Layers : ");
		sb.append(d.layers.size());
		sb.append("\n");
		sb.append("Size : ");
		sb.append(d.size.x+" x "+d.size.y);
		sb.append("\n");
		sb.append("Background : ");
		sb.append(d.background.toString());
		sb.append("\n");
		return getInfoDialog(sb,R.string.dialog_title_drawing_info);
	}

	public Dialog getInfoDialog(StringBuilder sb,int title) {
		TextView t = new TextView(_act);
		t.setText(sb.toString());
		return new AlertDialog.Builder(_act)
			.setTitle(title)
			.setIcon(R.drawable.i_info)
			.setView(t)
			.setNegativeButton(R.string.drawview_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					
				}
			}).create();
	}
	
	private void setupListeners() {
		_strokeInfoListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				getStrokeInfo(request).show();
			}
		};
		_strokeTextListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				_drawView.editText();
				
			}
		};
		_strokeOutlineListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				
				_drawView.outline (request );	
				
			}
		};
		_strokesTextPathListener = new OnEWAsyncListener<ArrayList<Stroke>>(_act) {
			@Override
			public void onEWAsync(ArrayList<Stroke> request) {
				// get stroke
				PointVec templatePtah=null;
				for (int j=1;j<request.size();j++) {
					if (request.get(j) instanceof Stroke) {
						Stroke s= request.get(j);
						int start = 0;
						if (s.type==Type.TEXT_TTF) {start=1;}
						for (int i=start;i<s.points.size() ;i++) {
							PointVec pv = s.points.get(i);
							if (pv.size()>1) {
								templatePtah=pv;
								break;
							}
						}
						if (templatePtah!=null) {break;}
					}
				}
				if (templatePtah!=null) {
					Stroke textStroke = null;
					if (request.size()>0) {
						if (request.get(0) instanceof Stroke) {
							Stroke s =   request.get(0);
							if (s.type==Type.TEXT_TTF) {
								textStroke = s;
							}
						}
					}
					if (textStroke!=null && textStroke.points.size()>0) {
						PointVec newPath = null;
						if (textStroke.points.size()>1) {
						  newPath = textStroke.points.get(1);
						  newPath.clear();
						} else {
							newPath=new PointVec();
							textStroke.points.add(newPath);
						}
						// translate so that the first points coinicide
						for (int i=0; i<templatePtah.size();i++) {
							PathData p = new PathData();
							newPath.add(p);
							if (i==0) {
								_usePointF.set(templatePtah.get(0));
								PointUtil.mulVector(_usePointF, _usePointF, -1);
								PointUtil.addVector(textStroke.points.get(0).get(0), _usePointF, _usePointF);
							}
							PointUtil.addVector(templatePtah.get(i),p,_usePointF);
						}
						textStroke.update(false, _drawView._renderer, UpdateFlags.ALL);
						_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
						_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
						_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
						_drawView.updateDisplay();
					} else {
						Toast.makeText(c, "Select the text first ..", 500).show();
					}
				} else {
					Toast.makeText(c, "No path found, select the path second..", 500).show();
				}
			}
		};
		_strokeTextFixWidListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				request.textXScale=-request.textXScale;
				request.update(false, _drawView._renderer, UpdateFlags.ALL);
				_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
				_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				_drawView.updateDisplay();
			}
		};
		
		_strokeFontListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(final Stroke request) {
				ArrayList<DrawingElement> sel= new ArrayList<DrawingElement>();
				sel.add(request);
				_strokesFontListener.onAsync(sel );
			}
		};
		
		_groupFontListener = new OnEWAsyncListener<Group>(_act) {
			@Override
			public void onEWAsync(final Group request) {
				ArrayList<DrawingElement> sel= new ArrayList<DrawingElement>();
				sel.add(request);
				_strokesFontListener.onAsync(sel);
			}
		};
		_strokesFontListener = new OnEWAsyncListener<ArrayList<DrawingElement>>(_act) {
			
			@Override
			public void onEWAsync(final ArrayList<DrawingElement> request) {
				_drawView._fontController.getFontsDialog(_act, new OnEWClickListener(_act) {   
					
					@Override
					public void onEWClick(View v) {
						FontController fc = FileRepository.getFileRepository(null).getFontController();//_drawView.getContext(),
						for (Stroke s : _drawView._selectionOverlay.getAllStrokes(request,SEL_TEXT_TYPES)) {
							s.fontName = fc._ttFontsOrder.get((Integer)v.getTag());
							s.update(false, _drawView._renderer, UpdateFlags.ALL);
						}
						_drawView._fontController.closeDialog();
						_drawView._updateFlags[DrawView.UPDATE_CONTROLS]=true;
						_drawView._updateFlags[DrawView.UPDATE_ANCHOR]=true;
						_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
						_drawView.updateDisplay();
					}
				}).show();
			}
		};
		_groupGroupListener = new OnEWAsyncListener<ArrayList<DrawingElement>>(_act) {
			
			@Override
			public void onEWAsync(final ArrayList<DrawingElement> request) {
				Group g = _drawView.group(request,0);
				//_drawView._selectionOverlay.clear();
				_drawView._selectionOverlay._selection.clear();
				_drawView._selectionOverlay.selectElement(g);
				
			}
		};
		_strokeCloseListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				for (PointVec p : request.points) {
					p.closed=true;
				}
				//request.makePath();
				//request.updatePainters(0,_drawView._viewPort.data);
				request.update(false, _drawView._renderer, UpdateFlags.ALL);
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				_drawView.updateDisplay();
			}
		};
		/*
		_strokeUpdateListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				for (PointVec p : request.points) {
					p.closed=true;
				}
				//request.makePath();
				//request.updatePainters(0,_drawView._viewPort.data);
				request.update(false, _drawView._renderer, UpdateFlags.ALL);
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				_drawView.updateDisplay();
			}
		};
		*/
		_strokeUnCloseListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				for (PointVec p : request.points) {
					p.closed=false;
				}
				//request.makePath();
				//request.updatePainters(0,_drawView._viewPort.data);
				request.update(false, _drawView._renderer, UpdateFlags.ALL);
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				_drawView.updateDisplay();
			}
		};
		_strokeHolesListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				request.holesEven=!request.holesEven;
				//request.makePath();
				//request.updatePainters(0,_drawView._viewPort.data);
				request.update(false, _drawView._renderer, UpdateFlags.ALL);
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				_drawView.updateDisplay();
			}
		};
		_strokeSmoothListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				_drawView.addPointsInternal(request);
				_drawView.smoothPoints(request, 0.9f, ModeData.Smooth.MIDPOINT);// calls update
			}
		};
		_strokeEditPointsListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				
			}
		};
		_strokeLockListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				request.locked=true;
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				_drawView.updateDisplay();
			}
		};
		_strokeUnlockListener = new OnEWAsyncListener<Stroke>(_act) {
			@Override
			public void onEWAsync(Stroke request) {
				request.locked=false;
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				_drawView.updateDisplay();
			}
		};
		_grpInfoListener = new OnEWAsyncListener<Group>(_act) {
			@Override
			public void onEWAsync(Group request) {
				getGroupInfo(request).show();
			}
		};
		_grpUngroupListener = new OnEWAsyncListener<Group>(_act) {
			@Override
			public void onEWAsync(Group request) {
				_drawView.unGroup(request);
			}
		};
		_grpFlattenListener = new OnEWAsyncListener<Group>(_act) {
			@Override
			public void onEWAsync(Group request) {
				_drawView.flattenGroup(request);
			}
		};
		_grpLockListener = new OnEWAsyncListener<Group>(_act) {
			@Override
			public void onEWAsync(Group request) {
				request.locked=true;
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				_drawView.updateDisplay();
			}
		};
		_grpUnlockListener = new OnEWAsyncListener<Group>(_act) {
			@Override
			public void onEWAsync(Group request) {
				request.locked=false;
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				_drawView.updateDisplay();
			}
		};
	}

	
}
