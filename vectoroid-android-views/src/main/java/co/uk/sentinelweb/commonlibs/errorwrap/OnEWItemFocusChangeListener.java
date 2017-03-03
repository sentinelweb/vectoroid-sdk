/**
 * 
 */
package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.view.View;
import android.view.View.OnFocusChangeListener;

public abstract class OnEWItemFocusChangeListener extends AbstractEWEventListener implements OnFocusChangeListener {
	public int position = -1;
	public OnEWItemFocusChangeListener(Context c,int pos) {
		super(c);
		this.position = pos;
	}
	@Override
	public final void onFocusChange(View v, boolean hasFocus) {
		try {
			onEWFocusChange(v,hasFocus);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}
	}
	public abstract void onEWFocusChange(View v, boolean hasFocus);
}