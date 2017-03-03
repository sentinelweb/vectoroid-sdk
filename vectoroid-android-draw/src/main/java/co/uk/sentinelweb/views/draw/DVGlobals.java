package co.uk.sentinelweb.views.draw;

public class DVGlobals {
	public static final String LOG_TAG = "DrawView";
	public static final boolean _isDebug = false;
	public static final String PREF_FILE = "DFPrefs";
	public static final String PREF_NEW_WIDTH = "new.width";
	public static final Integer PREF_NEW_WIDTH_DEF = 1024;
	public static final String PREF_NEW_HEIGHT = "new.height";
	public static final Integer PREF_NEW_HEIGHT_DEF = 2048;
	public static final String PREF_LAST_PHOTO = "photo.last";
	
	public enum TouchType{NONE,SINGLE,MULTI,MULTI_DISTINCT,MULTI_FULL}
	public static final int TOUCHTYPE_NONE=0;
	public static final int TOUCHTYPE_SINGLE=1;
	public static final int TOUCHTYPE_MULTI=2;
	public static final int TOUCHTYPE_MULTI_DISTINCT=3;
	public static final int TOUCHTYPE_MULTI_FULL=4;
	
	public static final String FEATURE_TOUCHSCREEN="android.hardware.touchscreen";
	public static final String FEATURE_TOUCHSCREEN_MULTITOUCH="android.hardware.touchscreen.multitouch";
	public static final String FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT="android.hardware.touchscreen.multitouch.distinct";
	public static final String FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND="android.hardware.touchscreen.multitouch.jazzhand";
	
   	public static final int MULTITOUCH_MINVER = 5;
   	
	
}
