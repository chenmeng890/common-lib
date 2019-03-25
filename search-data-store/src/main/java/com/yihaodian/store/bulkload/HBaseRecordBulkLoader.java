package com.yihaodian.store.bulkload;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

import com.yihaodian.store.conf.ConfigurationUtil;
import com.yihaodian.store.conf.DataStoreConfig;
import com.yihaodian.store.hbase.HDFSUtil;
import com.yihaodian.store.hbasestore.HBaseRecord;

/**
 * HBaseRecordBulkLoader:The data bulk import hbase, reduce the pressure on
 * hbase, fit in with the full amount to update data steps: 1,write to local
 * 2,upload to hdfs 3,data convert to HFile 4,HTable load HFile
 * 
 * @author zengfenghua
 * 
 */
public class HBaseRecordBulkLoader {

	static Logger log = Logger.getLogger(HBaseRecordBulkLoader.class);

	private HBaseRecordWriter hbaseRecordWriter;
	
	// 是否有多个Loader实例同时进行处理
	private boolean multiLoader = false;
	
	private String localRootPath;
	public static final String DEFAULT_LOCAL_ROOT_PATH = "/var/www/webapps/bulkload";

	public static final String hdfs_dump_path = "/user/search/bulkload/dump";

	public static final String hdfs_bulk_path = "/user/search/bulkload/bulk";

	private HDFSUtil hdfsUtil;

	private String _zk_quorums;

	private String _fs_name;

	private String _yarn_host;

	private String _tableName;

	private long _timestamp;
	
	private String localFilePath;
	
	private String bulk_input_dir;
	
	private String bulk_output_dir;
	
	private AtomicInteger fileNum=new AtomicInteger(0);
	
	private long max_file_size=5*1024*1024*1024l;
	
	Configuration conf;

	public HBaseRecordBulkLoader(String tableName, long timestamp){
		this(DataStoreConfig.getFs_name(), DataStoreConfig.getYarn_host(),
				DataStoreConfig.getZk_quorums(), tableName, timestamp, false,
				DEFAULT_LOCAL_ROOT_PATH);
	}
	
	public HBaseRecordBulkLoader(String tableName, long timestamp, boolean multiLoader){
		this(DataStoreConfig.getFs_name(), DataStoreConfig.getYarn_host(),
				DataStoreConfig.getZk_quorums(), tableName, timestamp, multiLoader,
				DEFAULT_LOCAL_ROOT_PATH);
	}
	
	public HBaseRecordBulkLoader(String tableName, long timestamp, boolean multiLoader,
			String localRootPath){
		this(DataStoreConfig.getFs_name(), DataStoreConfig.getYarn_host(),
				DataStoreConfig.getZk_quorums(), tableName, timestamp, multiLoader,
				localRootPath);
	}

	public HBaseRecordBulkLoader(String fs_name, String yarn_host,
			String zk_quorums, String tableName, long timestamp, boolean multiLoader,
			String localRootPath){
		this._fs_name = fs_name;
		this._yarn_host = yarn_host;
		this._zk_quorums = zk_quorums;
		this._tableName = tableName;
		this._timestamp = timestamp;
		this.multiLoader = multiLoader;
		this.localRootPath = localRootPath;
		init();
	}
	
	private void init(){	
		Date date=new Date(_timestamp);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
	    "yyyy-MM-dd_HH-mm-ss");
		String fileDirName = null;
		
		if (!multiLoader) {
			fileDirName=simpleDateFormat.format(date)+"_"+_tableName;
		} else {
			String uuid = UUID.randomUUID().toString();
			log.info("Use multi loader. Current uuid is: " + uuid);
			fileDirName=simpleDateFormat.format(date)+"_"+ uuid + "_" + _tableName;
		}
		
		this.localFilePath=localRootPath+"/"+fileDirName;
		this.bulk_input_dir = hdfs_dump_path+"/"+fileDirName;
		this.bulk_output_dir= hdfs_bulk_path+"/"+fileDirName;
		File localRootFile=new File(localRootPath);
		if(!localRootFile.exists()){
			localRootFile.mkdirs();
		} 
		try{
			conf = ConfigurationUtil.createMapReduceConf(
						_fs_name, _yarn_host, _zk_quorums, 2181);
			File localFile=new File(localFilePath);
			FileUtils.deleteDirectory(localFile);
			localFile.mkdir();
			hdfsUtil = new HDFSUtil(conf);
			hdfsUtil.deletePath(bulk_input_dir);
			hdfsUtil.deletePath(bulk_output_dir);
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
		createWriter();
	}
	
	private void createWriter(){
		hbaseRecordWriter=new HBaseRecordWriter(this.localFilePath+"/"+fileNum.getAndIncrement()+"_"+System.currentTimeMillis()+"_"+
				((int)(Math.random()*100000)));
	}

	public synchronized void writeLocal(List<HBaseRecord> hbaseRecords) throws IOException {
		hbaseRecordWriter.write(hbaseRecords);
        if(hbaseRecordWriter.size()>=max_file_size){
        	hbaseRecordWriter.close();
        	String hdfs_path=this.bulk_input_dir+"/"+hbaseRecordWriter.getFileName();
        	hdfsUtil.copyFromLocal(true, hbaseRecordWriter.getFilePath(), hdfs_path);
        	// Delete local file which has uploaded to HDFS.
        	FileUtils.deleteDirectory(new File(hbaseRecordWriter.getFilePath()));
        	log.info("Delete local file, path --> " + hbaseRecordWriter.getFilePath());
        	createWriter();
		}
	}

	public void bulkload() throws Exception {
		try {
			if(hbaseRecordWriter==null) return;
			if (hbaseRecordWriter != null) {
				hbaseRecordWriter.close();
			}
			String hdfs_path=this.bulk_input_dir+"/"+hbaseRecordWriter.getFileName();
			try{
				if(hbaseRecordWriter.size()>0){
					hdfsUtil.copyFromLocal(true, hbaseRecordWriter.getFilePath(), hdfs_path);
				}	
			}catch(Exception e){
				log.error(e.getMessage(), e);
			}
			BulkLoader.importTsvAndLoad(conf, _tableName, bulk_input_dir,
					bulk_output_dir, _timestamp);
		} finally {
			deleteOldData();
		}

	}

	private void deleteOldData() throws IOException {
		File file=new File(localFilePath);
		FileUtils.deleteDirectory(file);
		Date now = new Date();
		String dateType = "yyyy-MM-dd_HH-mm-ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateType);
		List<String> checkPaths = new ArrayList<String>();
		checkPaths.add(hdfs_dump_path);
		checkPaths.add(hdfs_bulk_path);
		if(hdfsUtil==null){
			hdfsUtil = new HDFSUtil(conf);
		}
		for (String checkPath : checkPaths) {
			List<String> childNames = hdfsUtil.getChildNames(checkPath);
			for (String childName : childNames) {
				try {
					if (childName.length() < dateType.length())
						continue;
					String datePrefix = childName.substring(0,
							dateType.length());
					Date date = simpleDateFormat.parse(datePrefix);
					/**
					 * Delete the data before three days
					 */
					if ((now.getTime() - date.getTime())
							/ (24 * 60 * 60 * 1000) > 2) {
						hdfsUtil.deletePath(checkPath + "/" + childName);
					}
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
//		long timestamp = System.currentTimeMillis();
//		log.info("timestamp==" + timestamp);
//		String tableName="bulkloadTest";
//		tableName=HBaseStoreManager.tableName(tableName);
//		HBaseRecordBulkLoader hBaseRecordBulkLoader = new HBaseRecordBulkLoader(tableName, timestamp);
//		List<HBaseRecord> hbaseRecords = new ArrayList<HBaseRecord>();
//		hbaseRecords.add(new HBaseRecord("66", "66".getBytes(), timestamp));
//		hbaseRecords.add(new HBaseRecord("77", "77".getBytes(), timestamp));
//		hbaseRecords.add(new HBaseRecord("88", "88".getBytes(), timestamp));
//		hBaseRecordBulkLoader.writeLocal(hbaseRecords);
//		hBaseRecordBulkLoader.bulkload();
		
//		String jarFilePath = HBaseRecordBulkLoader.class.getProtectionDomain().getCodeSource().getLocation().getFile();  
//		// URL Decoding  
//		jarFilePath = java.net.URLDecoder.decode(jarFilePath, "UTF-8"); 
//		System.out.println(jarFilePath);
//		ConfigurationUtil.createMapReduceConf("ddd","ddee", "dewer",22);
	}

}
