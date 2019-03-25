package com.yihaodian.store.hbasestore;

import org.apache.hadoop.hbase.regionserver.BloomType;

public class HBaseConstants {

    public static final BloomType bloom_type=BloomType.ROW;

    public static final int TIME_TO_LIVE = 3600 * 24 * 5;
    
    public static final int MAX_VERSIONS = 3;  // 最大保留版本数目

}
