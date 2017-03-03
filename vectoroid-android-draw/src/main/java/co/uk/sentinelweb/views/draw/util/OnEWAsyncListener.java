package co.uk.sentinelweb.views.draw.util;

import android.content.Context;
import co.uk.sentinelweb.commonlibs.errorwrap.AbstractEWEventListener;
import co.uk.sentinelweb.views.draw.util.OnAsyncListener;

public abstract class OnEWAsyncListener<Param> extends AbstractEWEventListener implements OnAsyncListener<Param>
{
	public OnEWAsyncListener(Context c) {
		super(c);
	}

	public void onAsync(Param request) {
		try {
			onEWAsync(request);
		} catch (Exception e){
			performErrorProcessing(e,this,c);
		}

	}
	public abstract void onEWAsync(Param request) ;
}