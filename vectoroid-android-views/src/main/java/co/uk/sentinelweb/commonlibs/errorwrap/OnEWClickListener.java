package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public abstract class OnEWClickListener extends AbstractEWEventListener implements OnClickListener {
	
	public OnEWClickListener(Context c) {
		super(c);
	}
	@Override
	public void onClick(View v) {
		try {
			onEWClick(v);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}

	}
	protected abstract void onEWClick(View v) ;
}
