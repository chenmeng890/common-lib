package com.yihaodian.common.cache.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Update history
 * 
 * This class is not thread-safe.
 * @author zhouhang
 * 
 * @param <PAYLOAD>
 */
public class UpdateMap<PAYLOAD> {
	// history list, order by update time, in descending order.
	private ListEntry<UpdateEntry<PAYLOAD>> updateHead;

	// history map, to find and update the history quickly
	private Map<PAYLOAD, ListEntry<UpdateEntry<PAYLOAD>>> updateMap;

	public UpdateMap() {
		updateMap = new HashMap<PAYLOAD, ListEntry<UpdateEntry<PAYLOAD>>>();
	}

	/**
	 * Record update
	 * 
	 * @param data
	 * @param updateTime
	 */
	public void recordUpdate(PAYLOAD data, long updateTime) {
		ListEntry<UpdateEntry<PAYLOAD>> entry = updateMap.get(data);
		if (entry == null) {
			UpdateEntry<PAYLOAD> ue = new UpdateEntry<PAYLOAD>(data, updateTime);
			entry = new ListEntry<UpdateEntry<PAYLOAD>>(ue);
		} else {
			entry.getObject().setUpdateTime(updateTime);
			removeEntry(entry);
		}

		addNewEntry(entry);
		
		updateMap.put(data, entry);
	}

	/**
	 * Remove update history
	 * 
	 * @param data
	 */
	public void removeUpdate(PAYLOAD data) {
		ListEntry<UpdateEntry<PAYLOAD>> entry = updateMap.get(data);
		if (entry != null) {
			removeEntry(entry);
			updateMap.remove(data);
		}
	}

	/**
	 * Get updates before sepcified time
	 * 
	 * @param ts
	 * @return
	 */
	public List<PAYLOAD> getBefore(long ts) {
		List<PAYLOAD> list = new ArrayList<PAYLOAD>();

		if(updateHead == null) {
			return list;
		}
		ListEntry<UpdateEntry<PAYLOAD>> eldestUpdate = updateHead.getBefore();

		while (true) {
			long lastUpdateTime = eldestUpdate.getObject().getUpdateTime();

			if (lastUpdateTime <= ts) {
				PAYLOAD data = eldestUpdate.getObject().getData();
				list.add(data);
			} else {
				break;
			}

			if (eldestUpdate == updateHead) {
				break;
			} else {
				eldestUpdate = eldestUpdate.getBefore();
			}
		}

		return list;
	}

	/**
	 * Get oldest entry time.
	 * 
	 * @return
	 */
	public long getOldestTime() {
		if (updateHead != null) {
			return updateHead.getBefore().getObject().getUpdateTime();
		} else {
			// The eldest possible time might be inserted after now.
			return System.currentTimeMillis();
		}
	}

	/**
	 * Clear all data
	 */
	public void clear() {
		updateMap.clear();

		clearList();
	}

	public void dump() {
		StringBuilder sb = new StringBuilder();
		sb.append("UpdateMap, map : " + updateMap);
		System.out.println(sb.toString());
		
		dumpList();
	}

	// ////////////////////////////////////////////////////////////////////
	// Update list operations.
	// ////////////////////////////////////////////////////////////////////

	/**
	 * Add new entry to list
	 * 
	 * @param entry
	 */
	private void addNewEntry(ListEntry<UpdateEntry<PAYLOAD>> entry) {
		ListEntry<UpdateEntry<PAYLOAD>> iter = updateHead;
		if (iter == null) {
			updateHead = entry;
			return;
		}

		long ut = entry.getObject().getUpdateTime();

		while (true) {
			if (ut >= iter.getObject().getUpdateTime()) {
				entry.addBefore(iter);
				if (iter == updateHead) {
					updateHead = entry;
				}
				break;
			} else {
				// find next entry
				iter = iter.getAfter();
				// we have finished checking all entry, the target is the
				// oldest, so add it to the tail.
				if (iter == updateHead) {
					entry.addBefore(iter);
				}
			}
		}

	}

	private void removeEntry(ListEntry<UpdateEntry<PAYLOAD>> entry) {

		if (updateHead == entry) {
			if (entry.getAfter() == entry) {
				updateHead = null;
			} else {
				updateHead = entry.getAfter();
			}
		}
		entry.remove();
	}

	private void clearList() {
		if (updateHead == null) {
			return;
		}

		ListEntry<UpdateEntry<PAYLOAD>> iter = updateHead.getBefore();
		while (true) {
			if (iter == updateHead) {
				updateHead = null;
				break;
			} else {
				ListEntry<UpdateEntry<PAYLOAD>> next = iter.getBefore();
				iter.remove();
				iter = next;
			}
		}
	}

	private void dumpList() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("update list: ");
		sb.append(updateHead);
		if (updateHead != null) {
			ListEntry<UpdateEntry<PAYLOAD>> iter = updateHead.getAfter();
			while (true) {
				if (iter == updateHead) {					
					break;
				} else {
					ListEntry<UpdateEntry<PAYLOAD>> next = iter.getAfter();
					sb.append(iter).append(", ");
					iter = next;
				}
			}
		}
		
		System.out.println(sb.toString());
	}

}
