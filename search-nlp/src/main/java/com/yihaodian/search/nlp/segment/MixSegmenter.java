package com.yihaodian.search.nlp.segment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.yihaodian.search.nlp.help.Latin;
import com.yihaodian.search.nlp.model.Dictionary;
import com.yihaodian.search.nlp.model.Lexeme;
import com.yihaodian.search.nlp.model.SimpleDictionary;
import com.yihaodian.search.nlp.segment.lucene.MiniSegmentation;

public class MixSegmenter extends Segmenter {
	
	private static Logger log = Logger.getLogger(MixSegmenter.class);
	public static final int FORWARD_SEG = 1; //正向最大匹配
	
	public static final int REVERSE_SEG = 2; //逆向最大匹配
	
	public static final int FORWORD_REVERSE_SEG = 3; //双向最大匹配
	
	public static final int SLICE_FORWARD_SEG = 4; //正向最大+N-gram
	
	public static final int SLICE_REVERSE_SEG = 5; //逆向最大+N-gram
	
	public static final int REVERSE_SEG_RP = 6; //逆向最大匹配,保留标点

	private List<Segmenter> segmenters;
		
	private boolean isNeedMiniSeg = false; //是否需要再细切
	
	public MixSegmenter(Dictionary dictionary ,List<Segmenter> segmenters,boolean isNeedMiniSeg){
		this.dictionary = dictionary;
		this.segmenters = segmenters;
		this.isNeedMiniSeg = isNeedMiniSeg;
	}
	
	@Override
	public List<String> segment(String sentence) {
		//用其他 (正向或逆向或其他)分词器进行切分，保存到set中
		 if(sentence==null || sentence.length()==0){
				return null;
		 }
		if(segmenters.size()==1 && !isNeedMiniSeg){
			return segmenters.get(0).segment(sentence);
		}
		Set<String> set = new HashSet<String>();
		if(segmenters.size()>0){
			for(Segmenter seg:segmenters){
				List<String> list = seg.segment(sentence);
				set.addAll(list);
			}
		}
/*		Iterator<Entry<String,SimpleDictionary>> iter=dictionary.getMainDictMap().entrySet().iterator();
		while(iter.hasNext()){
			Entry<String,SimpleDictionary> entry=iter.next();
		SimpleDictionary simDict=entry.getValue();
		MiniSegmentation ms = new MiniSegmentation(simDict);
		try{
			List<String> list = ms.segment(sentence);
			if(list.size()>0)
				set.addAll(list);
		}catch (IOException e) {
			throw new RuntimeException("MiniSegmentation error!"+sentence +" -- "+e.getMessage());
			}	
		}*/
		//对于前面分词完后长度大于2的词，如果需要再细粒度的切分，调用MiniSegmentation进行切分
		if(isNeedMiniSeg && set.size()>0){
			Set<String> words = new HashSet<String>();
			Iterator<Entry<String,SimpleDictionary>> iter=dictionary.getMainDictMap().entrySet().iterator();
			while(iter.hasNext()){
				Entry<String,SimpleDictionary> entry=iter.next();
//				System.out.println(entry.getKey());
				SimpleDictionary simDict=entry.getValue();
				MiniSegmentation ms = new MiniSegmentation(simDict);
				try {
					for(String s:set){
						if(s.length()>2&&!Latin.isLatinString(s, true)){
							List<String> list = ms.segment(s);
							if(list.size()>0){
								ms.clearContext();
								words.addAll(list);
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					log.error(e.getMessage());
				}	
			}
			set.addAll(words);
		}
		//分词完后的词按照在原句中的位置顺序排序
		List<Word> list = new ArrayList<Word>();
		for(String s:set){
			Word w = new Word(s,sentence.toLowerCase());
			list.add(w);
		}
		Collections.sort(list, new WordComparator());
		List<String> wordList = new ArrayList<String>();
		for(Word w:list){ wordList.add(w.getText());}
		return wordList;
	}

	@Override
	public List<Lexeme> segmentComplex(String sentence) {
		if(sentence==null || sentence.length()==0){
				return null;
		}
		if(segmenters.size()==1 && !isNeedMiniSeg){
			return segmenters.get(0).segmentComplex(sentence);
		}
		Set<Lexeme> set = new HashSet<Lexeme>();
		if(segmenters.size()>0){
			for(Segmenter seg:segmenters){
				List<Lexeme> list = seg.segmentComplex(sentence);
				set.addAll(list);
			}
		}
		if(isNeedMiniSeg && set.size()>0){
			Set<Lexeme> words = new HashSet<Lexeme>();
			for(Lexeme s:set){
				if(s.getText().length()>2){
					Iterator<Entry<String,SimpleDictionary>> iter=dictionary.getMainDictMap().entrySet().iterator();
					while(iter.hasNext()){
						Entry<String,SimpleDictionary> entry=iter.next();
						SimpleDictionary simDict=entry.getValue();
						MiniSegmentation ms = new MiniSegmentation(simDict);
						try {
							List<String> list = ms.segment(s.getText());
							for(String str:list){
								words.add(new Lexeme(str));
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							log.error(e.getMessage());
						}	
					}
				}
			}
			set.addAll(words);
		}
		Map<String,Lexeme> map= new HashMap<String,Lexeme>();
		List<Word> list = new ArrayList<Word>();
		for(Lexeme l:set){
			map.put(l.getText(), l);
			Word w = new Word(l.getText(),sentence.toLowerCase());
			list.add(w);
		}
		Collections.sort(list, new WordComparator());
		List<Lexeme> wordList = new ArrayList<Lexeme>();
		for(Word w:list) {wordList.add(map.get(w.getText()));}
		return wordList;
	}

}

class Word{
	private String text;
	
	private int index;
	
	private int length;
	
	Word(String text,String target){
		this.text = text;
		this.length = text.length();
		this.index = target.indexOf(text)>=0?target.indexOf(text):10000+target.indexOf(text);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		if(index<0) {
			this.index = 10000+index;
		}
		this.index = index;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}
	
}

class WordComparator implements Comparator<Word>{

	public int compare(Word o1, Word o2) {
		if(o1.getIndex()!=o2.getIndex()) return o1.getIndex()-o2.getIndex();
		if(o1.getIndex()==o2.getIndex()){
			return o1.getLength()-o2.getLength();
		}
		return 0;		
	}

}
