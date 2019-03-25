package com.yihaodian.store.hbasestore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;

/**
 *  HBaseRecordScanner instance through HBaseStore to obtain
 *
 */
public class HBaseRecordScanner implements HBaseScanner<HBaseRecord>{
	private ResultScanner scanner;
	private Iterator<Result> it;
	// <family, qualifier>
	private Map<byte[], List<byte[]>> qualifiers;
	
	protected HBaseRecordScanner(ResultScanner scanner){
		this(scanner, null);
	}
	
	protected HBaseRecordScanner(ResultScanner scanner, Map<byte[], List<byte[]>> qualifiers){
		this.scanner = scanner;
		it = scanner.iterator();
		this.qualifiers = qualifiers;
	}
	
	@Override
	public HBaseRecord next(){
		HBaseRecord record = null;
		Result rs = it.next();
		if (!rs.isEmpty()) {
			record = HBaseRecord.fromResult(rs, qualifiers);
		}
		
		return record;
	}
	
	@Override
	public List<HBaseRecord> next(int n){
		List<HBaseRecord> records = new ArrayList<HBaseRecord>();
		for (int i = 0; i < n; i++) {
			if (this.hasNext()) {
				records.add(HBaseRecord.fromResult(it.next(), qualifiers));
			} else {
				break;
			}
		}
		
		return records;
	}
	
	@Override
	public boolean hasNext(){
		return it.hasNext();
	}
	
	@Override
	public void close(){
		scanner.close();
	}
}
