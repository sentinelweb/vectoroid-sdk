package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;

/* the parent of the error wrapped listeners */
public abstract class AbstractEWEventListener {
	public Context c;
	protected String extraData = ""; 
	
	public AbstractEWEventListener(Context c) {
		super();
		this.c = c;
	}
	public static void performErrorProcessing (Throwable e,Object o,Context c) {
		ErrorWrapGlobals.performErrorProcessing( e, o, c, "none");
	}
	/*
    static void performErrorProcessing (Throwable e,Object o,Context c,String extra) {
		try {
			if (e instanceof OutOfMemoryError) {
				System.gc();
			}
			e.printStackTrace();
			Log.e(ErrorWrapGlobals.ERRORWRAP_LOG_TAG,"Exception:"+e.getMessage(),e);
			String errorMsg = o.getClass().getName()+":"+e.getClass().getName()+" - "+e.getMessage();
			//Log.e(Globals.TAG,errorMsg);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			//Log.e(Globals.TAG,sw.toString());
			Intent i = new Intent(c,ErrorWrapGlobals.errorWarpActivityClass);
			Bundle b = new Bundle();
			b.putString(AbstractEWActivity.INTENT_MESSAGE, errorMsg);
			b.putString(AbstractEWActivity.INTENT_STACK, sw.toString()+"\nextra:\n"+extra);
			i.putExtra(AbstractEWActivity.INTENT_EXTRA, b);
			c.startActivity(i);
		} catch (Throwable t) {
			t.initCause(e);
			Log.e(ErrorWrapGlobals.ERRORWRAP_LOG_TAG, "An exception occured in error handling",t);
			//Toast.makeText(c, "An exception occurred but handler failed.", 1000);
		}
	}
	*/
}
