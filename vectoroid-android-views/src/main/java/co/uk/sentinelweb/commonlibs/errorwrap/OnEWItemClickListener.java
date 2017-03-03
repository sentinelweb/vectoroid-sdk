package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public abstract class OnEWItemClickListener extends AbstractEWEventListener implements OnItemClickListener {
	
	public OnEWItemClickListener(Context c) {
		super(c);
	}
	@Override
	public final void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
		try {
			onEWItemClick(parent,  view,  pos,  id);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}

	}
	public abstract void onEWItemClick(AdapterView<?> parent, View view, int pos, long id) ;
}
