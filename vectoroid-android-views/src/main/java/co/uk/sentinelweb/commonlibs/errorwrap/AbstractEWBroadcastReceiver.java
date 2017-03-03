package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public abstract class AbstractEWBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//super.onDestroy();
		try {
			onEWReceive( context,  intent);
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,context,null);
		}
	}
	public abstract void onEWReceive(Context context, Intent intent);
}
