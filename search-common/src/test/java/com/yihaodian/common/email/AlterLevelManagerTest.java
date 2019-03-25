package com.yihaodian.common.email;

import org.junit.Assert;
import org.junit.Test;

import com.yihaodian.common.email.AlterLevelManager.ALERT_LEVEL_EM;

import junit.framework.TestCase;

public class AlterLevelManagerTest extends TestCase {
	@Test
	public void test(){
		Assert.assertTrue(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.INFO, ALERT_LEVEL_EM.INFO));
		Assert.assertTrue(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.EXCEPTION, ALERT_LEVEL_EM.INFO));
		Assert.assertTrue(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.WARN, ALERT_LEVEL_EM.INFO));
		Assert.assertTrue(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.FATAL, ALERT_LEVEL_EM.INFO));
		
		Assert.assertFalse(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.INFO, ALERT_LEVEL_EM.EXCEPTION));
		Assert.assertTrue(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.EXCEPTION, ALERT_LEVEL_EM.EXCEPTION));
		Assert.assertTrue(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.WARN, ALERT_LEVEL_EM.EXCEPTION));
		Assert.assertTrue(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.FATAL, ALERT_LEVEL_EM.EXCEPTION));
		
		Assert.assertFalse(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.INFO, ALERT_LEVEL_EM.WARN));
		Assert.assertFalse(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.EXCEPTION, ALERT_LEVEL_EM.WARN));
		Assert.assertTrue(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.WARN, ALERT_LEVEL_EM.WARN));
		Assert.assertTrue(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.FATAL, ALERT_LEVEL_EM.WARN));
		
		Assert.assertFalse(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.INFO, ALERT_LEVEL_EM.FATAL));
		Assert.assertFalse(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.EXCEPTION, ALERT_LEVEL_EM.FATAL));
		Assert.assertFalse(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.WARN, ALERT_LEVEL_EM.FATAL));
		Assert.assertTrue(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.FATAL, ALERT_LEVEL_EM.FATAL));
		
		Assert.assertTrue(AlterLevelManager.isNeedSend(null, null));
		Assert.assertTrue(AlterLevelManager.isNeedSend(null, ALERT_LEVEL_EM.FATAL));
		Assert.assertTrue(AlterLevelManager.isNeedSend(ALERT_LEVEL_EM.FATAL, null));
	}
}
