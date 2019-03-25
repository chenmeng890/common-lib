package com.yihaodian.store.bulkload;

import org.junit.Before;
import org.junit.Test;

import com.yihaodian.store.hbasestore.HBaseRecord;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class HBaseRecordBulkLoaderTest {
	
	HBaseRecordBulkLoader hBaseRecordBulkLoader;
	
	@Before
	public void setUp(){
		hBaseRecordBulkLoader=new HBaseRecordBulkLoader("bulkloadTest", 11111111l);
	}
	
	@Test
	public void testWriteLocal() throws IOException{
		List<HBaseRecord> hbaseRecords=new ArrayList<HBaseRecord>();
		hbaseRecords.add(new HBaseRecord("11","11".getBytes(),11111111l));
		hbaseRecords.add(new HBaseRecord("22","22".getBytes(),11111111l));
		hbaseRecords.add(new HBaseRecord("33","33".getBytes(),11111111l));
		hBaseRecordBulkLoader.writeLocal(hbaseRecords);
	}
	
	@Test
	public void testBulkLoad() throws Exception{
		List<HBaseRecord> hbaseRecords=new ArrayList<HBaseRecord>();
		hbaseRecords.add(new HBaseRecord("11","11".getBytes(),11111111l));
		hbaseRecords.add(new HBaseRecord("22","22".getBytes(),11111111l));
		hbaseRecords.add(new HBaseRecord("33","33".getBytes(),11111111l));
		hBaseRecordBulkLoader.writeLocal(hbaseRecords);
		hBaseRecordBulkLoader.bulkload();
	}

}
