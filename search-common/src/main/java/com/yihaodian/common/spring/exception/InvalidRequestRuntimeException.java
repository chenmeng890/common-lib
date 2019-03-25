package com.yihaodian.common.spring.exception;

import org.springframework.http.HttpStatus;

import com.yihaodian.common.spring.ResultBean;

public class InvalidRequestRuntimeException extends RequestRuntimeException {
	private static final long serialVersionUID = 3568395377136183710L;
	
	public InvalidRequestRuntimeException(String msg) {
		super(msg, ResultBean.INVALID_REQUST, HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	public InvalidRequestRuntimeException(String msg, Throwable cause) {
		super(msg, ResultBean.INVALID_REQUST, HttpStatus.UNPROCESSABLE_ENTITY, cause);
	}
	
	public InvalidRequestRuntimeException(String msg, String reason) {
		super(msg, reason, ResultBean.INVALID_REQUST, HttpStatus.UNPROCESSABLE_ENTITY);
	}
	
	public InvalidRequestRuntimeException(String msg, String reason, Throwable cause) {
		super(msg, reason, ResultBean.INVALID_REQUST, HttpStatus.UNPROCESSABLE_ENTITY, cause);
	}
	
	public InvalidRequestRuntimeException(String msg, int err, HttpStatus httpStatus) {
		super(msg, err, httpStatus);
	}
	
	public InvalidRequestRuntimeException(String msg, int err, HttpStatus httpStatus, Throwable cause) {
		super(msg, err, httpStatus, cause);
	}
	
	public InvalidRequestRuntimeException(String msg, String reason, int err, HttpStatus httpStatus) {
		super(msg, reason, err, httpStatus);
	}
	
	public InvalidRequestRuntimeException(String msg, String reason, int err, HttpStatus httpStatus, Throwable cause) {
		super(msg, reason, err, httpStatus, cause);
	}

}
