package com.yihaodian.common.bdb;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BDBStorePerformanceTest {

	static String dbPath = "./var/www/data/test/bdbstore";
	static String dbName = "test";

	public static BDBStore<String, String> getStore(int type) {

		File dbdir = new File(dbPath);
		if (dbdir.exists() && dbdir.isDirectory()) {
			for (File file : dbdir.listFiles()) {
				file.delete();
			}
			dbdir.delete();
		}

		BDBStore<String, String> store = null;
		switch (type) {
		case 1:
			store = new MockStore(dbPath, dbName, false, 10);
			break;
		case 2:
			store = new MockCachedStore(dbPath, dbName, false, 10);
			break;
		case 3:
			store = new MockConCachedStore(dbPath, dbName, false, 10);
			break;
		case 4:
			store = new MockStore(dbPath, dbName, false, 80);
			break;
		}

		return store;
	}

	public static void main(String[] args) {

		
		for (int type = 1; type <= 4; ++type) {
			System.out.println("Type: " + type);
			BDBStore<String, String> store = getStore(type);
			test(store, 10000, 1000000, 24, 0, 1000000);
			store.close();

			store = getStore(type);
			test(store, 10000, 1000000, 0, 12, 1000000);
			store.close();

			store = getStore(type);
			test(store, 10000, 1000000, 24, 12, 1000000);
			store.close();
		}

		// Type: 1
		// reads: 1000023, writes: 0, time: 4783
		// reads: 0, writes: 1000011, time: 25853
		// reads: 1000023, writes: 45126, time: 2281
		// Type: 2
		// reads: 1000023, writes: 0, time: 1527
		// reads: 0, writes: 1000011, time: 28942
		// reads: 1000023, writes: 123127, time: 4739
		// Type: 3
		// reads: 1000023, writes: 0, time: 950
		// reads: 0, writes: 1000011, time: 28920
		// reads: 1000023, writes: 133681, time: 5971
		// Type: 4
		// reads: 1000023, writes: 0, time: 4223
		// reads: 0, writes: 1000011, time: 24400
		// reads: 1000023, writes: 36374, time: 1739


	}

	public static void test(final BDBStore<String, String> store,
			final int cacheSize, final int valueRange, final int readers,
			final int writers, final int limit) {

		final AtomicInteger reads = new AtomicInteger();
		final AtomicInteger writes = new AtomicInteger();

		class ReaderThread extends Thread {
			@Override
			public void run() {
				while (true) {
					int i = (int) (valueRange * Math.random());
					String str = Integer.toString(i);
					store.get(str);
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
					String str = Integer.toString(i);
					store.put(str, str);

					writes.incrementAndGet();

					if (writes.get() >= limit || reads.get() >= limit) {
						return;
					}
				}
			}
		}

		if (writers == 0) {
			for (int i = 0; i < valueRange; ++i) {
				String str = Integer.toString(i);
				store.put(str, str);
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

	}

}
