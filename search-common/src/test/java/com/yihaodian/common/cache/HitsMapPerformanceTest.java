package com.yihaodian.common.cache;

import com.yihaodian.common.cache.util.HitsMap;

public class HitsMapPerformanceTest {

	public static void testAccess(int limit, int range) {
		
		HitsMap<Integer> hm = new HitsMap<Integer>();
		
		long s = System.currentTimeMillis();
		
		for(int i = 0; i < limit; ++i) {
			int data = (int) Math.floor(range * Math.random());
			hm.recordAccess(data, 1);			
		}
		
		long e = System.currentTimeMillis();
		
		System.out.println("limit: " + limit + ", range: " + range + ", time: " + (e -s));		
	}

	public static void main(String[] args) {
		int limit = 1000000;
		int range = 10000;
		testAccess(limit, range);
		//limit: 1000000, range: 10000, time: 541
	}
}
