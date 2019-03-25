package com.yihaodian.store.hbasestore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HRegionInfo;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.yihaodian.configcentre.client.utils.YccGlobalPropertyConfigurer;

public class HBaseTableTool {
	private static Logger log = Logger.getLogger(HBaseTableTool.class);
	public String _zk_quorums = "yhd-hadoop01.int.yihaodian.com,yhd-hadoop02.int.yihaodian.com,yhd-hadoop03.int.yihaodian.com";

	private static HBaseTableTool instance = new HBaseTableTool();
//	public static String _zk_quorums="192.168.130.107,192.168.130.108,192.168.130.109,10.161.144.126,10.161.144.127";

	private String hdfs_root = "hdfs://10.4.11.42:8020";
	private HBaseAdmin hbaseAdmin = null;
	private Configuration conf;
	private String env = "";
	private HBaseTableTool(){
		String config = System.getProperty("global.config.path");
		if (config == null) {
			System.setProperty("global.config.path", "/var/www/webapps/config");
		}

		String poolId = "yihaodian_search-data-service";
		FileInputStream fis = null;
		try {
			String dataId = "hadoop.properties";
			String data = YccGlobalPropertyConfigurer.loadConfigString(poolId,
					dataId, false);
			Properties prop = YccGlobalPropertyConfigurer
					.loadPropertiesFromString(data);
			// Get zk_quorums
			String zk_quorums = prop.getProperty("zk_quorums");
			if (zk_quorums != null) {
				log.info("Get config from ycc, zk_quorums:" + zk_quorums);
				_zk_quorums = zk_quorums;
			}
			log.info("_zk_quorums: " + _zk_quorums);
			
			// Get hdfs_root
			String hdfs = prop.getProperty("fs_name");
			if (hdfs != null) {
				this.hdfs_root = hdfs;
			}
			log.info("hdfs_root: " + hdfs_root);

			// Get env
			String cfgPath = System.getProperty("global.config.path");
			if (StringUtils.isEmpty(cfgPath)) {
				log.error("global.config.path is not configured.");
				cfgPath = "/var/www/webapps/config";
			}
			File envFile = new File(cfgPath, "env.ini");
			if (envFile.exists()) {
				fis = new FileInputStream(envFile);
				prop.load(fis);
				String envStr = prop.getProperty("env");
				if (envStr != null) {
					String trimEnv = envStr.trim();
					if (!trimEnv.equals("production")) {
						env = "-" + trimEnv;
					}
				}
			} else {
				log.error("File doesn't exist, " + envFile.getAbsolutePath());
			}
			log.info("env: " + env);
		} catch (Exception e) {
			log.warn("Ycc init error " + poolId, e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		configure();
		
		try {
			//checkAndUploadCoprocessor();
		} catch (Exception e) {
			log.error("Check coprocessor jar file error!" ,e);
		}
	}
	
	private synchronized void configure() {
		conf = HBaseUtil.createConf(_zk_quorums, 2181);
        conf.setLong(HConstants.HBASE_CLIENT_INSTANCE_ID, System.currentTimeMillis());
	}
	
	public static HBaseTableTool getInstance(){
		return instance;
	}
	
	/**
	 * 根据表名获取表
	 * @param tableName
	 * @return
	 */
	public HTable getHtable(String tableName) throws MasterNotRunningException, ZooKeeperConnectionException, IOException{
		HTable hTable = null;
		if(hbaseAdmin == null){
			synchronized (this) {
				if(hbaseAdmin == null){
					hbaseAdmin = new HBaseAdmin(conf);
				}
			}
		}
		if (hbaseAdmin.isTableAvailable(Bytes.toBytes(tableName))) {
			hTable = new HTable(conf, Bytes.toBytes(tableName));
		}
		return hTable;
	}
	
	/**
	 * 根据表名获取所有结果集
	 * @param tableName
	 * @return
	 */
	public List<Result> getResultList(String tableName) throws MasterNotRunningException, ZooKeeperConnectionException, IOException{
		List<HRegionInfo> hRegionList = null;
		List<Result> resultList = null;
		HTable hTable = null;
		if(hbaseAdmin == null){
			synchronized (this) {
				if(hbaseAdmin == null){
					hbaseAdmin = new HBaseAdmin(conf);
				}
			}
		}
		if (hbaseAdmin.isTableAvailable(Bytes.toBytes(tableName))) {
			hTable = new HTable(conf, Bytes.toBytes(tableName));
			hRegionList = hbaseAdmin.getTableRegions(Bytes.toBytes(tableName));
			if(hRegionList != null){
				int totalCount = 0 ;
				resultList = new ArrayList<Result>();
				for (HRegionInfo regionInfo:hRegionList) {
					byte[] startRow = regionInfo.getStartKey();
					byte[] endRow = regionInfo.getEndKey();
					try {
						Scan scan = new Scan();
						scan.setStartRow(startRow);
						scan.setStopRow(endRow);
						ResultScanner scanner = hTable.getScanner(scan);
						if(scanner == null){
							log.info("no scanner for htable.");
							break;
						}
						Result result;
						try {
							result = scanner.next();
							while(result != null){
								resultList.add(result);
								result = scanner.next();
								totalCount++;
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					log.info("the result size of Hbase table-->"+ tableName + " is " + totalCount);
				}
			}
		}
		return resultList;
	}
	
	/**
	 * 根据表获取具体key的结果集
	 * @param hTable
	 * @param rowKey
	 * @return
	 */
	public Result getResult(HTable hTable,String rowKey) throws IOException{
		Result result = null;
		if(hTable != null){
			Get get = new Get(Bytes.toBytes(rowKey));
			result = hTable.get(get);
		}
		return result;
	}
	
	
}
