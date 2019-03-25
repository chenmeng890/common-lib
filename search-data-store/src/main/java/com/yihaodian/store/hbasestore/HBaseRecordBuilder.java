package com.yihaodian.store.hbasestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.yihaodian.common.serializer.YSerializer;
import com.yihaodian.common.serializer.impl.KryoSerializer;

public class HBaseRecordBuilder<T> {
	private static Logger log = Logger.getLogger(HBaseRecordBuilder.class);
	private YSerializer _serializer;
	private Class clazz;
	// Don't compress during serialization to save CPU time. 
	private boolean deflate = false;
	
	public HBaseRecordBuilder(Class<T> clazz){
		this.clazz = clazz;
		_serializer = new KryoSerializer(deflate);
	}
	
	/**
	 * convert to {@link HBaseRecord}
	 * @param key
	 * @param obj
	 * @return <code>HBaseRecord</code>
	 */
	public HBaseRecord toHBaseRecord(String key, T obj){
		return toHBaseRecord(key, obj, 	System.currentTimeMillis());
	}
	
	/**
	 * convert to {@link HBaseRecord}
	 * @param key
	 * @param obj
	 * @param timestamp
	 * @return
	 */
	public HBaseRecord toHBaseRecord(String key, T obj, Long timestamp){
		byte[] data = _serializer.toBytes(obj);
		HBaseRecord record = new HBaseRecord(key, data, timestamp);
		return record;
	}
	
	public HBaseRecord toHBaseRecord(String key, T obj, Long timestamp, Map<byte[], Map<byte[], byte[]>> qualifiers){
		byte[] data = _serializer.toBytes(obj);
		HBaseRecord record = new HBaseRecord(key, data, timestamp, qualifiers);
		return record;
	}
	
	public T fromHBaseRecord(HBaseRecord record){
		return (T) _serializer.fromBytes(clazz, record.getData());		
	}
	
	public Object fromHBaseRecord(HBaseRecord record, Class clazz){
		return _serializer.fromBytes(clazz, record.getData());		
	}
	
	public List<Object> fromQualifierDatas(HBaseRecord record, Class clazz){
		List<Object> list = new ArrayList<Object>();
		Map<byte[], Map<byte[], byte[]>> qualifierDatas = record.getQualifierDatas();
		if (qualifierDatas != null && !qualifierDatas.isEmpty()) {
			for (Entry<byte[], Map<byte[], byte[]>> familyEntry : qualifierDatas.entrySet()) {
				Map<byte[], byte[]> familyValues = familyEntry.getValue();
				if (familyValues != null && !familyValues.isEmpty()) {
					for (Entry<byte[], byte[]> qualifierEntry : familyValues.entrySet()) {
						if (qualifierEntry.getValue() != null) {
							list.add((Object) _serializer.fromBytes(clazz, qualifierEntry.getValue()));
						}
					}
				}
			}
		}
		return list;
	}
		
}
