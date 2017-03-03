package co.uk.sentinelweb.commonlibs.errorwrap;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ErrorWrapGlobals {
	public static Class errorWarpActivityClass = null;
	public static String ERRORWRAP_LOG_TAG = "ErrorWarpper";
	
	public static final String INTENT_EXTRA = "co.uk.sentinelweb.commonlibs.errorwrap.ERROR";
	public static final String INTENT_STACK = "stack";
	public static final String INTENT_MESSAGE = "message";
	
	public static void performErrorProcessing (Throwable e,Object o,Context c,String extra) {
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
			e.fillInStackTrace();
			sw.append("------------- full trace ---------------------\n");
			Throwable extr = e;
			while (extr!=null) {
				sw.append("\nNext >>"+extr.getClass()+":"+extr.getMessage()+"\n");
				StackTraceElement[] stackTrace = extr.getStackTrace();
				
				for (int i=0;i<stackTrace.length;i++) {
					sw.append("\t"+stackTrace[i].getClassName()+"."+stackTrace[i].getMethodName()+"("+stackTrace[i].getFileName()+":"+stackTrace[i].getLineNumber()+")\n");
				}
				extr = extr.getCause();
			}
			//Log.e(Globals.TAG,sw.toString());
			Intent i = new Intent(c,ErrorWrapGlobals.errorWarpActivityClass);
			Bundle b = new Bundle();
			b.putString(INTENT_MESSAGE, errorMsg);
			b.putString(INTENT_STACK, sw.toString()+"\nextra:\n"+extra);
			i.putExtra(INTENT_EXTRA, b);
			c.startActivity(i);
		} catch (Throwable t) {
			t.initCause(e);
			Log.e(ErrorWrapGlobals.ERRORWRAP_LOG_TAG, "An exception occured in error handling",t);
			//Toast.makeText(c, "An exception occurred but handler failed.", 1000);
		}
	}
}
