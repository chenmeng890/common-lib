package com.yihaodian.common.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CachePerformanceTest {

	public static void main(String[] args) {

		test(1000000, 100000000, 240, 0, 100000000);
		// no-lock reads: 1000023, writes: 0, time: 1459
		// non-fair: reads: 1000023, writes: 0, time: 1174
		// Fair: reads: 1000023, writes: 0, time: 1113
		// Conc: reads: 1000023, writes: 0, time: 871
		// no-lock conc: reads: 1000023, writes: 0, time: 905

		test(1000000, 100000000, 0, 120, 100000000);
		// no-lock: reads: 0, writes: 1000011, time: 2625
		// non-fair: reads: 0, writes: 1000011, time: 8598ss
		// Fair: reads: 0, writes: 1000011, time: 18134
		// Conc: reads: 0, writes: 1000011, time: 5417
		// no-lock reads: 0, writes: 1000010, time: 867

		test(1000000, 100000000, 240, 120, 100000000);
		// no-lock reads: 1000023, writes: 317035, time: 1936
		// non-fair: reads: 250092, writes: 1000011, time: 9617
		// Fair: reads: 1000023, writes: 495571, time: 16809
		// Conc: reads: 1000023, writes: 467145, time: 4466
		// no-lock conc: reads: 1000023, writes: 96405, time: 668

		// test(10000, 12500, 200, 0, 10000000);

		// reader = 240, writer = 120, Conc LFU
		// reads: 100000239, writes: 0, time: 41493
		// hit=100000239, access=100000239, ratio=1.0
		// reads: 0, writes: 100000119, time: 43265
		// hit=0, access=0, ratio=0.0
		// reads: 100000239, writes: 40500708, time: 56903
		// hit=82617917, access=100000239, ratio=0.826

		// reader = 240, writer = 120, LFU
		// reads: 100000237, writes: 0, time: 40819
		// hit=8237454, access=100000237, ratio=0.082
		// reads: 0, writes: 100000119, time: 538439
		// hit=0, access=0, ratio=0.0
		// reads: 100000239, writes: 4338140, time: 583749
		// hit=14637944, access=100000239, ratio=0.146


		// reader = 240, writer = 120, ConcurrentHashMap
		// reads: 100000231, writes: 0, time: 31531
		// reads: 0, writes: 100000118, time: 33616
		// reads: 100000237, writes: 10521827, time: 36470
		try {
			TimeUnit.SECONDS.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void test(final int cacheSize, final int valueRange,
			final int readers, final int writers, final int limit) {
		 final LFUCache<Integer, String> cache = new LFUCache<Integer,
		 String>(
		 cacheSize, 5 * 1000);
		// final ConcurrentLFUCache<Integer, String> cache = new
		// ConcurrentLFUCache<Integer, String> (8, cacheSize, 0);
		// final Map<Integer, String> cache = new HashMap<Integer,
		// String>(cacheSize);

//		final Map<Integer, String> cache = new ConcurrentHashMap<Integer, String>(
//				cacheSize);

		// final LRUCache<Integer, String> cache = new LRUCache<Integer,
		// String>(
		// cacheSize, 5 * 1000);

		final AtomicInteger reads = new AtomicInteger();
		final AtomicInteger writes = new AtomicInteger();

		class ReaderThread extends Thread {
			@Override
			public void run() {
				while (true) {
					int i = (int) (valueRange * Math.random());
					cache.get(i);
					reads.getAndIncrement();

					if (writes.get() >= limit || reads.get() >= limit) {
						return;
					}
				}
			}
		}

		class WriterThread extends Thread {
			@Override
			public void run() {
				while (true) {
					int i = (int) (valueRange * Math.random());
					cache.put(i, Integer.toString(i));

					writes.incrementAndGet();

					if (writes.get() >= limit || reads.get() >= limit) {
						return;
					}
				}
			}
		}

		if (writers == 0) {
			for (int i = 0; i < valueRange; ++i) {
				cache.put(i, Integer.toString(i));
			}
		}

		List<Thread> ts = new ArrayList<Thread>();

		for (int i = 0; i < readers; ++i) {
			ReaderThread reader = new ReaderThread();
			reader.setName("reader");
			ts.add(reader);
		}

		for (int i = 0; i < writers; ++i) {
			WriterThread writer = new WriterThread();
			writer.setName("Writer");
			ts.add(writer);
		}

		long s = System.currentTimeMillis();
		for (Thread t : ts) {
			t.start();
		}
		for (Thread t : ts) {
			try {
				t.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		long e = System.currentTimeMillis();

		System.out.println("reads: " + reads.get() + ", writes: "
				+ writes.get() + ", time: " + (e - s));

		cache.clear();
		System.gc();
		try {
			TimeUnit.SECONDS.sleep(30);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// System.out.println(cache.cacheStats());
	}

}
