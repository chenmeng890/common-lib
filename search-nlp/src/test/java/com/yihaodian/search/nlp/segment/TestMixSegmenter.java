package com.yihaodian.search.nlp.segment;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.yihaodian.search.nlp.model.Dictionary;


public class TestMixSegmenter {
	
	@Test
   public void testMixSeg(){
//	String dictpath="D:/workspace/main/search-nlp/src/test/resources/dictionary";
	String dictpath ="/var/www/data/mandy/";
	Dictionary dict=new Dictionary(dictpath,true);
	ForwardSegmenter seg =new ForwardSegmenter(dict);
	List<Segmenter> seglist = new ArrayList<Segmenter>();
	seglist.add(seg);
	MixSegmenter mixSeg = new MixSegmenter(dict, seglist, true);
	String sentence ="中华人民共和国";
	List<String> words = mixSeg.segment(sentence);
	List<String> ret =new ArrayList<String>();
	ret.add("中华");
	ret.add("人民");
	ret.add("共和");
	ret.add("共和国");
	assertEquals(words,ret);
   }
}
