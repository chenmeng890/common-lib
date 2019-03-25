package com.yihaodian.common.cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.naming.OperationNotSupportedException;

import org.apache.log4j.Logger;

import com.yihaodian.common.cache.util.ListEntry;
import com.yihaodian.common.cache.util.MapEntry;


/**
 * LRUCache Map which will record update and access of elements,
 * it'll clean up the least recently accessed/updated elements to maintain 
 * list size, and it'll clean up expired elements to maintain freshness.
 * 
 * @author zhouhang
 *
 * @param <K>
 * @param <V>
 */
public class LRUCache<K, V> implements YCache<K, V> {
	private static Logger log = Logger.getLogger(LRUCache.class);
	
	private int capacity;
	private long expireTime;
	
	private Map<K, LRUEntry<K, V>> map;
	
	// lock for the map
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();
    
	private ListEntry<LRUEntry<K, V>> accessHead;
	private ListEntry<LRUEntry<K, V>> updateHead;
	
	// lock for access record
    private final Lock accessLock = new ReentrantLock();
    
    CacheStats stats = new CacheStats();
    
	private Evicator evicator;
	
	private CacheExpireListener<K> expireListener;
	
	public LRUCache(int maxCapacity, long expireTime) {
		this.capacity = maxCapacity;
		this.expireTime = expireTime;
				
		map = new HashMap<K, LRUEntry<K, V>>(maxCapacity);
		
		start();
	}
	
	public void start(){
		if(expireTime > 0) {
			evicator = new Evicator();
			evicator.setDaemon(true);
			evicator.setName("LRUCacheEvicatorThread");
			evicator.start();
		}
	}

    protected void recordUpdate(LRUEntry<K, V> entry){
		entry._recordUpdate(this);		
    }
    
    protected void recordAccess(LRUEntry<K, V> entry){
		entry.recordAccess(this);
    }

	@Override
	public V put(K k, V v) {
		V rv = null;
		try{
			writeLock.lock();
			rv = _put(k, v);
		} finally{
			writeLock.unlock();
		}
		return rv;
		
	}
	
	private V _put(K k, V v) {
		// Clean up if the map is full. 
		while(size() >= capacity){
			this._removeEldestEntry();
		}

		LRUEntry<K, V> entry = map.get(k);
		if(entry == null) {
			entry = new LRUEntry<K, V>(k, v);
		}

		// record access
		recordUpdate(entry);		

		map.put(k, entry);
		return v;
	}

	@Override
	public V get(K k) {
		V rv = null;
		try{
			readLock.lock();

			stats.incAccess(1);
			LRUEntry<K, V> entry = map.get(k);
			if(entry != null){
				// record access
				recordAccess(entry);
				rv = entry.getMapEntry().getValue();
				stats.incHit(1);
			}
		}finally{
			readLock.unlock();
		}
		return rv;
	}

	@Override
	public V remove(K k) {
		V rv = null;
		try{
			writeLock.lock();

			rv = _remove(k);
		} finally{
			writeLock.unlock();
		}
		return rv;
	}
	
	private V _remove(K k){
		LRUEntry<K, V> entry = map.get(k);
		if(entry == null) {
			return null;
		}
		// remove the data
		map.remove(k);
		
		// record access
		entry._remove(this);
		return entry.getMapEntry().getValue();
	}

	@Override
	public boolean containsKey(K k) {
		boolean rb = false;
		try{
			readLock.lock();
			rb = map.containsKey(k);
		}finally{
			readLock.unlock();
		}
		return rb;
	}

	@Override
	public void clear() {
		try{
			writeLock.lock();
			// remove references
			for(LRUEntry<K, V> entry: map.values()){
				entry._remove(this);
			}

			map.clear();
			accessHead = null;
			updateHead = null;

			stats.clear();
		} finally{
			writeLock.unlock();
		}
	}

	public void close(){
		clear();
		if(this.evicator != null) {
			evicator.interrupt();
		}
	}
	
	
	@Override
	public int size() {
		int size = 0;
		try{
			readLock.lock();
			size = map.size();
		}finally{
			readLock.unlock();
		}
		return size;
	}
	

	@Override
	public boolean isEmpty() {
		boolean isEmpty = true;
		try{
			readLock.lock();
			isEmpty = map.isEmpty();
		}finally{
			readLock.unlock();
		}
		return isEmpty;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		try{
			writeLock.lock();
			for(Entry<? extends K, ? extends V> entry: map.entrySet() ){
				_put(entry.getKey(), entry.getValue());
			}
		} finally{
			writeLock.unlock();
		}
		
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		Set<Entry<K, V>> set = null;
		try{
			readLock.lock();
			set = new HashSet<Entry<K, V>>();
			for(LRUEntry<K, V> entry: map.values()){
				set.add(entry.getMapEntry());
			}
		}finally{
			readLock.unlock();
		}
		return set;
	}

	@Override
	public Set<K> keySet() {
		Set<K> ks = null;
		try{
			readLock.lock();
			ks =new HashSet<K>(map.keySet());
		}finally{
			readLock.unlock();
		}
		return ks;
	}
	
	public void dump(){
		StringBuilder builder = new StringBuilder();
		builder.append("LRUCache: ").append("size: ").append(size())
		.append(", max capacity: ").append(capacity)
		.append(", expire time: ").append(expireTime);
		
		
		if(!isEmpty()){
			builder.append(", access order: [");
			ListEntry<LRUEntry<K, V>> accessCursor = accessHead;
			while(true){
				builder.append(accessCursor.getObject().getMapEntry().getKey()).append(", ");
				accessCursor = accessCursor.getAfter();
				if(accessCursor  == accessHead){
					break;
				}
			}
			
			builder.append("], update order: [");
			ListEntry<LRUEntry<K, V>> updateCursor = updateHead;
			while(true){
				builder.append(updateCursor.getObject().getMapEntry().getKey()).append(", ");
				updateCursor = updateCursor.getAfter();
				if(updateCursor  == updateHead){
					break;
				}
			}
			builder.append("].");
		}
		System.out.println(builder.toString());
	}

	private void _removeEldestEntry(){
		if(map.isEmpty()) {
			return;
		}
		
		ListEntry<LRUEntry<K, V>> eldestAcess = accessHead.getBefore();
		LRUEntry<K, V> entry = (LRUEntry<K, V>)eldestAcess.getObject();
		
		_remove(entry.getMapEntry().getKey());		
	}
	
	public void clearExpiredEntry(){
		try{
			writeLock.lock();
			_clearExpiredEntry();
		}finally{
			writeLock.unlock();
		}

	}

	private void _clearExpiredEntry(){
		if(map.isEmpty())
			return;
		ListEntry<LRUEntry<K, V>> eldestUpdate = updateHead.getBefore();
		long now = System.currentTimeMillis();
		while(true){
			long lastUpdateTime = ((LRUEntry<K, V>)eldestUpdate.getObject()).getUpdateTime();
			if(lastUpdateTime + expireTime <= now) {
				K expireKey = ((LRUEntry<K, V>)eldestUpdate.getObject()).getMapEntry().getKey();
				_remove(expireKey);
				if(expireListener != null) {
					expireListener.onExpire(expireKey);
				}
				if(map.isEmpty()) {
					break;
				} else{
					eldestUpdate = updateHead.getBefore();
				}				
			} else {
				break;
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
        return this.capacity;
    }

    public long expireTime() {
        return this.expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }
	
	/**
	 * Evicator thread that kicks out expired entries
	 * @author zhouhang
	 *
	 */
	private class Evicator extends Thread{
		private static final int MIN_SLEEP_TIME = 5 * 1000; 
		
		@Override
		public void run(){
			while(true) {
				try{					
					clearExpiredEntry();
					//Calculate time to sleep
					long tts = expireTime;
					if(!isEmpty()) {
						try{
							readLock.lock();						
							tts = ((LRUEntry<K, V>) updateHead.getBefore().getObject()).getUpdateTime() + expireTime - System.currentTimeMillis();							
						}catch(Exception e) {
							log.error("Got exception: ", e);	
						} finally{
							readLock.unlock();
						}
					} 
					
					if(tts < MIN_SLEEP_TIME) {
						tts = MIN_SLEEP_TIME;
					}
					
					Thread.sleep(tts);
				} catch (InterruptedException e){
					log.warn("Thread is interrupted, exiting ...", e);
					break;
				} catch(Exception e) {
					log.error("Got exception: ", e);						
				}
			}
		}
			
	}
		
	/**
	 * LRUEntry to record access and update
	 * @author zhouhang
	 *
	 * @param <K>
	 * @param <V>
	 */
	private static class LRUEntry<K, V> {
		private MapEntry<K, V> mapEntry;
		private long updateTime;
		private long accessTime;
		private ListEntry<LRUEntry<K, V>> access;
		private ListEntry<LRUEntry<K, V>> update;

		public LRUEntry(K k, V v){
			mapEntry = new MapEntry<K, V>(k, v);	    		
			access = new ListEntry<LRUEntry<K, V>>(this);
			update = new ListEntry<LRUEntry<K, V>>(this);
			updateTime = System.currentTimeMillis();
			accessTime = System.currentTimeMillis();
		}	
		
		public void _remove(LRUCache<K, V> cache) {	
			if(cache.updateHead == update){
				if(update.getAfter() == update){
					cache.updateHead = null;
				} else {
					cache.updateHead = update.getAfter();
				}
			}
			update.remove();
			
			if(cache.accessHead == access){
				if(access.getAfter() == access){
					cache.accessHead = null;
				} else {
					cache.accessHead = access.getAfter();
				}
			}
			access.remove();			
		}

		public void _recordAccess(LRUCache<K, V> cache){
			accessTime = System.currentTimeMillis();
			if(this.access != cache.accessHead) {
				access.remove();
				access.addBefore(cache.accessHead);
				accessTime = System.currentTimeMillis();
				cache.accessHead = this.access;
			}
		}
		
		// lock protected
		public void recordAccess(LRUCache<K, V> cache){
			try{
				cache.accessLock.lock();
				_recordAccess(cache);
			} finally{
				cache.accessLock.unlock();
			}
		}
		
		public void _recordUpdate(LRUCache<K, V> cache){
			if(this.update != cache.updateHead) {
				update.remove();
				update.addBefore(cache.updateHead);
				updateTime = System.currentTimeMillis();
				cache.updateHead = this.update;
			}
			// update is also an access.
			_recordAccess(cache);
		}
		
		public MapEntry<K, V> getMapEntry() {
			return mapEntry;
		}

		public long getUpdateTime() {
			return updateTime;
		}
		
		@Override
		public String toString(){
			StringBuilder builder = new StringBuilder();
			builder.append("LRUEntry (")
			.append(" key: ").append(mapEntry.getKey())
			.append(", value: ").append(mapEntry.getValue())
			.append(", update time: ").append(updateTime)
			.append(", access time: ").append(accessTime).append(")");
			return builder.toString();
		}
	}

	@Override
	public void setExpireListener(CacheExpireListener<K> listener) {
		this.expireListener = listener;
	}
	
	@Override
	public V putIfAbsent(K k, V v) throws Exception {
		throw new OperationNotSupportedException("putIfAbsent");
	}

	@Override
	public List<K> getTopHits(int count) throws Exception {
		throw new OperationNotSupportedException("getTopHits");	
	}
	
	

}
