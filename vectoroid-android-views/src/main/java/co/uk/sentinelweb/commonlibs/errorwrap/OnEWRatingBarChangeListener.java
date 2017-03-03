package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

public abstract class OnEWRatingBarChangeListener extends AbstractEWEventListener implements OnRatingBarChangeListener {
	
	public OnEWRatingBarChangeListener(Context c) {
		super(c);
	}
	@Override
	public final void onRatingChanged(RatingBar ratingBar, float rating,	boolean fromUser) {
		try {
			doRatingChanged( ratingBar,  rating,	 fromUser);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}

	}
	public abstract void doRatingChanged(RatingBar ratingBar, float rating,	boolean fromUser);

}
