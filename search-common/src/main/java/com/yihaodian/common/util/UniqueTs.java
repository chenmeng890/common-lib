package com.yihaodian.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Pseudo unique time-stamp generator
 * 
 * The generated time stamp might be much ahead of the system clock when it's called very frequently, 
 * and might be at most 1s behind the system clock when is called less frequently.
 * 
 * See UniqueTsTest for typical usages.
 * 
 * @author zhouhang
 *
 */
public class UniqueTs {
	private static UniqueTs instance = new UniqueTs();
	
	public static UniqueTs get() {
		return instance;
	}
	
	private AtomicLong id = new AtomicLong();
	
	private Map<String, AtomicLong> idMap = new HashMap<String, AtomicLong>();
		
	public UniqueTs() {		
	}	
	
	public void init() {
		long ts = System.currentTimeMillis();
		if(id.get() < ts) {
			id.set(ts);
		}
	}
	
	public synchronized long next() {
		long next = id.incrementAndGet();
		
		// always check the current ts
		long ts = System.currentTimeMillis();		
		if(next < ts - 1000) {
			id.set(ts);
			next = ts;
		}		
		return next;
	}

	//use diff id by name
	public synchronized long next(String name) {
		if (!idMap.containsKey(name)) {
			idMap.put(name, new AtomicLong());
		}
		long next = idMap.get(name).incrementAndGet();
		
		// always check the current ts
		long ts = System.currentTimeMillis();		
		if(next < ts - 1000) {
			idMap.get(name).set(ts);
			next = ts;
		}		
		return next;
	}

}


