package co.uk.sentinelweb.views.draw.overlay;

import android.graphics.Canvas;
import co.uk.sentinelweb.views.draw.model.internal.OnDrawTouchListener;
import co.uk.sentinelweb.views.draw.model.internal.TouchData;
import co.uk.sentinelweb.views.draw.util.DispUtil;
import co.uk.sentinelweb.views.draw.view.DrawView;

public abstract class Overlay implements OnDrawTouchListener {
	float _density = -1;
	DrawView _drawView;
	public Overlay(DrawView d) {
		this._drawView=d;
		if (_density==-1) {_density=DispUtil.getDensity(d.getContext());	}
	}

	public void draw(Canvas c) {
		
	}

	public boolean onTouch(TouchData td) {
		return false;
	}
	
}
