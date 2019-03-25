package com.yihaodian.store.tracker;

import java.util.concurrent.atomic.AtomicLong;

import com.yihaodian.common.util.NetUtil;

public class TrackerIdUtil {

	private static AtomicLong prevId = new AtomicLong();
	private static final String localIP = NetUtil.getLocalIP();
	
	public static String genTrackerId() {
		return nextId() + "_" + localIP;
	}
	
	private static synchronized long nextId() {
		long cur = System.currentTimeMillis();

		// avoid duplicates
		if (prevId.get() == cur) {
			cur = cur + 1;
		}

		prevId.set(cur);
		return cur;
	}
}
