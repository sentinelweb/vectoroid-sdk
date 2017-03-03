package co.uk.sentinelweb.views.draw.overlay;

import android.graphics.Canvas;
import android.widget.EditText;
import co.uk.sentinelweb.views.draw.model.internal.TouchData;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class EditTextOverlay extends Overlay {
	public EditText _editTxt;
	public EditTextOverlay(DrawView d) {
		super(d);
		_editTxt=new EditText(d.getContext());
		_editTxt.setWidth(300);
		_editTxt.setMinimumWidth(300);
		_editTxt.forceLayout();
		//_editTxt.setDrawingCacheEnabled(true);
		
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see co.uk.sentinelweb.views.draw.overlay.Overlay#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw(Canvas c) {
		//_editTxt.setDrawingCacheEnabled(true);
		//_editTxt.buildDrawingCache();
		//c.drawBitmap(_editTxt.get, 50, 50,_drawView._modePaint);
		_editTxt.draw(c);
		//c.drawBitmap(, 0, 0, paint);
	}
	
	/* (non-Javadoc)
	 * @see co.uk.sentinelweb.views.draw.overlay.Overlay#onTouch(co.uk.sentinelweb.views.draw.model.internal.TouchData)
	 */
	@Override
	public boolean onTouch(TouchData td) {
		_editTxt.onTouchEvent(td._e);
		return super.onTouch(td);
	}
	
}
