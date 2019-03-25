package com.yihaodian.common.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.yihaodian.common.cache.util.HitsMap;

public class HitsMapTest {

	@Test
	public void testAccess() {
		List<Integer> data = new ArrayList<Integer>(); 
		
		HitsMap<Integer> hm = new HitsMap<Integer>();
		for(int i = 1; i < 10; ++ i) {
			hm.recordAccess(i, i);
			data.add(i);
		}
		List<Integer> list = hm.getHits(9, true);
		Assert.assertEquals(data, list);
		
		//System.out.println(hm.toString());
		
		for(int i = 1; i < 5; ++ i) {
			hm.recordAccess(i, i);
		}
		
		List<Integer> data2 = Arrays.asList(1, 2, 5, 6, 3, 7, 8, 4, 9);
		List<Integer> list2 = hm.getHits(9, true);
		Assert.assertEquals(data2, list2);	
		
	}
	
	@Test
	public void testRemove() {
	List<Integer> data = new ArrayList<Integer>(); 
		
		HitsMap<Integer> hm = new HitsMap<Integer>();
		for(int i = 1; i < 10; ++ i) {
			hm.recordAccess(i, i);
			data.add(i);
		}
		List<Integer> list = hm.getHits(9, true);
		Assert.assertEquals(data, list);
		
		hm.remove(5);
		data.remove(new Integer(5));
		List<Integer> list2 = hm.getHits(9, true);
		Assert.assertEquals(data, list2);
	}
	
	@Test
	public void testRemoveAdd() {
	List<Integer> data = new ArrayList<Integer>(); 
		
		HitsMap<Integer> hm = new HitsMap<Integer>();
		for(int i = 1; i < 10; ++ i) {
			hm.recordAccess(i, i);
			data.add(i);
		}
		List<Integer> list = hm.getHits(9, true);
		Assert.assertEquals(data, list);
		
		hm.remove(5);
		data.remove(new Integer(5));
		List<Integer> list2 = hm.getHits(9, true);
		Assert.assertEquals(data, list2);
		
		hm.recordAccess(20, 5);
		data.add(4, 20);
		List<Integer> list3 = hm.getHits(9, true);
		Assert.assertEquals(data, list3);
	}

}
