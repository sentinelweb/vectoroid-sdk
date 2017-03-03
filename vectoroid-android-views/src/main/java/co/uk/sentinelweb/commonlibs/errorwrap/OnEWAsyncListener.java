package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;

public abstract class OnEWAsyncListener<Param> extends AbstractEWEventListener
{
	public OnEWAsyncListener(Context c) {
		super(c);
	}

	//public abstract void onAsync(Param request);

	public void onAsync(Param request) {
		try {
			onEWAsync(request);
		} catch (Exception e){
			performErrorProcessing(e,this,c);
		}

	}
	public abstract void onEWAsync(Param request) ;
}