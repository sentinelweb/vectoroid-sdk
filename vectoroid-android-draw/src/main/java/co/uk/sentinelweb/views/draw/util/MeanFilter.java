package co.uk.sentinelweb.views.draw.util;

import android.graphics.PointF;

public class MeanFilter {
	public static final int DEFAULT_LEN = 11;
	float[][] points = new float[2][DEFAULT_LEN];
	int currentPos=0;
	int actualValues = 0;
	int length = DEFAULT_LEN;
	float pressureLimit = 0.2f;
	
	public MeanFilter(int length,float pressureLimit) {
		super();
		this.length = length;
		this.pressureLimit = pressureLimit;
	}
	
	public void add(float x,float y) {
		points[0][currentPos]=x;
		points[1][currentPos]=y;
		currentPos++;
		currentPos%=length;
		if (actualValues<length) {
			actualValues++;
			actualValues=Math.min(actualValues, length);
		}
	}
	
	public void clear() {currentPos=0;actualValues=0;}
	public void get(PointF defaultPoint,float pressure){
		if (actualValues==0) {return;}
		if (pressure<pressureLimit) {
			float xmean = PointUtil.getMean(points[0], actualValues);
			float ymean = PointUtil.getMean(points[1], actualValues);
			float pressureRatio = pressure/pressureLimit;
			defaultPoint.x=(pressureRatio)*defaultPoint.x+(1-pressureRatio)*xmean;
			defaultPoint.y=(pressureRatio)*defaultPoint.y+(1-pressureRatio)*ymean;
		};
	}
	
	public void setLength(int i) {
		length = i;
		clear();
	}	
}
