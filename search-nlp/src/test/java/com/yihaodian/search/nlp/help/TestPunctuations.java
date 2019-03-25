package com.yihaodian.search.nlp.help;

import static org.junit.Assert.*;

import org.junit.Test;



public class TestPunctuations{
    
	@Test
	public void testIsPunctuation(){
		String s=",";
		boolean b = Punctuations.isPunctuation(s);
		assertEquals(true, b);
		s ="string"; 
		b = Punctuations.isPunctuation(s);
		assertEquals(false, b);
	}
}
