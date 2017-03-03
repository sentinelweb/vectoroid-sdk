package co.uk.sentinelweb.commonlibs.numscroll;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;
import co.uk.sentinelweb.commonlibs.R;
import co.uk.sentinelweb.commonlibs.util.DispUtil;

/**
 * @author robert
 * 
 * NOTES:
 * * need attr.xml content too
 * USAGE:
 * add xmlns:myapp="http://schemas.android.com/apk/res/<PACKAGE>" to root layout objects
 * <co.uk.sentinelweb.commonlibs.numscroll.NumberScroller android:background="@drawable/numscroller_bg" myapp:largecrement="50" 
			  			myapp:fgMargin="3dp" myapp:fg="@drawable/numscroller_cover" android:gravity="center_horizontal" myapp:min="0" android:layout_height="80dp" android:paddingTop="5dp" 
			  			android:layout_width="100dp" android:id="@+id/ws_port" android:shadowColor="#880000" android:textColor="#ffff00" 
			  			android:shadowDx="5" android:shadowDy="5" android:shadowRadius="5" android:textSize="30dp" myapp:max="10000" myapp:value="4444" myapp:digits="4" 
			  			android:textStyle="bold"  android:text="4444" android:focusable="true" />
 *
 * 
 *
 */
public class NumberScroller extends TextView {
	private static final int FLASHTIME = 300;
	private static final int NUMBER_REPEAT_TIME = 100;
	private static final int TAP_TIME = 500;
	private static final int AUTOCREMENT_ONSET_TIME = 500;
	private int DP_PER_LEVEL=15;
	private int pixPerLevel=DP_PER_LEVEL;
	
	//Paint textPaint;
	Paint coverPaint;
	Paint crementPaint;
	
	private int max=10;
	private int min=0;
	private int value=-1;
	private int fgMargin = 0;
	private int digits = 1;
	//private String text=Integer.toString(value);
	// tmp variables
	private float downY = 0;
	private float downX = 0;
	private int downValue = 0;
	private int largecrement = 1;
	
	private long downtime = 0;
	private long lastIncrement = 0;
	private long lastDecrement = 0;
	private RectF lastCrementRect = new RectF();
	private boolean modifiedOnTouch = false;
	private boolean isAutoCrementing=false;
	private boolean largeScroll=false;
	float indicatorActionAreaDivisor = 3;
	
	private ArrayList<OnNumberScrollerChangedListener> numberScrollerChangedListeners = new ArrayList<OnNumberScrollerChangedListener>();
	private OnNumberWrapListener onNumberWrapListener;
	Bitmap fgCover;
	Bitmap resizedCover;
	Handler afterCrementHandler = new Handler();
	Handler crementHandler =new Handler();
	//int textColor=Color.YELLOW;
	//int shadowColor=Color.RED;
	private static float density = -1; 
	
	public NumberScroller(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public NumberScroller(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public NumberScroller(Context context) {
		super(context);
		init(context,null);
	}
	
	private void init(Context context, AttributeSet attrs) {
		//BlurMaskFilter mBlur = new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL);
		if (attrs!=null) {
			TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.NumberScroller);
			/*
			CharSequence s = a.getString(R.styleable.NumberScroller_text);
	        if (s != null) {
	            text=s.toString();
	        }
	        */
	        BitmapDrawable icondrawable = (BitmapDrawable)a.getDrawable(R.styleable.NumberScroller_fg); 
	        if (icondrawable!=null) {
	        	fgCover = icondrawable.getBitmap();
	        }
	        min = a.getInteger(R.styleable.NumberScroller_min, 0);
	        max = a.getInteger(R.styleable.NumberScroller_max, 10);
	        value = a.getInteger(R.styleable.NumberScroller_value, min);
	        digits = a.getInteger(R.styleable.NumberScroller_digits, min);
	        largecrement = a.getInteger(R.styleable.NumberScroller_largecrement, min);
	        fgMargin = (int) a.getDimension(R.styleable.NumberScroller_fgMargin, 0);
	        //Log.d(Globals.TAG, "fgMargin:");
		}
		pixPerLevel = (int)(DP_PER_LEVEL*DispUtil.getDensity(context));
		setValue(value);
		coverPaint = new Paint();
		crementPaint = new Paint();
		crementPaint.setColor( Color.argb(128, 255, 0, 0) );
		crementPaint.setStyle(Style.FILL_AND_STROKE);
		/*
		textPaint = new Paint();
		textPaint.setColor(getTextColors().getDefaultColor());
		textPaint.setTextSize(getTextSize());
		if (shadowColor!=0) {
			textPaint.setShadowLayer(5, 3, 3, shadowColor);
		}
		textPaint.setAntiAlias(true);
		*/
		
	}
	
	public boolean isChanging() {
		return downY!=0;
	}
	float[] mavg =new float[3];
	int mavgidx = 0; 
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//return super.onTouchEvent(event);
		if (isEnabled()) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					downY = event.getY();
					downX = event.getX();
					downValue = value;
					//downTmpValue=value; 
					downtime = System.currentTimeMillis();
					modifiedOnTouch = false;
					//downAccum = 0;
					largeScroll = event.getX()<getMeasuredWidth()/2;
					break;
				case MotionEvent.ACTION_MOVE:
					if (!isAutoCrementing) {
						int diff = (int)(downY-event.getY());
						int increment = diff/pixPerLevel;
						
						/*
						int diff = (int)(downTmpValue-event.getY());
						downTmpValue = (int) event.getY();
						int sign = diff<0?-1:1;
						//downAccum+= sign*diff*diff*0.5;
						//downAccum+= diff;
						float factor =  sign * Math.min( diff*diff*0.5f, 100 );
						mavg[mavgidx] = factor;
						downAccum += (mavg[0]+ mavg[1]+ mavg[2]) / 3;
						int increment = downAccum/pixPerLevel;
						*/
						
						int newValue = (increment*(largeScroll?largecrement:1)+downValue);
						//newValue=Math.min(max, newValue);
						//newValue=Math.max(min, newValue);
						//Log.d(Globals.TAG, diff+" : "+increment+" : "+newValue+" : "+value);
						//if (downTmpValue == newValue) {
						//	break;
						//}
						//downTmpValue = newValue;
						newValue=detectWrap(newValue, true);
						//if (newValue<=max && newValue>=min) {
						//Log.d(Globals.TAG, diff+" : "+increment+" : "+newValue+" : "+value);
						if (value!=newValue ) {
							int oldval= value;
							setValue(newValue,true);
							if (Math.abs(oldval-newValue)>(max-min)*0.6) {
								if (onNumberWrapListener!=null) {
									onNumberWrapListener.onWrap((oldval-newValue)>0,true);
								}
							}
							modifiedOnTouch = true;
							//setValue(newValue);
						}
						if (downY>getHeight()-getHeight()/indicatorActionAreaDivisor) {
							if (!modifiedOnTouch && System.currentTimeMillis()-downtime>AUTOCREMENT_ONSET_TIME) {
								isAutoCrementing = true;
								if (event.getX()<getMeasuredWidth()/2) {
									incrementTimer();
								} else {
									decrementTimer();
								}
							}
						}
					//setValue(detectWrap(newValueup, true));//try this
					//} 
					}
					break;
				case MotionEvent.ACTION_UP:
					if (!isAutoCrementing) {
						if (!modifiedOnTouch && System.currentTimeMillis()-downtime<TAP_TIME) {
							if (downY>getHeight()-getHeight()/indicatorActionAreaDivisor) {
								int newValueup = value;
								if (event.getX()<getMeasuredWidth()/2) {
									newValueup++;
									lastIncrement = System.currentTimeMillis();
								} else {
									newValueup--;
									lastDecrement = System.currentTimeMillis();
								}
								setValue(detectWrap(newValueup, true));
								if (newValueup!=value) {
									if (onNumberWrapListener!=null) {
										onNumberWrapListener.onWrap((value-newValueup)<0,true);
									}
								}
								downtime = 0;
								downY = 0;
								downValue = 0;
							}
						}
					} else {
						isAutoCrementing = false;
						downtime = 0;
						downY = 0;
						downValue = 0;
					}
					
					if (downValue!=value ) {
						for  (OnNumberScrollerChangedListener n:this.numberScrollerChangedListeners) {
							n.onChanged(value, true);
						}
						//this.numberScrollerChangedListener.onChanged(value, true);
					}
					break;
			}
		}
		return true;
	}
	
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode==KeyEvent.KEYCODE_DPAD_UP) {
			increment(false);return true;
		} else if (keyCode==KeyEvent.KEYCODE_DPAD_DOWN) {
			decrement(false);return true;
		} else if (keyCode==KeyEvent.KEYCODE_DPAD_CENTER) {
			this.clearFocus();return true;
		} 
		return false;
	}

	private int detectWrap(int newValue, boolean fromTouch) {
		
		if (onNumberWrapListener==null) {//default behaviour
			newValue=Math.min(max, newValue);
			newValue=Math.max(min, newValue);
			return newValue;
		}
		if (newValue>max) {
			if (onNumberWrapListener.checkWrap(newValue, value, fromTouch)) {
				//newValue=min;
				newValue=wrap(newValue);
			}
		}
		if (newValue<min) {
			if (onNumberWrapListener.checkWrap(newValue, value, fromTouch)) {
				//newValue=max;
				newValue=wrap(newValue);
			}
		}
		//Log.d(Globals.TAG, diff+" : "+increment+" : "+newValue+" : "+value);
		return newValue;//TODO fix
	}
	
	
	
	private int wrap(int value) {
		int mymax = max+1;
		value = value - (int)((value - min) / (mymax - min)) * (mymax - min);
		if (value < 0)  {//This corrects the problem caused by using Int instead of Floor
			value = value + mymax - min;
		}
		return value;
		//int size = max-min+1;// cos max is inclusive
		//int mul = v/size;
		//return v-mul*size;
	}
	private Runnable postIncrement = new Runnable() {public void run() { incrementTimer();}};
	private void incrementTimer() {
		increment(1,true);
		if (isAutoCrementing) {
			crementHandler.postDelayed(postIncrement, NUMBER_REPEAT_TIME);
		}
	}
	
	public void increment(boolean fromTouch) {
		increment(1, fromTouch);
	}
	public void increment(int amt,boolean fromTouch) {
		int newTestValue=value+ amt;
		int newValue = detectWrap(newTestValue, false);
		boolean wasWrapped = newTestValue!= newValue;
		if (value!=newValue ) {
			lastIncrement = System.currentTimeMillis();
			setValue(newValue,fromTouch);
			if (wasWrapped && onNumberWrapListener!=null) {
				onNumberWrapListener.onWrap(true, false);
			}
		}
	}
	private Runnable postDecrement = new Runnable() {public void run() { decrementTimer();}};
	private void decrementTimer() {
		decrement(1,true);
		if (isAutoCrementing) {
			crementHandler.postDelayed(postDecrement, NUMBER_REPEAT_TIME);
		}
	}
	public void decrement(boolean fromTouch) {
		decrement(1, fromTouch);
	}
	public void decrement(int amt,boolean fromTouch) {
		int newTestValue=value- amt;
		int newValue = detectWrap(newTestValue, false);
		boolean wasWrapped = newTestValue!= newValue;
		if (value!=newValue ) {
			lastDecrement = System.currentTimeMillis();
			setValue(newValue,fromTouch);
			if (wasWrapped && onNumberWrapListener!=null) {
				onNumberWrapListener.onWrap(false, false);
			}
		}
	}
	
	public int getMax() {
		return max;
	}
	
	public int getMin() {
		return min;
	}
	
	public void setMax(int max) {
		this.max = max;
	}
	
	public void setMin(int min) {
		this.min = min;
	}
	
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		if (value!=this.value) {
			setValue( value,false);
			for  (OnNumberScrollerChangedListener n:this.numberScrollerChangedListeners) {
				n.onChanged(value, false);
			}
		}
	}
	public void setValue(int value,boolean fromTouch) {
		if (value!=this.value) {
			this.value = value;
			setText(lpad(value));
			for  (OnNumberScrollerChangedListener n:this.numberScrollerChangedListeners) {
				n.onChange(value, fromTouch);
			}
		}
		//invalidate();
	}
	
	//public void setText(String text) {
	//	this.text = text;
	//	invalidate();
	//}
	private void resizeFg() {
		if (resizedCover==null && fgCover!=null) {
			Matrix matrix = new Matrix();
			matrix.postScale((getMeasuredWidth()-2*fgMargin)/ (float) fgCover.getWidth(),(getMeasuredHeight()-2*fgMargin)/ (float) (fgCover.getHeight()));
			//} else {
			//	matrix.postScale(1, 1);
			//}
			resizedCover = Bitmap.createBitmap(fgCover, 0, 0,fgCover.getWidth(), fgCover.getHeight(),matrix, true);
			//fgCover = null;
		}
	}
	private Runnable afterCrement = new Runnable() {public void run() { invalidate();}};
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		resizeFg();
		canvas.drawBitmap(resizedCover, fgMargin, fgMargin, coverPaint);
		//canvas.drawRect(0, 0, getWidth(), getHeight(), crementPaint);
		
		float indictorRounding = 10;
		if (System.currentTimeMillis()-lastIncrement<FLASHTIME) {
			//lastCrementRect.set(0f, 0f, getWidth(), (getHeight()/indicatorActionAreaDivisor));
			lastCrementRect.set(0, getHeight() - getHeight()/indicatorActionAreaDivisor, getWidth()/2, getHeight());
			canvas.drawRoundRect(lastCrementRect ,indictorRounding,indictorRounding ,crementPaint);
			afterCrementHandler.removeCallbacks(afterCrement);
			afterCrementHandler.postDelayed(afterCrement, FLASHTIME+1);
		}
		if (System.currentTimeMillis()-lastDecrement<FLASHTIME) {
			lastCrementRect.set(getWidth()/2, getHeight() - getHeight()/indicatorActionAreaDivisor, getWidth(), getHeight());
			canvas.drawRoundRect(lastCrementRect ,indictorRounding,indictorRounding, crementPaint);
			afterCrementHandler.removeCallbacks(afterCrement);
			afterCrementHandler.postDelayed(afterCrement, FLASHTIME+1);
		}
		//canvas.drawColor(Color.argb(255, 20, 20, 20));
		//canvas.drawText(getText().toString(), 10, 50, textPaint);
	}
	
	private StringBuffer useString = new StringBuffer();
	public  String lpad(int value) {
		useString.delete(0, useString.length());
		useString.append(value);
		while (useString.length()<digits) {
			useString.insert(0, "0");
		}
		return useString.toString();
	}
	
	public static abstract class OnNumberScrollerChangedListener{
		public abstract void onChange(int newValue,boolean fromTouch);
		public abstract void onChanged(int newValue,boolean fromTouch);
	}
	
	
	public void addNumberScrollerChangedListener(OnNumberScrollerChangedListener numberScrollerChangedListener) {
		this.numberScrollerChangedListeners.add(numberScrollerChangedListener);
	}
	
	public static abstract class OnNumberWrapListener{
		public boolean checkWrap(int newValue,int oldvalue, boolean fromTouch) {
			return false;
		}
		public abstract void onWrap( boolean up,boolean fromTouch);
	}
	
	public void setOnNumberWrapListener(OnNumberWrapListener onNumberWrapListener) {
		this.onNumberWrapListener = onNumberWrapListener;
	}

	public void updateLayout() {
		resizedCover=null;
	}
	
	
	
}
