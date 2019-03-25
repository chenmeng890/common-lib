package com.yihaodian.common.spring.exception;

import org.springframework.http.HttpStatus;


public class RequestRuntimeException extends SpringRuntimeException{

	private static final long serialVersionUID = 4777011887086274817L;
	
	private final String reason;
	
	private final int err;
	private final HttpStatus httpStatus;

	public RequestRuntimeException(String msg, int err, HttpStatus httpStatus) {
		this(msg, "", err, httpStatus);
	}
	
	public RequestRuntimeException(String msg, int err, HttpStatus httpStatus, Throwable cause) {
		this(msg, "", err, httpStatus, cause);
	}
	
	public RequestRuntimeException(String msg, String reason, int err, HttpStatus httpStatus) {
		super(msg);
		this.reason = reason;
		this.err = err;
		this.httpStatus = httpStatus;
	}
	
	public RequestRuntimeException(String msg, String reason, int err, HttpStatus httpStatus, Throwable cause) {
		super(msg, cause);
		this.reason = reason;
		this.err = err;
		this.httpStatus = httpStatus;
	}

	public String getReason() {
		return reason;
	}

	public int getErr() {
		return err;
	}

	public HttpStatus getHttpStatus() {
		return httpStatus;
	}
}
