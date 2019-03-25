package com.yihaodian.common.perf;

import org.junit.Test;

import com.yihaodian.common.perf.PerfTracker;

public class PerfTrackerTest {

	@Test
	public void testTfs() throws InterruptedException {
		PerfTracker pt = new PerfTracker(PerfTrackerTest.class);
		Thread.sleep(10);
		pt.tfs("testTfs::10");
		Thread.sleep(1000);
	}
	

	@Test
	public void testTfl() throws InterruptedException {
		PerfTracker pt = new PerfTracker(PerfTracker.class);
		Thread.sleep(10);
		pt.tfl("testTfs::0-10");
		Thread.sleep(10);
		pt.tfl("testTfs::10-20");
		pt.tfs("testTfs::0-20");
		Thread.sleep(1000);
	}
	

}
