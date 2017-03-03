package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

public abstract class OnEWItemLongClickListener extends AbstractEWEventListener implements OnItemLongClickListener {
	
	public OnEWItemLongClickListener(Context c) {
		super(c);
	}
	@Override
	public final boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
		try {
			return onEWItemLongClick(parent,  view,  pos,  id);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}
		return true;
	}
	public abstract boolean onEWItemLongClick(AdapterView<?> parent, View view, int pos, long id) ;
}
