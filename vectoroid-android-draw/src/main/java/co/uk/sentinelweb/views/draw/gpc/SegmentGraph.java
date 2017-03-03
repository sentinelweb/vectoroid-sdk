package co.uk.sentinelweb.views.draw.gpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import co.uk.sentinelweb.views.draw.DVGlobals;
import co.uk.sentinelweb.views.draw.model.PointVec;
import co.uk.sentinelweb.views.draw.model.path.PathData;
import co.uk.sentinelweb.views.draw.util.PointUtil;

public class SegmentGraph {
	public SegmentGraph () {
		
	}
	
	
	public static class Vertex {
		public HashSet<Edge> edges =new HashSet<Edge>();
		//Edge next;
		//Edge last;
		public PathData p;
		
		public Vertex(PathData p) {
			super();
			this.p = p;
		}
		
		public boolean equals(PointF p1) {
			return Math.abs(p.x-p1.x)<1e-6 && Math.abs(p.y-p1.y)<1e-6;
		}
		
		
		public String toString() {return "vtx{"+PointUtil.tostr(p)+":"+edges.size()+"}";}
	}
	
	public static class Edge {
		public static final Vertex VERTICAL=new Vertex(new PathData(0f,0f));
		public static final Vertex NONE=new Vertex(new PathData(0f,0f));
		public Vertex start;
		public Vertex end;
		
		public Edge(Vertex start, Vertex end) {
			super();
			this.start = start;
			this.end = end;
			start.edges.add(this);
			end.edges.add(this);
		}
		public String toString() {return "edge{"+PointUtil.tostr(start.p)+"-"+PointUtil.tostr(end.p)+"("+PointUtil.dist(start.p,end.p)+")"+hashCode()+"}";}
		public boolean isStart(Vertex v) {
			return start.p.equals(v.p.x,v.p.y);
		}
		public boolean isEnd(Vertex v) {
			return end.p.equals(v.p.x,v.p.y);
		}
		public Vertex otherSide(Vertex v) {
			return isStart(v)?end:start;
		}
	}
	
	public static class Intersection {
		public Edge e1;
		public Edge e2;
		public PathData p;
		public Intersection(Edge e1, Edge e2, PathData p) {
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
	
	public ArrayList<Edge> outline = new ArrayList<Edge>();
	
	Vertex lastVertex = null;
	//Edge lastEdge = null;
	Vertex leftMost = null;
	
	public Vertex addVertex(PathData p) {
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
			//lastVertex.edges.add(e);
			//v.edges.add(e);
			edges.add( e );

		}
		lastVertex = v;
		if (leftMost==null) {
			leftMost=v;
		} else if (leftMost.p.x>v.p.x) {
			leftMost=v;
		}
		return v;
	}
	
	public void findIntersections(ArrayList<Edge> edges) {
		intersections.clear();
		intersectionLookup.clear();
		ArrayList<Edge> edgesDup = new ArrayList<Edge>(edges);
		//Log.d(Globals.LOG_TAG, "findIntersections:"+edges.size());
		while (edgesDup.size()>0) {// test edge
			Edge e1 = edgesDup.remove(0);
			for (Edge e2:edgesDup) {
				PathData p = intersection(e1,e2);
				if (p!=null && !(e1.start.equals(p) || e1.end.equals(p) || e2.start.equals(p) || e2.end.equals(p))) {//
						Intersection i = new Intersection(e1,e2,p);
						intersections.add(i);
						addLookup(e1, i);
						addLookup(e2, i);
				}
			}
		}
		Log.d(DVGlobals.LOG_TAG,"inters added:"+intersections.size() );
	}

	private void addLookup(Edge e1, Intersection i) {
		ArrayList<Intersection> e1Ints= intersectionLookup.get(e1);
		if (e1Ints==null) {
			e1Ints=new ArrayList<Intersection>();
			intersectionLookup.put(e1,e1Ints);
		}
		e1Ints.add(i);
	}
	
	RectF useRect = new RectF();
	private PathData intersection(Edge e1, Edge e2) {
		double dx1=e1.end.p.x-e1.start.p.x;
		double dx2=e2.end.p.x-e2.start.p.x;
		double dy1=e1.end.p.y-e1.start.p.y;
		double dy2=e2.end.p.y-e2.start.p.y;
		double den = dy2*dx1-dx2*dy1;
		if (den==0) {return null;}
		double ux = (dx2*(e1.start.p.y-e2.start.p.y) - dy2*(e1.start.p.x-e2.start.p.x))/den;
		double uy = (dx1*(e1.start.p.y-e2.start.p.y) - dy1*(e1.start.p.x-e2.start.p.x))/den;
		//Log.d(Globals.LOG_TAG,"computeIntersection : );
		if ( ux>=0 && ux<=1 && uy>=0 && uy<=1) {
			double x = e1.start.p.x+ux*dx1;
			double y = e1.start.p.y+ux*dy1;
			//Log.d(Globals.LOG_TAG,"computeIntersection :" +
			//		"e1("+PointUtil.tostr(e1.start.p)+"-"+PointUtil.tostr(e1.end.p)+")" +
			//		"e2("+PointUtil.tostr(e2.start.p)+"-"+PointUtil.tostr(e2.end.p)+")" +
			//		" ux:"+ux+" uy:"+uy+" int["+x+","+y+"]");
			PathData res = new PathData((float)x,(float)y);
			useRect.set(e1.start.p.x,e1.start.p.y,e1.end.p.x,e1.end.p.y);
			boolean checkE1 = PointUtil.checkBounds2(useRect, res);
			useRect.set(e2.start.p.x,e2.start.p.y,e2.end.p.x,e2.end.p.y);
			boolean checkE2 = PointUtil.checkBounds2(useRect, res);
			if (checkE1 && checkE2) {
				return res;
			}
			//float x2= e2.start.p.x+uy*dx2;
			//float y2= e2.start.p.y+uy*dy2;
			//Log.d(Globals.LOG_TAG,"computeIntersection : x:"+x2+" y:"+y2);
		} 
		
		return null;
	}
	
	public void construct(PointVec pv) {
		Log.d(DVGlobals.LOG_TAG,"pvsize:"+pv.size() );
		for (PathData p : pv) {
			addVertex(p);
		}
		//addVertex(pv.get(0));
		//closeloop
		PointF last = pv.get(pv.size()-1);
		if (!pv.get(0).equals(last.x, last.y)) {
			Edge e = new Edge(verticies.get(verticies.size()-1),verticies.get(0));
			edges.add(e);
		}
		
		Log.d(DVGlobals.LOG_TAG,"verticies:"+verticies.size() );
		for (Vertex v :verticies) {
			Log.d(DVGlobals.LOG_TAG,""+v );
		}
		Log.d(DVGlobals.LOG_TAG,"edges:"+edges.size() );
		for (Edge v :edges) {
			Log.d(DVGlobals.LOG_TAG,""+v );
		}
	}
	/*
	public void extract(PointVec pointVec) {
		pointVec.clear();
		Edge currentEdge = outline.get(0);
		Vertex currentVertex = null;
		Log.d(Globals.LOG_TAG, "extract:"+pointVec.size());
		while (currentVertex!=leftMost) {
			if (currentVertex==null) {
				currentVertex=leftMost;
			} 
			pointVec.add(currentVertex.p);
			currentVertex=currentEdge.otherSide(currentVertex);
			for (Edge e :currentVertex.edges) {if (e!=currentEdge && (currentVertex.edges.size()==2 || outline.contains(e))) {currentEdge=e;break;}}
			//Log.d(Globals.LOG_TAG, "extract:"+currentVertex+" : "+currentEdge);
		}
		Log.d(Globals.LOG_TAG, "extract:"+pointVec.size());
		pointVec.closed=true;
	}
	*/
	public void testIntersection() {
		Vertex v1=new Vertex(new PathData(1,1));
		Vertex v2=new Vertex(new PathData(4,3));
		Vertex v3=new Vertex(new PathData(3,1));
		Vertex v4=new Vertex(new PathData(4,5));
		Edge e1 = new Edge(v1, v2);
		Edge e2 = new Edge(v3, v4);
		intersection( e1,  e2);
	}
	
	public void removeIntersections() {
		findIntersections(edges);
		while (intersections.size()>0) {
			removeIntersection();
		}
	}
	
	public Vertex findCloseVertex(PointF p) {
		float tolerance=20f;
		for (Vertex v :verticies) {
			if (Math.abs(v.p.x-p.x)<tolerance && Math.abs(v.p.y-p.y)<tolerance) return v;
		}
		return null;
	}
	
	public void removeIntersection() {
		ArrayList<Edge> edgesWithIntersections = new ArrayList<Edge>();
		edgesWithIntersections.clear();
		edgesWithIntersections.addAll(intersectionLookup.keySet());
		//ArrayList<Edge> sortedEdges = new ArrayList<Edge>();
		
		for (int i=0;i<edges.size();i++) {
			final Edge theEdge = edges.get(i);
			ArrayList<Intersection> edgeIntersections = intersectionLookup.get(theEdge);
			if (edgeIntersections!=null && edgeIntersections.size()>0) {
				//make a list of intersections sorted by dist form the start - to use for creating edges
				//ArrayList<Intersection> tmpEdgeIntersections=new ArrayList<SegmentGraph1.Intersection>();
				//tmpEdgeIntersections.addAll(edgeIntersections);
				/*
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
				*/
				/*
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
				*/
				
				Intersection firstIntersection = edgeIntersections.remove(0);
				Edge intersectingEdge = firstIntersection.e1==theEdge?firstIntersection.e2:firstIntersection.e1;
				//Log.d(Globals.LOG_TAG, "edges b4:"+edges.size()+"\n"+theEdge+"\n"+intersectingEdge+"\n------");
				
				//Vertex newVertex = findCloseVertex(firstIntersection.p);
				//if (newVertex==null) 
				Vertex newVertex=new Vertex(firstIntersection.p);
				Edge theEdgeStart = new Edge(theEdge.start, newVertex);
				Edge theEdgeEnd = new Edge(theEdge.end, newVertex);
				Edge intersectingEdgeStart = new Edge(intersectingEdge.start, newVertex);
				Edge intersectingEdgeEnd = new Edge(intersectingEdge.end, newVertex);
				theEdge.start.edges.remove(theEdge);
				theEdge.end.edges.remove(theEdge);
				intersectingEdge.start.edges.remove(intersectingEdge);
				intersectingEdge.end.edges.remove(intersectingEdge);
				edges.remove(theEdge);
				edges.remove(intersectingEdge);
				edges.add(theEdgeStart);
				edges.add(theEdgeEnd);
				edges.add(intersectingEdgeStart);
				edges.add(intersectingEdgeEnd);
				edgesWithIntersections.remove(theEdge);
				edgesWithIntersections.remove(intersectingEdge);
				edgesWithIntersections.add(theEdgeStart);
				edgesWithIntersections.add(theEdgeEnd);
				edgesWithIntersections.add(intersectingEdgeStart);
				edgesWithIntersections.add(intersectingEdgeEnd);
				//Log.d(Globals.LOG_TAG, "edges after:"+edges.size()+"\n"+theEdgeStart+"\n"+theEdgeEnd+"\n"+intersectingEdgeStart+"\n"+intersectingEdgeEnd+"\n------");
				if (edgeIntersections.size()>0 || intersectionLookup.get(intersectingEdge).size()>1) {
					break;
				}
				
			}
		}
		findIntersections(edgesWithIntersections);
	}
	
	public void traceOutlineTurtle(PointVec pointVec) {
		pointVec.clear();
		outline.clear();
		Vertex currentVertex = null;
		boolean fwd=true;
		Edge currentEdge=null;
		while(currentVertex != leftMost) {
			if (currentVertex==null){currentVertex=leftMost;}
			Log.d(DVGlobals.LOG_TAG, "traceOutline:"+fwd+":"+currentVertex.toString());
			if (currentEdge==null) {// initialiser
				for (Edge e : currentVertex.edges) {
					if (currentEdge==null) {
						currentEdge=e;continue;
					}
					if ( currentEdge.isStart(currentVertex)  ) {
						if (e.end.p.y<currentEdge.end.p.y) {
							currentEdge=e;
						}
					} else {
						if (e.start.p.y<currentEdge.start.p.y) {
							currentEdge=e;
						}
					}
				}
				pointVec.add(currentVertex.p);
				currentVertex=currentEdge.otherSide(currentVertex);
				outline.add(currentEdge);
				pointVec.add(currentVertex.p);
				continue;
			}
			float angle = (float)(2*Math.PI);
			Edge nextEdge = null;
			for (Edge e : currentVertex.edges) {
				if (e==currentEdge){continue;}
				float calcAngle = PointUtil.calcAngle2PI(currentEdge.otherSide(currentVertex).p, currentVertex.p, e.otherSide(currentVertex).p);
				if (calcAngle<angle) {
					angle=calcAngle;
					nextEdge=e;
				}
			}
			currentEdge=nextEdge;
			currentVertex=currentEdge.otherSide(currentVertex);
			outline.add(currentEdge);
			pointVec.add(currentVertex.p);
		}
		pointVec.closed=true;
	}

	/*
	private ArrayList<Intersection> foundIntersections = new ArrayList<SegmentGraph1.Intersection>();
	
	private void  checkIntersection(Edge e) {
		foundIntersections.clear();
		for (Intersection i :intersections) {
			if (i.e1==e||i.e2==e) {
				foundIntersections.add(i);
			}
		}
	}
	*/
	/////////////////////////////////////////// Edge scan //////////////////////////////////////////////////////
	public static class Scanline {
		public float position=0;
		public float min = -1;
		public float max = -1;
		public ArrayList<ScanLineReigon> thisRegions = new ArrayList<ScanLineReigon>();
		public ArrayList<ScanLineReigon> lastRegions = new ArrayList<ScanLineReigon>();
		Edge scanEdge = new Edge(new Vertex(new PathData(position,min)),new Vertex(new PathData(position,max)));
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
				ArrayList<Intersection> tmpEdgeIntersections=new ArrayList<SegmentGraph.Intersection>();
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
				PathData start = theEdge.start.p;
				PathData end = null;
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
	
}
