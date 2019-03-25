package org.apache.hadoop.hbase.mapreduce;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.util.Base64;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ImportTsvSurrogate extends ImportTsv {

//	public static void createHbaseAdmin(Configuration conf) throws IOException {
//		ImportTsv.createHbaseAdmin(conf);
//	}

	  public static Job createSubmittableJob(Configuration conf, String[] args)
	  throws IOException, ClassNotFoundException {

	    // Support non-XML supported characters
	    // by re-encoding the passed separator as a Base64 string.
	    String actualSeparator = conf.get(SEPARATOR_CONF_KEY);
	    if (actualSeparator != null) {
	      conf.set(SEPARATOR_CONF_KEY,
	               Base64.encodeBytes(actualSeparator.getBytes()));
	    }

	    // See if a non-default Mapper was set
	    String mapperClassName = conf.get(MAPPER_CONF_KEY);
	    Class mapperClass = mapperClassName != null ?
	        Class.forName(mapperClassName) : DEFAULT_MAPPER;

	    String tableName = args[0];
	    Path inputDir = new Path(args[1]);
	    Job job = new Job(conf, NAME + "_" + tableName);
	    job.setJarByClass(mapperClass);
	    FileInputFormat.setInputPaths(job, inputDir);
	    job.setInputFormatClass(TextInputFormat.class);
	    job.setMapperClass(mapperClass);

	    String hfileOutPath = conf.get(BULK_OUTPUT_CONF_KEY);
	    if (hfileOutPath != null) {
	      HTable table = new HTable(conf, tableName);
	      job.setReducerClass(PutSortReducer.class);
	      Path outputDir = new Path(hfileOutPath);
	      FileOutputFormat.setOutputPath(job, outputDir);
	      job.setMapOutputKeyClass(ImmutableBytesWritable.class);
	      job.setMapOutputValueClass(Put.class);
	      HFileOutputFormat.configureIncrementalLoad(job, table);
	    } else {
	      // No reducers.  Just write straight to table.  Call initTableReducerJob
	      // to set up the TableOutputFormat.
	      TableMapReduceUtil.initTableReducerJob(tableName, null, job);
	      job.setNumReduceTasks(0);
	    }

	    TableMapReduceUtil.addDependencyJars(job);
	    TableMapReduceUtil.addDependencyJars(job.getConfiguration(),
	        com.google.common.base.Function.class /* Guava used by TsvParser */);
	    return job;
	  }

}
