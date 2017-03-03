package co.uk.sentinelweb.views.draw.overlay;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import co.uk.sentinelweb.views.draw.gpc.SegmentGraph;
import co.uk.sentinelweb.views.draw.gpc.SegmentGraph.Edge;
import co.uk.sentinelweb.views.draw.gpc.SegmentGraph.Intersection;
import co.uk.sentinelweb.views.draw.gpc.SegmentGraph.ScanEdgeData;
import co.uk.sentinelweb.views.draw.gpc.SegmentGraph.ScanLineReigon;
import co.uk.sentinelweb.views.draw.gpc.SegmentGraph.Vertex;
import co.uk.sentinelweb.views.draw.model.internal.TouchData;
import co.uk.sentinelweb.views.draw.view.DrawView;

public class OutlineOverlay extends Overlay {
	//public SegmentGraph _seg;
	public SegmentGraph _seg;
	Paint interPaint;
	Paint vertexPaint;
	Paint edgePaint;
	Paint outPaint;
	Paint regPaint;
	Paint actPaint;
	int ctr=0;
	public OutlineOverlay(DrawView d) {
		super(d);
		vertexPaint=  new Paint();
		vertexPaint.setStyle(Style.STROKE);
		vertexPaint.setARGB(255, 0, 0, 255);
		vertexPaint.setStrokeWidth(1);
		vertexPaint.setTextSize(15);
		
		interPaint = new Paint(vertexPaint);
		interPaint.setARGB(255, 0, 255, 0);
		interPaint.setStrokeWidth(3);
		
		edgePaint = new Paint(vertexPaint);
		edgePaint.setARGB(255, 0,  0,255);
		edgePaint.setStrokeWidth(3);
		edgePaint.setAntiAlias(true);
		
		outPaint = new Paint(vertexPaint);
		outPaint.setARGB(255, 255,  0,0);
		outPaint.setStrokeWidth(1);
		outPaint.setAntiAlias(true);
		
		regPaint = new Paint(vertexPaint);
		regPaint.setARGB(255, 255,  255,0);
		regPaint.setStrokeWidth(1);
		
		actPaint = new Paint(vertexPaint);
		actPaint.setARGB(255, 255,  255,255);
		actPaint.setStrokeWidth(3);
		actPaint.setAntiAlias(true);
	}

	/* (non-Javadoc)
	 * @see co.uk.sentinelweb.views.draw.overlay.Overlay#draw(android.graphics.Canvas)
	 */
	@Override
	public void draw(Canvas c) {
		if (_seg!=null) {
			for (Vertex v : _seg.verticies) {
				c.drawCircle(v.p.x,v.p.y, 7, vertexPaint);
			}
			for (Intersection i : _seg.intersections) {
				c.drawCircle(i.p.x,i.p.y, 9, interPaint);
			}
			int ctr1=0;
			//for (ScanEdgeData e:_seg.sortedEdgeData) {
			//ScanPointData spd=_seg.sortedEdgeData.get(ctr);
			//Edge e = spd.e;
			//for (int i=0;i<=ctr;i++) {
			for (int i=0;i<_seg.edges.size();i++) {
				Edge e = _seg.edges.get(i);
				edgePaint.setARGB(255, 0, ctr1*16, ctr1*16);
				c.drawLine(e.start.p.x,e.start.p.y,e.end.p.x,e.end.p.y, edgePaint);
				c.drawCircle(e.start.p.x,e.start.p.y,5, edgePaint);
				c.drawCircle(e.end.p.x,e.end.p.y,5, edgePaint);
				++ctr1;
				ctr1%=16;
			}
			
			if (_seg.scanline!=null) {
				regPaint.setARGB(255, 255,128, 0);
				c.drawLine(_seg.scanline.position,_seg.scanline.min,_seg.scanline.position,_seg.scanline.max, regPaint);
				for (int i=0;i<_seg.scanline.lastRegions.size();i++) {
					ScanLineReigon r = _seg.scanline.lastRegions.get(i);
					regPaint.setARGB(255, 255,255, r.inside?255:0);
					c.drawLine(_seg.scanline.position,r.start,_seg.scanline.position,r.end, regPaint);
				}
				for (ScanEdgeData spd:_seg.activeEdges) {
					c.drawLine(spd.e.start.p.x,spd.e.start.p.y,spd.e.end.p.x,spd.e.end.p.y, actPaint);
				}
			}
			
			
			for (Edge e:_seg.outline) {
				//Edge e = _seg.outline.get(i);
				c.drawLine(e.start.p.x,e.start.p.y,e.end.p.x,e.end.p.y, outPaint);
			}
			
		}
	}

	/* (non-Javadoc)
	 * @see co.uk.sentinelweb.views.draw.overlay.Overlay#onTouch(co.uk.sentinelweb.views.draw.model.internal.TouchData)
	 */
	@Override
	public boolean onTouch(TouchData td) {
		if (_seg!=null) {
			switch (td._a) {
				case ACTION_UP:
					//ctr++;
					//ctr%=_seg.edges.size();
					//ctr%=_seg.sortedEdgeData.size();
					//_seg.scanStep();
					//_seg.removeIntersection();
					_drawView.updateDisplay();
					return false;
	
				default:
					return false;
			}
		}
		return false;
	}

}
