package com.yihaodian.common.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.yihaodian.common.email.AlterLevelManager.ALERT_LEVEL_EM;

import junit.framework.TestCase;

public class EmailSenderTest extends TestCase {
	
	@Test
	public void testNomarl() throws IOException {
		String describe = "测试描述";
		String alterItem = "测试项目";
		ALERT_LEVEL_EM alterLevel = ALERT_LEVEL_EM.FATAL;
		ALERT_LEVEL_EM alterThreshold = ALERT_LEVEL_EM.WARN;
		String content = "测试内容";
		
		InputStream is = null;
		is = new FileInputStream(new File(EmailSenderTest.class.getClassLoader().getResource("email.properties").getFile()));
		
		Properties mailProp = new Properties();
		if (is != null) {
			mailProp.load(is);
		}
		//Assert.assertTrue(EmailSender.emailSender(describe, alterItem, alterLevel, alterThreshold, content, msgTo, msgCc, mailProp));
		
		EmailConfig emailConfig = new EmailConfig(mailProp);
		emailConfig.setAlterItem(alterItem);
		emailConfig.setDescribe(describe);
		emailConfig.setAlterLevel(alterLevel);
		emailConfig.setAlterThreshold(alterThreshold);
		emailConfig.setContent(content);
		emailConfig.setMsgTo("mailuser1@yhd.cn");
		Assert.assertTrue(EmailSender.emailSender(emailConfig));
	}
}
