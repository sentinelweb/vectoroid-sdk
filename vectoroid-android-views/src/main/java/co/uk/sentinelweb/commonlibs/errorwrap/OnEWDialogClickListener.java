package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.content.DialogInterface;

public abstract class OnEWDialogClickListener extends AbstractEWEventListener implements DialogInterface.OnClickListener {
	
	public OnEWDialogClickListener(Context c) {
		super(c);
	}
	@Override
	public void onClick(DialogInterface dialog, int whichButton) {
		try {
			onEWClick(dialog,whichButton);
		} catch (Exception e){
			performErrorProcessing(e,this,c);
		}

	}
	public abstract void onEWClick(DialogInterface dialog, int whichButton) ;
}
