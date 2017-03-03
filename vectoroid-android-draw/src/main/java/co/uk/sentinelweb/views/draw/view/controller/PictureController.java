package co.uk.sentinelweb.views.draw.view.controller;

import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import co.uk.sentinelweb.views.draw.util.OnEWAsyncListener;
import co.uk.sentinelweb.views.draw.view.PictureView;
import co.uk.sentinelweb.views.draw.view.PictureView.Fix;

public class PictureController {
	public enum PicEditComponents {PICEDIT,OK, CANCEL, ZOOM, SCALE, ROTATE, PAN, SHEAR};
	private View _scaleButton;
	private View _okButton;
	private View _cancelButton;
	private View _rotateButton;
	private View _moveButton;
	private View _shearButton;
	private View _zoomButton;
	
	private Activity _act;
	private PictureView _picEditView;
	
	private OnEWAsyncListener<Integer> _okListener;
	private OnEWAsyncListener<Integer> _cancelListener;
	private OnEWAsyncListener<View> _extraButListener;
	
	public PictureController(Activity act,	HashMap<PicEditComponents, List<Integer>> picMap) {
		this._act=act;
		setup(picMap);
	}
	
	public void setup( HashMap<PicEditComponents, List<Integer>> map) {
		 for (PicEditComponents f:map.keySet()) {
		    	switch (f) {
			    	case OK:
			    		_okButton=(View) _act.findViewById(map.get(f).get(0));
			    		_okButton.setOnClickListener(_okClickListener);
			    		break;
			    	case CANCEL:
			    		_cancelButton=(View) _act.findViewById(map.get(f).get(0));
			    		_cancelButton.setOnClickListener(_cancelClickListener);
			    		break;
			    	case ZOOM:
			    		_zoomButton=(View) _act.findViewById(map.get(f).get(0));
			    		_zoomButton.setOnClickListener(_zoomClickListener);
			    		break;
			    	case SCALE:
			    		_scaleButton=(View) _act.findViewById(map.get(f).get(0));
			    		_scaleButton.setOnClickListener(_scaleClickListener);
			    		break;
			    	case ROTATE:
			    		_rotateButton=(View) _act.findViewById(map.get(f).get(0));
			    		_rotateButton.setOnClickListener(_rotateClickListener);
			    		break;
			    	case PAN:
			    		_moveButton=(View) _act.findViewById(map.get(f).get(0));
			    		_moveButton.setOnClickListener(_panClickListener);
			    		break;
			    	case SHEAR:
			    		_shearButton=(View) _act.findViewById(map.get(f).get(0));
			    		_shearButton.setOnClickListener(_shearClickListener);
			    		break;
			    	case PICEDIT:
			    		_picEditView=(PictureView) _act.findViewById(map.get(f).get(0));
			    		//okButton.setOnClickListener(okClickListener);
			    		break;
		    	};
		 }
	}
	
	public void updatePicButtons() {
		_moveButton.setSelected(_picEditView.getFixMode() == Fix.PAN);
		_scaleButton.setSelected(_picEditView.getFixMode() == Fix.SCALE);
		_rotateButton.setSelected(_picEditView.getFixMode() == Fix.ROTATE);
		_shearButton.setSelected(_picEditView.getFixMode() == Fix.SHEAR);
		_zoomButton.setSelected(_picEditView.getFixMode() == Fix.ZOOM);
	}
	
	OnClickListener _rotateClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			_picEditView.setFixMode(_picEditView.getFixMode() != Fix.ROTATE?Fix.ROTATE:Fix.NONE);
			updatePicButtons();
			if (_extraButListener!=null) {
				_extraButListener.onAsync(_rotateButton);
			}
		}
	};
	OnClickListener _scaleClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
		_picEditView.setFixMode(_picEditView.getFixMode() != Fix.SCALE?Fix.SCALE:Fix.NONE);
		updatePicButtons();
		if (_extraButListener!=null) {
			_extraButListener.onAsync(_scaleButton);
		}
	}};
	OnClickListener _panClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
		_picEditView.setFixMode(_picEditView.getFixMode() != Fix.PAN?Fix.PAN:Fix.NONE);
		updatePicButtons();
		if (_extraButListener!=null) {
			_extraButListener.onAsync(_moveButton);
		}
	}};
	OnClickListener _shearClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
		_picEditView.setFixMode(_picEditView.getFixMode() != Fix.SHEAR?Fix.SHEAR:Fix.NONE);
		updatePicButtons();
		if (_extraButListener!=null) {
			_extraButListener.onAsync(_shearButton);
		}
	}};
	OnClickListener _zoomClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
		_picEditView.setFixMode(_picEditView.getFixMode() != Fix.ZOOM?Fix.ZOOM:Fix.NONE);
		updatePicButtons();
		if (_extraButListener!=null) {
			_extraButListener.onAsync(_zoomButton);
		}
	}};
	OnClickListener _okClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (_okListener!=null) {
				_okListener.onAsync(0);
			}
	}};
	OnClickListener _cancelClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
		//bodyFlipper.setDisplayedChild(BODY_FLIP_DRAW);
			if (_cancelListener!=null) {
				_cancelListener.onAsync(0);
			}
	}};
	/**
	 * @return the _okListener
	 */
	public OnEWAsyncListener<Integer> getOkListener() {
		return _okListener;
	}

	/**
	 * @param _okListener the _okListener to set
	 */
	public void setOkListener(OnEWAsyncListener<Integer> okListener) {
		this._okListener = okListener;
	}

	/**
	 * @return the _cancelListener
	 */
	public OnEWAsyncListener<Integer> getCancelListener() {
		return _cancelListener;
	}

	/**
	 * @param _cancelListener the _cancelListener to set
	 */
	public void setCancelListener(OnEWAsyncListener<Integer> cancelListener) {
		this._cancelListener = cancelListener;
	}
	
	public void setExtraButListener(OnEWAsyncListener<View> listener) {
		this._extraButListener = listener;
	}
	public void processButtons(OnEWAsyncListener<View> callback) {
		callback.onAsync(_okButton);
		callback.onAsync(_cancelButton);
		callback.onAsync(_zoomButton);
		callback.onAsync(_scaleButton);
		callback.onAsync(_rotateButton);
		callback.onAsync(_moveButton);
		callback.onAsync(_shearButton);
	}
}
