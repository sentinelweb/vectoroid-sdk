package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public abstract class OnEWTouchListener extends AbstractEWEventListener implements OnTouchListener {
	public OnEWTouchListener(Context c) {
		super(c);
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
