/**
 * 
 */
package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnEWItemTouchListener extends AbstractEWEventListener implements OnTouchListener {
	public int position = -1;
	public OnEWItemTouchListener(Context c,int pos) {
		super(c);
		this.position = pos;
	}
	@Override
	public final boolean onTouch(View v, MotionEvent event) {
		try {
			return onEWTouch(v,event);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}
		return false;
	}
	public abstract boolean onEWTouch(View v, MotionEvent event); 
}