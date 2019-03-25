package com.yihaodian.search.nlp.help;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestChineseDigit {
	@Test
	public void isDigit(){
		char c ='百';
		boolean b = ChineseDigit.isDigit(c);
		assertEquals(true,b);
	}
	
	@Test
	public void parseDigit(){
		String target = "三六一";
		int number = ChineseDigit.parseDigit(target.toCharArray(), target.length());
		assertEquals(361,number);
	}
	

}
