package com.yihaodian.store.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

/**
 * This class is not needed anymore,
 * clients can use <code>HBaseStore</code>
 * @author zengfenghua
 * @deprecated
 */
public class HBaseUtil {

	static Logger log = Logger.getLogger(HBaseUtil.class);
	public static final String quorums_dev = "192.168.130.107,192.168.130.108,192.168.130.109";
	public static final String quorums_stg = "10.63.0.154,10.63.0.155,10.63.0.156";
	public static final String quorums_prod = "10.3.0.66,10.3.0.68,10.2.1.126";
	private Configuration conf = null;
	private HTablePool tablePool = null;
	private String quorums;

	public HBaseUtil(String quorums) {
		this.quorums = quorums;
		init();
	}

	private void init() {
		Configuration HBASE_CONFIG = new Configuration();
		HBASE_CONFIG.set("hbase.zookeeper.quorum", quorums);
		HBASE_CONFIG.set("hbase.zookeeper.property.clientPort", "2181");
		conf = HBaseConfiguration.create(HBASE_CONFIG);
		tablePool = new HTablePool(conf, Integer.MAX_VALUE);
	}

	private HBaseAdmin getHBaseAdmin() {
		HBaseAdmin admin = null;
		try {
			admin = new HBaseAdmin(conf);
		} catch (Exception e) {
			synchronized (this) {
				try {
					admin = new HBaseAdmin(conf);
				} catch (Exception e1) {
					log.error("new HBaseAdmin(conf) ", e1);
					init();
					try {
						admin = new HBaseAdmin(conf);
					} catch (Exception e2) {
						log.error("new HBaseAdmin(conf) ", e2);
					}
				}
			}
		}
		return admin;
	}
	
	// 建表
	public void creatTable(String tableName, String[] familys) throws Exception {
		HBaseAdmin admin = getHBaseAdmin();
		if (admin.tableExists(tableName)) {
			log.info("table already exists! " + tableName);
		} else {
			HTableDescriptor tableDesc = new HTableDescriptor(tableName);
			if (familys == null || familys.length <= 0) {
				return;
			}
			for (int i = 0; i < familys.length; i++) {
				tableDesc.addFamily(new HColumnDescriptor(familys[i]));
			}
			admin.createTable(tableDesc);
			log.info("create table " + tableName + " ok.");
		}

	}

	public void creatTable(String tableName, Collection<String> familys)
			throws Exception {
		HBaseAdmin admin = getHBaseAdmin();
		if (admin.tableExists(tableName)) {
			log.info("table already exists! " + tableName);
		} else {
			HTableDescriptor tableDesc = new HTableDescriptor(tableName);
			if (familys == null || familys.size() <= 0) {
				return;
			}
			for (String family : familys) {
				tableDesc.addFamily(new HColumnDescriptor(family));
			}
			admin.createTable(tableDesc);
			log.info("create table " + tableName + " ok.");
		}
	}

	// 删除�?
	public void deleteTable(String tableName) throws Exception {
		HBaseAdmin admin = getHBaseAdmin();
		admin.disableTable(tableName);
		admin.deleteTable(tableName);
		log.info("delete table " + tableName + " ok.");
	}

	// 添加记录
	public void addRecord(String tableName, String rowKey, String family,
			String qualifier, byte[] value) throws IOException {
		HTableInterface table = tablePool.getTable(tableName);
		Put put = new Put(Bytes.toBytes(rowKey));
		put.add(Bytes.toBytes(family), Bytes.toBytes(qualifier), value);
		try {
			table.put(put);
		}catch(Exception e){
			synchronized (this) {
				try{
					table.put(put);
				}catch(Exception e1){
					init();
					table.put(put);
				}		
			}
		} 
		finally {	
			if (table != null) {
				table.close();
				tablePool.putTable(table);
			}
		}

	}

	// 删除�?��记录
	public void delRecord(String tableName, String rowKey) throws IOException {	 
		HTableInterface table = tablePool.getTable(tableName);
		List<Delete> list = new ArrayList<Delete>();
		Delete del = new Delete(rowKey.getBytes());
		list.add(del);
		try {		
			table.delete(list);
		}catch(Exception e){
			synchronized (this) {
				try{
					table.delete(list);
				}catch(Exception e1){
					init();
					table.delete(list);
				}		
			}
		} 
		finally {
			if (table != null) {
				table.close();
			}
		}
	}

	// 查找�?��记录
	public Result getOneRecord(String tableName, String rowKey)
			throws IOException {
		HTableInterface table = tablePool.getTable(tableName);
		Get get = new Get(rowKey.getBytes());
		Result rs=null;
		try {
			rs = table.get(get);		
		}catch(Exception e){
			synchronized (this) {
				try{
					rs = table.get(get);
				}catch(Exception e1){
					init();
					rs = table.get(get);
				}		
			}
		}
		finally {
			if (table != null) {
				table.close();
			}
		}
		if (rs!=null && rs.isEmpty()) {
			return null;
		}
		return rs;
	}

	public List<byte[]> getResultByRowKey(String tableName, String rowKey)
			throws IOException {
		List<byte[]> resultList = null;
		HTableInterface table = tablePool.getTable(tableName);
		Get get = new Get(rowKey.getBytes());
		Result rs=null;
		try {
			rs = table.get(get);			
		}catch(Exception e){
			synchronized (this) {
				try{
					rs = table.get(get);
				}catch(Exception e1){
					init();
					rs = table.get(get);
				}		
			}
		} finally {
			if (table != null) {
				table.close();
			}
		}
		if (rs!=null && !rs.isEmpty()) {
			resultList = new LinkedList<byte[]>();
			for (KeyValue kv : rs.raw()) {
				resultList.add(kv.getValue());
			}
		}
		return resultList;
	}

	public byte[] getOneRecordOneValue(String tableName, String rowKey)
			throws IOException {
		HTableInterface table = tablePool.getTable(tableName);

		Get get = new Get(rowKey.getBytes());
		Result rs=null;
		try {
		    rs = table.get(get);		    
		} catch(Exception e){
			synchronized (this) {
				try{
					rs = table.get(get);
				}catch(Exception e1){
					init();
					rs = table.get(get);
				}		
			}
		}finally {
			if (table != null) {
				table.close();
			}
		}
		if (rs!=null && !rs.isEmpty()) {
			for (KeyValue kv : rs.raw()) {
				return kv.getValue();
			}
		}
		return null;
	}

	// 返回�?��结果
	public ResultScanner getAllRecord(String tableName) throws IOException {
		Scan s = new Scan();
		ResultScanner ss=null;
		HTable table=null;
		try{
			table = new HTable(conf, tableName);
			ss = table.getScanner(s);
		}catch(Exception e){
			synchronized (this) {
				try{
					table = new HTable(conf, tableName);
					ss = table.getScanner(s);
				}catch(Exception e1){
					init();
					table = new HTable(conf, tableName);
					ss = table.getScanner(s);
				}		
			}
		}	
		return ss;
	}

	public Configuration getConf() {
		return conf;
	}

}
