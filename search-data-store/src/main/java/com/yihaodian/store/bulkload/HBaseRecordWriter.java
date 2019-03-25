package com.yihaodian.store.bulkload;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.yihaodian.configcentre.utils.FileUtils;
import com.yihaodian.store.hbasestore.HBaseRecord;

/**
 * 
 * @author zengfenghua
 *
 */
public class HBaseRecordWriter {
	
	static Logger log = Logger.getLogger(HBaseRecordWriter.class);
	
    AtomicLong count=new AtomicLong(0);
	
	BufferedWriter bufferedWriter;
	
	Gson gson=new Gson();
    
    File file;
    
    String filePath;
	
    protected HBaseRecordWriter(String filePath){
    	this.filePath=filePath;
    	this.file=new File(filePath);
	}

    protected void write(List<HBaseRecord> hbaseRecords) throws IOException{
		if(bufferedWriter==null){
			bufferedWriter=new BufferedWriter(new FileWriter(file),10240000);
		}
		
		for(HBaseRecord hbaseRecord:hbaseRecords){
			bufferedWriter.write(gson.toJson(hbaseRecord));
			bufferedWriter.newLine();
			count.addAndGet(1);
		}
	}
	
    protected void close() throws IOException{
		if(bufferedWriter!=null){
			bufferedWriter.close();
		}
		log.info("HBaseRecordWriter count=="+count.get());
	}

    protected long size(){
    	return file.length();
    }
    
    protected String getFilePath(){
    	return filePath;
    }
    
    protected String getFileName(){
    	return file.getName();
    }
}
