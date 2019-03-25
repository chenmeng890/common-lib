package com.yihaodian.common.email;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.yihaodian.common.email.AlterLevelManager.ALERT_LEVEL_EM;

/**
 * 邮件发送类
 * @author xuchaoqun
 *
 */
public class EmailSender {
	private static final Logger log = Logger.getLogger(EmailSender.class);
	
	/**
	 * 发送邮件接口
	 * @param describe 描述
	 * @param alterItem 项目
	 * @param alterLevel 邮件级别
	 * @param alterThreshold 报警阈值
	 * @param content 内容
	 * @param msgTo 收件人
	 * @param msgCc 抄送
	 *  @param mailProp 邮箱配置
	 * @return 是否发送成功
	 */
	@Deprecated
	public static boolean emailSender(String describe, String alterItem, ALERT_LEVEL_EM alterLevel, 
			ALERT_LEVEL_EM alterThreshold, String content, String msgTo, String msgCc, Properties mailProp) {
		if (AlterLevelManager.isNeedSend(alterLevel, alterThreshold)) {//需要发送邮件
			return emailSender(describe, alterItem, alterLevel, content,  msgTo, msgCc, mailProp);
		} else {
			log.info("当前邮件级别低于阈值，不发送");
		}
		return false;
	}
	
	public static boolean emailSender(EmailConfig emailConfig) {
		Properties mailProp = emailConfig.getMailProp();
		if (mailProp == null) {
			log.error("邮箱配置为空，发送失败");
			return false;
		}
		
		if (!checkMailProp(mailProp)) {
			log.error("邮箱配置不正确，发送失败");
			return false;
		}
		
		String msgTo = emailConfig.getMsgTo();
		String msgCc = emailConfig.getMsgCc();
		if (AlterLevelManager.isNeedSend(emailConfig.getAlterLevel(), emailConfig.getAlterThreshold())) {//需要发送邮件
			return emailSender(emailConfig.getDescribe(), emailConfig.getAlterItem(), 
					emailConfig.getAlterLevel(), emailConfig.getContent(), msgTo, msgCc, emailConfig.getMailProp());
		} else {
			log.info("当前邮件级别低于阈值，不发送");
		}
		return false;
	}
	
	private static boolean emailSender(String describe, String alterItem, ALERT_LEVEL_EM alterLevel, 
			String content, String msgTo, String msgCc, Properties mailProp) {
		try {
			if (StringUtils.isEmpty(msgTo)) {
				log.error("收件人为空，发送失败");
				return false;
			} else {
				mailProp.setProperty("mail.msgTo", msgTo);
				if (StringUtils.isEmpty(msgCc)) {
					mailProp.setProperty("mail.msgCc", msgTo);
				} else {
					mailProp.setProperty("mail.msgCc", msgCc);
				}
			}
			
			EmailUtil mailUtil = new EmailUtil(mailProp);
			StringBuilder title = new StringBuilder();
			
			if (!StringUtils.isEmpty(alterLevel.toString())) {
				title.append("[").append(alterLevel).append("]");
			}
			if (!StringUtils.isEmpty(alterItem)) {
				title.append("[").append(alterItem).append("]");
			}
			if (!StringUtils.isEmpty(describe)) {
				title.append("[").append(describe).append("]");
			}
			
			if (StringUtils.isEmpty(msgCc)) {
				return mailUtil.sendMail(title.toString(), content, false);
			} else {
				return mailUtil.sendMail(title.toString(), content, true);
			}
		} catch (Exception e) {
			log.error("发送失败");
		}
		
		return false;
	}
	
	/**
	 * 检查邮箱配置
	 * @param mailProp
	 * @return 配置是否完全
	 */
	private static boolean checkMailProp(Properties mailProp) {
		if (mailProp == null || mailProp.isEmpty()) {
			return false;
		}
		String host = mailProp.getProperty("mail.smtp.host");
		String msgFrom = mailProp.getProperty("mail.msgFrom");
		String name = mailProp.getProperty("mail.name");
		String password = mailProp.getProperty("mail.password");
		
		if (StringUtils.isEmpty(host) || StringUtils.isEmpty(msgFrom) ||
				StringUtils.isEmpty(name) || StringUtils.isEmpty(password)) {
			return false;
		}
		return true;
	}
}
