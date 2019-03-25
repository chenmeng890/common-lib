package com.yihaodian.common.bdb;

import java.util.Set;

/**
 * BDB Operation interface
 * 
 * @author zhouhang
 *
 * @param <K>
 * @param <V>
 */
public interface BDBOperation<K, V> {

	/**
	 * Store new/updated data
	 * @param k
	 * @param v
	 * @return
	 */
	public V put(K k, V v);
	
	/**
	 * Retrieve the data
	 * @param k
	 * @return
	 */
	public V get(K k);
	
	/**
	 * Check whether contains key
	 * @param k
	 * @return
	 */
	public boolean containsKey(K k);
	
	/**
	 * Remove the data
	 * @param k
	 * @return
	 */
	public boolean remove(K k);

	/**
	 * Get key set
	 * @return
	 */
	public Set<K> keySet();
	
	/**
	 * Verify the integrity of the DB
	 * @return
	 */
	public boolean verify();

	/**
	 * Verify by access all the data concurrently
	 * @param numThread: thread to access the data
	 * @return
	 */
	public boolean readVerifyThorough(int numThread);

	public void printStats();

	public void close();
	
	/**
	 * Item number in BDB store
	 * @return
	 */
	public long size();	

}
