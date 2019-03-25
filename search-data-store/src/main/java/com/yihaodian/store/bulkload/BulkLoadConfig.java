package com.yihaodian.store.bulkload;

import org.apache.hadoop.conf.Configuration;

/**
 * 
 * bulkLoad config 
 * 
 * @author zengfenghua
 *
 */
public class BulkLoadConfig {
	
	private Configuration conf;
	
    private String tableName;
	
	private String bulk_input_dir;
	
	private long timestamp;
	
	private String bulk_output_dir;
	
	private String[] columnFamilies;

	public BulkLoadConfig(Configuration conf,String tableName, String bulk_input_dir,
			long timestamp, String bulk_output_dir, String[] columnFamilies) {
		super();
		this.conf=conf;
		this.tableName = tableName;
		this.bulk_input_dir = bulk_input_dir;
		this.timestamp = timestamp;
		this.bulk_output_dir = bulk_output_dir;
		this.columnFamilies = columnFamilies;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getBulk_input_dir() {
		return bulk_input_dir;
	}

	public void setBulk_input_dir(String bulk_input_dir) {
		this.bulk_input_dir = bulk_input_dir;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getBulk_output_dir() {
		return bulk_output_dir;
	}

	public void setBulk_output_dir(String bulk_output_dir) {
		this.bulk_output_dir = bulk_output_dir;
	}

	public Configuration getConf() {
		return conf;
	}

	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public String[] getColumnFamilies() {
		return columnFamilies;
	}

	public void setColumnFamilies(String[] columnFamilies) {
		this.columnFamilies = columnFamilies;
	}
	
}
