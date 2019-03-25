package com.yihaodian.store.bulkload;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.mapreduce.ImportTsvSurrogate;
import org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

import com.yihaodian.store.conf.ConfigurationUtil;
import com.yihaodian.store.hbasestore.HBaseStoreManager;

/**
 * load sqoop dump into hbase.
 * 
 */
public class BulkLoader {

	static Logger log = Logger.getLogger(BulkLoader.class);
	
	static String JAVA_OPTS="-server -Xmx4096m -XX:NewSize=1000m -XX:MaxNewSize=1000m -XX:MaxPermSize=256m -XX:SurvivorRatio=4" +
			" -XX:MaxDirectMemorySize=100m -XX:MaxPermSize=256m -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=85";
	
	public static void importTsvAndLoad(BulkLoadConfig config) throws Exception{
		importTsvAndLoad(config.getConf(), config.getTableName(), config.getColumnFamilies(),
			config.getBulk_input_dir(), config.getBulk_output_dir(),config.getTimestamp());
	}

	@Deprecated
    public static void importTsvAndLoad(Configuration conf, String tableName,
			String inputDir, String bulkOutputDir,long timestamp) throws Exception {
		importTsvAndLoad(conf, tableName, null, inputDir, bulkOutputDir, timestamp);
	}
    
    public static void importTsvAndLoad(Configuration conf, String tableName, String[] families, 
			String inputDir, String bulkOutputDir,long timestamp) throws Exception {

		log.info("import: " + tableName);
		/**
		 * Check whether there is a table in the hbase, if not just create one
		 */
		String zk_quorum=conf.get(HConstants.ZOOKEEPER_QUORUM);
		HBaseStoreManager.getInstance(zk_quorum).getStore(tableName, false, families);
		importTsv(conf, tableName,timestamp, inputDir, bulkOutputDir);
		log.info("import done: " + tableName);

		log.info("bulkload: " + tableName);
		completeBulkLoad(conf, bulkOutputDir, tableName);
		log.info("bulkload done: " + tableName);
	}

	/**
	 * generate StoreFiles for bulk-loading
	 * 
	 * @param tableName
	 * @param inputTableDir
	 */
	private static void importTsv(Configuration conf, String tableName,long ts,
			String inputTableDir, String outputTableDir) throws Exception {
		conf.set("importtsv.mapper.class",
				TsvToHBaseMapper.class.getCanonicalName());
		conf.set("importtsv.bulk.output", outputTableDir);
		conf.set(TsvToHBaseMapper.conf_table_name, tableName);
		conf.setLong("importtsv.timestamp", ts);
		conf.setInt(HConstants.HBASE_RPC_TIMEOUT_KEY, 3600000);
		//map split min size is 512M
		conf.setLong("mapred.min.split.size", 512*1024*1024);
		conf.set("mapreduce.map.java.opts", JAVA_OPTS);
		conf.set("mapreduce.reduce.java.opts", JAVA_OPTS);
		String[] params = { tableName, inputTableDir };
		log.info("before importTsv fs.defaultFS:"+conf.get("fs.defaultFS"));
		String[] otherArgs = new GenericOptionsParser(conf, params)
				.getRemainingArgs();
		Job job = ImportTsvSurrogate.createSubmittableJob(conf, otherArgs);
		try{
			job.waitForCompletion(true);
		}catch(Exception e){
			if(e.getMessage().contains("java.io.FileNotFoundException")
					&& e.getMessage().contains("JobHistory.getJob")){
				log.error("JobHistory.getJob "+job.getJobID().toString()+" status error ,sleep 30 minutes");
				Thread.currentThread().sleep(30*60*1000);
				return ;
			}
			throw e;
		}
	}

	/**
	 * 
	 * 
	 * @param path
	 * @param tableName
	 * @throws Exception
	 */
	private static void completeBulkLoad(Configuration conf, String path,
			String tableName) throws Exception {
		// LoadIncrementalHFiles.main(args);
		String[] args = new String[] { path, tableName };
		log.info("before completeBulkLoad fs.defaultFS:"+conf.get("fs.defaultFS"));
		ToolRunner.run(new LoadIncrementalHFiles(conf), args);
	}
	
	public static void main(String[] args) throws Exception{
		Configuration config=ConfigurationUtil.getDefaultConf();
		LoadIncrementalHFiles files=new LoadIncrementalHFiles(config);
		System.out.println(files.getConf().get("fs.defaultFS"));
	}
}
