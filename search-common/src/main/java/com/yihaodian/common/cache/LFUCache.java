package com.yihaodian.common.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.yihaodian.common.cache.util.HitsMap;
import com.yihaodian.common.cache.util.UpdateMap;
import com.yihaodian.common.thread.NamedThreadFactory;

/**
 * Least Frequently Used cache replacement algorithm.
 * 
 * 
 * Optimization, Simplifications & Impact:
 * 
 * 1. Access will be updated in the background, and only in batch.
 * 
 * Pros: The overhead of updating access record will be minimized. Cons: The
 * frequency used to remove items from cache will not be up-to-date.
 * 
 * 2. Entries with the same hit number will be stored in Set (compared to
 * LinkedList sorted by access time).
 * 
 * Cons: when eldest entry is removed, the latest added items might be remove.
 * 
 * Since eldest entry will only be removed in batch, and hits will only be
 * updated in batch too, so the worst case is not likely to happen.
 * 
 * 3. Cache size is not strictly controlled.
 * 
 * Cache size will be controlled by a background thread, in batch.
 * 
 * 4. Use fair sync
 * 
 * Non fair sync will favor writer over reader. In cache cases, read is far more
 * frequent than writer, so we need fair sync.
 * 
 * 
 * @author zhouhang
 * 
 * @param <K>
 * @param <V>
 */
public class LFUCache<K, V> implements YCache<K, V> {
	private static Logger log = Logger.getLogger(LFUCache.class);

	private int capacity;
	private long expireTime;

	private ConcurrentMap<K, V> map;

	// hits frequency
	private Lock hitsLock = new ReentrantLock(true);
	private HitsMap<K> hitsMap;

	// Temporary access map to buffer access records and update them in batch
	private Map<K, Integer> tmpAccessMap;
	// Access lock only protect batch operation of tmpAccessMap, not add/update.
	private Lock accessLock = new ReentrantLock(true);

	private Lock updateLock = new ReentrantLock(true);
	private UpdateMap<K> updateMap;

	private final float tmpAccessRatio = 0.1f;
	private final float lowerWaterMarkRatio = 0.90f;
	private final float higherWaterMarkRatio = 0.95f;

	private int tmpAccessLimit;
	private int lowerWaterMarkLimit;
	private int higherWaterMarkLimit;

	private Evicator evicator;

	private ExecutorService accessRecorder;

	private ExecutorService capacityKeeper;

	private ExecutorService updateRecorder;

	private ExecutorService expireTrigger;

	private ExecutorService hitsKeeper;

	CacheStats stats = new CacheStats();

	private CacheExpireListener<K> expireListener;

	public LFUCache(int maxCapacity, long expireTime) {
		this.capacity = maxCapacity;
		this.expireTime = expireTime;
		// The default fill ration of ConcurrentMap is 0.75, so do the math.
		map = new ConcurrentHashMap<K, V>((int) Math.floor((double)maxCapacity * 4 / 3));
		hitsMap = new HitsMap<K>();

		tmpAccessMap = new HashMap<K, Integer>();

		updateMap = new UpdateMap<K>();

		tmpAccessLimit = (int) Math.floor(capacity * tmpAccessRatio);
		lowerWaterMarkLimit = (int) Math.floor(capacity * lowerWaterMarkRatio);
		higherWaterMarkLimit = (int) Math
				.floor(capacity * higherWaterMarkRatio);

		accessRecorder = new ThreadPoolExecutor(1, 1, 1000L,
				TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(5000,
						true), new NamedThreadFactory("LFU-accessRecorder"),
				new ThreadPoolExecutor.DiscardPolicy());

		capacityKeeper = new ThreadPoolExecutor(1, 1, 1000L,
				TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(5000,
						true), new NamedThreadFactory("LFU-capacityKeeper"),
				new ThreadPoolExecutor.CallerRunsPolicy());

		if (expireTime > 0) {
			updateRecorder = new ThreadPoolExecutor(1, 1, 1000L,
					TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
							5000, true), new NamedThreadFactory(
							"LFU-updateRecorder"),
					new ThreadPoolExecutor.DiscardPolicy());
			expireTrigger = new ThreadPoolExecutor(1, 1, 1000L,
					TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
							5000, true), new NamedThreadFactory(
							"LFU-expireTrigger"),
					new ThreadPoolExecutor.CallerRunsPolicy());
		}

		hitsKeeper = new ThreadPoolExecutor(1, 1, 1000L, TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<Runnable>(5000, true),
				new NamedThreadFactory("LFU-capacityKeeper"),
				new ThreadPoolExecutor.DiscardPolicy());

		start();
	}

	private void start() {
		if (expireTime > 0) {
			evicator = new Evicator();
			evicator.setDaemon(true);
			evicator.setName("LFU-Evicator");
			evicator.start();
		}
	}

	public List<K> getTopHits(int count) {
		List<K> list;
		count = Math.min(size(), count);

		try {
			hitsLock.lock();
			list = hitsMap.getHits(count, false);
		} finally {
			hitsLock.unlock();
		}

		return list;
	}

	private void removeAccessRecord(final K k) {
		hitsKeeper.submit(new Runnable() {
			@Override
			public void run() {
				_removeAccessRecord(k);
			}
		});
	}

	private void _removeAccessRecord(K k) {
		try {
			hitsLock.lock();
			hitsMap.remove(k);
		} finally {
			hitsLock.unlock();
		}
	}

	// Update access record in the background
	private void refreshAccessRecord() {

		if (tmpAccessMap.size() < tmpAccessLimit) {
			return;
		}	

		try {
			accessLock.lock();
			if (tmpAccessMap.size() >= tmpAccessLimit) {
				final Map<K, Integer> partHits = new HashMap<K, Integer>();
				partHits.putAll(tmpAccessMap);
				tmpAccessMap.clear();
				hitsKeeper.submit(new Runnable() {
					@Override
					public void run() {
						_refreshAccessRecord(partHits);
					}
				});
			}
		} finally {
			accessLock.unlock();
		}

	}

	// Update access record in batch
	private void _refreshAccessRecord(Map<K, Integer> partHits) {
		try {
			hitsLock.lock();
			if (!partHits.isEmpty()) {
				for (Entry<K, Integer> entry : partHits.entrySet()) {
					hitsMap.recordAccess(entry.getKey(), entry.getValue());
				}
				partHits.clear();
			}
		} finally {
			hitsLock.unlock();
		}
	}

	private void recordAccess(final K k) {
		accessRecorder.submit(new Runnable() {
			@Override
			public void run() {
				_recordAccess(k);
			}
		});
	}

	private void _recordAccess(final K k) {

		Integer accessNum = tmpAccessMap.get(k);
		if (accessNum == null) {
			accessNum = 0;
		}
		++accessNum;
		tmpAccessMap.put(k, accessNum);

		if (tmpAccessMap.size() > tmpAccessLimit) {
			refreshAccessRecord();
		}

	}

	@Override
	public V put(K k, V v) {
		ensureCapacity(true);
		return _put(k, v);
	}

	public V putIfAbsent(K k, V v) {

		ensureCapacity(true);

		V rv = map.putIfAbsent(k, v);

		// record update
		recordUpdate(k);
		// record access
		recordAccess(k);

		return rv;
	}

	private V _put(K k, V v) {

		V rv = map.put(k, v);

		// record update
		recordUpdate(k);
		// record access
		recordAccess(k);

		return rv;
	}

	private void recordUpdate(final K k) {
		if (expireTime > 0) {
			updateRecorder.submit(new Runnable() {
				public void run() {
					_recordUpdate(k, System.currentTimeMillis());
				}
			});

		}
	}

	private void _recordUpdate(final K k, long updateTime) {
		try {
			updateLock.lock();
			updateMap.recordUpdate(k, updateTime);
		} finally {
			updateLock.unlock();
		}
	}

	private void removeUpdate(final K k) {
		if (expireTime > 0) {
			updateRecorder.submit(new Runnable() {
				public void run() {
					_removeUpdate(k);
				}
			});

		}
	}

	private void _removeUpdate(K k) {
		try {
			updateLock.lock();
			updateMap.removeUpdate(k);
		} finally {
			updateLock.unlock();
		}
	}

	// water mark keeping
	public void ensureCapacity(boolean background) {

		// Water mark keeping
		if (map.size() >= higherWaterMarkLimit) {
			if (background) {
				capacityKeeper.submit(new Runnable() {
					@Override
					public void run() {
						_ensureCapacity();
					}
				});
			} else {
				_ensureCapacity();
			}
		}
	}

	// water mark keeping
	private void _ensureCapacity() {
		if (map.size() < higherWaterMarkLimit) {
			return;
		}
		_removeEldestEntry(map.size() - lowerWaterMarkLimit);

	}

	@Override
	public V get(K k) {
		stats.incAccess(1);

		V rv = map.get(k);
		if (rv != null) {
			// record access
			recordAccess(k);
			stats.incHit(1);
		}
		return rv;
	}

	@Override
	public V remove(K k) {
		return _remove(k);
	}

	private V _remove(K k) {
		V v = map.remove(k);

		removeAccessRecord(k);

		removeUpdate(k);

		return v;
	}

	@Override
	public boolean containsKey(K k) {
		return map.containsKey(k);
	}

	@Override
	public void clear() {
		map.clear();
		hitsMap.clear();

		updateMap.clear();

		stats.clear();
	}

	public void close() {
		clear();
		if (this.evicator != null) {
			evicator.interrupt();
		}
		if (this.accessRecorder != null) {
			this.accessRecorder.shutdown();
		}
		if (this.capacityKeeper != null) {
			this.capacityKeeper.shutdown();
		}
		if (this.expireTrigger != null) {
			this.expireTrigger.shutdown();
		}
		if (this.hitsKeeper != null) {
			this.hitsKeeper.shutdown();
		}
		if (this.updateRecorder != null) {
			this.updateRecorder.shutdown();
		}
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
			_put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return map.entrySet();
	}

	@Override
	public Set<K> keySet() {
		return map.keySet();
	}

	public void dump() {
		_dump();
		_dumpInternal();
	}

	private void _dumpInternal() {
		StringBuilder builder = new StringBuilder();
		builder.append(">>> Internal map: ").append(map);
		System.out.println(builder.toString());
	}

	private void _dump() {
		StringBuilder builder = new StringBuilder();
		builder.append("LRUCache: ").append("size: ").append(size())
				.append(", lowerWaterMarkRatio: ").append(lowerWaterMarkRatio)
				.append(", max capacity: ").append(capacity)
				.append(", expire time: ").append(expireTime);

		if (!isEmpty()) {
			if (expireTime > 0) {
				try {
					updateLock.lock();
					builder.append(", update map: [").append(
							updateMap.toString());
				} finally {
					updateLock.unlock();
				}
			}

			try {
				hitsLock.lock();
				builder.append("], hitsMap: [").append(hitsMap.toString())
						.append("]");
			} finally {
				hitsLock.unlock();
			}

			HashMap<K, Integer> tamSnap = null;

			try {
				accessLock.lock();
				if (!tmpAccessMap.isEmpty()) {
					tamSnap = new HashMap<K, Integer>(tmpAccessMap);
				}

			} finally {
				accessLock.unlock();
			}
			if (tamSnap != null && !tamSnap.isEmpty()) {
				builder.append(", tmpAccessMap: ").append(tamSnap);
			}
		}
		System.out.println(builder.toString());
	}

	private void _removeEldestEntry(final int count) {
		if (map.isEmpty())
			return;

		List<K> eldest = null;
		try {
			hitsLock.lock();
			eldest = hitsMap.getHits(count, true);
		} finally {
			hitsLock.unlock();
		}

		if (eldest == null) {
			eldest = new ArrayList<K>();
		}

		int removed = 0;

		if (eldest != null) {
			for (K k : eldest) {
				V v = _remove(k);
				if (v == null) {
					throw new RuntimeException("Failed to remove: "
							+ k.toString());
				}
				removed++;
			}
		}

		// remove from the map brutely.
		List<K> keys = new ArrayList<K>(map.keySet()).subList(0, count
				- removed);
		for (K k : keys) {
			V v = _remove(k);
			if (v == null) {
				throw new RuntimeException("Failed to remove: " + k.toString());
			}
		}

	}

	public void clearExpiredEntry() {
		_clearExpiredEntry();
	}

	private void _clearExpiredEntry() {
		if (map.isEmpty())
			return;
		long now = System.currentTimeMillis();

		List<K> list = null;
		try {
			updateLock.lock();
			list = updateMap.getBefore(now - expireTime);
		} finally {
			updateLock.unlock();
		}

		if (list != null) {
			for (final K k : list) {
				_remove(k);
				// Notify listener
				if (expireListener != null) {
					expireTrigger.submit(new Runnable() {
						@Override
						public void run() {
							expireListener.onExpire(k);
						}
					});
				}

			}
		}

	}

	public CacheStats cacheStats() {
		return stats;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public int capacity() {
		return capacity;
	}

	public long expireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	/**
	 * Evicator thread that kicks out expired entries
	 * 
	 * @author zhouhang
	 * 
	 */
	private class Evicator extends Thread {
		private static final int MIN_SLEEP_TIME = 5 * 1000;

		@Override
		public void run() {
			while (true) {
				try {
					clearExpiredEntry();
					// Calculate time to sleep
					long tts = expireTime;

					try {
						updateLock.lock();
						tts = updateMap.getOldestTime() + expireTime
								- System.currentTimeMillis();
					} finally {
						updateLock.unlock();
					}

					if (tts < MIN_SLEEP_TIME) {
						tts = MIN_SLEEP_TIME;
					}

					if (tts > expireTime) {
						tts = expireTime;
					}

					Thread.sleep(tts);
				} catch (InterruptedException e) {
					log.warn("Thread is interrupted, exiting ...", e);
					break;
				} catch (Exception e) {
					log.error("Got exception: ", e);
				}
			}
		}

	}

	@Override
	public void setExpireListener(CacheExpireListener<K> listener) {
		this.expireListener = listener;
	}

}
