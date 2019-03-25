package com.yihaodian.common.zk;

import com.yihaodian.common.util.JmxUtil;

public class ZkConfigServiceManager implements ZkConfigServiceManagerMBean{
	
	ZkConfigService zkConfigService;
	
	protected ZkConfigServiceManager(ZkConfigService zkConfigService){
		this.zkConfigService=zkConfigService;
		JmxUtil.registerMBean(this);
	}

	@Override
	public String getConfig(String path, String configName) {
		return zkConfigService.takeConfigData(path, configName);
	}

	@Override
	public void updateConfigToZk(String path, String configName, String data) {
		zkConfigService.updateZKConfig(path, configName, data);
	}

	@Override
	public void updateConfigToLocal(String path, String configName, String data) {
		zkConfigService.updateLocalConfig(path, configName, data);
	}

}
