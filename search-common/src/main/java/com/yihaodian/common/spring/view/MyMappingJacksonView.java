package com.yihaodian.common.spring.view;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import com.yihaodian.common.spring.ParameterTool;



public class MyMappingJacksonView extends MappingJacksonJsonView{
	
	public static final Set<Integer> noContentStatus = new HashSet<Integer>(Arrays.asList(304));
	public static final Set<Integer> processedStatus = new HashSet<Integer>(Arrays.asList(401,404,422,500));
	public static final String DEFAULT_CALLBACK_PARAM = "cb";
	private String callBackParam = DEFAULT_CALLBACK_PARAM;
	
	@Override
	protected Object filterModel(Map<String, Object> model) {
		Set<String> renderedAttributes =
				!CollectionUtils.isEmpty(this.getRenderedAttributes()) ? this.getRenderedAttributes() : model.keySet();
		for (Map.Entry<String, Object> entry : model.entrySet()) {
			if (!(entry.getValue() instanceof BindingResult) && renderedAttributes.contains(entry.getKey())) {
				
				return entry.getValue();
			}
		}
		return null;
	}
	
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		if( response instanceof JsonpResponseWrapper ){
			int status = ((JsonpResponseWrapper) response).getHttpStatus();
         	if( noContentStatus.contains(status) ) {
         		return;
         	} else {
         		super.renderMergedOutputModel(model, request, response);
         	}
		} else {
			super.renderMergedOutputModel(model, request, response);
		}
	}
	
	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String callback = request.getParameter(callBackParam);
        
        if( StringUtils.isEmpty(callback) ) {
        	 super.render(model, request, response);
        } else {
        	
        	if( response instanceof JsonpResponseWrapper ) {
	        	int status = ((JsonpResponseWrapper) response).getHttpStatus();
	         	if( processedStatus.contains(status) ) {
	         		response.setStatus(HttpStatus.OK.value());
	         	} else if ( noContentStatus.contains(status) ) {
	         		super.render(model, request, response);
	         		return;
	         	}
        	}
        	
        	response.getOutputStream().write(new String(URLEncoder.encode(callback, "UTF-8") + "(").getBytes());
            super.render(model, request, response);
            response.getOutputStream().write(new String(");").getBytes());           
        }
    }
	
	@Override
	protected void prepareResponse(HttpServletRequest request, HttpServletResponse response) {
		super.prepareResponse(request, response);
		
		if( !ParameterTool.isResponseCache(request) ) {
			ParameterTool.setResponseNoCache(response);
		}
		
		String callback = request.getParameter(callBackParam);
		if( !StringUtils.isEmpty(callback) ) {
       	 	response.setContentType("text/html; charset=UTF-8");
       }
	}
    
    public void setCallBackParam(String callBackParam){
    	Assert.hasText(callBackParam, "'callBackParam' must not be empty");
		this.callBackParam = callBackParam;
	}
	
}
