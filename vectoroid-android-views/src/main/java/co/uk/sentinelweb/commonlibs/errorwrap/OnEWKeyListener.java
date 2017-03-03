package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

public abstract class OnEWKeyListener extends AbstractEWEventListener implements OnKeyListener {
	
	public OnEWKeyListener(Context c) {
		super(c);
	}
	@Override
	public final boolean onKey(View v, int keyCode, KeyEvent event) {
		try {
			return onEWKey(v,keyCode,event);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}
		return false;
	}
	public abstract boolean onEWKey(View v, int keyCode, KeyEvent event);
}
