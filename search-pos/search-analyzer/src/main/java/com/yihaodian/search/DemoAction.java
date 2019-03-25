package com.yihaodian.search;

import com.opensymphony.xwork2.Action;

public class DemoAction implements Action {
	private DemoService demoService;
	public void setDemoService(DemoService value) {
		this.demoService = value;
	}

	public DemoService getDemoService() {
		return this.demoService;
	}
	
	private int maxBrandId;
	public int getMaxBrandId() {
		return maxBrandId;
	}
	
	public String execute() throws Exception {
		System.out.println("TestAction.execute(...)");
		maxBrandId = demoService.maxBrandId();
		return Action.SUCCESS;
	}

}
