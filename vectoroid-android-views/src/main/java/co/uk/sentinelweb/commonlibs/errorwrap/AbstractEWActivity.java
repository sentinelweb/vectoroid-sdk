package co.uk.sentinelweb.commonlibs.errorwrap;

//import com.google.android.apps.analytics.easytracking.EasyTracker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public abstract class AbstractEWActivity extends Activity {
	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		try {
            onEWCreate(savedInstanceState);
		} catch (final OutOfMemoryError e){
			System.gc();
			try {wait(200);} catch (final InterruptedException e1) {}
			boolean doError = true;
			try {
				onEWCreate(savedInstanceState);
				doError = false;
			} catch (final OutOfMemoryError e1){
				ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
				doError = false;
			} 
			if (doError) ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		} 
	}
	protected abstract void onEWCreate(Bundle savedInstanceState) ;
	@Override
	protected final void onPause() {
		
		super.onPause();
		try {
			onEWPause();
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWPause() ;
	
	@Override
	protected final void onRestart() {
		
		super.onRestart();
		try {
			onEWRestart();
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWRestart(); 
	@Override
	protected final void onResume() {
		
		super.onResume();
		try {
			onEWResume();
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWResume() ;

	@Override
	protected final void onStart() {
		
		super.onStart();
		try {
			onEWStart();
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWStart(); 

	@Override
	protected final void  onStop() {
		
		super.onStop();
		try {
			onEWStop();
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWStop() ;
	
	@Override
	protected final void  onDestroy() {
		
		super.onDestroy();
		try {
			onEWDestroy();
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWDestroy() ;
	
	protected abstract void onEWPostCreate(Bundle savedInstanceState) ;
	
	@Override
	protected final void  onPostCreate(final Bundle savedInstanceState) {
		
		super.onPostCreate(savedInstanceState);
		try {
			onEWPostCreate(savedInstanceState);
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract Dialog onEWCreateDialog(int i) ;
	
	@Override
	protected final Dialog  onCreateDialog(final int i) {
		
		try {
			final Dialog onEWCreateDialog = onEWCreateDialog(i);
			if (onEWCreateDialog!=null) {return onEWCreateDialog;}
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
		return super.onCreateDialog(i);
	}
	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		
		super.onCreateOptionsMenu(menu);
		try {
			return onEWCreateOptionsMenu(menu);

		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
		return false;
	}
	public abstract boolean onEWCreateOptionsMenu(Menu menu);
	@Override
	public final boolean onMenuItemSelected(final int featureId, final MenuItem item) {
		
		try {
			return onEWMenuItemSelected(featureId, item);
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
		return false;
	}
	protected abstract boolean onEWMenuItemSelected(int featureId, MenuItem item) ;

	@Override
	public final boolean onKeyDown(final int keyCode, final KeyEvent event) {
		try {
			if (!onEWKeyDown(keyCode, event)) {
				return super.onKeyDown(keyCode, event);//;
			}
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
		return false;
	}
	protected abstract boolean onEWKeyDown(int keyCode, KeyEvent event) ;
	
	@Override
	public final boolean onKeyUp(final int keyCode, final KeyEvent event) {
		try {

			if (!onEWKeyUp(keyCode, event)) {
				return super.onKeyUp(keyCode, event);//;
			}
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
		return false;
	}
	protected abstract boolean onEWKeyUp(int keyCode, KeyEvent event) ;
	
	@Override
	protected final void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			onEWActivityResult( requestCode,  resultCode,  data);

		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWActivityResult(int requestCode, int resultCode, Intent data);
	
	@Override
	protected final void onPrepareDialog(final int id, final Dialog dialog) {
		try {
			onEWPrepareDialog( id,  dialog);
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWPrepareDialog(int id, Dialog dialog);

	@Override
	public Object onRetainNonConfigurationInstance() {
		
		try {
			return onEWRetainNonConfigurationInstance( );
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
		return null;
	}
	protected abstract Object onEWRetainNonConfigurationInstance();

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		try {
			 onEWRestoreInstanceState( savedInstanceState);
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWRestoreInstanceState(Bundle savedInstanceState);

	@Override
	public final boolean onPrepareOptionsMenu(final Menu menu) {
		try {
			return onEWPrepareOptionsMenu( menu);
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
		return false;
	}
	
	public boolean onEWPrepareOptionsMenu(final Menu menu) {return true;}

    @Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		try {
			onEWUserLeaveHint( );
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected void onEWUserLeaveHint() {}
	/* (non-Javadoc)
	 * @see android.app.Activity#startActivity(android.content.Intent)
	 */
	@Override
	public void startActivity(final Intent intent) {
		super.startActivity(intent);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#startActivityForResult(android.content.Intent, int)
	 */
	@Override
	public void startActivityForResult(final Intent intent, final int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}
	
	@Override
	public final void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		try {
			onEWConfigurationChanged( newConfig);
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWConfigurationChanged(Configuration newConfig);
	
	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		try {
			onEWSaveInstanceState( outState);
		} catch (final Throwable e){
			ErrorWrapGlobals.performErrorProcessing(e,this,this,null);
		}
	}
	protected abstract void onEWSaveInstanceState(Bundle outState);
	
}
