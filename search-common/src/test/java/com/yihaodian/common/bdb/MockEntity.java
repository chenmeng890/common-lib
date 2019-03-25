package com.yihaodian.common.bdb;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

@Entity(version=0) 
public class MockEntity {
	@PrimaryKey
	private String k;
	
	private String v;
	
	private MockEntity(){
	}
	
	public MockEntity(String k, String v){
		this.k = k;
		this.v = v;
	}

	public String getK() {
		return k;
	}

	public void setK(String k) {
		this.k = k;
	}

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}
	
	
}