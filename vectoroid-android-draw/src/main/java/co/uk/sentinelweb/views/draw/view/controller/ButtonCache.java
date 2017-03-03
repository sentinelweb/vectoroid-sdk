package co.uk.sentinelweb.views.draw.view.controller;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.model.DrawingElement;
import co.uk.sentinelweb.views.draw.model.Group;
import co.uk.sentinelweb.views.draw.model.Stroke;
import co.uk.sentinelweb.views.draw.render.ag.StrokeIconRenderer;
import co.uk.sentinelweb.views.draw.util.OnAsyncListener;

public class ButtonCache {
	Context _context;
	//Drawing drawing;
	ArrayList<DrawingElement> _elements ;
	HashMap<Integer, View> _buttonCache;
	StrokeIconRenderer _sir;
	public OnButtonRequired _buttonMaker;

	
	public ButtonCache(Context c,ArrayList<DrawingElement> elements) {
		super();
		this._context = c;
		this._elements = elements;
		_sir=new StrokeIconRenderer();//DispUtil.getDensity(c)
		_buttonCache = new HashMap<Integer, View>();
		refreshButtons();
	}
	/*
	public Drawing getDrawing() {
		return drawing;
	}

	public void setDrawing(Drawing drawing) {
		this.drawing = drawing;
	}
	*/
	
	public void refreshButtons() {
		HashMap<Integer, View> newButtonCache = new HashMap<Integer, View>();
		for (int i =0;i< _elements.size();i++) {
			DrawingElement drawingElement = _elements.get(i);
			Integer hashCode = Integer.valueOf(drawingElement.hashCode());
			if (_buttonCache.containsKey(hashCode)) {
				newButtonCache.put(hashCode, _buttonCache.get(hashCode));
			} else {
				if (_buttonMaker!=null) {
					newButtonCache.put(Integer.valueOf(drawingElement.hashCode()), _buttonMaker.makeComponent(drawingElement));
				} else {
					newButtonCache.put(Integer.valueOf(drawingElement.hashCode()),makeDefaultButton(drawingElement));
				}
			}
		}
		_buttonCache = newButtonCache;
	}
	
	/**
	 * @return the elements
	 */
	public ArrayList<DrawingElement> getElements() {
		return _elements;
	}

	/**
	 * @param elements the elements to set
	 */
	public void setElements(ArrayList<DrawingElement> elements) {
		this._elements = elements;
		_buttonCache.clear();
		_sir.getRenderer().dropCache();
	}

	public View getView(DrawingElement d) {
		Integer hashCode = Integer.valueOf(d.hashCode());
		return _buttonCache.get(hashCode);
	}
	
	// this is the default button maker - not used for test program
	public View makeDefaultButton(DrawingElement drawingElement) {
		final ImageView bb = new ImageView(_context);
		bb.setBackgroundResource(R.drawable.butt_stroke);
		bb.setAdjustViewBounds(true);
		//bb.setScaleType(ScaleType.FIT_XY);
		bb.setMaxHeight(100);
		bb.setMinimumHeight(100);
		bb.setMaxWidth(100);
		bb.setMinimumWidth(100);
		if (drawingElement instanceof Group) {
			final Group g = (Group)drawingElement;
			bb.setImageBitmap(_sir.makeIcon(g, (int)(80),((BitmapDrawable)bb.getDrawable()).getBitmap()));
			
			bb.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			drawingElement.setUpdateListener(new OnAsyncListener<Stroke>() {
				@Override
				public void onAsync(Stroke request) {
					bb.setImageBitmap(_sir.makeIcon(g, (int)(80),((BitmapDrawable)bb.getDrawable()).getBitmap()));
					
				}
			});
		} else if (drawingElement instanceof Stroke){
			final Stroke stroke = (Stroke)drawingElement;
			bb.setImageBitmap(_sir.makeIcon(stroke, (int)(80),((BitmapDrawable)bb.getDrawable()).getBitmap()));
			
			bb.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			drawingElement.setUpdateListener(new OnAsyncListener<Stroke>() {
				@Override
				public void onAsync(Stroke request) {
					bb.setImageBitmap(_sir.makeIcon(stroke, (int)(80),((BitmapDrawable)bb.getDrawable()).getBitmap()));
					
				}
			});
		}
		return bb;
	}
	
	public static class OnButtonRequired
	{
		public View makeComponent(DrawingElement de){
			return null;
		}
	}
	
	public OnButtonRequired getButtonMaker() {
		return _buttonMaker;
	}

	public void setButtonMaker(OnButtonRequired buttonMaker) {
		this._buttonMaker = buttonMaker;
	}
	
	
}
