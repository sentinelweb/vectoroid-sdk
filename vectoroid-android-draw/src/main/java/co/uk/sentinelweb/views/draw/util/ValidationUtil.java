package co.uk.sentinelweb.views.draw.util;

import android.os.Build;

public class ValidationUtil {
	public static boolean isBlank(CharSequence str) {
		return str==null || "".equals(str);
	}
	
	public static int getSDK() {
		try {
			return Integer.parseInt(Build.VERSION.SDK);
		} catch (Throwable e) {
			return 1;
		}
	}
}
