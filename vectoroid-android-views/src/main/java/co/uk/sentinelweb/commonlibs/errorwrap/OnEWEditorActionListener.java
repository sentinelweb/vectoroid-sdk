package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public abstract class OnEWEditorActionListener extends AbstractEWEventListener implements OnEditorActionListener {
	public OnEWEditorActionListener(Context c) {
		super(c);
	}
	@Override
	public boolean onEditorAction(TextView v, int actionId,	KeyEvent event) {
		try {
			return onEWEditorAction(v,actionId,event);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}
		return false;
	}
	public abstract boolean onEWEditorAction(TextView v, int actionId,KeyEvent event) ;
}
