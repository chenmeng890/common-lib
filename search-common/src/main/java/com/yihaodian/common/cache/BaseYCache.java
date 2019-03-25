package com.yihaodian.common.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.naming.OperationNotSupportedException;

import org.apache.log4j.Logger;

/**
 *
 * @author zengfenghua
 *
 * @param <K>
 * @param <V>
 */
public class BaseYCache<K, V> implements YCache<K, V> {

    private static Logger log = Logger.getLogger(BaseYCache.class);
    protected Map<K, BaseYCache.CacheObject<V>> map = new HashMap<K, BaseYCache.CacheObject<V>>(200);
    private int capacity;
    protected long expireTime;
    CacheStats stats = new CacheStats();
    protected LinkedList<YCacheNode<K>> lifetimeList = new LinkedList<YCacheNode<K>>();
    private String name = BaseYCache.class.getSimpleName();

	private CacheExpireListener<K> expireListener;
    
    public BaseYCache(int maxSize, long maxLifetime) {
        this.capacity = maxSize;
        this.expireTime = maxLifetime;
        this.start();
    }

    public BaseYCache(String name, int maxSize, long maxLifetime) {
        this.name = name;
        this.capacity = maxSize;
        this.expireTime = maxLifetime;
        this.start();
    }

    @Override
    public synchronized V put(K key, V value) {
        if (key == null || value == null) {
            return null;
        }
        if (map.size() >= capacity && !map.containsKey(key)) {
            return null;
        }
        long timestamp = System.currentTimeMillis();
        BaseYCache.CacheObject<V> cacheObject = new BaseYCache.CacheObject<V>(value, timestamp);
        BaseYCache.CacheObject<V> answer = map.put(key, cacheObject);
        YCacheNode<K> node = new YCacheNode<K>(key, timestamp);
        this.lifetimeList.addFirst(node);
        return answer == null ? null : answer.object;
    }

    @Override
    public synchronized V get(K key) {
        if (key == null) {
            return null;
        }

        stats.incAccess(1);
        BaseYCache.CacheObject<V> cacheObject = map.get(key);
        if (cacheObject == null) {
//			cacheMisses++;
            return null;
        }
        stats.incHit(1);
        return cacheObject.object;
    }

    @Override
    public synchronized V remove(K key) {
        if (key == null) {
            return null;
        }
        BaseYCache.CacheObject<V> obj = map.remove(key);
        return obj == null ? null : obj.object;
    }

    @Override
    public synchronized void clear() {
        map.clear();
        stats.clear();
        lifetimeList.clear();
    }

    @Override
    public synchronized int size() {
        return map.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public synchronized boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Override
    public synchronized void putAll(Map<? extends K, ? extends V> map) {
        if (map == null) {
            return;
        }
        
        for(Entry<? extends K, ? extends V> entry: map.entrySet() ){
			put(entry.getKey(), entry.getValue());
		}
    }


    @Override
    public synchronized Set<Entry<K, V>> entrySet() {
    	final Map<K, V> result = new HashMap<K, V>();
        for (final Entry<K, BaseYCache.CacheObject<V>> entry : map.entrySet()) {
            result.put(entry.getKey(), entry.getValue().object);
        }
        return result.entrySet();   
    }

    @Override
    public synchronized Set<K> keySet() {
        return new HashSet<K>(map.keySet());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public synchronized void deleteExpiredEntries() {
        if (expireTime <= 0 || lifetimeList.size() <= 0) {
            return;
        }
        long current = System.currentTimeMillis();
        YCacheNode<K> node = lifetimeList.getLast();
        CacheObject<V> obj = null;
        while (node != null && current - node.timestamp >= expireTime) {
            lifetimeList.removeLast();
            obj = getCacheObject(node.key);
            if (obj != null && obj.timeStamp == node.timestamp) {
                remove(node.key);
                // notify listener
                if(expireListener != null) {
                	expireListener.onExpire(node.key);
                }
            }
            if (lifetimeList.size() > 0) {
                node = lifetimeList.getLast();
            } else {
                node = null;
            }
        }

    }

    private synchronized final CacheObject<V> getCacheObject(K k) {
        return map.get(k);
    }

    public void start() {
        CacheThread cacheThread = new CacheThread();
        cacheThread.setName("CacheThread");
        cacheThread.setDaemon(true);
        cacheThread.start();
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

    private class CacheThread extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(30 * 1000);
                    deleteExpiredEntries();
                } catch (InterruptedException e) {
                    log.error("CacheThead InterruptedException", e);
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e1) {
                        log.error("CacheThead InterruptedException", e1);
                        break;
                    }
                }
            }

        }
    }

    private static class CacheObject<V> {

        public V object;
        public long timeStamp;

        public CacheObject(V object, long timeStamp) {
            this.object = object;
            this.timeStamp = timeStamp;
        }
    }

    private static class YCacheNode<K> {

        K key;
        long timestamp;

        public YCacheNode(K key, long timestamp) {
            this.key = key;
            this.timestamp = timestamp;
        }
    }

	@Override
	public void setExpireListener(CacheExpireListener<K> listener) {
		this.expireListener = listener;
	}
	
	@Override
	public V putIfAbsent(K k, V v) throws Exception {
		// FIXME:
		return put(k, v);

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<K> getTopHits(int count) throws Exception {
		throw new OperationNotSupportedException("getTopHits");
	}
}
