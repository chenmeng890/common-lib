package com.yihaodian.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Logger;


public class EmailUtil {
	private final static Logger log = Logger.getLogger(EmailUtil.class);

	String propDir = null;
	Properties mailProp = null;

	String localIp = null;

	/**
	 * Load config from propDir
	 * @param propDir
	 */
	public EmailUtil(String propDir) {
		this.propDir = propDir;
		init();
	}
	
	/**
	 * Load config from properties
	 * @param mailProperties
	 */
	public EmailUtil(Properties mailProp){
		this.mailProp = mailProp;
		init();
	}

	void init() {
		this.localIp = NetUtil.getLocalIP();
		if (mailProp == null) {
			mailProp = new Properties();
			InputStream is = null;
			try {
				is = new FileInputStream(new File(propDir, "/mail.properties"));
				if (is != null) {
					mailProp.load(is);
				}
			} catch (Exception e) {
				log.error("load mail.properties error!", e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException ex) {
						log.error("", ex);
					}
				}
			}
		}
	}
	
	public void sendMail(String title, String message, boolean cc)
	{
		String host = mailProp.getProperty("mail.smtp.host");
		String msgFrom = mailProp.getProperty("mail.msgFrom");
		if (title == null) {
			title = mailProp.getProperty("mail.title1");
		}

		String name = mailProp.getProperty("mail.name");
		String password = mailProp.getProperty("mail.password");
		String msgTo = mailProp.getProperty("mail.msgTo");
		String msgCC = mailProp.getProperty("mail.msgCC");
		String sender = "send from: " + this.localIp;
		title = title + ", " + sender;
		String content = message + "</br>" + sender;
		

		try {
			// 建立会话
			mailProp.put("mail.smtp.auth", "true");
			Session session = Session.getInstance(mailProp);
			Message msg = new MimeMessage(session); // 建立信息
			// 解析发件
			msgTo = msgTo.replaceAll(",", ";");
			String[] msgTos = msgTo.split(";");
			Address[] addresses = new Address[msgTos.length];
			for (int i = 0; i < msgTos.length; i++) {
				addresses[i] = new InternetAddress(msgTos[i], false);
			}
			msg.setFrom(new InternetAddress(msgFrom)); // 发件
			msg.setRecipients(Message.RecipientType.TO, addresses); // 收件
			msg.setSentDate(new Date()); // 日期
			msg.setSubject(title); // 主题
			
			// 解析
			if (cc) {
				msgCC = msgCC.replaceAll(",", ";");
				if (msgCC != null && !msgCC.isEmpty()) {
					String[] msgCCs = msgCC.split(";");
					Address[] addresseCCs = new Address[msgCCs.length];
					for (int i = 0; i < msgCCs.length; i++) {
						addresseCCs[i] = new InternetAddress(msgCCs[i], false);
					}
					msg.addRecipients(RecipientType.CC, addresseCCs);//
				}
			}
		
			Multipart mm = new MimeMultipart();

			BodyPart mdp = new MimeBodyPart();
			mdp.setContent(content, "text/html;charset=utf-8");
			mm.addBodyPart(mdp);
			msg.setContent(mm);
			Transport tran = session.getTransport("smtp");
			tran.connect(host, name, password);
			tran.sendMessage(msg, msg.getAllRecipients()); //
			tran.close();
			log.info("send email success ");
		} catch (Exception e) {
			log.error("EmailAlert�?send mail exception!", e);
		}
	}

	public void sendMail(String title, String message) {
		this.sendMail(title, message, true);
	}

}
