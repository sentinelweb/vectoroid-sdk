package co.uk.sentinelweb.views.draw.model.path;

import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;

import co.uk.sentinelweb.views.draw.util.StrokeUtil;

public class Arc extends PathData {
	//public Type type = Type.ARC;
	public PointF r = new PointF();//SVG & Android
	// oval rotation
	public float xrot;//SVG // this doesn't fit with android API?
	public boolean largeArc;//SVG
	public boolean sweep;//SVG
	
	public RectF oval = new RectF();//android -  renderdata?
	public PointF startAndSweepAngle = new PointF();//android -  renderdata?
	public Arc(final Arc p) {
		super(p);
		type = Type.ARC;
		this.r.set(p.r);
		this.xrot=p.xrot;
		this.largeArc=p.largeArc;
		this.sweep=p.sweep;
	}
	
	public Arc(final PointF p, final float rx, final float ry, final float xrot, final boolean largeArc, final boolean sweep) {
		super(p);
		type = Type.ARC;
		this.r.set(rx,ry);
		this.xrot=xrot;
		this.largeArc=largeArc;
		this.sweep=sweep;
	}

	public Arc duplicate() {return new Arc(this);}
	
	/**
	 * From : https://github.com/TrevorPage/TPSVG_Android_SVG_Library/blob/master/TPSVG/src/com/trevorpage/tpsvg/Tpsvg.java
	 * with mods
	 * This method converts an SVG arc to an Android Canvas ARC. 
	 * 
	 * @param last last point
	 */

	public final boolean computArcFrom(final PointF last) {// float theta, boolean largeArcFlag, boolean sweepFlag,
        // Ensure radii are valid
        if (r.x == 0 || r.y == 0) {
                //path.lineTo(x, y);
                return false;
        }
        // Get the current (x, y) coordinates of the path
        //Point2D p2d = path.getCurrentPoint();
        
        final float x0 = last.x; //(float) p2d.getX();
        final float y0 = last.y; //(float) p2d.getY();
        // Compute the half distance between the current and the final point
        final float dx2 = (x0 - x) / 2.0f;
        final float dy2 = (y0 - y) / 2.0f;
        // Convert theta from degrees to radians
        final float theta = (float) Math.toRadians( xrot % 360f);

        //
        // Step 1 : Compute (x1, y1)
        //
        final float x1 = (float) (Math.cos(theta) * (double) dx2 + Math.sin(theta)
                        * (double) dy2);
        final float y1 = (float) (-Math.sin(theta) * (double) dx2 + Math.cos(theta)
                        * (double) dy2);
        // Ensure radii are large enough
        float rx = r.x;
        float ry = r.y;
        rx = Math.abs(rx);
        ry = Math.abs(ry);
        float Prx = rx * rx;
        float Pry = ry * ry;
        final float Px1 = x1 * x1;
        final float Py1 = y1 * y1;
        final double d = Px1 / Prx + Py1 / Pry;
        if (d > 1) {
                rx = Math.abs((float) (Math.sqrt(d) * (double) rx));
                ry = Math.abs((float) (Math.sqrt(d) * (double) ry));
                Prx = rx * rx;
                Pry = ry * ry;
        }

        //
        // Step 2 : Compute (cx1, cy1)
        //
        double sign = (largeArc == sweep) ? -1d : 1d;
        final float coef = (float) (sign * Math
                        .sqrt(((Prx * Pry) - (Prx * Py1) - (Pry * Px1))
                                        / ((Prx * Py1) + (Pry * Px1))));
        final float cx1 = coef * ((rx * y1) / ry);
        final float cy1 = coef * -((ry * x1) / rx);

        //
        // Step 3 : Compute (cx, cy) from (cx1, cy1)
        //
        final float sx2 = (x0 + x) / 2.0f;
        final float sy2 = (y0 + y) / 2.0f;
        final float cx = sx2
                        + (float) (Math.cos(theta) * (double) cx1 - Math.sin(theta)
                                        * (double) cy1);
        final float cy = sy2
                        + (float) (Math.sin(theta) * (double) cx1 + Math.cos(theta)
                                        * (double) cy1);

        //
        // Step 4 : Compute the angleStart (theta1) and the angleExtent (dtheta)
        //
        final float ux = (x1 - cx1) / rx;
        final float uy = (y1 - cy1) / ry;
        final float vx = (-x1 - cx1) / rx;
        final float vy = (-y1 - cy1) / ry;
        float p, n;
        // Compute the angle start
        n = (float) Math.sqrt((ux * ux) + (uy * uy));
        p = ux; // (1 * ux) + (0 * uy)
        sign = (uy < 0) ? -1d : 1d;
        float angleStart = (float) Math.toDegrees(sign * Math.acos(p / n));
        // Compute the angle extent
        n = (float) Math.sqrt((ux * ux + uy * uy) * (vx * vx + vy * vy));
        p = ux * vx + uy * vy;
        sign = (ux * vy - uy * vx < 0) ? -1d : 1d;
        float angleExtent = (float) Math.toDegrees(sign * Math.acos(p / n));
        if (!sweep && angleExtent > 0) {
                angleExtent -= 360f;
        } else if (sweep && angleExtent < 0) {
                angleExtent += 360f;
        }
        angleExtent %= 360f;
        angleStart %= 360f;

        //Arc2D.Float arc = new Arc2D.Float();
        final float _x = cx - rx;
        final float _y = cy - ry;
        final float _width = rx * 2.0f;
        final float _height = ry * 2.0f;
        
        //TODO sometimes all the calcs come out isNAN, need to find where this is coming from.
        
        if (!Float.isNaN( startAndSweepAngle.x) && !Float.isNaN( startAndSweepAngle.y) && !Float.isNaN(oval.top)) {
	        startAndSweepAngle.x=angleStart;
	        startAndSweepAngle.y=angleExtent;
	        oval = new RectF( _x, _y, _x+_width, _y+_height);
	        //path.arcTo(oval, angleStart, angleExtent);
	        
	        if (boundsCheck==null) {
	        	boundsCheck= new ArrayList<>();
	        	for (int i=0;i<12;i++) {boundsCheck.add(new PathData());}
	        }
	        StrokeUtil.genArc(this);
	        return true;
        } else {
        	return false;
        }
        
//        if(animHandler!=null){
//        	animHandler.arcParams(mProperties.id, path, angleStart, angleExtent, bounds);
//        	
//        }
//        else{
        	//
        	//addArc(new Arc(bounds, angleStart, angleExtent, mProperties.id));
//        }    
	}

	@Override
	public void computeBounds(final PointF calculatedCOG, final RectF calculatedBounds, final RectF accum, final PathData lastPathData) {
		if (lastPathData!=null && computArcFrom( lastPathData)) {
			accum.set(1e8f,1e8f,-1e8f,-1e8f);//bounds accumulator
			for (final PathData boundsPoint : boundsCheck) {
				super.incrementBounds((PointF)null, accum, boundsPoint);
			}
			super.incrementBounds(calculatedCOG, calculatedBounds, accum);
		} else {
			super.incrementBounds(calculatedCOG, calculatedBounds,this);
		}
	}

	
}
	