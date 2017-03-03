package co.uk.sentinelweb.views.draw.gpc;

import android.graphics.PointF;
import co.uk.sentinelweb.views.draw.model.PointVec;
import co.uk.sentinelweb.views.draw.model.Stroke;
import co.uk.sentinelweb.views.draw.model.path.PathData;

import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;

public class DrawingPolyConvert {
	
	public static PolyDefault toPoly(Stroke s) {
		PolyDefault p = new PolyDefault();
		addPoly(s, p);
		return p;
	}

	public static void addPoly(Stroke s, PolyDefault p) {
		for ( int j=0;j<s.points.size();j++ ) {
			PointVec pv = s.points.get(j);
			PolyDefault ptmp = p;
			if (s.points.size()>1 && j>0) {
				ptmp = new PolyDefault();
			}
			if (pv.isHole) {ptmp.setIsHole(true);}
			for ( int i=0;i<pv.size();i++ ) {
				PointF pt = pv.get(i);
				ptmp.add(pt.x, pt.y);
				
			}
			if (s.points.size()>1 && j>0) {
				p.add(ptmp);
			}
		}
	}
	
	public static void toStroke(Poly p,Stroke s) {
		s.clearPoints();
		toStrokeInternal( p, s);
	}
	
	private static void toStrokeInternal(Poly p,Stroke s) {
		s.newPoints();
		addPoints(s.currentVec, p);
		if (p.getNumInnerPoly()>0) {
			for (int i=1; i < p.getNumInnerPoly(); i++) {
				Poly pi = p.getInnerPoly(i);
				toStrokeInternal(pi,s);
				if (s.currentVec.isHole) {
					s.holesEven = true;
				}
			}
		}
	}
	
	private static void addPoints(PointVec pv,Poly p) {
		if (p.getNumInnerPoly()==1) {
			pv.isHole=p.isHole();
		}
		if (p.getNumInnerPoly()>0) {
			for (int i=0;i<p.getNumPoints();i++) {
				pv.add(new PathData(p.getX(i),p.getY(i)));
			}
		}
		//if (PointUtil.dist(pv.get(0), pv.get(pv.size()-1))<3) {
			pv.closed=true;
		//}
	}
}
