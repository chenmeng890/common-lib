package com.yihaodian.common.bdb;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Iterator;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class BDBStoreTest {
	//MockStore store;
	MockConCachedStore store;
	String dbPath = "./var/www/data/test/bdbstore";
	String dbName = "test";
	
	@Before
	public void setup(){
		store = new MockConCachedStore(dbPath, dbName, false, 10);
		//store = new MockStore(dbPath, dbName, false, 10);
		
	}
	
	@After
	public void cleanup(){
		store.close();
		File dbdir = new File(dbPath);
		for(File file: dbdir.listFiles()) {
			file.delete();
		}
		dbdir.delete();
	}
	

	@Test
	public void testBDBStore() {

		File dbdir = new File(dbPath);
		Assert.assertTrue(dbdir.exists());
		
	}

	@Test
	public void testPut() {
		String k = "1";
		String v = "one";
		
		Assert.assertEquals(null, store.put(k, v));

		Assert.assertEquals(1, store.keySet().size());
		Assert.assertEquals(v, store.get(k));
		System.out.println(store.getCacheStats());
	}

	@Test
	public void testGet() {
		String k = "1";
		String v = "one";
		store.put(k, v);
		Assert.assertEquals(v, store.get(k));
		
		k = "2";
		v = "two";	
		store.put(k, v);
		Assert.assertEquals(v, store.get(k));
		
	}

	@Test
	public void testContainsKey() {
		String k = "1";
		String v = "one";
		store.put(k, v);	
		Assert.assertTrue(store.containsKey(k));
		
		k = "2";
		Assert.assertFalse(store.containsKey(k));
	}

	@Test
	public void testRemove() {
		String k = "1";
		String v = "one";
		store.put(k, v);	
		Assert.assertTrue(store.containsKey(k));
		store.remove(k);
		Assert.assertFalse(store.containsKey(k));
	}

	@Test
	public void testKeySet() {
		String k = "1";
		String v = "one";
		store.put(k, v);	
		Set<String> keys = store.keySet();
		Assert.assertEquals(1, keys.size());
		Iterator<String> iter = keys.iterator();
		Assert.assertEquals(k, iter.next());
	}

	@Test
	public void testVerify() {
		String k = "1";
		String v = "one";
		store.put(k, v);	
		Assert.assertEquals(true, store.verify());
		
		k = "2";
		v = "two";
		store.put(k, v);	
		Assert.assertEquals(true, store.verify());
		
		store.remove(k);
		Assert.assertEquals(true, store.verify());

	}

	@Test
	public void testReadVerifyThorough() {
		String k = "1";
		String v = "one";
		store.put(k, v);	
		
		k = "2";
		v = "two";
		store.put(k, v);	
		Assert.assertEquals(true, store.readVerifyThorough(2));
	}

	@Test
	public void testPrintStats() {
		String k = "1";
		String v = "one";
		store.put(k, v);	
		
		k = "2";
		v = "two";
		store.put(k, v);		
	
		//store.printStats();
	
	}
	
	@Test
	public void testClose() {
		// It's ok to close twice.
		store.close();
	}

}
