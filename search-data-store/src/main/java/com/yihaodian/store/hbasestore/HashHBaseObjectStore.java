package com.yihaodian.store.hbasestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.hbase.client.ResultScanner;

/**
 * 
 * This class is suitable for those with a timestamp as key prefix of those cases, 
 * the aim is to disperse the pressure of the hot region
 * 
 * @author zengfenghua
 *
 * @param <T>
 */
public class HashHBaseObjectStore<T>{
	
	HBaseObjectStore<T> hbaseObjectStore;
	
	public static final int OLD_REMAINDER=5;
	public static final int CURRENT_REMAINDER=10;
	private int remainder = OLD_REMAINDER;
	
	public enum HashType{
		Old_type,  // 线上5台Region Server
		Current_type  // 线上10台Region Server
	}
	
	ExecutorService executorService = new ThreadPoolExecutor(20,100,60l,TimeUnit.SECONDS,
			 new LinkedBlockingQueue(500));
	
	public HashHBaseObjectStore(Class<T> clazz){
		this(clazz,true);
	}
	
	public HashHBaseObjectStore(Class<T> clazz,boolean addEnv){
		this(clazz, clazz.getSimpleName(), addEnv);
	}
	
	public HashHBaseObjectStore(Class<T> clazz, String storeName){
		this(clazz, storeName, true);	
	}
	
	public HashHBaseObjectStore(Class<T> clazz, String storeName, HashType hashType){
		this(clazz, storeName, false, 
				null, true, null, hashType);	
	}
	
	public HashHBaseObjectStore(Class<T> clazz, String storeName, boolean addEnv, HashType hashType){
		this(clazz, storeName, false, 
				null, addEnv, null, hashType);
	}
	
	public HashHBaseObjectStore(Class<T> clazz, String storeName,boolean addEnv){
		this(clazz, storeName,null, addEnv);
	}
	
	public HashHBaseObjectStore(Class<T> clazz, String storeName, String[] familys){
		this(clazz, storeName, false, familys, true);
	}
	
	public HashHBaseObjectStore(Class<T> clazz, String storeName, String[] familys,boolean addEnv){
		this(clazz, storeName, false, familys, addEnv);
	}
	
	public HashHBaseObjectStore(Class<T> clazz, String storeName, boolean truncate, 
			String[] familys, boolean addEnv){
		this(clazz, storeName, truncate, familys, addEnv, null);
	}
	
	public HashHBaseObjectStore(Class<T> clazz, String storeName, boolean truncate, 
			String[] familys, boolean addEnv, String[] coprocessors){
		this(new HBaseObjectStore<T>(clazz, storeName, truncate, familys, addEnv, coprocessors));
	}
	
	public HashHBaseObjectStore(Class<T> clazz, String storeName, boolean truncate, 
			String[] familys, boolean addEnv, String[] coprocessors, HashType hashType){
		this(new HBaseObjectStore<T>(clazz, storeName, truncate, familys, addEnv, coprocessors), hashType);
	}
	
	public HashHBaseObjectStore(Class<T> clazz, HBaseStore store) {
		this(new HBaseObjectStore<T>(clazz, store));
	}
	
	public HashHBaseObjectStore(HBaseObjectStore<T> hbaseObjectStore) {
		this(hbaseObjectStore, HashType.Old_type);
	}
	
	public HashHBaseObjectStore(HBaseObjectStore<T> hbaseObjectStore, HashType hashType) {
		this.hbaseObjectStore=hbaseObjectStore;
		if (HashType.Current_type.equals(hashType)){
			remainder = CURRENT_REMAINDER;
		}
	}

	private String hashKey(String key){
		if(key.hashCode()==Integer.MIN_VALUE){
			return "0_"+key;
		}else{
			return (Math.abs(key.hashCode())%remainder)+"_"+key;
		}
		
	}
	
	protected static String originalKey(String key){
		if (key == null) {
			throw new IllegalArgumentException("HashKey can not be null");
		}
		
		int start = key.indexOf("_");
		return key.substring(start + 1);
	}
	
	public T put(String key, T t) throws Exception{	
		return hbaseObjectStore.put(hashKey(key), t);
	}
	
	public T put(String key, T t, long timestamp) throws Exception{	
		return hbaseObjectStore.put(hashKey(key), t, timestamp);
	}
	
	public T put(String key, T t, Map<byte[], Map<byte[], byte[]>> qualifiers) throws Exception{
		return hbaseObjectStore.put(hashKey(key), t, qualifiers);
	}
	
	public T put(String key, T t, long timestamp, Map<byte[], Map<byte[], byte[]>> qualifiers) throws Exception{
		return hbaseObjectStore.put(hashKey(key), t, timestamp, qualifiers);
	}
	
	public T put(T t, String key, long timestamp, Map<String, String> fields) throws Exception{
		Map<byte[], Map<byte[], byte[]>> qualifiers = new HashMap<byte[], Map<byte[], byte[]>>();
		Map<byte[], byte[]> qualifier = new HashMap<byte[], byte[]>();
		for (Entry<String, String> entry : fields.entrySet()) {
			if (entry.getValue() == null) {
				throw new IllegalArgumentException("field:" + entry.getKey() + " value is null!");
			}
			qualifier.put(entry.getKey().getBytes(), entry.getValue().getBytes());
		}
		qualifiers.put(HBaseRecord.FAMILY_NAME_DATA_BYTES, qualifier);
		return hbaseObjectStore.put(hashKey(key), t, timestamp, qualifiers);
	}
	
	public Map<String, T> multiPut(Map<String, T> data, Map<String, Long> ts, long defaultTs) throws Exception{
		Map<String, T> hashData = new HashMap<String, T>();
		Map<String, Long> hashTs = new HashMap<String, Long>();
		
		for (Entry<String, T> entry : data.entrySet()) {
			String key = entry.getKey();
			String hashKey = hashKey(key);
			hashData.put(hashKey, entry.getValue());
			hashTs.put(hashKey, ts.get(key));
		}
		return hbaseObjectStore.multiPut(hashData, hashTs, defaultTs);
	}
	
	public Map<String, T> multiPut(Map<String, T> data, Map<String, Long> ts, long defaultTs, 
			Map<String, Map<String, String>> fieldsMap) throws Exception{
		Map<String, T> hashData = new HashMap<String, T>();
		Map<String, Long> hashTs = new HashMap<String, Long>();
		Map<String, Map<byte[], Map<byte[], byte[]>>> qualifiersMap = new HashMap<String, Map<byte[], Map<byte[], byte[]>>>();
		for (Entry<String, T> entryData : data.entrySet()) {
			String key = entryData.getKey();
			String hashKey = hashKey(key);
			hashData.put(hashKey, entryData.getValue());
			hashTs.put(hashKey, ts.get(key));
			
			Map<byte[], Map<byte[], byte[]>> qualifiers = new HashMap<byte[], Map<byte[], byte[]>>();
			Map<byte[], byte[]> qualifier = new HashMap<byte[], byte[]>();
			Map<String, String> fields = fieldsMap.get(key);
			if (fields == null ) {
				continue;
			}
			for (Entry<String, String> entry : fields.entrySet()) {
				if (entry.getValue() == null) {
					throw new IllegalArgumentException("field:" + entry.getKey() + " value is null!");
				}
				qualifier.put(entry.getKey().getBytes(), entry.getValue().getBytes());
			}
			qualifiers.put(HBaseRecord.FAMILY_NAME_DATA_BYTES, qualifier);
			qualifiersMap.put(hashKey, qualifiers);
		}
		
		return hbaseObjectStore.multiPut(hashData, hashTs, defaultTs, qualifiersMap);
	}
	
	public List<T> get(List<String> keys) throws Exception{
		List<String> hashKeys=new ArrayList<String>();
		for(String key:keys){
			hashKeys.add(hashKey(key));
		}
		return hbaseObjectStore.get(hashKeys);
	}
	
	public T get(String key) throws Exception{
		return hbaseObjectStore.get(hashKey(key));
	}
	
	public HbaseObject<T> getHbaseObject(String rowKey) throws Exception{
		return hbaseObjectStore.getHbaseObject(hashKey(rowKey));
	}
	
	public List<String> keys(String startKey,String endKey) throws Exception {
		return keys(startKey,endKey,null);
	}
	public List<String> keys(String startKey, String endKey, final Map<String, String> fields) throws Exception{
		Set<String> retKeys=new TreeSet<String>();
		List<String> realKeys=new ArrayList<String>();
		CompletionService<List<String>> completionService = new ExecutorCompletionService<List<String>>(executorService);
		for(int i=0;i<remainder;i++){
			final String startRow=i+"_"+startKey;
			final String stopRow=i+"_"+endKey;
			completionService.submit(new Callable<List<String>>() {	
				@Override
				public List<String> call() throws Exception {				
					return hbaseObjectStore.keys(startRow,stopRow,fields);
				}
			});
		}
		for(int i=0;i<remainder;i++){
			Future<List<String>> future = completionService.take();
			if(future!=null && future.get()!=null){ 
				realKeys.addAll(future.get());
			}		
		}	
		for(String key:realKeys){
			retKeys.add(originalKey(key));
		}
		return new ArrayList<String>(retKeys);
	}
	
	public Map<Long, T> getAllVersion(String key) throws Exception {
		return hbaseObjectStore.getAllVersion(hashKey(key));
	}
	
	public void remove(String key) throws Exception {
		hbaseObjectStore.remove(hashKey(key));
	}
	
	public void flush() throws Exception {
		hbaseObjectStore.flush();
	}
	
	public HBaseScanner<HbaseObject<T>> getHBaseScanner(String startRow, String stopRow) throws Exception{
		return getHBaseScanner(startRow, stopRow, null, null);
	}
	
	public HBaseScanner<HbaseObject<T>> getHBaseScanner(String startRow, String stopRow,
			Long timestamp) throws Exception{
		return getHBaseScanner(startRow, stopRow, timestamp, null);
	}
	
	public HBaseScanner<HbaseObject<T>> getHBaseScanner(String startRow, String stopRow,
			Long timestamp, Map<byte[], List<byte[]>> qualifiers) throws Exception {
	    return getHBaseScanner(startRow, stopRow, timestamp,timestamp==null?null:timestamp+1,qualifiers);
	}
	
	public HBaseScanner<HbaseObject<T>> getHBaseScanner(String startRow, String stopRow,
			Long mixStamp,Long maxStamp, Map<byte[], List<byte[]>> qualifiers) throws Exception {
		HbaseObjectScanner<T>[] hbaseObjectScanners=new HbaseObjectScanner[remainder];
		for(int i=0;i<remainder;i++){
			hbaseObjectScanners[i]=hbaseObjectStore.getHBaseScanner(i+"_"+startRow, i+"_"+stopRow, mixStamp, maxStamp, qualifiers);
		}
		return new HashHBaseObjectScanner<T>(hbaseObjectScanners);
	}
	
	public HBaseScanner<HbaseObject<T>> getHBaseScanner(String startRow, String stopRow, Integer cache) throws Exception{
		return getHBaseScanner(startRow, stopRow, null, null, cache);
	}
	
	public HBaseScanner<HbaseObject<T>> getHBaseScanner(String startRow, String stopRow,
			Long timestamp, Map<byte[], List<byte[]>> qualifiers, Integer cache) throws Exception {
		HbaseObjectScanner<T>[] hbaseObjectScanners=new HbaseObjectScanner[remainder];
		for (int i=0;i<remainder;i++) {
			hbaseObjectScanners[i]=hbaseObjectStore.getHBaseScanner(i+"_"+startRow, i+"_"+stopRow, timestamp, qualifiers, cache);
		}
	    return new HashHBaseObjectScanner<T>(hbaseObjectScanners);
	}
	
	public ResultScanner[] getResultScanners(String rowkeyPrefix) throws Exception {
		ResultScanner[] resultScanners=new ResultScanner[remainder];
		for(int i=0;i<remainder;i++){
			resultScanners[i]=hbaseObjectStore.getResultScanner(i+"_"+rowkeyPrefix);
		}
		return resultScanners;
	}
	
	public static void main(String[] args) {
		String key = "1_sdfssf";
		System.out.println(originalKey(key));
	}
}
