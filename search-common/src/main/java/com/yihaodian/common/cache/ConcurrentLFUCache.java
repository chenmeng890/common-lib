package com.yihaodian.common.cache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.yihaodian.common.util.MathUtil;

/**
 * Concurrent LFUCache to avoid lock contention.
 * 
 * @author zhouhang
 *
 */
public class ConcurrentLFUCache<K, V> implements YCache<K, V>  {
	
	private List<LFUCache<K, V>> segments;	
	private int level;
	private int mask;
	
	
	/**
	 * Ctor
	 * @param level : concurrent level
	 * @param maxCapacity
	 * @param expireTime
	 */
	public ConcurrentLFUCache(int level, int maxCapacity, long expireTime) {
		
		this.level = MathUtil.nextPowOf2(level);		
		mask = level - 1;
		
		int subCapacity = (int)Math.ceil((double)maxCapacity / this.level); 
		segments = new ArrayList<LFUCache<K, V>>(level);
		for(int i = 0; i < this.level; ++ i) {
			LFUCache<K, V> seg = new LFUCache<K, V>(subCapacity, expireTime); 
			segments.add(seg);
		}				
	}
	
	@Override
	public V putIfAbsent(K k, V v) {
		int idx = k.hashCode() & mask;
		return segments.get(idx).putIfAbsent(k, v);
	}
	
	@Override
	public V put(K k, V v) {
		int idx = k.hashCode() & mask;
		return segments.get(idx).put(k, v);		
	}
	@Override
	public V get(K k) {
		int idx = k.hashCode() & mask;
		return segments.get(idx).get(k);
	}
	
	@Override
	public V remove(K k) {
		int idx = k.hashCode() & mask;		
		return segments.get(idx).remove(k);
	}
	@Override
	public boolean containsKey(K k) {		
		int idx = k.hashCode() & mask;		
		return segments.get(idx).containsKey(k);
	}
	@Override
	public void clear() {
		for(LFUCache<K, V> seg : segments) {
			seg.clear();
		}	
		
	}
	@Override
	public int size() {
		int size = 0;
		for(LFUCache<K, V> seg : segments) {
			size += seg.size();
		}
		
		return size;
	}
	@Override
	public boolean isEmpty() {
		boolean empty = true;
		for(LFUCache<K, V> seg : segments) {
			if(!seg.isEmpty()) {
				empty = false;
			}
		}		
		return empty;
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for(Entry<? extends K, ? extends V> entry:  map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}		
	}
	
	@Override
	public Set<Entry<K, V>> entrySet() {
		Set<Entry<K, V>> set = new HashSet<Entry<K, V>>();
		for(LFUCache<K, V> seg : segments) {
			Set<Entry<K, V>> subSet = seg.entrySet();
			if(subSet != null) {
				set.addAll(subSet);
			}
		}
		
		return set;
	}
	@Override
	public Set<K> keySet() {
		Set<K> set = new HashSet<K>();
		for(LFUCache<K, V> seg : segments) {
			Set<K> subSet = seg.keySet();
			if(subSet != null) {
				set.addAll(subSet);
			}
		}
		
		return set;
	}
	
	@Override
	public CacheStats cacheStats() {
		CacheStats stats = new CacheStats();
		for(LFUCache<K, V> seg : segments) { 
			CacheStats sub = seg.cacheStats();
			stats.incAccess((int) sub.totalAccess());
			stats.incHit((int) sub.hitCount());
			stats.incAccessTimeout((int)sub.totalAccessTimeoutCount());
		}
		
		return stats;
	}
	@Override
	public void setCapacity(int capacity) {
		int subCapacity = (int)Math.ceil((double)capacity / level); 

		for(LFUCache<K, V> seg : segments) {
			seg.setCapacity(subCapacity);
		}		
	}
	@Override
	public int capacity() {
		return segments.get(0).capacity() * level;
	}
	@Override
	public long expireTime() {
		return segments.get(0).expireTime();
	}
	@Override
	public void setExpireTime(long expireTime) {
		for(LFUCache<K, V> seg : segments) {
			seg.setExpireTime(expireTime);
		}
		
	}
	@Override
	public void setExpireListener(CacheExpireListener<K> listener) {
		for(LFUCache<K, V> seg : segments) {
			seg.setExpireListener(listener);
		}
		
	}

	public List<K> getTopHits(int warmCount) {
		List<K> list = new ArrayList<K>();
		
		int subCount = (int)Math.floor((double)warmCount / level);
		for(LFUCache<K, V> seg : segments) {
			List<K> sub = seg.getTopHits(subCount);
			if(sub != null) {
				list.addAll(sub);
			}
		}
		
		return list;
	}
	
	public void close() {
		for(LFUCache<K, V> seg : segments) {
			seg.close();
		}
	}

	public void ensureCapacity() {
		for(LFUCache<K, V> seg : segments) {
			seg.ensureCapacity(false);
		}
		
		
	}
	
}
