package com.yihaodian.store.hdfs;

import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.tools.DistCp;
import org.apache.hadoop.tools.DistCpOptions;

public class DistCpHDFSFilesCopy {
	private Configuration conf;
	
	@SuppressWarnings("unused")
	private DistCpHDFSFilesCopy() {}
	
	public DistCpHDFSFilesCopy(Configuration conf, String jobName) {
		this.conf = conf;
		if (jobName != null) {
			conf.set(JobContext.JOB_NAME, jobName);
		}
	}
	
	public void copyHDFSFiles(List<Path> sourcePaths, Path targetPath) throws Exception {
		// 创建 map reduce job并且等待完成
		FileSystem fs = FileSystem.get(conf);
		if (!fs.exists(targetPath)) {
			fs.mkdirs(targetPath);
		}
		DistCpOptions options = new DistCpOptions(sourcePaths, targetPath);
		DistCp distCp = new DistCp(conf, options);
		Job copyJob = distCp.execute();
		copyJob.waitForCompletion(true);
	}
}
