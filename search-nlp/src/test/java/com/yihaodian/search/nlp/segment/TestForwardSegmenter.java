package com.yihaodian.search.nlp.segment;

import java.util.List;

import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;
import com.yihaodian.search.nlp.segment.ForwardSegmenter;
import com.yihaodian.search.nlp.util.PrintSegmenter;

public class TestForwardSegmenter {
	ForwardSegmenter seg;
	public void init(){
//		String dictpath="D:/workspace/main/search-nlp/src/test/resources/dictionary";
		String dictpath ="/var/www/data/mandy/";
		Dictionary dict=new Dictionary(dictpath,true);
		seg=new ForwardSegmenter(dict);
	}
	
	public void testSegment(){
		if(seg==null){
			init();
		}
		String input="孫子兵法三十六計 2010hanakimi 冬装抢新 经典时尚假2件保暖修身打底裙裤Q5376";
			List<Lexeme> les=seg.segmentComplex(input);
			System.out.println("原文："+input);
			PrintSegmenter.printLexemes("最大切词", les);
			
			input="hanakimi 2010冬装新款 七彩羊毛保暖内衣男款B9383";
			les=seg.segmentComplex(input);
			System.out.println("原文："+input);
			PrintSegmenter.printLexemes("最大切词", les);
		
	}
	
	public static void main(String[] args) {
		TestForwardSegmenter test = new TestForwardSegmenter();
		test.init();
		test.testSegment();
	}
}