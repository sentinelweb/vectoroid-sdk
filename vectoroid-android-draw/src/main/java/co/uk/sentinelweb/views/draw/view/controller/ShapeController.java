package co.uk.sentinelweb.views.draw.view.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import co.uk.sentinelweb.commonlibs.errorwrap.EWRunnable;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWAsyncListener;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWClickListener;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWDialogClickListener;
import co.uk.sentinelweb.commonlibs.numscroll.NumberScroller;
import co.uk.sentinelweb.commonlibs.numscroll.NumberScroller.OnNumberScrollerChangedListener;
import co.uk.sentinelweb.commonlibs.slider.Slider;
import co.uk.sentinelweb.commonlibs.slider.Slider.OnSliderChangeListener;
import co.uk.sentinelweb.commonlibs.slider.Slider.State;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.file.svg.importer.SVGParser;
import co.uk.sentinelweb.views.draw.model.Drawing;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.Group;
import co.uk.sentinelweb.views.draw.model.PointVec;
import co.uk.sentinelweb.views.draw.model.Stroke;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.util.StrokeUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;
import co.uk.sentinelweb.views.draw.view.ShapeView;

public class ShapeController {
	
	public enum Shape{NGON,STAR,ROSE,HYPOCYCLOID,EPICYCLOID,LISSAJOUS};
	
	Activity _act;
	DrawView _drawView;
	Dialog _shapeDialog;
	NumberScroller _dimNumberScroller;
	Slider _modifierSlider;
	CheckBox _outlineChk;
	View _polygonBut;
	View _starBut;
	View _roseBut;
	View _hypoBut;
	View _epiBut;
	View _lissBut;
	ShapeView _preview;
	OnNumberScrollerChangedListener _dimNumberChangedListener;
	//OnClickListener _polyClickListener ;
	//OnClickListener _starClickListener ;
	//OnClickListener _roseClickListener ;
	///OnClickListener _hypoClickListener ;
	//OnClickListener _epiClickListener ;
	//OnClickListener _lissClickListener ;
	OnSliderChangeListener _modChangeListener ;
	android.content.DialogInterface.OnClickListener _okListener;
	int _dimension = 5;
	float _modifier = 1;
	Shape _shapeType=Shape.NGON;
	Handler _h = new Handler();
	private EWRunnable _updShapeRunnable;
	long _lastUpdate = 0;
	File presetFile = null;
	public ShapeController(Activity act,	DrawView drawView2) {
		this._act=act;
		this._drawView=drawView2;
		createListeners();
	}
	private OnClickListener getCurveClickListener (final Shape type) {
		return new OnEWClickListener(_act) {
			@Override
			public void onEWClick(View v) {
				_shapeType=type;
				updateShape();
				updateButtons();
			}
		};
	}
	private void createListeners() {
		_dimNumberChangedListener = new OnNumberScrollerChangedListener() {
			
			@Override
			public void onChanged(int newValue, boolean fromTouch) {
				//_dimension=newValue;
				//updateShape();
			}
			
			@Override
			public void onChange(int newValue, boolean fromTouch) {
				_dimension=newValue;
				updateShape();
			}
		};
		
		_modChangeListener = new OnSliderChangeListener() {
			
			@Override
			public void onProgressChanged(Slider s, float _position, boolean fromUser) {
				if (_modifierSlider.getState()==State.STOP) {
					_modifier=_position;
					updateShape();
				} else if (_modifierSlider.getState()==State.SLIDING && (System.currentTimeMillis()-_lastUpdate)>100) {
					_modifier=_position;
					updateShape();
				}
			}
		};
		_okListener = new OnEWDialogClickListener(_act) {
			
			@Override
			public void onEWClick(DialogInterface dialog, int whichButton) {
				Stroke s = _preview.getStroke();
				Stroke newStroke = new Stroke(s.pen,s.fill);
				genShape(newStroke);
				_drawView.setShapeTemplate(newStroke);
				dialog.dismiss();
			}
		};
		_updShapeRunnable = new EWRunnable(this._act) {
			@Override
			public void doEWrun() {
				updateShape();
			}
		};
	}
	
	public Dialog getShapeDialog(Context c,int layoutId) {
		if (_shapeDialog==null) {
			FrameLayout fls = getShapeView(c, layoutId);
			
			_shapeDialog= new AlertDialog.Builder(_act)
				.setTitle(R.string.dialog_title_shape)
				.setIcon(R.drawable.i_shape)
				.setView(fls)
				.setPositiveButton(R.string.dialog_but_ok, _okListener)
				.create();
			
		}
		_dimNumberScroller.setValue(_dimension);
		_modifierSlider.setPosition(_modifier);
		_h.postDelayed(_updShapeRunnable, 200);
		updateButtons();
		return _shapeDialog;
	}
	
	private FrameLayout getShapeView(Context c, int layoutId) {
		FrameLayout fls = new FrameLayout(c);
		FrameLayout.inflate(c, layoutId, fls);
		_dimNumberScroller = (NumberScroller) fls.findViewById(R.id.shapedialog_sides_num);
		_dimNumberScroller.addNumberScrollerChangedListener(_dimNumberChangedListener);
		
		_polygonBut =  fls.findViewById(R.id.shapedialog_ngon_but);
		_polygonBut.setOnClickListener(getCurveClickListener(Shape.NGON));
		_starBut =  fls.findViewById(R.id.shapedialog_star_but);
		_starBut.setOnClickListener(getCurveClickListener(Shape.STAR));
		_roseBut =  fls.findViewById(R.id.shapedialog_rose_but);
		_roseBut.setOnClickListener(getCurveClickListener(Shape.ROSE));
		_hypoBut =  fls.findViewById(R.id.shapedialog_hypo_but);
		_hypoBut.setOnClickListener(getCurveClickListener(Shape.HYPOCYCLOID));
		_epiBut =  fls.findViewById(R.id.shapedialog_epi_but);
		_epiBut.setOnClickListener(getCurveClickListener(Shape.EPICYCLOID));
		_lissBut =  fls.findViewById(R.id.shapedialog_liss_but);
		_lissBut.setOnClickListener(getCurveClickListener(Shape.LISSAJOUS));
		
		_preview =  (ShapeView) fls.findViewById(R.id.shapedialog_preview);
		
		_outlineChk = (CheckBox) fls.findViewById(R.id.shapedialog_outline_chk);
		//_polygonBut =  fls.findViewById(R.id.shapedialog_ngon_but);
		//_starBut =  fls.findViewById(R.id.shapedialog_star_but);
		_modifierSlider = (Slider) fls.findViewById(R.id.shapedialog_mod);
		_modifierSlider.setOnSeekBarChangeListener(_modChangeListener);
		return fls;
	}
	
	public void updateButtons() {
		_polygonBut.setSelected(_shapeType==Shape.NGON);
		_starBut.setSelected(_shapeType==Shape.STAR);
		_roseBut.setSelected(_shapeType==Shape.ROSE);
		_hypoBut.setSelected(_shapeType==Shape.HYPOCYCLOID);
		_epiBut.setSelected(_shapeType==Shape.EPICYCLOID);
		_lissBut.setSelected(_shapeType==Shape.LISSAJOUS);
	}
	
	public void updateShape() {
		Log.d(DVGlobals.LOG_TAG,"updateShape():"+_preview);
		Stroke s =  _preview.getStroke();
		
		genShape(s);
		//_preview.setStroke(s);
		_preview.update();
		//_preview.set
		//_preview.fit();
		
		//_preview.invalidate();
		_lastUpdate=System.currentTimeMillis();
	}

	private void genShape(Stroke s) {
		PointVec pv = s.points.get(0);
		switch (_shapeType) {
			case NGON:StrokeUtil.generatePoly(pv, _dimension);break;
			case STAR:StrokeUtil.generateStar(pv, _dimension, _modifier);break;
			case ROSE:StrokeUtil.generateRose(pv, _dimension, _modifier);break;
			case HYPOCYCLOID:StrokeUtil.generateHypocycloid(pv, _dimension, _modifier);break;
			case EPICYCLOID:StrokeUtil.generateEpicycloid(pv, _dimension, _modifier);break;
			case LISSAJOUS:StrokeUtil.generateLissajous(pv, _dimension,(int)(_dimension/_modifier), 0);break;
		}
		s.updateBoundsAndCOG();
		StrokeUtil.scaleStroke(s, 2/Math.max(s.calculatedDim.x, s.calculatedDim.y));
	}
	
	public void processButtons(OnEWAsyncListener<View> callback) {
		callback.onAsync(_polygonBut);
		callback.onAsync(_starBut);
		callback.onAsync(_roseBut);
		callback.onAsync(_hypoBut);
		callback.onAsync(_lissBut);
		callback.onAsync(_epiBut);
	}
	
	private ArrayList<Stroke> processPresetFile() {
		if (presetFile!=null && presetFile.exists()) {
			ArrayList<Stroke> strokes = new ArrayList<Stroke>();
			try {
				InputStream is = new FileInputStream(presetFile);
				//Log.d(DVGlobals.LOG_TAG, "clipart:process:"+ci._label+":"+ci._tgtFile.getAbsolutePath());
				if (is!=null) {
					InputSource isc = new InputSource(is);
					SVGParser svgp = new SVGParser();
					Drawing d = svgp.parseSAX(isc);
					is.close();
					d.update(true, _drawView._renderer, UpdateFlags.ALL);
					Group currGrp = null;
					// RM:check layers if bg empty
					/*
					while (currGrp == null || currGrp.elements.size()==1) {
						if (currGrp == null ) {
							if (d.elements.get(0) instanceof Group) {
								currGrp = (Group)d.elements.get(0);
							}
							else {
								strokes .add((Stroke)d.elements.get(0));
								return strokes;
							}
						} else if (currGrp != null) {
							if (currGrp.elements.get(0) instanceof Group) {
								currGrp = (Group)currGrp.elements.get(0);
							}
							else {
								strokes .add((Stroke)currGrp.elements.get(0));
								return strokes;
							}
						} 
					}
					*/
				}
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	/**
	 * @return the presetFile
	 */
	public File getPresetFile() {
		return presetFile;
	}
	/**
	 * @param presetFile the presetFile to set
	 */
	public void setPresetFile(File presetFile) {
		this.presetFile = presetFile;
	}
	
	
}
