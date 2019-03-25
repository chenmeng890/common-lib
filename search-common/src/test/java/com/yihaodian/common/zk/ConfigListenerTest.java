package com.yihaodian.common.zk;

import org.junit.Assert;
import org.junit.Test;

import com.yihaodian.common.zk.ZkConfigService.ConfigChangeListener;

public class ConfigListenerTest {
	boolean receiveNofity = false;
	
	@Test
	public void testListener() throws Exception{
		YHDSearchZkConfigService configService = YHDSearchZkConfigService.instance;
		
		
		
		configService.addConfigChangeListener("test", "listenerTest", new ConfigChangeListener() {
			@Override
			public void onConfigChange(String configValue) {
				receiveNofity = true;
			}
		});
		Thread.sleep(1000 * 3);
		
		configService.updateZKConfig("test", "listenerTest", "1");
		
		Thread.sleep(1000 * 3);
		
		Assert.assertTrue(receiveNofity);
	}
}
