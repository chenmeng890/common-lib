package com.yihaodian.common.spring.result;

public abstract class ResultBase {
	protected String kind;
	protected Integer err;

	public Integer getErr() {
		return err;
	}

	public void setErr(Integer err) {
		this.err = err;
	}
	
	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}
}
