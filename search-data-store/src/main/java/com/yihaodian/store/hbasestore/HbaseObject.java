package com.yihaodian.store.hbasestore;

public class HbaseObject<T> {
	private String rowKey;
	private long timestamp;
	private T data;
	public HbaseObject(String rowKey, long timestamp, T data) {
		super();
		this.rowKey = rowKey;
		this.timestamp = timestamp;
		this.data = data;
	}
	public String getRowKey() {
		return rowKey;
	}
	public void setRowKey(String rowKey) {
		this.rowKey = rowKey;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return "WrappedData [rowKey=" + rowKey + ", timestamp=" + timestamp
				+ ", data=" + data + "]";
	}
}
