package com.yihaodian.common.cache;

import com.yihaodian.common.cache.CachePolicy.CacheType;
import com.yihaodian.common.cache.CachePolicy.EvictionPolicy;

public class CacheBuilder {
    public static <K, V> YCache<K, V> buildCache(CachePolicy policy) {

        CacheType cacheType = policy.getCacheType();
        long expireTime = policy.getExpireTime();
        int capacity = policy.getCapacity();

        YCache<K, V> cache;
        if (cacheType == CacheType.static_cache) {
            cache = new BaseYCache<K, V>(capacity, expireTime);
        } else {
            EvictionPolicy evictionPolicy = policy.getEvictionPolicy();
            if (evictionPolicy == EvictionPolicy.LFU) {
                cache = new LFUCache<K, V>(capacity, expireTime);
            } else {
                cache = new LRUCache<K, V>(capacity, expireTime);
            }
        }

        return cache;
    }
}