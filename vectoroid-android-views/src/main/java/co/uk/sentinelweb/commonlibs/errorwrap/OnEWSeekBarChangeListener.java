package co.uk.sentinelweb.commonlibs.errorwrap;

import android.content.Context;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public abstract class OnEWSeekBarChangeListener extends AbstractEWEventListener implements OnSeekBarChangeListener {
	
	public OnEWSeekBarChangeListener(Context c) {
		super(c);
	}
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		try {
			onEWProgressChanged( seekBar,  progress, fromUser);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}

	}
	public abstract void onEWProgressChanged(SeekBar seekBar, int progress, boolean fromUser) ;
	
	public void onStartTrackingTouch(SeekBar seekBar) {
		try {
			onEWStartTrackingTouch( seekBar);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}
	}
	public abstract void onEWStartTrackingTouch(SeekBar seekBar) ;
	public void onStopTrackingTouch(SeekBar seekBar) {
		try {
			onEWStopTrackingTouch( seekBar);
		} catch (Throwable e){
			performErrorProcessing(e,this,c);
		}
	}
	public abstract void onEWStopTrackingTouch(SeekBar seekBar);
}
