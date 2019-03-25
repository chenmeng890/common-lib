package com.yihaodian.store.hbasestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.log4j.Logger;

/**
 * 
 * htable 尽量只包含一个cf,因为region compaction is to a region
 * 尽量保持column family和column尽量短,因为他们会重复上千万次
 * 尽量不要每次都使用FAMILY_NAME_DATA.getBytes()而是使用
 * FAMILY_NAME_DATA_BYTES，使用上千万次的toBytes()也会占用大量的cpu时间
 */
public class HBaseRecord {
	public static final String FAMILY_NAME_DATA="data";

	public static final byte[] FAMILY_NAME_DATA_BYTES=FAMILY_NAME_DATA.getBytes();
	
	public static final byte[] DEFAULT_QUALIFIER_BYTES="0".getBytes();
	
	public static final Logger log = Logger.getLogger(HBaseRecord.class);
	
	private String key;
	private byte[] data;
	private Long timestamp;
	/**
	 * Custom qualifiers.
	 * family -> < qualifier, value > 
	 */
	private Map<byte[], Map<byte[], byte[]>> qualifierDatas;
	
	public HBaseRecord(String key, byte[] data) {
		this(key,data,System.currentTimeMillis());
	}

    /**
     * constructor HBaseRecord
     * @param key
     * @param data
     * @param timestamp
     */
	public HBaseRecord(String key, byte[] data, Long timestamp) {
		this(key, data, timestamp, null);
	}
	
	/**
	 * 
	 * @param key
	 * @param timestamp
	 * @param fields
	 */
	public HBaseRecord(String key, Long timestamp, Map<byte[], Map<byte[], byte[]>> qualifierDatas) {
		this(key, null, timestamp, qualifierDatas);
	}
	
	/**
	 * constructor HBaseRecord
	 * @param key
	 * @param data
	 * @param timestamp
	 * @param fields
	 */
	public HBaseRecord(String key, byte[] data, Long timestamp, Map<byte[], Map<byte[], byte[]>> qualifierDatas) {
		this.key = key;
		this.data = data;
		this.timestamp = timestamp;
		this.qualifierDatas = qualifierDatas;
	}
	
	public HBaseRecord(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}

	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}	
	
	public Map<byte[], Map<byte[], byte[]>> getQualifierDatas() {
		return qualifierDatas;
	}

	/**
	 * htable 尽量只包含一个cf,因为region compaction is to a region
	 * @return 
	 */
	public static String[] getFamily(){
		return new String[]{FAMILY_NAME_DATA};
	}
		
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + ((qualifierDatas == null) ? 0 : qualifierDatas.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HBaseRecord other = (HBaseRecord) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		if (qualifierDatas == null) {
			if (other.qualifierDatas != null)
				return false;
		} else if (!qualifierDatas.equals(other.qualifierDatas))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HBaseRecord [key=" + key + ", data=" + Arrays.toString(data)
				+ ", timestamp=" + timestamp + ", qualifierDatas="
				+ qualifierDatas + "]";
	}

	/**
	 * toPut() 
	 * htable 尽量只包含一个cf,因为region compaction is to a region
	 * @return Put
	 */
	public Put toPut() {
		Put put = new Put(key.getBytes(), timestamp);
		if (data != null) {
			put.add(FAMILY_NAME_DATA_BYTES, DEFAULT_QUALIFIER_BYTES, data);
		} else {
			if(qualifierDatas != null && !qualifierDatas.isEmpty()) {
				for (Entry<byte[], Map<byte[], byte[]>> family : qualifierDatas.entrySet()) {
					byte[] familyName = family.getKey();
					Map<byte[], byte[]> qualifiers = family.getValue();
					for (Entry<byte[], byte[]> qualifier : qualifiers.entrySet()) {
						byte[] qualifierName = qualifier.getKey();
						byte[] value = qualifier.getValue();
						put.add(familyName, qualifierName, value);
					}
				}
			}
		}
		
		

		return put;
	}
	
	public static HBaseRecord fromResult(Result rs){
		return fromResult(rs, null);
	}
	
	public static HBaseRecord fromResult(Result rs, Map<byte[], List<byte[]>> qualifiers){
		HBaseRecord record = null;
		if (rs != null && !rs.isEmpty()) {
			String key = new String(rs.getRow());

			// family -> < qualifier -> <timestamp -> value > >
			NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs
					.getMap();

			// Check family exists

			// default qualifier
			if (qualifiers == null) {
				NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map
						.get(HBaseRecord.FAMILY_NAME_DATA_BYTES);
				if (familyMap == null) {
					log.error("Missing field: "  + key);
					return record;
				}
				// Check value exists before timestamp
				NavigableMap<Long, byte[]> versions = familyMap
						.get(HBaseRecord.DEFAULT_QUALIFIER_BYTES);
				if (versions == null || versions.isEmpty() || versions.size() != 1) {
					log.error("Missing field: " + key);
					return record;
				}
				byte[] value = (byte[]) (versions.values().toArray()[0]);
				long ts = (Long) (versions.keySet().toArray()[0]);
				if (value != null) {
					record = new HBaseRecord(key, value, ts);
				}
			} else {  // Custom qualifiers
				long ts = 1l;
				Map<byte[], Map<byte[], byte[]>> qualDatas = new HashMap<byte[], Map<byte[],byte[]>>();
				for (Entry<byte[], List<byte[]>> family : qualifiers.entrySet()) {
					// Get faimly and init qualifier values.
					byte[] familyName = family.getKey();
					qualDatas.put(familyName, new HashMap<byte[], byte[]>());
					NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map
							.get(familyName);
					if (familyMap == null) {
						log.error("Missing family: "  + key + ", family: " + new String(familyName));
						continue;
					}
					
					List<byte[]> quals = family.getValue();
					for (byte[] qual : quals) {
						NavigableMap<Long, byte[]> versions = familyMap
								.get(qual);
						if (versions == null || versions.isEmpty() || versions.size() != 1) {
							log.error("Missing qualifiers: "  + key + ", family: " + new String(familyName)
									+ ", qualifier: " + new String(qual));
							continue;
						}
						byte[] value = (byte[]) (versions.values().toArray()[0]);
						ts = (Long) (versions.keySet().toArray()[0]);
						if (value != null) {
							// Put values to qualifier.
							qualDatas.get(familyName).put(qual, value);
						}
					}
				}
				record = new HBaseRecord(key, ts, qualDatas);
			}
		}

		return record;
	}
	
	public static Map<byte[], HBaseRecord> getRecordList(Result rs, Map<byte[], List<byte[]>> qualifiers){
		Map<byte[], HBaseRecord> recordMap = new HashMap<byte[], HBaseRecord>();
		if (rs != null && !rs.isEmpty()) {
			String key = new String(rs.getRow());
			// family -> < qualifier -> <timestamp -> value > >
			NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs
					.getMap();

			if (qualifiers == null) {
				log.error("qualifiers is null");
				return recordMap;
			} else {  // Custom qualifiers
				long ts = 1l;
				for (Entry<byte[], List<byte[]>> family : qualifiers.entrySet()) {
					// Get faimly and init qualifier values.
					byte[] familyName = family.getKey();
					NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map
							.get(familyName);
					if (familyMap == null) {
						log.error("Missing family: "  + key + ", family: " + new String(familyName));
						continue;
					}
					
					List<byte[]> quals = family.getValue();
					for (byte[] qual : quals) {
						NavigableMap<Long, byte[]> versions = familyMap
								.get(qual);
						if (versions == null || versions.isEmpty() || versions.size() != 1) {
							continue;
						}
						byte[] value = (byte[]) (versions.values().toArray()[0]);
						ts = (Long) (versions.keySet().toArray()[0]);
						if (value != null) {
							HBaseRecord record =  new HBaseRecord(key, value, ts);
							recordMap.put(qual, record);
						}
					}
				}
			}
		}
		return recordMap;
	}
	
	public static Map<byte[], List<HBaseRecord>> getAllRecordVersionList(Result rs, Map<byte[], List<byte[]>> qualifiers){
		Map<byte[], List<HBaseRecord>> recordMap = new HashMap<byte[], List<HBaseRecord>>();
		if (rs != null && !rs.isEmpty()) {
			String key = new String(rs.getRow());
			// family -> < qualifier -> <timestamp -> value > >
			NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs
					.getMap();

			if (qualifiers == null) {
				log.error("qualifiers is null");
				return recordMap;
			} else {  // Custom qualifiers
				long ts = 1l;
				for (Entry<byte[], List<byte[]>> family : qualifiers.entrySet()) {
					// Get faimly and init qualifier values.
					byte[] familyName = family.getKey();
					NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map
							.get(familyName);
					if (familyMap == null) {
						log.error("Missing family: "  + key + ", family: " + new String(familyName));
						continue;
					}
					
					List<byte[]> quals = family.getValue();
					for (byte[] qual : quals) {
						NavigableMap<Long, byte[]> versions = familyMap
								.get(qual);
						if (versions == null || versions.isEmpty()) {
							log.error("Missing qualifiers: "  + key + ", family: " + new String(familyName)
									+ ", qualifier: " + new String(qual));
							continue;
						}
						List<HBaseRecord> recordList = new ArrayList<HBaseRecord>();
						for (Entry<Long, byte[]> entry : versions.entrySet()) {
							byte[] value = entry.getValue();
							if (value != null) {
								ts = (Long) (entry.getKey());
								HBaseRecord record =  new HBaseRecord(key, entry.getValue(), ts);
								recordList.add(record);
							}
						}
						recordMap.put(qual, recordList);
					}
				}
			}
		}
		return recordMap;
	}
}
