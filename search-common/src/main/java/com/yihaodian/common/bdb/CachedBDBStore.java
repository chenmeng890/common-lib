package com.yihaodian.common.bdb;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Logger;

import com.yihaodian.common.cache.CacheStats;
import com.yihaodian.common.cache.ConcurrentLFUCache;
import com.yihaodian.common.cache.LFUCache;
import com.yihaodian.common.cache.YCache;
import com.yihaodian.common.thread.NamedThreadFactory;

public abstract class CachedBDBStore<K, V> extends BDBStore<K, V> {

	private static final int OBJECT_SIZE = 3000;
	private static Logger log = Logger.getLogger(CachedBDBStore.class);
	private boolean enableCache = false;
	private YCache<K, FutureTask<V>> cache;
	private ExecutorService bdbAcessor;
	private static final int DEFAUL_CONC_LEVEL = 8;
	private static final int BDB_CONC_LEVEL = 16;

	public CachedBDBStore(String dbPath, String dbName,
			boolean isReadOnly, int maxMemoryPercent) {
		this(dbPath, dbName, isReadOnly, maxMemoryPercent, false,
				DEFAUL_CONC_LEVEL);
	}

	public CachedBDBStore(String dbPath, String dbName,
			boolean isReadOnly, int maxMemoryPercent, boolean enableCache) {
		this(dbPath, dbName, isReadOnly, maxMemoryPercent, enableCache,
				DEFAUL_CONC_LEVEL);
	}

	public CachedBDBStore(String dbPath, String dbName,
			boolean isReadOnly, int maxMemoryPercent, boolean enableCache,
			int concurrentLevel) {
		super(dbPath, dbName, isReadOnly, !enableCache ? maxMemoryPercent
				: ((maxMemoryPercent > 10) ? 5 : (int) Math
						.ceil((double) maxMemoryPercent / 2)));
		this.enableCache = enableCache;
		if (enableCache) {
			int CachePercent = (maxMemoryPercent > 10) ? maxMemoryPercent - 5
					: (int) Math.floor((double) maxMemoryPercent / 2);
			int maxCacacity = (int) Math.floor((Runtime.getRuntime()
					.maxMemory() / 100 / OBJECT_SIZE * CachePercent));
			
			if(concurrentLevel < 1) {
				concurrentLevel = 1;
			}
			// Expiration is not required
			switch (concurrentLevel) {			
			case 1:
				cache = new LFUCache<K, FutureTask<V>>(maxCacacity, 0);
				break;
			default:
				cache = new ConcurrentLFUCache<K, FutureTask<V>>(
						concurrentLevel, maxCacacity, 0);
				break;
			}
			
			log.info("Cache is enabled, maxCacacity: " + maxCacacity + ", concurrentLevel: " + concurrentLevel);
		}
		this.bdbAcessor = Executors.newFixedThreadPool(BDB_CONC_LEVEL,
				new NamedThreadFactory("BDB-Accessor"));
	}

	public void warm(CachedBDBStore<K, V> oldStore, int warmCount) {
		if (oldStore == null) {
			return;
		}
		if (enableCache) {

			try {
				List<K> topHits = oldStore.cache.getTopHits(warmCount);
				for (K k : topHits) {
					get(k);
				}
			} catch (Exception e) {
				log.error("failed to warm", e);
			}

		}
	}

	private FutureTask<V> newPut(final V v) {
		return new FutureTask<V>(new Callable<V>() {
			@Override
			public V call() {
				return v;
			}
		});
	}

	private FutureTask<V> newGet(final K k) {
		return new FutureTask<V>(new Callable<V>() {
			@Override
			public V call() {
				return _getInternal(k);
			}
		});
	}

	private V _getInternal(K k) {
		return super.get(k);
	}

	@Override
	public V put(K k, V v) {
		FutureTask<V> ft = newPut(v);
		FutureTask<V> rv = null;
		if (enableCache) {
			rv = cache.put(k, ft);
			ft.run();
		}
		super.put(k, v);
		try {
			return ((rv == null) ? null : rv.get());
		} catch (Exception e) {
			log.error("failed to put " + k + ", " + v, e);
			return null;
		}
	}

	@Override
	public V get(K k) {
		try {
			FutureTask<V> rv = null;
			if (enableCache) {
				rv = cache.get(k);
				if (rv == null) {
					FutureTask<V> v = newGet(k);
					rv = cache.putIfAbsent(k, v);
					if (rv == null) {
						//bdbAcessor.submit(v);
						v.run();
						rv = v;
					}
				}
			} else {
				return super.get(k);
			}

			return rv.get();
		} catch (Exception e) {
			log.error("failed to get " + k, e);
			return null;
		}
	}

	@Override
	public boolean containsKey(K k) {
		if (enableCache) {
			if (cache.containsKey(k)) {
				return true;
			}
		}
		return super.containsKey(k);
	}

	@Override
	public boolean remove(K k) {
		if (enableCache) {
			cache.remove(k);
		}
		return super.remove(k);
	}

	@Override
	public void printStats() {
		if (enableCache) {
			System.out.println("Cache stats: " + cache.cacheStats().toString());
		}
		super.printStats();
	}

	@Override
	public void close() {
		if (enableCache) {
			cache.close();
		}
		super.close();
	}

	public CacheStats getCacheStats() {
		if (enableCache && this.cache != null) {
			return this.cache.cacheStats();
		}
		return null;
	}

	public long getCacheSize() {
		long size = 0L;
		if (enableCache && this.cache != null) {
			size = cache.size();
		}
		return size;
	}
}
