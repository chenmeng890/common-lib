package com.yihaodian.store.hbasestore;

import java.util.List;

/**
 * HBaseScanner is a interface ,HBaseRecordScanner HbaseObjectScanner, HashHBaseObjectScanner are realized that class
 * 
 * @author zengfenghua
 *
 * @param <T>
 */
public interface HBaseScanner<T> {
	
	public T next() throws Exception;
	public List<T> next(int num) throws Exception;
	public boolean hasNext() throws Exception;
	public void close() throws Exception;

}
