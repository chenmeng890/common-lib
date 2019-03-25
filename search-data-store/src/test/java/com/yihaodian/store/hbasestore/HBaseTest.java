/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.yihaodian.store.hbasestore;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Scan;

import com.yihaodian.store.zk.ZKUtil;

/**
 *
 */
public class HBaseTest {

	public static Configuration getConfig(String _hbaseQuorums) {
		Configuration HBASE_CONFIG = new Configuration();
		HBASE_CONFIG.set("hbase.zookeeper.quorum", _hbaseQuorums);
		HBASE_CONFIG.set("hbase.zookeeper.property.clientPort", "2181");
		Configuration conf = HBaseConfiguration.create(HBASE_CONFIG);

		return conf;
	}

	public static HBaseStore getStore(String _hbaseQuorums, String table) {
		Configuration conf = getConfig(_hbaseQuorums);
		HTablePool tablePool = new HTablePool(conf, Integer.MAX_VALUE);
		HTableInterface htable = tablePool.getTable(table);

		HBaseStore store = new HBaseStore(table, tablePool);
		return store;
	}

	public static void list(String _hbaseQuorums) throws Exception {
		Configuration conf = getConfig(_hbaseQuorums);

		HBaseAdmin admin = new HBaseAdmin(conf);

		HTableDescriptor[] tables = admin.listTables();
		for (HTableDescriptor table : tables) {
			//truncate
//			admin.disableTable(table.getName());
//			admin.deleteTable(table.getName());
//			admin.createTable(table);

			System.out.println(table.getNameAsString());
		}

		System.out.println(admin);
	}

	public static void main(String[] args) throws Exception {
		String _hbaseQuorums = "10.4.11.42,10.4.11.43,10.4.11.44";
		Configuration conf=HBaseUtil.createConf(_hbaseQuorums, 2181);
		HBaseAdmin hbaseAdmin=new HBaseAdmin(conf);
		HTableDescriptor htd=hbaseAdmin.getTableDescriptor("ProductIndexable_full-staging".getBytes());
		HColumnDescriptor hcd=htd.getFamily("data".getBytes());
		hcd.setMaxVersions(5);
		hbaseAdmin.modifyTable("ProductIndexable_full-staging", htd);
	}
}
