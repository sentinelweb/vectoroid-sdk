package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;

public abstract class EWRunnable implements Runnable  {
	protected Context c;
	private Runnable r;
	protected String extraData = ""; 
	
	public EWRunnable(Context c) {
		super();
		this.c = c;
		
	}
	
	@Override
	public final void run() {
		try {
			doEWrun();
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,c,extraData);
		}
	}
	public abstract void doEWrun();
}
