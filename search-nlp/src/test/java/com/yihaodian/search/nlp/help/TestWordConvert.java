package com.yihaodian.search.nlp.help;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestWordConvert {
	WordConvert convert = null;
    @Before   
	public void setup(){
		convert = WordConvert.getInstance();
	}
    
    @Test
    public void testBig52gb() throws Exception{
    	String input = convert.big52gb("手機，；電腦");
    	assertEquals("手机，；电脑", input);
    }
    
    @Test
    public void testComplexToSimple() throws Exception{
    	String input = convert.complexToSimple("手機，；電腦");
    	assertEquals("手机,;电脑", input);
    	}
}
