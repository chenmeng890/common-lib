package com.yihaodian.store.hbasestore;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;

/**
 * 
 * this class can modify table;
 * 
 * @author zengfenghua
 *
 */
public class HTableModifyTest {
	
	public static void main(String[] args) throws MasterNotRunningException, ZooKeeperConnectionException, IOException{
		String _hbaseQuorums = "10.4.11.42,10.4.11.43,10.4.11.44";
		Configuration conf=HBaseUtil.createConf(_hbaseQuorums, 2181);
		HBaseAdmin hbaseAdmin=new HBaseAdmin(conf);
		HTableDescriptor htd=hbaseAdmin.getTableDescriptor("search_product_total_num".getBytes());
		for(int i=0;i<htd.getColumnFamilies().length;i++){
			HColumnDescriptor hcd=htd.getColumnFamilies()[i];
			System.out.println(hcd.getNameAsString());
		}
	}

}
