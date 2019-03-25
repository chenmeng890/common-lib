package com.yihaodian.search.nlp.help;

import static org.junit.Assert.*;

import org.junit.Test;

import com.yihaodian.search.nlp.help.PinYinHelper;

public class TestPinYinHelper {

	private PinYinHelper helper = PinYinHelper.getInstance();
	@Test
	public void testPinYin(){
		String word="养乐多";
		String[] spell=helper.cn2Spell(word);
		StringBuilder sb=new StringBuilder();
		for(String s:spell)
			sb.append(s);
		assertEquals("yangleduo",sb.toString());
	}
	
}
