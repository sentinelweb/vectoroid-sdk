package co.uk.sentinelweb.views.draw.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import co.uk.sentinelweb.views.draw.model.internal.TouchData.Action;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class TouchTestOverlay extends Overlay{
	Paint rPaint;
	Paint tPaint;
	
	public TouchTestOverlay(DrawView d) {
		super(d);
		tPaint=  new Paint();
		tPaint.setStyle(Style.STROKE);
		tPaint.setARGB(255, 255, 255, 255);
		tPaint.setStrokeWidth(3);
		tPaint.setTextSize(15);
		
		rPaint = new Paint(tPaint);
		rPaint.setARGB(255, 255, 0, 0);
	}

	/* (non-Javadoc)
	 * @see co.uk.sentinelweb.views.draw.overlay.Overlay#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw(Canvas c) {
		if (_drawView._touchData._a!=Action.NONE) {
			c.drawCircle(_drawView._touchData._touchPointOnScreen.x, _drawView._touchData._touchPointOnScreen.y, 50, tPaint);
			c.drawText("1",_drawView._touchData._touchPointOnScreen.x +60, _drawView._touchData._touchPointOnScreen.y,  tPaint);
			if (_drawView._touchData!=null && _drawView._touchData._referenceOnScreen!=null) {
				c.drawCircle(_drawView._touchData._referenceOnScreen.x, _drawView._touchData._referenceOnScreen.y, 55, rPaint);
				c.drawText("1",_drawView._touchData._referenceOnScreen.x -70, _drawView._touchData._referenceOnScreen.y,  rPaint);
			}
			if (_drawView._touchData._isMulti) {
				c.drawCircle(_drawView._touchData._touchPointOnScreen2.x, _drawView._touchData._touchPointOnScreen2.y, 50, tPaint);
				c.drawText("2",_drawView._touchData._touchPointOnScreen2.x +60, _drawView._touchData._touchPointOnScreen2.y,  tPaint);
				c.drawCircle(_drawView._touchData._referenceOnScreen2.x, _drawView._touchData._referenceOnScreen2.y, 55, rPaint);
				c.drawText("2",_drawView._touchData._referenceOnScreen2.x -70, _drawView._touchData._referenceOnScreen2.y,  rPaint);
			}
		}
	}
	
}
