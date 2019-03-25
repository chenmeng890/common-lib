package com.yihaodian.common.zk;

import org.junit.Assert;
import org.junit.Test;

public class YHDSearchZKConfigServiceTest {
	
	@Test
	public void getConfigData(){
		String data=YHDSearchZkConfigService.instance.getConfigData("testpath1/testpath2", "openEarth","false");
		System.out.println(new Boolean(data));
	}
	
	
	@Test
	public void updateZkData() throws InterruptedException{
		String data=YHDSearchZkConfigService.instance.getConfigData("testpath1/testpath2", "testConfig7","true");	
		boolean dataBoolean=new Boolean(data);	
		System.out.println(dataBoolean);
		if(dataBoolean){
			data="false";
		}else{
			data="true";
		}
		YHDSearchZkConfigService.instance.updateZKConfig("testpath1/testpath2", "testConfig7", data);
		Thread.sleep(5000l);
		data=YHDSearchZkConfigService.instance.getConfigData("testpath1/testpath2", "testConfig7","true");
		boolean dataBoolean2=new Boolean(data);
		System.out.println(dataBoolean2);
		if(dataBoolean){
			Assert.assertFalse(dataBoolean2);
		}else{
			Assert.assertTrue(dataBoolean2);
		}
	}

}
