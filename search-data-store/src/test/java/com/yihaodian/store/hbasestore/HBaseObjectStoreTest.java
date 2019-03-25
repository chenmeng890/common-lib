package com.yihaodian.store.hbasestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

public class HBaseObjectStoreTest {

	@Test
	public void testCtor() throws Exception {
		// create
		String name = "HBaseObjectStoreTest-testCtor";
		HBaseObjectStore<String> store = new HBaseObjectStore<String>(
				String.class, name, true, null, false);

		// test
		String obj = "test";
		store.put(obj, obj);

		String ret = store.get(obj);
		Assert.assertEquals(obj, ret);

		// delete
		HBaseStoreManager.getInstance().removeStore(name);
	}

	@Test
	public void testCtorWithStoreName() throws Exception {
		String name = "HBaseObjectStoreTest";
		HBaseObjectStore<Integer> store = new HBaseObjectStore<Integer>(
				Integer.class, name, true, null, false);
		Integer obj = 12306;
		store.put(obj.toString(), obj);

		Integer ret = store.get(obj.toString());
		Assert.assertEquals(obj, ret);

		// delete
		HBaseStoreManager.getInstance().removeStore(name);
	}

	@Test
	public void testPut() throws Exception {
		String className = Integer.class.getCanonicalName();
		String name = className + "-testPut";
		HBaseObjectStore<Integer> store = new HBaseObjectStore<Integer>(
				Integer.class, name, true, null, false);
		Integer obj = 12307;
		String key = "12306";
		store.put(key, obj);

		Integer ret = store.get(key);
		Assert.assertEquals(obj, ret);

		HBaseStoreManager.getInstance().removeStore(name);

	}

	@Test
	public void testPutWithTs() throws Exception {
		String className = Integer.class.getCanonicalName();
		String name = className + "-testPutWithTs";

		HBaseObjectStore<Integer> store = new HBaseObjectStore<Integer>(
				Integer.class, name, true, null, false);

		String key = "12306";
		// Store the first
		Integer obj = 12308;
		Long ts = System.currentTimeMillis();
		store.put(key, obj, ts);
		Integer ret = store.get(key);
		Assert.assertEquals(obj, ret);

		// Store another
		obj = 12309;
		ts = System.currentTimeMillis();
		store.put(key, obj, ts);

		ret = store.get(key);
		Assert.assertEquals(obj, ret);

		// Store the same value
		obj = 12309;
		ts = System.currentTimeMillis();
		store.put(key, obj, ts);

		ret = store.get(key);
		Assert.assertEquals(obj, ret);

		// Store outdated value
		obj = 12310;
		ts = 1L;
		store.put(key, obj, ts);

		ret = store.get(key);
		Assert.assertFalse(obj.equals(ret));

		HBaseStoreManager.getInstance().removeStore(name);
	}

	@Test
	public void testGet() throws Exception {
		String className = Integer.class.getCanonicalName();
		String name = className + "-testGet";
		HBaseObjectStore<Integer> store = new HBaseObjectStore<Integer>(
				Integer.class, name, true, null, false);
		Integer obj = 12306;
		String key = "12306";
		store.put(key, obj);

		Integer ret = store.get(key);
		Assert.assertEquals(obj, ret);

		HBaseStoreManager.getInstance().removeStore(name);
	}

	@Test
	public void testGetWithTs() throws Exception {
		String className = Integer.class.getCanonicalName();
		String name = className + "-testGetWithTs";
		HBaseObjectStore<Integer> store = new HBaseObjectStore<Integer>(
				Integer.class, name, true, null, false);

		// Save version 1
		Integer obj = 12306;
		String key = "12306";
		Long ts1 = System.currentTimeMillis();
		store.put(key, obj, ts1);

		Integer ret = store.get(key);
		Assert.assertEquals(obj, ret);

		// Save version 2
		Integer obj2 = 12307;
		Long ts2 = System.currentTimeMillis() + 1;
		store.put(key, obj2, ts2);

		ret = store.get(key);
		Assert.assertEquals(obj2, ret);

		// Get older version
		Long ts = ts1;
		ret = store.get(key, ts);
		Assert.assertEquals(obj, ret);

		HBaseStoreManager.getInstance().removeStore(name);

	}

	//@Test
	public void testRemoveBetween() throws Exception {
		String className = Integer.class.getCanonicalName();
		String name = className + "-testRemoveBetween";
		HBaseObjectStore<Integer> store = new HBaseObjectStore<Integer>(
				Integer.class, name, true, null, true);

		long startT = System.currentTimeMillis();
		int n = new Random().nextInt(10) + 1;
		for (int i = 0; i < n; i++) {
			store.put(System.currentTimeMillis() + "_" + i, i, System.currentTimeMillis());
			store.flush();
		}
		long endT = System.currentTimeMillis();

		List<String> keys = store.getKeys(startT, endT);
		Assert.assertEquals(keys.size(), n);

		store.removeBetweenTimestamp(startT, endT);
		
		keys = store.getKeys(startT, endT);
		Assert.assertEquals(keys.size(), 0);
		
		HBaseStoreManager.getInstance().removeStore(name);
	}

	@Test
	public void testRemove() throws Exception {
		String className = Integer.class.getCanonicalName();
		String name = className + "-testRemove";
		HBaseObjectStore<Integer> store = new HBaseObjectStore<Integer>(
				Integer.class, name, true, null, false);

		// Save version 1
		Integer obj = 12306;
		String key = "12306";
		Long ts1 = System.currentTimeMillis();
		store.put(key, obj, ts1);

		Integer ret = store.get(key);
		Assert.assertEquals(obj, ret);

		// Save version 2
		Integer obj2 = 12307;
		Long ts2 = System.currentTimeMillis() + 1;
		store.put(key, obj2, ts2);

		ret = store.get(key);
		Assert.assertEquals(obj2, ret);

		// Remove
		store.remove(key);

		ret = store.get(key);
		Assert.assertNull(ret);

		// Get older version		
		ret = store.get(key, ts1);
		Assert.assertNull(ret);
		HBaseStoreManager.getInstance().removeStore(name);
	}

	@Test
	public void testVersions() throws Exception {
		String className = Integer.class.getCanonicalName();
		String name = className + "-testVersions";
		try{
			HBaseObjectStore<Integer> store = new HBaseObjectStore<Integer>(
					Integer.class, name, true, null, false);

			// Save version 1
			Integer obj = 12306;
			String key = "12306";
			Long ts1 = System.currentTimeMillis();
			store.put(key, obj, ts1);

			List<Long> versions = store.getVersions(key);
			Assert.assertEquals(1, versions.size());
			Assert.assertTrue(versions.contains(ts1));

			// Save version 2
			obj = 12307;
			Long ts2 = System.currentTimeMillis() + 1;
			store.put(key, obj, ts2);

			versions = store.getVersions(key);
			Assert.assertEquals(2, versions.size());
			Assert.assertTrue(versions.contains(ts2));

			versions = store.getVersions(key, 0L, ts1);
			Assert.assertEquals(1, versions.size());
			Assert.assertTrue(versions.contains(ts1));

			versions = store.getVersions(key, ts1, ts2);
			Assert.assertEquals(2, versions.size());
			Assert.assertTrue(versions.contains(ts1));
			Assert.assertTrue(versions.contains(ts2));
		} finally{
			HBaseStoreManager.getInstance().removeStore(name, false);
		}
	}

	@Test
	public void testClose() throws Exception {
		String className = Integer.class.getCanonicalName();
		String name = className + "-testClose";
		HBaseObjectStore<Integer> store = new HBaseObjectStore<Integer>(
				Integer.class, name, true, null, false);

		// Save version 1
		Integer obj = 12306;
		String key = "12306";
		Long ts = 100 * 300 * 1000L;
		store.put(key, obj, ts);

		HBaseStoreManager.getInstance().removeStore(name);
	}

	@Test
	public void testGetKeys() throws Exception {
		String className = String.class.getCanonicalName();
		String name = className + "-testGetKeys";
		HBaseObjectStore<String> store = new HBaseObjectStore<String>(
				String.class, name, true, null, false);

		// Save at ts 1
		String obj = "12306";
		String key = "12306";
		Long ts = System.currentTimeMillis();
		store.put(key, obj, ts);

		String ret = store.get(key);
		Assert.assertEquals(obj, ret);

		// Save at ts 2
		String obj2 = "12307";
		String key2 = "12307";
		long ts2 = System.currentTimeMillis() + 1;
		store.put(key2, obj2, ts2);

		ret = store.get(key2);
		Assert.assertEquals(obj2, ret);

		// Get keys
		List<String> keys = store.getKeys();
		Assert.assertEquals(2, keys.size());
		Assert.assertTrue(keys.contains(key));
		Assert.assertTrue(keys.contains(key2));

		List<String> objs = store.get(keys);
		Assert.assertEquals(2, objs.size());
		Assert.assertTrue(objs.contains(obj));
		Assert.assertTrue(objs.contains(obj2));

		// Get keys with time range
		keys = store.getKeys(ts2, Long.MAX_VALUE);
		Assert.assertEquals(1, keys.size());
		Assert.assertFalse(keys.contains(key));
		Assert.assertTrue(keys.contains(key2));

		objs = store.get(keys);
		Assert.assertEquals(1, objs.size());
		Assert.assertFalse(objs.contains(obj));
		Assert.assertTrue(objs.contains(obj2));

		HBaseStoreManager.getInstance().removeStore(name);
	}

	@Test
	public void testFields() throws Exception {
		String className = String.class.getCanonicalName();
		String name = className + "-testFields";
		HBaseObjectStore<String> store = new HBaseObjectStore<String>(
				String.class, name, true, null, false);

		// Save at ts 1
		String obj = "12306";
		String key = "12306";
		Long ts = System.currentTimeMillis();
		store.put(key, obj, ts);

		String ret = store.get(key);
		Assert.assertEquals(obj, ret);

		// Save at ts 2
		String obj2 = "12307";
		String key2 = "12307";
		long ts2 = System.currentTimeMillis() + 1;
		store.put(key2, obj2, ts2);

		ret = store.get(key2);
		Assert.assertEquals(obj2, ret);


		// Get keys
		List<String> keys = store.getKeys();
		Assert.assertEquals(2, keys.size());
		Assert.assertTrue(keys.contains(key));
		Assert.assertTrue(keys.contains(key2));

		List<String> objs = store.get(keys);
		Assert.assertEquals(2, objs.size());
		Assert.assertTrue(objs.contains(obj));
		Assert.assertTrue(objs.contains(obj2));

		// Get keys with time range
		keys = store.getKeys(ts2, Long.MAX_VALUE);
		Assert.assertEquals(1, keys.size());
		Assert.assertFalse(keys.contains(key));
		Assert.assertTrue(keys.contains(key2));

		objs = store.get(keys);
		Assert.assertEquals(1, objs.size());
		Assert.assertFalse(objs.contains(obj));
		Assert.assertTrue(objs.contains(obj2));

		// Get keys with time range and fields
		keys = store.getKeys(ts, ts2, null);
		Assert.assertEquals(2, keys.size());
		Assert.assertTrue(keys.contains(key));
		Assert.assertTrue(keys.contains(key2));

		HBaseStoreManager.getInstance().removeStore(name);
	}

	@Test
	public void testFieldsVersion() throws Exception {
		String className = String.class.getCanonicalName();
		String name = className + "-random-testFieldsVersion";
		try{
			HBaseObjectStore<String> store = new HBaseObjectStore<String>(
					String.class, name, true, null, false);

			// Save at ts 1
			String obj = "12306";
			String key = "12306";
			Long ts = System.currentTimeMillis();
			store.put(key, obj, ts);

			String ret = store.get(key);
			Assert.assertEquals(obj, ret);

			// Save at ts 2
			String obj2 = "12307";
			long ts2 = System.currentTimeMillis() + 1;
			store.put(key, obj2, ts2);

			ret = store.get(key);
			Assert.assertEquals(obj2, ret);
			
			// Get keys with time range and fields
			List<String> keys = store.getKeys(ts, ts2, null);
			Assert.assertEquals(1, keys.size());
			Assert.assertTrue(keys.contains(key));

			keys = store.getKeys(ts, ts);
			Assert.assertEquals(1, keys.size());
			Assert.assertTrue(keys.contains(key));

			keys = store.getKeys(ts2, ts2);
			Assert.assertEquals(1, keys.size());

			ret = store.get(key, ts);
			Assert.assertEquals(obj, ret);

			ret = store.get(key, ts2);
			Assert.assertEquals(obj2, ret);
		} finally{
			HBaseStoreManager.getInstance().removeStore(name,false);
		}
	}
	
	public static void main(String[] args) throws Exception
	{
		HBaseObjectStoreTest test = new HBaseObjectStoreTest();
		test.testRemoveBetween();
	}
}
