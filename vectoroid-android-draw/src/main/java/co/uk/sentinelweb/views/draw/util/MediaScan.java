package co.uk.sentinelweb.views.draw.util;

import java.io.File;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

public class MediaScan {
	static class FileScanner {
		MediaScannerConnection msc = null;
		File f;
		
		Context c;
		public FileScanner(Context c, File f){
			this.f=f;
			this.c=c;
			msc = new MediaScannerConnection(c.getApplicationContext(),new MediaScannerConnection.MediaScannerConnectionClient() {

	    		@Override
	    		public void onMediaScannerConnected() {
	    			msc.scanFile( FileScanner.this.f.getAbsolutePath(), null  );//FileScanner.this.f.type
	    		}

	    		@Override
	    		public void onScanCompleted(String path, Uri uri) {
	    			msc.disconnect();
	    		}

				
			});
	    	msc.connect();
		}
		
	}
	public static void scanFile(Context c, File f) {
		new FileScanner(c, f);
    }
}
