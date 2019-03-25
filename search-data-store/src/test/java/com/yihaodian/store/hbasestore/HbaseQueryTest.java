package com.yihaodian.store.hbasestore;

import java.util.Date;
import java.util.NavigableMap;

import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.junit.Test;

import com.google.gson.Gson;

public class HbaseQueryTest {
//	private static final String _zk_quorums = "10.161.166.211,10.161.166.212,10.161.166.213";
	private static final String _zk_quorums = "10.4.11.44,10.4.11.45,10.4.11.46,10.4.11.47,10.4.11.48";
	HBaseObjectStore<Object> hbaseObjectStore;
	String tableName = "search_pm_info";
	private static final Gson gson = new Gson();
	
	@Test
	public void test() throws Exception {
		HBaseStore store = HBaseStoreManager.getInstance(_zk_quorums).getStore(tableName, false);
		HTableInterface table = store.getHTable();
		
		Scan scan=new Scan();
		String key = "41280625_";
		scan.setStartRow(key.getBytes());
		scan.setStopRow((key + "_").getBytes());
		long minStamp= 0l;
		long maxStamp= Long.MAX_VALUE;
		scan.setTimeRange(minStamp, maxStamp);
		scan.setBatch(1000);
		scan.setCaching(1000);
		scan.setMaxVersions(10);
		ResultScanner results = table.getScanner(scan);
		Result rs = null;
		while ((rs = results.next()) != null) {
			if (!rs.isEmpty()) {
				NavigableMap<byte[], NavigableMap<byte[], NavigableMap<Long, byte[]>>> map = rs.getMap();
				for(byte[] family : map.keySet()) {
					System.out.println(new String(family));
				}
				byte[] row = rs.getRow();
				String rowId = new String(row);
				NavigableMap<byte[], NavigableMap<Long, byte[]>> familyMap = map.get("0".getBytes());
				for(byte[] family : familyMap.keySet()) {
					System.out.println(new String(family));
					NavigableMap<Long, byte[]> versionMap = familyMap.get(family);
					for (Long ts : versionMap.keySet()) {
						Date d = new Date(ts);
						System.out.println(d);
					}
					System.out.println();
				}
			}
		}
	}
}
