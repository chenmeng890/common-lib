package com.yihaodian.store.hdfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSFilesCopy {
	
	private Configuration sourceConf;
	private Configuration targetConf;
	
	public HDFSFilesCopy(Configuration sourceConf, Configuration targetConf) {
		this.sourceConf = sourceConf;
		this.targetConf = targetConf;
	}
	
	/**
	 * 将数据从源(source)的HDFS集群拷贝到目标(target)的DHFS集群
	 */
	public int copyUpdateFiles(String sourcePath, String targetPath, String jobName) throws Exception {
		// 跨集群拷贝
		List<String> childPaths = getHDFSChildPath(sourcePath, sourceConf);
		copyUpdateFiles(childPaths, targetPath, jobName);
		return 0;
	}
	
	/**
	 * 将更新数据从源(source)的HDFS集群拷贝到目标(target)的DHFS集群
	 */
	public int copyUpdateFiles(List<String> updateFiles, String targetPath, String jobName) throws Exception {
		List<Path> sourcePaths = new ArrayList<Path>();
		if (updateFiles != null && updateFiles.size() > 0) {
			for (String eachChildPath : updateFiles) {
				Path eachSourceFilePath = new Path(eachChildPath);
				sourcePaths.add(eachSourceFilePath);
			}
		}
		// 跨集群拷贝
		DistCpHDFSFilesCopy filesCopy = new DistCpHDFSFilesCopy(targetConf, jobName);
		filesCopy.copyHDFSFiles(sourcePaths, new Path(targetPath));
		return 0;
	}
	
	public List<String> getHDFSChildPath(String parentPath, Configuration conf) throws IOException {
		List<String> childPaths = new ArrayList<String>();
		FileSystem fs = FileSystem.get(conf);
		FileStatus fileList[] = fs.listStatus(new Path(parentPath));
		if (fileList != null && fileList.length > 0) {
			for (FileStatus each : fileList) {
				childPaths.add(parentPath + "/" + each.getPath().getName());
			}
		}
		return childPaths;
	}
}
