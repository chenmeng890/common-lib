package com.yihaodian.common.cache.util;

import java.util.Map;

/**
 * Simple MapEntry
 * 
 * @author zhouhang
 *
 * @param <K>
 * @param <V>
 */
public class MapEntry<K, V> implements Map.Entry<K, V>{

	private K k;
	private V v;
	
	public MapEntry(K k, V v){
		this.k = k;
		this.v = v;
	}
	
	@Override
	public K getKey() {
		
		return k;
	}

	@Override
	public V getValue() {
		
		return v;
	}

	@Override
	public V setValue(V v) {
		this.v = v;
		return v;
	}
	
	@Override
	public String toString(){
		return "MapEntry, K: " + k + ", V: " + v; 
	}
	
}