package com.yihaodian.store.conf;

import java.io.File;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.log4j.Logger;

import com.yihaodian.store.bulkload.HBaseRecordBulkLoader;

public class ConfigurationUtil {
	
	static Logger log = Logger.getLogger(ConfigurationUtil.class);
	
	public static final String defalut_fs_name = "hdfs://192.168.130.106:8020";
	
	public static final String default_quorum="192.168.130.107,192.168.130.108,192.168.130.109," +
			"10.161.144.126,10.161.144.127";
	
	public static final int default_port=2181;
	
	public static final String defalut_yarn_host="hadoopcm0";
	
	public static final String fs_name_ip_production="10.4.11.42";
	
	public static final String jobHistoryServer_production="10.4.11.45:10020";
	
	public static Configuration createHBaseConf(String quorum, int port) {
		Configuration conf = new Configuration();
		conf.set(HConstants.ZOOKEEPER_QUORUM, quorum);
		conf.setInt(HConstants.ZOOKEEPER_CLIENT_PORT, port);
		conf.setInt(HConstants.HBASE_CLIENT_RETRIES_NUMBER,3);
		conf.setInt(HConstants.HBASE_CLIENT_SCANNER_TIMEOUT_PERIOD,3600000);
		conf.setInt(HConstants.HBASE_RPC_TIMEOUT_KEY, 3600000);
		
		return conf;
	} 
	
	public static Configuration createConf(String fs_name,String yarn_host,String quorum, int port) {
		Configuration conf = createHBaseConf(quorum, port);
		conf.set("fs.defaultFS", fs_name);
		conf.set("yarn.resourcemanager.hostname", yarn_host);
		conf.set("mapreduce.framework.name", "yarn");
		conf.set("mapred.job.priority", "VERY_HIGH");
		conf.set("fs.hdfs.impl",org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
		conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());
		String env=DataStoreConfig.getEnv();
		log.info("env===="+env);
		if(env.equals("production") || env.equals("staging")){
			conf.set("ha.zookeeper.quorums",quorum);
			conf.set("fs.defaultFS", "hdfs://Search:8020");
			conf.set("mapreduce.jobhistory.address",jobHistoryServer_production);
			conf.set("dfs.client.failover.proxy.provider.Search",    
					 "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
			conf.set("dfs.nameservices", "Search");
			conf.set("dfs.ha.namenodes.Search","NN1,NN2");
			conf.set("dfs.namenode.rpc-address.Search.NN1", "10.4.11.42:8020");
			conf.set("dfs.namenode.rpc-address.Search.NN2", "10.4.11.43:8020");
			log.info("production or staging fs.defaultFS is "+conf.get("fs.defaultFS"));
		}
		return conf;
	}
	
	public static Configuration getDefaultConf() throws IOException{
		return createConf(DataStoreConfig.getFs_name(), DataStoreConfig.getYarn_host(),
				DataStoreConfig.getZk_quorums(),2181);
	}
	
	public static Configuration createConf(Configuration conf) throws IOException{
		Configuration config=createConf(DataStoreConfig.getFs_name(), DataStoreConfig.getYarn_host(),
				DataStoreConfig.getZk_quorums(),2181);
		HBaseConfiguration.merge(conf, config);		
		return conf;
	}
	
	public static Configuration createMapReduceConf(String fs_name,String yarn_host,String quorum, int port) throws IOException{
		Configuration conf=createConf(fs_name,yarn_host,quorum,port);
		String classPath=System.getProperty("java.class.path");
		String os = System.getProperties().getProperty("os.name");
		String seperator = os.toLowerCase().contains("windows") ? ";" : ":";
		boolean isTomcat=false;
		if(classPath!=null){
			if(classPath.indexOf("tomcat")>0){
				isTomcat=true;
			}else{
				String[] jarPaths=classPath.split(seperator);
				for(String jarPath:jarPaths){
					if(jarPath.endsWith(".jar")){
						addTmpJar(jarPath, conf);
					}	
				}
			}	
		}
		
		if(isTomcat || classPath==null){
			String jarFilePath = HBaseRecordBulkLoader.class.getProtectionDomain().getCodeSource().getLocation().getFile();  
			// URL Decoding  
			jarFilePath = java.net.URLDecoder.decode(jarFilePath, "UTF-8"); 
			File jarFile=new File(jarFilePath);
			File parentFile=jarFile.getParentFile();
			String[] childNames=parentFile.list();
			if(childNames!=null){
				for(String childName:childNames){
					if(childName.endsWith(".jar")){
						addTmpJar(parentFile.getAbsolutePath()+"/"+childName, conf);
					}
				}
			}
		}
		return conf;
	}
	
	public static Configuration createDefaultMapReduceConf() throws IOException{
		return createMapReduceConf(DataStoreConfig.getFs_name(), DataStoreConfig.getYarn_host(),
				DataStoreConfig.getZk_quorums(),2181);
	}
	
	
	/** 
	 * 为Mapreduce添加第三方jar包 
	 *  
	 * @param jarPath 
	 *            举例：D:/Java/new_java_workspace/scm/lib/guava-r08.jar 
	 * @param conf 
	 * @throws IOException 
	 */  
	public static void addTmpJar(String jarPath, Configuration conf) throws IOException {  
	    System.setProperty("path.separator", ":");  
	    FileSystem fs = FileSystem.getLocal(conf);  
	    String newJarPath = new Path(jarPath).makeQualified(fs).toString();  
	    String tmpjars = conf.get("tmpjars");  
	    if (tmpjars == null || tmpjars.length() == 0) {  
	        conf.set("tmpjars", newJarPath);  
	    } else {  
	        conf.set("tmpjars", tmpjars + "," + newJarPath);  
	    }  
	}  

	public static void main(String[] args) throws IOException{
		Configuration conf=new Configuration();
		conf.set("test1", "test1");
		createConf(conf);
		System.out.println(conf.get("dfs.nameservices"));
	}
}
