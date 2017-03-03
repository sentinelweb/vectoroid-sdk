package co.uk.sentinelweb.views.draw.util;

import java.util.ArrayList;
import java.util.HashMap;

import co.uk.sentinelweb.commonlibs.Globals;
import co.uk.sentinelweb.views.draw.DVGlobals;

import android.os.SystemClock;
import android.util.Log;

public class Profiler {
	long time= SystemClock.uptimeMillis();
	HashMap<String,Long> _marks = new HashMap<String,Long>();
	ArrayList<String> _marksOrder = new ArrayList<String>();
	StringBuilder sb=new StringBuilder();
	
	
	public Profiler() {
		start();
	}

	public void start() {
		_marks.clear();
		_marksOrder.clear();
		time = SystemClock.uptimeMillis();
	}
	
	public void mark(String mark) {
		if (!_marks.containsKey(mark)) {
			_marks.put(mark,SystemClock.uptimeMillis());
		} else {
			_marks.put(mark,_marks.get(mark)-SystemClock.uptimeMillis());
		}
		if (!_marksOrder.contains(mark)) {
			_marksOrder.add(mark);
		}
	}
	
	public void dump(String tag) {
		sb.delete(0,sb.length());
		sb.append(tag);
		sb.append(">");
		long lasttm=0;
		for (String mark:_marksOrder) {
			sb.append(mark);
			sb.append(":");
			Long tm = _marks.get(mark);
			if (tm>time) {
				tm=tm-time;
				sb.append(tm);
			} else {
				sb.append(tm);
				sb.append("^");
			}
			sb.append("[");
			sb.append(tm-lasttm);
			sb.append("]");
			sb.append(" ");
			lasttm = tm;
		}
		sb.append("total:");
		sb.append(SystemClock.uptimeMillis()-time);
		if (DVGlobals._isDebug) Log.d(DVGlobals.LOG_TAG,sb.toString());
	}
}
