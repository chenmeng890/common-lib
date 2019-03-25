package com.yihaodian.common.mail;

import java.util.Arrays;
import java.util.Map;

import org.apache.log4j.Logger;

import com.yihaodian.common.util.EmailUtil;

import freemarker.cache.TemplateLoader;

public class EmailManager {
	static Logger log = Logger.getLogger(EmailManager.class);

	public static enum ALERT_LEVEL {
		INFO, EXCEPTION, WARN, FATAL
	}

	TemplateManager templateManager;

	EmailUtil mailSender;

	public EmailManager(String mailPropDir, TemplateLoader loader) {
		templateManager = new TemplateManager(loader);

		mailSender = new EmailUtil(mailPropDir);
	}

	/**
	 * send alert using template.
	 * 
	 * @param templateName
	 * @param title
	 * @param root
	 */
	public void sendAlert(ALERT_LEVEL level, String templateName, String title,
			Map<String, Object> root) {
		String content = null;
		try {
			if (templateManager != null) {
				content = templateManager.process(templateName, root);
			} else {
				content = root.toString();
			}
		} catch (Exception e) {
			log.error("send mail error " + title + " " + root, e);
		}

		title = formTitle(level, title);
		content = formContent(content);
		
		if(log.isDebugEnabled()) {
			log.debug(content);
		}
		mailSender.sendMail(title, content, level != ALERT_LEVEL.INFO);
	}

	/**
	 * send alert
	 * 
	 * @param level
	 * @param title
	 * @param content
	 */
	public void sendAlert(ALERT_LEVEL level, String title, String content) {
		title = formTitle(level, title);
		content = formContent(content);
		mailSender.sendMail(title, content, level != ALERT_LEVEL.INFO);
	}

	String formTitle(ALERT_LEVEL level, String title) {
		return title = "[" + level.name() + "] " + title;
	}

	String formContent(String content) {
		content = content + "</br>" + "Alert Level:"
				+ Arrays.toString(ALERT_LEVEL.values());

		return content;
	}
}
