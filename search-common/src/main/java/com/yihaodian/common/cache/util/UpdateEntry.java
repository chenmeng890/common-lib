package com.yihaodian.common.cache.util;

/**
 * Entry for update history
 * @author zhouhang
 *
 * @param <PAYLOAD>
 */
public class UpdateEntry<PAYLOAD> {
	private long updateTime;
	private PAYLOAD data;
	
	public UpdateEntry(PAYLOAD data, long updateTime) {
		this.data = data;
		this.updateTime = updateTime;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public PAYLOAD getData() {
		return data;
	}

	public void setData(PAYLOAD data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "UpdateEntry [updateTime=" + updateTime + ", data=" + data + "]";
	}
	
	
}
