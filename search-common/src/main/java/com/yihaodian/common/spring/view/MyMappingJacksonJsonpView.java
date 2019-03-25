package com.yihaodian.common.spring.view;


public class MyMappingJacksonJsonpView extends MyMappingJacksonView{
	
	public static final String DEFAULT_CONTENT_TYPE = "application/javascript";

    @Override
    public String getContentType() {
        return DEFAULT_CONTENT_TYPE;
    }
}
