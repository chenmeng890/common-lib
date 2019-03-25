package com.yihaodian.store.hbasestore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.io.IOUtils;
import org.apache.log4j.Logger;

import com.yihaodian.common.util.EnvUtil;
import com.yihaodian.store.conf.DataStoreConfig;

public class HBaseStoreManager {
	private static Logger log = Logger.getLogger(HBaseStoreManager.class);
	private static ConcurrentMap<String,HBaseStoreManager> instances=new ConcurrentHashMap<String, HBaseStoreManager>();
	private static final String COPROCESSOR_PATH = "/hbase/coprocessor/hbase-coprocessor.jar";
	public static final String COPROCESSOR_ROW_COUNT = "coprocessor_row_count";

	private Map<String, HBaseStore> stores = new HashMap<String, HBaseStore>();
	
	public String _zk_quorums = "yhd-hadoop01.int.yihaodian.com," +
			"yhd-hadoop02.int.yihaodian.com,yhd-hadoop03.int.yihaodian.com";
	private String hdfs_root = "hdfs://10.4.11.42:8020";

	private Configuration conf;
	private HTablePool tablePool;
	private static String _env = "";
	
	static {
		String env = EnvUtil.getEnv();
		if (!env.equals("production")) {
			_env = "-" + env;
		}
	}

	private HBaseStoreManager(String zk_quorums) {
		this._zk_quorums = zk_quorums;
		log.info("_zk_quorums: " + _zk_quorums);
		this.hdfs_root = DataStoreConfig.getFs_name();
		log.info("hdfs_root: " + hdfs_root);	
		configure();		
		try {
			//checkAndUploadCoprocessor();
		} catch (Exception e) {
			log.error("Check coprocessor jar file error!" ,e);
		}
	}

	/**
	 * For applications which require high-end multithreaded access (e.g.,
	 * web-servers or application servers that may serve many application
	 * threads in a single JVM), one solution is HTablePool. But as written
	 * currently, it is difficult to control client resource consumption when
	 * using HTablePool.
	 * 
	 * Another solution is to precreate an HConnection using
	 * 
	 * HConnectionManager.createConnection(Configuration) as well as an
	 * ExecutorService; then use the HTable(byte[], HConnection,
	 * ExecutorService)
	 */
	private synchronized void configure() {
		conf = HBaseUtil.createConf(_zk_quorums, 2181);
        conf.setLong(HConstants.HBASE_CLIENT_INSTANCE_ID, System.currentTimeMillis());
		tablePool = HBaseUtil.createHTablePool(conf);
	}

	public static HBaseStoreManager getInstance() {
		return getInstance(DataStoreConfig.getZk_quorums());
	}
	
	public static HBaseStoreManager getInstance(String zk_quorums) {
		HBaseStoreManager hBaseStoreManager=instances.get(zk_quorums);
		if (hBaseStoreManager == null) {
			synchronized (instances) {
				hBaseStoreManager=instances.get(zk_quorums);
				if (hBaseStoreManager == null) {
					hBaseStoreManager = new HBaseStoreManager(zk_quorums);
					instances.put(zk_quorums, hBaseStoreManager);
				}
			}
		}
		return hBaseStoreManager;
	}
	
	public static void clearInstance(){
		instances.clear();
	}

	/**
	 * Get store name
	 * 
	 * @param regex
	 * @return
	 */
	public synchronized List<String> getStoreList(String regex) {
		List<String> list = new ArrayList<String>();
		try {
			HBaseAdmin admin = new HBaseAdmin(conf);
			HTableDescriptor[] tables = admin.listTables(regex);
			if (tables != null && tables.length > 0) {
				for (HTableDescriptor table : tables) {
					String name = table.getNameAsString();
					if (name != null) {
						list.add(name);
					}
				}
			}
		} catch (MasterNotRunningException e) {
			log.error(e);
		} catch (ZooKeeperConnectionException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}

		return list;
	}

	/**
	 * Get the table descriptor list.
	 * 
	 * @return
	 */
	public synchronized HTableDescriptor[] getStoreList() {
		try {
			HBaseAdmin admin = new HBaseAdmin(conf);
			HTableDescriptor[] tables = admin.listTables();
			return tables;
		} catch (MasterNotRunningException e) {
			log.error(e);
		} catch (ZooKeeperConnectionException e) {
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		}
		return null;
	}

	public HBaseStore getStore(String name) {
		return getStore(name,true);
	}
	/**
	 *
	 * @param name
	 * @param useEnv  是否表名后添加环境变量
	 * @return
	 */
	public HBaseStore getStore(String name,boolean addEnv) {
		return getStore(name, HBaseRecord.getFamily(),addEnv, null);
	}

	public HBaseStore getStore(String name, String[] familyList) {
		return getStore(name, familyList, true, null);
	}
	
	public HBaseStore getStore(String name, boolean addEnv, String[] familyList) {
		return getStore(name, familyList, addEnv, null);
	}

	public HBaseStore getStore(String name, String[] familyList, boolean addEnv, String[] coprocessors) {
		String tableName=addEnv?tableName(name):name;
		return _getStore(tableName, familyList, coprocessors);
	}

	private synchronized HBaseStore _getStore(String name, String[] familyList, String[] coprocessors) {
		HBaseStore store = stores.get(name);
		if (store == null) {
			HTableInterface table = null;
			try {
				String[] familys;
				if (familyList != null && familyList.length > 0) {
					familys = familyList;
				} else {
					familys = HBaseRecord.getFamily();
				}

				HBaseAdmin admin = new HBaseAdmin(conf);
				if (admin.tableExists(name)) {
					log.info("table already exists! " + name);
					// table=new HTable(name.getBytes(), hconnection,
					// sharedThreadPool);
					HTableDescriptor tableDesc = tablePool.getTable(name)
							.getTableDescriptor();
					HColumnDescriptor[] existingFamilys = tableDesc
							.getColumnFamilies();
					for (String family : familys) {
						HColumnDescriptor col = null;
						for (HColumnDescriptor c: existingFamilys) {
							if (c.getNameAsString().equals(family)) {
								col = c;
								break;
							}
						}
						HColumnDescriptor target = getHColumnDescriptor(family);
						if (col == null) {
							log.info("Adding column, name: " + name + ", column " + target);
							admin.disableTable(name.getBytes());
							admin.addColumn(name.getBytes(), target);							
							admin.enableTable(name.getBytes());
						}			
						
					}
				} else {
					HTableDescriptor tableDesc = new HTableDescriptor(name);
					for (int i = 0; i < familys.length; i++) {
						tableDesc.addFamily(getHColumnDescriptor(familys[i]));
					}
					/**
					 * 设置协处理器
					 */
					if (coprocessors != null) {
						for (int i = 0; i < coprocessors.length; i++) {
							if (COPROCESSOR_ROW_COUNT.equals(coprocessors[i])) {
								tableDesc.addCoprocessor(
										"org.apache.hadoop.hbase.coprocessor.example.RowCountEndpoint", 
										new Path(hdfs_root, COPROCESSOR_PATH), 1001, null);
								log.info(name + " set coprocessor: " + COPROCESSOR_ROW_COUNT);
							}
						}
					}
					
					admin.createTable(tableDesc);
					log.info("create table " + name + " ok.");
				}
				table = tablePool.getTable(name);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				if (table != null) {
					store = new HBaseStore(name, tablePool);
					if (store != null) {
						stores.put(name, store);
					}
					try {
						table.close();
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}

			}

		}
		return store;
	}
	
	/**
	 * 上传协处理jar包到hdfs
	 * @throws Exception
	 */
	public void checkAndUploadCoprocessor() throws Exception{
		System.setProperty("HADOOP_USER_NAME", "hadoop");
		
		/**
		 * Step 1: Check if the coprocessor jar file is exits.
		 */
		Configuration config=new Configuration();
		FileSystem fs=FileSystem.get(URI.create(hdfs_root), config);
		Path path = new Path(COPROCESSOR_PATH);
		boolean isExits = fs.exists(path);
		
		/**
		 * Step 2: Upload file if it dose not exits.
		 */
		if (!isExits) {
			OutputStream os=fs.create(path);
			InputStream is = this.getClass().getClassLoader().
					getResourceAsStream("hbase-coprocessor.jar");
			IOUtils.copyBytes(is, os, 4096, true);
		}
	}
	
	public synchronized void removeStore(String name){
		removeStore(name, true);
	}
	
	public synchronized void removeStore(String name, boolean addEnv) {
		String tableName = addEnv ? tableName(name) : name;
		HBaseAdmin admin;
		try {
			admin = new HBaseAdmin(conf);
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
			log.info("delete table " + name + " ok.");
		} catch (MasterNotRunningException e) {
			log.error("Failed to remove " + name, e);
		} catch (ZooKeeperConnectionException e) {
			log.error("Failed to remove " + name, e);
		} catch (IOException e) {
			log.error("Failed to remove " + name, e);
		}
		stores.remove(tableName);
	}

	public static String tableName(String name) {
		return name + _env;
	}

	public synchronized HTablePool getHTablePool() {
		if (tablePool == null) {
			configure();
		}
		return tablePool;
	}
	
	/**
	 * columnFamily common config, min_version and ttl no need config,
	 * because maybe cause some question
	 * @param columnFamily
	 * @return
	 */
	private static HColumnDescriptor getHColumnDescriptor(String columnFamily){
		HColumnDescriptor hcd=new HColumnDescriptor(columnFamily);
		hcd.setBloomFilterType(HBaseConstants.bloom_type);
		hcd.setTimeToLive(HBaseConstants.TIME_TO_LIVE);
		hcd.setMaxVersions(HBaseConstants.MAX_VERSIONS);
		return hcd;
	}
	
	public void updateColumnDescriptor(String tableName, HColumnDescriptor hcd) throws Exception{
		HBaseAdmin admin = new HBaseAdmin(conf);
		admin.modifyColumn(tableName, hcd);
		admin.close();
	}
	
	public static void main(String[] args) {
		System.out.println(HBaseStoreManager._env);
	}
}
