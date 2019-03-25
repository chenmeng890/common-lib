package com.yihaodian.store.hbasestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.yihaodian.store.hbasestore.HBaseRecord;
import com.yihaodian.store.hbasestore.HBaseRecordBuilder;

/**
 * HbaseObjectScanner instance through HBaseObjectStore to obtain
 *
 */
public class HbaseObjectScanner<T> implements HBaseScanner<HbaseObject<T>>{
	private Class<T> clazz;
	private HBaseRecordScanner scanner;
	private HBaseRecordBuilder<T> builder;
	
	protected HbaseObjectScanner(HBaseRecordScanner scanner, Class<T> clazz) {
		this.scanner = scanner;
		this.clazz = clazz;
		builder = new HBaseRecordBuilder<T>(clazz);
	}

	@Override
	public HbaseObject<T> next() throws Exception {
		
		HBaseRecord record = scanner.next();	
		T data = builder.fromHBaseRecord(record);
		HbaseObject<T> hbaseObject = new HbaseObject<T>(record.getKey(),record.getTimestamp(),data);
		return hbaseObject;
	}
	
	@Override
	public List<HbaseObject<T>> next(int num) throws Exception {
		List<HbaseObject<T>> hbaseObjects = new ArrayList<HbaseObject<T>>();
		int count = 0;
		while (count < num && hasNext()) {
			count++;
			hbaseObjects.add(next());
		}
		return hbaseObjects;
	}
	
	@Override
	public boolean hasNext() throws Exception {
		return scanner.hasNext();
	}
	
	@Override
	public void close() throws Exception {
		scanner.close();
	}
}
