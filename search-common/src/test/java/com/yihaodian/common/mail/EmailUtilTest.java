package com.yihaodian.common.mail;

import com.yihaodian.common.util.EmailUtil;

public class EmailUtilTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EmailUtil email = new EmailUtil(
				"D:/yihaodian/svn/main/search/search-common/src/test/resources");
		
		email.sendMail("test", "hello", false);
	}

}
