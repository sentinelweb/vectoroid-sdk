package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.view.View;
import android.view.View.OnLongClickListener;

public abstract class OnEWLongClickListener extends AbstractEWEventListener implements OnLongClickListener {
	
	public OnEWLongClickListener(Context c) {
		super(c);
	}
	@Override
	public final boolean onLongClick(View v) {
		try {
			return onEWLongClick(v);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}
		return false;
	}
	public abstract boolean onEWLongClick(View v) ;
}
