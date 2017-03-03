package co.uk.sentinelweb.commonlibs.errorwrap;

import android.app.Application;
import android.content.res.Configuration;

public abstract class AbstractEWApplication extends Application {

	//public static final String INTENT_EXTRA = "com.robmunro.mypod.ERROR";
	public static final String INTENT_STACK = "stack";
	public static final String INTENT_MESSAGE = "message";
	protected String extraData = ""; 
	@Override
	public final void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		try {
			onEWCreate();
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWCreate() ;
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		try {
			onEWLowMemory();
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWLowMemory() ;
	
	@Override
	public final void onConfigurationChanged(Configuration newConfig) {
		
		super.onConfigurationChanged(newConfig);
		try {
			onEWConfigurationChanged( newConfig);
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	
	protected void onEWConfigurationChanged(Configuration newConfig) {}
	
	@Override
	public final void  onTerminate() {
		super.onTerminate();
		try {
			onEWTerminate();
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWTerminate() ;
	
}
