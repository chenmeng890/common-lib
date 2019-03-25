package com.yihaodian.common.zk;

public interface ZkConfigServiceManagerMBean {
	
	public String getConfig(String path, String configName);
	
	public void updateConfigToZk(String path, String configName,String data);
	
	public void updateConfigToLocal(String path, String configName,String data);

}
