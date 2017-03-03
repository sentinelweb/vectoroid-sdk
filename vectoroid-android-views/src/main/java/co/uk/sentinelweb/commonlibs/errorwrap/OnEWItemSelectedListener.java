package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public abstract class OnEWItemSelectedListener extends AbstractEWEventListener implements OnItemSelectedListener {
	
	public OnEWItemSelectedListener(Context c) {
		super(c);
	}
	@Override
	public final void onItemSelected(AdapterView<?> arg0, View arg1,int pos, long arg3) {
		try {
			onEWItemSelected( arg0,  arg1, pos,  arg3);
		} catch (Exception e){
			performErrorProcessing(e,this,c);
		}

	}
	public abstract void onEWItemSelected(AdapterView<?> arg0, View arg1,int pos, long arg3) ;
	
	@Override
	public final void onNothingSelected(AdapterView<?> arg0) {
		try {
			onEWNothingSelected( arg0);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}

	}
	public abstract void onEWNothingSelected(AdapterView<?> arg0);
}
