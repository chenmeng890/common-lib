package com.yihaodian.store.hbasestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ExecutorService;

import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.coprocessor.Batch;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.ipc.BlockingRpcCallback;
import org.apache.hadoop.hbase.ipc.ServerRpcController;
import org.apache.log4j.Logger;

import com.yihaodian.store.coprocessor.generated.RowCountProtos;
import com.yihaodian.store.coprocessor.generated.RowCountProtos.RowCountService;
import com.yihaodian.store.hbasestore.dataconvert.BaseConvert;
import com.yihaodian.store.hbasestore.dataconvert.StringConvert;

/**
 * 
 * 
 *
 */

public class HBaseStore {

	private static Logger log = Logger.getLogger(HBaseStore.class);

	private static long EMPTY = -1;
	private long evictMark = EMPTY;
	private long evictInterval = EMPTY;

	private String tablename;

	private HConnection hconnection;

	private ExecutorService pool;

	private HTablePool htablePool;

	/**
	 * constructor HBaseStore HTable is a not thread safe class, we can get
	 * <code>HTable</code> by tablePool.getTable(tableName)
	 * 
	 * @param tablename
	 * @param tablePool
	 */
	public HBaseStore(String tablename, HTablePool tablePool) {
		this.tablename = tablename;
		this.htablePool = tablePool;
	}

	public HBaseStore(String tablename, HConnection hconnection,
			ExecutorService pool) {
		this.tablename = tablename;
		this.hconnection = hconnection;
		this.pool = pool;
	}
	
	public void resetHTablePool(HTablePool tablePool){
		this.htablePool=tablePool;
	}

	/**
	 * this method can get a HTable reference by tablename, used up can invoke
	 * <code>HTable.close()</code> not return to HTablePool
	 * 
	 * @return
	 */
	public HTableInterface getHTable() {
		HTableInterface htable = null;
		if (hconnection != null && pool != null) {
			try {
				htable = new HTable(tablename.getBytes(), hconnection, pool);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		} else if (htablePool != null) {
			htable = htablePool.getTable(tablename);
		}
		return htable;
	}

	private static long relaxEndTs(long endTs) {
		return ((endTs == Long.MAX_VALUE) ? Long.MAX_VALUE : endTs + 1);
		
	}

	private static long relaxStartTs(long startTs) {		
		return startTs;
	}
	
	
	public List<HBaseRecord> get(List<String> keys) throws Exception{
		return get(keys,null);
	}
	/**
	 * batch Get 
	 * @param keys
	 * @param version
	 * @return
	 * @throws Exception 
	 */
	public List<HBaseRecord> get(List<String> keys,Long version) throws Exception {
		List<HBaseRecord> records = new ArrayList<HBaseRecord>();
		if (keys == null || keys.isEmpty()) {
			return records;
		}
		List<Get> gets = new ArrayList<Get>();
		for (String key : keys) {
			Get get = new Get(key.getBytes());
			if(version!=null){
				get.setTimeStamp(version);
			}
			gets.add(get);
		}
		HTableInterface table = getHTable();
		if (table == null) {
			return records;
		}
		try {
			Result[] results = table.get(gets);
			if (results != null && results.length != 0) {
				for (Result rs : results) {
					if (!rs.isEmpty()) {
						HBaseRecord record = HBaseRecord.fromResult(rs);
						if (record != null) {
							records.add(record);
						} else {
							log.error("Failed to get record " + rs);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + tablename + ":" + keys, e);
		} finally {
			closeHTable(table);
		}
		return records;
	}
	
	/**
	 * Get multiple objects at once
	 * 
	 * @param keys
	 * @param startTs
	 * @param endTs
	 * @return
	 * @throws Exception 
	 */
	public List<HBaseRecord> get(List<String> keys, long startTs, long endTs) throws Exception {

		long relaxedEndTs = relaxEndTs(endTs);
		long relaxedStartTs = relaxStartTs(startTs);

		List<HBaseRecord> records = new ArrayList<HBaseRecord>();
		if (keys == null || keys.isEmpty()) {
			return records;
		}

		List<Get> gets = new ArrayList<Get>();
		for (String key : keys) {
			Get get = new Get(key.getBytes());
			try {
				get.setTimeRange(relaxedStartTs, relaxedEndTs);
			} catch (IOException e1) {
				throw new Exception("Invalid timestamp start " + relaxedStartTs
						+ ", end: " + relaxedEndTs, e1);
			}
			gets.add(get);
		}
		HTableInterface table = getHTable();
		if (table == null) {
			return records;
		}
		try {
			Result[] results = table.get(gets);
			if (results != null && results.length != 0) {
				for (Result rs : results) {
					if (!rs.isEmpty()) {
						HBaseRecord record = HBaseRecord.fromResult(rs);
						if (record != null) {
							records.add(record);
						} else {
							log.error("Failed to get record " + rs);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + tablename + ":" + keys, e);
		} finally {
			closeHTable(table);
		}
		return records;
	}
	
	public List<HBaseRecord> getMulti(List<String> keys,
			Map<byte[], List<byte[]>> qualifiers) throws Exception {
		List<HBaseRecord> records = new ArrayList<HBaseRecord>();
		if (keys == null || keys.isEmpty()) {
			return records;
		}

		List<Get> gets = new ArrayList<Get>();
		for (String key : keys) {
			Get get = new Get(key.getBytes());
			gets.add(get);
		}
		HTableInterface table = getHTable();
		if (table == null) {
			return records;
		}
		
		try {
			Result[] results = table.get(gets);
			if (results != null && results.length != 0) {
				for (Result rs : results) {
					if (!rs.isEmpty()) {
						HBaseRecord record = HBaseRecord.fromResult(rs, qualifiers);
						if (record != null) {
							records.add(record);
						} else {
							log.error("Failed to get record " + rs);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + new String(table.getTableName()) + ":"
					+ keys, e);
		} finally {
			closeHTable(table);
		}
	
		return records;
	}

	/**
	 * Get object with at the time of "timestamp"
	 * 
	 * @param key
	 * @param timestamp
	 * @return
	 * @throws Exception 
	 */
	public HBaseRecord get(String key) throws Exception {
		return get(key, null);
	}

	public HBaseRecord get(String key, Long timestamp) throws Exception {

		HBaseRecord record = null;

		Get get = new Get(key.getBytes());
		if (timestamp != null)
			get.setTimeStamp(timestamp);
		HTableInterface table = getHTable();
		if (table == null) {
			return record;
		}
		try {
			Result rs = table.get(get);
			record = HBaseRecord.fromResult(rs);
		} catch (IOException e) {
			throw new Exception("Failed to get " + new String(table.getTableName()) + ":"
					+ key, e);
		} finally {
			closeHTable(table);
		}
		return record;

	}

	/**
	 * Get object with at the time of "timestamp"
	 * 
	 * @param key
	 * @param timestamp
	 * @return
	 * @throws Exception 
	 */
	public HBaseRecord get(String key, long startTs, long endTs) throws Exception {

		HBaseRecord record = null;

		long relaxedEndTs = relaxEndTs(endTs);
		long relaxedStartTs = relaxStartTs(startTs);

		Get get = new Get(key.getBytes());
		try {
			get.setTimeRange(relaxedStartTs, relaxedEndTs);
		} catch (IOException e1) {
			throw new Exception("Invalid timestamp start " + relaxedStartTs + ", end: "
					+ relaxedEndTs, e1);
		}
		HTableInterface table = getHTable();
		if (table == null) {
			return record;
		}
		try {
			Result rs = table.get(get);
			record = HBaseRecord.fromResult(rs);
		} catch (IOException e) {
			throw new Exception("Failed to get " + new String(table.getTableName()) + ":"
					+ key, e);
		} finally {
			closeHTable(table);
		}
		return record;

	}

	/**
	 * get all version record of given Key.
	 * 
	 * @param key
	 * @return
	 * @throws Exception 
	 */
	public Map<Long, HBaseRecord> getAllVersion(String key) throws Exception {
		Map<Long, HBaseRecord> ret = new HashMap<Long, HBaseRecord>();

		Get get = new Get(key.getBytes());
		get.setMaxVersions();
		HTableInterface table = getHTable();
		if (table == null) {
			return ret;
		}
		try {
			Result rs = table.get(get);
			if (!rs.isEmpty()) {
				// family -> < qualifier -> <timestamp -> value > >
				NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs
						.getMap();

				// Check family exists
				NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map
						.get(HBaseRecord.FAMILY_NAME_DATA_BYTES);
				if (familyMap != null) {
					// Check value exists before timestamp
					NavigableMap<Long, byte[]> versionMap = familyMap
							.get(HBaseRecord.DEFAULT_QUALIFIER_BYTES);
					if (versionMap != null && !versionMap.isEmpty()) {
						Iterator<Long> it = versionMap.keySet().iterator();
						while (it.hasNext()) {
							Long ts = it.next();
							byte[] value = versionMap.get(ts);
							HBaseRecord record = new HBaseRecord(key, value, ts);
							ret.put(ts, record);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + new String(table.getTableName()) + ":"
					+ key, e);
		} finally {
			closeHTable(table);
		}

		return ret;
	}
	
	public Map<Long, HBaseRecord> getAllVersion(String key, byte[] family, 
			byte[] qualifier) throws Exception {
		Map<Long, HBaseRecord> ret = new HashMap<Long, HBaseRecord>();
	
		Get get = new Get(key.getBytes());
		get.setMaxVersions();
		HTableInterface table = getHTable();
		if (table == null) {
			return ret;
		}
		try {
			Result rs = table.get(get);
			if (!rs.isEmpty()) {
				// family -> < qualifier -> <timestamp -> value > >
				NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs
						.getMap();
	
				// Check family exists
				NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map
						.get(family);
				if (familyMap != null) {
					// Check value exists before timestamp
					NavigableMap<Long, byte[]> versionMap = familyMap
							.get(qualifier);
					if (versionMap != null && !versionMap.isEmpty()) {
						Iterator<Long> it = versionMap.keySet().iterator();
						while (it.hasNext()) {
							Long ts = it.next();
							byte[] value = versionMap.get(ts);
							Map<byte[], Map<byte[], byte[]>> qualifierDatas = 
									new HashMap<byte[], Map<byte[],byte[]>>();
							Map<byte[], byte[]> qulifier = new HashMap<byte[], byte[]>();
							qulifier.put(qualifier, value);
							qualifierDatas.put(family, qulifier);
							HBaseRecord record = new HBaseRecord(key, ts, qualifierDatas);
							ret.put(ts, record);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + new String(table.getTableName()) + ":"
					+ key, e);
		} finally {
			closeHTable(table);
		}
	
		return ret;
	}
	
	public Map<Long, HBaseRecord> getAllVersion(String key, Map<byte[], List<byte[]>> qualifiers) throws Exception {
		Map<Long, HBaseRecord> ret = new HashMap<Long, HBaseRecord>();
	
		Get get = new Get(key.getBytes());
		get.setMaxVersions();
		HTableInterface table = getHTable();
		if (table == null) {
			return ret;
		}
		try {
			Result rs = table.get(get);
			// Version -> <family -> qualifiers>
			Map<Long, Map<byte[], Map<byte[], byte[]>>> allVersionQualifiers = 
					new HashMap<Long, Map<byte[],Map<byte[],byte[]>>>();
			if (!rs.isEmpty()) {
				// family -> < qualifier -> <timestamp -> value > >
				NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs
						.getMap();
				for (Entry<byte[], List<byte[]>> familyEntry : qualifiers.entrySet()) {
					byte[] family = familyEntry.getKey();
					// Check family exists
					NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map
							.get(family);
					
					if (familyMap != null) {
						List<byte[]> qualifierNames = familyEntry.getValue();
						for (byte[] qualifierName : qualifierNames) {
							// Check value exists before timestamp
							NavigableMap<Long, byte[]> versionMap = familyMap
									.get(qualifierName);
							if (versionMap != null && !versionMap.isEmpty()) {
								Iterator<Long> it = versionMap.keySet().iterator();
								while (it.hasNext()) {
									Long ts = it.next();
									Map<byte[], Map<byte[], byte[]>> qualifierDatas = 
											allVersionQualifiers.get(ts);
									if (qualifierDatas == null) {
										qualifierDatas = 
												new HashMap<byte[], Map<byte[],byte[]>>();
										allVersionQualifiers.put(ts, qualifierDatas);
									}
									byte[] value = versionMap.get(ts);
									
									Map<byte[], byte[]> qulifier = qualifierDatas.get(family);
									if (qulifier == null ) {
										qulifier = new HashMap<byte[], byte[]>();
										qualifierDatas.put(family, qulifier);
									}
									
									qulifier.put(qualifierName, value);
								}
							}
						}
					}
				}
				
				for (Entry<Long, Map<byte[], Map<byte[], byte[]>>> entry : allVersionQualifiers.entrySet()) {
					Long version = entry.getKey();
					HBaseRecord record = new HBaseRecord(key, version, entry.getValue());
					ret.put(version, record);
				}
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + new String(table.getTableName()) + ":"
					+ key, e);
		} finally {
			closeHTable(table);
		}
	
		return ret;
	}

	/**
	 * Save object
	 * 
	 * @param t
	 * @return
	 * @throws Exception 
	 */
	public HBaseRecord put(HBaseRecord record) throws Exception {
		HBaseRecord ret = record;
		Put put = record.toPut();
		HTableInterface table = getHTable();
		if (table == null) {
			return ret;
		}
		try {
			table.put(put);
		} catch (IOException e) {
			throw new Exception("Failed to save record " + record.toString(), e);
		} finally {
			closeHTable(table);
		}		
		return ret;
	}

	/**
	 * Save object in Batch
	 * 
	 * @param t
	 * @return
	 * @throws Exception 
	 */
	public List<HBaseRecord> put(List<HBaseRecord> records) throws Exception {

		List<HBaseRecord> ret = records;
		if (records == null || records.isEmpty()) {
			return null;
		}

		List<Put> puts = new ArrayList<Put>();
		for (HBaseRecord record : records) {
			Put put = record.toPut();
			puts.add(put);
		}
		HTableInterface table = getHTable();
		if (table == null) {
			return ret;
		}
		try {
			table.put(puts);
		} catch (IOException e) {
			throw new Exception("Failed to save record " + records.toString(), e);
		} finally {
			closeHTable(table);
		}
		return ret;
	}

	/**
	 * Delete the row
	 * 
	 * @param record
	 * @return
	 * @throws Exception 
	 */
	public void remove(String key) throws Exception {
		Delete delete = new Delete(key.getBytes());
		HTableInterface table = getHTable();
		if (table == null) {
			return;
		}
		try {
			table.delete(delete);
		} catch (IOException e) {
			throw new Exception("Failed to delete " + new String(table.getTableName())
					+ ":" + key, e);
		} finally {
			closeHTable(table);
		}

	}

	/**
	 * Delete several rows
	 * 
	 * @param keys
	 * @return
	 * @throws Exception 
	 */
	public void remove(List<String> keys) throws Exception {
		if (keys == null || keys.isEmpty()) {
			return;
		}
		List<Delete> deletes = new ArrayList<Delete>();

		for (String key : keys) {
			deletes.add(new Delete(key.getBytes()));
		}

		HTableInterface table = getHTable();
		if (table == null) {
			log.error("Failed to get table ");
			return;
		}
		try {
			table.delete(deletes);
		} catch (IOException e) {
			throw new Exception("Failed to delete " + new String(table.getTableName())
					+ ":" + keys, e);
		} finally {
			closeHTable(table);
		}
	}
	
	/**
	 * delete a version all data
	 * @param version
	 * @throws Exception 
	 */
	public void remove(Long version) throws Exception{
		if(version==null) return;
		List<byte[]> keys=keys(version);
		if(keys==null || keys.size()==0) return;
		List<Delete> deletes=new LinkedList<Delete>();
		Delete delete=null;
		for(byte[] key:keys){
			delete=new Delete(key);
			delete.deleteColumn(HBaseRecord.FAMILY_NAME_DATA_BYTES, 
					HBaseRecord.DEFAULT_QUALIFIER_BYTES, version);
			deletes.add(delete);
		}
		HTableInterface table = getHTable();
		if (table == null) {
			log.error("Failed to get table ");
			return;
		}
		try {
			table.delete(deletes);
		} catch (IOException e) {
			throw new Exception(e);			
		}finally{
			closeHTable(table);
		}
	}
	
	/**
	 * get all keys by version
	 * @param version
	 * @return
	 * @throws Exception 
	 */
	public List<byte[]> keys(Long version) throws Exception{
		List<byte[]> keys=new LinkedList<byte[]>();
		Scan scan = new Scan();
		HTableInterface table = getHTable();
		if (table == null) {
			return keys;
		}
		ResultScanner scanner = null;
		try {
            scan.addColumn(HBaseRecord.FAMILY_NAME_DATA_BYTES,
            		HBaseRecord.DEFAULT_QUALIFIER_BYTES);
            scan.setTimeStamp(version);
			scanner = table.getScanner(scan);
			Iterator<Result> iter = scanner.iterator();
			while (iter.hasNext()) {
				Result next = iter.next();
				keys.add(next.getRow());
			}
		} catch (IOException e) {
			throw new Exception(e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			closeHTable(table);
		}
		return keys;
	}

	/**
	 * Close the HTable.
	 */
	public void closeHTable(HTableInterface table) {
		try {
			if(table!=null){
				table.close();
			}		
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Return versions between startTs (inclusive) and  endTs (inclusive)
	 * @param key
	 * @param startTs
	 * @param endTs
	 * @return
	 * @throws Exception
	 */
	public List<Long> versions(String key, long startTs, long endTs) throws Exception {
		List<Long> versions = new ArrayList<Long>();

		long relaxedEndTs = relaxEndTs(endTs);
		long relaxedStartTs = relaxStartTs(startTs);

		Get get = new Get(key.getBytes());
		try {
			get.setTimeRange(relaxedStartTs, relaxedEndTs);
		} catch (IOException e1) {
			log.error("Invalid timestamp start " + relaxedStartTs + ", end: "
					+ relaxedEndTs, e1);
			return versions;
		}
		get.setMaxVersions();
		HTableInterface table = getHTable();
		if (table == null) {
			return versions;
		}
		try {
			Result rs = table.get(get);
			if (!rs.isEmpty()) {
				// family -> < qualifier -> <timestamp -> value > >
				NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs
						.getMap();

				// Check family exists
				NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map
						.get(HBaseRecord.FAMILY_NAME_DATA_BYTES);
				if (familyMap != null) {
					// Check value exists before timestamp
					NavigableMap<Long, byte[]> versionMap = familyMap
							.get(HBaseRecord.DEFAULT_QUALIFIER_BYTES);
					if (versionMap != null && !versionMap.isEmpty()) {
						versions.addAll(versionMap.keySet());
					}
				}
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + new String(table.getTableName()) + ":"
					+ key, e);
		} finally {
			closeHTable(table);
		}
		return versions;
	}

	/**
	 * 
	 * @param key
	 * @param startTs
	 * @param endTs
	 * @param family
	 * @param qualifierList
	 * @return
	 * @throws Exception
	 */
	public Map<byte[], List<Long>> versions(String key, long startTs, long endTs, byte[] family, 
			List<byte[]> qualifierList) throws Exception {
		Map<byte[], List<Long>> versions = new HashMap<byte[], List<Long>>();

		long relaxedEndTs = relaxEndTs(endTs);
		long relaxedStartTs = relaxStartTs(startTs);

		Get get = new Get(key.getBytes());
		try {
			get.setTimeRange(relaxedStartTs, relaxedEndTs);
		} catch (IOException e1) {
			log.error("Invalid timestamp start " + relaxedStartTs + ", end: "
					+ relaxedEndTs, e1);
			return versions;
		}
		get.setMaxVersions();
		HTableInterface table = getHTable();
		if (table == null) {
			return versions;
		}
		try {
			Result rs = table.get(get);
			if (!rs.isEmpty()) {
				// family -> < qualifier -> <timestamp -> value > >
				NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs
						.getMap();

				// Check family exists
				NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map
						.get(family);
				if (familyMap != null) {
					// Check value exists before timestamp
					for (byte[] qualifier : qualifierList) {
						NavigableMap<Long, byte[]> versionMap = familyMap
								.get(qualifier);
						if (versionMap != null && !versionMap.isEmpty()) {
							versions.put(qualifier, new ArrayList(versionMap.keySet()));
						}
					}
				}
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + new String(table.getTableName()) + ":"
					+ key, e);
		} finally {
			closeHTable(table);
		}
		return versions;
	}
	
	/**
	 * Return fields between startTs (inclusive) and endTs (inclusive)
	 * @param key
	 * @param startTs
	 * @param endTs
	 * @return
	 * @throws Exception
	 */
	public Map<String, String> fields(String key, long startTs, long endTs) throws Exception {
		Map<String, String> fields = null;

		long relaxedEndTs = relaxEndTs(endTs);
		long relaxedStartTs = relaxStartTs(startTs);

		Get get = new Get(key.getBytes());
		try {
			get.setTimeRange(relaxedStartTs, relaxedEndTs);
		} catch (IOException e1) {
			log.error("Invalid timestamp start " + relaxedStartTs + ", end: "
					+ relaxedEndTs, e1);
			return null;
		}

		get.addFamily(HBaseRecord.FAMILY_NAME_DATA_BYTES);
		HTableInterface table = getHTable();
		if (table == null) {
			return fields;
		}
		try {
			Result rs = table.get(get);
			fields = toFields(rs);
		} catch (IOException e) {
			throw new Exception("Failed to get " + new String(table.getTableName()) + ":"
					+ key, e);
		} finally {
			closeHTable(table);
		}
		return fields;
	}

	private Map<String, String> toFields(Result rs) {
		Map<String, String> fields = new HashMap<String, String>();
		if (rs != null && !rs.isEmpty()) {
			String key = new String(rs.getRow());

			// family -> < qualifier -> <timestamp -> value > >
			NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs
					.getMap();

			// Check family exists
			NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map
					.get(HBaseRecord.FAMILY_NAME_DATA_BYTES);
			if (familyMap == null) {
				log.error("Missing field " + " in " + tablename + ":" + key);
				return null;
			}

			// Check value exists before timestamp
			for (Entry<byte[], NavigableMap<Long, byte[]>> entry : familyMap
					.entrySet()) {

				String column = new String(entry.getKey());

				NavigableMap<Long, byte[]> versions = familyMap.get(entry
						.getKey());
				if (versions == null || versions.isEmpty()
						|| versions.size() != 1) {
					log.error("Missing field " + " in " + tablename + ":" + key);
					return null;
				}

				String value = new String(
						(byte[]) (versions.values().toArray()[0]));
				fields.put(column, value);
			}
		}
		return fields;
	}
	
	/**
	 * Return all versions for all columns between startTs (inclusive) and endTs (inclusive)
	 * @param key
	 * @param startTs
	 * @param endTs
	 * @return
	 * @throws Exception
	 */
	public Map<String, List<Long>> columnVersions(String key, long startTs, long endTs) throws Exception {
		Map<String, List<Long>> columnVersions = new HashMap<String, List<Long>>();

		long relaxedEndTs = relaxEndTs(endTs);
		long relaxedStartTs = relaxStartTs(startTs);

		Get get = new Get(key.getBytes());
		try {
			get.setTimeRange(relaxedStartTs, relaxedEndTs);
		} catch (IOException e1) {
			log.error("Invalid timestamp start " + relaxedStartTs + ", end: "
					+ relaxedEndTs, e1);
			return null;
		}
		get.setMaxVersions();
		get.addFamily(HBaseRecord.FAMILY_NAME_DATA_BYTES);
		HTableInterface table = getHTable();
		if (table == null) {
			return columnVersions;
		}
		try {
			Result rs = table.get(get);
			if (!rs.isEmpty()) {
				// family -> < qualifier -> <timestamp -> value > >
				NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs
						.getMap();

				// Check family exists
				NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map
						.get(HBaseRecord.FAMILY_NAME_DATA_BYTES);
				if (familyMap != null) {
					// Check value exists before timestamp
					for (Entry<byte[], NavigableMap<Long, byte[]>> entry : familyMap
							.entrySet()) {
						NavigableMap<Long, byte[]> versionMap = familyMap
								.get(entry.getKey());
						if (versionMap != null && !versionMap.isEmpty()) {
							columnVersions.put(new String(entry.getKey()), new ArrayList(versionMap.keySet()));
						}
					}
				}
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + new String(table.getTableName()) + ":"
					+ key, e);
		} finally {
			closeHTable(table);
		}
		return columnVersions;
	}
	

	///////////////////////////////////////////////
	// Get Keys
	///////////////////////////////////////////////
	/**
	 * Get keys
	 * 
	 * @param startTs
	 * @param endTs
	 * @return
	 * @throws Exception 
	 */
	public List<String> keys(long startTs, long endTs) throws Exception {
		return keys(startTs, endTs, null);
	}

	/**
	 * Get keys with filters on fields.
	 * 
	 * @param startTs
	 * @param endTs
	 * @param filters
	 * @return
	 * @throws Exception 
	 */
	public List<String> keys(long startTs, long endTs,
			Map<String, String> filters) throws Exception {
		List<String> keys = new ArrayList<String>();

		long relaxedEndTs = relaxEndTs(endTs);
		long relaxedStartTs = relaxStartTs(startTs);

		Scan scan = new Scan();
		HTableInterface table = getHTable();
		if (table == null) {
			return keys;
		}
		ResultScanner scanner = null;
		try {
			scan.setTimeRange(relaxedStartTs, relaxedEndTs);
			if (filters != null && !filters.isEmpty()) {
				List<Filter> list = new ArrayList<Filter>();
				for (Entry<String, String> entry : filters.entrySet()) {
					// Filter out items don't match the mapping.
					Filter filter = new SingleColumnValueFilter(
							HBaseRecord.FAMILY_NAME_DATA_BYTES, entry.getKey()
									.getBytes(), CompareOp.EQUAL, entry
									.getValue().getBytes());
					list.add(filter);
				}
				scan.setFilter(new FilterList(
						FilterList.Operator.MUST_PASS_ALL, list));
			}
			scanner = table.getScanner(scan);
			Iterator<Result> iter = scanner.iterator();
			while (iter.hasNext()) {
				Result next = iter.next();
				keys.add(new String(next.getRow()));
			}
		} catch (IOException e) {

			throw new Exception("Failed to get keys, start: " + relaxedStartTs
					+ ", end: " + relaxedEndTs, e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			closeHTable(table);
		}

		return keys;
	}
	
	public List<String> keys(String startKey, String endKey) throws Exception {
		return keys(startKey,endKey,null);
	}
	
	public List<String> keys(String startKey, String endKey,  Map<String, String> filters) throws Exception {
		List<String> keys = new ArrayList<String>();
		Scan scan = new Scan();
		HTableInterface table = getHTable();
		if (table == null) {
			return keys;
		}
		ResultScanner scanner = null;
		try {
			scan.setStartRow(startKey.getBytes());
			scan.setStopRow(endKey.getBytes());
			if (filters != null && !filters.isEmpty()) {
				List<Filter> list = new ArrayList<Filter>();
				for (Entry<String, String> entry : filters.entrySet()) {
					// Filter out items don't match the mapping.
					Filter filter = new SingleColumnValueFilter(
							HBaseRecord.FAMILY_NAME_DATA_BYTES, entry.getKey()
									.getBytes(), CompareOp.EQUAL, entry
									.getValue().getBytes());
					list.add(filter);
				}
				scan.setFilter(new FilterList(
						FilterList.Operator.MUST_PASS_ALL, list));
			}
			scanner = table.getScanner(scan);
			Iterator<Result> iter = scanner.iterator();
			while (iter.hasNext()) {
				Result next = iter.next();
				keys.add(new String(next.getRow()));
			}
		} catch (IOException e) {
			throw new Exception("Failed to get keys, start: " + startKey + ", end: "
					+ endKey, e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			closeHTable(table);
		}
		return keys;
	}

	public void removeBetweenTimestamp(long startT, long endT) throws Exception {
		log.info(this.tablename + " remove (" + startT + " " + endT + ")");
		List<String> keys = keys(startT, endT);

		HTableInterface table = getHTable();
		List<Delete> deletes = new LinkedList<Delete>();
		try {
			Iterator<String> iter = keys.iterator();
			while (iter.hasNext()) {
				byte[] key = iter.next().getBytes();
				deletes.add(new Delete(key));
			}
			table.delete(deletes);

			log.info(this.tablename + " remove (" + startT + " " + endT + ") "
					+ keys.size());
		} catch (IOException e) {
			throw new Exception(this.tablename + " remove (" + startT + " " + endT + ") ", e);
		} finally {
			closeHTable(table);
		}
	}

	/**
	 * 删除timestamp以前的记录： 适合以timestamp为key的table
	 * 
	 * @param timestamp
	 */

	public void removeBeforeTimestampKey(long timestamp) {
		Scan scan = new Scan();
		HTableInterface table = getHTable();
		scan.setStopRow(String.valueOf(timestamp).getBytes());
		List<Delete> deletes = new LinkedList<Delete>();
		ResultScanner scanner = null;
		try {
			scanner = table.getScanner(scan);
			Iterator<Result> iter = scanner.iterator();
			while (iter.hasNext()) {
				Result ret = iter.next();
				deletes.add(new Delete(ret.getRow()));
			}
			table.delete(deletes);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			closeHTable(table);
		}
	}

	public long incrementColumnValue(String key, String family,
			String qualifier, long amount) throws Exception {
		HTableInterface table = getHTable();
		if (table == null) {
			return 0;
		}

		long c = 0;
		try {
			c = table.incrementColumnValue(key.getBytes(), family.getBytes(),
					qualifier.getBytes(), amount);
		} catch (Exception e) {
			throw new Exception("Failed to incrementColumnValue, key: " + key
					+ ", family: " + family + ", qualifier: " + qualifier
					+ ", amount: " + amount, e);
		} finally {
			closeHTable(table);
		}

		return c;
	}

	public <T> Map<String, String> getFields(String family, String qualifier) throws Exception {
		return getFields(family, qualifier, new StringConvert());
	}

	/**
	 * Get the keyValues for the given family and qualifier Key: rowKey; Value:
	 * column value
	 * 
	 * @param family
	 * @param qualifier
	 * @param convert
	 * @return
	 * @throws Exception 
	 */
	public <T> Map<String, String> getFields(String family, String qualifier,
			BaseConvert convert) throws Exception {
		HTableInterface table = getHTable();
		Map<String, String> map = new HashMap<String, String>();
		if (table == null) {
			return map;
		}

		ResultScanner scanner = null;
		try {
			scanner = table.getScanner(family.getBytes(),
					qualifier.getBytes());
			Iterator<Result> it = scanner.iterator();
			while (it.hasNext()) {
				Result result = it.next();
				byte[] value = result.getValue(family.getBytes(),
						qualifier.getBytes());
				String convertValue = convert.convert(value);
				map.put(new String(result.getRow()), convertValue);
			}

		} catch (Exception e) {
			throw new Exception("Failed to get fields, family: " + family
					+ ", qualifier: " + qualifier, e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			closeHTable(table);
		}

		return map;
	}
	
	/**
	 * 返回的每个cell为一个HBaseRecord对象
	 * @param key
	 * @param qualifiers
	 * @return
	 * @throws Exception
	 */
	public Map<byte[], HBaseRecord> getQualifiers(String key, Map<byte[], List<byte[]>> qualifiers)
			throws Exception{
		Map<byte[], HBaseRecord> recordMap = null;
		if (key == null) {
			return recordMap;
		}
		
		Get get = new Get(key.getBytes());
		if (qualifiers != null && qualifiers.size() > 0) {
			for (Entry<byte[], List<byte[]>> entry : qualifiers.entrySet()) {
				byte[] family = entry.getKey();
				List<byte[]> columns = entry.getValue();
				if (CollectionUtils.isNotEmpty(columns)) {
					for (byte[] column : columns) {
						get.addColumn(family, column);
					}	
				}
			}
		}
		
		HTableInterface table = getHTable();
		if (table == null) {
			return recordMap;
		}
		try {
			Result result = table.get(get);
			if (result != null && !result.isEmpty()) {
				recordMap = HBaseRecord.getRecordList(result, qualifiers);
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + tablename + ":" + key, e);
		} finally {
			closeHTable(table);
		}
		return recordMap;
	}
	
	/**
	 * 返回的每个cell为一个指定版本的HBaseRecord对象
	 * @param key
	 * @param qualifiers
	 * @return
	 * @throws Exception
	 */
	public Map<byte[], HBaseRecord> getQualifiersByVersion(String key, long version, Map<byte[], List<byte[]>> qualifiers)
			throws Exception{
		Map<byte[], HBaseRecord> recordMap = null;
		if (key == null) {
			return recordMap;
		}
		
		Get get = new Get(key.getBytes());
		if (qualifiers != null && qualifiers.size() > 0) {
			for (Entry<byte[], List<byte[]>> entry : qualifiers.entrySet()) {
				byte[] family = entry.getKey();
				List<byte[]> columns = entry.getValue();
				if (CollectionUtils.isNotEmpty(columns)) {
					for (byte[] column : columns) {
						get.addColumn(family, column);
					}	
				}
			}
		}
		get.setTimeStamp(version);//set timestamp
		HTableInterface table = getHTable();
		if (table == null) {
			return recordMap;
		}
		try {
			Result result = table.get(get);
			if (result != null && !result.isEmpty()) {
				recordMap = HBaseRecord.getRecordList(result, qualifiers);
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + tablename + ":" + key, e);
		} finally {
			closeHTable(table);
		}
		return recordMap;
	}
	
	/**
	 * 返回的每个cell为一个HBaseRecord对象,且返回所有版本
	 * @param key
	 * @param qualifiers
	 * @return
	 * @throws Exception
	 */
	public Map<byte[], List<HBaseRecord>> getAllVersionQualifiers(String key, Map<byte[], List<byte[]>> qualifiers)
			throws Exception{
		Map<byte[], List<HBaseRecord>> recordMap = null;
		if (key == null) {
			return recordMap;
		}
		
		Get get = new Get(key.getBytes());
		get.setMaxVersions();
		if (qualifiers != null && qualifiers.size() > 0) {
			for (Entry<byte[], List<byte[]>> entry : qualifiers.entrySet()) {
				byte[] family = entry.getKey();
				List<byte[]> columns = entry.getValue();
				if (CollectionUtils.isNotEmpty(columns)) {
					for (byte[] column : columns) {
						get.addColumn(family, column);
					}	
				}
			}
		}
		
		HTableInterface table = getHTable();
		if (table == null) {
			return recordMap;
		}
		try {
			Result result = table.get(get);
			if (result != null && !result.isEmpty()) {
				recordMap = HBaseRecord.getAllRecordVersionList(result, qualifiers);
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + tablename + ":" + key, e);
		} finally {
			closeHTable(table);
		}
		return recordMap;
	}
	
	public HBaseRecord getQualifiers(String key, Long version, Map<byte[], List<byte[]>> qualifiers)
			throws Exception{
		HBaseRecord record = null;
		if (key == null) {
			return record;
		}
		
		Get get = new Get(key.getBytes());
		if(version!=null){
			get.setTimeStamp(version);
		}
		
		HTableInterface table = getHTable();
		if (table == null) {
			return record;
		}
		try {
			Result result = table.get(get);
			if (result != null && !result.isEmpty()) {
				record = HBaseRecord.fromResult(result, qualifiers);
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + tablename + ":" + key, e);
		} finally {
			closeHTable(table);
		}
		return record;
	}
	
	/**
	 * 
	 * @param startKey
	 * @param stopKey
	 * @param family
	 * @param qualifiers
	 * @param convert
	 * @return
	 * 	key: rowKey
	 *  value: <columnName, value>
	 * @throws Exception
	 * @Deprecated
	 */
	public Map<String, Map<String, String>> getMultiColumn(String startKey, 
			String stopKey, String family, String[] qualifiers, 
			BaseConvert convert) throws Exception {
		HTableInterface table = getHTable();
		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
		if (table == null) {
			return map;
		}

		ResultScanner scanner = null;
		try {
			Scan scan = new Scan();
			scan.setStartRow(startKey.getBytes());
			scan.setStopRow(stopKey.getBytes());
			for (int i = 0; i < qualifiers.length; i++) {
				scan.addColumn(family.getBytes(), qualifiers[i].getBytes());
			}
			scanner = table.getScanner(scan);
			Iterator<Result> it = scanner.iterator();
			while (it.hasNext()) {
				Result result = it.next();
				Map<String, String> columns = new HashMap<String, String>();
				map.put(new String(result.getRow()), columns);
				for (int i = 0; i < qualifiers.length; i++) {
					byte[] value = result.getValue(family.getBytes(),
							qualifiers[i].getBytes());
					if (value != null) {
						String convertValue = convert.convert(value);
						columns.put(qualifiers[i], convertValue);
					}
				}
				
			}

		} catch (Exception e) {
			throw new Exception("Failed to get fields, family: " + family
					+ ", qualifiers: ", e);
		} finally {
			if (scanner != null) {
				scanner.close();
			}
			closeHTable(table);
		}

		return map;
	}

	void flush() throws Exception {
		HTableInterface table = getHTable();		
		
		if (table == null) {
			return ;
		}
		
		try {
			table.flushCommits();

		} catch (Exception e) {
			throw new Exception("Failed to flush table", e);
		} finally {
			closeHTable(table);
		}
	}

	/**
	 * Get rows by the specified range, limited by pageSize.
	 * 
	 * @param startRow
	 * @param stopRow
	 * @param pageSize
	 * @deprecated
	 * @return
	 * @throws Exception 
	 */
	public List<HBaseRecord> getRangeRow(String startRow, String stopRow,
			long pageSize) throws Exception {
		List<HBaseRecord> records = new ArrayList<HBaseRecord>();

		HTableInterface table = getHTable();
		if (table == null) {
			return records;
		}
		try {
			Scan scan = new Scan(startRow.getBytes(), stopRow.getBytes());
			PageFilter pf = new PageFilter(pageSize);
			scan.setFilter(pf);
			ResultScanner results = table.getScanner(scan);
			if (results != null) {
				for (Result rs : results) {
					if (!rs.isEmpty()) {
						HBaseRecord record = HBaseRecord.fromResult(rs);
						if (record != null) {
							records.add(record);
						} else {
							log.error("Failed to get record " + rs);
						}
					}
				}
			}
		} catch (IOException e) {
			throw new Exception("Failed to get " + tablename + ":[" + startRow + ", "
					+ stopRow + "]", e);			
		} finally {
			closeHTable(table);
		}
		return records;
	}
	
	/**
	 * 
	 * @param startRow
	 * @param stopRow
	 * @return
	 */
	public HBaseRecordScanner getHBaseScanner(String startRow, String stopRow)
			throws Exception{
		return getHBaseScanner(startRow, stopRow, null, null);
	}
	
	/**
	 * 
	 * @param startRow
	 * @param stopRow
	 * @param timestampe
	 * @return
	 * @throws Exception
	 */
	public HBaseRecordScanner getHBaseScanner(String startRow, String stopRow,
			Long timestampe)
			throws Exception{
		return getHBaseScanner(startRow, stopRow, timestampe, null);
	}
	
	/**
	 * 获取scanner，避免直接获取结果集List产生的长时间等待
	 * @param startRow
	 * @param stopRow
	 * @return
	 * @throws Exception
	 */
	public HBaseRecordScanner getHBaseScanner(String startRow, String stopRow,
			Long timestamp, Map<byte[], List<byte[]>> qualifiers) throws Exception {
		if(timestamp==null){
			return getHBaseScanner(startRow, stopRow, 0L,Long.MAX_VALUE, qualifiers);
		}else{
			return getHBaseScanner(startRow, stopRow, timestamp,timestamp+1, qualifiers);
		}
	}
	
	public HBaseRecordScanner getHBaseScanner(String startRow, String stopRow,
			Long timestamp, Map<byte[], List<byte[]>> qualifiers, Integer caching) throws Exception {
		if(timestamp==null){
			return getHBaseScanner(startRow, stopRow, 0L,Long.MAX_VALUE, qualifiers, caching);
		}else{
			return getHBaseScanner(startRow, stopRow, timestamp,timestamp+1, qualifiers, caching);
		}
	}
	
	public HBaseRecordScanner getHBaseScanner(String startRow, String stopRow,
			Long minStamp,Long maxStamp, Map<byte[], List<byte[]>> qualifiers) throws Exception {
		HBaseRecordScanner scanner = null;
		HTableInterface table = getHTable();
		if (table == null) {
			return scanner;
		}
		try {
			Scan scan=new Scan();
			if(startRow!=null) scan.setStartRow(startRow.getBytes());
			if(stopRow!=null) scan.setStopRow(stopRow.getBytes());
			if(minStamp==null) minStamp=0l;
			if(maxStamp==null) maxStamp=Long.MAX_VALUE;
			scan.setTimeRange(minStamp, maxStamp);
			scan.setBatch(1000);
			scan.setCaching(1000);
			ResultScanner results = table.getScanner(scan);
			scanner = new HBaseRecordScanner(results, qualifiers);
		} catch (IOException e) {
			throw new Exception("Failed to get " + tablename + ":[" + startRow + ", "
					+ stopRow + "]", e);			
		} finally {
			closeHTable(table);
		}
		
		return scanner;
	}
	
	public HBaseRecordScanner getHBaseScanner(String startRow, String stopRow,
			Long minStamp,Long maxStamp, Map<byte[], List<byte[]>> qualifiers, Integer caching) throws Exception {
		HBaseRecordScanner scanner = null;
		HTableInterface table = getHTable();
		if (table == null) {
			return scanner;
		}
		try {
			Scan scan=new Scan();
			if(startRow!=null) scan.setStartRow(startRow.getBytes());
			if(stopRow!=null) scan.setStopRow(stopRow.getBytes());
			if(minStamp==null) minStamp=0l;
			if(maxStamp==null) maxStamp=Long.MAX_VALUE;
			scan.setTimeRange(minStamp, maxStamp);
			scan.setBatch(1000);
			if (caching == null) {
				scan.setCaching(1000);
			} else {
				scan.setCaching(caching);
			}
			ResultScanner results = table.getScanner(scan);
			scanner = new HBaseRecordScanner(results, qualifiers);
		} catch (IOException e) {
			throw new Exception("Failed to get " + tablename + ":[" + startRow + ", "
					+ stopRow + "]", e);			
		} finally {
			closeHTable(table);
		}
		
		return scanner;
	}
	
	/**
	 * 计算某范围内的数据条数
	 * @param startRow
	 * @param stopRow
	 * @return
	 * @throws Throwable 
	 */
	public long getRowCount(String startRow, String stopRow) throws Throwable{
		HTableInterface table = getHTable();
		long count = 0;
		if (table == null) {
			return count;
		}
		
		try {
			final RowCountProtos.CountRequest request = RowCountProtos.CountRequest.getDefaultInstance();
			
			Map<byte[],Long> results = table.coprocessorService(RowCountService.class,
					startRow.getBytes(), stopRow.getBytes(),
			        new Batch.Call<RowCountProtos.RowCountService,Long>() {
			          public Long call(RowCountProtos.RowCountService counter) throws IOException {
			            ServerRpcController controller = new ServerRpcController();
			            BlockingRpcCallback<RowCountProtos.CountResponse> rpcCallback =
			                new BlockingRpcCallback<RowCountProtos.CountResponse>();
			            counter.getRowCount(controller, request, rpcCallback);
			            RowCountProtos.CountResponse response = rpcCallback.get();
			            if (controller.failedOnException()) {
			              throw controller.getFailedOn();
			            }
			            return (response != null && response.hasCount()) ? response.getCount() : 0;
			          }
			        });
			Iterator<Long> it = results.values().iterator();
			while (it.hasNext()) {
				count += it.next();
			}
		} catch(Exception t) {
			throw new Exception("Failed to count row " + tablename + ":[" + startRow + ", "
					+ stopRow + "], please check the coprocessor", t);
		} finally {
			closeHTable(table);
		}
		
		return count;
	}
}
