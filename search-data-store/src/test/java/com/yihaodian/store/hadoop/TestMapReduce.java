package com.yihaodian.store.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobID;
import org.apache.hadoop.mapred.RunningJob;

public class TestMapReduce {
	
	public static void main(String[] args) throws IOException{
		Configuration conf=new Configuration();
		conf.set("fs.defaultFS","hdfs://10.4.11.42:8020" );
		conf.set("yarn.resourcemanager.hostname", "10.4.11.42");
		conf.set("mapreduce.framework.name", "yarn");
		conf.set("mapreduce.jobhistory.address","10.4.11.45:10020");
		JobClient jobClient=new JobClient(conf);
		RunningJob job=jobClient.getJob(JobID.forName("job_1422948423416_13680"));
		job.killJob();
	}

}
