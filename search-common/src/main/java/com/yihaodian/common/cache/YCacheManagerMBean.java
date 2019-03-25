package com.yihaodian.common.cache;

/**
 * 
 * @author zengfenghua
 *
 */
public interface YCacheManagerMBean {
    
	public boolean isOpenCache();
	public void openCache();
	public void closeCache();
	public void clearCache();
	public void updateCacheSize(int size);
	public int getCacheSize();
	public int getMaxCacheSize();
	//显示多少分钟
	public String getCacheMaxLifeTime();
	public void updateCacheMaxLifeTime(long lifetime);
        
	public long getCacheHit();
	public long getCacheTotalAccess();
	public String getCacheRatio();
}
