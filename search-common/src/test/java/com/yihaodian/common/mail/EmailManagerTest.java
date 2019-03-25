package com.yihaodian.common.mail;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.yihaodian.common.mail.EmailManager.ALERT_LEVEL;

public class EmailManagerTest {

	EmailManager em = null;

	@Before
	public void setup() throws IOException {
		String config = "/var/www/webapps/config/ycc/snapshot/yihaodian_search-data-service";

		ClassPathTemplateLoader loader = new ClassPathTemplateLoader(null,
				(String[]) null);
		em = new EmailManager(config, loader);
	}

//	@Test
//	public void testAlertMail() {
//		em.sendAlert(ALERT_LEVEL.WARN, "index failed",
//				"failed by unkown reason ");
//	}

}
