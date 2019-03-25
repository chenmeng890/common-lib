package com.yihaodian.common.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConcurrentLFUCacheTest {
	
	ConcurrentLFUCache<Integer, String> cache;  
	
	@Before
	public void setup(){
		cache = new ConcurrentLFUCache<Integer, String>(4, 100, 1000);
	}
	
	@After
	public void tearDown(){
		cache.clear();
		cache = null;
	}

	@Test
	public void testPutGet() {
		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i);
			cache.put(i, str);
			Assert.assertEquals(str, cache.get(i));
		}		
	}

	@Test
	public void testUpdate() {
		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i);
			cache.put(i, str);
			Assert.assertEquals(str, cache.get(i));
		}
		
		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i + 10);
			cache.put(i, str);
			Assert.assertEquals(str, cache.get(i));
		}
	}

	@Test
	public void testRemove() {
		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i);
			cache.put(i, str);
			Assert.assertEquals(str, cache.get(i));
		}
		
		for(int i = 0; i < 5; ++ i) {
			cache.remove(i);
			Assert.assertNull(cache.get(i));
		}
		
		for(int i = 5; i < 10; ++ i) {		
			Assert.assertNotNull(cache.get(i));
		}
	}

	@Test
	public void testContainsKey() {
		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i);
			cache.put(i, str);
			Assert.assertEquals(str, cache.get(i));
			Assert.assertTrue(cache.containsKey(i));
		}
		
		for(int i = 15; i < 20; ++ i) {		
			Assert.assertFalse(cache.containsKey(i));
		}
		
	}

	@Test
	public void testClear() {
		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i);
			cache.put(i, str);
			Assert.assertEquals(str, cache.get(i));		
		}
		
		cache.clear();
		
		for(int i = 0; i < 10; ++ i) {			
			Assert.assertNull(cache.get(i));
		}	
		
	}

	@Test
	public void testSize() {
		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i);
			cache.put(i, str);
			Assert.assertEquals(str, cache.get(i));		
		}
		
		Assert.assertEquals(10, cache.size());
	}

	@Test
	public void testIsEmpty() {
		
		Assert.assertTrue(cache.isEmpty());
		
		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i);
			cache.put(i, str);
			Assert.assertEquals(str, cache.get(i));		
		}
		
		Assert.assertFalse(cache.isEmpty());
		
		cache.clear();
		
		Assert.assertTrue(cache.isEmpty());
		
		
	}

	@Test
	public void testPutAll() {
		Map<Integer, String> map = new HashMap<Integer, String>();
		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i);
			map.put(i, str);
		}
		
		cache.putAll(map);
		
		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i);
			Assert.assertEquals(str, cache.get(i));			
		}
	}

	@Test
	public void testEntrySet() {
		Map<Integer, String> map = new HashMap<Integer, String>();

		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i);
			cache.put(i, str);
			Assert.assertEquals(str, cache.get(i));		
			map.put(i, str);

		}
		
		Set<Entry<Integer, String>> set = cache.entrySet();
		for(Entry<Integer, String> e : set) {
			Assert.assertEquals(e.getValue(), map.get(e.getKey()));
			map.remove(e.getKey());
		}
		
		Assert.assertTrue(map.isEmpty());
	}

	@Test
	public void testKeySet() {
		Map<Integer, String> map = new HashMap<Integer, String>();

		for(int i = 0; i < 10; ++ i) {
			String str = Integer.toString(i);
			cache.put(i, str);
			Assert.assertEquals(str, cache.get(i));		
			map.put(i, str);

		}
		
		Assert.assertEquals(map.keySet(), cache.keySet());
	}


}
