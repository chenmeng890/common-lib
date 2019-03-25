package com.yihaodian.common.spring.result;

public class WrongResultBean extends ResultBase {
	private String data;
	private String reason;
	
	public WrongResultBean(Integer err, String kind, String data){
		this(err, kind, data, "");
	}
	
	public WrongResultBean(Integer err, String kind, String data, String reason){
		this.err = err;
		this.kind = kind;
		this.data = data;
		this.reason = reason;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getData() {
		return data;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
