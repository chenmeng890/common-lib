package com.yihaodian.common.util;

import org.junit.Assert;
import org.junit.Test;

public class NetUtilTest {

	@Test
	public void test() {
		String localIp = NetUtil.getLocalIP();
		//System.out.println(localIp);
		Assert.assertNotNull(localIp);
		Assert.assertNotSame("127.0.0.1", localIp);
		String localAddress = NetUtil.localAddress();
		Assert.assertNotNull(localAddress);
	}

}
