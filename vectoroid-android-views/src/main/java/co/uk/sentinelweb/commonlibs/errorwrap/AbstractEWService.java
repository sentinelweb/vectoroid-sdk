package co.uk.sentinelweb.commonlibs.errorwrap;

import android.app.Service;
import android.content.Intent;

public abstract class AbstractEWService extends Service {

	public static final String INTENT_EXTRA = "com.robmunro.mypod.ERROR";
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
	public final void onRebind(Intent intent) {
		// TODO Auto-generated method stub
		super.onRebind(intent);
		try {
			onEWRebind(intent);
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	public abstract void onEWRebind(Intent intent);
	
	@Override
	public final void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		try {
			onEWStart(intent, startId);
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWStart(Intent intent, int startId); 

	
	@Override
	public final void  onDestroy() {
		super.onDestroy();
		try {
			onEWDestroy();
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWDestroy() ;
	
	/*
	void performErrorProcessing (Throwable e) {
		if (e instanceof OutOfMemoryError) {
			System.gc();
		}
		e.printStackTrace();
		String errorMsg = this.getClass().getName()+":"+e.getClass().getName()+" - "+e.getMessage();
		//Log.e(Globals.TAG,errorMsg);
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		//Log.e(Globals.TAG,sw.toString());
		Intent i = new Intent(this,ErrorWrapGlobals.errorWarpActivityClass);
		Bundle b = new Bundle();
		b.putString(INTENT_MESSAGE, errorMsg);
		b.putString(INTENT_STACK, sw.toString()+"\nextra:\n"+extraData);
		i.putExtra(INTENT_EXTRA, b);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i);
	}
	*/
}
