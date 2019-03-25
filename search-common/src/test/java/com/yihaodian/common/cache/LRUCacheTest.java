package com.yihaodian.common.cache;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.yihaodian.common.cache.LRUCache;

public class LRUCacheTest {
	
	private static LRUCache<Integer, String> cache;

	@BeforeClass
	public static void setup(){
		cache = new LRUCache<Integer, String>(10, 10 * 1000);
	}
	
	@After
	public void cleanup(){
		cache.clear();
	}


	@Test
	public void testLRUCache() {
		
		Assert.assertTrue(cache.isEmpty());
		Assert.assertTrue(cache.entrySet() == null || cache.entrySet().isEmpty());
		Assert.assertTrue(cache.keySet() == null || cache.keySet().isEmpty());
	}

	@Test
	public void testPut() {
		cache.put(10, "ten");		
		Assert.assertEquals(1, cache.size());
		Assert.assertFalse(cache.isEmpty());
		Assert.assertEquals("ten", cache.get(10));
	}

	@Test
	public void testGet() {
		cache.put(10, "ten");
		
		Assert.assertEquals(1, cache.size());
		Assert.assertFalse(cache.isEmpty());
		Assert.assertEquals("ten", cache.get(10));
		Assert.assertTrue(cache.containsKey(10));

	}

	@Test
	public void testRemove() {
		cache.put(10, "ten");
		
		Assert.assertEquals(1, cache.size());
		Assert.assertFalse(cache.isEmpty());
		Assert.assertEquals("ten", cache.get(10));
		
		cache.remove(10);
		
		Assert.assertTrue(cache.isEmpty());
		Assert.assertTrue(cache.entrySet() == null || cache.entrySet().isEmpty());
		Assert.assertTrue(cache.keySet() == null || cache.keySet().isEmpty());	
	}

	@Test
	public void testClearExpiredEntry(){
		cache = new LRUCache<Integer, String>(10, 1 * 1000);

		cache.put(10, "ten");
		
		Assert.assertEquals(1, cache.size());
		Assert.assertFalse(cache.isEmpty());
		Assert.assertEquals("ten", cache.get(10));
		
		try {
			Thread.sleep(2 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cache.clearExpiredEntry();
		
		Assert.assertTrue(cache.isEmpty());
		Assert.assertTrue(cache.entrySet() == null || cache.entrySet().isEmpty());
		Assert.assertTrue(cache.keySet() == null || cache.keySet().isEmpty());	
	}

	@Test
	public void testClear() {
		cache.put(10, "ten");
		
		Assert.assertEquals(1, cache.size());
		Assert.assertFalse(cache.isEmpty());
		Assert.assertEquals("ten", cache.get(10));
		
		cache.clear();
		
		Assert.assertEquals(0, cache.size());
		Assert.assertTrue(cache.isEmpty());
		Assert.assertTrue(cache.entrySet() == null || cache.entrySet().isEmpty());
		Assert.assertTrue(cache.keySet() == null || cache.keySet().isEmpty());		
	}

	@Test
	public void testPutAll() {
		Map<Integer, String> map = new HashMap<Integer, String>(); 
		map.put(10, "ten");
		map.put(5, "five");
		
		cache.putAll(map);
		Assert.assertEquals(map.size(), cache.size());
		Assert.assertEquals(map.isEmpty(), cache.isEmpty());
		Assert.assertEquals(map.keySet(), cache.keySet());
		
	}
	
	@Test
	public void testOverflow() {
		for (int i = 0; i < 10; ++i) {
			cache.put(i, Integer.toString(i));
		}
		
		for (int i = 0; i < 5; ++i) {
			cache.get(i);
		}
		
		
		for (int i = 10; i < 15; ++i) {
			cache.put(i, Integer.toString(i));
		}
		
		// 5 6 7 8 9 should not evicted
		for(int i = 5; i < 10; ++i){
			Assert.assertNull(cache.get(i));	
		}
		// 1 2 3 4 should be reserved since they are recently accessed
		for(int i = 0; i < 5; ++i){
			Assert.assertNotNull(cache.get(i));	
		}
		// Newly added elements are reserved.
		for (int i = 10; i < 15; ++i) {
			Assert.assertNotNull(cache.get(i));	
		}

	}
	
	@Test
	public void testPerformance() {	
		
		int maxSize = 10000;
		System.out.println("Problem size " + maxSize);
		cache = new LRUCache<Integer, String>(maxSize, 10 * 1000);

		 
		long s = System.currentTimeMillis(); 
		for (int i = 1; i < maxSize; ++i) {
			cache.put(i, Integer.toString(i));			
		}
		long e = System.currentTimeMillis();
		System.out.println("[Cache] Inserting objects, takes " + (e - s));
		
		s = System.currentTimeMillis(); 
		for (int i = 1; i < maxSize; ++i) {
			cache.put(i, Integer.toString(i));			
		}
		e = System.currentTimeMillis();
		System.out.println("[Cache] Inserting objects again, takes " + (e - s));
		
		Map<Integer, String> map = new HashMap<Integer, String>(maxSize);
		s = System.currentTimeMillis(); 
		for (int i = 1; i < maxSize; ++i) {
			map.put(i, Integer.toString(i));			
		}
		e = System.currentTimeMillis();
		System.out.println("[Map] Inserting objects, takes " + (e - s));
		
		s = System.currentTimeMillis(); 
		for (int i = 1; i < maxSize; ++i) {
			cache.get(i);			
		}
		e = System.currentTimeMillis();
		System.out.println("[Cache] Accessing objects, takes " + (e - s));
		
		s = System.currentTimeMillis(); 
		for (int i = 1; i < maxSize; ++i) {
			cache.get(i);			
		}
		e = System.currentTimeMillis();
		System.out.println("[Cache] Accessing objects again, takes " + (e - s));
		
		
		s = System.currentTimeMillis(); 
		for (int i = 1; i < maxSize; ++i) {
			map.get(i);			
		}
		e = System.currentTimeMillis();
		System.out.println("[Map] Accessing objects, takes " + (e - s));
	}

    
    public static void main(String[] args){
    	final LRUCache<Integer, String> cache = new LRUCache<Integer, String>(100, 10 * 1000);
    	class ReaderThread extends Thread{
    		@Override
    		public void run(){
    			while(true){

    			int i = (int)(100 * Math.random());
    			System.out.println(this.getName() + ",Accessing key: " + i + "value: " + cache.get(i));
    			

    			}
    		}
    	}
    	
    	class WriterThread extends Thread{
    		@Override
    		public void run(){
    			while(true){
    			int i = (int)(100 * Math.random());    			
    			cache.put(i, Integer.toString(i));
    			System.out.println(this.getName() + ", Adding key: " + i + "value: " + cache.get(i));
    			}
    		}
    	}
        
    	ReaderThread reader = new ReaderThread();
    	reader.setName("reader");
    	reader.start();
    	
    	WriterThread writer = new WriterThread();
    	writer.setName("Writer");
    	writer.start();
    	
      	ReaderThread reader2 = new ReaderThread();
    	reader2.setName("reader2");
    	reader2.start();
    	
    	ReaderThread reader3 = new ReaderThread();
    	reader3.setName("reader3");
    	reader3.start();
    	
    	WriterThread writer2 = new WriterThread();
    	writer2.setName("Writer2");
    	writer2.start();
    	
    	try {
			Thread.sleep(1000L * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
