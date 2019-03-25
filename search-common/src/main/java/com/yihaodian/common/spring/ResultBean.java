package com.yihaodian.common.spring;



public class ResultBean {
	public static final Integer OK = 0;
	public static final Integer CREATEED = 0;
	public static final Integer NOT_MODIFY = 0;
	public static final Integer UNAUTHORIZED = 401;
	public static final Integer NOT_FOUND = 404;
	public static final Integer INVALID_REQUST = 422;
	public static final Integer SYS_ERROR = 500;
	
	public static final ResultBean SUCCESS_OK = new ResultBean(null, OK,"success");
	
	private Object data;
	
	private Integer err;
	
	private String msg;
	
	public ResultBean() { }
	
	public ResultBean(Object data, Integer err) {
		this.data = data;
		this.err = err;
	}
	
	public ResultBean(Object data, Integer err, String msg) {
		this.data = data;
		this.err = err;
		this.msg = msg;
	}
	
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	public Integer getErr() {
		return err;
	}
	public void setErr(Integer err) {
		this.err = err;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		return "ResultBean [data=" + data + ", err=" + err + "]";
	}
}
