package com.yihaodian.store.hbasestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.yihaodian.store.hbasestore.dataconvert.BaseConvert;
/**
 * this class is interface class,
 * client can new HBaseObjectStore instance
 *
 * @param <T>
 */
public class HBaseObjectStore<T> {
	private static Logger log = Logger.getLogger(HBaseObjectStore.class);
	private HBaseStore store;
	private HBaseRecordBuilder<T> builder;
	private String storeName;
	private String[] familys;
	private Class<T> clazz;
	
	/**
	 * tablename is same to className
	 * @param className
	 */
	public HBaseObjectStore(Class<T> clazz){
		this(clazz,true);
	}
	
	/**
	 * 
	 * @param className
	 * @param addEnv 是否表名后添加环境变量
	 */
	public HBaseObjectStore(Class<T> clazz,boolean addEnv){
		this(clazz,clazz.getSimpleName(),true);
	}
	
	/**
	 * 
	 * @param className
	 * @param storeName
	 */
	public HBaseObjectStore(Class<T> clazz, String storeName){
		this(clazz, storeName,true);		
	}
	
	public HBaseObjectStore(Class<T> clazz, String storeName,boolean addEnv){
		this(clazz, storeName,null, addEnv);		
	}
	
	
	public HBaseObjectStore(Class<T> clazz, String storeName, String[] familys){
		this(clazz, storeName, false, familys, true);
	}
	
	public HBaseObjectStore(Class<T> clazz, String storeName, String[] familys,boolean addEnv){
		this(clazz, storeName, false, familys, addEnv);
	}
	
    /**
     * 
     * @param className
     * @param storeName
     * @param truncate
     * @param familys
     * @param evict
     */
	public HBaseObjectStore(Class<T> clazz, String storeName, boolean truncate, 
			String[] familys, boolean addEnv){
		this(clazz, storeName, truncate, familys, addEnv, null);
	}
	
	public HBaseObjectStore(Class<T> clazz, String storeName, boolean truncate, 
			String[] familys, boolean addEnv, String[] coprocessors){
		this.clazz=clazz;
		this.storeName = storeName;
		this.familys = familys;
		
		if(truncate) {
			HBaseStoreManager.getInstance().removeStore(storeName);
		}
		store = HBaseStoreManager.getInstance().getStore(storeName, familys, addEnv, coprocessors);
		builder = new HBaseRecordBuilder<T>(clazz);
	}
	
	public HBaseObjectStore(Class<T> clazz, HBaseStore store) {
		this.clazz = clazz;
		this.store = store;
		this.builder = new HBaseRecordBuilder<T>(clazz);
	}
	
	/**
	 * 
	 * @param key
	 * @param t
	 * @return
	 * @throws Exception 
	 */
	public T put(String key, T t) throws Exception{
		return put(key, t, System.currentTimeMillis());
	}
	
	/**
	 * put with timestamp
	 * @param key
	 * @param t
	 * @param timestamp
	 * @return
	 * @throws Exception 
	 */
	public T put(String key, T t, long timestamp) throws Exception{
		_ensureOpen();
		
		HBaseRecord record = builder.toHBaseRecord(key, t, timestamp);
		HBaseRecord ret = store.put(record);
		if(ret != null && ret.equals(record)) {
			return t;
		} else { 
			return null;
		}
	}
	
	/**
	 * Put with some visible fields
	 * @param key
	 * @param t
	 * @param fields
	 * @return
	 * @throws Exception 
	 */
	public T put(String key, T t, Map<byte[], Map<byte[], byte[]>> qualifiers) throws Exception{
		return put(key, t, System.currentTimeMillis(), qualifiers);
	}
	
	/**
	 * Put with some visible fields
	 * @param key
	 * @param t
	 * @param timestamp
	 * @param fields
	 * @return
	 * @throws Exception 
	 */
	public T put(String key, T t, long timestamp, Map<byte[], Map<byte[], byte[]>> qualifiers) throws Exception{
		_ensureOpen();
		
		HBaseRecord record = builder.toHBaseRecord(key, t, timestamp, qualifiers);
		HBaseRecord ret = store.put(record);
		if(ret != null && ret.equals(record)) {
			return t;
		} else { 
			return null;
		}
	}
	
	/**
	 * get all versions by key
	 * @param key
	 * @return
	 * @throws Exception 
	 */
	public List<Long> getVersions(String key) throws Exception{
		_ensureOpen();
		
		return store.versions(key, 0L, Long.MAX_VALUE);
	}
	
	/**
	 * 
	 * @param key
	 * @param columnFamily
	 * @param columnList
	 * @return key:column
	 *         value:versions
	 * @throws Exception
	 */
	public Map<String, List<Long>> getVersions(String key, String columnFamily, List<String> columnList) throws Exception{
		_ensureOpen();
		List<byte[]> qualifierList = new ArrayList<byte[]>();
		for (String column : columnList) {
			qualifierList.add(column.getBytes());
		}
		Map<byte[], List<Long>> versions = store.versions(key, 0L, Long.MAX_VALUE, columnFamily.getBytes(), qualifierList);
		Map<String, List<Long>> reslutMap = new HashMap<String, List<Long>>();
		for (Entry<byte[], List<Long>> entry : versions.entrySet()) {
			reslutMap.put(new String(entry.getKey()), entry.getValue());
		}
		return reslutMap;
	}
	
	/**
	 * get Versions by key,startTimeStamp and endTimeStamp
	 * @param key
	 * @param startTs
	 * @param endTs
	 * @return
	 * @throws Exception 
	 */
	public List<Long> getVersions(String key, long startTs, long endTs) throws Exception{
		_ensureOpen();
		
		return store.versions(key, startTs, endTs);
	}
        
	/**
	 * get all the version of given key
	 * @param key
	 * @return 
	 * @throws Exception 
	 */
	public Map<Long, T> getAllVersion(String key) throws Exception {
		_ensureOpen();
		
		Map<Long,HBaseRecord> records = store.getAllVersion(key);
		Map<Long, T> ret = new HashMap<Long, T>();
		for(Entry<Long, HBaseRecord> entry : records.entrySet())
		{
			T t = builder.fromHBaseRecord(entry.getValue());
			ret.put(entry.getKey(), t);
		}

		return ret;
	}

	/**
	 * get method by key
	 * @param key
	 * @return
	 * @throws Exception 
	 */
	public T get(String key) throws Exception{
		return get(key,null);
	}
	
	/**
	 * this method return T through key and timestamp
	 * @param key
	 * @param timestamp
	 * @return
	 * @throws Exception 
	 */
	public T get(String key, Long timestamp) throws Exception{	
		T t=null;
		HBaseRecord hr=store.get(key,timestamp);
		if(hr!=null){
			t=builder.fromHBaseRecord(hr);
		}
		return t;	
	}
	
	/**
	 * 
	 * @param key
	 * @param startTs
	 * @param endTs
	 * @return
	 * @throws Exception 
	 */
	public T get(String key, long startTs, long endTs) throws Exception{
		_ensureOpen();
		
		T t = null;
		HBaseRecord record = store.get(key, startTs, endTs);
		if(record != null) {
			t = builder.fromHBaseRecord(record);
		}
		return t;
	}
	
	/**
	 * this method is fetch fields by key 
	 * @param key
	 * @return
	 * @throws Exception 
	 */
	public Map<String, String> getFields(String key) throws Exception{
		return getFields(key, 0L, Long.MAX_VALUE);
	}
	
	public Map<String, String> getFields(String key, long timestamp) throws Exception{		
		return getFields(key, 0L, timestamp);		
	}
	
	public Map<String, String> getFields(String key, long startTs, long endTs) throws Exception{
		_ensureOpen();
		
		return store.fields(key, startTs, endTs);
	}
	
	/**
	 * fetch all versions for all columns by key
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public Map<String, List<Long>> columnVersions(String key) throws Exception{
		return columnVersions(key, 0L, Long.MAX_VALUE);
	}
	
	public Map<String, List<Long>> columnVersions(String key, long startTs, long endTs) throws Exception{
		_ensureOpen();
		return store.columnVersions(key, startTs, endTs);
	}
	
	//////////////////////////////////////////////////////////////////////////////
	//// Multi Put/Get operations
	//////////////////////////////////////////////////////////////////////////////
	
    public Map<String, T> multiPut(Map<String, T> data) throws Exception{	
		return multiPut(data,HConstants.LATEST_TIMESTAMP);
	}
	
	public Map<String, T> multiPut(Map<String, T> data, Long timestamp) throws Exception{	
		
		List<HBaseRecord> records = new ArrayList<HBaseRecord>();
		
		if(data == null || data.isEmpty()) {
			return data;
		}
		
		for(Entry<String, T> entry : data.entrySet()) {
			HBaseRecord record = builder.toHBaseRecord(entry.getKey(), entry.getValue(), timestamp);
			records.add(record);
		}
		
		_ensureOpen();
		store.put(records);
		return data;
	}
	
	public Map<String, T> multiPut(Map<String, T> data, Map<String, Long> ts, long defaultTs, 
			Map<String, Map<byte[], Map<byte[], byte[]>>> qualifiersMap) throws Exception{	
		List<HBaseRecord> records = new ArrayList<HBaseRecord>();
		
		if(data == null || data.isEmpty()) {
			return data;
		}
		
		for(Entry<String, T> entry : data.entrySet()) {
			String key = entry.getKey();
			Long timestamp = ts.get(key);
			if (timestamp == null) {
				timestamp = defaultTs;
			}
			Map<byte[], Map<byte[], byte[]>> qualifiers = qualifiersMap.get(key);
			HBaseRecord record = builder.toHBaseRecord(key, entry.getValue(), timestamp, qualifiers);
			records.add(record);
		}
		
		_ensureOpen();
		store.put(records);
		return data;
	}
	
	public Map<String, T> multiPut(Map<String, T> data, Map<String, Long> ts, long defaultTs) throws Exception{	
		List<HBaseRecord> records = new ArrayList<HBaseRecord>();
		
		if(data == null || data.isEmpty()) {
			return data;
		}
		
		for(Entry<String, T> entry : data.entrySet()) {
			String key = entry.getKey();
			Long timestamp = ts.get(key);
			if (timestamp == null) {
				timestamp = defaultTs;
			}
			HBaseRecord record = builder.toHBaseRecord(key, entry.getValue(), timestamp);
			records.add(record);
		}
		
		_ensureOpen();
		store.put(records);
		return data;
	}
	
	public Map<String, T> multiGet(List<String> keys) throws Exception{
		return multiGet(keys,null);
	}
	
	public Map<String, T> multiGet(List<String> keys,Long version) throws Exception{
		_ensureOpen();
		
		Map<String, T> map = new LinkedHashMap<String, T>();

		List<HBaseRecord> records = store.get(keys,version);
		if(records != null && !records.isEmpty()) {
			for(HBaseRecord r : records) {
				T t = builder.fromHBaseRecord(r);
				if(t != null){
					map.put(r.getKey(), t);
				} else {
					log.error("Failed to translate to HBaseRecord.");
				}
			}
		}	
		return map;
	}
	
	/**
	 * muliGet 
	 * @param keys
	 * @param startTs
	 * @param endTs
	 * @return
	 * @throws Exception 
	 */
	public Map<String, T> multiGet(List<String> keys, long startTs, long endTs) throws Exception{
		_ensureOpen();
		
		Map<String, T> map = new LinkedHashMap<String, T>();

		List<HBaseRecord> records = store.get(keys, startTs, endTs);
		if(records != null && !records.isEmpty()) {
			for(HBaseRecord r : records) {
				T t = builder.fromHBaseRecord(r);
				if(t != null){
					map.put(r.getKey(), t);
				} else {
					log.error("Failed to translate to HBaseRecord.");
				}
			}
		}
		
		return map;
	}
	
	public List<T> get(List<String> keys) throws Exception{
		_ensureOpen();
		Map<String, T> map=multiGet(keys);
		if(map==null){
			return null;
		}
		List<T> ret=new ArrayList<T>(map.values());
		
		return ret;
	}
	
	public List<T> get(List<String> keys, long timestamp) throws Exception{
		_ensureOpen();
		Map<String, T> map=multiGet(keys,timestamp);
		if(map==null){
			return null;
		}
		List<T> ret=new ArrayList<T>(map.values());
		return ret;		
	}
	
	public List<T> get(List<String> keys, long startTs, long endTs) throws Exception{
		_ensureOpen();
		
		List<T> list = new ArrayList<T>();		
		List<HBaseRecord> records = store.get(keys, startTs, endTs);
		if(records != null && !records.isEmpty()) {
			for(HBaseRecord r : records) {
				T t = builder.fromHBaseRecord(r);
				if(t != null){
					list.add(t);
				} else {
					log.error("Failed to translate to HBaseRecord.");
				}
			}
		}
		return list;
	}
	
	//////////////////////////////////////////////////////////////////////////////
	//// Keys operations
	//////////////////////////////////////////////////////////////////////////////
	public List<String> getKeys() throws Exception{
		_ensureOpen();
		return store.keys(0L, Long.MAX_VALUE);
	}
	/**
	 * get all keys between startts and endts
	 * @param startTs
	 * @param endTs
	 * @return
	 * @throws Exception 
	 */
	public List<String> getKeys(long startTs, long endTs) throws Exception{
		_ensureOpen();
		return store.keys(startTs, endTs);
	}
	
	public List<String> getKeys(long startTs, long endTs, Map<String, String> fields) throws Exception{
		_ensureOpen();
		return store.keys(startTs, endTs, fields);
	}
	
	public List<String> keys(String startKey, String endKey) throws Exception {
		return keys(startKey, endKey,null);
	}
	
	public List<String> keys(String startKey, String endKey,  Map<String, String> fields) throws Exception {
		_ensureOpen();
		return store.keys(startKey, endKey, fields);
	}
	
	public List<byte[]> keys(Long version) throws Exception{
		_ensureOpen();
		return store.keys(version);
	}
	
	public void remove(String key) throws Exception {
		_ensureOpen();
		store.remove(key);		
	}
	
	public void remove(Long version) throws Exception {
		_ensureOpen();
		store.remove(version);		
	}
	
	
	/**
	 * remove data between startT and endT
	 * @param startT
	 * @param endT
	 * @throws Exception 
	 */
	public void removeBetweenTimestamp(long startT, long endT) throws Exception{
		_ensureOpen();
		 store.removeBetweenTimestamp(startT, endT);
	}
	
	/**
	 * remove before timestamp
	 * @param timestamp
	 * @throws Exception 
	 */
	public void removeBeforeTimestamp(long timestamp) throws Exception{
		_ensureOpen();
		 store.removeBeforeTimestampKey(timestamp);
	}
	
	public void remove(List<String> keys) throws Exception {
		_ensureOpen();
		store.remove(keys);		
	}
	
	public long incrementColumnValue(String key, String family, String qualifier, 
			long amount) throws Exception{
		_ensureOpen();
		return store.incrementColumnValue(key, family, qualifier, amount);
	}
	
	public Map<String, String> getFields(String family, String qualifier, BaseConvert convert) throws Exception{
		_ensureOpen();
		return store.getFields(family, qualifier, convert);
	}
	
	/**
	 * Get object by the specified row range. 
	 * @param startRow
	 * @param stopRow
	 * @param pageSize
	 * @return
	 * @throws Exception 
	 */
	public List<T> getRangeRow(String startRow,
			String stopRow, long pageSize) throws Exception{
		List<T> list = new ArrayList<T>();		
		List<HBaseRecord> records = store.getRangeRow(startRow, stopRow, pageSize);
		if(records != null && !records.isEmpty()) {
			for(HBaseRecord r : records) {
				T t = builder.fromHBaseRecord(r);
				if(t != null){
					list.add(t);
				} else {
					log.error("Failed to translate to HBaseRecord.");
				}
			}
		}
		return list;
	}
	
	private void _ensureOpen() throws Exception {
		if(store == null) {
			synchronized(this) {
				if(store == null) {
					store = HBaseStoreManager.getInstance().getStore(storeName, familys);
				}
			}
		}
		if(store == null) {
			throw new Exception("HBaseObjectStore could not connect to HBase.");
		}
	}

	public String getStoreName() {
		return storeName;
	}
	
	public void flush() throws Exception {
		store.flush();
	}
	
	public String getTableName(){
		return HBaseStoreManager.tableName(storeName);
	}
	
	public HBaseStore getHBaseStore(){
		return store;
	}
	
	public HbaseObject<T> getHbaseObject(String rowKey) throws Exception{
		HBaseRecord hr=store.get(rowKey);
		T t = builder.fromHBaseRecord(hr);
		return new HbaseObject<T>(hr.getKey(),hr.getTimestamp(),t);
	}
	
	public List<HbaseObject<T>> getHbaseObjectAllVersions(String rowKey) throws Exception{
		Map<Long, HBaseRecord> map=store.getAllVersion(rowKey);
		List<HbaseObject<T>> list=new ArrayList<HbaseObject<T>>();
		for(Entry<Long,HBaseRecord> entry:map.entrySet()){
			HBaseRecord hr=entry.getValue();
			T t = builder.fromHBaseRecord(hr);
			HbaseObject<T> ho=new HbaseObject<T>(hr.getKey(),hr.getTimestamp(),t);
			list.add(ho);
		}
		return list;
	}
	
//	public List<HbaseObject<T>> getHbaseObject(String rowKey, long version, String columnFamily, List<String> columnList) throws Exception{
//		List<HbaseObject<T>> list = new ArrayList<HbaseObject<T>>();
//		List<byte[]> qualifierList = new ArrayList<byte[]>();
//		for (String column : columnList) {
//			qualifierList.add(column.getBytes());
//		}
//		Map<byte[], List<byte[]>> qualifiers = new HashMap<byte[], List<byte[]>>();
//		qualifiers.put(columnFamily.getBytes(), qualifierList);
//		HBaseRecord hr=store.getQualifiers(rowKey, version, qualifiers);
//		List<T> tList = builder.fromQualifierDatas(hr);
//		for (T t : tList) {
//			list.add(new HbaseObject<T>(hr.getKey(),hr.getTimestamp(),t));
//		}
//		return list; 
//	}
	
	public HbaseObjectScanner<T> getHBaseScanner(String startRow, String stopRow) throws Exception{
		return getHBaseScanner(startRow, stopRow, null, null);
	}
	
	public HbaseObjectScanner<T> getHBaseScanner(String startRow, String stopRow, Integer caching) throws Exception{
		return getHBaseScanner(startRow, stopRow, null, null, caching);
	}
	
	public HbaseObjectScanner<T> getHBaseScanner(String startRow, String stopRow,
			Long timestamp) throws Exception{
		return getHBaseScanner(startRow, stopRow, timestamp, null);
	}
	
	public HbaseObjectScanner<T> getHBaseScanner(String startRow, String stopRow,
			Long timestamp, Map<byte[], List<byte[]>> qualifiers) throws Exception {
		HBaseRecordScanner recordScanner = 
				store.getHBaseScanner(startRow, stopRow, timestamp, qualifiers);
		return new HbaseObjectScanner<T>(recordScanner, clazz);
	}
	
	public HbaseObjectScanner<T> getHBaseScanner(String startRow, String stopRow,
			Long timestamp, Map<byte[], List<byte[]>> qualifiers, Integer caching) throws Exception {
		HBaseRecordScanner recordScanner = 
				store.getHBaseScanner(startRow, stopRow, timestamp, qualifiers, caching);
		return new HbaseObjectScanner<T>(recordScanner, clazz);
	}
	
	public HbaseObjectScanner<T> getHBaseScanner(String startRow, String stopRow,
			Long mixStamp,Long maxStamp, Map<byte[], List<byte[]>> qualifiers) throws Exception {
		HBaseRecordScanner recordScanner = 
			store.getHBaseScanner(startRow, stopRow, mixStamp,maxStamp, qualifiers);
	    return new HbaseObjectScanner<T>(recordScanner, clazz);
	}
	
	public HbaseObjectScanner<T> getHBaseScanner(String startRow, String stopRow,
			Long mixStamp,Long maxStamp, Map<byte[], List<byte[]>> qualifiers, Integer cache) throws Exception {
		HBaseRecordScanner recordScanner = 
			store.getHBaseScanner(startRow, stopRow, mixStamp,maxStamp, qualifiers, cache);
	    return new HbaseObjectScanner<T>(recordScanner, clazz);
	}
	
	public ResultScanner getResultScanner(String rowkeyPrefix) throws Exception {
		Scan scan = new Scan();
		scan.addColumn(HBaseRecord.FAMILY_NAME_DATA_BYTES,
				HBaseRecord.DEFAULT_QUALIFIER_BYTES);
		scan.setFilter(new PrefixFilter(Bytes.toBytes(rowkeyPrefix)));
		
		HTableInterface htable = store.getHTable();
		return htable.getScanner(scan);
	}
}
