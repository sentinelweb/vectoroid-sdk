package co.uk.sentinelweb.commonlibs.errorwrap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class SendObject {
	
	private static String  ver = null;
	
	public static void launchSend(Activity act,String subject,String content) {
		launchSend( act, subject, content, null);
	}
	
	public static boolean launchSend(Activity act,String subject,String content,String to) {
		try {
			String toURL = "mailto://";
			String[] addressList = null;
			if (to!=null) {	toURL+=to;addressList= new String[] {to};	}
			Intent myIntent = new Intent(Intent.ACTION_SEND,Uri.parse(toURL));
			myIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
			if (addressList!=null) {
				myIntent.putExtra(Intent.EXTRA_EMAIL ,addressList);
			}
			myIntent.putExtra(Intent.EXTRA_TEXT, content);
			myIntent.setType("message/rfc822"); 
			act.startActivity(myIntent); 
			return true;
		} catch (Throwable e) {
			Toast.makeText(act, "No mail action configured : you may need to install one", 500).show();
		}
		return false;
	}
	
	public  static String getPhoneDetails(Context c) {
		StringBuilder sb = new StringBuilder();
		sb.append( "\n\nPhone " );
		sb.append("brand/model:" );
		sb.append(Build.BRAND );
		sb.append(":" );
		sb.append(	Build.MODEL );
		sb.append("\n "  );
		sb.append("Version:" );
		sb.append(getVersion(c) );
		sb.append(" inc:" );
		sb.append(Build.VERSION.INCREMENTAL );
		sb.append(" sdk:" );
		sb.append(Build.VERSION.SDK );
		sb.append(" rel:" );
		sb.append(Build.VERSION.RELEASE );
		sb.append( " disp:" );
		sb.append(getDisplaySize( c) );
		return sb.toString();
	}

	public static String getVersion(Context c) {
		if (ver==null) {
			try {
				ver = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				ver = "Unknown";  
			}
		}
		return ver;
	}
	public static String getDisplaySize(Context c) {
		DisplayMetrics metrics=new DisplayMetrics();
		getDisplay(c).getMetrics(metrics);
		return metrics.widthPixels+"x"+metrics.heightPixels;
	}

	
	public  static Display getDisplay(Context context) {
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay();
	}
	
	public static void launchMarket(Activity act,String packagename){
		try {
			Intent myIntent = new Intent(Intent.ACTION_VIEW,Uri.parse(packagename));
			act.startActivity(myIntent); 
		} catch (Throwable e) {
			Toast.makeText(act, "Couldn't load market application.", 500).show();
		}
	}
}
