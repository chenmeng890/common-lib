package com.yihaodian.search.nlp;

import junit.framework.Assert;

import com.yihaodian.search.nlp.help.PinYinHelper;

public class TestPinYinHelper {

	private PinYinHelper helper = PinYinHelper.getInstance();
	
	public void testPinYin(){
		String word="嗳呵";
		String[] spell=helper.cn2Spell(word);
		StringBuilder sb=new StringBuilder();
		for(String s:spell)
			sb.append(s);
		Assert.assertEquals("aihe",sb.toString());
	}
	
	public static void main(String[] args) {
		TestPinYinHelper test=new TestPinYinHelper();
		test.testPinYin();
	}
}
