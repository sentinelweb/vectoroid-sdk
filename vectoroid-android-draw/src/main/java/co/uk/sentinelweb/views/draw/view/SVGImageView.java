package co.uk.sentinelweb.views.draw.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import co.uk.sentinelweb.commonlibs.errorwrap.OnEWClickListener;
import co.uk.sentinelweb.commonlibs.util.DispUtil;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.R;
import co.uk.sentinelweb.views.draw.file.DrawingFileUtil;
import co.uk.sentinelweb.views.draw.file.SaveFile;
import co.uk.sentinelweb.views.draw.file.svg.importer.SVGParser;
import co.uk.sentinelweb.views.draw.model.Drawing;
import co.uk.sentinelweb.views.draw.model.UpdateFlags;
import co.uk.sentinelweb.views.draw.model.ViewPortData;
import co.uk.sentinelweb.views.draw.render.ag.AndGraphicsRenderer;
import co.uk.sentinelweb.views.draw.util.OnEWAsyncListener;
import co.uk.sentinelweb.views.draw.util.PointUtil;
import co.uk.sentinelweb.views.draw.util.StrokeUtil;

public class SVGImageView extends ImageView {
	
	public static final int LOADSTATE_UNLOADED = 0 ;
	public static final int LOADSTATE_LOADING = 1 ;
	public static final int LOADSTATE_UPDATING = 2 ;
	public static final int LOADSTATE_LOADED = 3 ;
	public static final int LOADSTATE_FAILED = 4 ;
	
	Drawing d = null;
	AndGraphicsRenderer agr ;
	PointF _tl;
	Paint testPaint;
	RectF drawingBounds = new RectF();
	private String svgPath;
	int loadState = LOADSTATE_UNLOADED;
	OnEWAsyncListener<Integer> onLoadListener;
	boolean _isAsset = false;
	boolean _correctBounds=false;
	private SaveFile _saveFile;
	private Bitmap _previewBitmap;
	Rect _previewSrcRect;
	RectF _previewTgtRect;
	Integer borderColor = null;
	Paint borderPaint;
	OnEWClickListener _swipeLeftListener;
	OnEWClickListener _swipeRightListener;
	float _swipeDist = 200;
	private float _density;
	public SVGImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context,attrs);
	}

	public SVGImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
	}

	public SVGImageView(Context context) {
		super(context);
		init(context,null);
	}   
	
	public void init(Context context,AttributeSet attrs) {
		_tl=new PointF();
		agr = new AndGraphicsRenderer();//context
		testPaint = new Paint();
		testPaint.setColor(Color.BLUE);
		testPaint.setTextSize(20);
		testPaint.setAntiAlias(true);
		testPaint.setFilterBitmap(true);
		borderPaint = new Paint();
		borderPaint.setAntiAlias(true);
		borderPaint.setStyle(Style.STROKE);
		_density = DispUtil.getDensity(context);
		borderPaint.setStrokeWidth(1*_density);
		_swipeDist = 200*_density;
		if (attrs!=null) {
			TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.SVGImageView);
			String svgPath = a.getString(R.styleable.SVGImageView_svg);
			//loadSVGFile();
			setAsset(svgPath);
		}
	}
	PointF _touchDownPt = new PointF(-1,-1);
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "shapeview:ontouch:"+event.getAction()+":"+isClickable());
		if (event.getAction()==MotionEvent.ACTION_DOWN ) {
			_touchDownPt.x = event.getX();
			return true;
		}
		else if (event.getAction()==MotionEvent.ACTION_MOVE ) {
			if (_swipeLeftListener!=null && _touchDownPt.x!=-1 && _touchDownPt.x-event.getX()<-_swipeDist) {
				_swipeLeftListener.onClick(this);
				_touchDownPt.x=-1;
				return true;
			} else if (_swipeLeftListener!=null  && _touchDownPt.x!=-1 && _touchDownPt.x-event.getX()>_swipeDist) {
				_swipeRightListener.onClick(this);
				_touchDownPt.x=-1;
				return true;
			}
		}
		else if (event.getAction()==MotionEvent.ACTION_UP ) {
			if (isClickable() && _touchDownPt.x!=-1) {
				return performClick();
			} 
			_touchDownPt.x=-1;
		}
		return isClickable();
	}

	/* (non-Javadoc)
	 * @see android.widget.ImageView#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		//canvas.drawRect(5, 5, 20, 20, testPaint);
		//ViewPortData vpd = ViewPortData.getFullDrawing(d);
		if (loadState==LOADSTATE_LOADED && d!=null) {
			ViewPortData vpd = ViewPortData.getFromBounds(drawingBounds);
			int dWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
			int dHeight = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
			//RectF calculatedBounds = d.calculatedBounds;
			RectF calculatedBounds = drawingBounds;
			float xscaling = (float)dWidth / calculatedBounds.width();
			
			float yscaling = (float)dHeight / calculatedBounds.height();
			float scaling = Math.min( xscaling,  yscaling );
			scaling = scaling*0.95f;
			//float aspect = calculatedBounds.width()/calculatedBounds.height();
			//float daspect = dWidth/dHeight;
			//Log.d(DVGlobals.LOG_TAG, "scr:"+dWidth +"x"+dHeight +" : "+ calculatedBounds.width() +"x"+ calculatedBounds.height()+" : scaling:"+scaling);
			_tl.set(0, 0);
			//if (aspect>1 ) {
				_tl.y=(dHeight/scaling - calculatedBounds.height())/-2;
			//} else {
				_tl.x=(dWidth/scaling - calculatedBounds.width())/-2;
			//}
			// topleft is inverted, hence -
			_tl.y-=getPaddingTop()/scaling;
			_tl.x-=getPaddingLeft()/scaling;
			//DebugUtil.logCall("padding:"+getPaddingTop()+"x"+getPaddingLeft(),new Exception(),-1);
			vpd.topLeft.set(_tl);
			vpd.zoom=scaling;
			agr.setVpd(vpd);
			agr.setCanvas(canvas);
			agr.setupViewPort();
			//DebugUtil.logCall( "svg:rendering:"+vpd.zoom,new Exception());
			agr.render(d);
			if (borderColor!=null) {
				canvas.drawRect(drawingBounds, borderPaint);
			}
			agr.revertViewPort();
			
		} else {
			String text = "Unloaded";
			switch(loadState) {
				case LOADSTATE_LOADING:
					if (_previewBitmap!=null) {showPreview(canvas);}
					text = "Loading";
					break;
				case LOADSTATE_UPDATING:
					if (_previewBitmap!=null) {showPreview(canvas);}
					text = "Updating";
					break;
				case LOADSTATE_UNLOADED:text = "No Image";break;
				case LOADSTATE_FAILED:text = "Failed:"+svgPath;break;
			}
			canvas.drawText(text, 20, 40, testPaint);
		}
	}
	
	private void showPreview(Canvas canvas) {
		
		canvas.drawBitmap(_previewBitmap,_previewSrcRect,_previewTgtRect , testPaint);
	}

	/* (non-Javadoc)
	 * @see android.widget.ImageView#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	public void setAsset(String assetPath) {
		loadState=LOADSTATE_LOADING;
		_isAsset = true;
		if (assetPath!=null) {
			new LoadSVGTask().execute(assetPath);
		} else {
			d=null;
			loadState=LOADSTATE_UNLOADED;
			invalidate();
		}
	}
	
	public void setFile(String assetPath) {
		loadState=LOADSTATE_LOADING;
		_isAsset = false;
		if (assetPath!=null) {
			new LoadSVGTask().execute(assetPath);
		} else {
			d=null;
			loadState=LOADSTATE_UNLOADED;
			invalidate();
		}
	}
	
	public void setDrawingFile(File f,SaveFile saveFile) {
		loadState=LOADSTATE_LOADING;
		this._saveFile=saveFile;
		_isAsset = false;
		if (f!=null && f.exists()) {
			new LoadDrawingTask().execute(f);
		} else {
			d=null;
			loadState=LOADSTATE_UNLOADED;
			invalidate();
		}
	}
	
	/**
	 * @return the _preview
	 */
	public void setPreview(File f) {
		if (_previewBitmap!=null) {_previewBitmap.recycle();}
		_previewBitmap=null;
		if (f!=null && f.exists()) {
			try {
				_previewBitmap=BitmapFactory.decodeFile(f.getAbsolutePath());
				float scale = Math.min(getMeasuredWidth()/(float)_previewBitmap.getWidth(), getMeasuredHeight()/(float)_previewBitmap.getHeight());
				PointF tl = new PointF(
						(getMeasuredWidth()-_previewBitmap.getWidth()*scale)/2f,
						(getMeasuredHeight()-_previewBitmap.getHeight()*scale)/2f
				);
				_previewSrcRect=new Rect(0,0, _previewBitmap.getWidth(), _previewBitmap.getHeight());
				_previewTgtRect=new RectF(tl.x,tl.y,tl.x+_previewBitmap.getWidth()*scale,tl.y+_previewBitmap.getHeight()*scale);
			} catch (Throwable e) {
				if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "preview load failed : "+f.getAbsolutePath(),e);
			} 
		} 
		invalidate();
	}


	private class LoadSVGTask extends AsyncTask<String, Integer, Long> {
		@Override
		protected Long doInBackground(String... params) {
			String uri = params[0];
			SVGImageView.this.svgPath=uri;
			if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "SVGImageView.this.svgPath: "+SVGImageView.this.svgPath);
			if (svgPath!=null) {
				try {
					loadState=LOADSTATE_LOADING;
					this.publishProgress(loadState);
					d=null;
					agr.dropCache();
					System.gc();
					InputStream is = null;
					if (_isAsset) {
						is = getContext().getResources().getAssets().open(svgPath);
					} else {
						try {
							is = new FileInputStream(new File(svgPath));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (is!=null) {
						InputSource isc = new InputSource(is);
						SVGParser svgp = new SVGParser();
						d = svgp.parseSAX(isc);
						is.close();
						loadState=LOADSTATE_UPDATING;
						this.publishProgress(loadState);
						d.update(true, agr, UpdateFlags.ALL);
						d.computeBounds(drawingBounds);
						
						//if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "LoadSVGTask:drawing bounds:"+PointUtil.tostr(d.calculatedBounds)+":"+PointUtil.tostr(d.size)+PointUtil.tostr(drawingBounds));
						
						if (_correctBounds) {
							correctBounds();
						}
						loadState=LOADSTATE_LOADED;
						this.publishProgress(loadState);
					}
					
				} catch (SAXException e) {
					failLoad(e);
				} catch (ParserConfigurationException e) {
					failLoad(e);
				} catch (IOException e) {
					failLoad(e);
				}
				catch (OutOfMemoryError e) {
					failLoad(e);
				}
			}
			return null;
		}

		private void failLoad(Throwable e) {
			if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "Load failed:"+SVGImageView.this.svgPath,e);
			loadState=LOADSTATE_FAILED;
			this.publishProgress(loadState);
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Long result) {
			invalidate();
			if (onLoadListener!=null) {
				onLoadListener.onAsync(loadState);
			}
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			invalidate();
			if (loadState!=LOADSTATE_LOADED && onLoadListener!=null) {
				onLoadListener.onAsync(loadState);
			}
		}
	}
	private class LoadDrawingTask extends AsyncTask<File , Integer, Long> {

		@Override
		protected Long doInBackground(File... params) {
			File f  = params[0];
			SVGImageView.this.svgPath=f.getAbsolutePath();
			if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "SVGImageView.this.svgPath: "+SVGImageView.this.svgPath);
			if (svgPath!=null) {
				try {
					loadState=LOADSTATE_LOADING;
					this.publishProgress(loadState);
					d=null;
					agr.dropCache();
					System.gc();
					d = DrawingFileUtil.loadJSON(f,_saveFile);
					loadState=LOADSTATE_UPDATING;
					this.publishProgress(loadState);
					d.update(true, agr, UpdateFlags.ALL);
					d.computeBounds(drawingBounds);
					if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "LoadSVGTask:drawing bounds:"+PointUtil.tostr(d.calculatedBounds)+":"+PointUtil.tostr(d.size)+":"+PointUtil.tostr(drawingBounds));
					
					if (_correctBounds) {
						correctBounds();
					}
					loadState=LOADSTATE_LOADED;
				} catch (Exception e) {
					if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "Load failed:"+SVGImageView.this.svgPath,e);
					loadState=LOADSTATE_FAILED;
					this.publishProgress(loadState);
				} catch (OutOfMemoryError e) {
					loadState=LOADSTATE_FAILED;
					this.publishProgress(loadState);
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Long result) {
			invalidate();
			if (onLoadListener!=null) {
				onLoadListener.onAsync(loadState);
			}
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer... values) {
			invalidate();
			if (loadState!=LOADSTATE_LOADED && onLoadListener!=null) {
				onLoadListener.onAsync(loadState);
			}
		}
	}
	public Drawing getDrawing() {
		return d;
	}
	public void setDrawing(Drawing d,boolean compute) {
		this.d=d;
		d.update(true, agr, UpdateFlags.ALL);
		if (compute) {
			d.computeBounds(drawingBounds);
			if (drawingBounds.width()==0) {
				drawingBounds.set(d.calculatedBounds);
			}
		}
		else {drawingBounds.set(d.calculatedBounds);}
		loadState=LOADSTATE_LOADED;
		invalidate();
		if (onLoadListener!=null) {
			onLoadListener.onAsync(loadState);
		}
	}
	public int getLoadState() {
		return loadState;
	}
	/**
	 * @return the onLoadListener
	 */
	public OnEWAsyncListener<Integer> getOnLoadListener() {
		return onLoadListener;
	}

	/**
	 * @param onLoadListener the onLoadListener to set
	 */
	public void setOnLoadListener(OnEWAsyncListener<Integer> onLoadListener) {
		this.onLoadListener = onLoadListener;
	}

	/**
	 * @return the agr
	 */
	public AndGraphicsRenderer getRenderer() {
		return agr;
	}

	private void correctBounds() {
		//d.computeBounds(drawingBounds);
		PointF p = new PointF(-drawingBounds.left,-drawingBounds.top);
		StrokeUtil.translate(d, p, agr);
		d.size.set(drawingBounds.width(),drawingBounds.height());
		if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG, "correctBounds():"+PointUtil.tostr(d.calculatedBounds)+":"+PointUtil.tostr(d.size));
	}

	/**
	 * @return the correctBounds
	 */
	public boolean isCorrectBounds() {
		return _correctBounds;
	}

	/**
	 * @param correctBounds the correctBounds to set
	 */
	public void setCorrectBounds(boolean correctBounds) {
		this._correctBounds = correctBounds;
	}

	/**
	 * @return the borderColor
	 */
	public Integer getBorderColor() {
		return borderColor;
	}

	/**
	 * @param borderColor the borderColor to set
	 */
	public void setBorderColor(Integer borderColor) {
		this.borderColor = borderColor;
	}

	/**
	 * @param _swipeLeftListener the _swipeLeftListener to set
	 */
	public void setSwipeLeftListener(OnEWClickListener _swipeLeftListener) {
		this._swipeLeftListener = _swipeLeftListener;
	}

	/**
	 * @param _swipeRightListener the _swipeRightListener to set
	 */
	public void setSwipeRightListener(OnEWClickListener _swipeRightListener) {
		this._swipeRightListener = _swipeRightListener;
	}
	public void setSwipeDist(float dist) {
		_swipeDist=dist*_density;
	}
}
