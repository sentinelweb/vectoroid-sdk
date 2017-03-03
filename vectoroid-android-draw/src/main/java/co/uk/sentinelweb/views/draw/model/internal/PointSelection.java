package co.uk.sentinelweb.views.draw.model.internal;

import android.graphics.PointF;
import co.uk.sentinelweb.views.draw.model.Stroke;


public class PointSelection implements Comparable<PointSelection>{
	public Stroke s;
	public int strokeIndex;
	public int pointIndex;
	
	public PointSelection(Stroke s,int strokeIndex, int pointIndex) {
		super();
		this.s=s;
		this.strokeIndex = strokeIndex;
		this.pointIndex = pointIndex;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		//TODO test if the if is nessecary
		if (o instanceof PointSelection) {return equals((PointSelection) o);}
		return super.equals(o);
	}

	public boolean equals(PointSelection o) {
		return s==s && this.strokeIndex==o.strokeIndex && this.pointIndex==o.pointIndex;
	}

	@Override
	public int compareTo(PointSelection object2) {
		return (strokeIndex-object2.strokeIndex)*1000-(pointIndex-object2.pointIndex);
	}
	
	public PointF getPoint() {
		return s.points.get(strokeIndex).get(pointIndex);
	}
}