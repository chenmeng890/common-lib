package com.yihaodian.store.hbasestore;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.client.HTablePool;

import com.yihaodian.store.conf.ConfigurationUtil;

public class HBaseUtil {

	public static Configuration createConf(String quorum, int port) {
		return ConfigurationUtil.createHBaseConf(quorum, port);
	}

	public static HTablePool createHTablePool(Configuration conf) {
		HTablePool tablePool = new HTablePool(conf, Integer.MAX_VALUE);

		return tablePool;
	}

}
