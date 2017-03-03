package co.uk.sentinelweb.commonlibs.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DispUtil {
	private static float density = -1;
	public static float getDensity(Context context) {
		 if (density==-1) {
			try {
				DisplayMetrics metrics = getMetrics(context);
				density=metrics.density;
				//Log.d(Globals.TAG, "getdensity:"+density);
				
			} catch (Exception e) {
				//Log.d(Globals.TAG, "getdensity:",e);
				density=1;
			}
		}
		return density;
	}
	public  static DisplayMetrics getMetrics(Context context) {
		DisplayMetrics metrics=new DisplayMetrics();
		getDisplay(context).getMetrics(metrics);
		return metrics;
	}
	public  static Display getDisplay(Context context) {
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		return wm.getDefaultDisplay();
	}
}
