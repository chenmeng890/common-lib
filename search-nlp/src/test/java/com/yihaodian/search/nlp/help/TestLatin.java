package com.yihaodian.search.nlp.help;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestLatin {
    @Test
	public void  testIsLatin() {
		boolean b = Latin.isLatin('8');
		assertEquals(true,b);
		b = Latin.isLatin('s');
		assertEquals(true,b);
	}
    @Test
	public void  testIsDigit() {
		boolean b = Latin.isDigit('8');
		assertEquals(true,b);
		b = Latin.isDigit('s');
		assertEquals(false,b);
	}
    @Test
	public void testIsInteger() {
		boolean b = Latin.isInteger("80");
		assertEquals(true, b);
		b = Latin.isInteger("string");
		assertEquals(false, b);
	}
    @Test
	public void testIsFullwidthInteger() {
		boolean b = Latin.isFullwidthInteger("８０");
		assertEquals(true, b);
		b = Latin.isFullwidthInteger("80");
		assertEquals(false, b);
		b = Latin.isFullwidthInteger("Ｓｔｒｉｎｇ");
		assertEquals(false, b);
	}
    @Test
	public void testIsLetter() {
		boolean b = Latin.isLetter(10);
		assertEquals(false, b);
		b = Latin.isLetter(-10);
		assertEquals(false, b);
	}

    @Test
	public void testIsLatinString() {
		boolean b = Latin.isLatinString("spellcheck",false);
		assertEquals(true, b);
		b = Latin.isLatinString("Ｅｎｇｌｉｓｈ",true);
		assertEquals(true, b);
		b = Latin.isLatinString("中国",true);
		assertEquals(false, b);
	}

	
	
	@Test
	public void testIsCJKCharacter(){
		boolean b = Latin.isCJKCharacter('s');
		assertEquals(false, b);
		b =Latin.isCJKCharacter('中');
		assertEquals(true, b);
	}
	
	@Test
	public void testRegularize(){
        char c = Latin.regularize('S');
        assertEquals('s', c);
        c = Latin.regularize('Ｓ');
        assertEquals('S', c);
	}
	
	@Test
	public void testToDBC() {
	     String str = Latin.toDBC("ｅｎｇｌｉｓｈ　ｓｐｅｌｌｃｈｅｃｋ，１００-200");
	     assertEquals("english spellcheck,100-200", str);
	 }
}
