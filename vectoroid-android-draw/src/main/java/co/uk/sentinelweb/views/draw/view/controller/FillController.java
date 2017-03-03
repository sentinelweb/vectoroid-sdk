package co.uk.sentinelweb.views.draw.view.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.graphics.PointF;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import co.uk.sentinelweb.commonlibs.colorpicker.ColorPickerDialog;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWAsyncListener;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWItemSelectedListener;
import co.uk.sentinelweb.commonlibs.globutton.GlowButton;
import co.uk.sentinelweb.commonlibs.slider.GradientEditor;
import co.uk.sentinelweb.commonlibs.slider.Slider;
import co.uk.sentinelweb.commonlibs.slider.Slider.State;
import co.uk.sentinelweb.views.draw.model.Fill;
import co.uk.sentinelweb.views.draw.model.Fill.Type;
import co.uk.sentinelweb.views.draw.model.Gradient;
import co.uk.sentinelweb.views.draw.model.GradientData;
import co.uk.sentinelweb.views.draw.model.internal.ModeData.Mode;
import co.uk.sentinelweb.views.draw.util.OnAsyncListener;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class FillController {
	private static final int BMP_CTNR_IDX = 1;
	private static final int BMP_BUT_IDX = 0;
	private static final int BMP_ALPHA_IDX = 2;
	private static final int GRAD_CTNR_IDX = 3;
	private static final int GRAD_EDIT_IDX = 0;
	private static final int GRAD_BUT_IDX = 1;
	private static final int GRAD_TYPE_IDX = 2;
	private static final int COL_CTR_IDX = 1;
	private static final int COL_BUT_IDX = 0;
	private static final int SEL_IDX = 0;
	private  static final int CPICK_COL = 0;
	public enum FillComponents {
		SELECT, STROKE_COLOUR, COLOUR, GRADIENT, BITMAP
	};
	private boolean _backGroundMode = false;
	private DrawView _drawView;
	private Activity _act;
	private ColorPickerDialog _colorPickerDialog;
	private Gradient _g;
	private Fill _fill;
	private Spinner _select;
	private GlowButton _colourButton;
	private GlowButton _gradModeButton;
	private GradientEditor _gradEditor;
	private Spinner _gradTypeSpinner;
	private ViewGroup _gradCtnr;
	private ViewGroup _bmpCtnr;
	private ViewGroup _colourCtnr;
	private GlowButton _bmpButton;
	private Slider _bmpAlphaSlider;
	OnEWAsyncListener<GradientData> _gradDataListener;
	static List<Gradient.Type> gradTypeLookup = Arrays.asList(Gradient.Type.values());
	private GradientData _currentGradientData = null;
	private OnAsyncListener<Integer> _bitmapListener;
	
	public FillController(Activity act,	DrawView drawView2, HashMap<FillComponents, List<Integer>> fillMap) {
		this._act=act;
		this._drawView=drawView2;
		setup(fillMap);
		_fill=new Fill();
		_g=new Gradient();
		_g.colors = new int[]{0,0};
		_g.positions = new float[]{0,1};
		_fill._gradient=_g;
		setFill(_drawView.getCurrentFill());
		_gradDataListener = new OnEWAsyncListener<GradientData>(_act) {

			@Override
			public void onEWAsync(GradientData request) {
				setCurrentGradientData(request.p1,request.p2);
				
			}
		};
	}

	private void setup(HashMap<FillComponents, List<Integer>> map) {
		 _colorPickerDialog = new ColorPickerDialog(_act, colourPickerListener);
		 for (FillComponents f:map.keySet()) {
		    	switch (f) {
			    	case SELECT:
			    		_select = (Spinner) _act.findViewById(map.get(f).get(SEL_IDX));
			    		ArrayAdapter<Type> nkadapter = new ArrayAdapter<Type>(_act, android.R.layout.simple_spinner_dropdown_item, Type.values());
			            _select.setAdapter(nkadapter);
			    		_select.setOnItemSelectedListener(typeSelectItemSelectedListener);
			    		break;
			    	case COLOUR:
			    		_colourButton=(GlowButton) _act.findViewById(map.get(f).get(COL_BUT_IDX));
			    		_colourButton.setOnClickListener(colourClickListener);
			    		_colourButton.fixColours=true;
			    		_colourCtnr=(ViewGroup) _act.findViewById(map.get(f).get(COL_CTR_IDX));
			    		break;
			    	case GRADIENT:
			    		_gradTypeSpinner = (Spinner) _act.findViewById(map.get(f).get(GRAD_TYPE_IDX));
			    		ArrayAdapter< Gradient.Type> grtyadapter = new ArrayAdapter< Gradient.Type >(_act, android.R.layout.simple_spinner_dropdown_item, Gradient.Type.values());
			    		_gradTypeSpinner.setAdapter(grtyadapter);
			    		_gradTypeSpinner.setOnItemSelectedListener(gradTypeSelectItemSelectedListener);
			    		
			    		_gradModeButton=(GlowButton) _act.findViewById(map.get(f).get(GRAD_BUT_IDX));
			    		_gradModeButton.setOnClickListener(gModeClickListener);
			    		//gradModeButton.fixColours=true;
			    		
			    		_gradEditor = (GradientEditor) _act.findViewById(map.get(f).get(GRAD_EDIT_IDX));
			    		_gradEditor.setOnSeekBarChangeListener(gradSldrChangeListener);
			    		
			    		_gradCtnr=(ViewGroup) _act.findViewById(map.get(f).get(GRAD_CTNR_IDX));
			    		break;
			    	case BITMAP:
			    		_bmpButton=(GlowButton) _act.findViewById(map.get(f).get(BMP_BUT_IDX));
			    		_bmpButton.setOnClickListener(_bmpClickListener);
			    		
			    		_bmpCtnr=(ViewGroup) _act.findViewById(map.get(f).get(BMP_CTNR_IDX));
			    		_bmpAlphaSlider = (Slider)_act.findViewById(map.get(f).get(BMP_ALPHA_IDX));
			    		_bmpAlphaSlider.setOnSeekBarChangeListener(_bmpAlphaSldrChangeListener);
			    		break;
		    	}
		 }
	}
	
	public void setFill(Fill f) {
		_fill = f.duplicate();
		typeSelectItemSelectedListener.fromUser=false;
		_select.setSelection(Fill.typeLookup.indexOf(_fill.type));
		//typeSelectItemSelectedListener.disableUpdates=false;// get unset in the listener (which is called from a looper)
		
		setButtonColour(_colourButton,_fill._color);
		this._g=_fill._gradient;
		if (_fill._gradient!=null) {
			if (_fill._gradient.colors!=null) {
				_gradEditor.setGradient(_fill._gradient.colors, _fill._gradient.positions);
			}
			gradTypeSelectItemSelectedListener.fromUser=false;
			_gradTypeSpinner.setSelection(gradTypeLookup.indexOf(_fill._gradient.type));
			
			//gradTypeSelectItemSelectedListener.disableUpdates=false;
			setCurrentGradientData(_fill._gradient.data);
		} else {
			_fill._gradient = new Gradient();
			this._g=_fill._gradient;
			_g.colors = new int[]{0,0};
			_g.positions = new float[]{0,1};
			_currentGradientData=null;
			_bmpAlphaSlider.setPosition(_fill._bitmapAlpha);
		}
		_g.data=null;
		showSelectedViews();
	}
	
	private void showSelectedViews() {
		if (_colourCtnr!=null)_colourCtnr.setVisibility(View.GONE);
		if (_gradCtnr!=null)_gradCtnr.setVisibility(View.GONE);
		if (_bmpCtnr!=null)_bmpCtnr.setVisibility(View.GONE);
		Type type = Type.values()[_select.getSelectedItemPosition()];
		switch (type) {
			case COLOUR:
				if (_colourCtnr!=null) _colourCtnr.setVisibility(View.VISIBLE);
				break;
			case BITMAP:
				if (_bmpCtnr!=null)_bmpCtnr.setVisibility(View.VISIBLE);
				break;
			case GRADIENT:
				if (_gradCtnr!=null)_gradCtnr.setVisibility(View.VISIBLE);
				break;
		}
	}
	
	
	private void setButtonColour(GlowButton but, int color) {
		if (but!=null) {
			but.setTextColour(color);
			but.setGlowColour(color);
		}
	}
	
	ColorPickerDialog.OnColorChangedListener colourPickerListener = new ColorPickerDialog.OnColorChangedListener() {
  		@Override 
		public void colorChanged(int color) {
  			if (_colorPickerDialog.index==CPICK_COL) {
  				_colourButton.setTextColour(color);
  				_colourButton.setGlowColour(color);
  				_fill._color=color;
  			}
  			if (!_backGroundMode) {
	  			_drawView.setFillSelection(_fill);
	  			_drawView.copyFillToCurrent(_fill);
  			} else {
  				_drawView.setBackGround(_fill);
  			}
  		}
	};
	
	
	OnClickListener colourClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			_colorPickerDialog.index = CPICK_COL;
			_colorPickerDialog.show();
			_colorPickerDialog.setColor(((GlowButton)v).glowColour);
		}
	};
	
	
	OnClickListener gModeClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (_drawView._mode.opState==Mode.GRADIENT) {
				_drawView._mode.reverseState();
			} else {
				_drawView._mode.setReversibleState(Mode.GRADIENT);
				_drawView._gradientOverlay.initPoints(_backGroundMode, 
						_currentGradientData, 
						gradTypeLookup.get(_gradTypeSpinner.getSelectedItemPosition()),
						_gradDataListener
					);
			}
			_drawView.updateDisplay();
		}
	};
	
	Vector<GradientEditor.GradientElement> _gradCopy = new Vector<GradientEditor.GradientElement>();
	private GradientEditor.OnSliderChangeListener  gradSldrChangeListener = new GradientEditor.OnSliderChangeListener() {
		@Override
		public void onProgressChanged(GradientEditor v, boolean fromUser) {
			if (fromUser) {
				_gradEditor.getGradient(_gradCopy);
				if (_g.colors.length!=_gradCopy.size()) {
					_g.colors = new int[_gradCopy.size()];
					_g.positions = new float[_gradCopy.size()];
				}
				for (int i=0;i<_gradCopy.size();i++) {
					GradientEditor.GradientElement ge = _gradCopy.get(i);
					_g.colors[i]=ge.color;
					_g.positions[i]=ge.position;
				}
				if (!_backGroundMode) {
					_drawView.setFillSelection(_fill);
					_drawView.copyFillToCurrent(_fill);
				} else {
					_drawView.setBackGround(_fill);
				}
			}
		}
	};
	
	
	ItemSelectedListener typeSelectItemSelectedListener = new ItemSelectedListener(_act) {
		@Override
		public void onEWNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onEWItemSelected(AdapterView<?> arg0,View arg1, int pos, long arg3) {
			_fill.type=Type.values()[pos];
			if (!_backGroundMode) {_drawView.copyFillToCurrent(_fill);}
			if (!fromUser) {
				if (!_backGroundMode) {
					_drawView.setFillSelection(_fill);
				} else {
					_drawView.setBackGround(_fill);
				}
				fromUser=false;
			}
			showSelectedViews();
		}
	};
	
	ItemSelectedListener gradTypeSelectItemSelectedListener = new ItemSelectedListener(_act){
		@Override
		public void onEWNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onEWItemSelected(AdapterView<?> arg0,View arg1, int pos, long arg3) {
			_g.type=Gradient.Type.values()[pos];
			if (!_backGroundMode) {_drawView.copyFillToCurrent(_fill);}
			if (fromUser) {//RM 111011: remove ! (negate)
				if (!_backGroundMode) {
					_drawView.setFillSelection(_fill);
				} else {
					_drawView.setBackGround(_fill);
				}
			} else {
				fromUser = true;
			}
		}
	};
	
	private class ItemSelectedListener extends OnEWItemSelectedListener {
		public boolean fromUser=true;
		private ItemSelectedListener(Context c) {
			super(c);
		}
		
		@Override
		public void onEWNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onEWItemSelected(AdapterView<?> arg0,View arg1, int pos, long arg3) {
			
		}
	}
	
	/**
	 * @return the backGroundMode
	 */
	public boolean isBackGroundMode() {
		return _backGroundMode;
	}

	/**
	 * @param backGroundMode the backGroundMode to set
	 */
	public void setBackGroundMode(boolean backGroundMode) {
		this._backGroundMode = backGroundMode;
	}

	/**
	 * @return the currentGradientData
	 */
	public GradientData getCurrentGradientData() {
		return _currentGradientData;
	}

	/**
	 * @param _currentGradientData the currentGradientData to set
	 */
	public void setCurrentGradientData(PointF p1,PointF p2) {
		if (this._currentGradientData==null) {this._currentGradientData=new GradientData(p1, p2);}
		else {this._currentGradientData.set(p1, p2);}
	}
	
	public void setCurrentGradientData(GradientData gd) {
		this._currentGradientData=gd!=null?gd.duplicate():null;
	}

	/**
	 * @return the _bitmapListener
	 */
	public OnAsyncListener<Integer> getBitmapListener() {
		return _bitmapListener;
	}

	/**
	 * @param _bitmapListener the _bitmapListener to set
	 */
	public void setBitmapListener(OnAsyncListener<Integer> bitmapListener) {
		this._bitmapListener = bitmapListener;
	}
	
	OnClickListener _bmpClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (_fill.type==Type.BITMAP) {
				_bitmapListener.onAsync(0);
			}
		}
	};
	private Slider.OnSliderChangeListener  _bmpAlphaSldrChangeListener = new Slider.OnSliderChangeListener() {
		@Override
		public void onProgressChanged(Slider v, float progress, boolean fromUser) {
			_fill._bitmapAlpha=(int)progress;
			if (fromUser) {
				if (v.getState()==State.STOP) {
					_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				}
				if (!_backGroundMode) {
					_drawView.setFillSelection(_fill);
				} else {
					_drawView.setBackGround(_fill);
				}
			}
		}
	};
}
