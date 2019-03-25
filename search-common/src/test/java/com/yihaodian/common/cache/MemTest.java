package com.yihaodian.common.cache;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class MemTest {

	public static class BigData {

		String payload = new String("");
		public BigData(int i) {
			for(int j = 0; j < i; ++j) {
				payload = payload + Integer.toString(i % 10);
			}
		}
	}

	public static void main(String[] args) {
		int cacheSize = 128;
		int valueRange = 12800;

		final LFUCache<Integer, BigData> cache = new LFUCache<Integer, BigData>(
				cacheSize, 0 * 1000);
		for (int i = 0; i < valueRange; ++i) {
			cache.put(i, new BigData(i));
		}

		for (int i = 0; i < valueRange; ++i) {
			cache.get(i);
		}

		for (int i = 0; i < valueRange; ++i) {
			cache.put(valueRange+i, new BigData(valueRange+i));
		}

		try {
			TimeUnit.SECONDS.sleep(10);
			System.out.println("Read to capture");

			TimeUnit.SECONDS.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
