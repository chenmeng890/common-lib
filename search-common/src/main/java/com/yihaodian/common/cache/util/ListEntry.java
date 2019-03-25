package com.yihaodian.common.cache.util;

import com.yihaodian.common.cache.util.ListEntry;

/**
 * Simple linked list entry
 * @author zhouhang
 *
 */
public class ListEntry<T>{
	private T object;
	private ListEntry<T> before;
	private ListEntry<T> after;

	public ListEntry(T object) {
		this.object = object;
		this.before = this;
		this.after = this;
	}
	
	public T getObject(){
		return object;
	}
	
	public void addBefore(ListEntry<T> entry){
		if(entry != null){
			after = entry;
			before = entry.before;
			after.before = this;
			before.after = this;
		}
	}

	public void addAfter(ListEntry<T> entry){
		if(entry != null){
			before = entry;
			after = entry.after;			
			after.before = this;
			before.after = this;
		}
	}
	
	public void remove(){
		before.after = after;
		after.before = before;
		before = this;
		after = this;
	}
	
	@Override
	public String toString(){
		return object.toString();
	}

	public ListEntry<T> getBefore() {
		return before;
	}

	public void setBefore(ListEntry<T> before) {
		this.before = before;
	}

	public ListEntry<T> getAfter() {
		return after;
	}

	public void setAfter(ListEntry<T> after) {
		this.after = after;
	}
	
}