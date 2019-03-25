package com.yihaodian.common.cache;

/**
 * Listener to get notified when cache entry expires.
 * 
 * @author zhouhang
 *
 * @param <K>
 */
public interface CacheExpireListener<K> {
	/**
	 * Notify expire event
	 * 
	 * The process logic need be async to avoid blocking cache operations.
	 * @param k
	 */
	public void onExpire(K k);
}
