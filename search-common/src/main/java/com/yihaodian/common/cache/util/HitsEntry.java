package com.yihaodian.common.cache.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Per frequency access record.
 * 
 * @author zhouhang
 * 
 * @param <PAYLOAD>
 */
public class HitsEntry<PAYLOAD> {

	private final int hitNum;
	private List<PAYLOAD> hitsSet;

	public HitsEntry(int hitNum) {
		this.hitNum = hitNum;
		hitsSet = new ArrayList<PAYLOAD>();
	}

	public int getHitNum() {
		return hitNum;
	}

	public Collection<PAYLOAD> getHitsSet() {
		return hitsSet;
	}

	public boolean isEmpty() {
		return hitsSet.isEmpty();
	}

	public void add(PAYLOAD entry) {
		if (!hitsSet.contains(entry)) {
			hitsSet.add(entry);
		}
	}

	public void remove(PAYLOAD entry) {
		hitsSet.remove(entry);
	}

	@Override
	public String toString() {
		return "HitsEntry, hitNum: " + hitNum + ", hitSet: " + hitsSet;
	}
}
