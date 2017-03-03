package co.uk.sentinelweb.views.draw.gpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.graphics.PointF;
import android.util.Log;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.model.PointVec;
import co.uk.sentinelweb.views.draw.util.PointUtil;

public class SegmentGraphOld {
	public SegmentGraphOld () {
		
	}
	
	
	public static class Vertex {
		public ArrayList<Edge> edges =new ArrayList<Edge>();
		//Edge next;
		//Edge last;
		public PointF p;
		
		public Vertex(PointF p) {
			super();
			this.p = p;
		}
		
		public boolean equals(PointF p1) {
			return p.x==p1.x && p.y==p1.y;
		}
		//public String toString() {return "vtx{"+PointUtil.tostr(p)+":"+(next!=null)+":"+(last!=null)+"}";}
	}
	
	public static class Edge {
		public static final Vertex VERTICAL=new Vertex(new PointF(0f,0f));
		public static final Vertex NONE=new Vertex(new PointF(0f,0f));
		public Vertex start;
		public Vertex end;
		
		public Edge(Vertex start, Vertex end) {
			super();
			this.start = start;
			this.end = end;
		}
		public String toString() {return "edge{"+PointUtil.tostr(start.p)+"-"+PointUtil.tostr(end.p)+"}";}
		
		public Vertex getVertexForXPos(float xpos) {
			if (start.p.x==xpos && end.p.x==xpos) {
				return VERTICAL;
			} else if  (start.p.x==xpos) {
				return start;
			} else if  (end.p.x==xpos) {
				return end;
			} else return NONE;
		}
	}
	
	public static class Intersection {
		public Edge e1;
		public Edge e2;
		public PointF p;
		public Intersection(Edge e1, Edge e2, PointF p) {
			super();
			this.e1 = e1;
			this.e2 = e2;
			this.p = p;
		}
	}
	
	
	public ArrayList<Vertex> verticies = new ArrayList<Vertex>();
	public ArrayList<Edge> edges = new ArrayList<Edge>();
	public ArrayList<Intersection> intersections = new ArrayList<Intersection>();
	public HashMap<Edge,ArrayList<Intersection>> intersectionLookup = new HashMap<Edge,ArrayList<Intersection>>();
	
	public Set<Edge> outline = new HashSet<Edge>();
	
	Vertex lastVertex = null;
	//Edge lastEdge = null;
	Vertex leftMost = null;
	
	
	
	public Vertex addVertex(PointF p) {
		Vertex v = null;
		for (Vertex v1: verticies) {
			if (v1.equals(p)) {v=v1; break;}
		}
		if (v==null) {
			v = new Vertex( p );
			verticies.add( v );
		}
		if (lastVertex!=null) {
			Edge e = new Edge( lastVertex, v );
			//lastVertex.next=e;
			//v.last=e;
			lastVertex.edges.add(e);
			v.edges.add(e);
			edges.add( e );

		}
		lastVertex = v;
		if (leftMost==null) {
			leftMost=v;
		} else if (leftMost.p.x>v.p.x) {
			leftMost=v;
		}
		Log.d(DVGlobals.LOG_TAG, "addVertex:"+lastVertex);
		return v;
	}
	
	public void findIntersections() {
		ArrayList<Edge> edgesDup = new ArrayList<Edge>(edges);
		while (edgesDup.size()>0) {// test edge
			Edge e1 = edgesDup.remove(0);
			for (Edge e2:edgesDup) {
				if (e2==e1 || e1.start.equals(e2.end.p)|| e1.end.equals(e2.start.p)|| e2.start.equals(e1.end.p)||e2.end.equals(e2.start.p)) {continue;}
				PointF p = intersection(e1,e2);
				if (p!=null) {
					///boolean found = false;
					//ArrayList<Intersection> e1Ints = intersectionLookup.get(e1);
					//if (e1Ints!=null) {
					//	for (Intersection i :e1Ints) {
					//		if ((i.e1==e1&&i.e2==e2) || (i.e1==e2&&i.e2==e1) ) {found=true;}
					//	}
					//}
					//if (!found) {
						Intersection i = new Intersection(e1,e2,p);
						intersections.add(i);
						addLookup(e1, i);
						addLookup(e2, i);
						Log.d(DVGlobals.LOG_TAG,"inter added:"+intersections.size() );
					//}
				}
			}
		}
	}

	private void addLookup(Edge e1, Intersection i) {
		ArrayList<Intersection> e1Ints= intersectionLookup.get(e1);
		if (e1Ints==null) {
			e1Ints=new ArrayList<Intersection>();
			intersectionLookup.put(e1,e1Ints);
		}
		e1Ints.add(i);
	}

	private PointF intersection(Edge e1, Edge e2) {
		float dx1=e1.end.p.x-e1.start.p.x;
		float dx2=e2.end.p.x-e2.start.p.x;
		float dy1=e1.end.p.y-e1.start.p.y;
		float dy2=e2.end.p.y-e2.start.p.y;
		float den = dy2*dx1-dx2*dy1;
		if (den==0) {return null;}
		float ux = (dx2*(e1.start.p.y-e2.start.p.y) - dy2*(e1.start.p.x-e2.start.p.x))/den;
		float uy = (dx1*(e1.start.p.y-e2.start.p.y) - dy1*(e1.start.p.x-e2.start.p.x))/den;
		//Log.d(Globals.LOG_TAG,"computeIntersection : );
		if ( ux>=0 && ux<=1 && uy>=0 && uy<=1) {
			float x= e1.start.p.x+ux*dx1;
			float y= e1.start.p.y+ux*dy1;
			//Log.d(Globals.LOG_TAG,"computeIntersection :" +
			//		"e1("+PointUtil.tostr(e1.start.p)+"-"+PointUtil.tostr(e1.end.p)+")" +
			//		"e2("+PointUtil.tostr(e2.start.p)+"-"+PointUtil.tostr(e2.end.p)+")" +
			//		" ux:"+ux+" uy:"+uy+" int["+x+","+y+"]");
			return new PointF(x,y);
			//float x2= e2.start.p.x+uy*dx2;
			//float y2= e2.start.p.y+uy*dy2;
			//Log.d(Globals.LOG_TAG,"computeIntersection : x:"+x2+" y:"+y2);
		} 
		
		return null;
	}
	
	public void construct(PointVec pv) {
		for (PointF p : pv) {
			addVertex(p);
		}
		addVertex(pv.get(0));
	}
	
	public void testIntersection() {
		Vertex v1=new Vertex(new PointF(1,1));
		Vertex v2=new Vertex(new PointF(4,3));
		Vertex v3=new Vertex(new PointF(3,1));
		Vertex v4=new Vertex(new PointF(4,5));
		Edge e1 = new Edge(v1, v2);
		Edge e2 = new Edge(v3, v4);
		intersection( e1,  e2);
	}
	private ArrayList<Intersection> foundIntersections = new ArrayList<SegmentGraphOld.Intersection>();
	private void  checkIntersection(Edge e) {
		foundIntersections.clear();
		for (Intersection i :intersections) {
			if (i.e1==e||i.e2==e) {
				foundIntersections.add(i);
			}
		}
	}
	/////////////////////////////////////////// Edge scan //////////////////////////////////////////////////////
	public static class Scanline {
		public float position=0;
		public float min = -1;
		public float max = -1;
		public ArrayList<ScanLineReigon> thisRegions = new ArrayList<ScanLineReigon>();
		public ArrayList<ScanLineReigon> lastRegions = new ArrayList<ScanLineReigon>();
		Edge scanEdge = new Edge(new Vertex(new PointF(position,min)),new Vertex(new PointF(position,max)));
		public Edge getScanEdge() {
			scanEdge.start.p.x=position;
			scanEdge.end.p.x=position;
			scanEdge.start.p.y=min;
			scanEdge.end.p.y=max;
			return scanEdge;
		}
	}
	
	public static class ScanLineReigon {
		public Edge startEdge;
		public float start;
		public float end;
		public Edge endEdge;
		public boolean inside;
		
	}
	public static class ScanEdgeData {
		public Edge e;
		Vertex minXVertex ;
		Vertex maxXVertex;
		Vertex minYVertex;
		Vertex maxYVertex;
		boolean inside=false;
		//float minX = -1;
		//float maxX = -1;
		float scanLineIntersect=-1f;
		//public Vertex getMinXVertex() {return e.getVertexForXPos(minX);}
		//public Vertex getMaxXVertex() {return e.getVertexForXPos(maxX);}
	}
	
	public ArrayList<ScanEdgeData> activeEdges = new ArrayList<ScanEdgeData>();
	public ArrayList<ScanEdgeData> activeEdgesRemove = new ArrayList<ScanEdgeData>();
	public ArrayList<ScanEdgeData> activeEdgesRemoveVert = new ArrayList<ScanEdgeData>();
	public ArrayList<ScanEdgeData> sortedEdgeData = new ArrayList<ScanEdgeData>();
	public Scanline scanline ;
	int scanStep = 0;
	public void traceOutline() {
		scanline = new Scanline();
		buildEdges(scanline);
		scanStep=-1;
		scanline.lastRegions.clear();
		//scanline.lastRegions.add(new Reigon(scanline));
		
		//while (scanStep<sortedEdgeData.size()) {
			scanStep();
		//}
	}

	public void scanStep() {
		updatePosition();
		if (activeEdges.size()==0 && activeEdgesRemove.size()==0) {
			scanStep=-1;
			outline.clear();
			updatePosition();
		}
		activeEdgesRemoveVert.clear();
		for (ScanEdgeData spd:activeEdgesRemove) {
			if (spd.e.end.p.x==spd.e.start.p.x) {
				activeEdgesRemove.remove(spd);
				activeEdgesRemoveVert.add(spd);
			}
		}
		activeEdges.removeAll(activeEdgesRemove);
		
		// need to add relevant removed edges to output 
		// sort edge intersections vertically
		Collections.sort(activeEdges,new Comparator<ScanEdgeData>() {
			@Override
			public int compare(ScanEdgeData s1, ScanEdgeData s2) {
				if (s1.scanLineIntersect!=s2.scanLineIntersect) {
					return s1.scanLineIntersect<s2.scanLineIntersect?-1:1;
				} else if (s1.scanLineIntersect==s1.minYVertex.p.y) {
					return s1.maxYVertex.p.y<s2.maxYVertex.p.y?-1:1;
				} else  {
					return s1.minYVertex.p.y<s2.minYVertex.p.y?-1:1;
				}
			}
		});
		// build reigons.
		/*
		if (activeEdges.size()>0) {
			ScanEdgeData lastScanPt = activeEdges.get(0);
			ScanLineReigon rstart = new ScanLineReigon();
			rstart.start=scanline.min;
			rstart.startEdge=null;
			rstart.end=lastScanPt.scanLineIntersect;
			rstart.endEdge=lastScanPt.e;
			rstart.inside=false;
			scanline.thisRegions.add(rstart);
			
			for (int i=1;i<activeEdges.size();i++) {
				ScanEdgeData spd = activeEdges.get(i);
				ScanLineReigon r1 = new ScanLineReigon();
				if (spd.minXVertex.p.x==scanline.position) {// the start of a new edge
					if (spd.maxXVertex.p.x==spd.minXVertex.p.x) {// vertical edge
						r1.start=spd.minYVertex.p.y;
						r1.startEdge=spd.e;
						r1.end=spd.maxYVertex.p.y;;
						r1.endEdge=spd.e;
						r1.inside=true;
					} else if (spd.minXVertex.p.y==lastScanPt.minXVertex.p.y) {// starting vertex
						ScanEdgeData startEdge = (spd.maxXVertex.p.y<lastScanPt.maxXVertex.p.y)?spd:lastScanPt;
						ScanEdgeData endEdge = (spd.maxXVertex.p.y>lastScanPt.maxXVertex.p.y)?spd:lastScanPt;
						r1.start=startEdge.minYVertex.p.y;
						r1.startEdge=startEdge.e;
						r1.end=endEdge.maxYVertex.p.y;;
						r1.endEdge=endEdge.e;
						r1.inside=true;
					}
				} else if () {
					
				}
				
				scanline.thisRegions.add(r1);
				lastScanPt=spd;
			}
			ScanLineReigon rend = new ScanLineReigon();
			rend.start=lastScanPt.scanLineIntersect;
			rend.startEdge=lastScanPt.e;
			rend.end=scanline.max;
			rend.endEdge=null;
			rend.inside=false;
			scanline.thisRegions.add(rend);
		} else {
			ScanLineReigon rstart = new ScanLineReigon();
			rstart.start=scanline.min;
			rstart.startEdge=null;
			rstart.end=scanline.max;
			rstart.endEdge=null;
			rstart.inside=false;
			scanline.thisRegions.add(rstart);
		}
		*/
		if (activeEdges.size()>0) {
			ScanEdgeData edge = activeEdges.get(0);
			edge.inside=false;
			outline.add(edge.e);
			edge = activeEdges.get(activeEdges.size()-1);
			edge.inside=false;
			outline.add(edge.e);
		}
		activeEdges.removeAll(activeEdgesRemoveVert);
		ArrayList<ScanLineReigon> tmp=scanline.lastRegions;
		scanline.lastRegions=scanline.thisRegions;
		scanline.thisRegions=tmp;
		scanline.thisRegions.clear();
	}

	private void updatePosition() {
		scanStep++;
		//scanStep%=sortedEdgeData.size();
		if (scanStep<sortedEdgeData.size()) {
			ScanEdgeData e = sortedEdgeData.get(scanStep);
			scanline.position=e.minXVertex.p.x;
			activeEdges.add(e);
			boolean isVertex = false;
			boolean finishedCheckForNewEdges = false;
			while (!finishedCheckForNewEdges) {
				if (scanStep+1<sortedEdgeData.size()) {
					ScanEdgeData nxt = sortedEdgeData.get(scanStep+1);
					if (nxt.minXVertex.p.x==scanline.position) {
						scanStep++;
						activeEdges.add(nxt);
						//scanline.position=nxt.min;
						isVertex=true;
					} else{finishedCheckForNewEdges=true;}
				}else {finishedCheckForNewEdges=true;}
			}
		} else {
			// no new edges left - look ahead for closest endpoint
			float newPos = 1e6f;
			for (int i=0;i<activeEdges.size();i++) {
				newPos=Math.min(newPos, activeEdges.get(i).maxXVertex.p.x);
			}
			scanline.position=newPos;
		}
		activeEdgesRemove.clear();
		Edge scanEdge = scanline.getScanEdge();
		// get Edge intersections and remove old
		for (int i=0;i<activeEdges.size();i++) {
			ScanEdgeData thisEdge = activeEdges.get(i);
			
			PointF inter = intersection(scanEdge, thisEdge.e);
			Log.d(DVGlobals.LOG_TAG, "check edge res:"+PointUtil.tostr(inter)+" scan:"+scanEdge+"  edge:"+thisEdge.e);
			if (inter==null || inter.x==thisEdge.maxXVertex.p.x) {
				activeEdgesRemove.add(thisEdge);
			} else {
				thisEdge.scanLineIntersect=inter.y;
			}
		}
	}

	private void buildEdges(Scanline scanLine) {
		ArrayList<Edge> sortedEdges = new ArrayList<Edge>();
		for (final Edge theEdge : edges) {
			ArrayList<Intersection> edgeIntersections = intersectionLookup.get(theEdge);
			if (edgeIntersections!=null && edgeIntersections.size()>0) {
				//make a list of intersections sorted by dist form the start - to use for creating edges
				ArrayList<Intersection> tmpEdgeIntersections=new ArrayList<SegmentGraphOld.Intersection>();
				tmpEdgeIntersections.addAll(edgeIntersections);
				Collections.sort(tmpEdgeIntersections,new Comparator<Intersection>() {
					@Override
					public int compare(Intersection i1,Intersection i2) {
						float dist1=0;
						if (i1.e1==theEdge) {
							dist1 = PointUtil.dist(i1.e1.start.p, i1.p);
						} else {
							dist1 = PointUtil.dist(i1.e2.start.p, i1.p);
						}
						float dist2=0;
						if (i2.e1==theEdge) {
							dist2 = PointUtil.dist(i2.e1.start.p, i2.p);
						} else {
							dist2 = PointUtil.dist(i2.e2.start.p, i2.p);
						}
						float dist = dist1-dist2;
						return dist<0?-1:1;
					}
				});
				PointF start = theEdge.start.p;
				PointF end = null;
				while (tmpEdgeIntersections.size()>0) {
					Intersection i = tmpEdgeIntersections.remove(0);
					end = i.p;
					Edge e = new Edge(new Vertex(start),new Vertex(end));
					sortedEdges.add(e);
					start=end;
				}
				Edge e = new Edge(new Vertex(start),new Vertex(theEdge.end.p));
				sortedEdges.add(e);
			} else {
				sortedEdges.add(theEdge);
			}
		}
		
		Collections.sort(sortedEdges,new Comparator<Edge>() {
			@Override
			public int compare(Edge e1, Edge e2) {
				float f = Math.min(e1.start.p.x, e1.end.p.x)-Math.min(e2.start.p.x, e2.end.p.x);
				return (f<0?-1:(f==0?0:1));
			}
		});
		// build scan edge data
		for (int i=0;i<sortedEdges.size();i++) {
			Edge e= sortedEdges.get(i);
			ScanEdgeData ed = new ScanEdgeData();
			ed.e = e;
			ed.minXVertex=e.start.p.x<e.end.p.x?e.start:e.end;
			ed.maxXVertex=e.start.p.x>e.end.p.x?e.start:e.end;
			ed.minYVertex=e.start.p.y<e.end.p.y?e.start:e.end;
			ed.maxYVertex=e.start.p.y>e.end.p.y?e.start:e.end;
			if (scanLine.min!=-1) {
				scanLine.min=Math.min(scanLine.min,Math.min(e.start.p.y, e.end.p.y));
			} else {
				scanLine.min=Math.min(e.start.p.y, e.end.p.y);
			}
			if (scanLine.min!=-1) {
				scanLine.max=Math.max(scanLine.max,Math.max(e.start.p.y, e.end.p.y));
			} else{
				scanLine.max=Math.max(e.start.p.y, e.end.p.y);
			}
			//Log.d(Globals.LOG_TAG,"e: "+ed.minX+" - "+ed.maxX+" : "+(ed.maxX-ed.minX) );
			sortedEdgeData.add(ed);
		}
	}
	
	
	/*
	public void traceOutline() {
		Vertex currentVertex = null;
		boolean fwd=true;
		Edge currentEdge;
		while(currentVertex != leftMost) {
			if (currentVertex==null){currentVertex=leftMost;}
			Log.d(Globals.LOG_TAG, "traceOutline:"+fwd+":"+currentVertex.toString());
			currentEdge = fwd?currentVertex.next:currentVertex.last;
			checkIntersection(currentEdge);
			if (foundIntersections.size()>0) {
				// find the intersection cosest to this vertex
				Intersection currentInter = foundIntersections.get(0);
				for (int i=1;i<foundIntersections.size();i++) {
					Intersection intersection = foundIntersections.get(i);
					if (PointUtil.dist(currentVertex.p, intersection.p)<PointUtil.dist(currentVertex.p, currentInter.p)) {
						currentInter=intersection;
					}
				}
				// now find the minimum angle for the crossection and follow it. We need to traverse to another vertex on the intersecting edge
				Edge intersectingEdge = currentInter.e1!=currentEdge?currentInter.e1:currentInter.e2;
				float angleStart = PointUtil.calcAngle(currentVertex.p, currentInter.p, intersectingEdge.start.p);
				float angleEnd = PointUtil.calcAngle(currentVertex.p, currentInter.p, intersectingEdge.end.p);
				
				Vertex v1 = new Vertex(currentInter.p);
				Edge e1 = new Edge(fwd?currentEdge.start:currentEdge.end, v1);
				v1.last=e1;
				outline.add(e1);
				Edge e2=null;
				if (angleStart<angleEnd) {
					e2 = new Edge(v1, intersectingEdge.start);
					fwd=false;
				} else {
					e2 = new Edge(v1, intersectingEdge.end);
					fwd=true;
				}
				v1.next=e2;
				outline.add(e2);
				currentVertex=e2.end;
				continue;
			} else {
				currentVertex=fwd?currentEdge.end:currentEdge.start;
				outline.add(currentEdge);
			}
			
		}
	}
	*/
	
	
	
	
}
