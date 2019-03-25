package com.yihaodian.store.hbasestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import org.junit.Test;

public class HbaseStoreTest {
	public static final String[] FAMILYS = new String[]{"family0"};
	public static final byte[] FAMILY0 = "family0".getBytes();
	public static final byte[] FQ0 = "tag".getBytes();
	public static final byte[] FQ1 = "tag1".getBytes();
	
	@Test
	public void testCustomQualifiers() throws Exception{
		String className = HbaseStoreTest.class.getCanonicalName();
		String name = className + "-testCustomQualifiers";
		HBaseStore store = HBaseStoreManager.getInstance().getStore(name, FAMILYS);
		
		Map<byte[], List<byte[]>> qualifiers = new HashMap<byte[], List<byte[]>>();
		List<byte[]> fQuals = new ArrayList<byte[]>();
		fQuals.add(FQ0);
		fQuals.add(FQ1);
		qualifiers.put(FAMILY0, fQuals);

		// Save at ts 1
		String key = "12306";
		Long ts = System.currentTimeMillis();
		Map<byte[], Map<byte[], byte[]>> qualifierDatas = new HashMap<byte[], Map<byte[],byte[]>>();
		qualifierDatas.put(FAMILY0, new HashMap<byte[], byte[]>());
		qualifierDatas.get(FAMILY0).put(FQ0, "t1".getBytes());
		qualifierDatas.get(FAMILY0).put(FQ1, "t11".getBytes());
		HBaseRecord record = new HBaseRecord(key, ts, qualifierDatas);
		store.put(record);

		record = store.getQualifiers(key, null, qualifiers);
		Map<byte[], Map<byte[], byte[]>> values = record.getQualifierDatas();
		
		Assert.assertTrue("t1".equals(new String(values.get(FAMILY0).get(FQ0))));
		Assert.assertTrue("t11".equals(new String(values.get(FAMILY0).get(FQ1))));
		
		// Save at ts 2
		String key2 = "12307";
		long ts2 = System.currentTimeMillis() + 1;
		Map<byte[], Map<byte[], byte[]>> qualifierDatas2 = new HashMap<byte[], Map<byte[],byte[]>>();
		qualifierDatas2.put(FAMILY0, new HashMap<byte[], byte[]>());
		qualifierDatas2.get(FAMILY0).put(FQ0, "t2".getBytes());
		qualifierDatas2.get(FAMILY0).put(FQ1, "t21".getBytes());
		HBaseRecord record2 = new HBaseRecord(key2, ts2, qualifierDatas2);
		store.put(record2);

		record2 = store.getQualifiers(key2, null, qualifiers);
		values = record2.getQualifierDatas();
		Assert.assertTrue("t2".equals(new String(values.get(FAMILY0).get(FQ0))));
		Assert.assertTrue("t21".equals(new String(values.get(FAMILY0).get(FQ1))));
		
		HBaseStoreManager.getInstance().removeStore(name);
	}
	
	private Map<String, Map<String, String>> convertToReadable(
			Map<byte[], Map<byte[], byte[]>> qualifierDatas){
		Map<String, Map<String, String>> readableDatas = new HashMap<String, Map<String,String>>();
		for (Entry<byte[], Map<byte[], byte[]>> familyEntry : qualifierDatas.entrySet()) {
			byte[] familyName = familyEntry.getKey();
			Map<byte[], byte[]> qualifiers = familyEntry.getValue();
			readableDatas.put(new String(familyName), new HashMap<String, String>());
			for (Entry<byte[], byte[]> qualifierEntry : qualifiers.entrySet()) {
				readableDatas.get(new String(familyName)).put(
						new String(qualifierEntry.getKey()), new String(qualifierEntry.getValue()));
			}
		}
		
		return readableDatas;
	}
}
