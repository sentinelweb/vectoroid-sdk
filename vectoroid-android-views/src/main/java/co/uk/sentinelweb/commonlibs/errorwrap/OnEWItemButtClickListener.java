/**
 * 
 */
package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;

public abstract class OnEWItemButtClickListener extends AbstractEWEventListener implements OnClickListener {
	public int position = -1;
	public OnEWItemButtClickListener(Context c,int pos) {
		super(c);
		this.position = pos;
	}
	
	@Override
	public void onClick(View v) {
		try {
			onEWClick(v);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}

	}
	public abstract void onEWClick(View v) ;

}