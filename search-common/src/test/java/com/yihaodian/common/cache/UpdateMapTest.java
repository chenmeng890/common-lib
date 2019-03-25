package com.yihaodian.common.cache;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import com.yihaodian.common.cache.util.UpdateMap;

public class UpdateMapTest {

	@Test
	public void testAdd() {
		UpdateMap<Integer> map = new UpdateMap<Integer>();
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i <= 10; ++i) {
			map.recordUpdate(i, i);
			list.add(i);
		}

		Assert.assertEquals(list, map.getBefore(10));

		// map.dump();

	}

	@Test
	public void testUpdate() {
		UpdateMap<Integer> map = new UpdateMap<Integer>();
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i <= 10; ++i) {
			map.recordUpdate(i, i);
			list.add(i);
		}

		Assert.assertEquals(list, map.getBefore(10));
		// map.dump();

		for (int i = 1; i <= 5; ++i) {
			map.recordUpdate(i, 10 + i);
			list.remove((Integer) i);
		}
		Assert.assertEquals(list, map.getBefore(10));
		// map.dump();

	}

	@Test
	public void testRemove() {
		UpdateMap<Integer> map = new UpdateMap<Integer>();
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 1; i <= 10; ++i) {
			map.recordUpdate(i, i);
			list.add(i);
		}

		Assert.assertEquals(list, map.getBefore(10));
		// map.dump();

		for (int i = 1; i <= 5; ++i) {
			map.removeUpdate(i);
			list.remove((Integer) i);
		}
		Assert.assertEquals(list, map.getBefore(10));
		// map.dump();

	}
}
