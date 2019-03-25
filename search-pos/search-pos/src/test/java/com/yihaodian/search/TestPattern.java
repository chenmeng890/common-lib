package com.yihaodian.search;

import java.util.regex.Pattern;

public class TestPattern {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Pattern p = Pattern.compile(".*(电脑|数码|电配件).*");
		String ts = "0-950560-950656-17077-17080:手机数码电脑、家电-电脑产品-电脑配件-电脑附件";
		System.out.println(p.matcher(ts).matches());
	}

}
