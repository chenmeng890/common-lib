package com.yihaodian.store.hbasestore;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

public class HbaseTableToolTest {
	public static void main(String[] args) throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
		HBaseTableTool hTableTool = HBaseTableTool.getInstance();
		HTable hTable = hTableTool.getHtable("search_product");
		Result result = hTableTool.getResult(hTable, "1000022_1");
		if(result != null){
			byte[] name = result.getValue(Bytes.toBytes("0"), Bytes.toBytes("special_tag"));
			
			byte[] code = result.getValue(Bytes.toBytes("0"), Bytes.toBytes("product_code"));
			
			System.out.println(Bytes.toString(name));
			System.out.println(Bytes.toString(code));
		}
		
		
		System.out.println("------------------------------------");
		List<Result> list = hTableTool.getResultList("search_product");
		if (list != null && list.size() > 0) {
			for (Result result2 : list.subList(0, 50)) {
				byte[] name = result2.getValue(Bytes.toBytes("0"), Bytes.toBytes("special_tag"));
				
				byte[] code = result2.getValue(Bytes.toBytes("0"), Bytes.toBytes("product_code"));
				
				System.out.println(Bytes.toString(name));
				System.out.println(Bytes.toString(code));
			}
		}
		
	}
	
}
