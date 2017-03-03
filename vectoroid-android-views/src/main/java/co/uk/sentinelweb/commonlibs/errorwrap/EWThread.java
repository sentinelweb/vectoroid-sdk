package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;

public class EWThread extends Thread {
	public Context c;
	protected String extraErrorData = ""; 
	public EWThread(Context c) {
		super();
		this.c = c;
		
	}
	
	@Override
	public final void run() {
		try {
			doEWrun();
		} catch (Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,c,this.extraErrorData);
		}
	}
	public void doEWrun(){};
	
	public void addExtraErrorData(String data){
		this.extraErrorData+=data;
		this.extraErrorData+="\n";
	}
}
