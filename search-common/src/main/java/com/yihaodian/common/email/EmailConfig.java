package com.yihaodian.common.email;

import java.util.Properties;

import com.yihaodian.common.email.AlterLevelManager.ALERT_LEVEL_EM;

public class EmailConfig {

	private Properties mailProp;  //邮箱基本信息配置
	
	private String describe; //描述
	
	private String alterItem; //项目
	
	private ALERT_LEVEL_EM alterLevel; //邮件级别
	
	private ALERT_LEVEL_EM alterThreshold; //报警级别
	
	private String content; //正文
	
	private String msgTo; //收件人
	
	private String msgCc; //抄送
	
	public EmailConfig(Properties mailProp) {
		this.mailProp = mailProp;
	}
	
	public Properties getMailProp() {
		return mailProp;
	}

	public void setMailProp(Properties mailProp) {
		this.mailProp = mailProp;
	}
	public String getDescribe() {
		return describe;
	}
	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getAlterItem() {
		return alterItem;
	}

	public void setAlterItem(String alterItem) {
		this.alterItem = alterItem;
	}

	public ALERT_LEVEL_EM getAlterLevel() {
		return alterLevel;
	}

	public void setAlterLevel(ALERT_LEVEL_EM alterLevel) {
		this.alterLevel = alterLevel;
	}

	public ALERT_LEVEL_EM getAlterThreshold() {
		return alterThreshold;
	}

	public void setAlterThreshold(ALERT_LEVEL_EM alterThreshold) {
		this.alterThreshold = alterThreshold;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getMsgTo() {
		return msgTo;
	}

	public void setMsgTo(String msgTo) {
		this.msgTo = msgTo;
	}

	public String getMsgCc() {
		return msgCc;
	}

	public void setMsgCc(String msgCc) {
		this.msgCc = msgCc;
	}
}
