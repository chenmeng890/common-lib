package com.yihaodian.common.cache.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hits map. Not thread-safe.
 * 
 * @author zhouhang
 * 
 * @param <PAYLOAD>
 */
public class HitsMap<PAYLOAD> {

	private Map<Integer, ListEntry<HitsEntry<PAYLOAD>>> map = new HashMap<Integer, ListEntry<HitsEntry<PAYLOAD>>>();

	private Map<PAYLOAD, ListEntry<HitsEntry<PAYLOAD>>> plMap = new HashMap<PAYLOAD, ListEntry<HitsEntry<PAYLOAD>>>();

	/**
	 * Record access
	 * 
	 * @param pl
	 * @param hits
	 */
	public void recordAccess(PAYLOAD pl, int hits) {
		if (hits == 0) {
			return;
		}
		
		int hitNum = hits;

		ListEntry<HitsEntry<PAYLOAD>> entry = plMap.get(pl);
		if (entry != null) {
			// Remove payload from previous entry
			entry.getObject().remove(pl);
			// remove the hitsEntry if it's empty
			if (entry.getObject().isEmpty()) {
				removeFromList(entry);				
				map.remove(entry.getObject().getHitNum());
			}

			// Find new entry (if any)
			hitNum += entry.getObject().getHitNum();
			entry = map.get(hitNum);
		}

		if (entry == null) {
			// create new entry
			HitsEntry<PAYLOAD> he = new HitsEntry<PAYLOAD>(hitNum);
			entry = new ListEntry<HitsEntry<PAYLOAD>>(he);

			// add new entry
			addToList(entry);
			map.put(hitNum, entry);
		}

		// Add payload to new entry
		entry.getObject().add(pl);
		// update payload map
		plMap.put(pl, entry);
	}

	/**
	 * Remove hits for payload
	 * 
	 * @param pl
	 */
	public void remove(PAYLOAD pl) {
		if (pl == null) {
			return;
		}
		ListEntry<HitsEntry<PAYLOAD>> entry = plMap.remove(pl);
		if (entry != null) {
			// Remove payload from previous entry
			entry.getObject().remove(pl);
			// remove the hitsEntry if it's empty
			if (entry.getObject().isEmpty()) {
				removeFromList(entry);
			}
		}

	}

	public void clear() {
		if (map == null) {
			return;
		}
		map.clear();
		map = null;

		// clear list
		clearList();
	}

	/**
	 * Get top/bottom hits
	 * 
	 * @param count
	 * @param bottom
	 * @return
	 */
	public List<PAYLOAD> getHits(int count, boolean bottom) {

		// Empty, return null;
		if (hitsHead == null) {
			return null;
		}

		List<PAYLOAD> list = new ArrayList<PAYLOAD>();

		ListEntry<HitsEntry<PAYLOAD>> iter = hitsHead;
		if (bottom) {
			iter = iter.getBefore(); // get the least frequent used items
		}
		while (true) {
			// Get current payload
			Collection<PAYLOAD> set = iter.getObject().getHitsSet();
			if (set != null) {
				if(list.size() + set.size() <= count) {
					list.addAll(set);
				} else {
					for (PAYLOAD entry : set) {
						list.add(entry);
						if (list.size() >= count) {
							break;
						}
					}
				}
			}

			if (list.size() >= count) {
				break;
			}

			// Get next payload
			if (bottom) {
				if (iter == hitsHead) {
					break;
				}
				iter = iter.getBefore();
			} else {
				iter = iter.getAfter();
				// Finished the loop
				if (iter == hitsHead) {
					break;
				}
			}
		}

		return list;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();

		sb.append("HitsMap: ");

		ListEntry<HitsEntry<PAYLOAD>> iter = hitsHead;
		while (true) {
			if (iter == null) {
				break;
			}

			sb.append(iter.toString()).append("\n");

			// Get next payload
			iter = iter.getAfter();
			// Finished the loop
			if (iter == hitsHead) {
				break;
			}
		}

		return sb.toString();
	}

	// //////////////////////////////////////////////////////////////////////////////
	// List Operations.
	// //////////////////////////////////////////////////////////////////////////////
	/**
	 * The hits list, sorted by hits number, in decreasing order.
	 */
	private ListEntry<HitsEntry<PAYLOAD>> hitsHead;

	// Add new entry to list
	private void addToList(ListEntry<HitsEntry<PAYLOAD>> entry) {
		if (entry == null) {
			return;
		}

		if (hitsHead == null) {
			hitsHead = entry;
			return;
		}

		// Insert to the right position
		int hitNum = entry.getObject().getHitNum();

		// Start from the tail (with largest hit number)
		ListEntry<HitsEntry<PAYLOAD>> iter = hitsHead.getBefore();
		while (true) {
			// finished the loop, and reached the head
			if (iter == hitsHead) {
				if (iter.getObject().getHitNum() < hitNum) {
					entry.addBefore(hitsHead);
					hitsHead = entry;
				} else {
					entry.addAfter(iter);
				}
				break;
			} else {
				if (iter.getObject().getHitNum() < hitNum) {
					iter = iter.getBefore();
				} else {
					entry.addAfter(iter);
					break;
				}
			}
		}
	}

	// Remove from list
	private void removeFromList(ListEntry<HitsEntry<PAYLOAD>> entry) {
		if (entry == null) {
			return;
		}

		if (hitsHead == null) {
			return;
		}

		if (hitsHead == entry) {
			if (entry.getAfter() != entry) {
				hitsHead = entry.getAfter();
			} else {
				hitsHead = null;
			}
		}

		entry.remove();
	}

	// Clear list
	private void clearList() {
		if (hitsHead == null) {
			return;
		}

		ListEntry<HitsEntry<PAYLOAD>> iter = hitsHead.getBefore();
		while (true) {
			if (iter == hitsHead) {
				hitsHead = null;
				break;
			} else {
				ListEntry<HitsEntry<PAYLOAD>> next = iter.getBefore();
				iter.remove();
				iter = next;
			}
		}
	}

}
