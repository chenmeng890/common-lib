package com.yihaodian.search.nlp;

import java.io.IOException;
import java.util.List;

import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.segment.MixSegmenter;
import com.yihaodian.search.nlp.segment.ReverseSegmenter;
import com.yihaodian.search.nlp.segment.Segmenter;
import com.yihaodian.search.nlp.segment.SegmenterFactory;
import com.yihaodian.search.nlp.segment.WordSliceSegmenter;

public class TestSegmenter {
	
	private static String dictpath="/var/www/data/mandy/";
	private static Dictionary dict=new Dictionary(dictpath,true);
	
	public static void testNewQuerySegemter(){
		Segmenter segmenter = SegmenterFactory.newQuerySegmenter(dict);
		String str="东芝笔记本";
		List<String> strlist =segmenter.segment(str);
		System.out.println(strlist.toString());
	}
	
	public static void testDefaultMixSegmenter(){
		Segmenter seg=SegmenterFactory.mixSegmenter(dictpath, dict, MixSegmenter.FORWARD_SEG, false);
		String str="东芝笔记本";
		List<String> strlist =seg.segment(str);
		System.out.println(strlist.toString());
	}
	
	public static void testReverseSegmenter(){
		
		ReverseSegmenter seg = new ReverseSegmenter(dict, ReverseSegmenter.TYPE_LATIN_ORIGINAL, true,true,true);
//		WordSliceSegmenter segmenter=new WordSliceSegmenter(seg);
		
		String str="蓝罐曲奇  好奇金装";
		/*分词的简洁模式*/
		List<String> list =seg.segment(str);
//		List<String> list=segmenter.sliceSegment(strlist);
		if(list==null)
			System.out.println("error!");
		else{
			for(String le:list)
				System.out.println(le);
		}
		System.out.println("****************************************************");
		ReverseSegmenter seg1 = new ReverseSegmenter(dict);
		list = seg1.segment(str);
		if(list==null)
			System.out.println("error!");
		else{
			for(String le:list)
				System.out.println(le);
		}
	}
	
	public static void testSplitSynonymsWords(){
		Segmenter tempSegmenter = SegmenterFactory.newQuerySegmenter(dict);
		System.out.println(tempSegmenter.splitSynonymsWords("三星", true));
		System.out.println(tempSegmenter.splitSynonymsWords("苹果", true));
		System.out.println(tempSegmenter.splitSynonymsWords("索尼", true));
		System.out.println(tempSegmenter.splitSynonymsWords("sony", true));
		System.out.println("------------------");
		System.out.println(tempSegmenter.splitSynonymsWords("三星", false));
		System.out.println(tempSegmenter.splitSynonymsWords("苹果", false));
		System.out.println(tempSegmenter.splitSynonymsWords("索尼", false));
		System.out.println(tempSegmenter.splitSynonymsWords("sony", false));
	}

	public static void main(String[] args) throws IOException {
		testDefaultMixSegmenter();
//		testReverseSegmenter();
		testNewQuerySegemter();
		testSplitSynonymsWords();
	}
}
