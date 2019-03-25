package com.yihaodian.store.tracker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.yihaodian.common.thread.NamedThreadFactory;
import com.yihaodian.store.hbasestore.HBaseRecord;
import com.yihaodian.store.hbasestore.HBaseStore;
import com.yihaodian.store.hbasestore.HBaseStoreManager;

public class SearchTracker {
	
	private static Logger log = Logger.getLogger(SearchTracker.class);

	private static final ExecutorService trackerExecutor = new ThreadPoolExecutor(8, 16, 60,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000), new NamedThreadFactory(
					"search-tracker-executor"));
	
	static String tableName = "SearchFlowTracker";
	static String separtor = "_";
	static byte[] family = "f".getBytes();
	static byte[] qualifier = "q".getBytes();
	static HBaseStore store;
	static {
		try {
			store = HBaseStoreManager.getInstance().getStore(tableName);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * write tracker values to HBase
	 * key定义：requestId+"_"+type[mandy,broker,shard]
	 */
	public static void commitTracker(final String trackerId, final TrackerType type, final String trackValue) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				String rowKey = trackerId + separtor + type;
				byte[] value = trackValue.getBytes();
				HBaseRecord record = new HBaseRecord(rowKey, value, System.currentTimeMillis());
				try {
					store.put(record);
				} catch (Exception e) {
					log.error("", e);
				}
			}
		};
		trackerExecutor.submit(runnable);
	}

	public static enum TrackerType {
		Mandy,
		Broker,
		Shard
	}
	
}
