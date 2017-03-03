package co.uk.sentinelweb.views.draw.view.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import co.uk.sentinelweb.commonlibs.colorpicker.ColorPickerDialog;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWClickListener;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWDialogClickListener;
import co.uk.sentinelweb.commonlibs.globutton.GlowButton;
import co.uk.sentinelweb.commonlibs.slider.Slider;
import co.uk.sentinelweb.commonlibs.slider.Slider.State;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.model.Pen;
import co.uk.sentinelweb.views.draw.model.Pen.PenField;
import co.uk.sentinelweb.views.draw.model.StrokeDecoration;
import co.uk.sentinelweb.views.draw.model.StrokeDecoration.BreakType;
import co.uk.sentinelweb.views.draw.model.StrokeDecoration.Tip;
import co.uk.sentinelweb.views.draw.model.StrokeDecoration.Tip.Type;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class PenController {
	protected static final int CPICK_STROKE = 0;
	protected static final int CPICK_GLOW = 1;
	private ImageView _strColBut;
	private ImageView _gloColBut;
	
	private ImageView _capButtBut;
	private ImageView _capRoundBut;
	private ImageView _capSquareBut;
	private ImageView _joinBevelBut;
	private ImageView _joinRoundBut;
	private ImageView _joinMitreBut;
	private CheckBox _embEnableChk;
	
	private Slider _strSldr;
	private Slider _gloSldr;
	private Slider _alphaSldr;
	private Slider _roundSldr;
	private Slider _embAmbSldr;
	private Slider _embSpecSldr;
	private Slider _embRadSldr;
	
	//stroke ends
	private ImageView _tipNoneBut;
	private ImageView _tipArrowBut;
	private ImageView _tipDblArrowBut;
	private ImageView _tipCircleBut;
	private ImageView _tipSquareBut;
	private ImageView _tipDiamondBut;
	private CheckBox _tipClosedChk;
	private CheckBox _tipFillChk;
	private CheckBox _tipInsideChk;
	private ImageView _tipSmallBut;
	private ImageView _tipMedBut;
	private ImageView _tipLargeBut;
	
	StrokeDecoration.Tip _start = null;
	StrokeDecoration.Tip _end = null;
	StrokeDecoration.Tip _selected = null;
	
	private ImageView _startBut;
	private ImageView _endBut;
	private ImageView _breakBut;
	private boolean _isStrokeTipStartDialog;
	
	private ColorPickerDialog _colorPickerDialog;
	private Dialog _strokeTipDialog;
	private Dialog _breakDialog;
	
	private DrawView _drawView;
	
	private Activity _act;
	
	
	
	
	public PenController(Activity a, DrawView drawView, HashMap<PenField,List<Integer>> map) {
		super();
		this._drawView = drawView;
		_act=a;
		setupPen(map);
		makeStrokeEndDialog(a);
		setPen(_drawView.getCurrentPen());
	}
	
	private void makeStrokeEndDialog(Activity c) {
		FrameLayout fls = new FrameLayout(c);
		FrameLayout.inflate(c, R.layout.arrows, fls);
		_tipNoneBut = (GlowButton)fls.findViewById(R.id.arr_none);
		_tipNoneBut.setOnClickListener(getEndButListener(Type.NONE));
		_tipArrowBut = (GlowButton)fls.findViewById(R.id.arr_sgl_but);
		_tipArrowBut.setOnClickListener(getEndButListener(Type.ARROW));
		_tipDblArrowBut = (GlowButton)fls.findViewById(R.id.arr_dbl_but);
		_tipDblArrowBut.setOnClickListener(getEndButListener(Type.DBL_ARROW));
		_tipSquareBut = (GlowButton)fls.findViewById(R.id.arr_sq_but);
		_tipSquareBut.setOnClickListener(getEndButListener(Type.SQUARE));
		_tipDiamondBut = (GlowButton)fls.findViewById(R.id.arr_diamond_but);
		_tipDiamondBut.setOnClickListener(getEndButListener(Type.DIAMOND));
		_tipCircleBut = (GlowButton)fls.findViewById(R.id.arr_circle_but);
		_tipCircleBut.setOnClickListener(getEndButListener(Type.CIRCLE));
		_tipClosedChk = (CheckBox)fls.findViewById(R.id.arr_closed_chk);
		_tipFillChk = (CheckBox)fls.findViewById(R.id.arr_fill_chk);
		_tipInsideChk = (CheckBox)fls.findViewById(R.id.arr_inside_chk);
		_tipSmallBut = (GlowButton)fls.findViewById(R.id.arr_sml_but);
		_tipSmallBut.setOnClickListener(getEndSizeButListener(1));
		_tipMedBut = (GlowButton)fls.findViewById(R.id.arr_med_but);
		_tipMedBut.setOnClickListener(getEndSizeButListener(2));
		_tipLargeBut = (GlowButton)fls.findViewById(R.id.arr_lrg_but);
		_tipLargeBut.setOnClickListener(getEndSizeButListener(3));
		_strokeTipDialog=new AlertDialog.Builder(c)
			.setView(fls)
			.setTitle(R.string.dialog_title_stoke_tip)
			.setIcon(R.drawable.i_none)
			.setPositiveButton(R.string.dialog_but_ok,new OnEWDialogClickListener(c) {
				public void onEWClick(DialogInterface dialog,int which) {
					Tip selected=_isStrokeTipStartDialog?_start:_end;
					if (selected!=null) {
						selected.closed=_tipClosedChk.isChecked();
						selected.filled=_tipFillChk.isChecked();
						selected.inside=_tipInsideChk.isChecked();
					}
					PenField field = _isStrokeTipStartDialog?PenField.STARTTIP:PenField.ENDTIP;
					setSelectedTipTypeIcon(selected, _isStrokeTipStartDialog);
					_drawView.getCurrentPen().setField(field, selected);
					_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
					_drawView.setPenSelection(field, selected);
				}
			}).create();
	}
	
	private void _showStrokeEndDialog(boolean _isStrokeStartDialog) {
		this._isStrokeTipStartDialog=_isStrokeStartDialog;
		_strokeTipDialog.setTitle(
			_act.getResources().getString(R.string.dialog_title_stoke_tip)+" "+_act.getResources().getString(_isStrokeStartDialog?R.string.start:R.string.end)
		);
		Tip selected=_isStrokeTipStartDialog?_start:_end;
		if (selected!=null) {
			setSelectedTipType(selected.type);
			_tipClosedChk.setChecked(selected.closed);
			_tipFillChk.setChecked(selected.filled);
			_tipInsideChk.setChecked(selected.inside);
			setTipSizeBut(selected.size);
		}
		_strokeTipDialog.show();
	}
	
	private void _showBreakDialog() {
		if (_breakDialog==null) {
			final String[] breakItems=new String[]  {
					_act.getResources().getString(R.string.dialog_break_opt_solid),
					_act.getResources().getString(R.string.dialog_break_opt_dot),
					_act.getResources().getString(R.string.dialog_break_opt_dashed),
					_act.getResources().getString(R.string.dialog_break_opt_dotdash)
			};
			_breakDialog = new AlertDialog.Builder(_act)
				.setTitle(R.string.dialog_break_title)
				.setIcon(R.drawable.i_none)
				.setItems(breakItems,	new OnEWDialogClickListener(_act) {
					public void onEWClick(DialogInterface dialog,	int which) {
						BreakType bt = BreakType.SOLID;
						switch (which) {
							case 0:break;
							case 1:bt=BreakType.DOT;break;
							case 2:bt=BreakType.DASH;break;
							case 3:bt=BreakType.DOT_DASH;break;
						}
						setSelectedBreakIcon(bt);
						_drawView.getCurrentPen().setField(PenField.BREAKTYPE, bt);
						_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
						_drawView.setPenSelection(PenField.BREAKTYPE, bt);
					}
				}).create();
		}
		_breakDialog.show();
	}
	
	public void setupPen(HashMap<PenField,List<Integer>> map) {
	    _colorPickerDialog = new ColorPickerDialog(_act, colourPickerListener);
	    for (PenField f:map.keySet()) {
	    	switch (f) {
		    	case STROKE_WIDTH:
		    		_strSldr = (Slider) _act.findViewById(map.get(f).get(0));
		    		_strSldr.setOnSeekBarChangeListener(strSldrChangeListener);
					break;
				case STROKE_COLOUR:
					_strColBut = (GlowButton) _act.findViewById(map.get(f).get(0));
					_strColBut.setOnClickListener(strColClickListener);
					//_strColBut.fixColours=true;
					break;
				case GLOW_WIDTH:
					_gloSldr = (Slider) _act.findViewById(map.get(f).get(0));
		    		_gloSldr.setOnSeekBarChangeListener(gloSldrChangeListener);
					break;
				case GLOW_COLOUR:
					_gloColBut = (GlowButton) _act.findViewById(map.get(f).get(0));
					_gloColBut.setOnClickListener(gloColClickListener); 
					//_gloColBut.fixColours=true;
					break;
				//case ALPHA:
				//	_alphaSldr = (Slider) _act.findViewById(map.get(f).get(0));
				//	_alphaSldr.setOnSeekBarChangeListener(alphaSldrChangeListener);
					//alphaVal = (TextView)act.findViewById(map.get(f).get(1));
				//	break;
				case ROUNDING:
					_roundSldr = (Slider) _act.findViewById(map.get(f).get(0));
					_roundSldr.setOnSeekBarChangeListener(roundSldrChangeListener);
					break;
				case EMBOSS:
					_embEnableChk = (CheckBox) _act.findViewById(map.get(f).get(0));
					_embEnableChk.setOnClickListener(embChkClickListener);
					break;
				case EMBOSS_AMBIENT:
					_embAmbSldr = (Slider) _act.findViewById(map.get(f).get(0));
					_embAmbSldr.setOnSeekBarChangeListener(embAmbSldrChangeListener);
					break;
				case EMBOSS_SPECULAR:
					_embSpecSldr = (Slider) _act.findViewById(map.get(f).get(0));
					_embSpecSldr.setOnSeekBarChangeListener(embSpecSldrChangeListener);
					break;
				case EMBOSS_RADIUS:
					_embRadSldr = (Slider) _act.findViewById(map.get(f).get(0));
					_embRadSldr.setOnSeekBarChangeListener(embRadSldrChangeListener);
					break;
				case CAP:
					_capButtBut = (GlowButton) _act.findViewById(map.get(f).get(0));
					_capButtBut.setOnClickListener(getCapButListener(Cap.BUTT));
					_capRoundBut = (GlowButton) _act.findViewById(map.get(f).get(1));
					_capRoundBut.setOnClickListener(getCapButListener(Cap.ROUND));
					_capSquareBut = (GlowButton) _act.findViewById(map.get(f).get(2));
					_capSquareBut.setOnClickListener(getCapButListener(Cap.SQUARE));
					break;
				case JOIN:
					_joinMitreBut = (GlowButton) _act.findViewById(map.get(f).get(0));
					_joinMitreBut.setOnClickListener(getJoinButListener(Join.MITER));
					_joinRoundBut = (GlowButton) _act.findViewById(map.get(f).get(1));
					_joinRoundBut.setOnClickListener(getJoinButListener(Join.ROUND));
					_joinBevelBut = (GlowButton) _act.findViewById(map.get(f).get(2));
					_joinBevelBut.setOnClickListener(getJoinButListener(Join.BEVEL));
					break;
				case STARTTIP:
					_startBut = (GlowButton) _act.findViewById(map.get(f).get(0));
					_startBut.setOnClickListener(_startClickListener);
					break;
				case ENDTIP:
					_endBut = (GlowButton) _act.findViewById(map.get(f).get(0));
					_endBut.setOnClickListener(_endClickListener);
					break;
				case BREAKTYPE:
					_breakBut = (GlowButton) _act.findViewById(map.get(f).get(0));
					_breakBut.setOnClickListener(_breakClickListener);
					break;
		    	}
	    }
	}
	private String dp1(float f) {
		return BigDecimal.valueOf(f).setScale(1,BigDecimal.ROUND_HALF_EVEN).toString();
	}
	public void setPen(Pen pen) {
		//strVal.setText("" + dp1(pen.strokeWidth));
		//uiBox.sliderMap.get(R.id.main_str_wid_sldr).seekBar.setProgress((int) stroke.strWid);
		mapValueToSlider(pen.strokeWidth,_strSldr);
		//_strColBut.setGlowColour(pen.strokeColour);
		//_strColBut.setTextColour(pen.strokeColour); 
		
		//gloVal.setText("" + dp1(pen.glowWidth));
		//uiBox.sliderMap.get(R.id.main_glo_wid_sldr).seekBar.setProgress((int) stroke.gloWid);
		mapValueToSlider(pen.glowWidth,_gloSldr);
		//_gloColBut.setTextColour(pen.glowColour);
		//_gloColBut.setGlowColour(pen.glowColour);
		
		
		//if (alphaVal!=null) {alphaVal.setText("" + (int) pen.alpha);}
		//if (_alphaSldr!=null) {_alphaSldr.setPosition((int) pen.alpha);}
		
		//if (roundVal!=null) {roundVal.setText("" + (int) pen.rounding);}
		if (_roundSldr!=null) {_roundSldr.setPosition((int) pen.rounding);}
		
		//SeekBar s = uiBox.sliderMap.get(R.id.main_pen_emb_ambient_sldr).seekBar;
		//if (embAmbVal!=null) embAmbVal.setText("" + dp1(pen.embAmbient));
		if (_embAmbSldr!=null) _embAmbSldr.setPosition((int) pen.embAmbient*_embAmbSldr.getMax());
		//if (embSpecVal!=null) embSpecVal.setText("" + pen.embSpecular);
		if (_embSpecSldr!=null) _embSpecSldr.setPosition((int) pen.embSpecular);
		//if (embRadVal!=null) embRadVal.setText("" + pen.embRadius);
		if (_embRadSldr!=null) _embRadSldr.setPosition((int) pen.embRadius);
		if (_embEnableChk!=null) _embEnableChk.setChecked(pen.embEnable);
		
		setSelectedCap(pen.cap);
		setSelectedJoin(pen.join);
		_start = pen.startTip!=null?pen.startTip.duplicate():new Tip();
		setSelectedTipTypeIcon(_start, true);
		_end = pen.endTip!=null?pen.endTip.duplicate():new Tip();
		setSelectedTipTypeIcon(_end, false);
		setSelectedBreakIcon(pen.breakType);
	}

	private float mapSliderToValue(Slider s,float progress) {
		float range = s.getMax();
		float expandedSliderReigon = range/3;
		float expandedValueReigon = expandedSliderReigon/3; // the max value that is expanded
		float opVal = progress/3f;
		if (progress>expandedSliderReigon) {
			opVal = expandedValueReigon+(progress-expandedSliderReigon)*((s.getMax()-expandedSliderReigon)/(s.getMax()-expandedValueReigon));
		}
		s.setDisplayValue(opVal);
		return opVal;
	}
	
	private void mapValueToSlider(float value,Slider s) {
		float range = s.getMax();
		float expandedSliderReigon = range/3;
		float expandedValueReigon = expandedSliderReigon/3; // the max value that is expanded
		if (value<=expandedValueReigon) {
			s.setPosition((int)(value*3));
		} else {
			s.setPosition((int)(expandedSliderReigon+(value-expandedValueReigon)*(s.getMax()-expandedValueReigon)/(s.getMax()-expandedSliderReigon)));
		}
		s.setDisplayValue(value);
	}
	
	private void setSlider(boolean fromUser, float newVal,PenField f, TextView strVal,State s) {
		
		_drawView.getCurrentPen().setField(f, newVal);
		
		if (fromUser) {
			if (s==State.STOP) {
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
			}
			_drawView.setPenSelection(f, newVal);
		}
		if (strVal!=null) {strVal.setText("" + dp1(newVal));}
	}
	
	private void setSlider(boolean fromUser, int newVal,PenField f, TextView strVal) {
		if (fromUser) {
			_drawView.setPenSelection(f, newVal);
		}
		_drawView.getCurrentPen().setField(f, newVal);
		if (strVal!=null) {strVal.setText("" + newVal);}
	}
	
	ColorPickerDialog.OnColorChangedListener colourPickerListener = new ColorPickerDialog.OnColorChangedListener() {
  		public void colorChanged(int color) {
  			if (_colorPickerDialog.index==CPICK_STROKE) {
  				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
  				_drawView.getCurrentPen().setField(PenField.STROKE_COLOUR, color);
  				_drawView.setPenSelection(PenField.STROKE_COLOUR,color);
  				//_strColBut.setTextColour(color);
  				//_strColBut.setGlowColour(color);
  			} else if (_colorPickerDialog.index==CPICK_GLOW){
  				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
  				_drawView.getCurrentPen().setField(PenField.GLOW_COLOUR, color);
  				_drawView.setPenSelection(PenField.GLOW_COLOUR,color);
  				//_gloColBut.setTextColour(color);
  				//_gloColBut.setGlowColour(color);
  			} 
  		}
	};
	
	private Slider.OnSliderChangeListener  strSldrChangeListener = new Slider.OnSliderChangeListener() {

		@Override
		public void onProgressChanged(Slider v, float progress, boolean fromUser) {
			float newVal = mapSliderToValue(v,progress);
			setSlider(fromUser, newVal,PenField.STROKE_WIDTH,null,v.getState());//,strVal
		}
		
	};
	
	OnClickListener strColClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			_colorPickerDialog.index = CPICK_STROKE;
			_colorPickerDialog.show();
			_colorPickerDialog.setColor(((GlowButton)v).glowColour);
		}
	};
	
	private Slider.OnSliderChangeListener  gloSldrChangeListener = new Slider.OnSliderChangeListener() {
		@Override
		public void onProgressChanged(Slider v, float progress, boolean fromUser) {
			float newVal = mapSliderToValue(v,progress);
			setSlider(fromUser,newVal,PenField.GLOW_WIDTH,null,v.getState());
		}
	};
	
	OnClickListener gloColClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			_colorPickerDialog.index = CPICK_GLOW;
			_colorPickerDialog.show();
			_colorPickerDialog.setColor(((GlowButton)v).glowColour);
		}
	};
	/*
	private Slider.OnSliderChangeListener  alphaSldrChangeListener =  new Slider.OnSliderChangeListener() {

		@Override
		public void onProgressChanged(Slider v, float progress, boolean fromUser) {
			//float newVal = mapSliderToValue(v,progress);
			setSlider(fromUser, progress,StrokeField.ALPHA,null);
		}
	};
	*/
	private Slider.OnSliderChangeListener  roundSldrChangeListener =  new Slider.OnSliderChangeListener() {

		@Override
		public void onProgressChanged(Slider v, float progress, boolean fromUser) {
			//float newVal = mapSliderToValue(v,progress);
			setSlider(fromUser, progress,PenField.ROUNDING,null,v.getState());
			
		}
	};
	
	OnClickListener embChkClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			_drawView.getCurrentPen().setField(PenField.EMBOSS, _embEnableChk.isChecked());
			_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
			_drawView.setPenSelection(PenField.EMBOSS, _embEnableChk.isChecked());
			
		}
	};
	
	private Slider.OnSliderChangeListener  embAmbSldrChangeListener =  new Slider.OnSliderChangeListener() {

		@Override
		public void onProgressChanged(Slider v, float progress, boolean fromUser) {
			//float newVal = mapSliderToValue(v,progress);
			setSlider(fromUser, progress/(float)v.getMax(),PenField.EMBOSS_AMBIENT,null,v.getState());
		}
	};
	
	private Slider.OnSliderChangeListener  embSpecSldrChangeListener =  new Slider.OnSliderChangeListener() {

		@Override
		public void onProgressChanged(Slider v, float progress, boolean fromUser) {
			//float newVal = mapSliderToValue(v,progress);
			setSlider(fromUser, (float)progress,PenField.EMBOSS_SPECULAR,null,v.getState());
		}
	};
	
	private Slider.OnSliderChangeListener  embRadSldrChangeListener =  new Slider.OnSliderChangeListener() {

		@Override
		public void onProgressChanged(Slider v, float progress, boolean fromUser) {
			//float newVal = mapSliderToValue(v,progress);
			setSlider(fromUser, (float)progress,PenField.EMBOSS_RADIUS,null,v.getState());
		}
	};
	private void butSetter(ImageView g,boolean isThisMode) {
		if (g.isSelected() && !isThisMode) {g.setSelected(false);}
		else if (isThisMode) {g.setSelected(true);}
	}
	private void setSelectedCap(Cap cap){
		butSetter(_capButtBut,cap==Cap.BUTT);
		butSetter(_capRoundBut,cap==Cap.ROUND);
		butSetter(_capSquareBut,cap==Cap.SQUARE);
	}
	private OnClickListener getCapButListener(final Cap cap) {
		return new OnEWClickListener(_act) {
			@Override
			public void onEWClick(View v) {
				setSelectedCap(cap);
				_drawView.getCurrentPen().setField(PenField.CAP, cap);
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				_drawView.setPenSelection(PenField.CAP, cap);
			}
		};
	}
	
	private void setSelectedJoin(Join join){
		butSetter(_joinMitreBut,join==Join.MITER);
		butSetter(_joinRoundBut,join==Join.ROUND);
		butSetter(_joinBevelBut,join==Join.BEVEL);
	}
	
	private OnClickListener getJoinButListener(final Join join) {
		return new OnEWClickListener(_act) {
			@Override
			public void onEWClick(View v) {
				setSelectedJoin(join);
				_drawView.getCurrentPen().setField(PenField.JOIN, join);
				_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				_drawView.setPenSelection(PenField.JOIN, join);
			}
		};
	}
	
	// sets the button in the dialog and sets to appropriate object
	private void setSelectedTipType(StrokeDecoration.Tip.Type type){
		butSetter(_tipNoneBut,type==Type.NONE);
		butSetter(_tipArrowBut,type==Type.ARROW);
		butSetter(_tipDblArrowBut,type==Type.DBL_ARROW);
		butSetter(_tipSquareBut,type==Type.SQUARE);
		butSetter(_tipCircleBut,type==Type.CIRCLE);
		butSetter(_tipDiamondBut,type==Type.DIAMOND);
		
	}
	// sets the button on the main screen
	private void setSelectedTipTypeIcon(StrokeDecoration.Tip endType , boolean start){
		ImageView but = start?_startBut:_endBut;
		if (endType!=null) {
			switch (endType.type) {
				case NONE:but.setImageResource(R.drawable.i_arr_none);break;
				case ARROW:but.setImageResource(R.drawable.i_arr_sg_op_fwd);break;
				case DBL_ARROW:but.setImageResource(R.drawable.i_arr_db_op_fwd);break;
				case SQUARE:but.setImageResource(R.drawable.i_arr_square);break;
				case CIRCLE:but.setImageResource(R.drawable.i_arr_circle);break;
				case DIAMOND:but.setImageResource(R.drawable.i_arr_diamond);break;

			}
		} else {but.setImageResource(R.drawable.i_arr_none);}
	}
	
	private void setTipSizeBut(float size){
		butSetter(_tipSmallBut,size==1f);
		butSetter(_tipMedBut,size==2f);
		butSetter(_tipLargeBut,size==3f);
	}
	
	private OnClickListener getEndButListener(final StrokeDecoration.Tip.Type type) {
		return new OnEWClickListener(_act) {
			@Override
			public void onEWClick(View v) {
				setSelectedTipType(type);
				Tip selected=_isStrokeTipStartDialog?_start:_end;
				if (selected!=null) {
					selected.type=type;
				}
				//_drawView.getCurrentPen().setField(StrokeField.JOIN, join);
				//_drawView._updateFlags[DrawView.UPDATE_UNDO]=true;
				//_drawView.setPenSelection(StrokeField.JOIN, join);
			}
		};
	}
	private OnClickListener getEndSizeButListener(final float size) {
		return new OnEWClickListener(_act) {
			@Override
			public void onEWClick(View v) {
				setTipSizeBut( size);
				Tip selected=_isStrokeTipStartDialog?_start:_end;
				if (selected!=null) {
					selected.size=size;
				}
			}
		};
	}
	OnClickListener _startClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			_showStrokeEndDialog(true);
			
		}
	};
	OnClickListener _endClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			_showStrokeEndDialog(false);
			
		}
	};
	OnClickListener _breakClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			_showBreakDialog();
		}
	};
	
	private void setSelectedBreakIcon(BreakType breakType ){
		switch (breakType) {
			case SOLID:_breakBut.setImageResource(R.drawable.i_str_solid);break;
			case DOT:_breakBut.setImageResource(R.drawable.i_str_dotted);break;
			case DASH:_breakBut.setImageResource(R.drawable.i_str_dashed);break;
			case DOT_DASH:_breakBut.setImageResource(R.drawable.i_str_dotdashed);break;
		}
	}
}
