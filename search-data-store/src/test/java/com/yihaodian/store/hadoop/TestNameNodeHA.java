package com.yihaodian.store.hadoop;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.yihaodian.store.hbase.HDFSUtil;

public class TestNameNodeHA {
	
	public static void main(String args[]) throws IOException{
//		HDFSUtil hdfsUtil=new HDFSUtil("hdfs://10.4.11.42:8020");
//		List<String> childs=hdfsUtil.getChildPaths("/test");
//		System.out.println(childs);
		Configuration conf=new Configuration();
		conf.set("ha.zookeeper.quorums","10.4.11.44,10.4.11.45,10.4.11.46");
		conf.set("fs.defaultFS", "hdfs://Search:8020");
		conf.set("dfs.client.failover.proxy.provider.Search",    
				 "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
		conf.set("dfs.nameservices", "Search");
		conf.set("dfs.ha.namenodes.Search","NN1,NN2");
		conf.set("dfs.namenode.rpc-address.Search.NN1", "10.4.11.42:8020");
		conf.set("dfs.namenode.rpc-address.Search.NN2", "10.4.11.43:8020");
		FileSystem fs=FileSystem.get(conf);
		FileStatus[] fsPaths=fs.listStatus(new Path("/test"));
		for (FileStatus child : fsPaths) {
			Path childPath = child.getPath();
			String name = childPath.getName();
			System.out.println(name);
		}
	}

}
