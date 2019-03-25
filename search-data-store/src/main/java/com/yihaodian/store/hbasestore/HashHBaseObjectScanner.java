package com.yihaodian.store.hbasestore;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 
 * HashHBaseObjectScanner corresponding HashHBaseObjectStore
 * 
 * @author zengfenghua
 *
 */
public class HashHBaseObjectScanner<T> implements HBaseScanner<HbaseObject<T>>{
	
	HbaseObjectScanner<T>[] hbaseObjectScanners;
	
	Queue<HbaseObject<T>>[] queues;
	
	protected HashHBaseObjectScanner(HbaseObjectScanner<T>[] hbaseObjectScanners){
		this.hbaseObjectScanners=hbaseObjectScanners;
		this.queues=new Queue[hbaseObjectScanners.length];
		for(int i=0;i<queues.length;i++){
			this.queues[i]=new LinkedList<HbaseObject<T>>();
		}
	}

	@Override
	public HbaseObject<T> next() throws Exception {
		int index=0;
		for(int i=0;i<hbaseObjectScanners.length;i++){
			if(queues[i].isEmpty()){
				if(hbaseObjectScanners[i].hasNext()){
					queues[i].offer(hbaseObjectScanners[i].next());
				}else{
					continue;
				}			
			}
			HbaseObject<T> smaller=null;
			while(index<hbaseObjectScanners.length && smaller==null){
				smaller=queues[index].peek();
				if(smaller==null){
					index++;
				}
			}
			HbaseObject<T> head=queues[i].peek();
			String smallerKey=HashHBaseObjectStore.originalKey(smaller.getRowKey());
			String headKey=HashHBaseObjectStore.originalKey(head.getRowKey());
			if(smallerKey.compareTo(headKey)>0){
				index=i;
			}
		}
		
		HbaseObject<T> hbaseObj = queues[index].remove();
		String originalKey = HashHBaseObjectStore.originalKey(hbaseObj.getRowKey());
		hbaseObj.setRowKey(originalKey);
		return hbaseObj;
	}

	@Override
	public List<HbaseObject<T>> next(int num) throws Exception {
		List<HbaseObject<T>> ret=new ArrayList<HbaseObject<T>>();
		for(int i=0;i<num;i++){
			while(hasNext()){
				 ret.add(next());
			}    
		}
		return ret;
	}

	@Override
	public boolean hasNext() throws Exception {
		boolean ret=false;
		for(int i=0;i<hbaseObjectScanners.length;i++){
			ret= ret || hbaseObjectScanners[i].hasNext() || !queues[i].isEmpty();
		}
		return ret;
	}

	@Override
	public void close() throws Exception {
		for(int i=0;i<hbaseObjectScanners.length;i++){
			hbaseObjectScanners[i].close();
		}
	}

}
