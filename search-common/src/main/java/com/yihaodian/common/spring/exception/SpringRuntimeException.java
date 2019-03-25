package com.yihaodian.common.spring.exception;


public class SpringRuntimeException extends RuntimeException{

	private static final long serialVersionUID = 4777011887086274817L;
	
	private String reason;

	public SpringRuntimeException(String msg) {
		this(msg, "");
	}
	
	public SpringRuntimeException(String msg, Throwable cause) {
		this(msg, "", cause);
	}
	
	public SpringRuntimeException(String msg, String reason) {
		super(msg);
		this.reason = reason;
	}
	
	public SpringRuntimeException(String msg, String reason, Throwable cause) {
		super(msg, cause);
		this.reason = reason;
	}

	public String getReason() {
		return reason;
	}
}
