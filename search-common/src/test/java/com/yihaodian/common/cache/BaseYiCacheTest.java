package com.yihaodian.common.cache;

import org.junit.BeforeClass;
import org.junit.Test;

public class BaseYiCacheTest {
	
	static BaseYCache<Integer,String> cache;
    
	@BeforeClass
	public static void setup(){
		cache=new BaseYCache<Integer,String>(500,500);
	}
	
	@Test
	public void testBaseYiCache() throws InterruptedException{
		String str=null;
		for(int i=0;i<100;i++){
			str= "" + i;
			cache.put(i,str);
		}
		System.out.println("cache.size="+cache.size());
		for(int i=50;i<125;i++){
			cache.get(i);
		}
		System.out.println("cache.cacheHit="+cache.cacheStats().hitCount());
		System.out.println("cache.cacheMisses="+cache.cacheStats().totalAccess());
		Thread.sleep(1000);
		System.out.println("cache.size="+cache.size());
	}
}
