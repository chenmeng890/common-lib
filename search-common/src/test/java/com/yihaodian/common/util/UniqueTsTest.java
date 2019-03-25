package com.yihaodian.common.util;

import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Test;

public class UniqueTsTest {

	@Test
	public void testGlobalUts() {
		HashSet<Long> utsPool = new HashSet<Long>();
		
		for(int i = 0; i < 100; ++i) {
			utsPool.add(UniqueTs.get().next());
		}
		
		Assert.assertEquals(100, utsPool.size());
	}
	
	@Test
	public void testLocalUts() {
		HashSet<Long> utsPool = new HashSet<Long>();
		HashSet<Long> lutsPool1 = new HashSet<Long>();
		HashSet<Long> lutsPool2 = new HashSet<Long>();
		
		UniqueTs luts1 = new UniqueTs();
		UniqueTs luts2 = new UniqueTs();
		
		for(int i = 0; i < 100; ++i) {
			// generate ts1
			long ts1 = luts1.next();
			lutsPool1.add(ts1);
			utsPool.add(ts1);
			
			// generate ts1 with different frequency
			for(int j = 0; j < 2; ++j) {
				long ts2 = luts2.next();
				lutsPool2.add(ts2);
				utsPool.add(ts2);
			}			
		}
		
		Assert.assertEquals(100, lutsPool1.size());
		Assert.assertEquals(200, lutsPool2.size());
		// there are definitely duplicated value between ts1 and ts2
		Assert.assertNotSame(300, utsPool.size());
	}
	
	@Test
	public void testLag() {
		Assert.assertTrue(System.currentTimeMillis() - UniqueTs.get().next() < 1000);
		Assert.assertTrue(System.currentTimeMillis() - new UniqueTs().next() < 1000);
	}
	
	@Test
	public void testInit() {
		UniqueTs uniqueTs = new UniqueTs();
		uniqueTs.init();
	}
	
	@Test
	public void testUseDiffByName() {
		String name0 = "monitor0";
		String name1 = "monitor1";
		UniqueTs uniqueTs = new UniqueTs();
		uniqueTs.next(name0);
		uniqueTs.next(name1);
	}
}
