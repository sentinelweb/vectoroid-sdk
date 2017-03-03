package co.uk.sentinelweb.commonlibs.globutton;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ImageView;
import co.uk.sentinelweb.commonlibs.R;

public class GlowButton extends ImageView{
	static float density = -1; 
	Paint textPaintGlow;
	Paint borderPaintGlow;
	//Paint borderPaintGlow1;
	String text="none";
	public int glowColour = Color.RED;
	public int borderColour = Color.RED;
	public int textColour = Color.WHITE;
	public boolean fixColours = false;
	RectF borderRect;
	private int textLeft;
	private int textTop;
	private float borderRounding;
	private boolean fitIcon;
	int iconRes=-1;
	Bitmap icon;
	
	int iconTLRes=-1;
	Bitmap iconTL;
	
	PointF iconPos;
	boolean colorBut=false;
	private HashMap<String, Object> extra = new HashMap<String, Object>();
	
	public GlowButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context,attrs);
	}

	public GlowButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
	}

	public GlowButton(Context context) {
		super(context);
		init(context,null);
	}
	public GlowButton(Context context, int strCol, int gloCol, boolean fixglow) {
		super(context);
		init(context,null);
		this.textColour = strCol;
		this.glowColour = gloCol;
		textPaintGlow.setShadowLayer(5, 3, 3, this.glowColour);
		this.fixColours=fixglow;
	}
	
	private void init(Context context,AttributeSet attrs) {
		//BlurMaskFilter mBlur = new BlurMaskFilter(15, BlurMaskFilter.Blur.NORMAL);
		if (attrs!=null) {
			TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.GlowButton);
			CharSequence s = a.getString(R.styleable.GlowButton_text);
	        if (s != null) {
	            text=s.toString();
	        }
	        BitmapDrawable icondrawable = (BitmapDrawable)a.getDrawable(R.styleable.GlowButton_iconref); 
	        if (icondrawable!=null) {
	        	icon = icondrawable.getBitmap();
	        	
	        }
	        colorBut = a.getInteger(R.styleable.GlowButton_cbut, 0)==1;
		}
		if (density==-1) {
			try {
				DisplayMetrics metrics=new DisplayMetrics();
				((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
				density=metrics.density;
			} catch (Exception e) {
				density=1;
			}
		}
		if (text==null) text="null";
		textPaintGlow = new Paint();
		textPaintGlow.setColor(textColour);
		textPaintGlow.setStrokeWidth(0);
		textPaintGlow.setAntiAlias(true);
		textPaintGlow.setTypeface(Typeface.DEFAULT_BOLD);
		//textPaintGlow.setMaskFilter(mBlur);
		//fgPaint.setAntiAlias(true);
		//fgPaint.setDither(true);
		textPaintGlow.setStyle(Style.FILL);
		//textPaintGlow.setStrokeJoin(Paint.Join.BEVEL);
		//textPaintGlow.setStrokeCap(Paint.Cap.ROUND);
		if (icon==null && !colorBut) {
			textPaintGlow.setShadowLayer(5, 3, 3, Color.RED);
		}
		textPaintGlow.setTextSize(15*density);
		borderPaintGlow = new Paint();
		borderPaintGlow.setStyle(Style.STROKE);
		borderPaintGlow.setStrokeWidth(2*density);
		borderPaintGlow.setAlpha(192);
		borderPaintGlow.setAntiAlias(true);
		//borderPaintGlow1 = new Paint();
		//borderPaintGlow1.setStyle(Style.STROKE);
		//borderPaintGlow1.setStrokeWidth(4);
		//borderPaintGlow1.setAlpha(92);
		setTextPosition();
		borderRounding = 10*density;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		borderRect = new RectF(2,2,w-2,h-2);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setTextPosition();
		
	}

	/* (non-Javadoc)
	 * @see android.view.View#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		iconPos=null;
	}

	private void setTextPosition() {
		float wid = textPaintGlow.measureText(text, 0, text.length());
		textLeft = (int)((getMeasuredWidth() - wid - 1*density)/2 );
		textTop = (int)((getMeasuredHeight() - textPaintGlow.getTextSize())/2+textPaintGlow.getTextSize()-5*density);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		boolean border = false;
		if (!fixColours) {textColour = Color.WHITE;}
		if (isSelected()) {
			border=true;
			borderColour= Color.rgb(255, 128, 0);
			if (!fixColours) glowColour = Color.rgb(255, 128, 0);
		} else if (isPressed()) {
			border=true;
			if (!fixColours) glowColour = Color.RED;
			borderColour= Color.RED;
		} else if (isFocused()) {
			border=true;
			borderColour= Color.BLUE;
			if (!fixColours) glowColour = Color.BLUE;
		} else if (!isEnabled()) {
			if (!fixColours) {glowColour = Color.GRAY;	textColour = Color.BLACK;}
		} else {
			if (!fixColours) glowColour = Color.YELLOW;
		}
		textPaintGlow.setShadowLayer(0, 3, 3, glowColour);
		if (!fixColours && icon==null && !colorBut) textPaintGlow.setShadowLayer(5, 3, 3, glowColour);
		textPaintGlow.setColor(textColour);
		borderPaintGlow.setColor(borderColour);
		borderPaintGlow.setAlpha(192);
		//borderPaintGlow1.setColor(borderColour);borderPaintGlow1.setAlpha(128);
		if (icon==null && !colorBut) {
			canvas.drawText(text, textLeft, textTop, textPaintGlow);
		} else if (colorBut) {
			canvas.drawCircle(getMeasuredWidth()/2, getMeasuredHeight()/2, getMeasuredHeight()*0.25f, textPaintGlow);
		} else {
			if (iconPos==null) {
				calcBitmap();
			}
			canvas.drawBitmap(icon, iconPos.x, iconPos.y, textPaintGlow);
		}
		if (border) {
			canvas.drawRoundRect(borderRect, borderRounding, borderRounding, borderPaintGlow);
			//canvas.drawRoundRect(borderRect, borderRounding, borderRounding, borderPaintGlow1);
		}
		if (iconTL!=null) {
			canvas.drawBitmap(iconTL, getMeasuredWidth()-iconTL.getWidth()-5*density, 5*density, textPaintGlow);
		}
		//canvas.drawText("Test", 20, 30, textPaint);
	}
	private void calcBitmap() {
		iconPos = new PointF((getMeasuredWidth()-icon.getWidth())/2,(getMeasuredHeight()-icon.getHeight())/2);
	}

	public void setTextColour(int col) {
		textColour = col;
		invalidate();
	}
	public void setText(String text) {
		this.text=text;
		setTextPosition();
	}
	public void setGlowColour(int col) {
		glowColour = col;
		//if (icon==null && !colorBut) {textPaintGlow.setShadowLayer(5, 3, 3, glowColour);}
		invalidate();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean bubble =  super.onTouchEvent(event);
		//invalidate();
		return bubble;
	}
	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
		invalidate();
	}
	
	public void setIconRes(int iconId) {
		BitmapDrawable icondrawable = (BitmapDrawable)getContext().getResources().getDrawable(iconId); 
	    if (icondrawable!=null) {
	    	icon = icondrawable.getBitmap();
	    }
	    invalidate();
	}
	
	public void setIconTlRes(int iconId) {
		if (iconId != iconTLRes) {
			iconTLRes=iconId;
			if (iconId==-1) {iconTL=null;}
			else {
				BitmapDrawable icondrawable = (BitmapDrawable)getContext().getResources().getDrawable(iconId); 
			    if (icondrawable!=null) {
			    	iconTL = icondrawable.getBitmap();
			    }
			}
		    invalidate();
		}
	}
	
	public void putExtra(String s, Object o) {
		extra.put(s, o);
	}
	
	public Object putExtra(String s) {
		return extra.get(s);
	}

	/**
	 * @return the fitIcon
	 */
	public boolean isFitIcon() {
		return fitIcon;
	}

	/**
	 * @param fitIcon the fitIcon to set
	 */
	public void setFitIcon(boolean fitIcon) {
		this.fitIcon = fitIcon;
	}
	
}
