package com.yihaodian.common.spring.view;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class JsonpResponseWrapper extends HttpServletResponseWrapper{
	private int status;

	public JsonpResponseWrapper(HttpServletResponse response) {
		super(response);
	}

	@Override
	public void setStatus(int sc) {
		super.setStatus(sc);
		status = sc;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void setStatus(int sc, String sm) {
		super.setStatus(sc, sm);
		status = sc;
	}

	public int getHttpStatus() {
		return status;
	}
}
