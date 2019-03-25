package com.yihaodian.common.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class LFUCacheTest {
	private static LFUCache<Integer, String> cache;

	@BeforeClass
	public static void setup() {
		cache = new LFUCache<Integer, String>(10, 1000 * 1000);
	}

	@After
	public void cleanup() {
		cache.clear();
	}

	@Test
	public void testLRUCache() {

		Assert.assertTrue(cache.isEmpty());
		Assert.assertTrue(cache.entrySet() == null
				|| cache.entrySet().isEmpty());
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
		Assert.assertTrue(cache.entrySet() == null
				|| cache.entrySet().isEmpty());
		Assert.assertTrue(cache.keySet() == null || cache.keySet().isEmpty());
	}

	@Test
	public void testAddRemove() {
		for (int i = 0; i <= 3; ++i)
			cache.put(i, Integer.toString(i));

		for (int i = 3; i >= 0; --i) {
			cache.remove(i);
		}

	}

	@Test
	public void testClearExpiredEntry() {
		cache = new LFUCache<Integer, String>(10, 1 * 1000);

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
		Assert.assertTrue(cache.entrySet() == null
				|| cache.entrySet().isEmpty());
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
		Assert.assertTrue(cache.entrySet() == null
				|| cache.entrySet().isEmpty());
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

	//@Test
	public void testOverflow() {
		for (int i = 0; i < 10; ++i) {
			cache.put(i, Integer.toString(i));
		}

		for (int j = 0; j < 2; ++j) {
			for (int i = 0; i < 5; ++i) {
				cache.get(i);
			}
		}

		for (int i = 5; i < 10; ++i) {
			cache.get(i);
		}

		// All entries should be reserved since the map hasn't exceeded its
		// capacity
		for (int i = 0; i < 10; ++i) {
			Assert.assertNotNull(cache.get(i));
		}

		for (int i = 10; i < 11; ++i) {
			cache.put(i, Integer.toString(i));
			// refreshing the last item, so it won't be deleted.
			cache.get(i);
			cache.get(i);
		}

		// 0 1 2 3 4 should be reserved since they are most frequently accessed
		for (int i = 0; i < 5; ++i) {
			Assert.assertNotNull(cache.get(i));
		}

		// 5 6 7 should be evicted since they are least recently and frequently
		// accessed
		for (int i = 5; i < 8; ++i) {
			Assert.assertNull(cache.get(i));
		}

		// 8 9 should be reserved since they are updated recently
		for (int i = 8; i < 10; ++i) {
			Assert.assertNotNull(cache.get(i));
		}

		// Newly added elements are reserved.
		for (int i = 10; i < 11; ++i) {
			Assert.assertNotNull(cache.get(i));
		}

		cache.dump();

	}

	//@Test
	public void testGetTopHits() {
		for (int i = 0; i < 10; ++i) {
			cache.put(i, Integer.toString(i));
		}

		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j <= i; ++j) {
				cache.get(i);
			}
		}

		List<Integer> topHits = cache.getTopHits(5);
		if(topHits != null) {
			for (int i = 0; i < 5; ++i) {
				Assert.assertEquals(Integer.valueOf(4 - i), topHits.get(i));
			}
		}

		cache.dump();

	}

	@Test
	public void testPerformance() {

		int maxSize = 10000;
		System.out.println("Problem size " + maxSize);
		cache = new LFUCache<Integer, String>(maxSize, 10 * 1000);

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
	
}
