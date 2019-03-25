package com.yihaodian.common.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * 
 * @author zengfenghua
 *
 * @param <K>
 * @param <V>
 */
public interface YCache<K, V> {

	public V put(K k, V v);

	public V get(K k);

	public V remove(K k);

	public boolean containsKey(K k);

	public void clear();

	public int size();

	public boolean isEmpty();
	
	public void  putAll(Map<? extends K, ? extends V> map);
	
	public Set<Entry<K, V>> entrySet();
	
	public Set<K> keySet();
        
	public CacheStats cacheStats();

	public void setCapacity(int capacity);

	public int capacity();

	public long expireTime();

	public void setExpireTime(long expireTime);
	
	public void setExpireListener(CacheExpireListener<K> listener);

	public V putIfAbsent(K k, V v) throws Exception;

	public void close();

	public List<K> getTopHits(int count) throws Exception;
}
