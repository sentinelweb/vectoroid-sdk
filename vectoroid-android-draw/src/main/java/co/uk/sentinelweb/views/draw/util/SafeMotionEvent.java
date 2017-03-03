/**
 * 
 */
package co.uk.sentinelweb.views.draw.util;

import android.content.Context;
import android.graphics.PointF;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import co.uk.sentinelweb.commonlibs.Globals;
import co.uk.sentinelweb.views.draw.DVGlobals;

public class SafeMotionEvent {
		Object[] singleParam=new Object[1];
	   	Object[] noParam=new Object[0];
		Method getXMethod = null;
		
	   	Method getYMethod = null;
	   	Method getPointerCountMethod = null;
	   	Method getPointerIdMethod = null;
	   	public int ver = 1;
	   	public static final int ACTION_MASK =    255;
	   	public static final int ACTION_POINTER_DOWN = 5;
		public static final int ACTION_POINTER_UP = 6;
		public static final int ACTION_POINTER_ID_SHIFT = 8;
		public int _mulitTouchType = DVGlobals.TOUCHTYPE_NONE;
	   	//public int ACTION_MASK = -1;
	   	//public int ACTION_MASK = -1;
	   	
		public SafeMotionEvent(final Context c) {
			super();
			if (c.getPackageManager().hasSystemFeature(DVGlobals.FEATURE_TOUCHSCREEN)) {
				_mulitTouchType=DVGlobals.TOUCHTYPE_SINGLE;
			}
			if (c.getPackageManager().hasSystemFeature(DVGlobals.FEATURE_TOUCHSCREEN_MULTITOUCH)) {
				_mulitTouchType=DVGlobals.TOUCHTYPE_MULTI;
			} 
			if (c.getPackageManager().hasSystemFeature(DVGlobals.FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT)) {
				_mulitTouchType=DVGlobals.TOUCHTYPE_MULTI_DISTINCT;
			} 
			if (c.getPackageManager().hasSystemFeature(DVGlobals.FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND)) {
				_mulitTouchType=DVGlobals.TOUCHTYPE_MULTI_FULL;
			} 
			ver = Build.VERSION.SDK_INT;
			if (ver >= DVGlobals.MULTITOUCH_MINVER) {
				
				try {
					getXMethod = MotionEvent.class.getMethod("getX", new Class[] {Integer.TYPE});
					getYMethod = MotionEvent.class.getMethod("getY", new Class[] {Integer.TYPE});
					getPointerCountMethod = MotionEvent.class.getMethod("getPointerCount", new Class[] {});
					getPointerIdMethod = MotionEvent.class.getMethod("getPointerId", new Class[] {Integer.TYPE});
				} catch (final SecurityException e) {
					//e.printStackTrace();
				} catch (final NoSuchMethodException e) {
					//e.printStackTrace();
				}
			}
		}
	   
		public float getX(final int i, final MotionEvent me) {
			if ( ver>=DVGlobals.MULTITOUCH_MINVER && _mulitTouchType>DVGlobals.TOUCHTYPE_SINGLE && getXMethod!=null) {
				try {
					if (i>-1) {
						singleParam[0]=i;
						return (Float) getXMethod.invoke(me, singleParam);//new Object[]{i}
					}
				} catch (final IllegalArgumentException e) {
					// TODO Auto-generated catch block
					Log.d(Globals.LOG_TAG, "param:"+i);
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final InvocationTargetException e) {
					// TODO Auto-generated catch block
					//Log.d(Globals.LOG_TAG, "param:"+i,e);
					//e.printStackTrace();
				}
			}
			return -1;
		}
		
		public float getY(final int i, final MotionEvent me) {
			if (ver>=DVGlobals.MULTITOUCH_MINVER  && _mulitTouchType>DVGlobals.TOUCHTYPE_SINGLE && getYMethod!=null) {
				try {
					if (i>-1) {
						singleParam[0]=i;
						return (Float) getYMethod.invoke(me, singleParam);//new Object[]{i}
					}
				} catch (final IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final InvocationTargetException e) {
					//Log.d(Globals.LOG_TAG, "param:"+i,e);
				}
			}
			return -1;
		}
		
		public int getPointerCount(final MotionEvent me) {
			if (ver>=DVGlobals.MULTITOUCH_MINVER  && _mulitTouchType>DVGlobals.TOUCHTYPE_SINGLE && getPointerCountMethod!=null) {
				try {
					return (Integer) getPointerCountMethod.invoke(me, noParam);//new Object[]{}
				} catch (final IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return 1;
		}
		
		public int getPointerId(final int i, final MotionEvent me) {
			if (ver>=DVGlobals.MULTITOUCH_MINVER  && _mulitTouchType>DVGlobals.TOUCHTYPE_SINGLE && getPointerIdMethod!=null) {
				try {
					singleParam[0]=i;
					return (Integer) getPointerIdMethod.invoke(me, singleParam);//new Object[]{i}
				} catch (final IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (final InvocationTargetException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
			}
			return -1;
		}
	public void getPointer(final PointF p, final int pointer, final MotionEvent event) {
		p.set(getX(pointer, event),getY(pointer, event));
	}
	public float angle(final PointF start, final MotionEvent event) {
		final float x = start.x - getX(0, event);
		final float y = start.y - getY(0, event);
		return new Double(Math.atan2(x, y) * 180 / Math.PI).floatValue();
	}

	/** Calculate the angle of the first two fingers */
	public float angle(final MotionEvent event) {
		final float x = getX(0, event) - getX(1, event);
		final float y = getY(0, event) - getY(1, event);
		return new Double(Math.atan2(x, y) * 180 / Math.PI).floatValue();
	}

	public float angle(final PointF p1, final PointF p2) {
		final float x = p1.x - p2.x;
		final float y = p1.y - p2.y;
		return new Double(Math.atan2(x, y) * 180 / Math.PI).floatValue();
	}

	/** Determine the space between the first two fingers */
	public float spacing(final MotionEvent event) {
		final float x = getX(0, event) - getX(1, event);
		final float y = getY(0, event) - getY(1, event);
		return (float)Math.sqrt(x * x + y * y);
	}

	public float spacing(final PointF p1, final PointF p2) {
		final float x = p1.x - p2.x;
		final float y = p1.y - p2.y;
		return (float)Math.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	public void midPoint(final PointF point, final MotionEvent event) {
		final float x = getX(0, event) + getX(1, event);
		final float y = getY(0, event) + getY(1, event);
		point.set(x / 2, y / 2);
	}

	public void midPoint(final PointF point, final PointF p1, final PointF p2) {
		final float x = p1.x + p2.x;
		final float y = p1.y + p2.y;
		point.set(x / 2, y / 2);
	}
   }