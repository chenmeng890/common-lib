package com.yihaodian.common.perf;

import com.yihaodian.cal.api.CalAPI;
import com.yihaodian.cal.api.dto.CalUserDefinedMethodCallLog;
import com.yihaodian.common.util.JmxUtil;
/**
 * Simpler wrapper for CAL
 * @author zhouhang
 *
 */
public class PerfTracker {

	Class<?> clazz;
	long start;
	long current;
	
	static PerfTrackerManager perfTrackerManager=new PerfTrackerManager();
	
	public PerfTracker(Class<?> clazz) {
		this.start = System.currentTimeMillis();
		this.current = start;
		this.clazz = clazz;
	}
	
	/**
	 * track from start
	 * @param tag
	 * @return
	 */
	public long tfs(String tag){
		if(!perfTrackerManager.isOpen()){
			return 0l;
		}
		this.current = System.currentTimeMillis();
		long elasped = current - start;
		CalUserDefinedMethodCallLog clog = new CalUserDefinedMethodCallLog(
				clazz, tag, new Object[] {}, true,
				elasped, null, "pt");
		CalAPI.addCalUserDefinedMethodCallLog(clog);
		return elasped;
	}
	
	/**
	 * track from last tracked point
	 * @param tag
	 * @return
	 */
	public long tfl(String tag) {
		if(!perfTrackerManager.isOpen()){
			return 0l;
		}
		long old = current;
		current = System.currentTimeMillis();
		long elasped = current - old;
		CalUserDefinedMethodCallLog clog = new CalUserDefinedMethodCallLog(
				clazz, tag, new Object[] {}, true,
				elasped, null, "pt");
		CalAPI.addCalUserDefinedMethodCallLog(clog);
		return elasped;
	}
	
	/**
	 * track count of tag
	 */
	public long trackCount(String tag, long count) {
		if(!perfTrackerManager.isOpen()){
			return 0l;
		}
		CalUserDefinedMethodCallLog clog = new CalUserDefinedMethodCallLog(
				clazz, tag, new Object[] {}, true,
				count, null, "count");
		CalAPI.addCalUserDefinedMethodCallLog(clog);
		return count;
	}
	
	
	public static class PerfTrackerManager implements PerfTrackerManagerMBean{
		
		public boolean open=true;
		
		public PerfTrackerManager(){
			JmxUtil.registerMBean(this);
		}

		@Override
		public void open() {
			open=true;
		}

		@Override
		public void close() {
			open=false;
		}

		@Override
		public boolean isOpen() {
			return open;
		}
		
	}
	
    public static interface PerfTrackerManagerMBean{
		void open();
		void close();
		boolean isOpen();
	}
	
}
