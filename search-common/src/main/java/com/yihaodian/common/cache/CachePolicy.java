package com.yihaodian.common.cache;

public class CachePolicy {

    public enum CacheType {

        static_cache, dynamic_cache
    }

    public enum EvictionPolicy {

        LRU, LFU
    }
    int capacity;
    long expireTime;
    CacheType cacheType;
    EvictionPolicy evictionPolicy;

    public CachePolicy(int capacity, long expireTime, CacheType cacheType, EvictionPolicy evictionPolicy) {
        this.capacity = capacity;
        this.expireTime = expireTime;
        this.cacheType = cacheType;
        this.evictionPolicy = evictionPolicy;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getExpireTime() {
        return this.expireTime;
    }

    public void setCacheType(CacheType cacheType) {
        this.cacheType = cacheType;
    }

    public CacheType getCacheType() {
        return this.cacheType;
    }

    public void setEvictionPolicy(EvictionPolicy evictionPolicy) {
        this.evictionPolicy = evictionPolicy;
    }

    public EvictionPolicy getEvictionPolicy() {
        return this.evictionPolicy;
    }
}