/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yihaodian.common.cache;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

/**
 *
 */
public class YCacheManager implements YCacheManagerMBean {

    YCache cache;
    String name;

    public YCacheManager(String name, YCache cache) {
        this.name = name;
        this.cache = cache;

        StandardMBean mbean;
        try {
            mbean = new StandardMBean(this,
                    YCacheManagerMBean.class);
            if (mbean != null) {
                MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
                mbeanServer.registerMBean(mbean, new ObjectName(
                         "com.yihaodian:service="+name));

            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    public boolean isOpenCache() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void openCache() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void closeCache() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void clearCache() {
        cache.clear();
    }

    public void updateCacheSize(int size) {
        cache.setCapacity(size);
    }

    public int getCacheSize() {
        return cache.size();
    }

    public int getMaxCacheSize() {
        return cache.capacity();
    }

    public String getCacheMaxLifeTime() {
        //in min
        return "" + cache.expireTime() / TimeUnit.MINUTES.toMillis(1) +" minute";
    }

    public void updateCacheMaxLifeTime(long lifetime) {
        cache.setExpireTime(lifetime);
    }

    public long getCacheHit() {
        CacheStats stats = cache.cacheStats();
        return stats.hitCount();
    }

    public long getCacheTotalAccess() {
        CacheStats stats = cache.cacheStats();
        return stats.totalAccess();
    }

    public String getCacheRatio() {
        CacheStats stats = cache.cacheStats();
        return "" + stats.hitRatio();
    }
}
