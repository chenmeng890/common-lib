package com.yihaodian.search.nlp.segment;

//import java.io.File;
import java.util.List;

import com.yihaodian.search.nlp.model.Dictionary;
//import com.yihaodian.search.nlp.model.Lexeme;
import com.yihaodian.search.nlp.segment.MixSegmenter;
import com.yihaodian.search.nlp.segment.ReverseSegmenter;
import com.yihaodian.search.nlp.segment.Segmenter;
import com.yihaodian.search.nlp.segment.SegmenterFactory;
import com.yihaodian.search.nlp.util.PrintSegmenter;

public class TestSegmenterFactory {

//	private String dictpath="D:/workspace/main/search-nlp/src/test/resources/dictionary";
	private String dictpath="/var/www/data/mandy/";
	private String input=new String("三十六计");
	Dictionary dict;
	
	public void init(){
		dict=new Dictionary(dictpath,true);
		dict.setMaxDepth(8);
	}
	
	public void testReverseSegmenter(){
		init();
		Segmenter seg = new ReverseSegmenter(dict, 3, false, false);
		List<String> words=seg.segment(input);
		PrintSegmenter.printList("ReverseSegmenter",words);
//		List<Lexeme> lexemes=seg.segmentComplex(input);
//		PrintSegmenter.printLexemes("ReverseSegmenter",lexemes);
	}
	
	public void testNewDefaultSegmenter(){
		try{
			Segmenter seg=SegmenterFactory.newDefaultSegmenter(dictpath);
			List<String> words=seg.segment(input);
			PrintSegmenter.printList("NewDefaultSegmenter",words);
//			List<Lexeme> lexemes=seg.segmentComplex(input);
//			PrintSegmenter.printLexemes("NewDefaultSegmenter",lexemes);
		}catch (Exception e){
		}
	}
	
	public void testDefaultMixSegmenter(){
		init();
		Segmenter seg=SegmenterFactory.defaultMixSegmenter(dictpath, dict);
		List<String> words=seg.segment(input);
		PrintSegmenter.printList("DefaultMixSegmenter",words);
//		List<Lexeme> lexemes=seg.segmentComplex(input);
//		PrintSegmenter.printLexemes("DefaultMixSegmenter",lexemes);
	}
	
	public void testNewQuerySegmenter(){
		init();
		Segmenter seg=SegmenterFactory.newQuerySegmenter(dict);
		List<String> words=seg.segment(input);
		PrintSegmenter.printList("NewQuerySegmenter",words);
//		List<Lexeme> lexemes=seg.segmentComplex(input);
//		PrintSegmenter.printLexemes("NewQuerySegmenter",lexemes);
	}
	
	public void testMixSegmenter(){
		Segmenter seg=SegmenterFactory.mixSegmenter(
				dictpath, dict, MixSegmenter.FORWARD_SEG, false);
		List<String> words=seg.segment(input);
		PrintSegmenter.printList("MixSegmenter",words);
//		List<Lexeme> lexemes=seg.segmentComplex(input);
//		PrintSegmenter.printLexemes("MixSegmenter",lexemes);
	}
	
	public static void main(String[] args) throws Exception  {
		TestSegmenterFactory test=new TestSegmenterFactory();
		test.testNewQuerySegmenter();
		test.testDefaultMixSegmenter();
	}
}
