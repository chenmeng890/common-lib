package com.yihaodian.common.spring.result;

public class RightResultBean extends ResultBase {
	public RightResultBean(){
		this.err = ResultConstant.SUCC;
		this.kind = ResultConstant.SUCC_KIND;
	}
}
