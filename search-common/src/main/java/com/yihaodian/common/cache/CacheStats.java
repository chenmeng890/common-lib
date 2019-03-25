package com.yihaodian.common.cache;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;

public class CacheStats {

    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong accessCount = new AtomicLong();
    private final AtomicLong accessTimeoutCount = new AtomicLong();

    public void clear()
    {
        hitCount.set(0);
        accessCount.set(0);
        accessTimeoutCount.set(0);
    }
    
    public void incHit(int hit) {
        hitCount.addAndGet(hit);
    }

    public void incAccess(int access) {
        accessCount.addAndGet(access);
    }
    
    public void incAccessTimeout(int access) {
    	accessTimeoutCount.addAndGet(access);
    }
   
    public long hitCount() {
        return this.hitCount.get();
    }

    public long totalAccess() {
        return this.accessCount.get();
    }
    
    public long totalAccessTimeoutCount(){
    	return this.accessTimeoutCount.get();
    }

    public double hitRatio() {
       // double rate = this.hitCount.get() * 1.0 / (this.accessCount.get() + 1);
    	if(this.accessCount.get()==0) {
    		return 0.0;
    	}
		BigDecimal totalBigDecimal=new BigDecimal(this.accessCount.get());
		BigDecimal cacheHitDecimal=new BigDecimal(this.hitCount.get()); 
		double hitRatio=cacheHitDecimal.divide(totalBigDecimal,3,BigDecimal.ROUND_HALF_UP).doubleValue();
		return hitRatio;
    }
      
    public String toString()
    {
		return "hit=" + hitCount() + ", access=" + totalAccess() + ", ratio=" + hitRatio()
				+ ", timeout=" + totalAccessTimeoutCount();
    }
    
}