package com.yihaodian.common.spring.result;

import org.apache.commons.httpclient.HttpStatus;

public class ResultConstant {
	public static final Integer SUCC = 0;
	public static final String SUCC_KIND = "yhdsearch#success";
	public static final String ERR_KIND = "yhdsearch#error";
	
	public static final RightResultBean RIGHT_RESULT = new RightResultBean();
	
	public static final Integer OVER_THREASHOLD_ERR_CODE = 101;
	public static final String OVER_THREASHOLD_ERR_DATA = "over threashold error";
	public static final WrongResultBean OVER_THREASHOLD_RESULT = new WrongResultBean(OVER_THREASHOLD_ERR_CODE, ERR_KIND, OVER_THREASHOLD_ERR_DATA);
	
	public static final String SC_INTERNAL_SERVER_ERROR_DATA = "server internal error";
	public static final WrongResultBean SYS_ERR_RESULT = new WrongResultBean(HttpStatus.SC_INTERNAL_SERVER_ERROR, ERR_KIND, SC_INTERNAL_SERVER_ERROR_DATA);
	public static final String NOT_FOUND_ERR_DATA = "not found";
	public static final WrongResultBean NOT_FOUND_RESULT = new WrongResultBean(HttpStatus.SC_NOT_FOUND, ERR_KIND, NOT_FOUND_ERR_DATA);
	
	public static final String UNPROCESSABLE_ENTITY_ERR_DATA = "unprocessable entity";
	public static final WrongResultBean UNPROCESSABLE_RESULT = new WrongResultBean(HttpStatus.SC_UNPROCESSABLE_ENTITY, ERR_KIND, UNPROCESSABLE_ENTITY_ERR_DATA);
	
	public static final String SC_UNAUTHORIZED_ERR_DATA = "unauthorized";
	public static final WrongResultBean UNAUTHORIZED_RESULT = new WrongResultBean(HttpStatus.SC_UNAUTHORIZED, ERR_KIND, SC_UNAUTHORIZED_ERR_DATA);

}
